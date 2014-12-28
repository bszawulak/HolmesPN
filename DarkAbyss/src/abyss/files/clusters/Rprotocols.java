package abyss.files.clusters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.utilities.ByExt;
import rcaller.RCaller;
import rcaller.RCode;

/**
 * Klasa odpowiedzialna za uruchamianie skrypt�w �rodowiska R.
 * @author AR - g��wne metody komunikacji z R
 * @author MR - otoczka Runnable, metody pomocnicze do dzia�ania w w�tkach
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
	 * G��wna metoda wykonywalna, odpowiedzialna za uruchomienie procesu uruchamiania
	 * skrypt�w R.
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
	 * Metoda s�u��ca do ustawienia wszystkich parametr�w klastrowania 56 przypadk�w algorytm�w
	 * i miar odleg�o�ci. Musi by� wywo�ana, zanim ruszy g��wny proces klastrowania w osobnym
	 * w�tku. Metoda executeAllClustersScripts() wymaga, aby wszystkie dane poni�ej by�y
	 * prawid�owo okreslone.
	 * @param pathToR String - �cie�ka dost�pu do programu Rscript.exe
	 * @param pathOutput String - katalog in/out
	 * @param fileNameCSV String - nazwa pliku CSV
	 * @param scriptName String - �cie�ka do skryptu do wywo�ania
	 * @param commands String - �cie�ka do parametr�w wywo�ywania skryptu powy�ej
	 * @param pearsonScript String - skrypt dla metryk Personoa
	 * @param pearsonCommand - �cie�ka do parametr�w wywo�ania skrypu wy�ej
	 * @param nrClusters int - liczba klastr�w do przetworzenia w ka�dym z 56 przypadk�w
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
	 * Konstruktor domy�lny klasy potrzebny do niczego :)
	 */
	public Rprotocols() {
		
	}
	
	/**
	 * Konstruklor parametrowy klasy, okre�la tryb pracy
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
	}
	
	/**
	 * Metoda odpowiedzialna za klastrowanie precyzyjne dla algorytmu i metryki. Tworzy
	 * plik z numerami inwariant�w dla ka�dego klastra.
	 * @throws IOException - wyj�tek operacji na plikach
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
			//tu wstawi� logi w zale�no�ci od 'line'
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
