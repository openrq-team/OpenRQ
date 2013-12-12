#!/usr/bin/env python

import sys, socket
import time


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
symbol_counter = -1
total_packets = 0
block_counter = -1

kappa = 10 # K + Overhead
targetSource = set([0,4,8])
payloadRepair = set([10,11,12])

encoding_overhead = 500  # in milliseconds
previous_ts = 0

symbols_per_block = 0

sys.stderr.write('All set.\n')

while True:
        
        data, addr = s.recvfrom(32768)
        symbol_counter += 1
        total_packets += 1
       # sys.stderr.write('Packet ' + str(packetCounter) + ': ')
        ts = time.time()*1000
        delta = ts - previous_ts
        

        # if we found the number of packets
        # per block: perform the attack
        if (symbols_per_block != 0):
                
                # this is simulated, here we should
                # lookup a table with attack vectors
                if (symbols_per_block == 453):
                        if(symbol_counter == 100):
                                print("Packet " + str(symbol_counter) + " of block " + str(block_counter) + " DROPPED!")
                                continue         
                else:
                        print("BOOM!")


        # is this a new block?
        if (delta > encoding_overhead) and total_packets != 1:
                print("Packets: "+str(total_packets-1)+" Symbol: "+str(symbol_counter-1))
                
                if(symbols_per_block == 0):
                        symbols_per_block = symbol_counter-1

                symbol_counter = 0
                block_counter += 1
                print("--- time lapse --- ("+str(delta)+" ms)\n")


        s.sendto(data, knownServer)
        previous_ts = ts
            

#        if (packetCounter in targetSource) or (packetCounter > kappa and (packetCounter not in payloadRepair)):
#                sys.stderr.write('DROPPED\n')
#                continue
#        else:
#                sys.stderr.write('PASSED\n')
#                s.sendto(data, knownServer)
