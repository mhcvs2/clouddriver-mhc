/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.cats.dynomite;

import com.netflix.dyno.connectionpool.exception.DynoException;
import com.netflix.spinnaker.kork.dynomite.DynomiteClientDelegate;
import net.jodah.failsafe.RetryPolicy;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class DynomiteUtils {

    private DynomiteUtils() {
    }

    public static RetryPolicy greedyRetryPolicy(long delayMs) {
        return new RetryPolicy()
                .retryOn(Arrays.asList(
                        JedisException.class,
                        DynoException.class,
                        DynomiteClientDelegate.ClientDelegateException.class
                ))
                .withDelay(delayMs, TimeUnit.MILLISECONDS)
                .withMaxRetries(3);
    }
}
