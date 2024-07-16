package holmes.windows;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.settings.SettingsManager;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa operacji możliwych do wykonania w ramach okna ustawień programu.
 */
public class HolmesProgramPropertiesActions {
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
		String selectedFile = Tools.selectFileDialog("", filters, "Select Rscript.exe", 
				"Please select Rscript exe, usually located in R/Rx.x.x/bin directory.", "");
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
				GUIManager.getDefaultGUIManager().log("Rscript.exe manually located in "+selectedFile+". Settings file updated.", "text", true);
			
			} else {
				sm.setValue("r_path", "", true);
				GUIManager.getDefaultGUIManager().setRStatus(false);
				GUIManager.getDefaultGUIManager().log("Rscript.exe location unknown. Clustering procedures will not work.", "warning", true);	
			}
		}
	}
}
