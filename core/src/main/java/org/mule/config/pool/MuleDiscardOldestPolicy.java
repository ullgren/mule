/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.pool;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;

/**
 * The <code>MuleDiscardOldestPolicy</code> allows the rejection strategy
 * to execute custom logic that is needed in the context where the
 * rejected work is discarded according to the DISCARD_OLDEST strategy.
 */
public class MuleDiscardOldestPolicy extends DiscardOldestPolicy
{
    /**
     * Drops a task in the waiting queue and retries
     *
     * @param r the runnable task requested to be executed
     * @param e the executor attempting to execute this task
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
    {
        if (!e.isShutdown())
        {
            Runnable previous = e.getQueue().poll();
            if (previous != null && previous instanceof CancellableRunnable)
            {
                try
                {
                    ((CancellableRunnable) previous).cancel();
                }
                catch (Exception ex)
                {
                    // Don't do anything
                }
            }
            e.execute(r);
        }
    }
}
