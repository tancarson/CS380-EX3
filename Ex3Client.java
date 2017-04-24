import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Ex3Client {

	static Socket socket = null;
	static InputStream in = null;
	static OutputStream out = null;
	
	static int[] arrayBytes = null;
	static int[] arrayShorts = null;

	public static void main(String[] args) {

		// establish connection to server
		try {
			socket = new Socket("codebank.xyz", 38103);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//receive array size
		try {
			int arraySize = in.read();
			arrayBytes = new int[arraySize];
			
			//needed because if odd the last byte has to be converted
			if(arraySize % 2 == 0) arrayShorts = new int[arraySize/2];
			else arrayShorts = new int[arraySize/2 + 1];
			
			System.out.println("Array Size: " + arraySize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//read bytes from server
		for(int i = 0; i < arrayBytes.length; i++){
			try {
				arrayBytes[i] = in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Recieved from server: " + Arrays.toString(arrayBytes));
		
		//convert u_bytes into u_shorts and if odd add the last one as the upper
		for(int i = 0; i < arrayShorts.length; i++){
			if(i == arrayShorts.length - 1 && arrayBytes.length%2 == 1){
				arrayShorts[i] = (arrayBytes[i*2] << 8);
			}
			else{
				arrayShorts[i] = ((arrayBytes[i*2] << 8) + arrayBytes[i*2+1]);
			}
		}
		
		//send checksum to server
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort(checksum(arrayShorts));
		byte[] result = b.array();
		System.out.printf("Checksum result: %02x %02x\n", result[0], result[1]);
		for(int i = 0; i < 2; i++){
			try {
				out.write(result[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//receive server result
		try {
			System.out.println("Server Response: " + in.read());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//perform checksum (no u_shorts so I had to use bytes)
	public static short checksum(int[] b){
		long sum = 0;
		for(int i = 0; i < b.length; i++){
			sum += b[i];
			if((sum & 0xFFFF0000) != 0){
				
				sum &= 0xFFFF;
				sum++;
			}
		}
		return (short)~(sum & 0xFFFF);
	}
}
