package holmes.files.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import holmes.adam.mct.Runner;
import holmes.analyse.MCTCalculator;
import holmes.clusters.ClusteringExtended;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.files.io.snoopy.SnoopyWriter;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za export danych o sieci do tabeli texa. Jej obiekt jest aktywowany
 * w GUIManager przy starcie programu.
 */
public class TexExporter {
	private static LanguageManager lang = GUIManager.getLanguageManager();
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

		if(places.isEmpty() || transitions.isEmpty()) {
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), lang.getText("TEX_entry001"), 
					lang.getText("TEX_entry001t"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Normal Text File (.txt)", new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("save"), "", "");
		if(selectedFile.isEmpty())
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
					places1Col.add("$p_{"+i+"}$ & "+places.get(i).getName().replace("_", " "));
				}
				for(int i=(places.size()+1)/2; i<places.size(); i++) {
					places2Col.add(" & $p_{"+i+"}$ & "+places.get(i).getName().replace("_", " ")+"\\\\  \\hline");
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
					trans1Col.add("$t_{"+i+"}$ & "+transitions.get(i).getName().replace("_", " "));
				}
				for(int i=(transitions.size()+1)/2; i<transitions.size(); i++) {
					trans2Col.add(" & $t_{"+i+"}$ & "+transitions.get(i).getName().replace("_", " ")+"\\\\  \\hline");
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
			bw.write("\\bf Transition & \\bf Biological meaning & \\bf Transition & \\bf Biological meaning \\\\ \\hline"+newline);
			bw.write("\\endhead"+newline);
			bw.write("\\hline"+newline);
			bw.write("\\bf Transition & \\bf Biological meaning & \\bf Transition & \\bf Biological meaning \\\\ \\hline"+newline);
			for(int i=0; i<trans1Col.size(); i++) {
				bw.write(trans1Col.get(i)+trans2Col.get(i)+newline);
				
			}
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			
			bw.write(""+newline);
			bw.close();
			
			//a teraz coś z zupełnie innej beczki, zapis do SPPED w znormalizowanej formie
			ArrayList<String> placesNames = new ArrayList<String>();
			for(int i=0; i<places.size(); i++) {
				placesNames.add(places.get(i).getName());
				places.get(i).setName("p"+i);
			}
			ArrayList<String> transitionsNames = new ArrayList<String>();
			for(int i=0; i<transitions.size(); i++) {
				transitionsNames.add(transitions.get(i).getName());
				transitions.get(i).setName("t"+i);
			}
			
			SnoopyWriter sw = new SnoopyWriter();
			String path = Tools.getFilePath(new File(selectedFile));
			sw.writeSPPED(path+"net.spped");
			
			//przywróć nazwy
			for(int i=0; i<places.size(); i++) {
				places.get(i).setName(placesNames.get(i));
			}
			for(int i=0; i<transitions.size(); i++) {
				transitions.get(i).setName(transitionsNames.get(i));
			}
		} catch (Exception e) {
			String msg = lang.getText("LOGentry00211exception")+" "+selectedFile;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			msg = msg.replace(": ", ":\n");
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry002"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Metoda odpowiedzialna za zapis inwariantów do pliku.
	 */
	public void writeInvariants() {
		String mctFile = invMCTSubroutines();
		if(mctFile == null) {
			String msg = lang.getText("LOGentry00212");
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry003"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<ArrayList<String>> invariantsTable = new ArrayList<ArrayList<String>>();
		try { //ekstrakcja informacji na bazie algorytmów MCT (Adam)
			String line;
			BufferedReader br = new BufferedReader(new FileReader(mctFile));
			while((line = br.readLine()) != null && !line.contains("Invariants[IN MCT]")) 
				; //przewijanie do sekcji ze zbiorami MCT
			
			line = br.readLine(); //nazwy tranzycji
			while((line = br.readLine()) != null && !line.equals("")) {
				//parsowanie linii
				ArrayList<String> invTableRow = new ArrayList<String>();
				line = line.replace(" ", "");

				//String[] cells = line.split(";|\\t"); // tnij po średnikach i tab
				String[] cells = line.split("[;\\t]"); // tnij po średnikach i tab
				invTableRow.add("$x_{"+cells[0].replace(".", "")+"}$");
				if(cells[1].length() > 2) { //jeśli coś więcej niż [ ]
					String[] mctSet = cells[1].replace("]", "").replace("[", "").split(",");
					StringBuilder mctTableCell = new StringBuilder();
					for(int mct=0; mct<mctSet.length; mct++) {
						mctTableCell.append("$m_{");
						mctTableCell.append(mctSet[mct].replace("m", "")); //sam numer, m już dodano wyżej
						mctTableCell.append("}$");
						if(mct+1 < mctSet.length) //jeśli będą kolejne: przecinek
							mctTableCell.append(",");
					}
					invTableRow.add(mctTableCell.toString()); //dodaj zbiór MCT
				} else {
					invTableRow.add(""); //NO MCT
				}
				//teraz same tranzycje
				
				if(cells[2].length() > 2) { //jeśli coś więcej niż [ ]
					String[] transSet = cells[2].split(",");
					StringBuilder transTableCell = new StringBuilder();
					for(int tr=0; tr<transSet.length; tr++) {
						transTableCell.append("$t_{");
						transTableCell.append(transSet[tr].replace("t", "")); //sam numer, t już dodano wyżej
						transTableCell.append("}$");
						if(tr+1 < transSet.length) //jeśli będą kolejne: przecinek
							transTableCell.append(", ");
					}
					invTableRow.add(transTableCell + " \\\\ \\hline"); //dodaj zbiór MCT
				} else {
					invTableRow.add(" \\\\ \\hline"); //NO MCT
				}
				
				invariantsTable.add(invTableRow);
			}
	        br.close();
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00213exception")+" "+mctFile, "error", true);
		}
		
		//TERAZ ZAPIS DO PLIKU:
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Normal Text File (.txt)", new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("save"), "", "");
		if(selectedFile.equals(""))
			return;
		if(!selectedFile.contains(".txt"))
			selectedFile += ".txt";
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
			
			bw.write("{\\footnotesize"+newline);
			bw.write("\\begin{longtable}{| p{1.2cm} | p{4.5cm} | p{5cm}|}"+newline);
			bw.write("\\caption{List of invariants} \\label{tab:invariants} \\\\"+newline);
			bw.write("\\endfirsthead"+newline);
			bw.write("\\hline"+newline);
			bw.write("\\bf Invariant & \\bf MCT & \\bf Contained transitions \\\\  \\hline "+newline);
			bw.write("\\endhead"+newline);
			bw.write("\\hline "+newline);
			bw.write("\\bf Invariant & \\bf MCT & \\bf Contained transitions  \\\\  \\hline "+newline);

			for (ArrayList<String> row : invariantsTable) {
				bw.write(row.get(0) + " & " + row.get(1) + " & " + row.get(2) + newline);
			}
			
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			
			bw.write(""+newline);
			bw.close();
		} catch (Exception e) {
			String msg = lang.getText("LOGentry00214")+" "+selectedFile;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			msg = msg.replace(": ", ":\n");
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry002"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Metoda pomocnicza dla zapisu zbiorów MCT. Zleca utworzenie pliku generatorem MCT, w którym,
	 * tak się miło składa, 4 sekcja zawiera rozpisane MCT.
	 * @return Stri
	 */
	private String invMCTSubroutines() {
		String filePath = GUIManager.getDefaultGUIManager().getTmpPath() + "input.csv";
		int result = GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveInvariantsToCSV(filePath, true, true);
		if(result == -1) {
			String msg = lang.getText("LOGentry00215");
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			return null;
		}
		
		try {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00216"),"text",true);
			Runner mctRunner = new Runner();
			String[] args = new String[1];
			args[0] = filePath;
			mctRunner.activate(args); //throwable

			File csvFile = new File(filePath);
			String path = Tools.getFilePath(csvFile);
			csvFile.delete();
			
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00217")+ " " + path 
					+ "input.csv.analysed.txt", "text", true);
		
			return path+"input.csv.analysed.txt";
		} catch (IOException e) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00218exception")+" "+e.getMessage(), "error", true);
			return null;
		}
	}
	
	/**
	 * Metoda zapisująca zbiory MCT do pliku w formie tabeli Texa. Używa wewnętrznego generatora
	 * MCT programu, po zweryfikowaniu - zgodnego z algorytmem mct.jar do tej pory stosowanym 
	 * (i zawartym w pakiecie holmes.adam.mct)
	 */
	public void writeMCT() {
		MCTCalculator analyzer = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCTanalyzer();
		ArrayList<ArrayList<Transition>> mctSet = analyzer.generateMCT();
		if(mctSet == null) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00219"), "error", true);
			return;
		}
		
		if(mctSet.isEmpty()) {
			String msg = lang.getText("LOGentry00220");
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry004"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		for(int i=0; i<mctSet.size(); i++) { //wyzeruj MCT 1-elementowe
			ArrayList<Transition> mctRow = mctSet.get(i);
			if(mctRow.size()==1) {
				mctSet.set(i, null);
			}
		}
		for(int i=0; i<mctSet.size(); i++) { //skasuj MCT 1-elementowe
			ArrayList<Transition> mctRow = mctSet.get(i);
			if(mctRow == null) {
				mctSet.remove(i);
				i--;
			}
		}
		Object [] temp = mctSet.toArray(); 
		Arrays.sort(temp, new Comparator<Object>() { //sortuj MCT od najliczniejszego
			public int compare(Object o1, Object o2) {
		        ArrayList<Transition> temp1 = (ArrayList<Transition>)o1;
		        ArrayList<Transition> temp2 = (ArrayList<Transition>)o2;
		        if(temp1.size() > temp2.size())
		        	return -1;
		        else if(temp1.size() == temp2.size()) {
		        	return 0;
		        } else
		        	return 1;
		    }
		});
		
		mctSet.clear();
		for(Object o: temp) {
			mctSet.add((ArrayList<Transition>)o);
		}
		
		//TERAZ ZAPIS DO PLIKU:
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Normal Text File (.txt)", new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("save"), "", "");
		if(selectedFile.isEmpty())
			return;
		if(!selectedFile.contains(".txt"))
			selectedFile += ".txt";
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
			
			bw.write("{\\footnotesize"+newline);
			bw.write("\\begin{longtable}{| p{1.2cm} | p{5.2cm} | p{7.5cm} |}" + newline);
			bw.write("\\caption{List of non-trivial MCT sets} \\label{tab:mct} \\\\" + newline);
			bw.write("\\endfirsthead" + newline);
			bw.write("\\hline" + newline);
			bw.write("\\bf MCT-set & \\bf Contained transitions & \\bf Biological interpretation  \\\\  \\hline " + newline);
			bw.write("\\endhead" + newline);
			bw.write("\\hline " + newline);
			bw.write("\\bf MCT-set & \\bf Contained transitions & \\bf Biological meaning  \\\\  \\hline " + newline);
			
			for(int i=0; i<mctSet.size(); i++) {
				String mctNo = "$m_{"+(i+1)+"}$";
				StringBuilder transLine = new StringBuilder();
				for(int t=0; t<mctSet.get(i).size(); t++) {
					transLine.append(" $t_{");
					Transition trNumber = mctSet.get(i).get(t);
					int trID = transitions.lastIndexOf(trNumber);
					transLine.append(trID);
					transLine.append("}$");
					if(t+1 < mctSet.get(i).size())
						transLine.append(",");
				}
				bw.write(mctNo+" & "+transLine+" & \\\\ \\hline " + newline);
			}
			
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			
			bw.write(""+newline);
			bw.close();
		} catch (Exception e) {
			String msg = lang.getText("LOGentry00221")+" "+selectedFile;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			msg = msg.replace(": ", ":\n");
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry002"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void writeCluster(ClusteringExtended data) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Normal Text File (.txt)", new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("save"), "", "");
		if(selectedFile.isEmpty())
			return;
		
		if(!selectedFile.contains(".txt"))
			selectedFile += ".txt";
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
			//Tabelka główna:
			
			bw.write("{\\footnotesize"+newline);
			bw.write("\\begin{longtable}{| p{0.8cm} | p{1.2cm} |  p{4.8cm} | p{8.0cm} |}" + newline);
			bw.write("\\caption{Clusters composition} \\label{tab:clusterExt} \\\\" + newline);
			bw.write("\\endfirsthead" + newline);
			bw.write("\\hline" + newline);
			bw.write("\\bf Clust. & \\bf Invariant & \\bf MCTs: & \\bf No-MCT Transitions:  \\\\  \\hline " + newline);
			bw.write("\\endhead" + newline);
			bw.write("\\hline " + newline);
			bw.write("\\bf Clust. & \\bf Invariant & \\bf MCTs: & \\bf No-MCT Transitions:  \\\\  \\hline " + newline);
		
			for(int cl=0; cl<data.metaData.clusterNumber; cl++) {	
				String clCell = "$c_{"+(cl+1)+"}$ & ";  //nr klastra I komorka
				StringBuilder line = new StringBuilder();
				for(int inv=0; inv<data.clustersInv.get(cl).size(); inv++) { //tabelka inwariantów
					line = new StringBuilder();
					int invNumber = data.clustersInv.get(cl).get(inv);		
					ArrayList<String> invArray = data.getNormalizedInvariant(invNumber, true);
					
					line.append("$x_{").append(invNumber + 1).append("}$ & "); // nr inwariantu: II komorka
					
					String mctLine = invArray.get(1); //MCT
					mctLine = mctLine.replace("[", "");
					mctLine = mctLine.replace("]", "");
					if(!mctLine.isEmpty()) {
						String[] mctVector = mctLine.split(",");
						for (String mctTmp : mctVector) {
							line.append("$m_{").append(mctTmp).append("}$,");
						}
					}

					line.append("&");
					line = new StringBuilder(line.toString().replace(",&", " & "));
					
					for(int i=2; i<invArray.size(); i++)
					{
						String t = invArray.get(i);
						line.append("$t_{").append(t).append("}$, ");
					}
					line.append("\\\\ \\hline ");
					line = new StringBuilder(line.toString().replace(", \\\\", " \\\\"));
					
					if(inv==0) {
						bw.write(clCell+line+newline);
					} else {
						bw.write("      & "+line+newline);
					}
				}
			} 
			
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			
			bw.write(""+newline);
			bw.close();
		} catch (Exception e) {
			String msg = lang.getText("LOGentry00222")+" "+selectedFile;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			msg = msg.replace(": ", ":\n");
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry002"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void writeClusterExt(ClusteringExtended data) {
		ArrayList<ArrayList<Integer>> clustersMCT = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> clustersTransitions = new ArrayList<ArrayList<Integer>>();
		
		for(int cl=0; cl<data.metaData.clusterNumber; cl++) {	
			ArrayList<Integer> mctRow = new ArrayList<Integer>();
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			for(int tmp=0; tmp<data.mctSets.size(); tmp++) {
				mctRow.add(0);
			}
			for(int tmp=0; tmp<data.transNames.length-1; tmp++) {
				transRow.add(0);
			}
			
			for(int inv=0; inv<data.clustersInv.get(cl).size(); inv++) { //tabelka inwariantów
				int invNo = data.clustersInv.get(cl).get(inv);
				ArrayList<String> invArray = data.getNormalizedInvariant(invNo, true);
				//String nr = invArray.get(0); //
				String mctLine = invArray.get(1);
				
				mctLine = mctLine.replace("[", "");
				mctLine = mctLine.replace("]", "");
				if(!mctLine.isEmpty()) {
					String[] mctVector = mctLine.split(",");
					for (String s : mctVector) {
						try {
							int mctNumber = Integer.parseInt(s);
							int oldValue = mctRow.get(mctNumber);
							oldValue++;
							mctRow.set(mctNumber, oldValue); //występuje
						} catch (Exception ex) {
							GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00223exception")+" "
									+ex.getMessage(), "error", true);
						}
					}
				}
				
				for(int i=2; i<invArray.size(); i++)
				{
					try{
						int tranNumber = Integer.parseInt(invArray.get(i));
						int oldValue = mctRow.get(tranNumber);
						oldValue++;
						transRow.set(tranNumber, oldValue); //występuje
					} catch (Exception ex) {
						GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00224exception")+ " "
								+ex.getMessage(), "error", true);
					}
				}
			}
			clustersMCT.add(mctRow);
			clustersTransitions.add(transRow);
		}
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Normal Text File (.txt)", new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("save"), "", "");
		if(selectedFile.isEmpty())
			return;
		
		if(!selectedFile.contains(".txt"))
			selectedFile += ".txt";
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
			//Tabelka główna:
			
			bw.write("{\\footnotesize"+newline);
			bw.write("\\begin{longtable}{| p{1.2cm} | p{4.2cm} | p{7.5cm} |}" + newline);
			bw.write("\\caption{Clusters composition} \\label{tab:clusterExt} \\\\" + newline);
			bw.write("\\endfirsthead" + newline);
			bw.write("\\hline" + newline);
			bw.write("\\bf Cluster no & \\bf Contained MCT & \\bf Contained transitions  \\\\  \\hline " + newline);
			bw.write("\\endhead" + newline);
			bw.write("\\hline " + newline);
			bw.write("\\bf Cluster no & \\bf Contained MCT & \\bf Contained transitions  \\\\  \\hline " + newline);
		
			for(int cl=0; cl<data.metaData.clusterNumber; cl++) {	
				StringBuilder line = new StringBuilder("$c_{" + (cl + 1) + "$ & ");  //nr klastra
				for(int mct=0; mct<clustersMCT.get(cl).size(); mct++) {
					int number = clustersMCT.get(cl).get(mct);
					if(number>0) {
						line.append("$m_{").append(mct + 1).append("$, ");
					}
				}
				
				line.append("&");
				line = new StringBuilder(line.toString().replace(", &", " &"));
			}
			
			bw.write("\\end{longtable}"+newline);
			bw.write("}"+newline);
			
			bw.write(""+newline);
			bw.close();
		} catch (Exception e) {
			String msg = lang.getText("LOGentry00225")+" "+selectedFile;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			msg = msg.replace(": ", ":\n");
			JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), msg, 
					lang.getText("TEX_entry002"), JOptionPane.ERROR_MESSAGE);
		}
	}
}
