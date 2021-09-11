/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.mock.common;

import org.apache.dubbo.mock.common.exception.LifecycleException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Define a resource for test.
 */
class Resource implements Lifecycle {

    /**
     * Record the init count of this resource.
     */
    private AtomicInteger initCount = new AtomicInteger(0);

    /**
     * Record the start count of this resource.
     */
    private AtomicInteger startCount = new AtomicInteger(0);

    /**
     * Record the destroy count of this resource.
     */
    private AtomicInteger destroyCount = new AtomicInteger(0);

    /**
     * Returns the init count of this resource.
     */
    public int getInitCount() {
        return initCount.get();
    }

    /**
     * Returns the start count of this resource.
     */
    public int getStartCount() {
        return startCount.get();
    }

    /**
     * Returns the destroy count of this resource.
     */
    public int getDestroyCount() {
        return destroyCount.get();
    }

    @Override
    public void init() throws LifecycleException {
        this.initCount.incrementAndGet();
    }

    @Override
    public boolean start() throws LifecycleException {
        this.startCount.incrementAndGet();
        return true;
    }

    @Override
    public void destroy() throws LifecycleException {
        this.destroyCount.incrementAndGet();
    }
}
