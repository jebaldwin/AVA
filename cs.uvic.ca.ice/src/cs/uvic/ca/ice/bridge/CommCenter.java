package cs.uvic.ca.ice.bridge;

/* NOTE:
 * 
 * The ONLY observer of this class is InstanceMap.
 */

import java.io.BufferedReader;
import java.io.IOException;
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
			BufferedReader sin;

			try {
				sin = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			} catch (IOException e) {
				System.out.println("BufferedReader failed");
				sin = null;
			}
			
			char[] msg_buf = new char[BUF_LEN];
			Gson gson = new Gson();
			String json_str;
			
			for(;;) {
				//System.out.println("Waiting for data");
				
				int l1, l2, msg_len;
				try {
					l1 = sin.read();
					l2 = sin.read();
					msg_len = l1 ^ l2;
					sin.read();
					sin.read();
					sin.read(msg_buf, 0, msg_len);
				} catch (IOException e) {
					//System.out.println("read failed");
					break;
				}

				System.out.println("msg_len: " + msg_len);
				
				json_str = new String(msg_buf, 0, msg_len);
				
				System.out.println("Received -> (" + json_str.length() + ") :: " + json_str);
				
				Message msg = null;
				try {
					msg = gson.fromJson(json_str, Message.class);
					msg.setSocket(this.socket);
				
					msgQ.add(msg);

					CommCenter.this.setChanged();
					CommCenter.this.notifyObservers(msgQ);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				Arrays.fill(msg_buf, '\0');
			}
		}
	}
}
