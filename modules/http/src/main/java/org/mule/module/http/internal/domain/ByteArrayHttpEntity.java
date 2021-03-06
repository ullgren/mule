/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.http.internal.domain;

import org.mule.module.http.internal.domain.response.StreamedHttpEntity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Represents a byte array entity message.
 */
public class ByteArrayHttpEntity implements StreamedHttpEntity
{

    private byte[] content;

    public ByteArrayHttpEntity(byte[] content)
    {
        this.content = content;
    }

    public byte[] getContent()
    {
        return this.content;
    }

    @Override
    public InputStream getInputStream()
    {
        return new ByteArrayInputStream(content);
    }
}
