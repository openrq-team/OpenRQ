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

package testing;

import java.util.Set;
import RQLibrary.Encoder;
import RQLibrary.SystematicIndices;
import RQLibrary.Tuple;

public class find1Degree {

	public static void main(String[] args){
		
		int K = 10;
		
		while(K <= 56403){

			int Ki = SystematicIndices.getKIndex(K);
			int S = SystematicIndices.S(Ki);
			int H = SystematicIndices.H(Ki);
			int L = K + S + H;

			for (int row = S + H; row < L; row++) {

				Tuple tuple = new Tuple(K, row - S - H);

				Set<Integer> indexes = Encoder.encIndexes(K, tuple);

				if(indexes.size() == 1)
					System.out.println("\nFound! - K: "+K+" row: "+(row-S-H));
			}

			K = SystematicIndices.ceil(++K);
		}
	}
}
