#!/usr/bin/env python

import sys, socket
#from sets import Set


def fail(reason):
	sys.stderr.write(reason + '\n')
	sys.exit(1)

if len(sys.argv) != 2 or len(sys.argv[1].split(':')) != 3:
	fail('Usage: udp-relay.py localPort:remoteHost:remotePort')

localPort, remoteHost, remotePort = sys.argv[1].split(':')

try:
	localPort = int(localPort)
except:
	fail('Invalid port number: ' + str(localPort))
try:
	remotePort = int(remotePort)
except:
	fail('Invalid port number: ' + str(remotePort))

try:
	s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	s.bind(('', localPort))
except:
	fail('Failed to bind on port ' + str(localPort))

knownServer = (remoteHost, remotePort)
packetCounter = -1
kappa = 10 # K + Overhead
targetSource = set([0,4,8])
payloadRepair = set([10,11,12])

sys.stderr.write('All set.\n')

while True:
        data, addr = s.recvfrom(32768)
        packetCounter += 1
        sys.stderr.write('Packet ' + str(packetCounter) + ': ')

        if (packetCounter in targetSource) or (packetCounter > kappa and (packetCounter not in payloadRepair)):
                sys.stderr.write('DROPPED\n')
                continue
        else:
                sys.stderr.write('PASSED\n')
                s.sendto(data, knownServer)
