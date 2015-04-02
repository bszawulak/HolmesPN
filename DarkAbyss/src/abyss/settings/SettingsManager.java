package abyss.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;

/**
 * Klasa zarządzająca plikiem konfiguracyjnym programu.
 * @author students - pierwsza podstawowa wersja która nic nie robiła :)
 * @author MR - w tej chwili większość dodatkowego kodu tutaj
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
		if(save)
			writeSettingsFile();
		
		if (found == false) {
			settings.add(new Setting(ID,value));
			return -1;
		}
		else return 0;
	}
	
	/**
	 * Metoda dodaje nową właściwość z ID oraz wartością
	 * @param ID String - unikalny ID
	 * @param value String - wartość właściwości
	 */
	private void addSetting(String ID, String value) {
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
	 * Metoda przywraca domyślne wartości ustawień programu.
	 */
	public void restoreDefaultSetting() {
		settings.clear();
		addSetting("abyss_version","1.30 release 30-3-2015");
		addSetting("r_path","c://Program Files//R//R-3.1.2//bin//Rscript.exe");
		addSetting("r_path64","c://Program Files//R//R-3.1.2//bin//x64//Rscript.exe");
		addSetting("ina_bat","START INAwin32.exe COMMAND.ina");
		addSetting("ina_COMMAND1"," 80 4294901760 0 1 :BNNATTFFFFFFFFTFTFFFFFTFFFFFFTTFFFFTFasiec");
		addSetting("ina_COMMAND2","nnsyp");
		addSetting("ina_COMMAND3","nnnfnn");
		addSetting("ina_COMMAND4","eqqy");
		addSetting("netExtFactor","100");
		addSetting("gridLines","1");
		addSetting("gridAlignWhenSaved","1");
		addSetting("usesSnoopyOffsets","1");
		//
		writeSettingsFile();
	}
	
	/**
	 * Metoda sprawdza, czy krytyczne właściwości zostały wczytane, tj. czy są w 
	 * (nieuszkodzonym) pliku właściwości, który właśnie został przeczytany.
	 * @return boolean - true, jeśli wszystkie ważne zostały wczytane
	 */
	@SuppressWarnings("unused")
	private boolean checkCriticalSetting() {
		String tmp = "";
		if(getValue("abyss_version") == null) return false;
		if(getValue("r_path") == null) return false;
		if(getValue("ina_bat") == null) return false;
		if(getValue("ina_COMMAND1") == null) return false;
		if(getValue("ina_COMMAND2") == null) return false;
		if(getValue("ina_COMMAND3") == null) return false;
		if(getValue("ina_COMMAND4") == null) return false;
		if((tmp = getValue("netExtFactor")) == null) return false;
			else try { int test = Integer.parseInt(tmp); } catch (Exception e) { return false; }

		if(getValue("gridLines") == null) return false;
		if(getValue("gridAlignWhenSaved") == null) return false;
		if(getValue("usesSnoopyOffsets") == null) return false;
		return true;
	}

	/**
	 * Metoda odpowiedzialna za zapis właściwości programu do pliku.
	 */
	private void writeSettingsFile() {
		try {
			File configFile = new File("abyss.cfg");
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
		Path path = Paths.get("abyss.cfg");
		String currentLine;
		settings.clear();
		try (Scanner scanner = new Scanner(path)) {
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				settings.add(convertLineToSetting(currentLine));
			}
			GUIManager.getDefaultGUIManager().log("Settings file read:","text", true);
			for(Setting s : settings) {
				GUIManager.getDefaultGUIManager().logNoEnter("ID: " , "bold", false);
				GUIManager.getDefaultGUIManager().logNoEnter(s.getID() , "italic", false);
				GUIManager.getDefaultGUIManager().logNoEnter(" VALUE: " , "bold", false);
				GUIManager.getDefaultGUIManager().log(s.getValue(), "italic", false);
				//GUIManager.getDefaultGUIManager().log("ID: "+s.getID()+" VALUE: "+s.getValue(),"italic", false);
			}
			
			boolean status = checkCriticalSetting();
			if(!status) {
				throw new IOException();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
				"The file \"abyss\".cfg, which normally contains the settings for this application,\n"
				+ "has not been found or contains invalid values. Restoring default file.",
				"Settings file not found or damaged",
				JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Settings file not found or damaged. The file \"abyss\".cfg, which normally "
					+ "contains the settings for this application, has not been found or contains invalid values. Restoring default file.", "error", true);
			
			if(error) 
				return;
			
			restoreDefaultSetting();
			readSettingsFile(true);
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
					"The file \"abyss\".cfg, which normally contains the settings for this application, is corrupt. Unable to load settings.",
					"Settings corrupt, converting line has failed.",
					JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Settings corrupt! The file \"abyss\".cfg, which normally contains the "
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