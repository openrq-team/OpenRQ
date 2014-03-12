/* 
 * Copyright 2014 Jose Lopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package RQLibrary;

import java.io.Serializable;

public final class EncodingPacket implements Serializable{

	private static final long serialVersionUID = -4830567467479891964L;
	private int SBN; 	// Source Block Number
	private int K; 		// Number of source symbols
	private int T; 		// Size of each source symbol
	private EncodingSymbol[] encoding_symbols;

	public EncodingPacket(int sBN, EncodingSymbol[] encoding_symbols, int k, int t) {

		/*
		if(sBN < 0) throw new IllegalArgumentException("Source Block Number must be non-negative.");
		if(encoding_symbols == null || encoding_symbols.length == 0) throw new IllegalArgumentException("The array of encoding symbols must be initialized/allocated.");
		if(t   < 1) throw new IllegalArgumentException("Size of source symbol must be positive.");
		if(k   < 1) throw new IllegalArgumentException("Number of source symbols must be positive.");
		*/
		
		SBN = sBN;
		this.encoding_symbols = encoding_symbols;
		K = k;
		T = t;
	}

	protected int getK() {
		
		return K;
	}

	protected int getT() {
		
		return T;
	}

	protected int getSBN() {
		
		return SBN;
	}

	public EncodingSymbol[] getEncoding_symbols() {
		
		return encoding_symbols;
	}	
}