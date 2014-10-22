/*
 * Copyright 2014 OpenRQ Team
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
package net.fec.openrq.parameters;


import java.io.Serializable;


/**
 * A serializable container for FEC parameters.
 * <p>
 * This class serializes the FEC parameters defined in class {@link FECParameters}.
 * 
 * @see FECParameters
 */
public class SerializableParameters implements Serializable {

    private static final long serialVersionUID = 990267693687317873L;

    private final long commonFecOTI;
    private final int schemeSpecFecOTI;


    /**
     * Constructs a new serializable object containing FEC parameters.
     * 
     * @param commonFecOTI
     *            A Common FEC Object Transmission Information
     * @param schemeSpecFecOTI
     *            A Scheme-Specific FEC Object Transmission Information
     */
    public SerializableParameters(long commonFecOTI, int schemeSpecFecOTI) {

        this.commonFecOTI = commonFecOTI;
        this.schemeSpecFecOTI = schemeSpecFecOTI;
    }

    /**
     * Returns the Common FEC Object Transmission Information.
     * 
     * @return the Common FEC Object Transmission Information
     */
    public long commonOTI() {

        return commonFecOTI;
    }

    /**
     * Returns the Scheme-Specific FEC Object Transmission Information.
     * 
     * @return the Scheme-Specific FEC Object Transmission Information
     */
    public int schemeSpecificOTI() {

        return schemeSpecFecOTI;
    }

    /**
     * Returns {@code true} if, and only if, this instance is equal to another object.
     * <p>
     * This instance ({@code this}) is equal to another object ({@code obj}), if and only if:
     * <ul>
     * <li>{@code obj} is non-null
     * <li>and {@code obj} is an instance of {@code SerializableParameters} <li>and {@code this}.{@link #commonOTI()} ==
     * {@code obj.commonOTI()} <li>and {@code this}.{@link #schemeSpecificOTI()} == {@code obj.schemeSpecificOTI()}
     * </ul>
     */
    @Override
    public boolean equals(Object obj) {

        return obj instanceof SerializableParameters && areEqual(this, (SerializableParameters)obj);
    }

    private static boolean areEqual(SerializableParameters sp1, SerializableParameters sp2) {

        return (sp1.commonFecOTI == sp2.commonFecOTI) && (sp1.schemeSpecFecOTI == sp2.schemeSpecFecOTI);
    }

    /**
     * Returns a hash code value based on the Common and Scheme-Specific FEC Object Transmission Informations.
     */
    @Override
    public int hashCode() {

        return 31 * Long.valueOf(commonFecOTI).hashCode() + schemeSpecFecOTI;
    }
}
