package abyss.unused;

import java.lang.instrument.Instrumentation;

public class ObjectSizeFetcher {
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static long getObjectSize(Object o) { 
    	return instrumentation.getObjectSize(o); 
    }
}

/*
public void saveInvCSV() {
	JFileChooser fc;
	if(lastPath==null)
		fc = new JFileChooser();
	else
		fc = new JFileChooser(lastPath);
	
	FileFilter csvFilter = new ExtensionFileFilter(".csv - Comma Separated Value", new String[] { "CSV" });
	String fileExtension = ".csv";
	fc.setFileFilter(csvFilter);
	fc.addChoosableFileFilter(csvFilter);
	fc.setAcceptAllFileFilterUsed(false);
	int returnVal = fc.showSaveDialog(null);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
		File file = fc.getSelectedFile();
		if(file.getPath().contains(".csv"))
			fileExtension = "";
		workspace.getProject().saveInvariantsToCSV(file.getPath() + fileExtension);
		setLastPath(file.getParentFile().getPath());
	}
}
*/