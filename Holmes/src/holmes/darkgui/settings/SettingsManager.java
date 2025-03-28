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
import holmes.darkgui.LanguageManager;

/**
 * Klasa zarządzająca plikiem konfiguracyjnym programu.
 */
public class SettingsManager {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private ArrayList<Setting> settings;

    /**
     * Konstruktor domyślny obiektu klasy SettingsManager.
     */
    public SettingsManager() {
        settings = new ArrayList<>();
    }

    /**
     * Metoda zwraca wartość związaną z podawanym ID.
     *
     * @param ID <b>String</b> - identyfikator właściwości
     * @return <b>String</b> - właściwość
     */
    public String getValue(String ID) {
        for (Setting s : settings) {
            if (s.getID().equals(ID))
                return s.getValue();
        }
        return null;
    }

    /**
     * Metoda ustawia nową wartość związaną z podawanym ID.
     *
     * @param ID    <b>String</b> - identyfikator właściwości
     * @param value <b>String</b> - nowa wartość właściwości
     * @param save  boolean - jeśli true to od razu zapis do pliku
     * @return int - 0 jeśli znaleziono id, -1 jeśli nie - dodawana jest wtedy od razu NOWA
     * właściwość pod podanym ID
     */
    public int setValue(String ID, String value, boolean save) {
        boolean found = false;
        for (Setting s : settings) {
            if (s.getID().equals(ID)) {
                found = true;
                s.setValue(value);
            }
        }
        if (!found) {
            settings.add(new Setting(ID, value));
            if (save) writeSettingsFile();

            return -1;
        } else {
            if (save) writeSettingsFile();

            return 0;
        }
    }

    /**
     * Metoda dodaje nową właściwość z ID oraz wartością
     * @param ID    <b>String</b> - unikalny ID
     * @param value <b>String</b> - wartość właściwości
     */
    private void addSetting(ArrayList<Setting> settings, String ID, String value) {
        for (Setting s : settings) {
            if (s.getID().equals(ID)) {
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
        settingsNew.add(new Setting("holmes_version", "2.0 (Kim Kitsuragi edition)"));
        //2.0 Tequila Sunset
        
        ArrayList<String> confiredDictionaries = new ArrayList<String>();
        confiredDictionaries.add("English");
        confiredDictionaries.add("Polish");
        confiredDictionaries.add("German");
        confiredDictionaries.add("Ukrainian");
        confiredDictionaries.add("Spanish");
        confiredDictionaries.add("French");
        confiredDictionaries.add("Italian");
        confiredDictionaries.add("YourLang");
        if(!confiredDictionaries.contains(getValue("selected_language"))) {
            settingsNew.add(new Setting("selected_language", "English"));
        } else {
            checkAndFix(settingsNew, "selected_language", getValue("selected_language"));
        }
        
        //checkAndFix(settingsNew, "selected_language", "English");
        checkAndFix(settingsNew, "r_path", "c://Program Files//R//R-3.1.2//bin//Rscript.exe");
        checkAndFix(settingsNew, "r_path64", "c://Program Files//R//R-3.1.2//bin//x64//Rscript.exe");
        checkAndFix(settingsNew, "lastOpenedPath", "");
        checkAndFix(settingsNew, "ina_bat", "START INAwin32.exe COMMAND.ina");
        checkAndFix(settingsNew, "ina_COMMAND1", " 80 4294901760 0 1 :BNNATTFFFFFFFFTFTFFFFFTFFFFFFTTFFFFTFasiec");
        checkAndFix(settingsNew, "ina_COMMAND2", "nnsyp");
        checkAndFix(settingsNew, "ina_COMMAND2p", "nnsnnnfnn");
        checkAndFix(settingsNew, "ina_COMMAND3", "nnnfnn");
        checkAndFix(settingsNew, "ina_COMMAND4", "eqqy");

        //width and height of the main window
        checkAndFix(settingsNew, "mainWindowStartMaximized", "true");
        checkAndFix(settingsNew, "mainWindowWidth", "1500");
        checkAndFix(settingsNew, "mainWindowHeight", "800");

        //program - ogólne
        checkAndFix(settingsNew, "programUseOldSnoopyLoaders", "0");
        checkAndFix(settingsNew, "programAskForRonStartup", "0");
        try {
            String tmp = getValue("programSnoopyLoaderNetExtFactor");
            int test = Integer.parseInt(tmp);
            checkAndFix(settingsNew, "programSnoopyLoaderNetExtFactor", "100");
        } catch (Exception e) {
            settingsNew.add(new Setting("programSnoopyLoaderNetExtFactor", "100"));
        }
        checkAndFix(settingsNew, "programUseSimpleEditor", "0");
        checkAndFix(settingsNew, "programDebugMode", "0");

        //edytor:
        checkAndFix(settingsNew, "editorGridLines", "1");
        checkAndFix(settingsNew, "editorGridAlignWhenSaved", "1");
        checkAndFix(settingsNew, "editorUseSnoopyOffsets", "1");
        try {
            String tmp = getValue("editorGraphArcLineSize");
            int test = Integer.parseInt(tmp);
            if (test < 1 || test > 3)
                throw new Exception();
            checkAndFix(settingsNew, "editorGraphArcLineSize", "1");
        } catch (Exception e) {
            settingsNew.add(new Setting("editorGraphArcLineSize", "1"));
        }

        checkAndFix(settingsNew, "editor3Dview", "0");
        checkAndFix(settingsNew, "editorSnoopyStyleGraphic", "0");
        checkAndFix(settingsNew, "editorSnoopyColors", "0");
        checkAndFix(settingsNew, "editorSnoopyCompatibleMode", "1");
        checkAndFix(settingsNew, "editorShowShortNames", "0");
        checkAndFix(settingsNew, "editorExportCheckAndWarning", "1");
        checkAndFix(settingsNew, "editorPortalLines", "1");
        checkAndFix(settingsNew, "editorNewPortalPlace", "0");

        try {
            String tmp = getValue("editorGraphFontSize");
            int test = Integer.parseInt(tmp);
            if (test < 7 || test > 25)
                throw new Exception();
            checkAndFix(settingsNew, "editorGraphFontSize", "11");
        } catch (Exception e) {
            settingsNew.add(new Setting("editorGraphFontSize", "11"));
        }
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

        checkAndFix(settingsNew, "simXTPNmassAction", "0");
        checkAndFix(settingsNew, "simXTPNreadArcTokens", "0");
        checkAndFix(settingsNew, "simXTPNreadArcDoNotTakeTokens", "1");
        checkAndFix(settingsNew, "simLogEnabled", "0");
        checkAndFix(settingsNew, "editorShortNameLowerIndex", "0");

        try {
            String tmp = getValue("systemUI");
            int test = Integer.parseInt(tmp);
            if (test < 0 || test > 4)
                throw new Exception();
            settingsNew.add(new Setting("systemUI", test + ""));
        } catch (Exception e) {
            settingsNew.add(new Setting("systemUI", "0"));
        }

        settings = new ArrayList<Setting>(settingsNew);
        writeSettingsFile();
    }

    /**
     * Sprawdzamy, czy dane ustawienie istnieje - jeśli tak, dodajemy je do nowej listy, jeśli nie - ustawiamy
     * jego nową wartość na domyślną.
     * @param settings ArrayList[<b>Setting</b>] - nowa tablica ustawień
     * @param ID <b>String</b> - identyfikator ustawienia
     * @param value <b>String</b> - wartość domyślna, jeśli nie udało się wczytać ustawienia z danym ID
     */
    private void checkAndFix(ArrayList<Setting> settings, String ID, String value) {
        if (getValue(ID) == null)
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
                cfgFileWriter.write(data.getID() + " " + data.getValue() + "\n");
            }
            cfgFileWriter.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, lang.getText("SetMan_entry001"), lang.getText("error"),
                    JOptionPane.ERROR_MESSAGE);
            GUIManager.getDefaultGUIManager().log(lang.getText("SetMan_entry001"), "error", true);
        }
    }

    /**
     * Metoda wczytuje plik właściwości albo go odtwarza w razie braku.
     * @param error <b>boolean</b> - true, jeśli wywołała samą siebie, bo nie dało się wczytać pliku, w takim
     *              wypadku podejmowana jest jeszcze TYLKO jedna próba, bez dalszej rekurencji
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

            GUIManager.getDefaultGUIManager().log(lang.getText("SetMan_entry002"), "text", true); //Settings file read:
            for (Setting s : settings) {
                GUIManager.getDefaultGUIManager().logNoEnter("ID: ", "bold", false);
                GUIManager.getDefaultGUIManager().logNoEnter(s.getID(), "italic", false);
                GUIManager.getDefaultGUIManager().logNoEnter(" VALUE: ", "bold", false);
                GUIManager.getDefaultGUIManager().log(s.getValue(), "italic", false);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    lang.getText("SetMan_entry003"), //The file holmes.cfg, which normally contains the settings for this application,\nhas not been found or contains invalid values. Restoring default file.
                    lang.getText("SetMan_entry004"), //Settings file not found or damaged
                    JOptionPane.ERROR_MESSAGE);
            //Settings file not found or damaged. The file holmes.cfg, which normally contains the settings for this application,
            // has not been found or contains invalid values. Restoring default file.
            GUIManager.getDefaultGUIManager().log(lang.getText("SetMan_entry005"), "error", true);

            if (error)
                return;

            checkAndRestoreSetting();
            writeSettingsFile();
        }
    }

    /**
     * Metoda odpowiedzialna za wczytanie właściwości i jej ID w lini (ID do pierwszej spacji).
     *
     * @param line <b>String</b> - linia z pliku
     * @return <b>Setting</b> - właściwość programu
     */
    private Setting convertLineToSetting(String line) {
        try {
            Random rand = new Random();
            String ID;
            String value;
            int index = line.indexOf(" ");
            if (index < 1) {
                ID = "Unknown_" + rand.nextInt(999999);
                value = line;
            } else {
                ID = line.substring(0, index);
                value = line.substring(index + 1, line.length());
            }

            return new Setting(ID, value);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    lang.getText("SetMan_entry006"), //The file holmes.cfg, which normally contains the settings for this application, is corrupt. Unable to load settings.
                    lang.getText("SetMan_entry007"), //Settings corrupt, converting line has failed.
                    JOptionPane.ERROR_MESSAGE);
            //Settings corrupt! The file holmes.cfg, which normally contains the settings for this application, is corrupt. Unable to load setting line: 
            GUIManager.getDefaultGUIManager().log(lang.getText("SetMan_entry008"), "error", true);
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