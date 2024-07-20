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

        //GUIManager:
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

        //GUIOperations:
        defaultEnglish.put("GUIO_openProject001","Reading project file succcessful.");
        defaultEnglish.put("LOGentry00014","Unable to extract net as image. Cannot save to file:");
        defaultEnglish.put("GUIO_messageBox001","File choosing error. Cannot proceed.");
        defaultEnglish.put("LOGentry00015","File choosing error. No extension:");
        defaultEnglish.put("GUIO_warning001","\nWarning: hierarchical net structure detected. Using Holmes project is strongly advised.\nOther save format will most probably fail.\n");
        defaultEnglish.put("GUIO_warning002","Warning. There has been a problem detecting type of the Petri net.\nHolmes project file as save is highly recommended.");
        defaultEnglish.put("GUIO_warning003","Selected net file format:");
        defaultEnglish.put("GUIO_warning003b","Net real type: ");
        defaultEnglish.put("GUIO_warning003c","Suggested file format: ");
        defaultEnglish.put("GUIO_warning003d","Selected format will not contain all features of the current Petri Net. Resulting file\nwill contain reduced net or will be corrupted.\n");
        defaultEnglish.put("GUIO_warning003title","Invalid out net file format");
        defaultEnglish.put("GUIO_button001","Load invariants");
        defaultEnglish.put("GUIO_button001b","Select invariant file");
        defaultEnglish.put("GUIO_inaInv001","Net saving as .pnt file failed. There may be problems with file:");
        defaultEnglish.put("GUIO_inaInv002"," or there is no network yet.");
        defaultEnglish.put("GUIO_inaInv003","Missing net or file");
        defaultEnglish.put("GUIO_inaInv004","INAwin32.exe will now start. This may take a while. Click OK and please wait.\nWhen console shows in, please type Y, then N after invariants are computed.");
        defaultEnglish.put("GUIO_inaInv005","Please wait");
        defaultEnglish.put("LOGentry00016","Activating INAwin32.exe. Please wait, this may take a few seconds due to OS delays.");
        defaultEnglish.put("LOGentry00017","INAwin32.exe process finished. Reading results...");
        defaultEnglish.put("GUIO_inaInv006","I/O operation: activating INAwin32.exe failed.");
        defaultEnglish.put("GUIO_inaInv007","No invariants file - using INAwin32.exe failed.");
        defaultEnglish.put("GUIO_inaInv008","Save .inv file");
        defaultEnglish.put("GUIO_inaInv009","No, thanks");
        defaultEnglish.put("GUIO_inaInv010","Do you want to save generated .inv file?");
        defaultEnglish.put("GUIO_inaInv011","Save the invariants?");
        defaultEnglish.put("GUIO_inaInv012","Select invariants target path");
        defaultEnglish.put("LOGentry00018","Invariants generation successful.");
        defaultEnglish.put("GUIO_inaInv013","Missing executables in the tools directory. Required: INAwin32.exe, ina.bat and COMMAND.ina");
        defaultEnglish.put("GUIO_inaInv014","Missing files");
        defaultEnglish.put("GUIO_invariants001","Invariants generation already in progress.");
        defaultEnglish.put("GUIO_invariants002","Generator working");
        defaultEnglish.put("GUIO_mct001","Exporting net into CSV file failed.");
        defaultEnglish.put("GUIO_mct002","Write error");
        defaultEnglish.put("LOGentry00019","Starting MCT generator.");
        defaultEnglish.put("GUIO_mct003","Select MCT target path");
        defaultEnglish.put("GUIO_mct004","MCT file created");
        defaultEnglish.put("GUIO_mct005","Operation successful.");
        defaultEnglish.put("LOGentry00020","MCT file saved. Path:");
        defaultEnglish.put("GUIO_mct006","MCT file created");
        defaultEnglish.put("GUIO_mct007","File operation failed when creating MCT sets.");
        defaultEnglish.put("GUIO_mct008","MCT generator error");
        defaultEnglish.put("LOGentry00021","MCT generator failed:");
        defaultEnglish.put("LOGentry00022","Warning: unable to check if given clusters number (");
        defaultEnglish.put("LOGentry00023",") exceeds invariants number. If so, the procedure may fail.");
        defaultEnglish.put("GUIO_clusters001","Select CH metric directory");
        defaultEnglish.put("GUIO_clusters002","Use temporary directory");
        defaultEnglish.put("GUIO_clusters003","Multiple CH metric files can we written into default temporary directory (inadvised) or into\nthe selected one.");
        defaultEnglish.put("GUIO_clusters004","Directory selection");
        defaultEnglish.put("GUIO_clusters005","Select CH metric dir");
        defaultEnglish.put("GUIO_clusters006","Target directory for CH metric results");
        defaultEnglish.put("LOGentry00024","CH metric files will be put into the");
        defaultEnglish.put("LOGentry00025","Cluster files will be put into the");
        defaultEnglish.put("LOGentry00026","Cluster files will be put into the");
        defaultEnglish.put("LOGentry00027","Warning: Celinski-Harabasz metric computation in 32bit mode for large number of invariants can cause R/OS failure.");
        defaultEnglish.put("GUIO_clusters007","CH metric computation failed for");
        defaultEnglish.put("GUIO_clusters008"," clusters.\nPath:");
        defaultEnglish.put("LOGentry00028","Warning: unable to check if a given number of clusters (");
        defaultEnglish.put("LOGentry00029",") exceeds invariants number. If so, the procedure may fail.");
        defaultEnglish.put("GUIO_clusters009","Select cluster directory");
        defaultEnglish.put("GUIO_clusters009b","Use temporary directory");
        defaultEnglish.put("GUIO_clusters009c","Cancel operation");
        defaultEnglish.put("GUIO_clusters010","Multiple cluster files can we written into default temporary directory (not advised) or into\nthe selected one.");
        defaultEnglish.put("GUIO_clusters011","Select cluster dir");
        defaultEnglish.put("GUIO_clusters012","Target directory for cluster results");
        defaultEnglish.put("LOGentry00030","Cluster files will be put into the");
        defaultEnglish.put("GUIO_clusters013","Clustering generation failed for");
        defaultEnglish.put("GUIO_clusters014"," clusters.\nPath:");
        defaultEnglish.put("GUIO_csv001","CSV invariants file (.csv)");
        defaultEnglish.put("GUIO_csv002","Select CSV");
        defaultEnglish.put("GUIO_csv003","Select CSV file");
        defaultEnglish.put("GUIO_csv004","Select invariants file manually");
        defaultEnglish.put("GUIO_csv004b","Use computed invariants");
        defaultEnglish.put("LOGentry00031","Please select invariant file (.CSV) for the clustering manually or use invariants\nfrom the current network (they must be computed/loaded already!).");
        defaultEnglish.put("GUIO_inv001","Invariants source");
        defaultEnglish.put("GUIO_inv002","Exporting invariants into CSV file failed.\nCluster procedure cannot begin.");
        defaultEnglish.put("GUIO_inv003","CSV export error");
        defaultEnglish.put("GUIO_clusters015","Manually locate file");
        defaultEnglish.put("GUIO_clusters015b","Cancel procedure");
        defaultEnglish.put("GUIO_clusters016","No input.csv file in:\n");
        defaultEnglish.put("GUIO_clusters017","\nDo you want to select location manually?");
        defaultEnglish.put("GUIO_clusters018","No CSV invariants file");
        defaultEnglish.put("GUIO_clusters019","Select CSV invariants file");
        defaultEnglish.put("GUIO_clusters020","CSV invariants file:");
        defaultEnglish.put("GUIO_clusters021"," located. Starting single clustering procedure.");
        defaultEnglish.put("GUIO_clusters022","Starting MCT generator for file:");
        defaultEnglish.put("GUIO_clusters023","MCT generation (file) failed for:");
        defaultEnglish.put("LOGentry00032","R function failed for parameters:");
        defaultEnglish.put("LOGentry00032b","File name:");
        defaultEnglish.put("LOGentry00032c","Output dir:");
        defaultEnglish.put("LOGentry00032d","Algorithm:");
        defaultEnglish.put("LOGentry00032e","Metric:");
        defaultEnglish.put("LOGentry00032f","No. of clusters:");
        defaultEnglish.put("GUIO_clusters024","Clustering failed. Check log.");
        defaultEnglish.put("LOGentry00033","Unable to update simulator fields: (XTPN=");
        defaultEnglish.put("LOGentry00033b","; stepsValue=");
        defaultEnglish.put("LOGentry00033c","; stepsValue=");
        defaultEnglish.put("GUIO_fix001","Invisible arc: p");
        defaultEnglish.put("GUIO_fix002",". Removing...");
        defaultEnglish.put("GUIO_fix003","Invisible arc: t");
        defaultEnglish.put("GUIO_fix004","Arc list:");
        defaultEnglish.put("GUIO_fix005",", processed arcs:");
        defaultEnglish.put("GUIO_fix006",", removed ghost-arcs:");

        //short:
        defaultEnglish.put("exit","Exit");
        defaultEnglish.put("saveAndExit","Save and Exit");
        defaultEnglish.put("save","Save");
        defaultEnglish.put("select","Select");
        defaultEnglish.put("cancel","Cancel");
        defaultEnglish.put("fixed","Fixed");
        defaultEnglish.put("netSimWork01","Net simulator working");
        defaultEnglish.put("error","Error");
        defaultEnglish.put("critError","Critical error");




        return defaultEnglish;
    }
}
