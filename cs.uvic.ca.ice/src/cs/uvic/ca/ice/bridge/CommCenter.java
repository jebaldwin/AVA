package cs.uvic.ca.ice.bridge;

/* NOTE:
 * 
 * The ONLY observer of this class is InstanceMap.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;

public class CommCenter extends Observable implements Runnable {
	private static final CommCenter instance = new CommCenter();
	
	private ConcurrentLinkedQueue<Message> msgQ;
	private final int LISTEN_PORT = 4040;

	private CommCenter() {}
	
	public static CommCenter getCommCenter() {
		return instance;
	}
	
	public void run() {
		ServerSocket localSocket;
		
		msgQ = new ConcurrentLinkedQueue<Message>();
	
		localSocket = null;
		try {
			localSocket = new ServerSocket(LISTEN_PORT, 5, InetAddress.getLoopbackAddress());
		} catch (IOException e) {
			System.err.println("Error: failed to initiate listen on localhost:" + LISTEN_PORT);
			e.printStackTrace();
		}
		
		if(localSocket == null) {
			return;
		}
		
		/* blocks until a client connects */
		while(true) {
			DataSourceThread dst;
			
			dst = null;
			try {
				//System.out.println("Waiting for clients");
				dst = new DataSourceThread(localSocket.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(dst != null)
				dst.start();
		}		
	}

	
	protected class DataSourceThread extends Thread {
		private final int BUF_LEN = 1024;
		
		private Socket socket;
		
		public DataSourceThread(Socket s) {
			this.socket = s;
		}

		public void run() {
			InputStream sis;
			
			try {
				sis = this.socket.getInputStream();
			} catch (IOException e) {
				System.out.println("InputStream failed");
				sis = null;
			}
			
			Gson gson = new Gson();
			String json_str;
			
			for(;;) {
				byte[] msg_buf;
				int l1, l2, cr, lf;
				int msg_len;
				try {
					l1 = sis.read();
					l2 = sis.read();
					msg_len = (l2 << 8) | l1;

					cr = sis.read(); /* CR */
					lf = sis.read(); /* LF */

					//System.out.println("-->\tl1: 0x"   + Integer.toHexString(l1));
					//System.out.println("\tl2: 0x"      + Integer.toHexString(l2));
					System.out.println("\tmsg_len: 0x" + Integer.toHexString(msg_len));
					//System.out.println("\tcr: 0x"      + Integer.toHexString(cr));
					//System.out.println("\tlf: 0x"      + Integer.toHexString(lf));
					
					msg_buf = new byte[msg_len];
					for(int i = 0; i < msg_len; i++) {
						int c = sis.read();  
						msg_buf[i] = (byte) c;
					}					
				} catch (IOException e) {
					break;
				}

				json_str = new String(msg_buf, 0, msg_len);
				//System.out.println("--> [" + msg_len + "] " + json_str);
				
				//System.out.println("Received -> (" + json_str.length() + ") :: " + json_str);
				
				Message msg = null;
				try {
					msg = gson.fromJson(json_str, Message.class);
					msg.setSocket(this.socket);
				
					msgQ.add(msg);

					CommCenter.this.setChanged();
					CommCenter.this.notifyObservers(msgQ);
				} catch(Exception e) {
					//e.printStackTrace();
				}
			}
		}
	}
}
