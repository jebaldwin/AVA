The binaries and source code for the AVA framework can be retrieved from GitHub
at https://github.com/jebaldwin/AVA. Once the binaries have been downloaded,
please copy the contents of the plugins folder to the plugins folder of the IDA Pro
installation. You should have SequenceDiagramPlugin.plw under the plugins folder.
The java folder with its contents should also reside in the same folder, for example
C:/IDA/plugins/java/ava/ava.exe.

You may now run AVA by selecting \AVA Framework" under the plugins menu
in IDA Pro. AVA will then launch and the connection with IDA Pro will be created
automatically. If this plugin is selected from another running IDA Pro then it will
connect with the single instance of AVA and not launch another.


Setting up the Development Environment:

In order to develop for AVA, you will need to download the latest version of Eclipse
for RCP and RAP developers. You will also need Visual C++ installed. Visual
C++ 2008 Express Edition is recommended. AVA uses the Diver framework to create 
sequence diagrams, with minimal changes. The Diver framework with modications
is available at https://github.com/jebaldwin/Diver. You will need the
org.eclipse.zest.custom.sequence project to compile AVA.

There are 4 source folders on the AVA GitHub repository (https://github.com/jebaldwin/AVA). These are:

IDAPlugin contains the code for the IDA Pro plugin
RCPApp contains the Eclipse code for the standalone RCP executable application
SocketModule contains the code for socket communication between the IDA Pro (C++) and Eclipse (Java) plugins
org.eclipse.zest.custom.sequence.assembly contains the Eclipse plugin code for the Assembly extension to Diver

In order to run the code, you will need to have two path variables set up as follows:

IDASDK = IDA Pro SDK directory
IDAPATH = IDA Pro installation directory

You will also need to have Java installed and included on your path.
When debugging or running the application from Visual Studio, you will see a
message asking "Please specify the name of the executable le to be used for the
debug session". Close Visual Studio and a vcproj le containing your computer and
user name will have been created. The following lines in both the debug and release
section must be changed to:

Command="&quot;$(IDAPATH)\idag.exe&quot;"
WorkingDirectory="&quot;$(IDAPATH)\plugins&quot;"

When debugging or running from Visual Studio, the SequenceDiagramPlugin.plw
file will be automatically created and placed in the IDA Pro plugin directory. If you
make changes to the Eclipse plugins, you can re-export the ava.exe file and place it
within the java/ava folder under the IDA Pro plugins folder.

For easier debugging of the Eclipse plugin, you can establish the IDA Pro link
without launching the executable. To do so, you must add -p:40010 to the Eclipse
program arguments. This parameter is normally sent to the Java communication
module when starting it from the IDA pro plugin. Next comment out the call to
startUI() in the onRun function in IDAModule.cpp (line 249 at time of writing).

Finally there may be some dependencies that will need to be installed in your
development Eclipse workbench to use all features of AVA. You will definitely need
the following:

Zest: The Eclipse Visualization Toolkit (http://www.eclipse.org/gef/zest/)
AJDT: AspectJ Development Tools (http://www.eclipse.org/ajdt/)
