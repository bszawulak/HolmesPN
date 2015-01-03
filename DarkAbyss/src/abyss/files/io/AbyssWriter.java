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
	 * G��wna i jedyna metoda, zapisuj�ca plik sieci.
	 * @param path String - �cie�ka do pliku
	 */
	public void write(String path) {
		// Tu kiedys bylo pole test�w parser�w do XMLa. Polegly 2 z nich
		// (Xstream i simpleXML). Programisto (nieszcz�niku) kt�ry tu zagl�dasz,
		// masz lepsze rzeczy do robienia ni� babranie sie z nimi, czy z
		// zawartoscia klasy tej. (student)
		
		//tu by�em, Tony Halik (MR)
		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("petriNet", PetriNetData.class);

		try {

			String xml = xstream.toXML(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getData());
			
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
