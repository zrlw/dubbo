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

/**
 * All of allocated resources need to be managed, this interface defines
 * the basic methods({@link #init()}, {@link #start()}, {@link #destroy()})
 * for maintaining resources' lifecycle.
 */
public interface Lifecycle {

    /**
     * Initialize the resource before it's used. Also, you can ignore this method,
     * if the specified resource don't need to initialize.<p>
     * More importantly, the implementation of this interface must make sure it
     * can be call only once.
     *
     * @throws LifecycleException when an exception occurred
     */
    void init() throws LifecycleException;

    /**
     * Start the resource, if which need to start.<p>
     * The implementation classes need to be idempotent in concurrent situation.<p>
     *
     * @return {@code true} if the resource started successfully, otherwise {@code false}.
     * @throws LifecycleException when an exception occurred
     */
    boolean start() throws LifecycleException;

    /**
     * Destroy the resource, if the resource is still running.<p>
     * This method will be block, if the resource need a lot of time to release.<p>
     * More importantly, the implementation classes should have timeout when it's released.
     *
     * @throws LifecycleException when an exception occurred
     */
    void destroy() throws LifecycleException;
}
