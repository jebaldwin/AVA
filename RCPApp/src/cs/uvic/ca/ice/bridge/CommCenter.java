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
				dst = new DataSourceThread(localSocket.accept());
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
			try {
				PrintWriter sout = new PrintWriter(this.socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader sin;
			sin = null;
			try {
				sin = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String inputLine, outputLine;
			char[] msg_buf = new char[BUF_LEN];
			Gson gson = new Gson();
			
			for(;;) {
				try {
					sin.read(msg_buf);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(msg_buf);
				msgQ.add(gson.fromJson(new String(msg_buf), Message.class));				setChanged();
				notifyObservers(msgQ);
				msgQ.remove();
			}
		}
	}
}
