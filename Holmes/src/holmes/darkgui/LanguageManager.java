package holmes.darkgui;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class LanguageManager {
    private GUIManager overlord;
    private HashMap<String, String> defaultDictionary;
    private HashMap<String, String> availableLanguagesFromCfg;
    private HashMap<String, HashMap<String, String>> loadedLanguages;
    private HashMap<String, String> currentDictionary;

    private boolean languageCorrupted = false;
    private boolean fileCorrupted = false;
    private String selectedLanguage; //= "English";

    public static String newline = System.getProperty("line.separator");

    public LanguageManager() {
        this.overlord = GUIManager.getDefaultGUIManager();
        overlord.log("Holmes language processing started.", "text", true);

        defaultDictionary = LangEngDafaultDB.getDefaultEnglish();
        currentDictionary = defaultDictionary;

        overlord.logNoEnter("Language: ", "bold", false);
        overlord.logNoEnter("default english", "italic", false);
        overlord.logNoEnter(" Entries: ", "bold", false);
        overlord.log(""+defaultDictionary.size(), "italic", false);

        availableLanguagesFromCfg = new HashMap<String, String>();
        loadedLanguages = new HashMap<String, HashMap<String, String>>();

        if(LoadConfigFile() ) { //najpierw wczytaj config z informacją o językach i ich ścieżkach
            //następnie wczytaj pliki językowe:
            for(HashMap.Entry<String, String> entry : availableLanguagesFromCfg.entrySet()) {
                HashMap<String, String> loadedDict = LoadDictionaryFile(entry.getKey(), entry.getValue());
                if(!fileCorrupted) {
                    loadedLanguages.put(entry.getKey(), loadedDict);
                }
            }
            if(loadedLanguages.isEmpty()) {
                selectedLanguage = "English";
                currentDictionary = defaultDictionary;
            }
        }

        CheckDictionaryIntegrity();
    }
    
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * Metoda ustawia język w programie.
     * @param language <b>String</b>, nazwa języka
     */
    public void setLanguage(String language, boolean startup) {
        if(loadedLanguages.containsKey(language)) {
            selectedLanguage = language;
            currentDictionary = loadedLanguages.get(language);
            
            overlord.getSettingsManager().setValue("selected_language", language, !startup);
        } else {
            overlord.log("Language not found: " + language, "error", true);
            selectedLanguage = "English";
            currentDictionary = defaultDictionary;

            overlord.getSettingsManager().setValue("selected_language", "English", !startup);
        }
    }

    /**
     * Metoda zwracająca tekst z bazy danych językowej, jeżeli brak ID, zwraca ze słownika domyślnego
     * @param ID <b>String</b>, identyfikator tekstu
     * @return <b>String</b>, zwraca tekst ze słownika językowego
     */
    public String getText(String ID) {
        if(currentDictionary.containsKey(ID)) {
            String result = currentDictionary.get(ID);
            if(result.contains("\\n")) {
                result = result.replace("\\n", newline);
            }
            return result;
            //return currentDictionary.get(ID);
        } else {
            if(defaultDictionary.containsKey(ID)) {
                return defaultDictionary.get(ID);
            } else {
                overlord.log("Language Manager error, phrase ID: \"" + ID + "\" not found in internal backup dictionary.", "error", true);
                return "-----";
            }
        }
    }

    /**
     * Wczytuje plik language.cfg
     * @return <b>boolean</b>, zwraca true jeżeli wystąpił błąd
     */
    private boolean LoadConfigFile() {
        languageCorrupted = false;
        Path path = Paths.get("language.cfg");
        String currentLine;
        try (Scanner scanner = new Scanner(path)) {
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                //parse by ,
                String[] parts = currentLine.split(",");
                if (parts.length == 2) {
                    availableLanguagesFromCfg.put(parts[0], parts[1].substring(6));
                }
            }

            overlord.log("Language config read: " + availableLanguagesFromCfg.size() + " dictionaries.", "text", false);

            overlord.logNoEnter("Dictionary:", "bold", false);
            overlord.logNoEnter(" | ", "text", false);
            for (HashMap.Entry<String, String> entry : availableLanguagesFromCfg.entrySet()) {
                String key = entry.getKey();
                overlord.logNoEnter(key + " | ", "text", false);
            }
            overlord.log("", "text", false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error reading language config file",
                    "Config error",
                    JOptionPane.ERROR_MESSAGE);
            overlord.log("Error in language config file, reverting to default english.", "error", true);
            languageCorrupted = true;
        }
        return !languageCorrupted;
    }

    /**
     * Wczytuje plik językowy.
     * @param language String - nazwa języka
     * @param dictPath String - ścieżka do pliku językowego
     * @return HashMap<String, String> - zwraca słownik językowy
     */
    private HashMap<String, String> LoadDictionaryFile(String language, String dictPath) {
        HashMap<String, String> dictionary = new HashMap<String, String>();

        Path path = Paths.get(dictPath);
        String currentLine;
        try (Scanner scanner = new Scanner(path)) {
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine();
                if(currentLine.startsWith("//") || currentLine.length() < 5) {
                    continue;
                }
                //String[] parts = currentLine.split(",");
                try {
                    int comma = currentLine.indexOf(",");
                    if(comma == -1) continue;
                    String[] parts = {currentLine.substring(0, comma), currentLine.substring(comma+1)};
                    
                    if(dictionary.containsKey(parts[0])) {
                        overlord.log("Duplicate entry "+ parts[0] +" in language file: " + dictPath, "error", false);
                    } else {
                        dictionary.put(parts[0], parts[1].stripLeading());
                    }

                } catch (Exception e) {
                    overlord.log("Error parsing line (" + dictPath + "): " + currentLine, "text", true);
                }
            }

            overlord.logNoEnter("Loaded: ", "bold", false);
            overlord.logNoEnter(language, "italic", false);
            overlord.logNoEnter(" File: ", "bold", false);
            overlord.logNoEnter(dictPath, "italic", false);
            overlord.logNoEnter(" Entries: ", "bold", false);
            overlord.log(""+dictionary.size(), "italic", false);
        } catch (Exception e) {
            overlord.log("Error in language dictionary file " +dictPath+ "", "error", true);
            fileCorrupted = true;
        }

        return dictionary;
    }

    /**
     * Metoda iteruje po wszystkich tagach defaultDictionary i sprawdza czy są w currentDictionary
     * we wszystkich językach one występują. Wypisuje dla każdego języka brakujące tagi.
     */
    private void CheckDictionaryIntegrity() {
        for(HashMap.Entry<String, HashMap<String, String>> languagesEntry : loadedLanguages.entrySet()) { //dla każdego języka
            HashMap<String, String> language = languagesEntry.getValue();
            String langName = languagesEntry.getKey();
            for(HashMap.Entry<String, String> entry : defaultDictionary.entrySet())  { //dla każdego tagu w defaultDictionary
                String dafultKey = entry.getKey();
                if(!language.containsKey(dafultKey)) { //jeżeli tag nie istnieje w języku
                    overlord.log(langName + " missing tag: " + dafultKey, "warning", false);
                }
            }
        }
    }
}
