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

class SubBlock {

	private byte[] data;
	private long T;
	
	protected SubBlock(byte[] d, long t){
		
		this.data = d;
		this.T = t;
	}

	protected long getT() {
		
		return T;
	}

	protected void setT(long t) {
		
		T = t;
	}

	protected byte[] getData() {
		
		return data;
	}

	protected void setData(byte[] data) {
		
		this.data = data;
	}	
}