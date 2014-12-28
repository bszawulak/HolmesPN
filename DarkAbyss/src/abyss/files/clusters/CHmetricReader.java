package abyss.files.clusters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import abyss.utilities.ByExt;

public class CHmetricReader {
	
	
	// ta funkcja byla juz w pliku RunParser.java
	double ParseDouble(String strNumber) {
		if (strNumber != null && strNumber.length() > 0) {
			try {
				return Double.parseDouble(strNumber);
			} catch(Exception e) {
				return -1;   
			}
		}
		else return 0;
	}
		
	// parsowanie informacji z pojedynczego klastrowania
	ArrayList<Double> parseSingleClusterInfo(String source) throws NumberFormatException, IOException{
		ArrayList<Double> measureValuesHC = new ArrayList<Double>();
		File file = new File(source);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		String[] lines = str.split("Output:"); // podzial na linie
		for(int i = 3; i < lines.length; i=i+3){
			lines[i] = lines[i].replace("[2,]", "").trim();	
			String[] hcValues = lines[i].split("[\\s\\t\\n]+");
			for (int j = 0; j < hcValues.length; ++j) measureValuesHC.add(ParseDouble(hcValues[j])); 
		}
		return measureValuesHC;
	}
		
	public void executeReader(String CHmetricsPath) {
		try{
			String pathInput = new String("tmp\\CH");
			
			//pathInput = CHmetricsPath;
			
			File fp1 = new File(pathInput);
			FilenameFilter only = new ByExt("_clusters.txt");
			String[] dirList = fp1.list(only); // metoda list z filtrem
			for (int i = 0; i < dirList.length; ++i) { // dla wszystkich plikow z katalogu
				//ClusterReaderHS hc = new ClusterReaderHS();
				System.out.println(pathInput+"//"+dirList[i]);
				ArrayList<Double> measureValuesHS = parseSingleClusterInfo(pathInput+"//"+dirList[i]);
				for (int j = 0; j < measureValuesHS.size(); ++j) {
					System.out.print(measureValuesHS.get(j) + " ");
				}
				System.out.println("");
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
