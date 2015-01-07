package abyss.darkgui;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import abyss.adam.mct.Runner;
import abyss.files.clusters.Rprotocols;
import abyss.math.PetriNet;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za meta-obsługę wszystkich metod wejścia-wyjścia i paru innych, dla GUIManager.
 * W ogólności, inne elementy interfejsu wywołujś zawarte tutaj metody, a z nich sterowania w miarę 
 * potrzeby idzie dalej, aby zrealizować daną funkcję programu.
 * Krótko: kiedyś wszystkie metody tu zawarte były w klasie GUIManager. Ale zrobiło się tam zbyt tłoczno.
 * @author MR
 *
 */
public class GUIOperations {
	GUIManager overlord;

	/**
	 * Konstruktor domyślny obiektu klasy GUIOperations. A nóż do czegoś się przyda...
	 */
	public GUIOperations() {
		
	}
	
	/**
	 * Konstruktor główny obiektu klasy GUIOperations. Pobiera obiekt GUIManagera.
	 * @param mastah GUIManager
	 */
	public GUIOperations(GUIManager mastah) {
		this();
		overlord = mastah;
	}
	
	/**
	 * Metoda odpowiedzialna za import projektu z plików programów zewnętrznych.
	 * Obsługuje między innymi sieci zwykłe i czasowe programu Snoopy oraz sieci
	 * w formacie programu INA.
	 */
	public void importProject() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[3];
		filters[0] = new ExtensionFileFilter("Snoopy PN file (.spped)", new String[] { "SPPED" });
		filters[1] = new ExtensionFileFilter("Snoopy TPN file (.sptpt)", new String[] { "SPTPT" });
		filters[2] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, 
				"Select PN", "Select petri net file");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists())
			return;
		
		overlord.getWorkspace().getProject().loadFromFile(file.getPath());
		overlord.setLastPath(file.getParentFile().getPath());
		overlord.getSimulatorBox().createSimulatorProperties();
	}
	
	/**
	 * Metoda odpowiedzialna za otwieranie pliku z zapisaną siecią w formacie .abyss.
	 */
	public void openAbyssProject() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Abyss Petri Net file (.abyss)", new String[] { "ABYSS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select Abyss PN", 
				"Select petri net file in program native format");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists()) 
			return;
		
		overlord.getWorkspace().getProject().loadFromFile(file.getPath());
		overlord.setLastPath(file.getParentFile().getPath());
		overlord.getSimulatorBox().createSimulatorProperties();
	}
	
	/**
	 * Metoda odpowiedzialna za eksport sieci do pliku w formacie programu INA.
	 */
	public void exportAsPNT() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("INA PNT file (.pnt)", new String[] { "PNT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", 
				"Accept directory and filename");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		String fileExtension = ".pnt";
		if(selectedFile.toLowerCase().contains(".pnt"))
			fileExtension = "";
		
		overlord.getWorkspace().getProject().saveAsPNT(file.getPath() + fileExtension);
		overlord.setLastPath(file.getParentFile().getPath());
	}
	
	/**
	 * Metoda odpowiedzialna za zapis wygenerowanych inwariantów do pliku programu INA.
	 */
	public void exportGeneratedInvariants() {
		//TODO: da się: Tools.lastExtension

		String lastPath = overlord.getLastPath();
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter inaFilter = new ExtensionFileFilter("INA Invariants File (.inv)", new String[] { "INV" });
		FileFilter charlieFilter = new ExtensionFileFilter("Charlie Invariants File (.inv)", new String[] { "INV" });
		FileFilter csvFilter = new ExtensionFileFilter("Comma Separated Values (.csv)", new String[] { "CSV" });
		String fileExtension;
		fc.setFileFilter(inaFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.addChoosableFileFilter(charlieFilter);
		fc.addChoosableFileFilter(csvFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String description = fc.getFileFilter().getDescription();
			if (description.contains("INA")) {
				if(file.getPath().toLowerCase().contains(".inv"))
					fileExtension = "";
				else
					fileExtension = ".inv";
				overlord.getWorkspace().getProject().saveInvariantsToInaFormat(file.getPath() + fileExtension);
				overlord.setLastPath(file.getParentFile().getPath());
			} else if (description.contains("Comma")) {
				if(file.getPath().toLowerCase().contains(".csv"))
					fileExtension = "";
				else
					fileExtension = ".csv";
				overlord.getWorkspace().getProject().saveInvariantsToCSV(file.getPath() + fileExtension, false);
				overlord.setLastPath(file.getParentFile().getPath());
			} else if (description.contains("Charlie")) {
				if(file.getPath().toLowerCase().contains(".inv"))
					fileExtension = "";
				else
					fileExtension = ".inv";
				overlord.getWorkspace().getProject().saveInvariantsToCharlie(file.getPath() + fileExtension);
				overlord.setLastPath(file.getParentFile().getPath());
			}
		}
	}
	
	/**
	 * Metoda odpowiedzialna za eksport projektu do pliku graficznego w określonym formacie.
	 */
	public void exportProjectToImage() {
		//TODO: da się: Tools.lastExtension

		String lastPath = overlord.getLastPath();
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter pngFilter = new ExtensionFileFilter("Portable Network Graphics (.png)", new String[] { "png" });
		FileFilter bmpFilter = new ExtensionFileFilter("Bitmap Image File (.bmp)", new String[] { "bmp" });
		FileFilter jpegFilter = new ExtensionFileFilter("JPEG Image File (.jpeg)", new String[] { "jpeg" });
		FileFilter jpgFilter = new ExtensionFileFilter("JPEG Image File (jpg)", new String[] { "jpg" });
		fc.setFileFilter(pngFilter);
		fc.addChoosableFileFilter(pngFilter);
		fc.addChoosableFileFilter(bmpFilter);
		fc.addChoosableFileFilter(jpegFilter);
		fc.addChoosableFileFilter(jpgFilter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String ext = "";
			String extension = fc.getFileFilter().getDescription();
			if (extension.toLowerCase().contains(".png")) {
				ext = ".png";
			}
			if (extension.toLowerCase().contains(".bmp")) {
				ext = ".bmp";
			}
			if (extension.toLowerCase().contains(".jpeg") || extension.contains(".jpg")) {
				ext = ".jpeg";
			}
			//int index = 0;
			String fullPath = "";
			for (BufferedImage bi : overlord.getWorkspace().getProject().getImagesFromGraphPanels()) {
				try {
					String ext2 = "";
					String path = file.getPath();
					if(ext.equals(".png") && !(path.contains(".png"))) ext2 = ".png";
					if(ext.equals(".bmp") && !file.getPath().contains(".bmp")) ext2 = ".bmp";
					if(ext.equals(".jpeg") && !file.getPath().contains(".jpeg")) ext2 = ".jpeg";
					if(ext.equals(".jpeg") && !file.getPath().contains(".jpg")) ext2 = ".jpg";
					
					String fileName = file.getName();
					String pathOutput = file.getAbsolutePath().substring(0, 
							file.getAbsolutePath().lastIndexOf(File.separator)) + "//";
					fullPath = pathOutput+fileName+ext2;
					
					ImageIO.write(bi, ext.substring(1), new File(fullPath));
					//index++;
				} catch (IOException ex) {
					overlord.log("Unable to extract net as image. Cannot save to file "+fullPath, "error", true);
				}
			}
			overlord.setLastPath(file.getParentFile().getPath());
		}
	}

	/**
	 * Metoda odpowiedzialna za zapis projektu sieci do pliku natywnego aplikacji.
	 */
	public void saveAsAbyssFile() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Abyss Petri Net (.abyss)", new String[] { "ABYSS" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);

		String fileExtension = ".abyss";
		if(selectedFile.toLowerCase().contains(".abyss"))
			fileExtension = "";
		
		overlord.getWorkspace().getProject().saveAsAbyss(file.getPath() + fileExtension);
		overlord.setLastPath(file.getParentFile().getPath());
	}
	
	/**
	 * Metoda ogólnego zapisu, pozwala wybrać format wyjściowy. Domyślnie SPPED
	 */
	public void saveAsGlobal() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[3];
		filters[0] = new ExtensionFileFilter("Snoopy Petri Net (.spped)", new String[] { "SPPED" });
		filters[1] = new ExtensionFileFilter("Abyss Petri Net (.abyss)", new String[] { "ABYSS" });
		filters[2] = new ExtensionFileFilter("INA PNT format (.pnt)", new String[] { "PNT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "");
		if(selectedFile.equals("")) {
			return;
		}
		
		String extension = Tools.lastExtension;
		if(extension == null || extension.equals("")) {
			JOptionPane.showMessageDialog(null, "File choosing error. Cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
			overlord.log("File choosing error. No extension: "+extension, "error", true);
			return;
		}
		if (extension.contains(".spped")) {
			File file = new File(selectedFile);
			String fileExtension = ".spped";
			if(selectedFile.toLowerCase().contains(".spped"))
				fileExtension = "";
			
			overlord.getWorkspace().getProject().saveAsSPPED(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
		}
		if (extension.contains(".abyss")) {
			File file = new File(selectedFile);
			String fileExtension = ".abyss";
			if(selectedFile.toLowerCase().contains(".abyss"))
				fileExtension = "";
			
			overlord.getWorkspace().getProject().saveAsAbyss(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
		}
		if (extension.contains(".pnt") ) {
			File file = new File(selectedFile);
			String fileExtension = ".pnt";
			if(selectedFile.toLowerCase().contains(".pnt"))
				fileExtension = "";
			
			overlord.getWorkspace().getProject().saveAsPNT(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
		}
		
		
	}
	
	/**
	 * Metoda odpowiedzialna za wczytywanie inwariantów z pliku wygenerowanego programem INA.
	 */
	public void loadExternalAnalysis() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("INA Invariants file (.inv)", new String[] { "INV" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load invariants", "Select invariant file");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists())
			return;

		PetriNet project = overlord.getWorkspace().getProject();
		project.loadInvariantsFromFile(file.getPath());
		overlord.getInvariantsBox().showInvariants(project.getInaInvariants());
		overlord.setLastPath(file.getParentFile().getPath());
		overlord.getSimulatorBox().createSimulatorProperties();
	}
	
	/**
	 * Metoda uruchamia sekwencję zdarzeń prowadząca do wygenerowania inwariantów
	 * za pomocą programu INA działającego jako niezależna aplikacja. Zaleca się
	 * nie zaglądanie jak i co ona robi (metoda, nie INA), gdyż może to doprawadziź
	 * do słabszych duchem programistów do rozstroju nerwowego, szczególnie w kontekscie
	 * operacji na plikach.
	 */
	public void generateINAinvariants() {
		String stars = "************************************************************************************************";
		//showConsole(true);
		String toolPath = overlord.getToolPath();
		File tmpPNTfile = new File(toolPath+"siec.pnt");
		String x = tmpPNTfile.getPath();
		overlord.getWorkspace().getProject().saveAsPNT(x);
		//zakończono zapis do pliku .pnt
		long size = tmpPNTfile.length(); //124 dla nieistniejącej (pustej) sieci
		if(size <154) {
			String msg = "Net saving as .pnt file failed. There may be problems with file: "+x + 
					" or there is no network yet.";
			JOptionPane.showMessageDialog(null, msg, "Missing net or file", JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			return;
		}
		
		File inaExe = new File(toolPath+"INAwin32.exe");
		File batFile = new File(toolPath+"ina.bat");
		File commandFile = new File(toolPath+"COMMAND.ina");
		String abyssPath = overlord.getAbyssPath();
		if(inaExe.exists() && commandFile.exists()) {
			try {
				JOptionPane.showMessageDialog(null, "INAwin32.exe will now start. Click OK and please wait.", "Patience is a virtue", JOptionPane.INFORMATION_MESSAGE);
				overlord.log(stars, "text", false);
				overlord.log("Activating INAwin32.exe. Please wait, this may take a few seconds due to OS delays.", "text", true);
				//kopiowanie plików:
				Tools.copyFileByPath(inaExe.getPath(), abyssPath+"\\INAwin32.exe");
				Tools.copyFileByPath(batFile.getPath(), abyssPath+"\\ina.bat");
				Tools.copyFileByPath(commandFile.getPath(), abyssPath+"\\COMMAND.ina");
				Tools.copyFileByPath(tmpPNTfile.getPath(), abyssPath+"\\siec.pnt");
				
				String[] command = {"ina.bat"};
			    ProcessBuilder b = new ProcessBuilder(command);
			    Process proc;
			
				proc = b.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while (in.readLine() != null) ; 
				//while (in.readLine() != null) ;
				Thread.sleep(200);
				proc.destroy();
				
				new File(abyssPath+"\\INAwin32.exe").delete();
				new File(abyssPath+"\\ina.bat").delete();
				new File(abyssPath+"\\COMMAND.ina").delete();
				new File(abyssPath+"\\siec.pnt").delete();
				
				File t1 = new File(abyssPath+"\\SESSION.ina");
				File t2 = new File(abyssPath+"\\OPTIONS.ina");
				File t3 = new File(abyssPath+"\\INVARI.hlp");
				if(t1.exists())
					t1.delete();
				if(t2.exists())
					t2.delete();
				if(t3.exists())
					t3.delete();
				overlord.log("INAwin32.exe process terminated. Reading results into network now.", "text",true);
			} catch (Exception e) {
				String msg = "I/O operation: activating INA process failed.";
				JOptionPane.showMessageDialog(null, msg, "Critical error", JOptionPane.ERROR_MESSAGE);
				overlord.log(msg, "error", true);
				overlord.log(stars, "text", false);
				return;
			}
			
			// check whether the file with T-invariants has been generated
			File invariantsFile = new File("siec.inv");
			if (!invariantsFile.exists())  
			{
				String msg = "No invariants file - creating using INAwin32.exe unsuccessful.";
				JOptionPane.showMessageDialog(null,msg,	"Critical error",JOptionPane.ERROR_MESSAGE);
				overlord.log(msg, "error", true);
				return;
			}
			
			//wczytywanie inwariantów do systemu:
			PetriNet project = overlord.getWorkspace().getProject();
			project.loadInvariantsFromFile(invariantsFile.getPath());

			overlord.getInvariantsBox().showInvariants(project.getInaInvariants());
			//overlord.getInvariantsBox().showExternalInvariants(project.getInaInvariants());
			overlord.getSimulatorBox().createSimulatorProperties();
		
			//co dalej z plikiem?
			String lastPath = overlord.getLastPath();
			Object[] options = {"Save .inv file", "No, thanks",};
			int n = JOptionPane.showOptionDialog(null,
							"Do you want to save generated .inv file?",
							"Save the invariants?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) { //save the file
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter("INA Invariants file (.inv)",  new String[] { "INV" });
				String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", 
						"Select invariants target path");
				
				if(!selectedFile.equals("")) { //jeśli wskazano plik
					File file = new File(selectedFile);
					String ext = "";
					if(!file.getPath().contains(".inv"))
						ext = ".inv";
					File properName = new File(file.getPath() + ext);
					Tools.copyFileDirectly(invariantsFile, properName);
					overlord.setLastPath(file.getParentFile().getPath());
				}
			}
			overlord.log("Invariants generation successful.", "text", true);
			overlord.log(stars, "text", false);
			invariantsFile.delete();
			//showConsole(false);
		} else { //brak plikow
			String msg = "Missing executables in the tools directory. Required: INAwin32.exe, ina.bat and COMMAND.ina";
			JOptionPane.showMessageDialog(null,msg,	"Missing files",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
		}
	}

	/**
	 * Metoda generująca najbardziej postawową wersję pliku zbiorów MCT.
	 */
	public void generateSimpleMCTFile() {
		String filePath = overlord.getTmpPath() + "input.csv";
		int result = overlord.getWorkspace().getProject().saveInvariantsToCSV(filePath, true);
		if(result == -1) {
			String msg = "Exporting net into CSV file failed.";
			JOptionPane.showMessageDialog(null,msg,	"Write error",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			return;
		}
		
		try {
			overlord.log("Starting MCT generator.","text",true);
			Runner mctRunner = new Runner();
			String[] args = new String[1];
			args[0] = filePath;
			mctRunner.activate(args); //throwable
			String lastPath = overlord.getLastPath();
			
			FileFilter[] filters = new FileFilter[1];
			filters[0] = new ExtensionFileFilter("MCT sets file (.mct)",  new String[] { "MCT" });
			String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select MCT target path");
			
			if(selectedFile.equals("")) { //jeśli nie wybrano lokalizacji, zostaje w tmp
				File csvFile = new File(filePath);
				csvFile.delete();
				
				JOptionPane.showMessageDialog(null,"MCT file created","Operation successful.",JOptionPane.INFORMATION_MESSAGE);
				overlord.log("MCT file saved. Path: " + overlord.getTmpPath() + "input.csv.analysed.txt", "text", true);
			} else {
				File file = new File(selectedFile);
				
				String ext = "";
				if(!file.getPath().contains(".mct"))
					ext = ".mct";
				File properName = new File(file.getPath() + ext);
				File generatedMCT = new File(overlord.getTmpPath() + "input.csv.analysed.txt");
				Tools.copyFileDirectly(generatedMCT, properName);
				
				generatedMCT.delete();
				File csvFile = new File(filePath);
				csvFile.delete();
				overlord.setLastPath(file.getParentFile().getPath());
				
				JOptionPane.showMessageDialog(null,"MCT file created","Operation successful.",JOptionPane.INFORMATION_MESSAGE);
				overlord.log("MCT file saved. Path: "+filePath, "text", true);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File operation failed when creating MCT sets.", 
					"MCT generator error",JOptionPane.ERROR_MESSAGE);
			overlord.log("MCT generator failed: "+e.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda odpowiedzialna za obliczanie metryk Calinskiego-Harabasza dla klastrów
	 * sieci Petriego.
	 * @param howMany int - maksymalna liczba klastrów
	 * @return String - katalog z plikami miar
	 */
	public String generateAllCHindexes(int howMany) {
		overlord.showConsole(true);
		if(!overlord.getRStatus()) { //sprawda, czy Rscript.exe jest na miejscu
			overlord.r_env_missing(); // zapytanie gdzie się podziewa Rscript.exe
			if(!overlord.getRStatus()) { //jeśli wciąż...
				return null;
			}
		}
		
		String CSVfilePath= "";
		CSVfilePath = selectionOfSource();

		/*
		String filePath = tmpPath + "cluster.csv";
		
		//generowanie CSV, uda się, jeśli inwarianty istnieją
		int result = workspace.getProject().saveInvariantsToCSV(filePath, true);
		if(result == -1) {
			String msg = "Exporting net into CSV file failed. \nCluster procedure cannot begin without invariants.";
			JOptionPane.showMessageDialog(null,msg,	"CSV export error",JOptionPane.ERROR_MESSAGE);
			log(msg, "error", true);
			return null;
		}
		*/
		
		String dir_path = "";
		int c_number = howMany;
		try{
			int invNumber = 0;
			if(overlord.getWorkspace().getProject().getInvariantsMatrix() == null) {
				overlord.log("Warning: unable to check if given clusters number ("+howMany+") exceeds invariants "
						+ "number. If so, the procedure may fail.", "warning", true);
			} else {
				invNumber = overlord.getWorkspace().getProject().getInvariantsMatrix().size();
				if(invNumber < howMany)
					howMany = invNumber;
			}
			
			File test = new File(CSVfilePath);
			String CSVDirectoryPath = test.getParent();

			Object[] options = {"Select CH metric directory", "Use temporary directory",};
			int n = JOptionPane.showOptionDialog(null,
					"Multiple CH metric files can we written into default temporary directory (inadvised) or into\n"
					+ "the selected one. What to do?",
					"Directory selection", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				String choosenDir = Tools.selectDirectoryDialog(CSVDirectoryPath, "Select CH metric dir",
						"Target directory for CH metric results");
				if(choosenDir.equals("")) {
					dir_path = overlord.getTmpPath();
					overlord.log("CH metric files will be put into the "+dir_path, "text", true);
				} else {
					File dir = new File(choosenDir);
					dir_path = dir.getPath() + "//";
					
					Tools.copyFileByPath(CSVfilePath, dir_path+"cluster.csv");
					overlord.log("Cluster files will be put into the "+dir_path, "text", true);
				}
			} else { //default one
				dir_path = overlord.getTmpPath();
				overlord.log("Cluster files will be put into the "+dir_path, "text", true);
			}
			dir_path = dir_path.replace("\\", "/");
			
			File test64 = new File(overlord.getSettingsManager().getValue("r_path64"));
			String r_path = "";
			if(test64.exists())
				r_path = overlord.getSettingsManager().getValue("r_path64");
			else {
				r_path = overlord.getSettingsManager().getValue("r_path");
				overlord.log("Warning: Celinski-Harabasz metric computation in 32bit mode for large number of invariants can cause R/system crash","warning",true);
			}
			
			Runnable runnable = new Rprotocols(1);
			((Rprotocols)runnable).setForRunnableAllClusters(r_path, dir_path, "cluster.csv", 
					"scripts\\f_CHindex.r", "scripts\\f_clusters_run.r", 
					"scripts\\f_CHindex_Pearson.r", "scripts\\f_clusters_Pearson_run.r", howMany);
	        Thread thread = new Thread(runnable);
	        thread.start();
	        return dir_path;
		} catch (IOException e){
			String msg = "CH metric computation failed for " + c_number + " clusters.\nPath: "+dir_path;
			JOptionPane.showMessageDialog(null, msg, "Critical error",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			overlord.log(e.getMessage(), "error", false);
			return null;
		}
	}
	
	/**
	 * Metoda odpowiedzialna za generowanie klastrowań na podstawie sieci.
	 * @return String - ścieżka do pliku cluster.csv na bazie którego powstały inne pliki
	 */
	public String generateClustersCase56(int howMany) {
		overlord.showConsole(true);
		if(!overlord.getRStatus()) { //sprawdź, czy Rscript.exe jest na miejscu
			overlord.r_env_missing(); // zapytanie gdzie się podziewa Rscript.exe
			if(!overlord.getRStatus()) { //jeśli wciąż...
				return null;
			}
		}
		String CSVfilePath = "";
		CSVfilePath = selectionOfSource();
		
		String dir_path = "";
		int c_number = howMany;
		try{
			int invNumber = 0;
			if(overlord.getWorkspace().getProject().getInvariantsMatrix() == null) {
				overlord.log("Warning: unable to check if given clusters number ("+howMany+") exceeds invariants "
						+ "number. If so, the procedure may fail.", "warning", true);
			} else {
				invNumber = overlord.getWorkspace().getProject().getInvariantsMatrix().size();
				if(invNumber < howMany)
					howMany = invNumber;
			}
			
			File test = new File(CSVfilePath);
			String CSVDirectoryPath = test.getParent();

			Object[] options = {"Select cluster directory", "Use temporary directory",};
			int n = JOptionPane.showOptionDialog(null,
					"Multiple cluster files can we written into default temporary directory (not advised) or into\n"
					+ "the selected one. What to do?",
					"Directory selection", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				String choosenDir = Tools.selectDirectoryDialog(CSVDirectoryPath, "Select cluster dir",
						"Target directory for cluster results");
				if(choosenDir.equals("")) {
					dir_path = overlord.getTmpPath();
					overlord.log("Cluster files will be put into the "+dir_path, "text", true);
				} else {
					File dir = new File(choosenDir);
					dir_path = dir.getPath() + "//";
					
					Tools.copyFileByPath(CSVfilePath, dir_path+"cluster.csv");
					overlord.log("Cluster files will be put into the "+dir_path, "text", true);
				}
			} else { //default one
				dir_path = overlord.getTmpPath();
				overlord.log("Cluster files will be put into the "+dir_path, "text", true);
			}
			
			dir_path = dir_path.replace("\\", "/");
			
			Runnable runnable = new Rprotocols();
			((Rprotocols)runnable).setForRunnableAllClusters(overlord.getSettingsManager().getValue("r_path"), dir_path, "cluster.csv", 
					"scripts\\f_clusters.r", "scripts\\f_clusters_run.r", 
					"scripts\\f_clusters_Pearson.r", "scripts\\f_clusters_Pearson_run.r", c_number);
			((Rprotocols)runnable).setWorkingMode(0);
            Thread thread = new Thread(runnable);
            thread.start();
            
            return dir_path+"/"+"cluster.csv";
		}catch (IOException e){
			String msg = "Clustering generation failed for "+c_number+" clusters.\nPath: "+dir_path;
			JOptionPane.showMessageDialog(null, msg, "Critical error",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			overlord.log(e.getMessage(), "error", false);
			return null;
		}
	}
	
	/**
	 * Metoda zwraca ścieżkę do pliku CSV, najpierw jednak molestuje użytkownika celem
	 * określenia skąd ma ten plik właściwa sama wytrzasnąć.
	 * @return String - ścieżka do pliku CSV
	 */
	private String selectionOfSource() {
		String lastPath = overlord.getLastPath();
		if(overlord.getWorkspace().getProject().getInvariantsMatrix() == null) { //brak inwariantów
			FileFilter[] filters = new FileFilter[1];
			filters[0] = new ExtensionFileFilter("CSV invariants file (.csv)",  new String[] { "CSV" });
			String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select CSV", "Select CSV file");
			
			if(selectedFile.equals(""))
				return null;
			else
				return selectedFile;
		} else {
			//wybór: z sieci, czy wskazanie CSV
			Object[] options = {"Select CSV file manually", "Create CSV from net invariants",};
			int n = JOptionPane.showOptionDialog(null,
					"Select CSV file for clustering computation manually or extract CSV from the\n"
					+ "current network invariants (but they must be computed already)?",
					"Source CSV decision", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter("CSV invariants file (.csv)",  new String[] { "CSV" });
				String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select CSV", "Select CSV file");
				
				if(selectedFile.equals(""))
					return null;
				else
					return selectedFile;
			} else {
				//generowanie CSV, uda się, jeśli inwarianty istnieją
				String CSVfilePath = overlord.getTmpPath() + "cluster.csv";
				int result = overlord.getWorkspace().getProject().saveInvariantsToCSV(CSVfilePath, true);
				if(result == -1) {
					String msg = "Exporting net into CSV file failed. \nCluster procedure cannot begin without invariants.";
					JOptionPane.showMessageDialog(null,msg,	"CSV export error",JOptionPane.ERROR_MESSAGE);
					overlord.log(msg, "error", true);
					return null;
				}
				
				return CSVfilePath;
			}
		}
	}
	
	/**
	 * Metoda odpowiedzialna za wygenerowanie jednego klastrowania z inwariantami.
	 * @param clustersPath String - domyślna lokalizacja pliku CSV
	 * @param algorithm String - nazwa algorytmu klastrowania
	 * @param metric String - nazwa metryki dla powyższego
	 * @param howMany int - ile klastrów ma mieć klastrowanie
	 * @return String[5] - ścieżki do plików:
	 * 	resultFilePath_r; resultFilePath_MCT; resultFilePath_clusterCSV; cluster.pdf; dendrogram.pdf
	 */
	public String[] generateSingleClustering(String clustersPath, String algorithm, String metric, int howMany) {
		String filePath = clustersPath + "//cluster.csv";
		File csvFile = new File(filePath);
		if(csvFile.exists() == false) { //jeśli nie ma pliku
			Object[] options = {"Manually locate file", "Cancel procedure",};
			int n = JOptionPane.showOptionDialog(null,
							"No input.csv file in:\n"+filePath+ "\nDo you want to select location manually?",
							"No CSV invariants file", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter(".csv - Comma Separated Values", new String[] { "CSV" });
				filePath = Tools.selectFileDialog(clustersPath, filters, "Select", 
						"Select CSV invariants file");
				if(filePath.equals(""))
					return null;
				
				csvFile = new File(filePath);
				if(csvFile.exists() == false)
					return null;
			} else { 
				return null;
			}
		}

		String msg = "CSV invariants file: "+filePath+" located. Starting single clustering procedure." ;
		overlord.log(msg, "text", true);
		String resultFilePath_MCT = "";
		String resultFilePath_clusterCSV = filePath;
		try {
			overlord.log("Starting MCT generator for file: "+filePath, "text", true);
			Runner mctRunner = new Runner();
			mctRunner.activate(new String[] { filePath } ); //throwable
			
			resultFilePath_MCT = filePath + ".analysed.txt";
			
			
		} catch (Exception e) {
			msg = "MCT generation(file) failed for: "+filePath;
			overlord.log(msg, "text", true);
			JOptionPane.showMessageDialog(null, msg, "Critical error",JOptionPane.ERROR_MESSAGE);
			return null;
		}

		Rprotocols rp = new Rprotocols();
		String rPath = overlord.getSettingsManager().getValue("r_path");
		String csvFileName = csvFile.getName();
		String absolutePath = csvFile.getAbsolutePath();
		String pathOutput = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator)) + "//";
		String resultFilePath_r = "";
		pathOutput = pathOutput.replace("\\", "/");
		try {
			if(metric.equals("pearson") || metric.equals("correlation")) {
				resultFilePath_r = rp.generateSingleClustering(rPath, pathOutput, csvFileName, 
						"scripts\\f_SingleCluster_Pearson.r", metric, algorithm, howMany);
			} else {
				resultFilePath_r = rp.generateSingleClustering(rPath, pathOutput, csvFileName, 
						"scripts\\f_SingleCluster.r", metric, algorithm, howMany);
			}
		} catch (Exception e) {
			overlord.log("R function failed for parameters:", "error", true);
			overlord.log("File name: "+csvFileName, "error", false);
			overlord.log("Output dir: "+pathOutput, "error", false);
			overlord.log("Algorithm: "+algorithm, "error", false);
			overlord.log("Metric: "+metric, "error", false);
			overlord.log("No. of clusters: "+howMany, "error", false);
			JOptionPane.showMessageDialog(null, "Clustering failed. Check log.", "Critical error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String result[] = new String[5];
		result[0] = resultFilePath_clusterCSV;
		result[1] = resultFilePath_r;
		result[2] = resultFilePath_MCT;
		result[3] = clustersPath+"//"+algorithm+"_"+metric+"_clusters_ext_"+howMany+".pdf";
		result[4] = clustersPath+"//"+algorithm+"_"+metric+"_dendrogram_ext_"+howMany+".pdf";
		return result;
	}
}
