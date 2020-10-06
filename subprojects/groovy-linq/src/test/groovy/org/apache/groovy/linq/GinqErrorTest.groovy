/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.linq

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
class GinqErrorTest {
    @Test
    void "testGinq - from select - 1"() {
        def err = shouldFail '''\
            def numbers = [0, 1, 2]
            GINQ {
                from numbers
                select n
            }
        '''

        assert err.toString().contains('`in` is expected for `from`, e.g. `from n in nums` @ line 3, column 17.')
    }

    @Test
    void "testGinq - from select - 2"() {
        def err = shouldFail '''\
            def numbers = [0, 1, 2]
            GINQ {
                from n as numbers
                select n
            }
        '''

        assert err.toString().contains('`in` is expected for `from`, e.g. `from n in nums` @ line 3, column 17.')
    }

    @Test
    void "testGinq - from select - 3"() {
        def err = shouldFail '''\
            def numbers = [0, 1, 2]
            GINQ {
                from n, numbers
                select n
            }
        '''

        assert err.toString().contains('Only 1 argument expected for `from`, e.g. `from n in nums` @ line 3, column 17.')
    }

    @Test
    void "testGinq - from innerJoin select - 1"() {
        def err = shouldFail '''\
            def nums1 = [1, 2, 3]
            def nums2 = [1, 2, 3]
            assert [[1, 1], [2, 2], [3, 3]] == GINQ {
                from n1 in nums1
                innerJoin n2 in nums2
                select n1, n2
            }.toList()
        '''

        assert err.toString().contains('`on` clause is expected for `innerJoin` @ line 5, column 17.')
    }

}
