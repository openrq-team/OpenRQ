package net.fec.openrq.parameters;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class InternalConstants {

    // == data length == //
    // the RFC specifies a non-negative value, but we force a positive value here
    static final long MIN_F = 1L;
    static final long MAX_F = 946270874880L;
    static final int NUM_BYTES_F = 5;

    // == symbol size == //
    static final int MIN_T = 1;
    static final int MAX_T = (1 << 16) - 1;

    // == number of source blocks == //
    static final int MIN_Z = 1;
    static final int MAX_Z = 1 << 8;

    // == number of sub-blocks == //
    static final int MIN_N = 1;
    static final int MAX_N = 56403;

    // == source block number == //
    static final int MIN_SBN = 0;
    static final int MAX_SBN = (1 << 8) - 1;

    // == encoding symbol identifier == //
    static final int MIN_ESI = 0;
    static final int MAX_ESI = (1 << 24) - 1;
    static final int NUM_BYTES_ESI = 3;
}
