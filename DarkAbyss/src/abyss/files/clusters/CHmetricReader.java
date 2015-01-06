package abyss.files.clusters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.utilities.ByExt;

/**
 * Klasa odpowiedzialna za odczytanie wyników miar Celińskiego-Harabasza i zwrócenie w formie
 * macierzy wartości.
 * @author AR - główne metody
 * @author MR - drobne poprawki - dostowanie do programu
 *
 */
public class CHmetricReader {
	/**
	 * Dość niezwykły sposób przeprowadzania elementarnej operacji konwersji :)
	 * @param strNumber String - łańcuch znaków, który z pietyzmem zmieniamy w Double
	 * @return double - święta liczba rzeczywista
	 */
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
		
	/**
	 * Metoda czyta odpowiedni plik z miarą Celińskiego-Harabasza i zmienia wartości
	 * w nim zawarte na obiekt ArrayList[Double]
	 * @param source String - ścieżka do pliku
	 * @return ArrayList[Double] - wartości miary
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private ArrayList<Double> parseSingleClusterInfo(String source) throws NumberFormatException, IOException{
		ArrayList<Double> measureValuesHC = new ArrayList<Double>();
		File file = new File(source);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		String[] lines = str.split("Output:"); // podzial na linie
		for(int i = 3; i < lines.length; i=i+3) {
			lines[i] = lines[i].replace("[2,]", "").trim();	
			String[] hcValues = lines[i].split("[\\s\\t\\n]+");
			for (int j = 0; j < hcValues.length; ++j)
				measureValuesHC.add(ParseDouble(hcValues[j])); 
		}
		return measureValuesHC;
	}
		
	/**
	 * Metoda odpowiedzialna za wczytanie plików miar i zwrócenie macierzy wyników.
	 * @param CHmetricsPath String - ścieżka do katalogu z miarami
	 * @return ArrayList[ArrayList[Double]] - wyniki dla 56 klastrowań
	 */
	public ArrayList<ArrayList<Double>> readCHmetricsDirectory(String CHmetricsPath) {
		ArrayList<ArrayList<Double>> chData = new ArrayList<ArrayList<Double>>();
		String currentFile = "";
		try{
			//test:
			File fp1 = new File(CHmetricsPath);
			FilenameFilter only = new ByExt("_clusters.txt");
			String[] tempList = fp1.list(only); // metoda list z filtrem
			if(tempList.length != 56) {
				String msg = "Warning. Directory "+CHmetricsPath+" may not contain all necessary files. Operation "
						+ "will continue, but may fail.";
				GUIManager.getDefaultGUIManager().log(msg, "warning", true);
			}
			
			String[] dirList = ClusterReader.fillFileInfo();
			for (int i = 1; i < dirList.length; ++i) { // dla wszystkich 56 plikow
				currentFile = CHmetricsPath+"//"+dirList[i];
				ArrayList<Double> measureValuesHS = parseSingleClusterInfo(currentFile);
				chData.add(measureValuesHS);
				//for (int j = 0; j < measureValuesHS.size(); ++j) {
				//	System.out.print(measureValuesHS.get(j) + " ");
				//}
				//.out.println("");
			}
		} catch (IOException e){
			GUIManager.getDefaultGUIManager().log("Reading failed for Celiński-Harabasz metric in file "+currentFile, "error", true);
		}
		return chData;
	}
}
