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

    private static FECParameters variableDummyParams(int dataLength) {

        return FECParameters.newParameters(dataLength, TestingCommon.Dummy.T, TestingCommon.Dummy.Z);
    }


    public static final class NewEncoder {

        @Test
        public void testNoExceptions() {

            final byte[] data = TestingCommon.Dummy.data();
            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();

            OpenRQ.newEncoder(data, fecParams);
        }

        @Test(expected = NullPointerException.class)
        public void test_NPE_data() {

            final byte[] data = null;
            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();

            OpenRQ.newEncoder(data, fecParams);
        }

        @Test(expected = NullPointerException.class)
        public void test_NPE_params() {

            final byte[] data = TestingCommon.Dummy.data();
            final FECParameters fecParams = null;

            OpenRQ.newEncoder(data, fecParams);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE() {

            final byte[] data = TestingCommon.Dummy.data();
            final int dataLength = Integer.MAX_VALUE + 1;
            final FECParameters fecParams = variableDummyParams(dataLength);

            OpenRQ.newEncoder(data, fecParams);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_IOOBE() {

            final byte[] data = new byte[1];
            final int dataLength = 2;
            final FECParameters fecParams = variableDummyParams(dataLength);

            OpenRQ.newEncoder(data, fecParams);
        }
    }

    public static final class NewEncoderOffset {

        @Test
        public void testNoExceptions() {

            final byte[] data = TestingCommon.Dummy.data();
            final int offset = 0;
            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();

            OpenRQ.newEncoder(data, offset, fecParams);
        }

        @Test(expected = NullPointerException.class)
        public void test_NPE_data() {

            final byte[] data = null;
            final int offset = 0;
            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();

            OpenRQ.newEncoder(data, offset, fecParams);
        }

        @Test(expected = NullPointerException.class)
        public void test_NPE_params() {

            final byte[] data = TestingCommon.Dummy.data();
            final int offset = 0;
            final FECParameters fecParams = null;

            OpenRQ.newEncoder(data, offset, fecParams);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE() {

            final byte[] data = TestingCommon.Dummy.data();
            final int offset = 0;
            final int dataLength = Integer.MAX_VALUE + 1;
            final FECParameters fecParams = variableDummyParams(dataLength);

            OpenRQ.newEncoder(data, offset, fecParams);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_IOOBE_negOffset() {

            final byte[] data = TestingCommon.Dummy.data();
            final int offset = -1;
            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();

            OpenRQ.newEncoder(data, offset, fecParams);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_IOOBE_largeOffset() {

            final byte[] data = new byte[1];
            final int offset = 1;
            final int dataLength = 1;
            final FECParameters fecParams = variableDummyParams(dataLength);

            OpenRQ.newEncoder(data, offset, fecParams);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void test_IOOBE_dataLength() {

            final byte[] data = new byte[1];
            final int offset = 0;
            final int dataLength = 2;
            final FECParameters fecParams = variableDummyParams(dataLength);

            OpenRQ.newEncoder(data, offset, fecParams);
        }
    }

    public static final class NewDecoder {

        @Test
        public void testNoExceptions() {

            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();
            final int extraSymbols = 0;

            OpenRQ.newDecoder(fecParams, extraSymbols);
        }

        @Test(expected = NullPointerException.class)
        public void test_NPE() {

            final FECParameters fecParams = null;
            final int extraSymbols = 0;

            OpenRQ.newDecoder(fecParams, extraSymbols);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE_dataLength() {

            final int dataLength = Integer.MAX_VALUE;
            final FECParameters fecParams = variableDummyParams(dataLength);
            final int extraSymbols = 0;

            OpenRQ.newDecoder(fecParams, extraSymbols);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE_negExtraSymbols() {

            final FECParameters fecParams = TestingCommon.Dummy.fecParameters();
            final int extraSymbols = -1;

            OpenRQ.newDecoder(fecParams, extraSymbols);
        }
    }

    public static final class MinRepairSymbols {

        @Test
        public void testNoExceptions() {

            final int numSourceSymbols = 1;
            final int extraSymbols = 0;
            final double loss = 0D;

            OpenRQ.minRepairSymbols(numSourceSymbols, extraSymbols, loss);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE_nonPosNumSourceSymbols() {

            final int numSourceSymbols = 0;
            final int extraSymbols = 0;
            final double loss = 0D;

            OpenRQ.minRepairSymbols(numSourceSymbols, extraSymbols, loss);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE_negExtraSymbols() {

            final int numSourceSymbols = 1;
            final int extraSymbols = -1;
            final double loss = 0D;

            OpenRQ.minRepairSymbols(numSourceSymbols, extraSymbols, loss);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE_negLoss() {

            final int numSourceSymbols = 1;
            final int extraSymbols = 0;
            final double loss = -1D;

            OpenRQ.minRepairSymbols(numSourceSymbols, extraSymbols, loss);
        }

        @Test(expected = IllegalArgumentException.class)
        public void test_IAE_largeLoss() {

            final int numSourceSymbols = 1;
            final int extraSymbols = 0;
            final double loss = 2D;

            OpenRQ.minRepairSymbols(numSourceSymbols, extraSymbols, loss);
        }
    }
}
