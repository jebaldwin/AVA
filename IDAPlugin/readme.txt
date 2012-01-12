This is a java plugin template for IDA Pro

Introduction:

The project is composed of two modules. The c++ plugin for IDA pro and the java socket communication project.

Features:

- Socket bidirectional communication between Ida Pro and a java app.
- Use of the event pool of an invisible windows within the plugin to catch incoming messages from socket communication and avoid conflicts with IDA's single thread.
- Prevent the plugin from being loaded more than once (singleton pattern & run once functionality).
- Start java UI automatically.
- Restart java UI automatically after being closed.
- Transfer ports between java plugin and IDA pro dynamically. (If a port is in uses, the port number will increase and a new port will be used.)
- SDK functions encapsulated in a class to minimize sdk version changes impacts.
- Visual studio property sheets configured for IDA plugins.

Here is the file structure:

.\javaPluginIDA     -> Folder containing the C++ plugin solution.
.\SocketModule      -> Folder containing the c++ communication library 
                      (Could be shared between many plugins c++ projects).

.\javaPluginIDA\javaPluginIda -> Folder containing the plugin's java module.
.\SocketModule\idaComm        -> Folder containing the java communication library .
                                 (Could be shared between many plugins java projects).

Development environment setup steps:

- Add the environment variable IDASDK	pointing to the IDA SDK (e.g. C:\IDA55\sdk). (Remember that Visual Studio needs to be closed to see that change)
- Add the environment variable IDAPATH pointing to the IDA path (e.g. C:\IDA55).
- Add C:\Program Files\Java\jdk1.x.0_xx\bin to your path to be able to run java.exe and javaw.exe.
- If you run from Visual Studio, you will see a message asking "Please specify the name of the executable file to be used for the debug session".
- Close Visual studio. A JavaPluginIDA.vcproj.COMPUTERNAME.USERNAME.user will have been created.
  Change 
    Command=""
    WorkingDirectory=""
  to
    Command="&quot;$(IDAPATH)\idag.exe&quot;"
    WorkingDirectory="&quot;$(IDAPATH)\plugins&quot;"
  in both debug and release section. This automatically starts IDA pro when running/debugging in visual studio.
- Now you can run the plugin from Visual Studio.
- By pressing Alt-J, the plugin will start but won't work. You need to create plugin's jar file and copy them in C:\IDA55\plugins\java folder.
- Add both java projects in Eclipse (.\SocketModule\idaComm and .\javaPluginIDA\javaPluginIda) (The javaPluginIda must have idaComm as required project in its build path. This is already set in the eclipse .project file)
- Export each project separately to javaPluginIDA.jar and idaComm.jar. Be sure you use the same name as in the global variable gClasspath in the file IDAModule.cpp.
- Copy javaPluginIDA.jar and idaComm.jar in C:\IDA55\plugins\java.
- Now you can run the plugin from Visual Studio (or directly in IDA pro) and the "Java plugin for IDA pro" example will be displayed.

NOTE: If you DON'T want the java module to load automatically when starting the plugin in IDA pro, to debug the java project for example, you can comment the line StartUI() in IDAModule.cpp in the function CIDAModule::onRun(). After pressing alt-J, the java app will not be loaded from C:\IDA55\plugins\java\javaPluginIDA.jar and you will be able to debug the java module in Eclipse. (See next Note)

NOTE: To debug/run with eclipse, your must add -p:40010 to the program arguments. This parameter is normally sent to the java communication module when starting the java app with startUI() function. (See the function StartUI() in IDAModule.cpp) Because this line is a comment, you need to do it manually.

- Visual studio property sheets configured for IDA plugins.
  - By starting with that template, everything is ready for running an IDA pro plugin.

- Developers notes:

  - Classpath
    - The java application may have dependencies on java libraries. These file must be included in the Classpath string. In the present example, there is two jar in the classpath: the main project jar (javaPluginIDA.jar) and the communication jar (idaComm.jar).
    - These files must be in [C:\IDA55]\plugins\java for the plugin to work ([C:\IDA55] will change depending on the IDA location on your machine) 
    
  - Run once functionality.
    void CIDAModule::onRun(int arg) in the file IDAModule.cpp has a flag to display a message if the user start the plugin more than once.
    This may be disabled if not needed of course.

  - Transfer ports between java plugin and IDA pro dynamically ( see IDAModule.cpp )
    - // Communication Step 1 - Instanciate the c++ server.
      m_pJavaComm = new CComm(this); // this is the CIDAModule instance. (Our communication class need to know the plugin to take action upon message reception.)
      
    - // Communication Step 2 - Start C++ socket server (Will try port 40010 first, then automatically increment port after)
      unsigned short javaServerPort = m_pJavaComm->initServer();
      
    - // Communication Step 3 - Send the C++ server port to the java app by command line argument.
                                It will send the port to the java app as a command prompt argument.
                                It will also send the CLASSPATH to the java app the same way. (See Developers notes)
			startUI(); // javaW.exe vs java.exe
		
		- // Communication Step 4 - Start the java server (Will try port 40000 first, then automatically increment port after)
                                The java client will start automatically and will connect to the c++ server port.
                                The java client will send the java server port to the c++ server. (See at the end of the readme.txt)
      this.disassemblerIF = new DisassemblerSocketComms("JavaPluginIDA", cppServerPort);
      this.disassemblerIF.addObserver(commObserver); // received message observer 
      disassemblerThread = new Thread(disassemblerIF);
      disassemblerThread.start();
	
	  - // Communication Step 5 - Start the c++ socket client and connect to the java server on the port we just received.
      setJavaServerPort((unsigned short)strtoul(message.substr(20).c_str(), NULL, 10)); // The substr(20) is to remove the "updateJavaServerPort" in the message received and keep only the port.
	    initClient();
	    
	  - // Communication Step 6 - Send and receive
      std::string ack = m_pJavaComm->send("Message");
      
      void CIDAModule::CComm::receive(const std::string &message)
      {
        //Do actions here with received message.
      }
      
	  - // Communication Step 7 - Send a "socket communication closed" message to java app.
      if (m_pJavaComm != NULL){
          m_pJavaComm->send(gBye); // This will tell the java app to close gracefully.
          delete(m_pJavaComm);
          m_pJavaComm = NULL;
      }

  C++ project files__________________________________________________________________________________________________________________________________ 
  
    Stuff shared between all plugins: (sorry for french comment in theses files. It's because we use them for a while)
      PluginBase    -> IDA pro plugin's base class. 
                       Encapsulate plugin within classes. 
                       Provide useful functions to work with menu.
      PLWInterface  -> Provide functions to hook to IDA pro events.
                       Ensure only one instance of the plugin to run at a time per IDA pro running.
      IdaSdk        -> Class containing useful function for the IDA sdk.
                       Avoid code duplication among plugins.
      Utils         -> Class containing useful function not related to the IDA sdk.
                       Avoid code duplication among plugins.
      Callback      -> Class used by PluginBase and PLWInterface.
      
      multithreadControlWindow -> Socket communication related class used to receive messages. The fact IDA pro is single thread prevents plugins to receive asynchronous messages. It causes access violations. This class creates an invisible window to receive message through its event queue to be treated when IDA is ready to do so. The C++ socket communication server is in this class.
      
    Stuff specific to a plugin:  
      IDAModule     -> Your plugin's main file! This class inherits from CPluginBase and contains code implementation specific to your plugin.
                       Contains messages strings to be displayed in the IDA pro console.
                       Contains also the Classpath string (see next).
      
      SocketComm    -> Socket communication class.
                       Used to connect and send messages to java app.
                       Implement the IReceive interface to treat messages received from the java app.
                         See the virtual pure function: virtual void receive (const std::string &buffer) = 0;
                         (See the notes below explaining)

- To treat received messages on the c++ side, we only have to define a class inside the plugin's class (IDAModule) that inherits CSocketComm and implement the receive method of the IReceive interface. When a message is received from the java app, the virtual receive function will be called. Because it will have been implemented inside the plugin, we will have everything needed to deal with the message.

  Java project files__________________________________________________________________________________________________________________________________
  
    Stuff shared between all plugins: (sorry for French comment in theses files. It's because we use them for a while)
      DisassemblerComm       -> Generic communication file.
                                Could be used with other communication methods (e.g. JNI, Shared memory, etc)
      DisassemblerSocketComm -> Socket communication specific file.
                                Very simple server to listen for a single client socket connection.
                                Inherits from DisassemblerComms.
      DisassemblerServer     -> Socket communication server.
      SocketClient           -> Wraps socket creation and connection to the c++ server.
      
    Stuff specific to a plugin:  
      PluginNameApp          -> Example code. (See below to know what to add to your application to use the communication module)
      
- The communication run in a separate thread and notify the application when new messages are received. To be notified, you need to implement the method update of a class Observer. In the present example, I defined a class called DisassemblerCommMessagesObserver which implement the update method. The received message will be the second argument of the method (arg). The observer instance needs to be passed to the disassemblerSocketComm instance just before launching it. (See the following example)

  // Class members:
	private DisassemblerSocketComms disassemblerIF;
	private Thread disassemblerThread = null;
  private DisassemblerCommMessagesObserver commObserver = new DisassemblerCommMessagesObserver();

  // In the application constructor:
  
  // Get the c++ server port (passed as argument in command)
  String cppServerPort = "";
	for (int i=0; i<args.length; i++) {
    if (args[i].contains(DEFAULT_PORT)) {
      //Remove the "-p:" in front of the port number.
      cppServerPort = ((String) args[i]).substring(3);
    }
	}
	
	// Start the communication and register our observer.
	this.disassemblerIF = new DisassemblerSocketComms("JavaPluginIDA", cppServerPort);
  this.disassemblerIF.addObserver(commObserver); // IDA position observer 
  disassemblerThread = new Thread(disassemblerIF);
	disassemblerThread.start();