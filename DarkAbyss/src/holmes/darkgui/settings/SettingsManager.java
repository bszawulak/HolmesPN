package holmes.darkgui.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;

/**
 * Klasa zarządzająca plikiem konfiguracyjnym programu.
 * @author students - pierwsza podstawowa wersja która nic nie robiła :)
 * @author MR - w tej chwili większość kodu tutaj
 *
 */
public class SettingsManager {
	private ArrayList<Setting> settings;

	/**
	 * Konstruktor domyślny obiektu klasy SettingsManager.
	 */
	public SettingsManager() {
		settings = new ArrayList<Setting>();
	}
	
	/**
	 * Metoda zwraca wartość związaną z podawanym ID.
	 * @param ID String - identyfikator właściwości
	 * @return String - właściwość
	 */
	public String getValue(String ID) {
		for(Setting s : settings) {
			if(s.getID().equals(ID)) 
				return s.getValue();
		}
		return null;
	}
	
	/**
	 * Metoda ustawia nową wartość związaną z podawanym ID.
	 * @param ID String - identyfikator właściwości
	 * @param value String - nowa wartość właściwości
	 * @param save boolean - jeśli true to od razu zapis do pliku
	 * @return int - 0 jeśli znaleziono id, -1 jeśli nie - dodawana jest wtedy od razu NOWA
	 * 		właściwość pod podanym ID
	 */
	public int setValue(String ID, String value, boolean save) {
		boolean found= false;
		for(Setting s : settings) {
			if(s.getID().equals(ID)) {
				found = true;
				s.setValue(value);
			}
		}
		if (found == false) {
			settings.add(new Setting(ID,value));
			if(save) writeSettingsFile();
			
			return -1;
		} else {
			if(save) writeSettingsFile();
			
			return 0;
		}
	}
	
	/**
	 * Metoda dodaje nową właściwość z ID oraz wartością
	 * @param ID String - unikalny ID
	 * @param value String - wartość właściwości
	 */
	private void addSetting(ArrayList<Setting> settings, String ID, String value) {
		for(Setting s : settings) {
			if(s.getID().equals(ID)) {
				Random rand = new Random();
			    ID += rand.nextInt(999999);
			    break;
			}
		}
		settings.add(new Setting(ID, value));
	}
	
	/**
	 * Metoda odpowiedzialna za sprawdzenie wczytanych z pliku ustawień i w razie konieczności ich korektę.
	 */
	@SuppressWarnings("unused")
	private void checkAndRestoreSetting() {
		ArrayList<Setting> settingsNew = new ArrayList<Setting>();
		
		//checkAndFix(settingsNew, "abyss_version", "1.30 release 30-3-2015");
		//settingsNew.add(new Setting("abyss_version", "1.31 release 7-4-2015")); 
		//settingsNew.add(new Setting("abyss_version", "1.37 release 19-4-2015")); //always add new
		settingsNew.add(new Setting("holmes_version", "2.0.8"));
		
		checkAndFix(settingsNew, "r_path", "c://Program Files//R//R-3.1.2//bin//Rscript.exe");
		checkAndFix(settingsNew, "r_path64","c://Program Files//R//R-3.1.2//bin//x64//Rscript.exe");
		checkAndFix(settingsNew, "lastOpenedPath", "");
		checkAndFix(settingsNew, "ina_bat","START INAwin32.exe COMMAND.ina");
		checkAndFix(settingsNew, "ina_COMMAND1", " 80 4294901760 0 1 :BNNATTFFFFFFFFTFTFFFFFTFFFFFFTTFFFFTFasiec");
		checkAndFix(settingsNew, "ina_COMMAND2","nnsyp");
		checkAndFix(settingsNew, "ina_COMMAND2p", "nnsnnnfnn");
		checkAndFix(settingsNew, "ina_COMMAND3", "nnnfnn");
		checkAndFix(settingsNew, "ina_COMMAND4", "eqqy");
		
		//program - ogólne
		checkAndFix(settingsNew, "programUseOldSnoopyLoaders", "0");
		checkAndFix(settingsNew, "programAskForRonStartup", "0");
		try { 
			String tmp = getValue("programSnoopyLoaderNetExtFactor");
			int test = Integer.parseInt(tmp); 
			checkAndFix(settingsNew, "programSnoopyLoaderNetExtFactor", "100");
		} catch (Exception e) { settingsNew.add(new Setting("programSnoopyLoaderNetExtFactor", "100")); }
		checkAndFix(settingsNew, "programUseSimpleEditor", "0");
		checkAndFix(settingsNew, "programDebugMode", "0");
		
		//edytor:
		checkAndFix(settingsNew, "editorGridLines", "1");
		checkAndFix(settingsNew, "editorGridAlignWhenSaved", "1");
		checkAndFix(settingsNew, "editorUseSnoopyOffsets", "1");
		try { 
			String tmp = getValue("editorGraphArcLineSize");
			int test = Integer.parseInt(tmp);
			if(test < 1 || test > 3)
				throw new Exception();
			checkAndFix(settingsNew, "editorGraphArcLineSize", "1");
		} catch (Exception e) { settingsNew.add(new Setting("editorGraphArcLineSize", "1")); }
		
		checkAndFix(settingsNew, "editor3Dview", "0");
		checkAndFix(settingsNew, "editorSnoopyStyleGraphic", "0");
		checkAndFix(settingsNew, "editorSnoopyColors", "0");
		checkAndFix(settingsNew, "editorSnoopyCompatibleMode", "1");
		checkAndFix(settingsNew, "editorShowShortNames", "0");
		checkAndFix(settingsNew, "editorExportCheckAndWarning", "1");
		
		try { 
			String tmp = getValue("editorGraphFontSize");
			int test = Integer.parseInt(tmp);
			if(test < 8 || test > 15)
				throw new Exception();
			checkAndFix(settingsNew, "editorGraphFontSize", "11");
		} catch (Exception e) { settingsNew.add(new Setting("editorGraphFontSize", "11")); }
		checkAndFix(settingsNew, "editorGraphFontBold", "0");
		checkAndFix(settingsNew, "editorSubnetCompressMode", "0");
		
		//invariants:
		checkAndFix(settingsNew, "analysisBinaryCSVInvariants", "0");
		checkAndFix(settingsNew, "analysisFeasibleSelfPropAccepted", "1");
		checkAndFix(settingsNew, "analysisMCSReduction", "1");
		
		checkAndFix(settingsNew, "analysisRemoveNonInv", "0");
		checkAndFix(settingsNew, "analysisRemoveSingleElementInv", "0");
		
		//knockout:
		checkAndFix(settingsNew, "mctNameShow", "1");
		
		//symulator:
		checkAndFix(settingsNew, "simTransReadArcTokenReserv", "1"); //jeśli 1, tranzycje rezerwują tokeny poprzez readarc
		checkAndFix(settingsNew, "simPlacesColors", "1");
		checkAndFix(settingsNew, "simSingleMode", "1");
		checkAndFix(settingsNew, "simTDPNrunWhenEft", "0");
		
		settings = new ArrayList<Setting>(settingsNew);
		writeSettingsFile();
	}
	
	/**
	 * Metoda sprawdza, czy dane ustawienie istnieje - jeśli tak, dodaje je do nowej listy, jeśli nie - ustawia
	 * jego nową wartość na domyślną.
	 * @param settings ArrayList[Setting] - nowa tablica ustawień
	 * @param ID String - identyfikator ustawienia
	 * @param value String - wartość domyślna, jeśli nie udało się wczytać ustawienia z danym ID
	 */
	private void checkAndFix(ArrayList<Setting> settings, String ID, String value) {
		if(getValue(ID) == null)
			addSetting(settings, ID, value);
		else
			addSetting(settings, ID, getValue(ID));
	}

	/**
	 * Metoda odpowiedzialna za zapis właściwości programu do pliku.
	 */
	private void writeSettingsFile() {
		try {
			File configFile = new File("holmes.cfg");
			FileWriter cfgFileWriter = new FileWriter(configFile, false);
			for (Setting data : settings) {
				cfgFileWriter.write(data.getID() + " " + data.getValue()+"\n");
			}
			cfgFileWriter.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "For some reason, settings could not be saved.", "Unknown error",
					JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Unknown error, for some reason, settings could not be saved.", "error", true);
		}
	}

	/**
	 * Metoda wczytuje plik właściwości albo go odtwarza w razie braku.
	 * @param error boolean - true, jeśli wywołała samą siebie, bo nie dało się wczytać pliku, w takim
	 * 		wypadku podejmowana jest jeszcze TYLKO jedna próba, bez dalszej rekurencji
	 */
	private void readSettingsFile(boolean error) {
		Path path = Paths.get("holmes.cfg");
		String currentLine;
		settings.clear();
		try (Scanner scanner = new Scanner(path)) {
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				settings.add(convertLineToSetting(currentLine));
			}
			
			checkAndRestoreSetting();
			
			GUIManager.getDefaultGUIManager().log("Settings file read:","text", true);
			for(Setting s : settings) {
				GUIManager.getDefaultGUIManager().logNoEnter("ID: " , "bold", false);
				GUIManager.getDefaultGUIManager().logNoEnter(s.getID() , "italic", false);
				GUIManager.getDefaultGUIManager().logNoEnter(" VALUE: " , "bold", false);
				GUIManager.getDefaultGUIManager().log(s.getValue(), "italic", false);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
				"The file \"holmes\".cfg, which normally contains the settings for this application,\n"
				+ "has not been found or contains invalid values. Restoring default file.",
				"Settings file not found or damaged",
				JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Settings file not found or damaged. The file \"holmes\".cfg, which normally "
					+ "contains the settings for this application, has not been found or contains invalid values. Restoring default file.", "error", true);
			
			if(error) 
				return;
			
			checkAndRestoreSetting();
			writeSettingsFile();
		}
	}

	/**
	 * Metoda odpowiedzialna za wczytanie właściwości i jej ID w lini (ID do pierwszej spacji).
	 * @param line String - linia z pliku
	 * @return Setting - właściwość programu
	 */
	private Setting convertLineToSetting(String line) {
		try {
			Random rand = new Random();
			String ID = "";
			String value = "";
			int index = line.indexOf(" ");
			if(index < 1) {
				ID = "Unknown_" +rand.nextInt(999999);
				value = line;
			} else {
				ID = line.substring(0, index);
				value = line.substring(index + 1, line.length());
			}
			
			return new Setting(ID, value);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
					"The file \"holmes\".cfg, which normally contains the settings for this application, is corrupt. Unable to load settings.",
					"Settings corrupt, converting line has failed.",
					JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Settings corrupt! The file \"holmes\".cfg, which normally contains the "
					+ "settings for this application, is corrupt. Unable to load setting line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "italic", false);
			return null;
		}
	}
	
	/**
	 * Metoda wczytująca plik konfiguracyjny.
	 */
	public void loadSettings() {
		readSettingsFile(false);
	}
	
	/**
	 * Metoda zapisująca konfigurację do pliku.
	 */
	public void saveSettings() {
		writeSettingsFile();
	}
}