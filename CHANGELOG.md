## 3.3.2

Simplified the API for return types in Encoding/Decoding classes.
The returned generic types no longer have wildcards.
This modification is "backwards-compatible" with previous versions
(old code may have to change in declarations, for example,
from Iterable<? extends Something> to Iterable<Something>)

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.ArrayDataDecoder
 * ++ public Iterable<SourceBlockDecoder> sourceBlockIterable()
 * -- public Iterable<? extends SourceBlockDecoder> sourceBlockIterable()
* net.fec.openrq.ArrayDataEncoder
 * ++ public Iterable<SourceBlockEncoder> sourceBlockIterable()
 * -- public Iterable<? extends SourceBlockEncoder> sourceBlockIterable()
* net.fec.openrq.decoder.DataDecoder
 * ++ public Iterable<SourceBlockDecoder> sourceBlockIterable()
 * -- public Iterable<? extends SourceBlockDecoder> sourceBlockIterable()
* net.fec.openrq.encoder.DataEncoder
 * ++ public Iterable<SourceBlockEncoder> sourceBlockIterable()
 * -- public Iterable<? extends SourceBlockEncoder> sourceBlockIterable()


## 3.3.1

Fixed a bug in the decoding algorithm that affected the code resilience.


## 3.3

Improved performance a lot.

Clarified the meaning of "symbol overhead".

Added new methods for retrieving and changing the symbol overhead value in
a source block decoder.

Added a new method that returns the number of repair symbols starting at a
specified index.

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.OpenRQ
 * ++ public static ArrayDataDecoder newDecoderWithZeroOverhead(FECParameters)
 * ++ public static ArrayDataDecoder newDecoderWithOneOverhead(FECParameters)
 * ++ public static ArrayDataDecoder newDecoderWithTwoOverhead(FECParameters)
* net.fec.openrq.decoder.SourceBlockDecoder
 * ++ public int symbolOverhead()
 * ++ public void setSymbolOverhead(int)
* net.fec.openrq.parameters.ParameterChecker
 * ++ public static int numRepairSymbolsPerBlock(int, int)


## 3.2

Turned "net.fec.openrq.decoder.SourceBlockDecoder" thread safe, and added
method to retrieve the latest source block state (decoded/decoding failure).

Added class for storing information about a source block decoder.

Added more methods for checking bounds on the number of source symbols in a
block. Added a method for obtaining the number of repair symbols in a block
given the number of source symbols in the block.

Added a method for iterating over all source block decoders.

Added classes:
* net.fec.openrq.decoder.SBDInfo
* net.fec.openrq.decoder.SerializableSBDInfo

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.decoder.DataDecoder
 * ++ public Iterable<? extends SourceBlockDecoder> sourceBlockIterable()
* net.fec.openrq.decoder.SourceBlockDecoder
 * ++ public SourceBlockState latestState()
 * ++ public SBDInfo information()
* net.fec.openrq.parameters.ParameterChecker
 * ++ public static int minNumSourceSymbolsPerBlock()
 * ++ public static boolean isNumSourceSymbolsPerBlockOutOfBounds(int)
 * ++ public static int numRepairSymbolsPerBlock(int)


## 3.1

Added new methods for reading/writing FEC parameters. They now mimic those
found in class "net.fec.openrq.EncodingPacket" (backwards compatible with
previous versions).

Added classes:
* net.fec.openrq.parameters.SerializableParameters

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.parameters.FECParameters
 * ++ public static Parsed<FECParameters> parse(SerializableParameters)
 * ++ public static Parsed<FECParameters> parse(byte[])
 * ++ public SerializableParameters asSerializable()
 * ++ public byte[] asArray()
 * ++ public void writeTo(byte[])
 * ++ public ByteBuffer asBuffer()


## 3.0

Fixed critical bug related to the type of the deriver parameter "maximum
decoding block size" (breaks compatibility with previous versions).

Fixed deriver parameters checking bug.

Added method for obtaining the lowest bound for the "maximum decoding block
size".

Added new methods and changed parameter names from existing methods to handle
payload lengths specifically.

Added method for retrieving the maximum allowed "data length" given a "payload
length" and a "maximum decoding block size".

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.parameters.FECParameters
 * ++ public static FECParameters deriveParameters(long, int, long)
 * -- public static FECParameters deriveParameters(long, int, int)
* net.fec.openrq.parameters.ParameterChecker
 * ++ public static long minAllowedDecodingBlockSize(long, int)
 * -- public static int minAllowedDecodingBlockSize(long, int)
 * ++ public static boolean areValidDeriverParameters(long, int, long)
 * -- public static boolean areValidDeriverParameters(long, int, int)
 * ++ public static String getDeriverParamsErrorString(long, int, long)
 * -- public static String getDeriverParamsErrorString(long, int, int)
 * ++ public static long minDecodingBlockSize()
 * ++ public static int minPayloadLength()
 * ++ public static int maxPayloadLength()
 * ++ public static boolean isPayloadLengthOutOfBounds(int)
 * ++ public static int minAllowedPayloadLength(long)
 * ++ public static long maxAllowedDataLength(int, long)


## 2.0.1

Changed a method from non-static to static (backwards compatible with previous
version).

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.parameters.ParameterChecker
 * ++ public static int minAllowedSymbolSize()
 * -- public int minAllowedSymbolSize()


## 2.0

In general:
- Improved performance slightly.
- Renamed in general: "sub-blocks" to "interleaver length" in order to
distinguish the name from "source blocks".
- Renamed "Deriving parameters" to "Deriver parameters" since the latter is the
noun for the verb "derive"
- Renamed in Deriver parameters: "maximum payload length" to "payload length" in
order to simplify things.
- Removed unnecessary symbol alignment value from public parameters.
- Removed unnecessary overloading method (int overloading long)

Changed public method signatures:
(++/-- mean new/old methods, xx means deleted method)
* net.fec.openrq.parameters.FECParameters
 * xx public static FECParameters deriveParameters(int, int, int)
 * ++ public int interleaverLength()
 * -- public int numberOfSubBlocks()
* net.fec.openrq.parameters.ParameterChecker
 * ++ public static int minInterleaverLength()
 * -- public static int minNumSubBlocks()
 * ++ public static int maxInterleaverLength()
 * -- public static int maxNumSubBlocks()
 * ++ public static boolean areValidFECParameters(long, int, int, int)
 * -- public static boolean areValidFECParameters(long, int, int, int, int)
 * ++ public static String getFECParamsErrorString(long, int, int, int)
 * -- public static String testValidFECParameters(long, int, int, int, int)
 * ++ public static boolean areValidDeriverParameters(long, int, int)
 * -- public static boolean areValidDerivingParameters(long, int, int, int)
 * ++ public static String getDeriverParamsErrorString(long, int, int, int)
 * -- public static String testValidDerivingParameters(long, int, int, int)
 * ++ public static String getFECPayloadIDErrorString(int, int, int)
 * -- public static String testValidFECPayloadID(int, int, int)
* net.fec.openrq.parameters.ParameterIO
 * ++ public static int extractInterleaverLength(int)
 * -- public static int extractNumSubBlocks(int)


## 1.0.1

Fixed critical bug.


## 1.0

Initial release.
