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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


/**
 * Tests methods from class net.fec.openrq.OpenRQ.
 */
@RunWith(Enclosed.class)
public class OpenRQClassTest {

    private static FECParameters SPECIAL_PARAMS;


    @BeforeClass
    public static void initParams() {

        SPECIAL_PARAMS = FECParameters.newParameters(1337, 666, 3); // just because
    }


    public static final class NewEncoder {

        @Test
        public void testParams() {

            final FECParameters fecParams = SPECIAL_PARAMS;
            final byte[] data = new byte[fecParams.dataLengthAsInt()];
            final ArrayDataEncoder ade = OpenRQ.newEncoder(data, fecParams);

            Assert.assertEquals(fecParams, ade.fecParameters());
        }

        @Test
        public void testData() {

            final FECParameters fecParams = SPECIAL_PARAMS;
            final byte[] data = new byte[fecParams.dataLengthAsInt()];
            final ArrayDataEncoder ade = OpenRQ.newEncoder(data, fecParams);

            Assert.assertArrayEquals(data, ade.dataArray());
        }

        @Test
        public void testOffset() {

            final FECParameters fecParams = SPECIAL_PARAMS;
            final byte[] data = new byte[fecParams.dataLengthAsInt()];
            final ArrayDataEncoder ade = OpenRQ.newEncoder(data, fecParams);

            Assert.assertEquals(0, ade.dataOffset());
        }
    }

    public static final class NewEncoderOffset {

        @Test
        public void testParams() {

            final FECParameters fecParams = SPECIAL_PARAMS;
            final int offset = 42;
            final byte[] data = new byte[offset + fecParams.dataLengthAsInt()];
            final ArrayDataEncoder ade = OpenRQ.newEncoder(data, offset, fecParams);

            Assert.assertEquals(fecParams, ade.fecParameters());
        }

        @Test
        public void testData() {

            final FECParameters fecParams = SPECIAL_PARAMS;
            final int offset = 42;
            final byte[] data = new byte[offset + fecParams.dataLengthAsInt()];
            final ArrayDataEncoder ade = OpenRQ.newEncoder(data, offset, fecParams);

            Assert.assertArrayEquals(data, ade.dataArray());
        }

        @Test
        public void testOffset() {

            final FECParameters fecParams = SPECIAL_PARAMS;
            final int offset = 42;
            final byte[] data = new byte[offset + fecParams.dataLengthAsInt()];
            final ArrayDataEncoder ade = OpenRQ.newEncoder(data, offset, fecParams);

            Assert.assertEquals(offset, ade.dataOffset());
        }
    }
}
