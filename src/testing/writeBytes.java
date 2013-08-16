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