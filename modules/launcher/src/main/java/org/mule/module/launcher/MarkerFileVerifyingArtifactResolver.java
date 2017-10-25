/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.launcher;

import java.io.File;

import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.i18n.MessageFactory;
import org.mule.util.FilenameUtils;

/**
 *  This is an example of a ArtifactResolver that also includes some verification 
 *  logic. In this case it checks that the artifact contains a file called MagicFile 
 *  before allowing it to be deployed.
 *
 **/
public class MarkerFileVerifyingArtifactResolver extends DefaultArtifactResolver {
	
	protected transient final Log logger = LogFactory.getLog(getClass());

	@Override
	public File resolve(URL artifactUrl) throws Exception {
		
		File archive = super.resolve(artifactUrl);
		
        try (ZipFile zip = new ZipFile(archive)) {
            for (Enumeration<?> entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                logger.info("Entry : " + entry.getName());
                if ( "MagicFile".equals(entry.getName()) ) {
                	return archive;
                }
            }
        }
		throw new DeploymentInitException(
                MessageFactory.createStaticMessage("Mule artifact does not conatin the MagicFile: " + 
                		FilenameUtils.getBaseName(artifactUrl.toString())));
	}

}
