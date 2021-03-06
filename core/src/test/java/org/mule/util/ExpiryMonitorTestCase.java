/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.util;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.probe.JUnitProbe;
import org.mule.tck.probe.PollingProber;
import org.mule.util.monitor.Expirable;
import org.mule.util.monitor.ExpiryMonitor;

public class ExpiryMonitorTestCase extends AbstractMuleTestCase
{
    
    private static final int EXPIRE_TIME = 300;
    private static final int EXPIRE_INTERVAL = 100;
    // Add some time to account for the durations of the expiry process, since it is scheduled with fixed delay
    private static final int EXPIRE_TIMEOUT = EXPIRE_TIME + EXPIRE_INTERVAL + 100;
    private static final long DELTA_TIME = 10;

    private boolean expired = false;
    private long expiredTime = -1;
    
    private ExpiryMonitor monitor;

    @Before
    public void doSetUp() throws Exception
    {
        expired = false;
        monitor = new ExpiryMonitor("test", EXPIRE_INTERVAL, null, false);
    }

    @After
    public void after()
    {
        monitor.dispose();
    }

    @Test
    public void testExpiry() throws InterruptedException
    {
        final Expirable e = new Expirable()
        {
            @Override
            public void expired()
            {
                expire();
            }
        };
        monitor.addExpirable(EXPIRE_TIME, MILLISECONDS, e);
        new PollingProber(EXPIRE_TIMEOUT, 50).check(new JUnitProbe()
        {
            @Override
            public boolean test()
            {
                assertThat(expired, is(true));
                assertThat(monitor.isRegistered(e), is(false));
                return true;
            }
        });
    }

    @Test
    public void testNotExpiry() throws InterruptedException
    {
        final Expirable e = new Expirable()
        {
            @Override
            public void expired()
            {
                expire();
            }
        };
        long startTime = currentTimeMillis();
        monitor.addExpirable(EXPIRE_TIME, MILLISECONDS, e);
        monitor.run();
        assertThat(expired, is(false));
        new PollingProber(EXPIRE_TIMEOUT, 50).check(new JUnitProbe()
        {
            @Override
            public boolean test()
            {
                assertThat(expired, is(true));
                assertThat(monitor.isRegistered(e), is(false));
                return true;
            }
        });
        assertThat(expiredTime - startTime, greaterThanOrEqualTo(EXPIRE_TIME - DELTA_TIME));
    }

    @Test
    public void testExpiryWithReset() throws InterruptedException
    {
        final Expirable e = new Expirable()
        {
            @Override
            public void expired()
            {
                expire();
            }
        };
        monitor.addExpirable(EXPIRE_TIME, MILLISECONDS, e);
        monitor.run();
        assertThat(expired, is(false));
        long startTime = currentTimeMillis();
        monitor.resetExpirable(e);
        monitor.run();
        assertTrue(!expired);
        new PollingProber(600, 50).check(new JUnitProbe()
        {
            @Override
            public boolean test()
            {
                assertThat(expired, is(true));
                assertThat(monitor.isRegistered(e), is(false));
                return true;
            }
        });
        assertThat(expiredTime - startTime, greaterThanOrEqualTo(EXPIRE_TIME - DELTA_TIME));
    }

    @Test
    public void testNotExpiryWithRemove() throws InterruptedException
    {
        final Expirable e = new Expirable()
        {
            @Override
            public void expired()
            {
                expire();
            }
        };
        monitor.addExpirable(EXPIRE_TIME, MILLISECONDS, e);
        monitor.run();
        assertThat(expired, is(false));
        monitor.removeExpirable(e);
        
        Thread.sleep(EXPIRE_TIMEOUT);
        assertThat(expired, is(false));
        assertThat(monitor.isRegistered(e), is(false));
    }

    private void expire()
    {
        expiredTime = currentTimeMillis();
        expired = true;
    }
}
