package holmes.darkgui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import holmes.adam.mct.Runner;
import holmes.analyse.InvariantsCalculator;
import holmes.files.clusters.Rprotocols;
import holmes.files.io.ProjectReader;
import holmes.files.io.ProjectWriter;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.PetriNet.GlobalFileNetType;
import holmes.petrinet.data.PetriNet.GlobalNetType;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.HolmesFileView;
import holmes.utilities.Tools;
import holmes.varia.Check;
import holmes.workspace.ExtensionFileFilter;

import static holmes.graphpanel.EditorResources.*;

/**
 * Klasa odpowiedzialna za meta-obsługę wszystkich metod wejścia-wyjścia i paru innych, dla GUIManager.
 * W ogólności, inne elementy interfejsu wywołują zawarte tutaj metody, a z nich sterowania w miarę 
 * potrzeby idzie dalej, aby zrealizować daną funkcję programu.
 * Krótko: kiedyś wszystkie metody tu zawarte były w klasie GUIManager. Ale zrobiło się tam zbyt tłoczno.
 * Wciąż z resztą jest.
 */
public class GUIOperations {
	GUIManager overlord;

	/**
	 * Konstruktor domyślny obiektu klasy GUIOperations. A nóż do czegoś się przyda...
	 */
	public GUIOperations() {
		overlord = GUIManager.getDefaultGUIManager();
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
	public void importNetwork() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[5];
		filters[0] = new ExtensionFileFilter("All supported Snoopy files", new String[] { "SPPED", "SPEPT", "SPTPT", "PN" });
		filters[1] = new ExtensionFileFilter("Snoopy Petri Net file (.spped), (.pn)", new String[] { "SPPED" , "PN" });
		filters[2] = new ExtensionFileFilter("Snoopy Extended PN file (.spept), (.xpn)", new String[] { "SPEPT","XPN" });
		filters[3] = new ExtensionFileFilter("Snoopy Time PN file (.sptpt)", new String[] { "SPTPT" });
		filters[4] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters,  "Select PN", "Select petri net file", "");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists())
			return;
		
		boolean status = overlord.getWorkspace().getProject().loadFromFile(file.getPath());
		if(status) {
			overlord.setLastPath(file.getParentFile().getPath());
			overlord.getSimulatorBox().createSimulatorProperties(false);
			GUIManager.getDefaultGUIManager().getFrame().setTitle(
					"Holmes "+GUIManager.getDefaultGUIManager().getSettingsManager().getValue("holmes_version")+
					"  "+Tools.getFileName(file));
		}
	}
	
	/**
	 * Metoda odpowiedzialna za otwieranie pliku z zapisaną siecią w formacie .abyss lub pliku projektu
	 */
	@SuppressWarnings("unlikely-arg-type")
	public void selectAndOpenHolmesProject() {
		String lastPath = overlord.getLastPath();
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());
		FileFilter holmesProjFilter = new ExtensionFileFilter("Holmes Project file (.project)", new String[] { "PROJECT" });
		FileFilter projFilter = new ExtensionFileFilter("Old project file (.apf)", new String[] { "APF" });
		FileFilter abyssFilter = new ExtensionFileFilter("Abyss Petri Net (.abyss)", new String[] { "ABYSS" });

		fc.setFileFilter(holmesProjFilter);
		fc.addChoosableFileFilter(holmesProjFilter);
		fc.addChoosableFileFilter(projFilter);
		fc.addChoosableFileFilter(abyssFilter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String extension = fc.getFileFilter().getDescription();
			if(fc.getSelectedFile().toString().equals(""))
				return;
			if(!file.exists()) 
				return;
			
			boolean proceed = overlord.reset.newProjectInitiated();
			if(!proceed) {
				return;
			}
			
			boolean status = false;
			if (extension.toLowerCase().contains(".apf") || extension.toLowerCase().contains(".project")) { //ABYSS/Holmes project reader
				ProjectReader pRdr = new ProjectReader();
				//access controller:
				GUIController.access().setRefresh(false);

				status = pRdr.readProject(file.getPath());

				GUIController.access().setRefresh(true);
				
				overlord.setLastPath(file.getParentFile().getPath());	
				overlord.subnetsGraphics.resizePanels();
			} else if (extension.toLowerCase().contains(".abyss")) { //ABYSS parser, plug and pray
				status = overlord.getWorkspace().getProject().loadFromFile(file.getPath());
				overlord.setLastPath(file.getParentFile().getPath());
			}
			
			if(status) {
				overlord.log("Reading project file succcessful.", "text", true);
				GUIManager.getDefaultGUIManager().getFrame().setTitle(
						"Holmes "+GUIManager.getDefaultGUIManager().getSettingsManager().getValue("holmes_version")+
						"  "+Tools.getFileName(file));
			}
		}
	}
	
	/**
	 * Metoda odpowiedzialna za eksport sieci do pliku w formacie programu INA.
	 */
	public void exportAsPNT() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("INA PNT file (.pnt)", new String[] { "PNT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "", overlord.getWorkspace().getProject().getFileName());
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
	 * @param t_inv (<b>boolean</b>) true, jeśli zapisujemy t-inwarianty.
	 * @return (<b>boolean</b>) - true, jeśli operacja przebiegła bez problemów.
	 */
	public boolean exportGeneratedInvariants(boolean t_inv) {
		String lastPath = overlord.getLastPath();
		int result = 0;
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());
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
				
				result = overlord.getWorkspace().getProject().saveInvariantsToInaFormat(file.getPath() + fileExtension, t_inv);
			} else if (description.contains("Comma")) {
				if(file.getPath().toLowerCase().contains(".csv"))
					fileExtension = "";
				else
					fileExtension = ".csv";
				
				result = overlord.getWorkspace().getProject().saveInvariantsToCSV(file.getPath() + fileExtension, false, t_inv);
			} else if (description.contains("Charlie")) {
				if(file.getPath().toLowerCase().contains(".inv"))
					fileExtension = "";
				else
					fileExtension = ".inv";
				
				result = overlord.getWorkspace().getProject().saveInvariantsToCharlie(file.getPath() + fileExtension, t_inv);
			}
		}

		return result == 0;
	}
	
	/**
	 * Metoda odpowiedzialna za eksport projektu do pliku graficznego w określonym formacie.
	 */
	public void exportProjectToImage() {
		String lastPath = overlord.getLastPath();
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());
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
	 * Metoda odpowiedzialna za zapis projektu sieci do pliku natywnego aplikacji..
	 * @return (<b>boolean</b>) - status operacji: true jeśli nie było problemów.
	 */
	public boolean saveAsAbyssFile() {
		boolean status = false;
		String lastPath = overlord.getLastPath();
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());
		FileFilter holmesProjFilter = new ExtensionFileFilter("Holmes Project file (.project)", new String[] { "PROJECT" });
		FileFilter projFilter = new ExtensionFileFilter("Old Project file (.apf)", new String[] { "APF" });
		FileFilter abyssFilter = new ExtensionFileFilter("Abyss Petri Net (.abyss)", new String[] { "ABYSS" });

		fc.setFileFilter(holmesProjFilter);
		fc.addChoosableFileFilter(holmesProjFilter);
		fc.addChoosableFileFilter(projFilter);
		fc.addChoosableFileFilter(abyssFilter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String extension = fc.getFileFilter().getDescription();
			if (extension.toLowerCase().contains(".project")) {
				if(!Tools.overwriteDecision(file.getPath()))
					return false;
				String fileExtension = ".project";
				if(file.getPath().toLowerCase().contains(".project"))
					fileExtension = "";
				
				ProjectWriter pWrt = new ProjectWriter();
				status = pWrt.writeProject(file.getPath() + fileExtension);
				overlord.setLastPath(file.getParentFile().getPath());
				return status;
			}
			if (extension.toLowerCase().contains(".apf")) {
				if(!Tools.overwriteDecision(file.getPath()))
					return false;
				String fileExtension = ".apf";
				if(file.getPath().toLowerCase().contains(".apf"))
					fileExtension = "";
				
				ProjectWriter pWrt = new ProjectWriter();
				status = pWrt.writeProject(file.getPath() + fileExtension);
				overlord.setLastPath(file.getParentFile().getPath());
				return status;
			}
			if (extension.toLowerCase().contains(".abyss")) {
				if(!Tools.overwriteDecision(file.getPath()))
					return false;

				String fileExtension = ".abyss";
				if(file.getPath().toLowerCase().contains(".abyss"))
					fileExtension = "";
				status = overlord.getWorkspace().getProject().saveAsAbyss(file.getPath() + fileExtension);
				overlord.setLastPath(file.getParentFile().getPath());
				return status;
			}
		}
		return status;
	}
	
	/**
	 * Metoda ogólnego zapisu, pozwala wybrać format wyjściowy. Domyślnie SPPED.
	 * @return (<b>boolean</b>) - status operacji: true jeśli nie było problemów.
	 */
	public boolean saveAsGlobal() {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[6];
		
		filters[0] = new ExtensionFileFilter("All supported Snoopy files", new String[] { "SPPED", "SPEPT", "SPTPT" });
		filters[1] = new ExtensionFileFilter("Snoopy Petri Net (.spped)", new String[] { "SPPED" });
		filters[2] = new ExtensionFileFilter("Snoopy Extended Petri Net (.spept)", new String[] { "SPEPT" });
		filters[3] = new ExtensionFileFilter("Snoopy Time Petri Net (.sptpt)", new String[] { "SPTPT" });
		filters[4] = new ExtensionFileFilter("Holmes Project File (.project)", new String[] { "PROJECT" });
		filters[5] = new ExtensionFileFilter("INA PNT format (.pnt)", new String[] { "PNT" });

		String selectedFile = Tools.selectNetSaveFileDialog(lastPath, filters, "Save", "", overlord.getWorkspace().getProject().getFileName());
		if(selectedFile.equals("")) {
			return false;
		}
		
		if(!Tools.overwriteDecision(selectedFile))
			return false;
		
		String extension = Tools.lastExtension;
		if(extension == null || extension.equals("")) {
			JOptionPane.showMessageDialog(null, "File choosing error. Cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
			overlord.log("File choosing error. No extension: "+extension, "error", true);
			return false;
		}

		extension = checkFileFormatCorrectness(extension);

		if (extension.contains(".spped")) {
			File file = new File(selectedFile);
			String fileExtension = ".spped";
			if(selectedFile.toLowerCase().contains(".spped"))
				fileExtension = "";
			
			boolean status = overlord.getWorkspace().getProject().saveAsSPPED(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
			return status;
		} else if (extension.contains(".spept")) {
			File file = new File(selectedFile);
			String fileExtension = ".spept";
			if(selectedFile.toLowerCase().contains(".spept"))
				fileExtension = "";
			
			boolean status = overlord.getWorkspace().getProject().saveAsSPEPT(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
			return status;
		} else if (extension.contains(".sptpt")) {
			File file = new File(selectedFile);
			String fileExtension = ".sptpt";
			if(selectedFile.toLowerCase().contains(".sptpt"))
				fileExtension = "";
			
			boolean status = overlord.getWorkspace().getProject().saveAsSPTPT(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
			return status;
		} else if (extension.contains(".project")) {
			File file = new File(selectedFile);
			if(!Tools.overwriteDecision(file.getPath()))
				return false;
			String fileExtension = ".project";
			if(file.getPath().toLowerCase().contains(".project"))
				fileExtension = "";
			
			ProjectWriter pWrt = new ProjectWriter();
			boolean status = pWrt.writeProject(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
			return status;
		} else if (extension.contains(".pnt") ) {
			File file = new File(selectedFile);
			String fileExtension = ".pnt";
			if(selectedFile.toLowerCase().contains(".pnt"))
				fileExtension = "";
			
			boolean status = overlord.getWorkspace().getProject().saveAsPNT(file.getPath() + fileExtension);
			overlord.setLastPath(file.getParentFile().getPath());
			return status;
		}
		return false;
	}

	/**
	 * Metoda sprawdza, czy wybrany format zapisu się nadaje.
	 * @param extension String - wybrany format pliku
	 * @return String - format, który będzie użyty
	 */
	private String checkFileFormatCorrectness(String extension) {
		if(overlord.getSettingsManager().getValue("editorExportCheckAndWarning").equals("0"))
			return extension; //stop whining mode ON
		
		if(extension.contains(".project"))
			return extension; //always right! ;)
		
		if(extension.contains("all supported")) {
			extension = "snoopy petri net (.spped)";
			Tools.lastExtension = extension;
		}
		
		GlobalNetType netType = Check.getSuggestedNetType();
		
		String fileFormat;
		String additionalWhining = "";
		String netRealName ;
		if(netType != null) {
			GlobalFileNetType suggestion = Check.suggestesFileFormat(netType);
			fileFormat = Check.getExtension(suggestion);
			fileFormat = fileFormat.toLowerCase();
			netRealName = Check.getNetName(netType);
			if(Check.isHierarchical()) {
				additionalWhining = "\nWarning: hierarchical net structure detected. Using Holmes project is strongly advised.\n"
						+ "Other save format will most probably fail.\n";
			}
		} else {
			netRealName = "Unknown";
			fileFormat = "Holmes Project file";
			additionalWhining = "Warning. There has been a problem detecting type of the Petri net.\n"
					+ "Please advise authors and in the meantime: Holmes project file is STRONGLY RECOMMENDED.";
		}
		
		if(extension.toLowerCase().contains(fileFormat) && additionalWhining.length()==0) {
			return extension;
		} else {
			if(fileFormat.equals(".project"))
				fileFormat = "Holmes project file (.project)";

			Object[] options = {"Use selected anyway", "Use suggested format", "Save as project", "Cancel save",};
			int n = JOptionPane.showOptionDialog(null,
							"Selected net file format: "+extension+"\n"
							+ "Net real type: "+netRealName+"\n"
							+ "Suggested file format: "+fileFormat+"\n\n"
							+ "Selected format will not contain all features of the current Petri Net. Resulting file\n"
							+ "will contain reduced net or will be corrupted.\n"+additionalWhining,
							"Invalid out net file format", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (n == 0) {
				return extension;
			} else if (n == 1) {
				return "."+fileFormat;
			} else if (n == 2) {
				return ".project";
			} else {
				return "";
			}	
		}
	}
	
	/**
	 * Metoda odpowiedzialna za wczytywanie inwariantów z pliku.
	 * @param t_inv (<b>boolean</b>) true, jeśli chodzi o t-inwarianty.
	 * @return (<b>boolean</b>) - true, jeśli operacja się powiodła.
	 */
	public boolean loadExternalAnalysis(boolean t_inv) {
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		if(t_inv)
			filters[0] = new ExtensionFileFilter("T-invariants file (.inv)", new String[] { "INV" });
		else
			filters[0] = new ExtensionFileFilter("P-invariants file (.inv)", new String[] { "INV" });
		
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load invariants", "Select invariant file", "");
		if(selectedFile.equals(""))
			return false;
		
		File file = new File(selectedFile);
		if(!file.exists()) {
			return false;
		}

		PetriNet project = overlord.getWorkspace().getProject();
		boolean status = project.loadTPinvariantsFromFile(file.getPath(), t_inv);
		if(!status) {
			return false;
		}
		
		if(t_inv) {
			overlord.getT_invBox().showT_invBoxWindow(project.getT_InvMatrix());
			overlord.getSimulatorBox().createSimulatorProperties(false);
		} else {
			overlord.getP_invBox().showP_invBoxWindow(project.getP_InvMatrix());
		}
		return true;
	}
	
	/**
	 * Metoda uruchamia sekwencję zdarzeń prowadząca do wygenerowania inwariantów
	 * za pomocą programu INA działającego jako niezależna aplikacja. Zaleca się
	 * nie zaglądanie jak i co ona robi (metoda, nie INA), gdyż może to doprawadziź
	 * do słabszych duchem programistów do rozstroju nerwowego, szczególnie w kontekscie
	 * operacji na plikach.
	 * @param t_inv (<b>boolean</b>) true, jeśli mają być liczone t-inwarianty, false: p-inwarianty.
	 */
	@SuppressWarnings("all")
	public void generateINAinvariants(boolean t_inv) {
		String stars = "************************************************************************************************";
		String toolPath = overlord.getToolPath();
		File tmpPNTfile = new File(toolPath+"siec.pnt");
		String x = tmpPNTfile.getPath();
		overlord.getWorkspace().getProject().saveAsPNT(x); //zakończono zapis do pliku .pnt
		long size = tmpPNTfile.length(); //124 dla nieistniejącej (pustej) sieci
		if(size <154) {
			String msg = "Net saving as .pnt file failed. There may be problems with file: "+x + 
					" or there is no network yet.";
			JOptionPane.showMessageDialog(null, msg, "Missing net or file", JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			overlord.accessInvariantsWindow().setGeneratorStatus(false);
			return;
		}
		
		File inaExe = new File(toolPath+"INAwin32.exe");
		File batFile = new File(toolPath+"ina.bat");
		File commandFile = new File(toolPath+"COMMAND.ina");
		if(!t_inv)
			commandFile = new File(toolPath+"COMMANDp.ina");
		
		String holmesPath = overlord.getHolmesPath();
		if(inaExe.exists() && commandFile.exists()) {
			try {
				JOptionPane.showMessageDialog(null, "INAwin32.exe will now start. This may take a while. Click OK and please wait.\n"
						+ "When console shows in, please type Y, then N after invariants are computed.", "Please wait", JOptionPane.INFORMATION_MESSAGE);
				overlord.log(stars, "text", false);
				overlord.log("Activating INAwin32.exe. Please wait, this may take a few seconds due to OS delays.", "text", true);
				//kopiowanie plików:
				Tools.copyFileByPath(inaExe.getPath(), holmesPath+"\\INAwin32.exe");
				Tools.copyFileByPath(batFile.getPath(), holmesPath+"\\ina.bat");
				Tools.copyFileByPath(commandFile.getPath(), holmesPath+"\\COMMAND.ina");
				Tools.copyFileByPath(tmpPNTfile.getPath(), holmesPath+"\\siec.pnt");
				
				tmpPNTfile.delete();
				
				String[] command = {"ina.bat"};
			    ProcessBuilder b = new ProcessBuilder(command);
			    Process proc;
			
				proc = b.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while (in.readLine() != null) ; 
				//while (in.readLine() != null) ;
				Thread.sleep(200);
				proc.destroy();
				
				new File(holmesPath+"\\INAwin32.exe").delete();
				new File(holmesPath+"\\ina.bat").delete();
				new File(holmesPath+"\\COMMAND.ina").delete();
				new File(holmesPath+"\\siec.pnt").delete();
				
				File t1 = new File(holmesPath+"\\SESSION.ina");
				File t2 = new File(holmesPath+"\\OPTIONS.ina");
				File t3 = new File(holmesPath+"\\INVARI.hlp");
				if(t1.exists())
					t1.delete();
				if(t2.exists())
					t2.delete();
				if(t3.exists())
					t3.delete();
				overlord.log("INAwin32.exe process terminated. Reading results into network now.", "text",true);
			} catch (Exception e) {
				String msg = "I/O operation: activating INAwin32.exe failed.";
				JOptionPane.showMessageDialog(null, msg, "Critical error", JOptionPane.ERROR_MESSAGE);
				overlord.log(msg, "error", true);
				overlord.log(stars, "text", false);
				overlord.accessInvariantsWindow().setGeneratorStatus(false);
				return;
			}
			
			// check whether the file with T-invariants has been generated
			File invariantsFile = new File("siec.inv");
			if (!invariantsFile.exists()) {
				String msg = "No invariants file - using INAwin32.exe unsuccessful.";
				JOptionPane.showMessageDialog(null,msg,	"Critical error",JOptionPane.ERROR_MESSAGE);
				overlord.log(msg, "error", true);
				overlord.accessInvariantsWindow().setGeneratorStatus(false);
				return;
			}
			
			//wczytywanie inwariantów do systemu:
			PetriNet project = overlord.getWorkspace().getProject();
			boolean status = project.loadTPinvariantsFromFile(invariantsFile.getPath(), t_inv);
			if(!status) {
				return;
			}
			
			if(t_inv) {
				overlord.getT_invBox().showT_invBoxWindow(project.getT_InvMatrix());
				overlord.getSimulatorBox().createSimulatorProperties(false);
			} else {
				overlord.getP_invBox().showP_invBoxWindow(project.getP_InvMatrix());
			}
		
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
						"Select invariants target path", "");
				
				if(!selectedFile.equals("")) { //jeśli wskazano plik
					File file = new File(selectedFile);
					String ext = "";
					if(!file.getPath().contains(".inv"))
						ext = ".inv";
					File properName = new File(file.getPath() + ext);
					Tools.copyFileDirectly(invariantsFile, properName);
				}
			}
			overlord.log("Invariants generation successful.", "text", true);
			overlord.log(stars, "text", false);
			invariantsFile.delete();
			overlord.accessInvariantsWindow().setGeneratorStatus(false);
		} else { //brak plikow
			overlord.accessInvariantsWindow().setGeneratorStatus(false);
			String msg = "Missing executables in the tools directory. Required: INAwin32.exe, ina.bat and COMMAND.ina";
			JOptionPane.showMessageDialog(null,msg,	"Missing files",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
		}
	}
	
	/**
	 * Metoda szybkiego wywoływania generatora inwariantów algorytmem wewnętrznym.
	 */
	public void fastGenerateTinvariants() {
		boolean status = overlord.accessInvariantsWindow().isGeneratorWorking;
		if(status) {
			JOptionPane.showMessageDialog(null, "Invariants generation already in progress.", "Generator working",JOptionPane.WARNING_MESSAGE);
		} else {
			InvariantsCalculator invGenerator = new InvariantsCalculator(true);
			Thread myThread = new Thread(invGenerator);
			myThread.start();
		}
	}

	/**
	 * Metoda generująca najbardziej postawową wersję pliku zbiorów MCT.
	 */
	public void generateSimpleMCTFile() {
		String filePath = overlord.getTmpPath() + "input.csv";
		int result = overlord.getWorkspace().getProject().saveInvariantsToCSV(filePath, true, true);
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
			String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Select MCT target path", "");
			
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
				//overlord.setLastPath(file.getParentFile().getPath());
				
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
	 * Metoda odpowiedzialna za obliczanie metryk Calinskiego-Harabasza dla klastrów sieci Petriego.
	 * @param howMany int - maksymalna liczba klastrów
	 * @param commandsValidate ArrayList[String] - lista wywołań w R do uruchomienia
	 * @return String - katalog z plikami miar
	 */
	public String generateAllCHindexes(int howMany, ArrayList<String> commandsValidate) {
		if(!overlord.getRStatus()) { //sprawda, czy Rscript.exe jest na miejscu
			overlord.checkRlangStatus(true); // zapytanie gdzie się podziewa Rscript.exe
			if(!overlord.getRStatus()) { //jeśli wciąż...
				return null;
			}
		}
		
		String CSVfilePath = selectionOfSource();
		if(CSVfilePath == null)
			return null;
		
		String dir_path = "";
		int c_number = howMany;
		try{
			int invNumber;
			if(overlord.getWorkspace().getProject().getT_InvMatrix() == null) {
				overlord.log("Warning: unable to check if given clusters number ("+howMany+") exceeds invariants "
						+ "number. If so, the procedure may fail.", "warning", true);
			} else {
				invNumber = overlord.getWorkspace().getProject().getT_InvMatrix().size();
				if(invNumber < howMany)
					howMany = invNumber;
			}
			
			//File test = new File(CSVfilePath);
			//String CSVDirectoryPath = test.getParent();

			Object[] options = {"Select CH metric directory", "Use temporary directory",};
			int n = JOptionPane.showOptionDialog(null,
					"Multiple CH metric files can we written into default temporary directory (inadvised) or into\n"
					+ "the selected one. What to do?",
					"Directory selection", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				String choosenDir = Tools.selectDirectoryDialog(overlord.getLastPath(), "Select CH metric dir",
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
			
			overlord.showConsole(true);
			dir_path = dir_path.replace("\\", "/");
			
			File test64 = new File(overlord.getSettingsManager().getValue("r_path64"));
			String r_path;
			if(test64.exists())
				r_path = overlord.getSettingsManager().getValue("r_path64");
			else {
				r_path = overlord.getSettingsManager().getValue("r_path");
				overlord.log("Warning: Celinski-Harabasz metric computation in 32bit mode for large number of invariants can cause R/system crash","warning",true);
			}
			
			Runnable runnable = new Rprotocols(1);
			((Rprotocols)runnable).setForRunnableAllClusters(r_path, dir_path, "cluster.csv", 
					"scripts\\f_CHindex.r", "scripts\\f_clusters_run.r", 
					"scripts\\f_CHindex_Pearson.r", "scripts\\f_clusters_Pearson_run.r", howMany, commandsValidate);
	        Thread thread = new Thread(runnable);
	        thread.start();
	        return dir_path;
		} catch (IOException e){
			String msg = "CH metric computation failed for " + c_number + " clusters.\nPath: "+dir_path;
			JOptionPane.showMessageDialog(null, msg, "Critical error",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			overlord.log(e.getMessage(), "error", true);
			return null;
		}
	}
	
	/**
	 * Metoda odpowiedzialna za generowanie klastrowań na bazie inwariantów sieci.
	 * @param commandsValidate ArrayList[String] - lista wywołań w R do uruchomienia
	 * @return String - ścieżka do pliku cluster.csv na bazie którego powstały inne pliki
	 */
	public String generateClustersCase56(int howMany, ArrayList<String> commandsValidate) {
		if(!overlord.getRStatus()) { //sprawdź, czy Rscript.exe jest na miejscu
			overlord.checkRlangStatus(true); // zapytanie gdzie się podziewa Rscript.exe
			if(!overlord.getRStatus()) { //jeśli wciąż...
				return null;
			}
		}
		String CSVfilePath = selectionOfSource();
		if(CSVfilePath == null)
			return null;
		
		String dir_path = "";
		int c_number = howMany;
		try{
			int invNumber;
			if(overlord.getWorkspace().getProject().getT_InvMatrix() == null) {
				overlord.log("Warning: unable to check if a given number of clusters ("+howMany+") exceeds invariants "
						+ "number. If so, the procedure may fail.", "warning", true);
			} else {
				invNumber = overlord.getWorkspace().getProject().getT_InvMatrix().size();
				if(invNumber < howMany)
					howMany = invNumber;
			}

			Object[] options = {"Select cluster directory", "Use temporary directory", "Cancel operation"};
			int n = JOptionPane.showOptionDialog(null,
					"Multiple cluster files can we written into default temporary directory (not advised) or into\n"
					+ "the selected one. What to do?",
					"Directory selection", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				String choosenDir = Tools.selectDirectoryDialog(overlord.getLastPath(), "Select cluster dir",
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
			} else if(n==1) { //default one
				dir_path = overlord.getTmpPath();
				overlord.log("Cluster files will be put into the "+dir_path, "text", true);
			} else {
				return null;
			}
			
			overlord.showConsole(true);
			
			dir_path = dir_path.replace("\\", "/");
			
			Runnable runnable = new Rprotocols();
			((Rprotocols)runnable).setForRunnableAllClusters(overlord.getSettingsManager().getValue("r_path"), dir_path, "cluster.csv", 
					"scripts\\f_clusters.r", "scripts\\f_clusters_run.r", 
					"scripts\\f_clusters_Pearson.r", "scripts\\f_clusters_Pearson_run.r", c_number, commandsValidate);
			((Rprotocols)runnable).setWorkingMode(0);
            Thread thread = new Thread(runnable);
            thread.start();
            
            return dir_path+"/"+"cluster.csv";
		} catch (Exception e) {
			String msg = "Clustering generation failed for "+c_number+" clusters.\nPath: "+dir_path;
			JOptionPane.showMessageDialog(null, msg, "Critical error",JOptionPane.ERROR_MESSAGE);
			overlord.log(msg, "error", true);
			overlord.log(e.getMessage(), "error", true);
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
		if(overlord.getWorkspace().getProject().getT_InvMatrix() == null) { //brak inwariantów
			FileFilter[] filters = new FileFilter[1];
			filters[0] = new ExtensionFileFilter("CSV invariants file (.csv)",  new String[] { "CSV" });
			String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select CSV", "Select CSV file", "");
			
			if(selectedFile.equals(""))
				return null;
			else
				return selectedFile;
		} else {
			//wybór: z sieci, czy wskazanie CSV
			Object[] options = {"Select invariants file manually", "Use computed invariants", "Cancel operation"};
			int n = JOptionPane.showOptionDialog(null,
					"Please select invariant file (.CSV) for the clustering manually or use invariants\n"
					+ "from the current network (they MUST be computed/loaded already!).",
					"Invariants source", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter("CSV invariants file (.csv)",  new String[] { "CSV" });
				String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select CSV", "Select CSV file", "");
				
				if(selectedFile.equals(""))
					return null;
				else
					return selectedFile;
			} else if(n == 1) {
				//generowanie CSV, uda się, jeśli inwarianty istnieją
				String CSVfilePath = overlord.getTmpPath() + "cluster.csv";
				int result = overlord.getWorkspace().getProject().saveInvariantsToCSV(CSVfilePath, true, true);
				if(result == -1) {
					String msg = "Exporting invariants into CSV file failed. \nCluster procedure cannot begin.";
					JOptionPane.showMessageDialog(null,msg,	"CSV export error",JOptionPane.ERROR_MESSAGE);
					overlord.log(msg, "error", true);
					return null;
				}
				
				return CSVfilePath;
			} else {
				return null;
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
		if(!csvFile.exists()) { //jeśli nie ma pliku
			Object[] options = {"Manually locate file", "Cancel procedure",};
			int n = JOptionPane.showOptionDialog(null,
							"No input.csv file in:\n"+filePath+ "\nDo you want to select location manually?",
							"No CSV invariants file", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter(".csv - Comma Separated Values", new String[] { "CSV" });
				filePath = Tools.selectFileDialog(clustersPath, filters, "Select", "Select CSV invariants file", "");
				if(filePath.equals(""))
					return null;
				
				csvFile = new File(filePath);
				if(!csvFile.exists())
					return null;
			} else { 
				return null;
			}
		}

		String msg = "CSV invariants file: "+filePath+" located. Starting single clustering procedure." ;
		overlord.log(msg, "text", true);
		String resultFilePath_MCT;
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
		String resultFilePath_r;
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
			overlord.log("File name: "+csvFileName, "error", true);
			overlord.log("Output dir: "+pathOutput, "error", true);
			overlord.log("Algorithm: "+algorithm, "error", true);
			overlord.log("Metric: "+metric, "error", true);
			overlord.log("No. of clusters: "+howMany, "error", true);
			JOptionPane.showMessageDialog(null, "Clustering failed. Check log.", "Critical error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		String[] result = new String[5];
		result[0] = resultFilePath_clusterCSV;
		result[1] = resultFilePath_r;
		result[2] = resultFilePath_MCT;
		result[3] = clustersPath+"//"+algorithm+"_"+metric+"_clusters_ext_"+howMany+".pdf";
		result[4] = clustersPath+"//"+algorithm+"_"+metric+"_dendrogram_ext_"+howMany+".pdf";
		return result;
	}

	/**
	 * Metoda odpowiedzialna za wpisanie nowej wartości czasu/kroku w podoknie symulatora.
	 * @param XTPN (<b>boolean</b>) true, jeżeli mówimy o symulatorze XTPN.
	 * @param stepsValue (<b>long</b>) liczba kroków symulacji (aktualna).
	 * @param timeValue (<b>double</b>) aktualny czas od startu symulacji (XTPN).
	 */
	public void updateTimeStep(boolean XTPN, long stepsValue, double timeValue, double tau) {
		try {
			if (XTPN) {
				overlord.getSimulatorBox().getCurrentDockWindow().stepLabelXTPN.setText("" + stepsValue);
				overlord.getSimulatorBox().getCurrentDockWindow().timeLabelXTPN.setText(Tools.cutValueExt(timeValue, 2)
						+ " (" + Tools.cutValueExt(tau, 2) + ")");
			} else {
				overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("" + stepsValue);
			}
		} catch (Exception e) {
			overlord.log("Unable to update simulator fields: (XTPN="+XTPN+"; stepsValue="+stepsValue+"; stepsValue="+stepsValue + "."
					, "warning", true);
		}
	}
	
	public void markTransitions(int mode) {
		overlord.getWorkspace().getProject().resetNetColors();

		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		if(mode == 0) { //TPN only
			for(Transition t : transitions) {
				if(t.timeExtension.isTPN()) {
					t.drawGraphBoxT.setColorWithNumber(true, Color.green, false, -1, true, "TPN");
				}
			}
		} else if(mode == 1) { //DPN
			for(Transition t : transitions) {
				if(t.timeExtension.isDPN()) {
					t.drawGraphBoxT.setColorWithNumber(true, Color.green, false, -1, true, "DPN");
				}
			}
		} else if(mode == 2) { //TPN i TPN
			for(Transition t : transitions) {
				if(t.timeExtension.isTPN() && !t.timeExtension.isDPN()) {
					t.drawGraphBoxT.setColorWithNumber(true, tpnNOTdpn, false, -1, true, "TPN");
				} else if(!t.timeExtension.isTPN() && t.timeExtension.isDPN()) {
					t.drawGraphBoxT.setColorWithNumber(true, dpnNOTtpn, false, -1, true, "DPN");
				} else if(t.timeExtension.isTPN() && t.timeExtension.isDPN()) {
					t.drawGraphBoxT.setColorWithNumber(true, tpnANDdpn, false, -1, true, "TPN / DPN");
				}
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	
	public void fixArcsProblem() {
		ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();
		
		int arcSize = arcs.size();
		int arcCounter = 0;
		int ghosts = 0;
		
		for(Place p : places) { 
			for(ElementLocation el : p.getElementLocations()) {
				ArrayList<Arc> outArcs = el.getOutArcs();

				for(Arc a : outArcs) { //dla każdego łuku
					arcCounter++;
					if(!arcs.contains(a)) {
						ghosts++;
						int placeId = places.indexOf(p);
						int transId = transitions.indexOf((Transition)a.getEndNode());
						overlord.log("Invisible arc: p"+placeId+" -> t"+transId+". Removing...", "error", true);
						removeArc(a, arcs);
					}
				}
			}
		}
		
		for(Transition t : transitions) { 
			for(ElementLocation el : t.getElementLocations()) {
				ArrayList<Arc> outArcs = el.getOutArcs();

				for(Arc a : outArcs) { //dla każdego łuku
					arcCounter++;
					if(!arcs.contains(a)) {
						ghosts++;
						
						int transId = transitions.indexOf(t);
						int placeId = places.indexOf((Place)a.getEndNode());
						overlord.log("Invisible arc: t"+transId+" -> p"+placeId+". Removing...", "error", true);
						removeArc(a, arcs);
					}
				}
			}
		}
		
		overlord.log("Arc list: "+arcSize+", processed arcs: "+arcCounter+", removed ghost-arcs: "+ghosts, "text", true);
	}
	
	private void removeArc(Arc arc, ArrayList<Arc> arcs) {
		overlord.markNetChange();
		arc.unlinkElementLocations();
		arcs.remove(arc);
		if (arc.getPairedArc() != null) { // jeśli to read-arc, usuń też łuk sparowany
			Arc a = arc.getPairedArc();
			a.unlinkElementLocations();
			arcs.remove(a);
		}
	}
}
