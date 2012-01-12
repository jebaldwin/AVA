package cs.uvic.ca.idaplugin.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Wraps socket creation and tear down functionality
 */
public class DisassemblerServer {
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
    private int javaServerPort;

    public int getJavaServerPort(){
        return javaServerPort;
    }
    
	public DisassemblerServer(int port){
        javaServerPort = port;
        for (int i=0; i<9; i++){
            try {
                serverSocket = new ServerSocket(javaServerPort);
                return;
            } catch (IOException e) {
                if (i == 9){
                    System.err.println("Could not listen on port: " + port);
                } else {
                    javaServerPort++;
                }
            }
        }
	}
	
	// Attempt to connect, returning true if we can
	public boolean accept() {
        try {
            clientSocket = serverSocket.accept();
            return true;
        }
        catch (IOException e) {
            System.err.println("Accept failed.");
        }
        return false;
	}
	
	public OutputStream getOutputStream() {
		try {
			return clientSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public InputStream getInputStream() {
		try {
			return clientSocket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Close all sockets
	 */
	public void close() {
        try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
