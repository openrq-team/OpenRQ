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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class writeBytes {

	public static void main(String[] args) {

		int N = 100000000;
		byte[] buffer = new byte[N];
		
		for(int i = 0; i < N;)
			for(int bite = 0; bite < 256 && i < N ; bite++, i++)
				buffer[i] = (byte) i;
		
		try {
			FileOutputStream fos = new FileOutputStream(new File("" + N));
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			bos.write(buffer);
			bos.flush();
			bos.close();
		}catch(Exception e){
			System.err.println("err");
		}
	}
}
