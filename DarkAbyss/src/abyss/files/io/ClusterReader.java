package abyss.files.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import abyss.clusters.Clustering;

public class ClusterReader {

	public ClusterReader() {
		
	}
	
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
	        		entry.clusterSize = new int[entry.clusterNumber];
	        		entry.clusterMSS = new float[entry.clusterNumber];
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
	
}
