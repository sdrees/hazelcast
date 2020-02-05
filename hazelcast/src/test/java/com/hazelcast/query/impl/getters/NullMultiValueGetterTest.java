/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.query.impl.getters;

import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.hazelcast.test.HazelcastTestSupport.assertInstanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class NullMultiValueGetterTest {

    @Test
    public void test_getReturnType() {
        Class returnType = NullMultiValueGetter.NULL_MULTIVALUE_GETTER.getReturnType();

        assertNull(returnType);
    }

    @Test
    public void test_isCacheable() {
        boolean cacheable = NullMultiValueGetter.NULL_MULTIVALUE_GETTER.isCacheable();

        assertFalse(cacheable);
    }

    @Test
    public void test_getValue() throws Exception {
        Object value = NullMultiValueGetter.NULL_MULTIVALUE_GETTER.getValue("anything");

        assertInstanceOf(MultiResult.class, value);
        assertTrue(((MultiResult) value).isNullEmptyTarget());
    }
}
