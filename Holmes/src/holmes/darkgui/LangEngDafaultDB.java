package holmes.darkgui;

import java.util.HashMap;

/**
 * Klasa zawiera domyślne wartości dla języka angielskiego.
 */
public class LangEngDafaultDB {
    public LangEngDafaultDB() {
    }

    public static HashMap<String, String> getDefaultEnglish() {
        HashMap<String, String> defaultEnglish = new HashMap<String, String>();

        defaultEnglish.put("GUIM_toolboxTabName","Toolbox");
        defaultEnglish.put("GUIM_simulatorTabName","Simulator");
        defaultEnglish.put("GUIM_quicksimTabName","QuickSim");
        defaultEnglish.put("GUIM_tinvTabName","T-inv");
        defaultEnglish.put("GUIM_pinvTabName","P-inv");
        defaultEnglish.put("GUIM_mctTabName","MCT");
        defaultEnglish.put("GUIM_mcsTabName","MCS");
        defaultEnglish.put("GUIM_KnockoutTabName","Knockout");
        defaultEnglish.put("GUIM_fixTabName","NetFix");
        defaultEnglish.put("GUIM_clustersTabName","Clusters");
        defaultEnglish.put("GUIM_closingQuestion001","Net or its data have been changed since last save. Exit, save&exit or do not exit now?");
        defaultEnglish.put("GUIM_closeWindowTitle001","Project has been modified");
        defaultEnglish.put("LOGentry00001","Exiting program");
        defaultEnglish.put("GUIM_closingQuestion002","Are you sure you want to close the program?");
        defaultEnglish.put("LOGentry00002","Tools directory does not exist:");
        defaultEnglish.put("LOGentry00003","File COMMAND.ina does not exist or is corrupted.");
        defaultEnglish.put("GUI_inaProblem001","Unable to recreate COMMAND.ina.");
        defaultEnglish.put("GUI_inaProblem002","Error - COMMAND.ina");
        defaultEnglish.put("LOGentry00004","Unable to recreate COMMAND.ina. Invariants generator will work in Holmes mode only.");
        defaultEnglish.put("LOGentry00005","File COMMANDp.ina does not exist or is corrupted.");
        defaultEnglish.put("GUI_inaProblem003","Error - COMMANDp.ina");
        defaultEnglish.put("LOGentry00006","File ina.bat did not exist or was corrupted:");
        defaultEnglish.put("GUI_inaProblem004","Unable to recreate ina.bat. This is a critical error, possible write protection issues in program directory. Invariants generation using INAwin32 will most likely fail.");
        defaultEnglish.put("GUI_inaProblem005","Critical error - writing");
        defaultEnglish.put("LOGentry00007","Critical error, unable to recreate ina.bat file. Invariants generator will not work.");
        defaultEnglish.put("LOGentry00008R","Invalid path (");
        defaultEnglish.put("LOGentry00009R",") to Rscript executable file.");
        defaultEnglish.put("GUI_RscriptProblem001","Manually locate Rscript.exe");
        defaultEnglish.put("GUI_RscriptProblem002","R not installed");
        defaultEnglish.put("GUI_RscriptProblem003","Rscript.exe missing in path");
        defaultEnglish.put("GUI_RscriptProblem004","Missing executable");
        defaultEnglish.put("GUI_RscriptButton001","Select Rscript.exe");
        defaultEnglish.put("GUI_RscriptButton002tip","Please select Rscript exe, usually located in R/Rx.x.x/bin directory.");
        defaultEnglish.put("LOGentry00010R","Rscript executable file inaccessible. Some features (clusters operations) will be disabled.");
        defaultEnglish.put("LOGentry00011R","Rscript.exe manually located in");
        defaultEnglish.put("LOGentry00012R",". Settings file updated.");
        defaultEnglish.put("LOGentry00013R","Rscript.exe location unknown. Clustering procedures will not work.");
        defaultEnglish.put("GUI_simulator0001warn","Warning: simulator active. Cannot proceed until manually stopped.");
        defaultEnglish.put("GUI_simulator0002warn","Warning: XTPN simulator active. Cannot proceed until manually stopped.");
        defaultEnglish.put("exit","Exit");
        defaultEnglish.put("saveAndExit","Save and Exit");
        defaultEnglish.put("cancel","Cancel");
        defaultEnglish.put("fixed","Fixed");
        defaultEnglish.put("netSimWork01","Net simulator working");


        return defaultEnglish;
    }
}
