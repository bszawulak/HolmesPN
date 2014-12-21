package abyss.files.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import abyss.darkgui.GUIManager;
import rcaller.RCaller;
import rcaller.RCode;

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
	int type = 0; //default : all clusters
	
	public void run() {
		try {
			if(type==0) {
				RClusteringAll2();
				scriptName = scriptNamePearson;
				commands = commandsPearson;
				RClusteringAll2();
			} else {
				RClusteringSingle2();
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
	 
	public Rprotocols() {
		
	}
	
	public void setType(int x) {
		type = x;
	}
	
	public void RClusteringSingle (String pathToR, String pathOutput, String fileNameCSV, String scriptName, String miara_odl, String algorytm_c, int nrClusters) throws IOException{
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
	
	public void RClusteringAll(String pathToR, String pathOutput, String fileNameCSV, String scriptName, String commands, int nrClusters) throws IOException{		
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
	
	public void RClusteringSingle2 () throws IOException{
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
	
	public void RClusteringAll2() throws IOException{		
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
