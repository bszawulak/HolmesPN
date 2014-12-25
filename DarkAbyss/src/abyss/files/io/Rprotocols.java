package abyss.files.io;

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
			if(processingMode==0) {
				rClusteringAll2();
				scriptName = scriptNamePearson;
				commands = commandsPearson;
				rClusteringAll2();
			} else {
				rClusteringSingle2();
			}
		} catch (Exception e) {
			
		}
	}
	
	public void setR2(String pathToR, String pathOutput, String fileNameCSV, String scriptName, String miara_odl, String algorytm_c, int nrClusters) {
		this.pathToR = pathToR;
		this.pathOutput = pathOutput;
		this.fileNameCSV = fileNameCSV;
		this.scriptName = scriptName;
		this.miara_odl = miara_odl;
		this.algorytm_c = algorytm_c;
		this.nrClusters = nrClusters;
	}
	
	public void setForAllClusters(String pathToR, String pathOutput, String fileNameCSV, String scriptName, String commands, String pearsonScript, String pearsonCommand, int nrClusters) {
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
	
	public void setWorkingMode(int mode) {
		processingMode = mode;
	}
	
	public void rClusteringSingleOriginal (String pathToR, String pathOutput, String fileNameCSV, String scriptName, String miara_odl, String algorytm_c, int nrClusters) throws IOException{
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
	}
	
	public void rClusteringAllOriginal(String pathToR, String pathOutput, String fileNameCSV, String scriptName, String commands, int nrClusters) throws IOException{		
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
	}
	
	/**
	 * Metoda odpowiedzialna za klastrowanie precyzyjne dla algorytmu i metryki. Tworzy
	 * plik z numerami inwariantów dla ka¿dego klastra.
	 * @throws IOException - wyj¹tek operacji na plikach
	 */
	public void rClusteringSingle2() throws IOException{
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
	}
	
	public void rClusteringAll2() throws IOException{		
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
	}
}
