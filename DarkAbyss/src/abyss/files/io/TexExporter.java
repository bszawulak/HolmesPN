package abyss.files.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import abyss.adam.mct.Runner;
import abyss.darkgui.GUIManager;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za export danych o sieci do tabeli texa. Jej obiekt jest aktywowany
 * w GUIManager przy starcie programu.
 * @author MR
 *
 */
public class TexExporter {
	String newline = "\n";
	public TexExporter() {
		
	}
	
	/**
	 * Metoda służąca do zapisu tabel w formacie Tex do pliku tekstowego z miejscami
	 * i tranzycjami.
	 */
	public void writePlacesTransitions() {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

		if(places.size()==0 || transitions.size() == 0) {
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), "At least 1 place and transition needed.", 
					"Unable to export", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Normal Text File (.txt)", new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "");
		if(selectedFile.equals(""))
			return;
		
		if(!selectedFile.contains(".txt"))
			selectedFile += ".txt";
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
			
			ArrayList<String> places1Col = new ArrayList<String>();
			ArrayList<String> places2Col = new ArrayList<String>();
			if(places.size() == 1) {
				places1Col.add("$p_{0}$ & "+places.get(0).getName());
				places2Col.add(" &  & \\\\ \\hline");
			} else {
				for(int i=0; i<(places.size()+1)/2; i++) { //zawsze I kolumna ma tyle samo lub o 1 więcej niż II
					places1Col.add("$p_{"+i+"}$ & "+places.get(i).getName());
				}
				for(int i=(places.size()+1)/2; i<places.size(); i++) {
					places2Col.add(" & $p_{"+i+"}$ & "+places.get(i).getName()+"\\\\  \\hline");
				}
				if(places1Col.size() != places2Col.size()) {
					places2Col.add(" &  & \\\\ \\hline");
				}
			}
			
			ArrayList<String> trans1Col = new ArrayList<String>();
			ArrayList<String> trans2Col = new ArrayList<String>();
			if(transitions.size() == 1) {
				trans1Col.add("$t_{0}$ & "+transitions.get(0).getName());
				trans2Col.add(" &  & \\\\ \\hline");
			} else {
				for(int i=0; i<(transitions.size()+1)/2; i++) { //zawsze I kolumna ma tyle samo lub o 1 więcej niż II
					trans1Col.add("$t_{"+i+"}$ & "+transitions.get(i).getName());
				}
				for(int i=(transitions.size()+1)/2; i<transitions.size(); i++) {
					trans2Col.add(" & $t_{"+i+"}$ & "+transitions.get(i).getName()+"\\\\  \\hline");
				}
				if(trans1Col.size() != trans2Col.size()) {
					trans2Col.add(" &  & \\\\ \\hline");
				}
			}
			bw.write("{\\footnotesize"+newline);
			bw.write("\\begin{longtable}{| p{1.1cm} | p{6cm} | p{1.1cm} |  p{6cm} |}"+newline);
			bw.write("\\caption{List of places}\\label{tab:places}"+newline);
			bw.write("\\endfirsthead"+newline);
			bw.write("\\hline"+newline);
			bw.write("\\bf Place & \\bf Biological meaning & \\bf Place & \\bf Biological meaning \\\\ \\hline"+newline);
			bw.write("\\endhead"+newline);
			bw.write("\\hline"+newline);
			bw.write("\\bf Place & \\bf Biological meaning & \\bf Place & \\bf Biological meaning \\\\ \\hline"+newline);
			for(int i=0; i<places1Col.size(); i++) {
				bw.write(places1Col.get(i)+places2Col.get(i)+newline);
				
			}
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			bw.write(""+newline);
			bw.write(""+newline);
			
			bw.write("{\\footnotesize"+newline);
			bw.write("\\begin{longtable}{| p{1.1cm} | p{6cm} | p{1.1cm} |  p{6cm} |}"+newline);
			bw.write("\\caption{List of transitions}\\label{tab:transitions}"+newline);
			bw.write("\\endfirsthead"+newline);
			bw.write("\\hline"+newline);
			bw.write("\\bf Trans. & \\bf Biological meaning & \\bf Trans. & \\bf Biological meaning \\\\ \\hline"+newline);
			bw.write("\\endhead"+newline);
			bw.write("\\hline"+newline);
			bw.write("\\bf Trans. & \\bf Biological meaning & \\bf Trans. & \\bf Biological meaning \\\\ \\hline"+newline);
			for(int i=0; i<trans1Col.size(); i++) {
				bw.write(trans1Col.get(i)+trans2Col.get(i)+newline);
				
			}
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			
			bw.write(""+newline);
			bw.close();
		} catch (Exception e) {
			String msg = "Unable to save places and transition data to: "+selectedFile;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			msg = msg.replace(": ", ":\n");
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					"Write error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void writeInvariants() {
		String mctFile = invMCTSubroutines();
		if(mctFile == null) {
			String msg = "Unable to extract invariants data from the net.";
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					"Invariants export error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			ArrayList<ArrayList<String>> invariantsTable = new ArrayList<ArrayList<String>>();
			
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(mctFile));
			while((line = br.readLine()) != null && !line.contains("Invariants[IN MCT]")) 
				; //przewijanie do sekcji ze zbiorami MCT
			
			line = br.readLine(); //nazwy tranzycji
			while((line = br.readLine()) != null && !line.equals("")) {
				//parsowanie linii
				ArrayList<String> invRow = new ArrayList<String>();
				line = line.replace(" ", "");
				String[] cells = line.split(";");
				invRow.add("$x_{"+cells[0].replace(".", "")+"}$	");
				if(cells[1].length() > 2) { //jeśli coś więcej niż [ ]
					String[] mctSet = cells[1].replace("]", "").replace("[", "").split(",");
					for(int mct=0; mct<mctSet.length; mct++) {
						
					}
				} else {
					invRow.add("");
				}
			}
			
	        br.close();
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error. Cannot extract MCT from file "+mctFile, "error", true);
		}
		
	}
	
	/**
	 * Metoda pomocnicza dla zapisu inwariantów. Zleca utworzenie pliku generatorem MCT, w którym,
	 * tak się miło składa, 3 sekcja zawiera rozpisane inwarianty ze zbiorami MCT.
	 * @return
	 */
	private String invMCTSubroutines() {
		String filePath = GUIManager.getDefaultGUIManager().getTmpPath() + "input.csv";
		int result = GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveInvariantsToCSV(filePath, true);
		if(result == -1) {
			String msg = "Exporting net into CSV file failed.";
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			return null;
		}
		
		try {
			GUIManager.getDefaultGUIManager().log("Starting MCT generator.","text",true);
			Runner mctRunner = new Runner();
			String[] args = new String[1];
			args[0] = filePath;
			mctRunner.activate(args); //throwable

			File csvFile = new File(filePath);
			String path = Tools.getFilePath(csvFile);
			csvFile.delete();
			
			GUIManager.getDefaultGUIManager().log("MCT file saved. Path: " + path + "input.csv.analysed.txt", "text", true);
		
			return path+"input.csv.analysed.txt";
		} catch (IOException e) {
			GUIManager.getDefaultGUIManager().log("MCT generator failed: "+e.getMessage(), "error", true);
			return null;
		}
	}
	
	public void writeMCT() {
		
	}
}
