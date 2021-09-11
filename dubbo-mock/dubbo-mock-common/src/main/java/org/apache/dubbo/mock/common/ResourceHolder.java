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

import org.apache.dubbo.mock.common.exception.IllegalReferenceCountException;

/**
 * The container to hold the specified resource, which must extend {@link Lifecycle}.
 * <p>
 * The {@link #refCnt()} will increase {@code 1} if the resource was allocated,
 * on the other hand, the {@link #refCnt()} will decrease {@code 1}
 * if the resource was deallocated. If the {@link #refCnt()} is decreased to {@code 0},
 * the resource will be deallocated explicitly.
 * </p>
 *
 * @param <T> the specified type resource, which implemented {@link Lifecycle}.
 */
public interface ResourceHolder<T extends Lifecycle> extends ReferenceCounted {

    /**
     * Return the resource which is held by this {@link ResourceHolder}.
     */
    T get();

    /**
     * {@inheritDoc}
     */
    @Override
    ResourceHolder retain();

    /**
     * Should be called by every method that tries to access the resource to check
     * if the buffer was released before.
     *
     * @param holder to hold resource.
     */
    default void ensureAccessible(ResourceHolder holder) {
        if (holder == null || holder.refCnt() == 0) {
            throw new IllegalReferenceCountException(0);
        }
    }
}
