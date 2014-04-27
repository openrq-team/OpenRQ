# OpenRQ

## In a nutshell

OpenRQ provides a way to encode and decode data according to the fountain erasure code **RaptorQ**. More specifically, it is an implementation of a *Forward Error Correction Scheme for Object Delivery*, as specified in [RFC 6330](http://tools.ietf.org/html/rfc6330).

OpenRQ offers a rich open source Java API to developers who wish to incorporate erasure codes in their Content Delivery Protocols.

## Useful background definitions

<dl>
  <dt>Forward Error Correction (FEC):</dt>
  <dd>A technique for the recovery of errors in data disseminated over unreliable or noisy communication channels. The central idea is that the sender encodes the message in a redundant way by applying an error-correcting code, which allows the receiver to repair the errors.</dd>

  <dt>Erasure code:</dt>
  <dd>A FEC code with the capability to recover from losses in the communications. The data is divided into K source symbols, which are transformed in a larger number of N encoding symbols such that the original data can be retrieved from a subset of the N encoding symbols.</dd>

  <dt>Fountain code:</dt>
  <dd>A class of erasure codes with two important properties:
    <ul>
    <li>an arbitrary number of encoding symbols can be produced on the fly, simplifying the adaptation to varying loss rates;
    <li>the data can be reconstructed with high probability from any subset of the encoding symbols (of size equal to or slightly larger than the number of source symbols).
    </ul></dd>

  <dt>RaptorQ code:</dt>
  <dd>The closest solution to an ideal digital fountain code. It has the capability of achieving constant per-symbol encoding/decoding cost with an overhead near to zero.</dd>
</dl>

## Where to use OpenRQ

OpenRQ is intended as an erasure corrector for unreliable or noisy communication channels. Typically, the following steps are performed:
  1. Data is encoded using encoder objects, resulting in encoded packets;
  2. The encoded packets are transmitted to one or more receivers using any communication protocol;
  3. Encoded packets are collected by decoder objects at the receivers until a certain number is reached;
  4. Data is decoded from the encoding packets, resulting in the original data.

**Note:** *The use of OpenRQ is also recommended in situations where retransmissions are costly, such as when broadcasting data to multiple destinations, or when communication links are one-way.*

## Documentation

The Javadoc files for the OpenRQ API are found [here](http://zemasa.github.io/OpenRQ/docs)
