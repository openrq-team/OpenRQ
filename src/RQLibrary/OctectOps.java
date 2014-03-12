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

import java.util.Arrays;

abstract class OctectOps {
	
	protected static final short UNSIGN(byte b){
		
		return((short) (b & 0xFF));
	}

	protected static final byte getExp(int i){
		
		if(i >= 0)
			return (byte)OCT_EXP[i];
		else
			return (byte)OCT_EXP[UNSIGN((byte)i)];
	}		
	
	protected static final byte getLog(int i){

		return ((byte)OCT_LOG[UNSIGN((byte)i)]);		
	}

	protected static final byte addition(byte u, byte v){

		return (byte) (u ^ v);
	}
	
	protected static final byte subtraction(byte u, byte v){

		return addition(u,v);
	}
	
	protected static final byte division(byte u, byte v){
		
//		if(v == 0) throw new IllegalArgumentException("Denominator cannot be 0.");
		
		if(v == 1) 
			return u;
		
		if(u == 0) 
			return 0;
		else{
			
			byte quotient = getExp(UNSIGN(getLog(u-1)) - UNSIGN(getLog(v-1)) + 255);
			
			return quotient;
		}
	}
	
	protected static final byte product(byte u, byte v){
		
		if(u == 0 || v == 0) 
			return 0;
		
		if(u == 1)
			return v;
		
		if(v == 1)
			return u;
		else{
			
			byte product = getExp(UNSIGN(getLog(u-1)) + UNSIGN(getLog(v-1)));
			
			return product;
		}
	}
	
	protected static final byte alphaPower(int i){

		return getExp(i);
	}
	
	protected static final byte[] betaProduct(byte beta, byte[] U){
		
//		if(U == null || U.length == 0) throw new IllegalArgumentException("Array must be initialized/allocated.");
		
		if(beta == 1)
			return Arrays.copyOf(U, U.length);
		
		byte[] betaProduct = new byte[U.length];
		
		if(beta == 0) 
			return(betaProduct);
		
		for(int i=0; i<U.length; i++)
			betaProduct[i] = product(beta, U[i]);
		
		return betaProduct;
	}
	
	protected static final byte[] betaDivision(byte[] U, byte beta){
		
//		if(U == null || U.length == 0) throw new IllegalArgumentException("Array must be initialized/allocated.");
		
		if(beta == 1)
			return Arrays.copyOf(U, U.length);
		
		byte[] betaProduct = new byte[U.length];
		
		for(int i=0; i<U.length; i++)
			betaProduct[i] = division(U[i], beta);
		
		return betaProduct;
	}
	
	// betas com posicoes
	protected static final byte[] betaProduct(byte beta, byte[] U, int pos, int length){

//		if(U == null || U.length == 0) throw new IllegalArgumentException("Array must be initialized/allocated.");
		
		byte[] betaProduct = new byte[length];
		
		if(beta == 0) 
			return(betaProduct);
		
		for(int i=0; i<length; i++)
			betaProduct[i] = product(beta, U[i+pos]);
		
		return betaProduct;
	}
	
	protected static final byte[] betaDivision(byte[] U, int pos, int length, byte beta){

//		if(U == null || U.length == 0) throw new IllegalArgumentException("Array must be initialized/allocated.");
		
		byte[] betaProduct = new byte[length];

		for(int i=0; i<length; i++)
			betaProduct[i] = division(U[i+pos], beta);

		return betaProduct;
	}
	
	private static final char[] OCT_EXP = 
		{
			1, 2, 4, 8, 16, 32, 64, 128, 29, 58, 116, 232, 205, 135, 19, 38, 76,
			152, 45, 90, 180, 117, 234, 201, 143, 3, 6, 12, 24, 48, 96, 192, 157,
			39, 78, 156, 37, 74, 148, 53, 106, 212, 181, 119, 238, 193, 159, 35,
			70, 140, 5, 10, 20, 40, 80, 160, 93, 186, 105, 210, 185, 111, 222,
			161, 95, 190, 97, 194, 153, 47, 94, 188, 101, 202, 137, 15, 30, 60,
			120, 240, 253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223, 163,
			91, 182, 113, 226, 217, 175, 67, 134, 17, 34, 68, 136, 13, 26, 52,
			104, 208, 189, 103, 206, 129, 31, 62, 124, 248, 237, 199, 147, 59,
			118, 236, 197, 151, 51, 102, 204, 133, 23, 46, 92, 184, 109, 218,
			169, 79, 158, 33, 66, 132, 21, 42, 84, 168, 77, 154, 41, 82, 164, 85,
			170, 73, 146, 57, 114, 228, 213, 183, 115, 230, 209, 191, 99, 198,
			145, 63, 126, 252, 229, 215, 179, 123, 246, 241, 255, 227, 219, 171,
			75, 150, 49, 98, 196, 149, 55, 110, 220, 165, 87, 174, 65, 130, 25,
			50, 100, 200, 141, 7, 14, 28, 56, 112, 224, 221, 167, 83, 166, 81,
			162, 89, 178, 121, 242, 249, 239, 195, 155, 43, 86, 172, 69, 138, 9,
			18, 36, 72, 144, 61, 122, 244, 245, 247, 243, 251, 235, 203, 139, 11,
			22, 44, 88, 176, 125, 250, 233, 207, 131, 27, 54, 108, 216, 173, 71,
			142, 1, 2, 4, 8, 16, 32, 64, 128, 29, 58, 116, 232, 205, 135, 19, 38,
			76, 152, 45, 90, 180, 117, 234, 201, 143, 3, 6, 12, 24, 48, 96, 192,
			157, 39, 78, 156, 37, 74, 148, 53, 106, 212, 181, 119, 238, 193, 159,
			35, 70, 140, 5, 10, 20, 40, 80, 160, 93, 186, 105, 210, 185, 111,
			222, 161, 95, 190, 97, 194, 153, 47, 94, 188, 101, 202, 137, 15, 30,
			60, 120, 240, 253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223,
			163, 91, 182, 113, 226, 217, 175, 67, 134, 17, 34, 68, 136, 13, 26,
			52, 104, 208, 189, 103, 206, 129, 31, 62, 124, 248, 237, 199, 147,
			59, 118, 236, 197, 151, 51, 102, 204, 133, 23, 46, 92, 184, 109, 218,
			169, 79, 158, 33, 66, 132, 21, 42, 84, 168, 77, 154, 41, 82, 164, 85,
			170, 73, 146, 57, 114, 228, 213, 183, 115, 230, 209, 191, 99, 198,
			145, 63, 126, 252, 229, 215, 179, 123, 246, 241, 255, 227, 219, 171,
			75, 150, 49, 98, 196, 149, 55, 110, 220, 165, 87, 174, 65, 130, 25,
			50, 100, 200, 141, 7, 14, 28, 56, 112, 224, 221, 167, 83, 166, 81,
			162, 89, 178, 121, 242, 249, 239, 195, 155, 43, 86, 172, 69, 138, 9,
			18, 36, 72, 144, 61, 122, 244, 245, 247, 243, 251, 235, 203, 139, 11,
			22, 44, 88, 176, 125, 250, 233, 207, 131, 27, 54, 108, 216, 173, 71,
			142
		};
	
	private static final char[] OCT_LOG = 
		{
			0, 1, 25, 2, 50, 26, 198, 3, 223, 51, 238, 27, 104, 199, 75, 4, 100,
			224, 14, 52, 141, 239, 129, 28, 193, 105, 248, 200, 8, 76, 113, 5,
			138, 101, 47, 225, 36, 15, 33, 53, 147, 142, 218, 240, 18, 130, 69,
			29, 181, 194, 125, 106, 39, 249, 185, 201, 154, 9, 120, 77, 228, 114,
			166, 6, 191, 139, 98, 102, 221, 48, 253, 226, 152, 37, 179, 16, 145,
			34, 136, 54, 208, 148, 206, 143, 150, 219, 189, 241, 210, 19, 92,
			131, 56, 70, 64, 30, 66, 182, 163, 195, 72, 126, 110, 107, 58, 40,
			84, 250, 133, 186, 61, 202, 94, 155, 159, 10, 21, 121, 43, 78, 212,
			229, 172, 115, 243, 167, 87, 7, 112, 192, 247, 140, 128, 99, 13, 103,
			74, 222, 237, 49, 197, 254, 24, 227, 165, 153, 119, 38, 184, 180,
			124, 17, 68, 146, 217, 35, 32, 137, 46, 55, 63, 209, 91, 149, 188,
			207, 205, 144, 135, 151, 178, 220, 252, 190, 97, 242, 86, 211, 171,
			20, 42, 93, 158, 132, 60, 57, 83, 71, 109, 65, 162, 31, 45, 67, 216,
			183, 123, 164, 118, 196, 23, 73, 236, 127, 12, 111, 246, 108, 161,
			59, 82, 41, 157, 85, 170, 251, 96, 134, 177, 187, 204, 62, 90, 203,
			89, 95, 176, 156, 169, 160, 81, 11, 245, 22, 235, 122, 117, 44, 215,
			79, 174, 213, 233, 230, 231, 173, 232, 116, 214, 244, 234, 168, 80,
			88, 175			
		};
}