package holmes.files.clusters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.utilities.ByExt;

/**
 * Klasa odpowiedzialna za odczytanie wyników miar Celińskiego-Harabasza i zwrócenie w formie
 * macierzy wartości.
 * @author AR - główne metody
 * @author MR - drobne poprawki - dostowanie do programu
 *
 */
public class CHmetricReader {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	/**
	 * Dość niezwykły sposób przeprowadzania elementarnej operacji konwersji :)
	 * @param strNumber String - łańcuch znaków, który z pietyzmem zmieniamy w Double
	 * @return double - święta liczba rzeczywista
	 */
	double ParseDouble(String strNumber) {
		if (strNumber != null && !strNumber.isEmpty()) {
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
	 * @throws NumberFormatException ex1
	 * @throws IOException ex1
	 */
	private ArrayList<Double> parseSingleClusterInfo(String source) throws NumberFormatException, IOException {
		ArrayList<Double> measureValuesHC = new ArrayList<Double>();
		File file = new File(source);
		
		if(!file.exists()) { //wystarczy, bo procedura wczytywania do tabeli gdy natrafi na null wpisze 0.0
			return measureValuesHC;
		}
		
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		String[] lines = str.split("Output:"); // podzial na linie
		for(int i = 3; i < lines.length; i=i+3) {
			lines[i] = lines[i].replace("[2,]", "").trim();	
			String[] hcValues = lines[i].split("[\\s\\t\\n]+");
			for (String hcValue : hcValues)
				measureValuesHC.add(ParseDouble(hcValue));
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
				String msg = lang.getText("LOGentry00056a")+" "+CHmetricsPath+" "+lang.getText("LOGentry00056b");
				overlord.log(msg, "warning", true);
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
			overlord.log(lang.getText("LOGentry00057")+" "+currentFile, "error", true);
		}
		return chData;
	}
}
