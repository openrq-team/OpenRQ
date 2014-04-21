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
/**
 * This is the main package in the OpenRQ API.
 * <p>
 * The OpenRQ API provides a way to encode and decode data according to the fountain code RaptorQ, as defined in <a
 * href="http://tools.ietf.org/html/rfc6330">RFC 6330</a>.
 * <h2>Background</h2>
 * <dl>
 * <dt><b>Forward Error Correction (FEC):</b></dt>
 * <dd>A technique for the recovery of errors in data disseminated over unreliable or noisy communication channels. The
 * central idea is that the sender encodes the message in a redundant way by applying an error-correcting code, which
 * allows the receiver to repair the errors.</dd>
 * <dt><b>Erasure code:</b></dt>
 * <dd>A FEC code with the capability to recover from losses in the communications. The data is divided into K source
 * symbols, which are transformed in a larger number of N encoding symbols such that the original data can be retrieved
 * from a subset of the N encoding symbols.</dd>
 * <dt><b>Fountain code:</b></dt>
 * <dd>A class of erasure codes with two important properties:
 * <ul>
 * <li>an arbitrary number of encoding symbols can be produced on the fly, simplifying the adaptation to varying loss
 * rates;
 * <li>the data can be reconstructed with high probability from any subset of the encoding symbols (of size equal to or
 * slightly larger than the number of source symbols).
 * </ul>
 * </dd>
 * <dt><b>RaptorQ code:</b></dt>
 * <dd>The closest solution to an ideal digital fountain code. It has the capability of achieving constant per-symbol
 * encoding/decoding cost with an overhead near to zero.</dd>
 * </dl>
 * <h2>Where to use this API</h2> This API is intended as an erasure corrector for unreliable or noisy communication
 * channels. Typically, the following steps are performed:
 * <ol>
 * <li>Data is encoded using this API, resulting in encoded packets;
 * <li>The encoded packets are transmitted to one or more receivers using any communication protocol;
 * <li>Encoded packets are collected at the receivers until a certain number is reached;
 * <li>Data is decoded from the encoding packets, resulting in the original data.
 * </ol>
 * <b>NOTE:</b> <i>The use of this API is also recommended in situations where retransmissions are costly, such as when
 * broadcasting data to multiple destinations, or when communication links are one-way.</i>
 * <h2>How to use this API</h2>
 * <h3>Basic definitions</h3>
 * <dl>
 * <dt><b>Source data:</b></dt>
 * <dd>The data object to be encoded/decoded.</dd>
 * <dt><b>Source block:</b></dt>
 * <dd>A contiguous portion of the source data. Each source block is encoded/decoded independently from the others.
 * Source data is partitioned into source blocks in order to allow efficient decoding with limited working memory.</dd>
 * <dt><b>Source symbol:</b></dt>
 * <dd>A contiguous portion of a source block. Source symbols are the smallest unit of source data. Source blocks are
 * partitioned into source symbols in order to provide packets containing small amounts of data.</dd>
 * <dt><b>Repair symbol:</b></dt>
 * <dd>A piece of encoded data with the same size as a source symbol. Repair symbols are generated from an encoder.</dd>
 * <dt><b>Encoding symbol:</b></dt>
 * <dd>A generic term for a source or repair symbol.</dd>
 * <dt><b>Encoding packet:</b></dt>
 * <dd>A packet of data containing one or more encoding symbols.</dd>
 * <dt><b>Encoder:</b></dt>
 * <dd>An object that receives source data, encodes it, and produces encoding packets for transmission.</dd>
 * <dt><b>Decoder:</b></dt>
 * <dd>An object that receives encoding packets, decodes them, and produces the original source data.</dd>
 * </dl>
 * <h3>Initializing</h3> Encoders and decoders are initialized with the help of <i>FEC parameters</i>. These include the
 * length of the data and information on how to partition the source data into source blocks and source symbols.
 * Encoders are initialized by passing source data and FEC parameters. Decoders are initialized by passing FEC
 * parameters.
 * <p>
 * <b>NOTE:</b> <i>The same FEC parameters must be passed to an encoder and a decoder in order to correctly decode the
 * original source data, which usually means transmitting the parameters to a receiver before sending data, so that the
 * receiver is able to initialize a decoder that matches the sender's encoder.</i>
 * <p>
 * Class {@link net.fec.openrq.OpenRQ OpenRQ} is the entry point for the OpenRQ API, and provides static methods for
 * initializing encoders and decoders.
 * <p>
 * FEC parameters are represented by {@link net.fec.openrq.parameters.FECParameters FECParameters} instances. They
 * provide methods for writing the parameters to a buffer or a writable channel, and static methods for parsing/reading
 * parameters from a buffer or a readable channel. Please refer to the documentation of class
 * {@link net.fec.openrq.parameters.FECParameters FECParameters} for more details.
 * <h3>Encoding data</h3> Source data encoders are represented by {@link net.fec.openrq.encoder.DataEncoder DataEncoder}
 * instances. They encapsulate source data and provide a separate encoder for each source block.
 * <p>
 * Source block encoders are represented by {@link net.fec.openrq.encoder.SourceBlockEncoder SourceBlockEncoder}
 * instances. They provide encoding packets that contain encoding symbols from the source block being encoded.
 * <p>
 * Encoding packets are represented by {@link net.fec.openrq.EncodingPacket EncodingPacket} instances. They provide
 * methods for writing the packet to a buffer or a writable channel, and static methods for parsing/reading a packet
 * from a buffer or a readable channel.
 * <p>
 * Please refer to the documentation of classes {@link net.fec.openrq.encoder.DataEncoder DataEncoder},
 * {@link net.fec.openrq.encoder.SourceBlockEncoder SourceBlockEncoder} and {@link net.fec.openrq.EncodingPacket
 * EncodingPacket} for more details.
 * <h3>Decoding data</h3> Source data decoders are represented by {@link net.fec.openrq.decoder.DataDecoder DataDecoder}
 * instances. They deliver source data, as it is being decoded, and provide a separate decoder for each source block.
 * They also help parsing/reading encoding packets from buffers or readable channels.
 * <p>
 * Source block decoders are represented by {@link net.fec.openrq.decoder.SourceBlockDecoder SourceBlockDecoder}
 * instances. They receive encoding packets and provide an indication of the current state of the source block.
 * <p>
 * Please refer to the documentation of classes {@link net.fec.openrq.decoder.DataDecoder DataDecoder} and
 * {@link net.fec.openrq.decoder.SourceBlockDecoder SourceBlockDecoder} for more details.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
package net.fec.openrq;