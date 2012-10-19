package org.eclipse.zest.custom.sequence.assembly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.zest.custom.sequence.assembly.editors.AssemblySequenceEditor;
import org.eclipse.zest.custom.sequence.assembly.model.XMLUtils;
import org.eclipse.zest.custom.statediagram.assembly.editors.StateEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cs.uvic.ca.idaplugin.comm.DisassemblerSocketComms;

public class Startup implements IStartup {

	// Associated communications interface to disassembler
	public static final String DEFAULT_PORT = "-p:";
	public static DisassemblerSocketComms disassemblerIF;
	protected Thread disassemblerThread = null;
	protected DisassemblerCommMessagesObserver commObserver;
	private String message;
	private static final String pageBreak = "!jeb!";
	public static String disassembledFilename = "";
	private static final int MAX_AVAILABLE = 1;
	public final static Semaphore available = new Semaphore(MAX_AVAILABLE, true);
	public static Display display = PlatformUI.getWorkbench().getDisplay();
	public static boolean done = false;
	public static HashMap<String, DisassemblerSocketComms> disassemblerIFs = new HashMap<String, DisassemblerSocketComms>();
	public static int idaprosOpen = 0;

	protected class DisassemblerCommMessagesObserver implements Observer {

		// @Override
		public void update(Observable o, Object arg) {
		}
			
		public void fake(Observable o, Object arg) {
			final String receivedMessage = (String) arg;
			System.out.println("received a message " + receivedMessage);
			if (receivedMessage.compareTo("bye") == 0) {
				// Close app.
				idaprosOpen--;
				if (idaprosOpen == 0) {
					System.exit(0);
				}
			} else {
				if (receivedMessage.indexOf("hello") >= 0) {
					idaprosOpen++;
					IProgressMonitor progressMonitor = new NullProgressMonitor();
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IProject project = root.getProject("IDAPlugin");

					try {
						if (!project.exists()) {
							project.create(progressMonitor);
						}
						project.open(progressMonitor);

						// String env = System.getenv().get("IDAPATH");
						IPath path = new Path(receivedMessage.substring(receivedMessage.indexOf(" ") + 1));

						// grab name of files
						String fileName = receivedMessage.substring(receivedMessage.lastIndexOf(" ") + 1);
						IPath pathName = new Path(fileName);

						File file = new File(path.toOSString());

						String fileString = path.toOSString();
						fileString = fileString.substring(fileString.lastIndexOf(File.separator) + 1);
						fileString = fileString.replace(".ose", "");

						DisassemblerSocketComms comm = Startup.disassemblerIFs.get("first");
						if (comm != null) {
							Startup.disassemblerIFs.remove("first");
							Startup.disassemblerIFs.put(fileString, comm);
							comm.send(message);
						} else {
							comm = Startup.disassemblerIFs.get(fileString);
						}

						disassembledFilename = fileString;

						// Destination directory
						File dir = new File(project.getLocation().toOSString());

						// remove file if it exists
						File currFile = new File(dir.getAbsolutePath() + File.separator + file.getName());
						if (currFile.exists()) {
							currFile.delete();
						}

						// Move file to new directory
						File renameFile = new File(dir, file.getName());
						if (renameFile.exists()) {
							renameFile.delete();
						}
						boolean success = file.renameTo(renameFile);
						if (success) {
							// need to create axml file
							currFile = new File(dir.getAbsolutePath() + File.separator + file.getName());
							File newFile = XMLUtils.parseTextToXML(currFile, null, null);
							project.refreshLocal(IResource.DEPTH_ONE, null);
						}
						done = true;
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if (receivedMessage.startsWith("response")) {
						Activator.getDefault().activeeditor.receiveMessage(receivedMessage);
						/*
						 * LinkedList<AssemblySequenceEditor> list =
						 * Activator.getDefault().editors; for (int i = 0; i <
						 * list.size(); i++) { AssemblySequenceEditor ase =
						 * list.get(i); ase.receiveMessage(receivedMessage); }
						 */
					} else {
						if (receivedMessage.startsWith("debug")) {
							// for the dynamic editor
							// System.out.println("sending to dynamic editor");
							if (Activator.getDefault().dynamicEditor != null) {
								Activator.getDefault().dynamicEditor.receiveMessage(receivedMessage);
							}
						} else {
							if (receivedMessage.startsWith("navigate")) {
								// for the history editor
								// System.out.println("sending to navigation editor");
								if (Activator.getDefault().navEditor != null) {
									Activator.getDefault().navEditor.receiveMessage(receivedMessage);
								}
							} else {
								if (receivedMessage.startsWith("rename")) {
									/*
									 * if (Activator.getDefault().editor !=
									 * null) {
									 * Activator.getDefault().editor.receiveMessage
									 * (receivedMessage); }
									 */

									String[] lines = receivedMessage.split("\t");
									// rename in XML file
									// if
									// (store.getString(PreferenceConstants.P_STATIC).equals(PreferenceConstants.P_STATIC_RET))
									// {
									String original = lines[1];
									String newname = lines[2];
									String module = lines[3];

									LinkedList<AssemblySequenceEditor> list = Activator.getDefault().editors;
									LinkedList<AssemblySequenceEditor> thislist = new LinkedList<AssemblySequenceEditor>();
									AssemblySequenceEditor ase = null;

									for (int i = 0; i < list.size(); i++) {
										ase = list.get(i);
										if (ase.inputFile.getName().replace("axml", "exe").equals(module)) {
											thislist.add(ase);
										}
									}

									// create document from XML file
									Document document = null;
									Document pdocument = null;
									File rfile = null;
									File pfile = null;
									if (ase != null) {
										document = ase.document;
									} else {
										String filename = module.replace("exe", "axml");
										String persfilename = filename + ".dat";
										IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject("IDAPlugin").getFile(filename);
										IFile persfile = ResourcesPlugin.getWorkspace().getRoot().getProject("IDAPlugin").getFile(persfilename);
										String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + file.getFullPath().toOSString();
										String ppath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + persfile.getFullPath().toOSString();
										rfile = new File(path);
										if (!rfile.exists()) {
											try {
												rfile.createNewFile();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										pfile = new File(ppath);
										if (!pfile.exists()) {
											try {
												pfile.createNewFile();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										document = XMLUtils.createSequenceFromXML(rfile);
										pdocument = XMLUtils.createSequenceFromXML(pfile);
									}

									if (document != null && pdocument != null) {
										NodeList nodes = document.getElementsByTagName("function");
										for (int i = 0; i < nodes.getLength(); i++) {
											Element node = (Element) nodes.item(i);
											if (node.getAttribute("name").equals(original) && node.getAttribute("module").equals(module)) {
												node.setAttribute("name", newname);
											}
										}

										nodes = document.getElementsByTagName("call");
										for (int j = 0; j < nodes.getLength(); j++) {
											Element node = (Element) nodes.item(j);
											if (node.getAttribute("name").equals(original) && node.getAttribute("module").equals(module)) {
												node.setAttribute("name", newname);
											}
										}

										nodes = pdocument.getElementsByTagName("function");
										for (int j = 0; j < nodes.getLength(); j++) {
											Element node = (Element) nodes.item(j);
											if (node.getAttribute("name").equals(original) && node.getAttribute("module").equals(module)) {
												node.setAttribute("name", newname);
											}
										}

										nodes = pdocument.getElementsByTagName("expanded");
										for (int j = 0; j < nodes.getLength(); j++) {
											Element node = (Element) nodes.item(j);
											if (node.getAttribute("name").equals(original) && node.getAttribute("module").equals(module)) {
												node.setAttribute("name", newname);
											}
										}

										nodes = pdocument.getElementsByTagName("selection");
										for (int j = 0; j < nodes.getLength(); j++) {
											Element node = (Element) nodes.item(j);
											if (node.getAttribute("name").equals(original) && node.getAttribute("module").equals(module)) {
												node.setAttribute("name", newname);
											}
										}

										nodes = pdocument.getElementsByTagName("root");
										for (int j = 0; j < nodes.getLength(); j++) {
											Element node = (Element) nodes.item(j);
											if (node.getAttribute("name").equals(original) && node.getAttribute("module").equals(module)) {
												node.setAttribute("name", newname);
											}
										}

										// write to file
										// Prepare the DOM document for writing
										// can I just save the document on
										// editor exit?
										Source source = new DOMSource(document);

										Result result = new StreamResult(rfile);
										// Write the DOM document to the file
										TransformerFactory tf = TransformerFactory.newInstance();
										Transformer transformer;
										try {
											transformer = tf.newTransformer();
											transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
											transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
											transformer.setOutputProperty(OutputKeys.INDENT, "yes");
											transformer.transform(source, result);
										} catch (TransformerException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										// write to file
										// Prepare the DOM document for writing
										// can I just save the document on
										// editor exit?
										source = new DOMSource(pdocument);

										result = new StreamResult(pfile);
										// Write the DOM document to the file

										try {
											transformer = tf.newTransformer();
											transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
											transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
											transformer.setOutputProperty(OutputKeys.INDENT, "yes");
											transformer.transform(source, result);
										} catch (TransformerException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										display.asyncExec((new Runnable() {
											public void run() {
												try {
													ResourcesPlugin.getWorkspace().getRoot().getProject("IDAPlugin").refreshLocal(IResource.DEPTH_ONE, null);
												} catch (CoreException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
												// viewer.refresh();
											}
										}));

										for (int i = 0; i < thislist.size(); i++) {
											thislist.get(i).receiveMessage(receivedMessage);
										}
									}

									if (Activator.getDefault().dynamicEditor != null) {
										Activator.getDefault().dynamicEditor.receiveMessage(receivedMessage);
									}

									if (Activator.getDefault().navEditor != null) {
										Activator.getDefault().navEditor.receiveMessage(receivedMessage);
									}
								} else {
									if(receivedMessage.startsWith("innerloop")){
										if(Activator.getDefault().dynamicEditor != null){
											Activator.getDefault().dynamicEditor.receiveMessage(receivedMessage);
										}
									} else {
										if(receivedMessage.startsWith("outputActions")){
											StateEditor.receiveMessage(receivedMessage);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void earlyStartup() {
		URL installLoc = Platform.getInstallLocation().getURL();

		final File newFile = new File(installLoc.getPath() + "ports.txt");
		// final File newFile = new File("c:\\\\IDA\\plugins\\java\\ava\\" +
		// "ports.txt");
		if (newFile.exists()) {
			newFile.delete();
		}
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		// monitor a single file
		TimerTask task = new FileWatcher(newFile) {
			protected void onChange(File file) {
				// open file and read last entry and create dissasemblerIF
				BufferedReader input;
				try {
					input = new BufferedReader(new FileReader(newFile));
					try {
						String line = null; // not declared within while loop
						String lastline = "";
						while ((line = input.readLine()) != null) {
							lastline = line;
						}
						String[] tokens = lastline.split("\t");
						DisassemblerSocketComms sockcomm = new DisassemblerSocketComms("JavaPluginIDA", tokens[0]);
						sockcomm.addObserver(new DisassemblerCommMessagesObserver());
						Thread sockcommThread = new Thread(sockcomm);
						sockcommThread.start();
						disassemblerIFs.put(tokens[1], sockcomm);
					} finally {
						input.close();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Timer timer = new Timer();
		// repeat the check every second
		//timer.schedule(task, new Date(), 1000);

/*
		String cppServerPort = "";
		String args[] = Platform.getApplicationArgs();
		for (int i = 0; i < args.length; i++) {
			if (args[i].contains(DEFAULT_PORT)) {
				// Remove the "-p:" in front of the port number.
				cppServerPort = args[i].substring(3);
			}
		}

		if (cppServerPort.length() < 0) {
			cppServerPort = "-p:40010";
			cppServerPort = cppServerPort.substring(3);
		}

		if (cppServerPort != "") {
			// Instantiate the controller and start communication.
			disassemblerIF = new DisassemblerSocketComms("JavaPluginIDA", cppServerPort);
			commObserver = new DisassemblerCommMessagesObserver();
			disassemblerIF.addObserver(commObserver); // received message
			// observer
			disassemblerThread = new Thread(disassemblerIF);
			disassemblerThread.start();

			// don't know the module filename yet
			disassemblerIFs.put("first", disassemblerIF);
		} else {
			// textArea.setText("Please give the port number as argument like:\n-p:40010 for the communication to work.");
		}
*/
	}

	public static void send(String ID, String message) {
		System.out.println("Sending : " + message + " to IDA, file " + ID);
		if (ID == null) {
			// send to all
			Collection c = Startup.disassemblerIFs.values();
			for (Iterator iterator = c.iterator(); iterator.hasNext();) {
				DisassemblerSocketComms comm = (DisassemblerSocketComms) iterator.next();
				comm.send(message);
			}
			/*
			 * for (int i = 0; i < Startup.disassemblerIFs.size(); i++) {
			 * Startup.disassemblerIFs..get(i).send(message); }
			 */
		} else {
			Startup.disassemblerIFs.get(ID).send(message);
			// Startup.disassemblerIFs.get("first").send(message);
		}
	}
}
