package cs.uvic.ca.idaplugin.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.zest.custom.sequence.assembly.Activator;
import org.eclipse.zest.custom.sequence.assembly.Startup;
import org.eclipse.zest.custom.sequence.assembly.preferences.ASMPreferencePage;
import org.eclipse.zest.custom.sequence.assembly.preferences.PreferenceConstants;

/**
 * Very simple server to listen for a single client socket connection
 */
public class DisassemblerSocketComms extends DisassemblerComms {

	protected static final int SLEEP_TIME = 500; // 1000 - sleep for 1 second each iteration
    protected static int javaServerPort = 40000;
    public boolean isReady = false;
    public boolean idaOpen = false;
    
    // Client used to connect to IDA socket server.
	protected SocketClient socketClient;

	public DisassemblerSocketComms(String appName, String cppServerPort){
		super();
		this.appName = appName;
		initSocketClient(cppServerPort);
	}
	
    public void initSocketClient(String cppServerPort){
        int port = 0;
        try {
            //Instantiate socket client
            port = Integer.parseInt(cppServerPort);
        } catch (Exception e) {
            System.err.println(appName + " socket client initialization error: Port is not valid");
        }
        socketClient = new SocketClient(port);
   }

	@Override
	public synchronized void send(String message) {
		if(false /* Startup.disassemblerIF.idaOpen */ ){
			socketClient.send(message);
		} else {
			System.out.println("ida is not open");
		}
	}

	@Override
	public void close() {
		socketClient.send(BYE);
	}

    @Override
	public void run(){
		PrintWriter out = null;
		BufferedReader in = null;

		// Step 1: Create a ServerSocket.
    	DisassemblerServer socket = new DisassemblerServer(javaServerPort);
        // Update server port with the real socket used.
    	javaServerPort = socket.getJavaServerPort();

        // Here I must send the java server port to the IDA Pro c++ server
        // using the java client.
        if (socketClient.getCppServerPort() != 0){
        	if (socketClient.reStart()){
        		System.out.println(appName + " to IDA Pro communication established (Port " + socketClient.getCppServerPort() + ")");
        	}
       		idaOpen = socketClient.send("updateJavaServerPort " + Integer.toString(javaServerPort));

        	if(idaOpen){
				Startup.display.asyncExec(new Runnable() {
					public void run() {
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(Startup.display.getActiveShell());
						try {
							dialog.run(true, true, new IRunnableWithProgress(){
							    public void run(IProgressMonitor monitor) {
							        monitor.beginTask("Adding control flow information from IDAPro to the IDAPlugin project.", 300000);
							        while(!Startup.done){
							        	if(monitor.isCanceled()){
							        		break;
							        	}
							        	monitor.worked(1);
							        }
							        monitor.done();
							        
							        if(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_DEBUG).equals(PreferenceConstants.P_DEBUG_ALL)){
										Startup.send(null, "enableTracing");
								    } else {
								    	Startup.send(null, "disableTracing");
								    }
							        
							        if(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_LOG).equals(PreferenceConstants.P_LOG_INNER)){
										Startup.send(null, "enableInner");
								    } else {
								    	Startup.send(null, "disableInner");
								    }	
							        
							        int val = new Integer(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PREF_COUNT));
								    Startup.send(null, "prefCount " + val);//(val * 2));
							    }
							});
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							Startup.done = true;
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Startup.done = true;
							e.printStackTrace();
						}
				}});
        	}
		//}
        }

        String inputLine, outputLine;
        while (true) {
			try {
				// Step 2: Wait for a connection.
				socket.accept(); //Wait for a connection.

				// Step 3: Get input and output streams.
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// send connection successful message to client
				out.print(OK);
				out.flush();

		        try {
					// Step 4: Process connection (in a loop)
		            while ((inputLine = in.readLine()) != null) {
		                outputLine = processInput(inputLine);
		                out.println(outputLine);
		            }
		        } catch (java.net.SocketException e) { // ignore
		        	System.err.println(appName + " Socket exception: " + e.toString());
		        } finally {
		        	// Step 5: Close connection.
		        	out.close();
			        in.close();
			        socket.close();
		        }
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
        	try {
				Thread.sleep(SLEEP_TIME);	
			} catch (InterruptedException e2) {
				return;
			}
        }
    }
}