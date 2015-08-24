package holmes.files.clusters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import holmes.clusters.Clustering;
import holmes.clusters.ClusteringExtended;
import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;

/**
 * Klasa odpowiedzialna za wczytywanie plików z klastrowaniem dla danej sieci.
 * @author MR
 *
 */
public class ClusterReader {
	private String fileInfo[];

	/**
	 * Konstruktor domyślny obiektu ClusterReader.
	 */
	public ClusterReader() {
		fileInfo = fillFileInfo();
	}
	
	/**
	 * Metoda czyta dany katalog klastrowań i zwraca pełną tablicę danych.
	 * @param path String - ścieżka do katalogu z plikami
	 * @return ArrayList[ArrayList[Clustering]] - tablica danych
	 */
	public ArrayList<ArrayList<Clustering>> readDirectory(String path) {
		if(checkFiles(path) == -2) { //no cluster files
			return null;
		}
		
		if(checkFiles(path) == -1) { //no cluster files
			GUIManager.getDefaultGUIManager().log("Some files are still missing in "+path+" Possible further errors.", "error", true);
		}
		
		int tableLocation = 0;
		ArrayList<ArrayList<Clustering>> bigTable = new ArrayList<ArrayList<Clustering>>();
		for(int metric=1; metric<=8; metric++) { //dla ośmiu miar
			for(int alg=1; alg<=7; alg++) { //dla siedmiu algorytmów
				tableLocation = (metric-1)*7+alg; //co 7 alg kolejna miara
				String fileName = fileInfo[tableLocation];
				String[] splited = fileName.split("_");
				ArrayList<Clustering> table = readClusterFile(path+"\\"+fileName, splited[0], splited[1]);
				GUIManager.getDefaultGUIManager().logNoEnter("Processing data for: ", "text", true);
				GUIManager.getDefaultGUIManager().log(splited[0] + " " + splited[1], "italic",false);
				if(table == null) {
					GUIManager.getDefaultGUIManager().log("Failure to fill the data for "+splited[0]+"/"+splited[1], "error", true);
				}
				bigTable.add(table);
			}
		}
		return bigTable;
	}
	
	/**
	 * Metoda sprawdza, czy w danym katalogu znajduje się wszystkie 56 plików klastrowań. Jeśli nie,
	 * wywołuję metodę pomocniczną odtwarzającą brakujące pliki w formie szkieletowej wypełnionej zerami.
	 * @param path String - ścieżka do katalogu
	 */
	public int checkFiles(String path) {
		int result = 0;
		boolean firstFound = false;
		String foundTemplateName = "";
		int checkList[] = new int[57];
		File check = null;
		for(int i=1; i<=56; i++) {
			check = new File(path+"\\"+fileInfo[i]);
			if(check.exists()) {
				checkList[i] = 1;
				if(!firstFound) { //pierwszy znaleziony robi za template
					foundTemplateName = fileInfo[i];
					firstFound = true;
				}
			}
		}
		
		if(!firstFound) { //ERROR: not a single file		
			result = -2;
			GUIManager.getDefaultGUIManager().log("Critical error. Directory "+path+" does not contain "
					+ "any cluster file.", "error", true);
			return result;
		}
		
		for(int i=1; i<56; i++) {
			if(checkList[i] == 0) { //dla każdego brakującego pliku
				//stwórz kopię:
				try {
					GUIManager.getDefaultGUIManager().logNoEnter("Missing file: "+fileInfo[i], "warning", true);
					Tools.copyFileByPath(path+"\\"+foundTemplateName, path+"\\"+fileInfo[i]+".tmp");
					nullFile(path+"\\"+fileInfo[i]+".tmp", path);
					GUIManager.getDefaultGUIManager().log(" - Fixed: recreated clean.", "text", false);
				} catch (IOException e) {
					GUIManager.getDefaultGUIManager().log("Restoring missing file has failed.", "error", true);
					GUIManager.getDefaultGUIManager().log("Error: "+e.getMessage(), "error", true);
					result = -1;
				}
			}
		}
		return result;
	}
	
	/**
	 * Metoda pomocnicza, czyści plik do którego ścieżka to pierwszy parametr. 
	 * @param filePath String - plik do zmiany - wartości liczbowe na zera
	 * @param path String - katalog dla pliku tymczasowego
	 */
	private void nullFile(String filePath, String path) {
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(path+"\\c_tmp.txt"));
			
			
	        while ((line = br.readLine()) != null) {
	        	if(!line.contains("Output:Silhouette of "))
	        		line = line.replaceAll("(-)?\\d+(\\.\\d*)?","0"); //wszystkie cyfry zmienione na zera
	        		//line = line.replaceAll("[0-9]", "0");
	        	bw.write(line+"\n");
	        }
	        br.close();
	        bw.close();
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Critical error. Cleaning file "+filePath+" has failed.", "error", true);
		}
		
		try {
			String newFilePath = filePath.replace(".tmp", "");
			Tools.copyFileByPath(path+"\\c_tmp.txt", newFilePath);
			File d = new File(path+"\\c_tmp.txt");
	        d.delete();
	        
	        d = new File(filePath);
	        d.delete();
		} catch (IOException e) {
			GUIManager.getDefaultGUIManager().log("Critical error. Creating of cleaning file "+filePath+" has failed.", "error", true);
		}
        
	}

	/**
	 * Metoda czytająca wskazany plik klastrów, zwracająca tabelę danych o klastrowaniach w pliku
	 * od 2 do maksymalnej liczby w pliku.
	 * @param path String - plik do odczytu
	 * @param nameAlg String - algorytm klastrowania
	 * @param nameMetr String - nazwa metryki
	 * @return ArrayList[Clustering] - tabela danych o klastrowaniach
	 */
	public ArrayList<Clustering> readClusterFile(String path, String nameAlg, String nameMetr) {
		File x = new File(path);
		if(!x.exists()) 
			return null;
		
		ArrayList<Clustering> resTable = new ArrayList<Clustering>();
		Clustering entry; // = new Clustering();
		String line = "";
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
	        while ((line = br.readLine()) != null) {
	        	if(line.contains("Output:Silhouette of")) { //blok opisu klastrowania
	        		entry = new Clustering();
	        		String[] splited = line.split("\\s+");
	        		entry.invNumber = Integer.parseInt(splited[2]);
	        		entry.clusterNumber = Integer.parseInt(splited[5]);
	        		if(entry.clusterNumber<1)
	        			entry.clusterNumber = 1;
	        		//entry.clusterSize = new int[entry.clusterNumber];
	        		//entry.clusterMSS = new float[entry.clusterNumber];
	        		if(nameAlg.equals("average"))
	        			nameAlg = "UPGMA";
	        		entry.algorithmName = nameAlg;
	        		entry.metricName = nameMetr;
	        		line = br.readLine(); // następną linię ignorujemy ("Output: Cluster sizes and...")
	        		int readValues = 0;
	        		while(!(line = br.readLine()).contains("Output:Individual")) {
	        			//teraz czytamy parami linii, informacje o klastrach
	        			line = line.replace(":", " ");
	        			String line1[] = line.split("\\s+"); //wagi klastrów
	        			line = br.readLine();
	        			line = line.replace(":", " ");
	        			String line2[] = line.split("\\s+"); //miara MSS
	        			
	        			for(int i=1; i<line1.length; i++) {
	        				entry.clusterSize.add(Integer.parseInt(line1[i]));
	        				//entry.clusterSize[readValues] = Integer.parseInt(line1[i]);
	        				entry.clusterMSS.add(Float.parseFloat(line2[i]));
	        				//entry.clusterMSS[readValues] = Float.parseFloat(line2[i]);
	        				readValues++;
	        				if(readValues-1 > entry.clusterNumber) { //przepełnienie zakresu tablicy
	        					throw new Exception();
	        				}
	        			}
	        		}
	        		line = br.readLine(); // ingore: "Output:Individual silhouette widths:"
	        		line = br.readLine(); // ignore: "Output:   Min. 1st Qu.  Median    Mean 3rd Qu.    Max. "
	        		line = line.replace(":", " ");
	        		splited = line.split("\\s+"); 
	        		if(splited.length != 7) 
	        			throw new Exception();
	        		else {
	        			for(int i=1; i<7; i++) {
	        				entry.vectorMSS.add(Float.parseFloat(splited[i]));
	        				//entry.vectorMSS[i-1] = Float.parseFloat(splited[i]);
	        			}
	        		}
	        		entry.evalMSS = Float.parseFloat(splited[4]);
	        		
	        		for(int i=0; i<entry.clusterNumber;i++) {
	        			//if(entry.clusterSize[i] == 1)
	        			if(entry.clusterSize.get(i) == 1)
	        				entry.zeroClusters++;
	        		}
	        		
	        		resTable.add(entry);
	        	} //if(line.contains("Output:Silhouette of"))
	        	
	        } //while
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Critical error in readCluserFile while reading "+path, "error", true);
		}
		return resTable;
	}
	
	/**
	 * Metoda odpowiedzialna za utworzenie obiektu klasy kontenera ClusteringExtended
	 * przechowującego *naprawdę* wszystkie potrzebne i niepotrzebne dane o klastrowaniu
	 * konkretnego przypadku miary/metryki/liczby klastrów.
	 * @param filePaths String[3] - trzy pliki danych
	 * @param dataPackage Clustering - metaDane o klastrowaniu
	 * @return ClusteringExtended - obiekt kontener danych
	 */
	public ClusteringExtended readSingleClustering(String[] filePaths, Clustering dataPackage) {
		if(filePaths.length < 3)
			return null;
		
		ClusteringExtended data = new ClusteringExtended();
		data.metaData = Clustering.clone(dataPackage);
		
		//filePaths[0] = resultFilePath_clusterCSV;
		//filePaths[1] = resultFilePath_r;
		//filePaths[2] = resultFilePath_MCT;
		int transitionNumber = 0;
		
		File csvFile = new File(filePaths[0]);
		File mctFile = new File(filePaths[2]);
		File clustersFile = new File(filePaths[1]);
		
		GUIManager.getDefaultGUIManager().log("Attempting to extract clustering data from the following files:", "text", true);
		GUIManager.getDefaultGUIManager().logNoEnter("Invariants CSV:  ", "text", false);
		GUIManager.getDefaultGUIManager().log(csvFile.getAbsolutePath(), "italic", false);
		GUIManager.getDefaultGUIManager().logNoEnter("MCT master file: ", "text", false);
		GUIManager.getDefaultGUIManager().log(mctFile.getAbsolutePath(), "italic", false);
		GUIManager.getDefaultGUIManager().logNoEnter("Clustering file: ", "text", false);
		GUIManager.getDefaultGUIManager().log(clustersFile.getAbsolutePath(), "italic", false);
		
		// SEKCJA I: CZYTANIE PLIKU CSV - tranzycje i inwarianty
		
		if(!csvFile.exists()) {
			GUIManager.getDefaultGUIManager().log("CSV file missing, wrong path: "+filePaths[0], "error", true);
			return null;
		} else {
			try {
				BufferedReader br = new BufferedReader(new FileReader(csvFile));
				String line = br.readLine(); //linia tranzycji
				//transitionNumber = line.length() - line.replace(";", "").length(); // ( ¬‿¬) he he he
				if(line.charAt(0) != ';')
					line = ";"+line;
				
				data.transNames = line.split(";");
				transitionNumber = data.transNames.length - 1;
				data.metaData.transNumber = transitionNumber;
				
				//data.csvInvariants = new int[data.metaData.invNumber][transitionNumber+1];
				data.csvInvariants = new ArrayList<ArrayList<Integer>>();
				while((line = br.readLine()) != null && line.length() > 3) { //czytanie inwariantów
					ArrayList<Integer> invRow = new ArrayList<Integer>(); // nowa linia inwariantów
					
					String[] invTmp = line.split(";");
	        		int invIndex = Integer.parseInt(invTmp[0]);
	        		invRow.add(invIndex); //nr porządkowy inwariantu
	        		//data.csvInvariants[invCounter][0] = invIndex;
	        		for(int i=1; i<invTmp.length; i++) {
	        			invRow.add(Integer.parseInt(invTmp[i]));
	        			//data.csvInvariants[invCounter][i] = Integer.parseInt(invTmp[i]);
	        		}
	        		data.csvInvariants.add(invRow);
	        		//lepiej, żeby nie było więcej inv, bo nastąpi przepełnienie tab statycznej
		        }
		        br.close();
		            
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("CSV file corrupt, unable to retrieve invariants data. "
						+ "Path: "+filePaths[0], "error", true);
				return null;
			}
		}
		GUIManager.getDefaultGUIManager().log("CSV invariants: extracted.", "text",true);
		// SEKCJA II: CZYTANIE PLIKU MCT
		
		if(!mctFile.exists()) {
			GUIManager.getDefaultGUIManager().log("MCT file missing, wrong path: "+filePaths[2], "error", true);
			return null;
		} else {
			try {
				BufferedReader br = new BufferedReader(new FileReader(mctFile));
				String line; // = br.readLine(); //linia tranzycji
				while((line = br.readLine()) != null && !line.contains("Proper MCT sets")) 
					; //przewijanie do sekcji ze zbiorami MCT
				
				ArrayList<String> tmpMCT = new ArrayList<String>();
				if(line != null && line.contains("Proper MCT sets")) {
					while((line = br.readLine()) != null && !line.contains("Single MCT sets") 
							&& !line.equals("")) {
						tmpMCT.add(line); //wczytywanie linii opisujących zbiory MCT
					}
					br.close();

					data.mctSets = new ArrayList<ArrayList<Integer>>();
					for(int i=0; i<tmpMCT.size(); i++) {
						ArrayList<Integer> newMctList = new ArrayList<Integer>();
						
						line = tmpMCT.get(i);
						line = line.substring(line.indexOf("{")+1);
						line = line.substring(0, line.indexOf("}"));
						line = line.trim().replaceAll("\\s+", "");
						String[] tmpSplit = line.split(",");
						int transIndex = -1;
						int lastFound = 0;
						for(int j=0; j<tmpSplit.length; j++) {
							transIndex = -1;
							for(int k=lastFound; k<data.transNames.length; k++) {
								if(tmpSplit[j].equals(data.transNames[k])) {
									transIndex = k;
									lastFound = k;
									break;
								}
							}
							if(transIndex == -1) {
								transIndex = -1;
								GUIManager.getDefaultGUIManager().log("Something is wrong with MCT data. File:"+filePaths[2], "error", true);
								lastFound = 0; //nie zaszkodzi, choć już raczej nie pomoże...
							}
							newMctList.add(transIndex); //dodajemy nr porządkowy tranzycji
						}
						data.mctSets.add(newMctList); //dodajemy cały wiersza tranzycji w danym MCT
					}
					data.metaData.MCTnumber = data.mctSets.size(); //ile nietrywalnych MCT

				} else {
					GUIManager.getDefaultGUIManager().log("MCT file corrupt. Path: "+filePaths[2], "error", true);
					br.close();
					return null;
				}
				
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("MCT file reading error. Path: "+filePaths[2], "error", true);
				return null;
			}
		}
		GUIManager.getDefaultGUIManager().log("MCT data: extracted.", "text",true);
		//SEKCJA III: ODCZYT PLIKU KLASTROWANIA
		
		if(!clustersFile.exists()) {
			GUIManager.getDefaultGUIManager().log("Cluster file missing. Path: "+filePaths[1], "error", true);
			return null;
		} else {
			try {
				BufferedReader br = new BufferedReader(new FileReader(clustersFile));
				String line; // = br.readLine(); //linia tranzycji
				while((line = br.readLine()) != null && !line.contains("Silhouette of ")) 
					; //przewijanie do linii z liczbą tranzycji
				
				if(!line.contains(data.metaData.invNumber+"")) {
					GUIManager.getDefaultGUIManager().log("Critical error. File: "+filePaths[1]+
							"contains invalid number of invariants previously read from file "+filePaths[1], "error", true);
					br.close();
					return null;
				}
				line = line.substring(line.indexOf(data.metaData.invNumber+"")+(data.metaData.invNumber+"").length());
				if(!line.contains(data.metaData.clusterNumber+"")) {
					GUIManager.getDefaultGUIManager().log("Critical error. File: "+filePaths[1]+
							"contains invalid number of clusters.", "error", true);
					br.close();
					return null;
				}
				
				while((line = br.readLine()) != null && !line.contains("Individual silhouette widths")) 
					; //przewijanie *prawie* do sekcji z klastrami
				
				while((line = br.readLine()) != null && !line.contains("[1]"))
					;
				
				String clusterLine = "";
				data.clustersInv = new ArrayList<ArrayList<Integer>>();
				
				while(line != null && line.contains("[1]")) {
					clusterLine = line.substring(line.indexOf("]")+1);
					while((line = br.readLine()) != null && !line.contains("[1]") &&
							line.contains("\"")) { //jeśli klaster ma wiele linii
						line = line.substring(line.indexOf("]")+1);
						clusterLine += line.substring(line.indexOf("]")+1);
						
					}
					
					clusterLine = clusterLine.replace("\"", " "); //usuwanie cudzysłowów
					clusterLine = clusterLine.trim().replaceAll("\\s+", " "); //wiele spacji na jedną
					String[] clustInv  = clusterLine.split(" ");
					ArrayList<Integer> cluster = new ArrayList<Integer>(); // wektor dla klastra
					for(int i=0; i<clustInv.length; i++) {
						int invNo = Integer.parseInt(clustInv[i]);
						cluster.add(invNo - 1); // 'invNo-1' to id inwariantu z csvInvariants[id][]
					}
					data.clustersInv.add(cluster); //dodajemy klaster (inwarianty) do macierzy
				}
				br.close();
				
				//final check:
				if(data.metaData.clusterNumber != data.clustersInv.size()) {
					GUIManager.getDefaultGUIManager().log("Error: more clusters have been read than "+
							"should be possible. Cluster file corrupt. Aborting procedure. Path: "+filePaths[1], "error", true);
					return null;
				} 
				
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Cluster file corrupt. Path: "+filePaths[1], "error", true);
				return null;
			}
		}
		GUIManager.getDefaultGUIManager().log("Clustering data: extracted.", "text",true);
		return data;
	}
	
	/**
	 * Metoda pomocnicza, buduje wewnętrzną listę plików pełnego klastrowania
	 */
	public static String[] fillFileInfo() {
		String[] fileNames = new String[57];
		
		fileNames[1] = "average_correlation_clusters.txt";
		fileNames[2] = "centroid_correlation_clusters.txt";
		fileNames[3] = "complete_correlation_clusters.txt";
		fileNames[4] = "mcquitty_correlation_clusters.txt";
		fileNames[5] = "median_correlation_clusters.txt";
		fileNames[6] = "single_correlation_clusters.txt";
		fileNames[7] = "ward_correlation_clusters.txt";
		
		fileNames[8] = "average_pearson_clusters.txt";
		fileNames[9] = "centroid_pearson_clusters.txt";
		fileNames[10] = "complete_pearson_clusters.txt";
		fileNames[11] = "mcquitty_pearson_clusters.txt";
		fileNames[12] = "median_pearson_clusters.txt";
		fileNames[13] = "single_pearson_clusters.txt";
		fileNames[14] = "ward_pearson_clusters.txt";
		
		fileNames[15] = "average_binary_clusters.txt";
		fileNames[16] = "centroid_binary_clusters.txt";
		fileNames[17] = "complete_binary_clusters.txt";
		fileNames[18] = "mcquitty_binary_clusters.txt";
		fileNames[19] = "median_binary_clusters.txt";
		fileNames[20] = "single_binary_clusters.txt";
		fileNames[21] = "ward.D_binary_clusters.txt";
		
		fileNames[22] = "average_canberra_clusters.txt";
		fileNames[23] = "centroid_canberra_clusters.txt";
		fileNames[24] = "complete_canberra_clusters.txt";
		fileNames[25] = "mcquitty_canberra_clusters.txt";
		fileNames[26] = "median_canberra_clusters.txt";
		fileNames[27] = "single_canberra_clusters.txt";
		fileNames[28] = "ward.D_canberra_clusters.txt";
		
		fileNames[29] = "average_euclidean_clusters.txt";
		fileNames[30] = "centroid_euclidean_clusters.txt";
		fileNames[31] = "complete_euclidean_clusters.txt";
		fileNames[32] = "mcquitty_euclidean_clusters.txt";
		fileNames[33] = "median_euclidean_clusters.txt";
		fileNames[34] = "single_euclidean_clusters.txt";
		fileNames[35] = "ward.D_euclidean_clusters.txt";
		
		fileNames[36] = "average_manhattan_clusters.txt";
		fileNames[37] = "centroid_manhattan_clusters.txt";
		fileNames[38] = "complete_manhattan_clusters.txt";
		fileNames[39] = "mcquitty_manhattan_clusters.txt";
		fileNames[40] = "median_manhattan_clusters.txt";
		fileNames[41] = "single_manhattan_clusters.txt";
		fileNames[42] = "ward.D_manhattan_clusters.txt";
		
		fileNames[43] = "average_maximum_clusters.txt";
		fileNames[44] = "centroid_maximum_clusters.txt";
		fileNames[45] = "complete_maximum_clusters.txt";
		fileNames[46] = "mcquitty_maximum_clusters.txt";
		fileNames[47] = "median_maximum_clusters.txt";
		fileNames[48] = "single_maximum_clusters.txt";
		fileNames[49] = "ward.D_maximum_clusters.txt";
		
		fileNames[50] = "average_minkowski_clusters.txt";
		fileNames[51] = "centroid_minkowski_clusters.txt";
		fileNames[52] = "complete_minkowski_clusters.txt";
		fileNames[53] = "mcquitty_minkowski_clusters.txt";
		fileNames[54] = "median_minkowski_clusters.txt";
		fileNames[55] = "single_minkowski_clusters.txt";
		fileNames[56] = "ward.D_minkowski_clusters.txt";
		
		return fileNames;
	}
	
	
	
}
