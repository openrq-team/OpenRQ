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

import java.util.Comparator;

final class ComparatorEncodingSymbol implements Comparator<EncodingSymbol>{

	@Override
	public int compare(EncodingSymbol s1, EncodingSymbol s2) {
		
		if(s1.getESI()<s2.getESI()){
			
			return -1;
		}
		
		else{
			
			if(s1.getESI()>s2.getESI()){
				
				return 1;
			}
			
			else{
				
				return 0;
			}
		}
	}
}