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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This class is to test {@link AbstractResourceHolder}.
 */
class ResourceHolderTest {

    private Resource resource;
    private ResourceHolderInstance holder;

    @BeforeEach
    public void setUp(){
        resource = new Resource();
        holder = new ResourceHolderInstance(resource);
    }

    @AfterEach
    public void tearDown(){
        resource = null;
        holder = null;
    }

    @Test
    public void testResourceHolder() {
        int times = 100;
        // using multi-thread to visit the holder.
        int threadCount = 10;
        CountDownLatch getVisitorCountDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new GetVisitor(holder, times, getVisitorCountDownLatch));
            thread.setName("GetVisitor-" + (i + 1));
            thread.start();
        }
        try {
            getVisitorCountDownLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // check AbstractResourceHolder#get() method in concurrent situation
        Assertions.assertEquals(holder.refCnt(), threadCount * times);


        CountDownLatch retainVisitorCountDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new RetainVisitor(holder, times, getVisitorCountDownLatch));
            thread.setName("RetainVisitor-" + (i + 1));
            thread.start();
        }
        try {
            retainVisitorCountDownLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // check AbstractResourceHolder#retain() method in concurrent situation
        Assertions.assertEquals(holder.refCnt(), threadCount * times * 2);

        CountDownLatch releaseVisitorCountDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new ReleaseVisitor(holder, times * 2, releaseVisitorCountDownLatch));
            thread.setName("ReleaseVisitor-" + (i + 1));
            thread.start();
        }
        try {
            releaseVisitorCountDownLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // check AbstractResourceHolder#release() method in concurrent situation
        Assertions.assertEquals(holder.refCnt(), 0);

        // check resource.
        Assertions.assertEquals(resource.getInitCount(), 1);
        Assertions.assertEquals(resource.getStartCount(), 1);
        Assertions.assertEquals(resource.getDestroyCount(), 1);
    }

    @Test
    public void testComplexSituation(){
        int times = 100;
        // using multi-thread to visit the holder.
        int threadCount = 10;
        // check complex visitor
        CountDownLatch complexVisitorCountDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new ComplexVisitor(holder, times , complexVisitorCountDownLatch));
            thread.setName("ComplexVisitor-" + (i + 1));
            thread.start();
        }
        try {
            complexVisitorCountDownLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // check AbstractResourceHolder#get() and AbstractResourceHolder#release() method in concurrent situation
        Assertions.assertEquals(holder.refCnt(), 0);

        // check resource.
        Assertions.assertEquals(resource.getInitCount(), 1);
        Assertions.assertEquals(resource.getStartCount(), 1);
        Assertions.assertEquals(resource.getDestroyCount(), 1);
    }

    class ResourceHolderInstance extends AbstractResourceHolder<Resource> {

        public ResourceHolderInstance(Resource resource) {
            super(resource);
        }
    }

    abstract class AbstractVisitor implements Runnable {

        public AbstractVisitor(ResourceHolderInstance holder, int times, CountDownLatch latch) {
            this.holder = holder;
            this.times = times;
            this.latch = latch;
        }

        private ResourceHolderInstance holder;
        private int times;
        private CountDownLatch latch;

        public ResourceHolderInstance getHolder() {
            return holder;
        }

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {
                this.doRun();
            }
            this.latch.countDown();
        }

        protected abstract void doRun();
    }

    class GetVisitor extends AbstractVisitor {

        public GetVisitor(ResourceHolderInstance holder, int times, CountDownLatch latch) {
            super(holder, times, latch);
        }

        @Override
        protected void doRun() {
            this.getHolder().get();
        }
    }

    class RetainVisitor extends AbstractVisitor {

        public RetainVisitor(ResourceHolderInstance holder, int times, CountDownLatch latch) {
            super(holder, times, latch);
        }

        @Override
        protected void doRun() {
            this.getHolder().retain();
        }
    }

    class ReleaseVisitor extends AbstractVisitor {

        public ReleaseVisitor(ResourceHolderInstance holder, int times, CountDownLatch latch) {
            super(holder, times, latch);
        }

        @Override
        protected void doRun() {
            this.getHolder().release();
        }
    }

    class ComplexVisitor extends AbstractVisitor{

        public ComplexVisitor(ResourceHolderInstance holder, int times, CountDownLatch latch) {
            super(holder, times, latch);
        }

        @Override
        protected void doRun() {
            this.getHolder().get();
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to sleep");
            }
            this.getHolder().release();
        }
    }
}
