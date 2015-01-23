package abyss.settings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;

/**
 * Klasa zarządzająca plikiem konfiguracyjnym programu.
 * @author students - pierwsza podstawowa wersja
 * @author MR - w tej chwili ponad połowa dodatkowego kodu tutaj
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
	 */
	public int setValue(String ID, String value) {
		boolean found= false;
		for(Setting s : settings) {
			if(s.getID().equals(ID)) {
				found = true;
				s.setValue(value);
				return 0;
			}
		}
		if (!found) settings.add(new Setting(ID,value));
		return -1;
	}
	
	/**
	 * Metoda dodaje nową właściwość z ID oraz wartością
	 * @param ID String - unikalny ID
	 * @param value String - wartość właściwości
	 */
	public void addSetting(String ID, String value) {
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
		addSetting("abyss_version","1.25 release 18-1-2015");
		addSetting("r_path","c://Program Files//R//R-3.1.2//bin//Rscript.exe");
		addSetting("r_path64","c://Program Files//R//R-3.1.2//bin//x64//Rscript.exe");
		addSetting("ina_bat","START INAwin32.exe COMMAND.ina");
		addSetting("ina_COMMAND1"," 80 4294901760 0 1 :BNNATTFFFFFFFFTFTFFFFFTFFFFFFTTFFFFTFasiec");
		addSetting("ina_COMMAND2","nnsyp");
		addSetting("ina_COMMAND3","nnnfnn");
		addSetting("ina_COMMAND4","eqqy");
		//
		write();
	}
	
	/**
	 * Metoda sprawdza, czy krytyczne właściwości zostały wczytane, tj. czy są w 
	 * (nieuszkodzonym) pliku właściwości, który właśnie został przeczytany.
	 * @return boolean - true, jeśli wszystkie ważne zostały wczytane
	 */
	private boolean checkCriticalSetting() {
		if(getValue("abyss_version") == null) return false;
		if(getValue("r_path") == null) return false;
		if(getValue("ina_bat") == null) return false;
		if(getValue("ina_COMMAND1") == null) return false;
		if(getValue("ina_COMMAND2") == null) return false;
		if(getValue("ina_COMMAND3") == null) return false;
		if(getValue("ina_COMMAND4") == null) return false;
		return true;
	}

	/**
	 * Metoda odpowiedzialna za zapis właściwości programu do pliku.
	 */
	public void write() {
		try {
			FileOutputStream fileOut = new FileOutputStream("abyss.cfg");
			PrintStream ps = new PrintStream(fileOut);
			for (Setting data : settings) {
				ps.println(data.getID() + " " + data.getValue());
			}
			fileOut.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unknown error",
					"For some reason, settings could not be saved.",
					JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Unknown error, for some reason, settings could not be saved.", "error", true);
		}
	}

	/**
	 * Metoda wczytuje plik właściwości albo go odtwarza w razie braku.
	 * @param err boolean - true, jeśli wywołała samą siebie, bo nie dało się wczytać pliku, w takim
	 * 		wypadku podejmowana jest jeszcze tylko jedna próba bez dalszej rekurencji
	 */
	public void read(boolean err) {
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
			if(!status)
				throw new IOException();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Settings not found!",
				"The file \"abyss\".cfg, which normally contains the settings for this application, has not been found.\n"
				+ "Creating default file.",
				JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Settings not found! The file \"settings\".stg, which normally "
					+ "contains the settings for this application, has not been found. Creating default file.", "error", true);
			
			if(err) return;
			restoreDefaultSetting();
			read(true);
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
			JOptionPane.showMessageDialog(null,"Settings corrupt, converting line has failed.",
					"The file \"abyss\".cfg, which normally contains the settings for this application, is corrupt. Unable to load settings.",
					JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Settings corrupt! The file \"abyss\".cfg, which normally contains the "
					+ "settings for this application, is corrupt. Unable to load setting line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "italic", false);
			return null;
		}
	}
	
	public void loadSettings() {
		read(false);
	}
	
	public void saveSettings() {
		write();
	}
}