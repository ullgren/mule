/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

public class XmlFilterFunctionalTestCase extends AbstractXmlFunctionalTestCase
{
    public static final int MAX_COUNT = 100;
    public static final String STRING_MESSAGE = "Hello world";

    private static final Logger LOGGER = getLogger(XmlFilterFunctionalTestCase.class);

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/module/xml/xml-filter-functional-test-service.xml"},
            {ConfigVariant.FLOW, "org/mule/module/xml/xml-filter-functional-test-flow.xml"}});
    }

    public XmlFilterFunctionalTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Test
    public void testNotXml() throws Exception
    {
        LOGGER.debug("not xml");
        MuleClient client = muleContext.getClient();
        client.dispatch("in", STRING_MESSAGE, null);
        MuleMessage response = client.request("notxml", TIMEOUT);
        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals(STRING_MESSAGE, response.getPayloadAsString());
    }

    @Test
    public void testOther() throws Exception
    {
        LOGGER.debug("other");
        doTestXml("other", getResourceAsString("org/mule/issues/many-sends-mule-1758-test-service.xml"));
    }

    @Test
    public void testSelf() throws Exception
    {
        LOGGER.debug("self");
        doTestXml("self", getConfigAsString());
    }

    public void doTestXml(String endpoint, String xml) throws Exception
    {
        MuleClient client = muleContext.getClient();
        client.dispatch("in", xml, null);
        MuleMessage response = client.request(endpoint, TIMEOUT * 2);
        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals(xml, response.getPayloadAsString());
    }

    @Test
    public void testMany() throws Exception
    {
        Random random = new Random();
        for (int i = 0; i < MAX_COUNT; ++i)
        {
            switch (random.nextInt(3))
            {
                case 0 :
                    testNotXml();
                    break;
                case 1 :
                    testOther();
                    break;
                case 2 :
                    testSelf();
                    break;
                default :
                    throw new IllegalStateException("Bad case");
            }
        }
    }
}
