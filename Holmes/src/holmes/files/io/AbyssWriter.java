package holmes.files.io;

import java.io.PrintWriter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNetData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Klasa odpowiedzialna za zapis struktury sieci do pliku natywnego programu.
 * Wymaga XStream 1.4.7
 */
public class AbyssWriter {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
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
			PetriNetData dataModule = overlord.getWorkspace().getProject().getDataCore();
			String xml = xstream.toXML(dataModule);
			
			PrintWriter zapis = new PrintWriter(path);
			zapis.println(xml);
			zapis.close();
			overlord.log(lang.getText("LOGentry00155")+" "+path + ".abyss", "text", true);
			overlord.markNetSaved();
			return true;
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			overlord.log(lang.getText("LOGentry00156exception")+" " + e.getMessage(), "error", true);
			return false;
		}
	}
}
