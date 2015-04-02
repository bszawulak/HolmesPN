package abyss.windows;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.settings.SettingsManager;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa operacji możliwych do wykonania w ramach okna ustawień programu.
 * @author MR
 *
 */
public class AbyssProgramPropertiesActions {
	SettingsManager sm;
	
	/**
	 * Konstruktor obiektu klasy AbyssProgramPropertiesActions.
	 */
	public AbyssProgramPropertiesActions(SettingsManager sm) {
		this.sm = sm;
	}

	/**
	 * Metoda ustawia ścieżkę dostępu do środowiska R.
	 */
	public void setRPath() {
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Rscript.exe (.exe)",  new String[] { "EXE" });
		String selectedFile = Tools.selectFileDialog("", filters, "Select Rscript.exe", 
				"Please select Rscript exe, usually located in R/Rx.x.x/bin directory.");
		if(selectedFile.equals("")) {
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
