package assemblyvisualiser.convertxml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import assemblyvisualiser.AssemblyVisualiserPlugin;

public class ConvertXMLtoMVIS {

	private static String WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
	private static final String CONTENT_XSL = "convertXMLtoContent.xsl";
	private static final String MARKUP_XSL = "convertXMLtoMarkup.xsl";

	public static void convertContentVis(File contentFile, File xmlFile) {

		try {
			//File contentFile = new File(WORKSPACE_ROOT + File.separator + "JHotDraw" + File.separator + "Content.vis");
			contentFile.delete();
			contentFile.createNewFile();
			String contents = xslConvert(CONTENT_XSL, xmlFile);
			//need to remove top xml generated lines from contents
			int index = contents.indexOf("\n");
			contents = contents.substring(index + 1);
			
            FileWriter out = new FileWriter(contentFile);
            out.write(contents);
            out.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void convertMarkupVis(File markupFile, File xmlFile) {

		try {
			//File markupFile = new File(WORKSPACE_ROOT + File.separator + "JHotDraw" + File.separator + "Markup.mvis");
			markupFile.delete();
			markupFile.createNewFile();
			String contents = xslConvert(MARKUP_XSL, xmlFile);
			
			//need to remove top xml generated lines from contents
			int index = contents.indexOf("\n");
			contents = contents.substring(index + 1);
			
            FileWriter out = new FileWriter(markupFile);
            out.write(contents);
            out.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String xslConvert(String conversionFile, File xmlFile) {

		try {
			Bundle bund = AssemblyVisualiserPlugin.getDefault().getBundle();
			//Bundle bund = Platform.getBundle("AssemblyVisualiser");
			String path = bund.getLocation() + "src" + Path.SEPARATOR + "assemblyvisualiser" + Path.SEPARATOR + "convertxml" + Path.SEPARATOR + conversionFile;
			path = path.substring(path.indexOf("file:") + 5);
		    File xslFile = new File(path);

			TransformerFactory transFact = TransformerFactory.newInstance();

			try {
				Transformer trans = transFact.newTransformer(new StreamSource(xslFile));
				StringWriter stringWriter = new StringWriter();
				StreamResult streamResult = new StreamResult(stringWriter);
				trans.transform(new StreamSource(xmlFile), streamResult);
				String output = stringWriter.toString();
				stringWriter.close();			
				return output;
			} catch (TransformerException e) {
				e.printStackTrace();
			} 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
}
