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
