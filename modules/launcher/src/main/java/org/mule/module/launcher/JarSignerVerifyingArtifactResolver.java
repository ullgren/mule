/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.LoggerFactory;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;

/**
 *  This is ArtifactResolver checks that the Mule application has been signed using jarsigner and verifies the signature 
 *  before allowing the artifact to be deployed.
 *  
 *  To enable add the following to wrapper.conf
 *  
 *  <pre>
 *  wrapper.java.additional.<n>=-Dmule.artifactResolver=org.mule.module.launcher.JarSignerVerifyingArtifactResolver 
 *  wrapper.java.additional.<n>=-Dmule.jarsigner.keystore=/path/to/keystore-user-to-sign.jks
 *  </pre>
 *
 **/
public class JarSignerVerifyingArtifactResolver extends DefaultArtifactResolver {
	
    private static final String META_INF = "META-INF/";
    // prefix for new signature-related files in META-INF directory
    private static final String SIG_PREFIX = META_INF + "SIG-";
    private static final String JKS = "jks";
    
    protected static final String ARTIFACT_RESOLVER_KEYSTORE_PROPERTY = "mule.jarsigner.keystore";
    protected static final String ARTIFACT_RESOLVER_KEYSTORETYPE_PROPERTY = "mule.jarsigner.keystoretype";
    protected static final String ARTIFACT_RESOLVER_REQUIRE_ALL_SIGNED_PROPERTY = "mule.jarsigner.requireAllSigned";
    
	protected transient final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean requireAllSigned = true;

	private CertificateFactory certificateFactory;

	private CertPathValidator validator;
	
	private PKIXParameters pkixParameters;
	
	public JarSignerVerifyingArtifactResolver() {
		try {
			String keystore = System.getProperty(ARTIFACT_RESOLVER_KEYSTORE_PROPERTY);
			String keystoretype = System.getProperty(ARTIFACT_RESOLVER_KEYSTORETYPE_PROPERTY, JKS);
			this.requireAllSigned = BooleanUtils.toBoolean(System.getProperty(ARTIFACT_RESOLVER_REQUIRE_ALL_SIGNED_PROPERTY, "true"));
			this.certificateFactory = CertificateFactory.getInstance("X.509");
			this.validator = CertPathValidator.getInstance("PKIX");
			loadKeyStore(keystore, keystoretype);
		} catch (CertificateException | NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to load keystore.", e);
		}
	}

	@Override
	public File resolve(URL artifactUrl) throws Exception {
		byte[] buffer = new byte[8192];
		
		File archive = super.resolve(artifactUrl);
        logger.info("Verify " + archive.getAbsolutePath());
		try(JarFile jf = new JarFile(archive, true)) {
	        Enumeration<JarEntry> entries = jf.entries();
	        while (entries.hasMoreElements()) {
	            JarEntry je = entries.nextElement();
	            InputStream is = null;
	            try {
	                is = jf.getInputStream(je);
	                while (is.read(buffer, 0, buffer.length) != -1) {
	                    // we just read. this will throw a SecurityException
	                    // if  a signature/digest check fails.
	                }
	            } finally {
	                if (is != null) {
	                    is.close();
	                }
	            }
	            if (this.requireAllSigned ) {
	            	CodeSigner[] signers = je.getCodeSigners();
	            	boolean isSigned = (signers != null);	            
	            	if  ( requireAllSigned && !je.isDirectory() && !isSigned && !signatureRelated(je.getName()) ) {
	            		throw new SecurityException("AArtifact contains resource [" + je.getName() + "] that is not signed.");
	            	}
	            	if (isSigned) {
                        for (CodeSigner signer: signers) {
                        	// Signer Validate certificate ...
                        	verifySignerInfo(signer);
                        }
                    }
	            }
	        }
		}
		
		return archive;
	}
	
    /**
     * signature-related files include:
     * . META-INF/MANIFEST.MF
     * . META-INF/SIG-*
     * . META-INF/*.SF
     * . META-INF/*.DSA
     * . META-INF/*.RSA
     * . META-INF/*.EC
     */
    private boolean signatureRelated(String name) {
        String ucName = name.toUpperCase(Locale.ENGLISH);
        if (ucName.equals(JarFile.MANIFEST_NAME) ||
            ucName.equals(META_INF) ||
            (ucName.startsWith(SIG_PREFIX) &&
                ucName.indexOf("/") == ucName.lastIndexOf("/"))) {
            return true;
        }

        if (ucName.startsWith(META_INF) &&
            isBlockOrSF(ucName)) {
            // .SF/.DSA/.RSA/.EC files in META-INF subdirs
            // are not considered signature-related
            return (ucName.indexOf("/") == ucName.lastIndexOf("/"));
        }

        return false;
    }
    
    /**
     * Utility method used by JarVerifier and JarSigner to determine the signature file names and PKCS7 block files names that are supported
     *
     * @param  s file name
     * @return <code>true</code> if the input file name is a supported Signature File or PKCS7 block file name
    */
    public static boolean isBlockOrSF(String s) {
    	// we currently only support DSA and RSA PKCS7 blocks
       if (s.endsWith(".SF") || s.endsWith(".DSA") || s.endsWith(".RSA")) {
        return true;
       }
       return false;
    }

    /**
     * Verifies the singer
     * @throws CertificateException 
     * @throws InvalidAlgorithmParameterException 
     * @throws CertPathValidatorException 
     */
    private void verifySignerInfo(CodeSigner signer) throws CertificateException, CertPathValidatorException, InvalidAlgorithmParameterException {
    	List<? extends Certificate> certs = signer.getSignerCertPath().getCertificates();
        
        CertPath cp = certificateFactory.generateCertPath(certs);
        validator.validate(cp, pkixParameters);
    }
    
    void loadKeyStore(String keyStoreName, String storetype) {

        try {
            
            Set<TrustAnchor> tas = new HashSet<>();
            try {
                KeyStore caks = getCacertsKeyStore();
                if (caks != null) {
                    Enumeration<String> aliases = caks.aliases();
                    while (aliases.hasMoreElements()) {
                        String a = aliases.nextElement();
                        try {
                            tas.add(new TrustAnchor((X509Certificate)caks.getCertificate(a), null));
                        } catch (Exception e2) {
                            // ignore, when a SecretkeyEntry does not include a cert
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore, if cacerts cannot be loaded
            }
            
            try {
	            KeyStore store = KeyStore.getInstance(storetype);
	            if (keyStoreName == null) {
	            	store.load(null, null);
	            } else {
	            
	                keyStoreName = keyStoreName.replace(File.separatorChar, '/');
	                URL url = null;
	                try {
	                    url = new URL(keyStoreName);
	                } catch (java.net.MalformedURLException e) {
	                    // try as file
	                    url = new File(keyStoreName).toURI().toURL();
	                }
	                InputStream is = null;
	                try {
	                    is = url.openStream();
	                    store.load(is, null);
	                } finally {
	                    if (is != null) {
	                        is.close();
	                    }
	                }
	            }
                Enumeration<String> aliases = store.aliases();
                while (aliases.hasMoreElements()) {
                    String a = aliases.nextElement();
                    try {
                        X509Certificate c = (X509Certificate)store.getCertificate(a);
                        // Only add TrustedCertificateEntry and self-signed
                        // PrivateKeyEntry
                        if (store.isCertificateEntry(a) ||
                                c.getSubjectDN().equals(c.getIssuerDN())) {
                            tas.add(new TrustAnchor(c, null));
                        }
                    } catch (Exception e2) {
                        // ignore, when a SecretkeyEntry does not include a cert
                    }
                }
            } finally {
                try {
                    pkixParameters = new PKIXParameters(tas);
                    pkixParameters.setRevocationEnabled(false);
                } catch (InvalidAlgorithmParameterException ex) {
                    // Only if tas is empty
                }
            }
        } catch (IOException | NoSuchAlgorithmException ioe) {
            throw new RuntimeException("Unable to load keystore. " +
                                        ioe.getMessage());
        } catch (java.security.cert.CertificateException ce) {
            throw new RuntimeException("Certificate exception." +
                                        ce.getMessage());
        } catch (KeyStoreException kse) {
            throw new RuntimeException("Unable to instantiate keystore class. " +
                kse.getMessage());
        }
    }
    
    /**
     * Returns the keystore with the configured CA certificates.
     */
    private KeyStore getCacertsKeyStore()
        throws Exception
    {
        String sep = File.separator;
        File file = new File(System.getProperty("java.home") + sep
                             + "lib" + sep + "security" + sep
                             + "cacerts");
        if (!file.exists()) {
            return null;
        }
        KeyStore caks = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            caks = KeyStore.getInstance(JKS);
            caks.load(fis, null);
        }
        return caks;
    }


}
