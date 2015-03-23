package abyss.files.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.math.MinCutSetData;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

public class MCSoperations {
	
	public static boolean saveAllMCS(MinCutSetData data) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MCS full data file (.mcs)",  new String[] { "MCS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select MCS data file target path");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null,"Incorrect file location.","Operation failed.",JOptionPane.ERROR_MESSAGE);
		} else {
			String extension = ".mcs";
			if(selectedFile.contains(extension) == false)
				selectedFile += extension;
			
			try {
				PrintWriter pw = new PrintWriter(selectedFile);
				int dataSize = data.accessMCStransitions().size();
				pw.write("MCS list size:"+dataSize+"\n");
				
				for(int i=0; i<dataSize; i++) {
					String buffer = "";
					pw.write("[["+data.accessMCStransitions().get(i)+"]]\n");
					ArrayList<Set<Integer>> dataMatrix = data.accessMCSdata().get(i);
					
					for(Set<Integer> set : dataMatrix) {
						buffer = "--[";
						for(int el : set) {
							buffer += el+",";
						}
						buffer += "]";
						buffer = buffer.replace(",]", "]\n");
						pw.write(buffer);
					}
				}
				
				pw.close();
			} catch (Exception e) {
			}
		}
		return false;
	}
}
