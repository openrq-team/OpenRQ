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
package net.fec.openrq.core.util.arithmetic;

/**
 * Defines arithmetical functions not present in class {@code java.lang.Math}.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ExtraMath {

    /**
     * Returns the ceiling value of an integer division.
     * 
     * @param num
     *            The numerator
     * @param den
     *            The denominator
     * @return the ceiling value of an integer division
     * @exception ArithmeticException
     *                If the denominator is equal to zero
     */
    public static int ceilDiv(int num, int den) {

        return (int)Math.ceil((double)num / den);
    }

    /**
     * Returns the ceiling value of a long integer division.
     * 
     * @param num
     *            The numerator
     * @param den
     *            The denominator
     * @return the ceiling value of a long integer division
     * @exception ArithmeticException
     *                If the denominator is equal to zero
     */
    public static long ceilDiv(long num, long den) {

        return (long)Math.ceil((double)num / den);
    }
    
    public static final int ceil(double x)
	{
		return((int) Math.ceil(x));
	}

	public static final int floor(double x)
	{		
		return((int) Math.floor(x));
	}

    private ExtraMath() {

        // not instantiable
    }
}
