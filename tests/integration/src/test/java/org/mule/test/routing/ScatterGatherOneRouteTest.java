/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.routing;

import org.mule.api.MuleContext;
import org.mule.api.config.ConfigurationException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.routing.ScatterGatherRouter;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.yourkit.util.Asserts;

public class ScatterGatherOneRouteTest extends AbstractMuleTestCase
{
    private MuleContext context;

    @Before
    public void before() throws InitialisationException, ConfigurationException
    {
        context = new DefaultMuleContextFactory().createMuleContext();
    }

    @After
    public void after()
    {
        if (context != null)
        {
            context.dispose();
        }
    }

    @Test(expected = ConfigurationException.class)
    public void oneRouteOnXml() throws Exception
    {
        SpringXmlConfigurationBuilder builder = new SpringXmlConfigurationBuilder("scatter-gather-one-route-test.xml");
        builder.configure(context);
    }

    @Test(expected = InitialisationException.class)
    public void oneRouteProgramatically() throws Exception
    {
        ScatterGatherRouter sc = new ScatterGatherRouter();
        sc.setRoutes(Collections.<MessageProcessor>emptyList());

        try
        {
            sc.initialise();
        }
        catch (InitialisationException e)
        {
            Asserts.assertTrue(e.getCause() instanceof IllegalStateException);
            throw e;
        }
    }
}
