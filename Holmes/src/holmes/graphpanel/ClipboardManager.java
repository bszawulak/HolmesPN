package holmes.graphpanel;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

//import biz.source_code.base64Coder.Base64Coder;

/**
 * Storing object in clipboard as 'DarkAbyss:'+Serialized(ArrayList<ElementLocation>)+':'+Serialized(ArrayList<Arc>)
 * @author Antrov
 * 
 */
public class ClipboardManager implements ClipboardOwner {

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
	}

	/**
	 * Place a String on the clipboard, and make this class the owner of the
	 * Clipboard's contents.
	 */
	@SuppressWarnings("unused")
	private static void setClipboardContents(String aString) {
		StringSelection stringSelection = new StringSelection(aString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	/**
	 * Get the String residing on the clipboard.
	 * 
	 * @return any text found on the Clipboard; if none found, return an empty
	 *         String.
	 */
	private static String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null)
				&& contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException ex) {
				// highly unlikely since we are using a standard DataFlavor
				System.out.println(ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Serializes given ArrayList of ElementLocation and Arc and send it to clipboard
	 * @param elementLocationGroup ArrayList[ElementLocation]
	 * @param arcGroup ArrayList[Arc]
	 */
	 static void sendObjectsToClipboard(ArrayList<ElementLocation> elementLocationGroup, ArrayList<Arc> arcGroup) {
		String serializedObject = "DarkAbyss:"+elementLocationGroup.toString()+':'+arcGroup.toString();
		setClipboardContents(serializedObject);
	}
	
	public static Serializable getObjectFromClipboard()
	{
		return null;
	}

	/**
	 * Checks if content in clipboard store some data from GrpahPanel like Arc,
	 * Node or ArrayList of one of them
	 * 
	 * @return
	 */
	public static boolean checkClipboardContentCompatibility() {
		String clipbrdContent = getClipboardContents();
		if (clipbrdContent == null)
			return false;
		String[] inputData = clipbrdContent.split(":");
		return inputData.length == 3 && Objects.equals(inputData[0], "DarkAbyss");
	}
/*
	public static Object fromString(String s) {
		String[] inputData = s.split(":");
		if (inputData.length != 3 || inputData[0] != "DarkAbyss")
			return null;
		byte[] data = Base64Coder.decode(s);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/

	/** Write the object to a Base64 string. */
	/*
	private static String toString(Serializable o) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			String s = new String(Base64Coder.encode(baos.toByteArray()));
			// setClipboardContents(s);
			return s;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/

}
