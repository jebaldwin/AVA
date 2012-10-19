package cs.uvic.ca.ice.bridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.CharBuffer;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.omg.CORBA.portable.InputStream;

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
				System.out.println("Waiting for clients");
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
			
			for(;;) {
				System.out.println("Waiting for data");
				
				try {
					sin.read(msg_buf);
				} catch (IOException e) {
					System.out.println("read failed");
					break;
				}
				
				/* Find end of JSON string */
				int json_len, json_objects;
				json_objects = 0;
				for(json_len = 0; json_len < BUF_LEN; json_len++)
				{
					if(msg_buf[json_len] == '{')
						json_objects++;
					
					if(msg_buf[json_len] == '}')
					{
						json_objects--;
					
						if(json_objects == 0)
							json_len++;
							break;
					}
				}

				String json_str = new String(msg_buf, 0, json_len);
				
				System.out.println("json_len: " + json_len);
				System.out.println("Received -> (" + json_str.length() + ") :: " + json_str);
				
				msgQ.add(gson.fromJson(json_str, Message.class));
				CommCenter.this.setChanged();
				CommCenter.this.notifyObservers(msgQ);
				msgQ.remove();
			}
		}
	}
}
