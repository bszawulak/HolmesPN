package holmes.files.clusters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import rcaller.RCaller;
import rcaller.RCode;

/**
 * Klasa odpowiedzialna za uruchamianie skryptów środowiska R.
 * @author AR - główne metody komunikacji z R
 * @author MR - otoczka Runnable, metody pomocnicze do działania w wąąkach
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
	
	ArrayList<String> commandsValidate;
	
	/**
	 * Główna metoda wykonywalna, odpowiedzialna za uruchomienie procesu uruchamiania
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
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error: 579016465 (Rprotocols) | Exception "+ex, "error", false);
		}
	}
	
	/**
	 * Metoda służąca do ustawienia wszystkich parametrów klastrowania 56 przypadków algorytmów
	 * i miar odległości. Musi być wywołana, zanim ruszy główny proces klastrowania w osobnym
	 * wątku. Metoda executeAllClustersScripts() wymaga, aby wszystkie dane poniżej były
	 * prawidłowo okreslone.
	 * @param pathToR String - ścieżka dostępu do programu Rscript.exe
	 * @param pathOutput String - katalog in/out
	 * @param fileNameCSV String - nazwa pliku CSV
	 * @param scriptName String - ścieżka do skryptu do wywołania
	 * @param commands String - ścieżka do parametrów wywoływania skryptu powyżej
	 * @param pearsonScript String - skrypt dla metryk Persona
	 * @param pearsonCommand - ścieżka do parametrów wywołania skrypu wyżej
	 * @param nrClusters int - liczba klastrów do przetworzenia w każdym z 56 przypadków
	 * @param commandsValidate ArrayList[String] - lista wywołań w R do uruchomienia
	 */
	public void setForRunnableAllClusters(String pathToR, String pathOutput, String fileNameCSV, String scriptName, String commands, 
			String pearsonScript, String pearsonCommand, int nrClusters, ArrayList<String> commandsValidate) {
		this.pathToR = pathToR;
		this.pathOutput = pathOutput;
		this.fileNameCSV = fileNameCSV;
		this.scriptName = scriptName;
		this.nrClusters = nrClusters;
		this.commands = commands;
		
		this.scriptNamePearson = pearsonScript;
		this.commandsPearson = pearsonCommand;
		
		this.commandsValidate = commandsValidate;
	}
	
	/**
	 * Konstruktor domyślny klasy potrzebny do niczego :)
	 */
	public Rprotocols() {
		
	}
	
	/**
	 * Konstruktor parametrowy klasy, określa tryb pracy
	 * @param mode (<b>int</b>) tryb.
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
		while ((linia = in.readLine()) != null) sb.append(linia).append("\n");
		in.close();
		return sb.toString();
	}

	/**
	 * Obliczanie miar CH dla klastrów.
	 * @throws IOException ex1
	 */
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
			if(!commandsValidate.contains(line)) {
				continue;
			}
			
			
			RCaller rcaller = new RCaller();
			RCode code = new RCode();
			rcaller.setRscriptExecutable(pathToR);
			rcaller.cleanRCode();
			code.addRCode(str);
			
			GUIManager.getDefaultGUIManager().log("Processing CH: "+line, "text", true);
			
			//int reallyToCompute = nrClusters + 1; //don't ask (MR)
			
			String function = "veni1(" + line + ", \"" + pathOutput + "\",\"" + fileNameCSV + "\"," + nrClusters + ")";
			code.addRCode(function);
			String replaced = line.replace("\"", "");
			String[] parts = replaced.split(",");
			String filename = pathOutput + parts[1] + "_" + parts[0] + "_clusters.txt";
			rcaller.redirectROutputToFile(filename, false);
			rcaller.setRCode(code);
			rcaller.runOnly();
		}
		br.close();
		GUIManager.getDefaultGUIManager().log("All Celiński-Harabasz metrics has been computed.", "text", true);
	}
	
	/**
	 * Metoda odpowiedzialna za klastrowanie precyzyjne dla algorytmu i metryki. Tworzy
	 * plik z numerami inwariantów dla każdego klastra.
	 * @throws IOException - wyjątek operacji na plikach
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
			if(!commandsValidate.contains(line)) {
				continue;
			}
			//counter++;
			RCaller rcaller = new RCaller();
			RCode code = new RCode();
			rcaller.setRscriptExecutable(pathToR);
			rcaller.cleanRCode();
			code.addRCode(str);
			
			GUIManager.getDefaultGUIManager().log("Processing: "+line, "text", true);
			//tu wstawić logi w zależności od 'line'
			String function = "veni1(" + line + ", \"" + pathOutput + "\",\"" + fileNameCSV + "\"," + nrClusters + ")";
			code.addRCode(function);
			String replaced = line.replace("\"", "");
			String[] parts = replaced.split(",");
			String filename = pathOutput + parts[1] + "_" + parts[0] + "_clusters.txt";
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
		String function = "veni1(\"" + miara_odl + "\",\"" + algorytm_c + "\", \"" + pathOutput + "\",\"" + fileNameCSV + "\"," + nrClusters + ")";
		code.addRCode(function);
		String filename = pathOutput + algorytm_c + "_" + miara_odl + "_clusters_ext_" + nrClusters + ".txt";
		rcaller.redirectROutputToFile(filename, false);
		rcaller.setRCode(code);
		rcaller.runOnly();
		
		return filename;
	}
}
