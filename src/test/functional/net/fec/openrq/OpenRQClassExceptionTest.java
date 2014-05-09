/*
 * Copyright 2014 Jose Lopes
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
package net.fec.openrq;


import net.fec.openrq.parameters.FECParameters;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


/**
 * Tests methods from class net.fec.openrq.OpenRQ.
 */
@RunWith(Enclosed.class)
public final class OpenRQClassExceptionTest {

    public static final class NewEncoder {

        @Test(expected = NullPointerException.class)
        public void testNullData() {

            OpenRQ.newEncoder(null, TestingCommon.Dummy.fecParameters());
        }

        @Test(expected = NullPointerException.class)
        public void testNullFECParams() {

            OpenRQ.newEncoder(TestingCommon.Dummy.data(), null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testIllegalFECParams() {

            OpenRQ.newEncoder(TestingCommon.Dummy.data(),
                FECParameters.newParameters(Integer.MAX_VALUE + 1,
                    TestingCommon.Dummy.T, TestingCommon.Dummy.Z));
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void testLargerDataLength() {

            OpenRQ.newEncoder(new byte[1],
                FECParameters.newParameters(2,
                    TestingCommon.Dummy.T, TestingCommon.Dummy.Z));
        }
    }
}
