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

public class SingularMatrixException extends Exception{

	private static final long serialVersionUID = -5498117821525565618L;
	public static final String msg = "Matrix is singular, therefore not invertible.";
	
	public SingularMatrixException(){
		
		super(msg);
	}
	
	public SingularMatrixException(String message){
		
		super(message);
	}
	
	public SingularMatrixException(String message, Throwable cause){
		
		super(message, cause); 
	}
	
	public SingularMatrixException(Throwable cause){
		
		super(cause); 
	}
}