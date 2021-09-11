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
import org.apache.dubbo.mock.common.exception.LifecycleException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * The basic implementation of a {@link ResourceHolder} that holds it's data.
 *
 * @param <T> the specified type resource, which implemented {@link Lifecycle}.
 */
public abstract class AbstractResourceHolder<T extends Lifecycle> implements ResourceHolder<T> {

    /**
     * Constructors the basic ResourceHolder with the specified resource.
     *
     * @param resource the resource to be held.
     */
    public AbstractResourceHolder(T resource) {
        Objects.requireNonNull(resource, "resource cannot be null!");
        this.resource = resource;
        this.referenceCount = 0;
    }

    /**
     * The resource.
     */
    private volatile T resource;

    /**
     * The reference count.
     */
    private volatile int referenceCount;

    private final AtomicIntegerFieldUpdater<AbstractResourceHolder> REFERENCE_COUNT_UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(AbstractResourceHolder.class, "referenceCount");

    /**
     * The resource is initialized or not.
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * The resource is ready to visit or not.
     */
    private volatile boolean ready = false;

    /**
     * The resource is destroyed or not.
     */
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * Update the reference count.
     *
     * @param isIncreased {@code true} if the reference count need to increase, otherwise {@code false}.
     */
    private void updateReferenceCount(boolean isIncreased) {
        int oldValue = this.refCnt();
        if(isIncreased){
            if (oldValue < 0) {
                throw new IllegalReferenceCountException(oldValue);
            }
        }else{
            if (oldValue <= 0) {
                throw new IllegalReferenceCountException(oldValue);
            }
        }

        while (oldValue >= 0 && !REFERENCE_COUNT_UPDATER.
            compareAndSet(this, oldValue, isIncreased ? oldValue + 1 : oldValue - 1)) {
            oldValue = this.refCnt();
        }
    }

    @Override
    public T get() {
        // check the resource is initialized or not.
        if (this.initialized.compareAndSet(false, true)) {
            // initialize this resource.
            this.resource.init();
            // start this resource.
            if (!this.resource.start()) {
                throw new LifecycleException("The resource cannot start");
            }
            // make this resource ready to visit.
            this.ready = true;
        }
        // record the reference count if the resource is ready.
        if (this.ready) {
            this.retain();
        }
        // make sure this resource can be access.
        ensureAccessible(this);
        return this.resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int refCnt() {
        return this.referenceCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceHolder retain() {
        // increase the reference count.
        this.updateReferenceCount(true);
        return this;
    }

    @Override
    public boolean release() {
        // decrease the reference count.
        this.updateReferenceCount(false);
        // release the resource if the reference count equals 0.
        if (this.refCnt() == 0) {
            // destroy the resource
            if (destroyed.compareAndSet(false, true)) {
                this.resource.destroy();
                this.resource = null;
            }
            return true;
        }
        return false;
    }
}
