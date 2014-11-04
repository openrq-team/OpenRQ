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

/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra;


import net.fec.openrq.util.linearalgebra.factory.Basic1DFactory;
import net.fec.openrq.util.linearalgebra.factory.Basic2DFactory;
import net.fec.openrq.util.linearalgebra.factory.CCSFactory;
import net.fec.openrq.util.linearalgebra.factory.CRSFactory;
import net.fec.openrq.util.linearalgebra.factory.Factory;


/**
 * Tiny class for common things.
 */
public final class LinearAlgebra {

    /**
     * The la4j version.
     */
    public static final String VERSION = "0.4.9";

    /**
     * The la4j name.
     */
    public static final String NAME = "la4j";

    /**
     * The la4j date.
     */
    public static final String DATE = "Jan 2014";

    /**
     * The la4j full name.
     */
    public static final String FULL_NAME = NAME + "-" + VERSION + " (" + DATE + ")";

    /**
     * The {@link net.fec.openrq.util.linearalgebra.factory.Basic1DFactory} singleton instance.
     */
    public static final Factory BASIC1D_FACTORY = new Basic1DFactory();

    /**
     * The {@link net.fec.openrq.util.linearalgebra.factory.Basic2DFactory} singleton instance.
     */
    public static final Factory BASIC2D_FACTORY = new Basic2DFactory();

    /**
     * The {@link net.fec.openrq.util.linearalgebra.factory.CRSFactory} singleton instance.
     */
    public static final Factory CRS_FACTORY = new CRSFactory();

    /**
     * The {@link net.fec.openrq.util.linearalgebra.factory.CCSFactory} singleton instance.
     */
    public static final Factory CCS_FACTORY = new CCSFactory();

    /**
     * The default dense factory singleton instance. References the {@link LinearAlgebra#BASIC2D_FACTORY}.
     */
    public static final Factory DENSE_FACTORY = BASIC2D_FACTORY;

    /**
     * The default sparse factory singleton instance. References the {@link LinearAlgebra#CRS_FACTORY}.
     */
    public static final Factory SPARSE_FACTORY = CRS_FACTORY;

    /**
     * The default matrix factory singleton instance. References the {@link LinearAlgebra#BASIC2D_FACTORY}.
     */
    public static final Factory DEFAULT_FACTORY = BASIC2D_FACTORY;

    /**
     * The array with all factories available.
     */
    public static final Factory FACTORIES[] = {BASIC1D_FACTORY,
                                               BASIC2D_FACTORY,
                                               CRS_FACTORY,
                                               CCS_FACTORY};
}
