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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SpringBootConditionsTest {

    @Test
    void testSpringBoot3ConditionWithValidVersion() {
        String version = "3.2.1"; // Example of a valid version
        assertTrue(SpringBoot3Condition.matches(version));
    }

    @Test
    void testSpringBoot3ConditionWithInvalidVersion() {
        String version = "2.5.0"; // Invalid major version
        assertFalse(SpringBoot3Condition.matches(version));
    }

    @Test
    void testSpringBoot3ConditionWithNullVersion() {
        String version = null;
        assertFalse(SpringBoot3Condition.matches(version)); // Should not throw
    }

    @Test
    void testSpringBoot4ConditionWithValidVersion() {
        String version = "4.0.0"; // Example of a valid version
        assertTrue(SpringBoot4Condition.matches(version));
    }

    @Test
    void testSpringBoot4ConditionWithInvalidVersion() {
        String version = "3.2.1"; // Invalid major version
        assertFalse(SpringBoot4Condition.matches(version));
    }

    @Test
    void testSpringBoot4ConditionWithNullVersion() {
        String version = null;
        assertFalse(SpringBoot4Condition.matches(version)); // Should not throw
    }

    @Test
    void testAutoConfigurationImports() {
        String resource = "META-INF/spring.factories";
        assertNotNull(getClass().getClassLoader().getResource(resource));
        // Assuming the file existence check is enough
        // Check contents here if necessary
    }
}
