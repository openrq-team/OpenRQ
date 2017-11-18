## OpenRQ: _an open-source RaptorQ implementation_

Please visit the [OpenRQ Wiki](https://github.com/openrq-team/OpenRQ/wiki) for information about the project.

### Implementation maturity

OpenRQ is an implementation of a raptor code that follows exactly the instructions of [RFC 6330 (RaptorQ)](https://tools.ietf.org/html/rfc6330). The following features are available:
- splitting of data into blocks where each block can be independently encoded/decoded
- encoding blocks into individual packets that can be independently transmitted to the receiver
- decoding blocks from individual packets received in any order
- configuration of encoding/decoding parameters such as number of blocks and packet size
- multiple ways to transmit packets or configuration parameters (e.g., using Java serialization, ```Data{Out,In}putStreams```, or ```ByteBuffers``` and ```Channels``` from java.nio)

There is one feature still *missing*, though:
- [packet interleaving](https://en.wikipedia.org/wiki/Forward_error_correction#Interleaving)<sup>1</sup>

Other useful features, such as allowing encoding/decoding directly from files or continually from streams, are not implemented yet. Users can only encode/decode from arrays of bytes.

### Performance

Performance is still not optimal due to a bottleneck present in the decoding function.<sup>2</sup>

Current block decoding throughputs by total number of source symbols:

| Symbols per block | Decoding throughput |
|:------------------|:--------------------|
| 10                | 43.14 Mbps |
| 50                | 50.06 Mbps |
| 100               | 44.62 Mbps |
| 500               | 26.21 Mbps |
| 1000              | 15.39 Mbps |

### Development status

Not being actively developed any more, but still accepting bug reports and pull requests. Other than that, we have no planned time to return to an active development.

### Related projects

- [orq](https://github.com/olanmatt/orq) - An open-source RaptorQ implementation in C++

---

<sub><sup>1</sup> This is defined in RFC 6330 as "sub-blocks" and OpenRQ only allows at most one sub-block to be configured per source block.

<sub><sup>2</sup> The decoding function follows the algorithm defined in RFC 6330 straightforwardly. Our implementation of the algorithm has been done with care for maximal performance (e.g., we use linear algebra functions optimized for sparse matrices), however our decoding speed is at least one order of magnitude slower than that of an [official RaptorQ implementation](https://www.qualcomm.com/products/raptorq/evaluation-kit). This makes us believe a decoding algorithm more efficient than the one defined in the RFC exists but we have no knowledge of it.</sub>
