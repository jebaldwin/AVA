package cs.uvic.ca.idaplugin.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Wraps socket creation and connection to a server
 */
public class SocketClient {
	
	private int cppServerPort = 0;
	private PrintWriter output;
	private BufferedReader input;
	private String host = "127.0.0.1";
	private Socket clientSocket = null;
	private boolean clientstarted = false;
	
	public SocketClient(int port){
        cppServerPort = port;
	}
	
    public int getCppServerPort(){
        return cppServerPort;
    }

	private boolean Start() {
		try {
			// Step 1: Create a Socket to make connection
			// In Step 1, we create a Socket to connect to the server.
			// The Socket constructor establishes the connection to the server.
			// If the connection attempt is successful, this statement returns a Socket.
			// create Socket to make connection to server
			clientSocket = new Socket(InetAddress.getByName( host ), cppServerPort );

			// Step 2: Get the input and output streams
			// In Step 2, the client uses Socket methods getInputStream and getOutput-
			// Stream to obtain references to the Sockets InputStream and OutputStream.
			// set up output stream for objects
			output = new PrintWriter(clientSocket.getOutputStream(), true);

			// flush output buffer to send header information
			output.flush();

			// set up input stream for objects
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			clientstarted = true;
			return clientstarted;
		} catch ( IOException ioException ) {
            System.err.println("Java socket client is unable to connect to the cpp server on port: " + cppServerPort + "\n" + "Error: " + ioException.toString());
		}
		return false;
	}
	
	public boolean reStart(){
		if (clientstarted){
			System.out.println("Java socket client is restarting because a new IDA pro socket server is running.\n");
			closeConnection();
		}
		return Start();
	}

	private void closeConnection(){
		try {
			// Step 4: Close connection
			// In Step 4, the client closes the connection
			// when the transmission is complete by invoking the close method on the Socket and the
			// corresponding streams.
			output.close();
			input.close();
			clientSocket.close();
			clientstarted = false;
		}
		// process problems communicating with server
		catch ( IOException ioException ) {
			ioException.printStackTrace();
		}
	}
	
    @Override
	protected void finalize(){
		closeConnection();
    }
	
	// send message to server
	public boolean send( String message )
	{
		// send object to server
		try {
			output.print(message);
			output.flush();
			return true;
		}
		// process problems sending object
		catch ( Exception exception ) {
			System.err.println("sendData: Error writing object " + exception.toString());					
			return false;
		}
	}
}