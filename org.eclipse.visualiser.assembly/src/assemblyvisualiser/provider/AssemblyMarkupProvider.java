package assemblyvisualiser.provider;


import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import assemblyvisualiser.AssemblyVisualiserPlugin;

public class AssemblyMarkupProvider extends SimpleMarkupProvider { //implements ISelectionListener {

	private static String WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
	private final static boolean debugLoading = false;

	private Map kinds;

	/**
	 * Initialise the provider - loads markup information from a file
	 */
	public void initialise() {
		kinds = new HashMap();
	}

	/**
	 * Load the markup information from given input stream
	 * 
	 * @param in
	 */
	public void loadMarkups(InputStream in) {
		int scount = 0; // How many stripes added altogether

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();

			// Go through the file until we hit the end
			// Each line has a format like:
			// Stripe:ABC.A Kind:S1 Offset:5 Depth:1
			while (line != null && line.length() != 0) {

				// Process lines starting Stripe:
				if (line.startsWith("Stripe:")) { //$NON-NLS-1$
					String membername = null;
					String kindStr = null;
					int offset = 0;
					int depth = 1;

					// Retrieve the fully qualified membername, e.g. ABC.A
					membername = retrieveKeyValue("Stripe:", line); //$NON-NLS-1$

					// Retrieve the Kind:, e.g. S1
					kindStr = retrieveKeyValue("Kind:", line); //$NON-NLS-1$
					IMarkupKind kind;
					if (kinds.get(kindStr) instanceof IMarkupKind) {
						kind = (IMarkupKind) kinds.get(kindStr);
					} else {
						kind = new SimpleMarkupKind(kindStr);
						kinds.put(kindStr, kind);
					}

					try {
						super.addMarkupKind(kind);
					} catch (NullPointerException npe) {
						// ignore for plugin initialization
						//npe.printStackTrace();
					}

					// Retrieve the Offset:, e.g. 42
					offset = Integer.parseInt(retrieveKeyValue("Offset:", line), 16); //$NON-NLS-1$

					// Retrieve the Depth:, e.g. 30
					depth = Integer.parseInt(retrieveKeyValue("Depth:", line), 16); //$NON-NLS-1$
;
					// Create a new stripe and add it as a markup
					Stripe newstripe = new Stripe(kind, offset, depth);
					addMarkup(membername, newstripe);
					scount++;

					if (debugLoading)
						System.err.println("Loading new stripe: Adding " + newstripe + " for " + membername); //$NON-NLS-1$ //$NON-NLS-2$
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Problem loading markup data"); //$NON-NLS-1$
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Problem loading markup data"); //$NON-NLS-1$
			e.printStackTrace();
		}
		processMarkups();
	}

	/**
	 * Given a 'key' it looks for the key in a supplied string and returns the
	 * value after the key. For example, looking for "Fred:" in the string
	 * "Barney:40 Fred:45 Betty:40" would return "45". If values need to have
	 * spaces in then _ characters can be used, this method will translate those
	 * to spaces before it returns.
	 * 
	 * @param what
	 *            The key to look for
	 * @param where
	 *            The string to locate the key in
	 * @return the value after the key (whitespace is the value delimiter)
	 */
	private String retrieveKeyValue(String what, String where) {
		if (debugLoading)
			System.err.println("looking for '" + what + "' in '" + where + "'");
		if (where.indexOf(what) == -1)
			return null;
		String postWhat = where.substring(where.indexOf(what) + what.length());
		String result = postWhat;
		if (result.indexOf(" ") != -1)
			result = postWhat.substring(0, postWhat.indexOf(" "));
		result = result.replace('_', ' ');
		if (debugLoading)
			System.err.println("Returning '" + result + "'");
		return result;
	}
	
	public void resetVisualiser(File fileURL) {
		resetMarkupsAndKinds();

		//ConvertXMLtoMVIS.convertContentVis();
		//ConvertXMLtoMVIS.convertMarkupVis();

		try {
			InputStream in = new FileInputStream(fileURL);
			loadMarkups(in);
			in.close();
		} catch (IOException ioe) {
			VisualiserPlugin.logException(ioe);
		}
		
	}
	
	public boolean processMouseclick(IMember member, Stripe stripe, int buttonClicked) {
		String tempName = member.getFullname();
		String[] tokens = tempName.split("\\.");
		openFile(tokens[0], tokens[1], stripe.getOffset());
		return false;
	}
	
	public static void openFile(String moduleName, String fileName, int lineNumber) {
		IFile filetemp = AssemblyVisualiserPlugin.file;
		//IProject[] projs = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IPath path = new Path(filetemp.getParent().getFullPath() + File.separator + moduleName + File.separator + "asm" + File.separator + fileName);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		if(!file.exists()){
			path = new Path(filetemp.getParent().getFullPath() + File.separator + moduleName + File.separator + "asm" + File.separator + fileName + ".txt");
			file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		}
		try {
			ITextEditor editor = (ITextEditor) IDE.openEditor(AssemblyVisualiserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage(), file, true);
			gotoLine(lineNumber - 1, editor);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Jumps to the given line.
	 * 
	 * @param line
	 *            the line to jump to
	 */
	private static void gotoLine(int line, ITextEditor editor) {

		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		try {

			int start = document.getLineOffset(line);
			editor.selectAndReveal(start, 0);

			IWorkbenchPage page = editor.getSite().getPage();
			page.activate(editor);

		} catch (BadLocationException x) {
			// ignore
		}
	}
}
