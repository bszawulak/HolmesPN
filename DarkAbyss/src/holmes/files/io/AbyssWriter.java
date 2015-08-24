package holmes.files.io;

import java.io.PrintWriter;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNetData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Klasa odpowiedzialna za zapis struktury sieci do pliku natywnego programu.
 * Wymaga XStream 1.4.7
 * @author students
 *
 */
public class AbyssWriter {

	/**
	 * Główna i jedyna metoda, zapisująca plik sieci.
	 * @param path String - ścieżka do pliku
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean write(String path) {
		// Tu kiedys bylo pole testów parserów do XMLa. Poległy 2 z nich
		// (Xstream i simpleXML). Programisto (nieszczęśniku) który tu zaglądasz,
		// masz lepsze rzeczy do robienia niż babranie sie z nimi, czy z
		// zawartoscia klasy tej. (student)
		
		//tu byłem, Tony Halik (MR)
		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("petriNet", PetriNetData.class);

		try {
			PetriNetData dataModule = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getDataCore();
			String xml = xstream.toXML(dataModule);
			
			PrintWriter zapis = new PrintWriter(path);
			zapis.println(xml);
			zapis.close();
			GUIManager.getDefaultGUIManager().log("Network has been saved to file: "+path + ".abyss", "text", true);
			GUIManager.getDefaultGUIManager().markNetSaved();
			return true;
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
			return false;
		}
	}
}
