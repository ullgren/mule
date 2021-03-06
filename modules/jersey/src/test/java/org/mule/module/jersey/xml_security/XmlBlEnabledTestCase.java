/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.jersey.xml_security;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mule.module.http.api.client.HttpRequestOptionsBuilder.newOptions;
import static org.mule.util.xmlsecurity.XMLSecureFactories.EXPAND_ENTITIES_PROPERTY;
import org.mule.api.MuleMessage;
import org.mule.api.client.OperationOptions;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class XmlBlEnabledTestCase extends XmlBlBase
{

    // this is disabled (secure) by default, so we need to change it for the test
    @Rule
    public final SystemProperty expandEntities = new SystemProperty(EXPAND_ENTITIES_PROPERTY, "true");

    @Test
    public void expandsEntitiesWhenEnabled() throws Exception
    {
        MuleMessage testMuleMessage = getTestMuleMessage(TEST_MESSAGE);
        testMuleMessage.setPayload(xmlWithEntities);
        testMuleMessage.setProperty("Content-Type", "application/xml");

        OperationOptions options = newOptions().method("POST").build();
        MuleMessage result = client.send(format("http://localhost:%d/service/customer", port.getNumber()), testMuleMessage, options);

        assertThat(result.getPayloadAsString(), not(containsString("password")));
        assertThat(result.getPayloadAsString(), containsString("0101"));
    }
}


