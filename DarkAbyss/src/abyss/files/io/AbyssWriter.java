package abyss.files.io;

import java.io.PrintWriter;

import abyss.darkgui.GUIManager;
import abyss.math.PetriNetData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Klasa odpowiedzialna za zapis struktury sieci do pliku natywnego programu.
 * @author students
 *
 */
public class AbyssWriter {

	/**
	 * Główna i jedyna metoda, zapisująca plik sieci.
	 * @param path String - ścieżka do pliku
	 */
	public void write(String path) {
		// Tu kiedys bylo pole testów parserów do XMLa. Poległy 2 z nich
		// (Xstream i simpleXML). Programisto (nieszczęśniku) który tu zaglądasz,
		// masz lepsze rzeczy do robienia niż babranie sie z nimi, czy z
		// zawartoscia klasy tej. (student)
		
		//tu byłem, Tony Halik (MR)
		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("petriNet", PetriNetData.class);

		try {
			PetriNetData dataModule = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getData();
			String xml = xstream.toXML(dataModule);
			
			/*
			if(path.contains(".abyss")) {
				if(path.indexOf(".abyss") == path.length()-6) {
					path = path.replace(".abyss", "");
				}
			}
			PrintWriter zapis = new PrintWriter(path + ".abyss");
*/
			PrintWriter zapis = new PrintWriter(path);
			zapis.println(xml);
			zapis.close();
			GUIManager.getDefaultGUIManager().log("Network has been saved to file: "+path + ".abyss", "text", true);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}
	}
}
