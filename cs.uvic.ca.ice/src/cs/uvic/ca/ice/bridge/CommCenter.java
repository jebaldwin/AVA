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
		private final int BUF_LEN = 16384;
		
		private Socket socket;
		
		public DataSourceThread(Socket s) {
			this.socket = s;
		}
		
		private int jsonLength(char[] msg_buf, int offset) {
			int json_len, json_objects;
			
			json_objects = 0;

			/* FIXME: This effectively skips an entire buffer if it spans
			 * multiple buffers.
			 */
			if(msg_buf[0] != '{')
				return -1;
			
			for(json_len = offset; json_len < BUF_LEN; json_len++)
			{
				if(msg_buf[json_len] == '{')
				{
					json_objects++;
				}
					
				if(msg_buf[json_len] == '}')
				{
					json_objects--;
				
					if(json_objects == 0)
					{
						json_len++;
						break;
					}
				}
			}
			
			if(json_objects != 0)
				return -1;
			
			return (json_len-offset);
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
			String msg_rem = null;
			Gson gson = new Gson();
			boolean json_obj = true;
			int json_str_ctr;
			
			for(;;) {
				//System.out.println("Waiting for data");
				
				try {
					sin.read(msg_buf);
				} catch (IOException e) {
					//System.out.println("read failed");
					break;
				}
				
				json_str_ctr = 0;
				json_obj = true;
				
				while(json_obj) {
					int json_len;
					
					/* Find end of JSON string */
					if(msg_rem != null) {
						int mrs = msg_rem.length(); 
						msg_rem += new String(msg_buf);
						json_len = jsonLength(msg_rem.toCharArray(), json_str_ctr);
						
						json_str_ctr = json_len - mrs;
						msg_rem = null;
					}
					
					json_len = jsonLength(msg_buf, json_str_ctr);
					if(json_len == -1)
						break;
					
					String json_str = new String(msg_buf, json_str_ctr, json_len);
					json_str_ctr += json_len;
					if(json_str_ctr >= BUF_LEN) {
						msg_rem = new String(msg_buf, json_str_ctr-json_len, json_len);
					}
					
					//System.out.println("Received -> (" + json_str.length() + ") :: " + json_str);
				
					Message msg = null;
					try {
						msg = gson.fromJson(json_str, Message.class);
						msg.setSocket(this.socket);
				
						msgQ.add(msg);

						CommCenter.this.setChanged();
						CommCenter.this.notifyObservers(msgQ);
					} catch(Exception e) {
						System.out.println("*** Failed to parse message ***");
						System.out.println("    Thrown: " + e);
						System.out.println("    State: msg=" + msg);
						System.out.println("           msgQ=" + msgQ);
						System.out.println("           gson=" + gson);
						e.printStackTrace();
					}
					
					if(0 <= json_str_ctr || json_str_ctr < BUF_LEN)
						json_obj = msg_buf[json_str_ctr] == '{' ? true : false;
					else
						json_obj = false;
				}
				
				Arrays.fill(msg_buf, '\0');
			}
		}
	}
}
