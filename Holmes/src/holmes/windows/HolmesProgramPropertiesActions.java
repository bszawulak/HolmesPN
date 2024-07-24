package holmes.windows;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.darkgui.settings.SettingsManager;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa operacji możliwych do wykonania w ramach okna ustawień programu.
 */
public class HolmesProgramPropertiesActions {
	private static GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static LanguageManager lang = GUIManager.getLanguageManager();
	SettingsManager sm;
	
	/**
	 * Konstruktor obiektu klasy HolmesProgramPropertiesActions.
	 */
	public HolmesProgramPropertiesActions(SettingsManager sm) {
		this.sm = sm;
	}

	/**
	 * Metoda ustawia ścieżkę dostępu do środowiska R.
	 */
	public void setRPath() {
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Rscript.exe (.exe)",  new String[] { "EXE" });
		String selectedFile = Tools.selectFileDialog("", filters, lang.getText("HPPAwin_entry001"), 
				lang.getText("HPPAwin_entry001t"), "");
		if(selectedFile.isEmpty()) {
			return;
		} else {
			if(!selectedFile.contains("x64")) { //jeśli nie wskazano 64bitowej wersji
				String dest = selectedFile.substring(0,selectedFile.lastIndexOf(File.separator));
				dest += "\\x64\\Rscript.exe";
				if(Tools.ifExist(dest))
					sm.setValue("r_path64", dest, true);
				else
					sm.setValue("r_path64", "", true);
			} else {
				sm.setValue("r_path64", selectedFile, true);
			}
			
			if(Tools.ifExist(selectedFile)) {
				sm.setValue("r_path", selectedFile, true);
				GUIManager.getDefaultGUIManager().setRStatus(true);
				String strB = String.format(lang.getText("HPPAwin_entry002"), selectedFile);
				GUIManager.getDefaultGUIManager().log(strB, "text", true);
			
			} else {
				sm.setValue("r_path", "", true);
				GUIManager.getDefaultGUIManager().setRStatus(false);
				GUIManager.getDefaultGUIManager().log(lang.getText("HPPAwin_entry003"), "warning", true);	
			}
		}
	}
}
