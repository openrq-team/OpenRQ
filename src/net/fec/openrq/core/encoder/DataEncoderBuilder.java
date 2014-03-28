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

package net.fec.openrq.core.encoder;


import net.fec.openrq.core.parameters.ParameterChecker;


/**
 * Builder class for data encoder instances. This class follows the "Builder" design pattern, where multiple properties
 * may be configured and a final {@code DataEncoder} instance is returned upon calling the method {@link #build()}.
 * <p>
 * The following are some assignable properties that affect the final data encoder instance:
 * <ul>
 * <li>maximum payload length</li>
 * <li>maximum block size in working memory</li>
 * </ul>
 * <p>
 * If some property is not assigned, a default value is automatically assigned to it. Default values for each property
 * are defined as static fields.
 * <p>
 * All property assigning methods return the {@code this} instance in order to allow chained invocation:
 * 
 * <pre>
 * DataEncoder encoder = builder
 *                       .maxPayload(maxPay)
 *                       .maxBlockInMemory(maxBlock)
 *                       .build();
 * </pre>
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 * @param <T>
 *            A sub-type of {@code DataEncoder}
 */
public interface DataEncoderBuilder<T extends DataEncoder> {

    /**
     * Default value of 1392 bytes for the maximum payload length.
     */
    public static final int DEF_MAX_PAYLOAD_LENGTH = 1392; // P'

    /**
     * Default value of 76800 bytes for the maximum block size that is decodable in working memory.
     */
    public static final int DEF_MAX_DEC_BLOCK_SIZE = 76800;    // WS // B


    // TODO enable interleaving level property
    // /**
    // * Default value of 8 bytes for the minimum sub-symbol size.
    // */
    // public static final int DEF_MIN_SUB_SYMBOL = 8; // SS

    /**
     * Assigns the provided value to the property of <i>maximum payload length in number of bytes</i>.
     * <p>
     * This property affects the maximum size of an encoding symbol, which will be equal to the provided payload length
     * rounded down to the closest multiple of {@code Al}, where {@code Al} is the symbol alignment parameter.
     * <p>
     * Note that there are lower and upper bounds on the size of an encoding symbol given a certain data length, and if
     * the derived encoding symbol size is outside these bounds, then a {@code IllegalArgumentException} is thrown.
     * Refer to methods {@link ParameterChecker#symbolSizeLowerBound(long)} and
     * {@link ParameterChecker#symbolSizeUpperBound(long)} for information about these bounds.
     * 
     * @param maxPayloadLen
     *            The maximum payload length in number of bytes
     * @return this builder
     * @exception IllegalArgumentException
     *                If {@code maxPayloadLen} is non-positive or if the derived encoding symbol size is outside
     *                well-defined bounds
     * @see ParameterChecker#symbolSizeLowerBound(long)
     * @see ParameterChecker#symbolSizeUpperBound(long)
     */
    public DataEncoderBuilder<T> maxPayload(int maxPayloadLen);

    /**
     * Assigns the {@linkplain #DEF_MAX_PAYLOAD_LENGTH default value} to the property of <i>maximum payload length in
     * number of bytes</i>.
     * 
     * @return this builder
     * @see #maxPayload(int)
     */
    public DataEncoderBuilder<T> defaultMaxPayload();

    /**
     * Assigns the provided value to the property of <i>maximum block size in number of bytes that is decodable in
     * working memory</i>.
     * <p>
     * This property allows the decoder to work with a limited amount of memory in an efficient way.
     * 
     * @param maxBlock
     *            A number of bytes indicating the maximum block size that is decodable in working memory
     * @return this builder
     * @exception IllegalArgumentException
     *                If {@code maxBlock} is non-positive
     */
    public DataEncoderBuilder<T> maxDecoderBlock(int maxBlock);

    /**
     * Assigns the {@linkplain #DEF_MAX_DEC_BLOCK_SIZE default value} to the property of <i>maximum block size in number
     * of bytes that is decodable in working memory</i>.
     * 
     * @return this builder
     * @see #maxDecoderBlock(int)
     */
    public DataEncoderBuilder<T> defaultMaxDecoderBlock();

    // TODO enable interleaving level property
    // /**
    // * Assigns the provided value to the property of <i>lower bound on the sub-symbol size in units of {@code Al},
    // where
    // * {@code Al} is the symbol alignment parameter</i>.
    // * <p>
    // * This property affects the amount of interleaving used by the partitioning of an object into source blocks and
    // * sub-blocks.
    // *
    // * @param minSubSymbol
    // * The lower bound on the sub-symbol size in units of {@code Al}
    // * @return this builder
    // * @exception IllegalArgumentException
    // * If {@code minSubSymbol} is non-positive
    // */
    // public DataEncoderBuilder minSubSymbol(int minSubSymbol);
    //
    // /**
    // * Assigns the {@linkplain #DEF_MIN_SUB_SYMBOL default value} to the property of <i>lower bound on the sub-symbol
    // * size in units of {@code Al}, where {@code Al} is the symbol alignment parameter</i>.
    // *
    // * @return this builder
    // * @see #minSubSymbol(int)
    // */
    // public DataEncoderBuilder defaultMinSubSymbol();

    /**
     * Returns a {@code DataEncoder} instance based on the assigned properties to this builder.
     * 
     * @return a {@code DataEncoder} instance
     */
    public T build();
}
