package abyss.files.io;

import java.io.PrintWriter;

import abyss.darkgui.GUIManager;
import abyss.math.PetriNetData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class AbyssWriter {

	public void write(String path) {
		// Tu kiedys bylo pole testów parserów do XMLa. Polegly 2 z nich
		// (Xstream i simpleXML). Programisto (nieszczsniku) który tu zagladasz,
		// masz lepsze rzeczy do robienia ni¿ babranie sie z nimi, czy z
		// zawartoscia klasy tej.
		
		//tu bylem, Tony Halik
		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("petriNet", PetriNetData.class);

		try {

			String xml = xstream.toXML(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getData());
			if(path.contains(".abyss")) {
				if(path.indexOf(".abyss") == path.length()-6) {
					path = path.replace(".abyss", "");
				}
			}
			PrintWriter zapis = new PrintWriter(path + ".abyss");

			zapis.println(xml);
			zapis.close();
			GUIManager.getDefaultGUIManager().log("Network save into file "+path + ".abyss", "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}
	}
}
