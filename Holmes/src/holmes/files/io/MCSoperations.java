package holmes.files.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

public class MCSoperations {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	
	/**
	 * Metoda służąca do zapisu listy zbiorów MCS wybranej tranzycji.
	 * @param data MinCutSetData - obiekt bazy zbiorów
	 * @param pos int - numer wybranej reakcji
	 * @param name String - nazwa reakcji
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public static boolean saveSingleMCS(MCSDataMatrix data, int pos, String name) {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("ObjR single MCS data file (.objR)",  new String[] { "OBJR" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("MCS_entry001a")
				, lang.getText("MCS_entry001b"), "");
		
		if(selectedFile.isEmpty()) {
			JOptionPane.showMessageDialog(null, lang.getText("MCS_entry002"), lang.getText("MCS_entry003"), 
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
				overlord.log(lang.getText("LOGentry00205exception")
						+" "+e.getMessage(), "error", true);
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
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MCS full data file (.mcs)",  new String[] { "MCS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("MCS_entry004a"), lang.getText("MCS_entry004b"), "");
		
		if(selectedFile.isEmpty()) {
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
				overlord.log(lang.getText("LOGentry00206exception")+" "+e.getMessage(), "error", true);
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
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("ObjR single MCS data file (.objr)",  new String[] { "OBJR" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("MCS_entry005a"), lang.getText("MCS_entry005b"), "");
		
		if(selectedFile.isEmpty()) {
			JOptionPane.showMessageDialog(null, lang.getText("MCS_entry006"), lang.getText("MCS_entry006"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			MCSDataMatrix dataCore = overlord.getWorkspace().getProject().getMCSdataCore();
	
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
				
				String line = "";
				line = buffer.readLine();
				
				line = line.substring(line.indexOf(":")+1);
				overlord.accessMCSWindow().accessLogField().append("Read line:"+line+"\n");
				
				line = buffer.readLine();
				int index = line.indexOf(":");
				line = line.substring(index+1);
				int insertPos = Integer.parseInt(line);
				
				int transSize = overlord.getWorkspace().getProject().getTransitions().size();
				int dataSize = dataCore.getSize();
				
				if(dataSize == 0) {
					dataCore.initiateMCS();
					dataSize = dataCore.getSize();
				}
				
				if(insertPos >= dataSize) {
					JOptionPane.showMessageDialog(null, lang.getText("MCS_entry008"), 
							lang.getText("MCS_entry007"), JOptionPane.ERROR_MESSAGE);
					buffer.close();
					return false;
				}

				overlord.accessMCSWindow().accessLogField().append(lang.getText("MCS_entry009")+" "+
						overlord.getWorkspace().getProject().getTransitions().get(insertPos).getName()+"\n");
				
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
							JOptionPane.showMessageDialog(null, lang.getText("MCS_entry010"), 
									lang.getText("MCS_entry007"), JOptionPane.ERROR_MESSAGE);
							buffer.close();
							overlord.accessMCSWindow().accessLogField().append(lang.getText("LOGentry00207"));
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

				overlord.accessMCSWindow().accessLogField().append(lang.getText("LOGentry00208"));
				
				buffer.close();
				return true;
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00209exception")+" "+e.getMessage(), "error", true);
				overlord.accessMCSWindow().accessLogField().append(lang.getText("LOGentry00209exception")+" "+e.getMessage()+"\n");
				return false;
			}
		}
	}
	
	/**
	 * Metoda odczytująca plik wszystkich obliczonych zbiorów MCS.
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public static boolean loadAllMCS() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MCS full data file (.mcs)",  new String[] { "MCS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("MCS_entry011a"), lang.getText("MCS_entry011b"), "");
		
		if(selectedFile.isEmpty()) {
			JOptionPane.showMessageDialog(null,lang.getText("MCSC_entry012"),lang.getText("MCSC_entry007"),JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			MCSDataMatrix dataCore = overlord.getWorkspace().getProject().getMCSdataCore();
		
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(selectedFile));
				BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
				
				String line = "";
				line = buffer.readLine();
				int index = line.indexOf(":");
				line = line.substring(index+1);
				int dataSize = Integer.parseInt(line);
				
				int transSize = overlord.getWorkspace().getProject().getTransitions().size();
				if(dataSize != transSize) {
					JOptionPane.showMessageDialog(null, lang.getText("MCSC_entry013"), 
							lang.getText("MCSC_entry007"), JOptionPane.ERROR_MESSAGE);
					buffer.close();
					return false;
				}
				
				if(!dataCore.checkDataReplacing()) {
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
					JOptionPane.showMessageDialog(null, lang.getText("MCSC_entry014"), 
							lang.getText("MCSC_entry015"), JOptionPane.WARNING_MESSAGE);
				}
				
				int dataMCSsize = dataCore.getCalculatedMCSnumber();
				overlord.accessMCSWindow().accessLogField().append(lang.getText("MCSC_entry016a")+ " "+dataMCSsize+" "+lang.getText("MCSC_entry016b"));
				
				buffer.close();
				return true;
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00210exception")+" "+e.getMessage(), "error", true);
				return false;
			}
		}
	}
}
