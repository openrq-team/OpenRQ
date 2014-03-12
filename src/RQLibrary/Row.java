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

import java.util.Set;

class Row{
	
	public int id;
	public int nonZeros;
	public int degree;
	public Set<Integer> edges = null;
	public boolean isHDPC;
	
	protected Row(int i, int r, int d, boolean hdpc){
		
		id = i;
		nonZeros = r;
		degree = d;
		isHDPC = hdpc;
	}
	
	protected Row(int i, int r, int d, boolean hdpc, Set<Integer> e){
	
		id = i;
		nonZeros = r;
		degree = d;
		edges = e;
		isHDPC = hdpc;
	}
	
	@Override
	public boolean equals(Object o){
		
		if(!o.getClass().getName().equals(this.getClass().getName())) 
			return false;
		else
			if(((Row) o).id == this.id)
				return true;
			else
				return false;
	}	
}