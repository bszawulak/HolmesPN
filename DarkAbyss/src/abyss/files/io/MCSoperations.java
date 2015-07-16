package abyss.files.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.math.pnElements.MCSDataMatrix;
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
	public static boolean saveSingleMCS(MCSDataMatrix data, int pos, String name) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("ObjR single MCS data file (.objR)",  new String[] { "OBJR" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select objR MCS file target path", "");
		
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
				ArrayList<ArrayList<Integer>> dataMatrix = data.getMCSlist(pos);
				ArrayList<ArrayList<Integer>> infoMatrix = data.getMCSlistInfo(pos);
				
				int currentProcessed = 0;
				for(ArrayList<Integer> set : dataMatrix) {
					buffer = "[";
					for(int el : set) {
						buffer += el+",";
					}
					buffer += "]";
					buffer = buffer.replace(",]", "]");
					
					ArrayList<Integer> infoSet = infoMatrix.get(currentProcessed);
					buffer += " info: [";
					for(int el : infoSet) {
						buffer += el+",";
					}
					buffer += "]";
					buffer = buffer.replace(",]", "]\n");
					
					currentProcessed++;
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
	public static boolean saveAllMCS(MCSDataMatrix data) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MCS full data file (.mcs)",  new String[] { "MCS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select MCS data file target path", "");
		
		if(selectedFile.equals("")) {
			//JOptionPane.showMessageDialog(null,"Incorrect file location.","Operation failed.",JOptionPane.ERROR_MESSAGE);
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
					ArrayList<ArrayList<Integer>> dataMatrix = data.accessMCSdata().get(i);
					ArrayList<ArrayList<Integer>> infoMatrix = data.accessMCSinfo().get(i);
					
					int currentProcessed = 0;
					for(ArrayList<Integer> set : dataMatrix) {
						buffer = "--[";
						for(int el : set) {
							buffer += el+",";
						}
						buffer += "]";
						buffer = buffer.replace(",]", "]");
						
						ArrayList<Integer> infoSet = infoMatrix.get(currentProcessed);
						buffer += " info: [";
						for(int el : infoSet) {
							buffer += el+",";
						}
						buffer += "]";
						buffer = buffer.replace(",]", "]\n");
						
						currentProcessed++;
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
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "Select objR MCS data file", "");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null, "Incorrect file location.", "Operation failed.", 
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			MCSDataMatrix dataCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
	
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
				
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
				
				ArrayList<ArrayList<Integer>> dataMatrix = new ArrayList<ArrayList<Integer>>();
				ArrayList<ArrayList<Integer>> infoMatrix = new ArrayList<ArrayList<Integer>>();
				
				line = buffer.readLine();
				
				while(line != null && line.contains("[")) {
					int separator = line.indexOf("info");
					String secondLine = line.substring(separator+5);
					line = line.substring(0, separator-1);
					
					line = line.substring(line.indexOf("[")+1);
					line = line.substring(0, line.length() - 1);
					line = line.trim();
					String[] numberOfSet = line.split(",");
					if(numberOfSet.length == 0) {
						line = buffer.readLine();
						continue; //następny
					}
					ArrayList<Integer> set = new ArrayList<Integer>();
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
					dataMatrix.add(set);
					
					//wczytywanie drugiego zbioru danych:
					secondLine = secondLine.substring(secondLine.indexOf("[")+1);
					secondLine = secondLine.substring(0, secondLine.length() - 1);
					secondLine = secondLine.trim();
					String[] secondInfoSet = secondLine.split(",");
					if(secondInfoSet.length == 0) {
						line = buffer.readLine();
						continue; //następny
					}
					ArrayList<Integer> infoSet = new ArrayList<Integer>();
					for(String element : secondInfoSet) {
						int t = Integer.parseInt(element);
						infoSet.add(t);
					}
					infoMatrix.add(infoSet);
					
					
					line = buffer.readLine();
				}
				dataCore.insertMCS(dataMatrix, infoMatrix, insertPos, true);

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
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "Select MCS data file", "");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null,"Incorrect file location.","Operation failed.",JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			MCSDataMatrix dataCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
		
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
				
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
					
					ArrayList<ArrayList<Integer>> dataVector = new ArrayList<ArrayList<Integer>>();
					ArrayList<ArrayList<Integer>> infoMatrix = new ArrayList<ArrayList<Integer>>();
					
					while(line.contains("--")) {
						int separator = line.indexOf("info");
						String secondLine = line.substring(separator+5);
						line = line.substring(0, separator-1);
						
						line = line.substring(line.indexOf("[")+1);
						line = line.substring(0, line.length() - 1);
						line = line.trim();
						String[] numberOfSet = line.split(",");
						if(numberOfSet.length == 0) {
							line = buffer.readLine();
							continue; //następny
						}
						ArrayList<Integer> set = new ArrayList<Integer>();
						for(String element : numberOfSet) {
							int t = Integer.parseInt(element);
							set.add(t);
						}
						dataVector.add(set);
						
						//wczytywanie drugiego zbioru danych:
						secondLine = secondLine.substring(secondLine.indexOf("[")+1);
						secondLine = secondLine.substring(0, secondLine.length() - 1);
						secondLine = secondLine.trim();
						String[] secondInfoSet = secondLine.split(",");
						if(secondInfoSet.length == 0) {
							line = buffer.readLine();
							continue; //następny
						}
						ArrayList<Integer> infoSet = new ArrayList<Integer>();
						for(String element : secondInfoSet) {
							int t = Integer.parseInt(element);
							infoSet.add(t);
						}
						infoMatrix.add(infoSet);
						
						
						line = buffer.readLine();
						if(line == null) {
							break;
						}
					}
					dataCore.insertMCS(dataVector, infoMatrix, readData-1, false);
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
