package abyss.files.clusters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import abyss.darkgui.GUIManager;
import rcaller.RCaller;
import rcaller.RCode;

/**
 * Klasa odpowiedzialna za uruchamianie skryptów œrodowiska R.
 * @author AR - g³ówne metody komunikacji z R
 * @author MR - otoczka Runnable, metody pomocnicze do dzia³ania w w¹tkach
 *
 */
public class Rprotocols implements Runnable {
	String pathToR;
	String pathOutput;
	String fileNameCSV;
	String scriptName;
	String miara_odl;
	String algorytm_c;
	int nrClusters;
	String commands;
	
	String scriptNamePearson;
	String commandsPearson;
	int processingMode = 0; //default : all clusters
	
	/**
	 * G³ówna metoda wykonywalna, odpowiedzialna za uruchomienie procesu uruchamiania
	 * skryptów R.
	 */
	public void run() {
		try {
			if(processingMode == 0) {
				executeAllClustersScripts();
				scriptName = scriptNamePearson;
				commands = commandsPearson;
				executeAllClustersScripts();
			} else if(processingMode == 1) { //CH index
				executeCHmetricScripts();
				scriptName = scriptNamePearson;
				commands = commandsPearson;
				executeCHmetricScripts();
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Metoda s³u¿¹ca do ustawienia wszystkich parametrów klastrowania 56 przypadków algorytmów
	 * i miar odleg³oœci. Musi byæ wywo³ana, zanim ruszy g³ówny proces klastrowania w osobnym
	 * w¹tku. Metoda executeAllClustersScripts() wymaga, aby wszystkie dane poni¿ej by³y
	 * prawid³owo okreslone.
	 * @param pathToR String - œcie¿ka dostêpu do programu Rscript.exe
	 * @param pathOutput String - katalog in/out
	 * @param fileNameCSV String - nazwa pliku CSV
	 * @param scriptName String - œcie¿ka do skryptu do wywo³ania
	 * @param commands String - œcie¿ka do parametrów wywo³ywania skryptu powy¿ej
	 * @param pearsonScript String - skrypt dla metryk Personoa
	 * @param pearsonCommand - œcie¿ka do parametrów wywo³ania skrypu wy¿ej
	 * @param nrClusters int - liczba klastrów do przetworzenia w ka¿dym z 56 przypadków
	 */
	public void setForRunnableAllClusters(String pathToR, String pathOutput, String fileNameCSV, String scriptName, String commands, String pearsonScript, String pearsonCommand, int nrClusters) {
		this.pathToR = pathToR;
		this.pathOutput = pathOutput;
		this.fileNameCSV = fileNameCSV;
		this.scriptName = scriptName;
		this.nrClusters = nrClusters;
		this.commands = commands;
		
		this.scriptNamePearson = pearsonScript;
		this.commandsPearson = pearsonCommand;
	}
	
	/**
	 * Konstruktor domyœlny klasy potrzebny do niczego :)
	 */
	public Rprotocols() {
		
	}
	
	/**
	 * Konstruktor parametrowy klasy, okreœla tryb pracy
	 * @param mode
	 */
	public Rprotocols(int mode) {
		this();
		setWorkingMode(mode);
	}
	
	public void setWorkingMode(int mode) {
		processingMode = mode;
	}
	
	
	String getFileContent(String source) throws IOException{
		FileReader we = new FileReader(source);
		BufferedReader in = new BufferedReader(we);
		String linia;
		StringBuilder sb = new StringBuilder();
		while ((linia = in.readLine()) != null) sb.append(linia+"\n");
		in.close();
		return sb.toString();
	}

	public void executeCHmetricScripts() throws IOException{		
		File file = new File(scriptName);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		
		BufferedReader br = new BufferedReader(new FileReader(commands));
		String line;
		
		while ((line = br.readLine()) != null) {
			RCaller rcaller = new RCaller();
			RCode code = new RCode();
			rcaller.setRscriptExecutable(pathToR);
			rcaller.cleanRCode();
			code.addRCode(str);
			
			GUIManager.getDefaultGUIManager().log("Processing CH: "+line, "text", true);
			
			//int reallyToCompute = nrClusters + 1; //don't ask (MR)
			
			String function = new String("veni1("+line+", \""+pathOutput+"\",\""+fileNameCSV+"\","+nrClusters+")");
			code.addRCode(function);
			String replaced = line.replace("\"", "");
			String[] parts = replaced.split(",");
			String filename = new String(pathOutput+parts[1]+"_"+parts[0]+	"_clusters.txt");
			rcaller.redirectROutputToFile(filename, false);
			rcaller.setRCode(code);
			rcaller.runOnly();
		}
		br.close();
		GUIManager.getDefaultGUIManager().log("All Celinœki-Harabasz metrics has been computed.", "text", true);
	}
	
	/**
	 * Metoda odpowiedzialna za klastrowanie precyzyjne dla algorytmu i metryki. Tworzy
	 * plik z numerami inwariantów dla ka¿dego klastra.
	 * @throws IOException - wyj¹tek operacji na plikach
	 */
	public void executeAllClustersScripts() throws IOException{		
		File file = new File(scriptName);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		
		BufferedReader br = new BufferedReader(new FileReader(commands));
		String line;
		
		//int counter = 0;
		while ((line = br.readLine()) != null) {
			//counter++;
			RCaller rcaller = new RCaller();
			RCode code = new RCode();
			rcaller.setRscriptExecutable(pathToR);
			rcaller.cleanRCode();
			code.addRCode(str);
			
			GUIManager.getDefaultGUIManager().log("Processing: "+line, "text", true);
			//tu wstawiæ logi w zale¿noœci od 'line'
			String function = new String("veni1("+line+", \""+pathOutput+"\",\""+fileNameCSV+"\","+nrClusters+")");
			code.addRCode(function);
			String replaced = line.replace("\"", "");
			String[] parts = replaced.split(",");
			String filename = new String(pathOutput+parts[1]+"_"+parts[0]+	"_clusters.txt");
			rcaller.redirectROutputToFile(filename, false);
			rcaller.setRCode(code);
			rcaller.runOnly();
		}
		br.close();
		GUIManager.getDefaultGUIManager().log("All clusterings has been computed.", "text", true);
	}
	
	public String generateSingleClustering(String pathToR, String pathOutput, String fileNameCSV, 
			String scriptName, String miara_odl, String algorytm_c, int nrClusters) throws IOException{
		File file = new File(scriptName);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		
		String str = new String(data, "UTF-8");
		
		RCaller rcaller = new RCaller();
		RCode code = new RCode();
		rcaller.setRscriptExecutable(pathToR);
		rcaller.cleanRCode();
		code.addRCode(str);
		String function = new String("veni1(\""+miara_odl+"\",\""+algorytm_c+"\", \""+pathOutput+"\",\""+fileNameCSV+"\","+nrClusters+")");
		code.addRCode(function);
		String filename = new String(pathOutput+algorytm_c+"_"+miara_odl+"_clusters_ext_"+nrClusters+".txt");
		rcaller.redirectROutputToFile(filename, false);
		rcaller.setRCode(code);
		rcaller.runOnly();
		
		return filename;
	}
}
