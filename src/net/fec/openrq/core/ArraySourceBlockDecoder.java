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
package net.fec.openrq.core;


import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.fec.openrq.core.decoder.SourceBlockDecoder;
import net.fec.openrq.core.decoder.SourceBlockState;
import net.fec.openrq.core.util.rq.SingularMatrixException;
import net.fec.openrq.core.util.rq.Utilities;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class ArraySourceBlockDecoder implements SourceBlockDecoder {

    // requires valid arguments
    static ArraySourceBlockDecoder newDecoder(
        byte[] array,
        int arrayOff,
        FECParameters fecParams,
        int sbn,
        int K,
        int extraSymbols)
    {

        final int paddedLen = K * fecParams.symbolSize();
        final int arrayLen = Math.min(paddedLen, array.length - arrayOff);
        final PaddedByteArray data = PaddedByteArray.newArray(array, arrayOff, arrayLen, paddedLen);

        return new ArraySourceBlockDecoder(data, fecParams, sbn, K, extraSymbols);
    }


    private final PaddedByteArray data;

    private final FECParameters fecParams;
    private final int sbn;
    private final int K;
    private final int symbolOverhead;
    
    private Set<EncodingSymbol> repairSymbols;
    private BitSet receivedSourceSymbols;
    SourceBlockState state;
	Map<Integer, byte[]> esiToLTCode;
    
    private ArraySourceBlockDecoder(
        PaddedByteArray data,
        FECParameters fecParams,
        int sbn,
        int K,
        int extraSymbols)
    {

        this.data = data;

        this.fecParams = fecParams;
        this.sbn = sbn;
        this.K = K;
        this.symbolOverhead = extraSymbols;
        
        this.repairSymbols = new HashSet<EncodingSymbol>();
        this.receivedSourceSymbols = new BitSet(K);
        this.state = SourceBlockState.INCOMPLETE;
        this.esiToLTCode = new TreeMap<Integer, byte[]>();
    }

    @Override
    public int sourceBlockNumber() {

        return sbn;
    }

    @Override
    public int numberOfSourceSymbols() {

        return K;
    }

    @Override
    public boolean containsSourceSymbol(int encSymbolID) {

        return receivedSourceSymbols.get(encSymbolID);
    }

    @Override
    public boolean containsRepairSymbol(int encSymbolID) {

        for (EncodingSymbol repairSymbol : repairSymbols)
        	if (repairSymbol.getESI() == encSymbolID)
        		return true;
        
        return false;
    }

    @Override
    public boolean isSourceBlockDecoded() {

    	if (state == SourceBlockState.DECODED)
    		return true;
    	
        return false;
    }

    @Override
    public Set<Integer> missingSourceSymbols() {

        Set<Integer> missingSourceSymbols = new TreeSet<Integer>();
        
        for (int index = receivedSourceSymbols.nextClearBit(0); index != K; receivedSourceSymbols.nextClearBit(index))
        	missingSourceSymbols.add(index);
        
        return missingSourceSymbols;
    }

    @Override
    public SourceBlockState putSourceSymbol(int encSymbolID, ByteBuffer sourceSymbol) {

    	checkESI(encSymbolID);

        // put source symbol data
        byte[] array = sourceSymbol.array(); // TODO unecessary copy?
        data.putBytes(encSymbolID * fecParams.symbolSize(), array);

        // do we have all source symbols?
        if (receivedSourceSymbols.nextClearBit(0) == K){
        	state = SourceBlockState.DECODED;
        	return state;
        }
        else
        	// check if we have enough symbols to decode
        	if (receivedSourceSymbols.cardinality() + repairSymbols.size() >= K + symbolOverhead)
        		return decode();
        	else
        		return state;
    }

    @Override
    public SourceBlockState putSourceSymbol(int encSymbolID, byte[] sourceSymbol, int offset) {

    	checkESI(encSymbolID);

        // put source symbol data
    	int t = fecParams.symbolSize();
        data.putBytes(encSymbolID * t, sourceSymbol, offset, t);

        // do we have all source symbols?
        if (receivedSourceSymbols.nextClearBit(0) == K){
        	state = SourceBlockState.DECODED;
        	return state;
        }
        else
        	// check if we have enough symbols to decode
        	if (receivedSourceSymbols.cardinality() + repairSymbols.size() >= K + symbolOverhead)
        		return decode();
        	else
        		return state;
    }

    @Override
    public SourceBlockState putRepairSymbol(int encSymbolID, ByteBuffer repairSymbol) {

    	// generate FEC Payload ID
        FECPayloadID fpid = FECPayloadID.makeFECPayloadID(sbn, encSymbolID, fecParams);
    	
        // generate repair symbol
        EncodingSymbol repair = EncodingSymbol.newRepairSymbol(fpid, repairSymbol.array()); // TODO unecessary copy?
        
        // add this repair symbol to the set of received repair symbols
        repairSymbols.add(repair);

        // do we have all source symbols?
        if (receivedSourceSymbols.nextClearBit(0) == K){
        	state = SourceBlockState.DECODED;
        	return state;
        }
        else
        	// check if we have enough symbols to decode
        	if (receivedSourceSymbols.cardinality() + repairSymbols.size() >= K + symbolOverhead)
        		return decode();
        	else
        		return state;
    }

    @Override
    public SourceBlockState putRepairSymbol(int encSymbolID, byte[] repairSymbol, int offset) {

    	// generate FEC Payload ID
        FECPayloadID fpid = FECPayloadID.makeFECPayloadID(sbn, encSymbolID, fecParams);
    	
        // generate repair symbol
        EncodingSymbol repair = EncodingSymbol.newRepairSymbol(fpid, Arrays.copyOfRange(repairSymbol, offset, repairSymbol.length)); // TODO unecessary copy?
        
        // add this repair symbol to the set of received repair symbols
        repairSymbols.add(repair);

        // do we have all source symbols?
        if (receivedSourceSymbols.nextClearBit(0) == K){
        	state = SourceBlockState.DECODED;
        	return state;
        }
        else
        	// check if we have enough symbols to decode
        	if (receivedSourceSymbols.cardinality() + repairSymbols.size() >= K + symbolOverhead)
        		return decode();
        	else
        		return state;
    }
    
    private final void checkESI(int esi) {

        if (esi < 0 || esi >= K) throw new IllegalArgumentException("invalid encoding symbol identifier");
    }
    
    private final SourceBlockState decode() {

    	// generate intermediate symbols -- watch out for decoding failure
    	byte[] intermediate_symbols = generateIntermediateSymbols();
    	
    	if (intermediate_symbols == null) {
    		state = SourceBlockState.DECODING_FAILURE;
    		return state;
    	}

    	/*
    	 * with the intermediate symbols calculated, one can recover
    	 *  every missing source symbol
    	 */
    	
    	// recover missing source symbols
    	for(Map.Entry<Integer, byte[]> missing : esiToLTCode.entrySet())
    	{
    		// multiply the constraint matrix line relative to the missing source symbol
    		//  by the vector of intermediate symbols to recover the missing source symbol
    		byte[] original_symbol = Utilities.multiplyByteLineBySymbolVector(missing.getValue(), intermediate_symbols, fecParams.symbolSize());

    		// write to data buffer
    		data.putBytes(missing.getKey(), original_symbol);
    	}

    	// update decoding status
    	state = SourceBlockState.DECODED;
    	
    	return state;
    }
    
    private final byte[] generateIntermediateSymbols() {
    	
    	// constraint matrix parameters
    	int kPrime = SystematicIndices.ceil(K);
    	int Ki = SystematicIndices.getKIndex(K);
    	int S = SystematicIndices.S(Ki);
    	int H = SystematicIndices.H(Ki);
    	int L = kPrime + S + H;
    	int T = fecParams.symbolSize();
    	
    	// number of extra repair symbols to be used for the decoding process
    	int overhead = repairSymbols.size() - (receivedSourceSymbols.size() - receivedSourceSymbols.cardinality());
    	
    	// number of rows in the decoding matrix
    	int M = L + overhead;

    	// allocate memory for the decoding matrix
		byte[][] constraint_matrix = new byte[M][];

		// generate the original constraint matrix
		byte[][] lConstraint = LinearSystem.generateConstraintMatrix(kPrime, T);
		
		// copy to our decoding matrix
		for (int row = 0; row < L; row++)
			constraint_matrix[row] = lConstraint[row];

		// initialize D
		byte[][] D = new byte[M][T];

		// populate D with the received source symbols
        for (int index = receivedSourceSymbols.nextSetBit(0); index != -1; receivedSourceSymbols.nextSetBit(index))
        	data.getBytes(index, D[S + H + index]);
				
		/*
		 * for every repair symbol received
		 *  - replace a missing source symbol's decoding matrix line for its corresponding line
		 *  - populate D accordingly
		 */

   		Iterator<EncodingSymbol> repair_symbol = repairSymbols.iterator();

   		// identify missing source symbols and replace their lines with "repair lines"
   		for (Integer missing_ESI : missingSourceSymbols()) {

   			EncodingSymbol repair = (EncodingSymbol) repair_symbol.next();
   			int row = S + H + missing_ESI;

   			// replace line S + H + missing_ESI with the line for encIndexes
   			Set<Integer> indexes = LinearSystem.encIndexes(kPrime, new Tuple(kPrime, repair.getISI(K)));

   			byte[] newLine = new byte[L];

   			for(Integer col : indexes)
   				newLine[col] = 1;

   			esiToLTCode.put(missing_ESI, constraint_matrix[row]);
   			constraint_matrix[row] = newLine;

   			// fill in missing source symbols in D with the repair symbols
   			D[row] = repair.data();
   		}				

   		// insert the values for overhead (repair) symbols					
   		for(int row = L; row < M; row++)
   		{
   			EncodingSymbol repair = (EncodingSymbol) repair_symbol.next();

   			// generate the overhead lines
   			Tuple tuple = new Tuple(K, repair.getISI(K));

   			Set<Integer> indexes = LinearSystem.encIndexes(K, tuple);

   			byte[] newLine = new byte[L];

   			for(Integer col : indexes)
   				newLine[col] = 1;

   			constraint_matrix[row] = newLine;

   			// update D with the data for that symbol
   			D[row] = repair.data();
   		}

   		/*
   		 * with the decoding matrix created and vector D populated,
   		 *  we have the system of linear equations ready to be solved 
   		 */

   		byte[] intermediate_symbols = null;
   		
   		try {
			intermediate_symbols = LinearSystem.PInactivationDecoding(constraint_matrix, D, T, kPrime);
		} catch (SingularMatrixException e) {
			
			return null;
		}
   		
   		return intermediate_symbols;
    }
}
