package abyss.files.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.math.MinCutSetData;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

public class MCSoperations {
	
	/**
	 * Metoda służąca do zapisu listy zbiorów MCS wybranej tranzycji.
	 * @param data MinCutSetData - obiekt bazy zbiorów
	 * @param pos int - numer wybranej reakcji
	 * @param name String - nazwa reakcji
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public static boolean saveSingleMCS(MinCutSetData data, int pos, String name) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("ObjR single MCS data file (.objR)",  new String[] { "OBJR" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select objR MCS file target path");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null, "Incorrect file location.", "Operation failed.", 
					JOptionPane.ERROR_MESSAGE);
		} else {
			String extension = ".objR";
			if(selectedFile.contains(extension) == false)
				selectedFile += extension;
			
			try {
				PrintWriter pw = new PrintWriter(selectedFile);
				pw.write("Objective reaction: "+name+"\n");
				pw.write("Location:"+pos+"\n");
				
				String buffer = "";
				//pw.write("[["+data.accessMCStransitions().get(pos)+"]]\n");
				ArrayList<Set<Integer>> dataMatrix = data.getMCSlist(pos);
				
				for(Set<Integer> set : dataMatrix) {
					buffer = "[";
					for(int el : set) {
						buffer += el+",";
					}
					buffer += "]";
					buffer = buffer.replace(",]", "]\n");
					pw.write(buffer);
				}
				
				
				pw.close();
				return true;
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("MCS data file writing operation failed.", "error", true);
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Metoda zapisująca wszystkie zbiory MCS sieci.
	 * @param data MinCutSetData - obiekt danych MCS
	 * @return boolean - true, jeśli operacja się powiodła
	 */
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
				return true;
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("MCS data file writing operation failed.", "error", true);
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Metoda wczytująca plik zbiorów MCS dla wybranej reakcji (w pliku).
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public static boolean loadSingleMCS() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("ObjR single MCS data file (.objr)",  new String[] { "OBJR" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "Select objR MCS data file");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null, "Incorrect file location.", "Operation failed.", 
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			MinCutSetData dataCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
	
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
				
				String line = "";
				line = buffer.readLine();
				
				line = line.substring(line.indexOf(":")+1);
				GUIManager.getDefaultGUIManager().accessMCSWindow().accessLogField().append("Read line:"+line+"\n");
				
				line = buffer.readLine();
				int index = line.indexOf(":");
				line = line.substring(index+1);
				int insertPos = Integer.parseInt(line);
				
				int transSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
				int dataSize = dataCore.getSize();
				
				if(dataSize == 0) {
					dataCore.initiateMCS();
					dataSize = dataCore.getSize();
				}
				
				if(insertPos >= dataSize) {
					JOptionPane.showMessageDialog(null, "Invalid entry location. Not enough transition in net.", 
							"Operation failed.", JOptionPane.ERROR_MESSAGE);
					buffer.close();
					return false;
				}
				
				GUIManager.getDefaultGUIManager().accessMCSWindow().accessLogField().append("Net transition: "+
						GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(insertPos).getName()+"\n");
				
				ArrayList<Set<Integer>> dataVector = new ArrayList<Set<Integer>>();
				
				line = buffer.readLine();
				
				while(line != null && line.contains("[")) {
					line = line.substring(line.indexOf("[")+1);
					line = line.substring(0, line.length() - 1);
					line = line.trim();
					String[] numberOfSet = line.split(",");
					if(numberOfSet.length == 0) {
						line = buffer.readLine();
						continue; //następny
					}
					Set<Integer> set = new LinkedHashSet<Integer>();
					for(String element : numberOfSet) {
						int t = Integer.parseInt(element);
						if(t >= transSize) {
							JOptionPane.showMessageDialog(null, "Transition index in read MCS set exceed transitions number!", 
									"Operation failed.", JOptionPane.ERROR_MESSAGE);
							buffer.close();
							GUIManager.getDefaultGUIManager().accessMCSWindow().accessLogField().append("MCS data for given transition read: failed.\n");
							return false;
						}
						set.add(t);
					}
					dataVector.add(set);
					line = buffer.readLine();
				}
				dataCore.insertMCS(dataVector, insertPos, true);

				GUIManager.getDefaultGUIManager().accessMCSWindow().accessLogField().append("MCS data for given transition read: success.\n");
				
				buffer.close();
				return true;
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Reading MCS data file failed. File corrupt.", "error", true);
				GUIManager.getDefaultGUIManager().accessMCSWindow().accessLogField().append("MCS data for given transition read: failed.\n");
				return false;
			}
		}
	}
	
	/**
	 * Metoda odczytująca plik wszystkich obliczonych zbiorów MCS.
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public static boolean loadAllMCS() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MCS full data file (.mcs)",  new String[] { "MCS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "Select MCS data file");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null,"Incorrect file location.","Operation failed.",JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			MinCutSetData dataCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
		
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
				
				String line = "";
				line = buffer.readLine();
				int index = line.indexOf(":");
				line = line.substring(index+1);
				int dataSize = Integer.parseInt(line);
				
				int transSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
				if(dataSize != transSize) {
					JOptionPane.showMessageDialog(null, "MCS data file refers to different number of transitions!", 
							"Operation failed.", JOptionPane.ERROR_MESSAGE);
					buffer.close();
					return false;
				}
				
				if(dataCore.checkDataReplacing() == false) {
					buffer.close();
					return false;
				}
				
				dataCore.initiateMCS();
				
				line = buffer.readLine();
				int readData = 0;
				while(line != null && line.contains("[[")) {
					readData++; //przeczytano dane nowej tranzycji
					line = buffer.readLine();
					if(line == null) {
						break;
					}
					if(line.contains("[[")) { //pusty zbiór
						continue;
					}
					
					ArrayList<Set<Integer>> dataVector = new ArrayList<Set<Integer>>();
					while(line.contains("--")) {
						line = line.substring(line.indexOf("[")+1);
						line = line.substring(0, line.length() - 1);
						line = line.trim();
						String[] numberOfSet = line.split(",");
						if(numberOfSet.length == 0) {
							line = buffer.readLine();
							continue; //następny
						}
						Set<Integer> set = new LinkedHashSet<Integer>();
						for(String element : numberOfSet) {
							int t = Integer.parseInt(element);
							set.add(t);
						}
						dataVector.add(set);
						line = buffer.readLine();
						if(line == null) {
							break;
						}
					}
					dataCore.insertMCS(dataVector, readData-1, false);
				}
				if(transSize != readData) {
					JOptionPane.showMessageDialog(null, "Warning! Not all MCS data have been read!", 
							"Operation malfunction.", JOptionPane.WARNING_MESSAGE);
				}
				
				int dataMCSsize = dataCore.getCalculatedMCSnumber();
				GUIManager.getDefaultGUIManager().accessMCSWindow().accessLogField().append("MCS data for whole net have been read: "+dataMCSsize+" lists with sets.\n");
				
				buffer.close();
				return true;
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Reading MCS data file failed. File corrupt.", "error", true);
				return false;
			}
		}
	}
}
