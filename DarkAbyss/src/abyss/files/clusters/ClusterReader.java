package abyss.files.clusters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import abyss.clusters.Clustering;
import abyss.darkgui.GUIManager;
import abyss.utilities.Tools;

/**
 * Klasa odpowiedzialna za wczytywanie plików z klastrowaniem dla danej sieci.
 * @author MR
 *
 */
public class ClusterReader {
	private String fileInfo[];

	/**
	 * Konstruktor domyœlny obiektu ClusterReader.
	 */
	public ClusterReader() {
		fillFileInfo();
	}
	
	/**
	 * Metoda czyta dany katalog klastrowañ i zwraca pe³n¹ tablicê danych.
	 * @param path String - œcie¿ka do katalogu z plikami
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
		for(int metric=1; metric<=8; metric++) { //dla oœmiu miar
			for(int alg=1; alg<=7; alg++) { //dla siedmiu algorytmów
				tableLocation = (metric-1)*7+alg; //co 7 alg kolejna miara
				String fileName = fileInfo[tableLocation];
				String[] splited = fileName.split("_");
				ArrayList<Clustering> table = readClusterFile(path+"\\"+fileName, splited[0], splited[1]);
				GUIManager.getDefaultGUIManager().log("reading: "+ splited[0] + " " + splited[1], "text",true);
				if(table == null) {
					GUIManager.getDefaultGUIManager().log("Failure to fill the data for "+splited[0]+"/"+splited[1], "error", true);
				}
				bigTable.add(table);
			}
		}
		return bigTable;
	}
	
	/**
	 * Metoda sprawdza, czy w danym katalogu znajduje siê wszystkie 56 plików klastrowañ. Jeœli nie,
	 * wywo³ujê metodê pomocniczn¹ odtwarzaj¹c¹ brakuj¹ce pliki w formie szkieletowej wype³nionej zerami.
	 * @param path String - œcie¿ka do katalogu
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
			if(checkList[i] == 0) { //dla ka¿dego brakuj¹cego pliku
				//stwórz kopiê:
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
	 * Metoda pomocnicza, czyœci plik do którego œcie¿ka to pierwszy parametr. 
	 * @param filePath String - plik do zmiany - wartoœci liczbowe na zera
	 * @param path String - katalog dla pliku tymczasowego
	 */
	private void nullFile(String filePath, String path) {
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(path+"\\c_tmp.txt"));
			
			
	        while ((line = br.readLine()) != null) {
	        	if(!line.contains("Output:Silhouette of "))
	        		line = line.replaceAll("(-)?\\d+(\\.\\d*)?","0");
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
	 * Metoda czytaj¹ca wskazany plik klastrów, zwracaj¹ca tabelê danych o klastrowaniach w pliku
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
	        		entry.clusterSize = new int[entry.clusterNumber];
	        		entry.clusterMSS = new float[entry.clusterNumber];
	        		if(nameAlg.equals("average"))
	        			nameAlg = "UPGMA";
	        		entry.algorithmName = nameAlg;
	        		entry.metricName = nameMetr;
	        		line = br.readLine(); // nastêpn¹ liniê ignorujemy ("Output: Cluster sizes and...")
	        		int readValues = 0;
	        		while(!(line = br.readLine()).contains("Output:Individual")) {
	        			//teraz czytamy parami linii, informacje o klastrach
	        			line = line.replace(":", " ");
	        			String line1[] = line.split("\\s+"); //wagi klastrów
	        			line = br.readLine();
	        			line = line.replace(":", " ");
	        			String line2[] = line.split("\\s+"); //miara MSS
	        			
	        			for(int i=1; i<line1.length; i++) {
	        				entry.clusterSize[readValues] = Integer.parseInt(line1[i]);
	        				entry.clusterMSS[readValues] = Float.parseFloat(line2[i]);
	        				readValues++;
	        				if(readValues-1 > entry.clusterNumber) { //przepe³nienie zakresu tablicy
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
	        			for(int i=1; i<7; i++)
	        				entry.vectorMSS[i-1] = Float.parseFloat(splited[i]);
	        		}
	        		entry.evalMSS = Float.parseFloat(splited[4]);
	        		
	        		for(int i=0; i<entry.clusterNumber;i++) {
	        			if(entry.clusterSize[i] == 1)
	        				entry.zeroClusters++;
	        		}
	        		
	        		resTable.add(entry);
	        	} //if(line.contains("Output:Silhouette of"))
	        	
	        } //while
		} catch (IOException e) {
			// coœ nie tak z plikiem
			e.printStackTrace();
		} catch (NumberFormatException e) {
			//parsowanie zawiod³o, z³y format pliku
		} catch (Exception e) {
			//tutaj wci¹¿ wina pliku
		}
		return resTable;
	}
	
	/**
	 * Metoda pomocnicza, buduje wewnêtrzn¹ listê plików pe³nego klastrowania
	 */
	private void fillFileInfo() {
		fileInfo = new String[57];
		fileInfo[1] = "average_binary_clusters.txt";
		fileInfo[2] = "centroid_binary_clusters.txt";
		fileInfo[3] = "complete_binary_clusters.txt";
		fileInfo[4] = "mcquitty_binary_clusters.txt";
		fileInfo[5] = "median_binary_clusters.txt";
		fileInfo[6] = "single_binary_clusters.txt";
		fileInfo[7] = "ward.D_binary_clusters.txt";
		
		fileInfo[8] = "average_correlation_clusters.txt";
		fileInfo[9] = "centroid_correlation_clusters.txt";
		fileInfo[10] = "complete_correlation_clusters.txt";
		fileInfo[11] = "mcquitty_correlation_clusters.txt";
		fileInfo[12] = "median_correlation_clusters.txt";
		fileInfo[13] = "single_correlation_clusters.txt";
		fileInfo[14] = "ward_correlation_clusters.txt";
		
		fileInfo[15] = "average_pearson_clusters.txt";
		fileInfo[16] = "centroid_pearson_clusters.txt";
		fileInfo[17] = "complete_pearson_clusters.txt";
		fileInfo[18] = "mcquitty_pearson_clusters.txt";
		fileInfo[19] = "median_pearson_clusters.txt";
		fileInfo[20] = "single_pearson_clusters.txt";
		fileInfo[21] = "ward_pearson_clusters.txt";
		
		fileInfo[22] = "average_canberra_clusters.txt";
		fileInfo[23] = "centroid_canberra_clusters.txt";
		fileInfo[24] = "complete_canberra_clusters.txt";
		fileInfo[25] = "mcquitty_canberra_clusters.txt";
		fileInfo[26] = "median_canberra_clusters.txt";
		fileInfo[27] = "single_canberra_clusters.txt";
		fileInfo[28] = "ward.D_canberra_clusters.txt";
		
		fileInfo[29] = "average_euclidean_clusters.txt";
		fileInfo[30] = "centroid_euclidean_clusters.txt";
		fileInfo[31] = "complete_euclidean_clusters.txt";
		fileInfo[32] = "mcquitty_euclidean_clusters.txt";
		fileInfo[33] = "median_euclidean_clusters.txt";
		fileInfo[34] = "single_euclidean_clusters.txt";
		fileInfo[35] = "ward.D_euclidean_clusters.txt";
		
		fileInfo[36] = "average_manhattan_clusters.txt";
		fileInfo[37] = "centroid_manhattan_clusters.txt";
		fileInfo[38] = "complete_manhattan_clusters.txt";
		fileInfo[39] = "mcquitty_manhattan_clusters.txt";
		fileInfo[40] = "median_manhattan_clusters.txt";
		fileInfo[41] = "single_manhattan_clusters.txt";
		fileInfo[42] = "ward.D_manhattan_clusters.txt";
		
		fileInfo[43] = "average_maximum_clusters.txt";
		fileInfo[44] = "centroid_maximum_clusters.txt";
		fileInfo[45] = "complete_maximum_clusters.txt";
		fileInfo[46] = "mcquitty_maximum_clusters.txt";
		fileInfo[47] = "median_maximum_clusters.txt";
		fileInfo[48] = "single_maximum_clusters.txt";
		fileInfo[49] = "ward.D_maximum_clusters.txt";
		
		fileInfo[50] = "average_minkowski_clusters.txt";
		fileInfo[51] = "centroid_minkowski_clusters.txt";
		fileInfo[52] = "complete_minkowski_clusters.txt";
		fileInfo[53] = "mcquitty_minkowski_clusters.txt";
		fileInfo[54] = "median_minkowski_clusters.txt";
		fileInfo[55] = "single_minkowski_clusters.txt";
		fileInfo[56] = "ward.D_minkowski_clusters.txt";
	}
}
