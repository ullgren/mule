/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.launcher;

import java.io.File;
import java.net.URL;

public class DefaultArtifactResolver implements ArtifactResolver {

	@Override
	public File resolve(URL artifactUrl) throws Exception {
        // normalize the full path + protocol to make unzip happy
		return new File(artifactUrl.toURI());
	}

}
