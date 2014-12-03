package abyss.math.parser;

import java.io.PrintWriter;

import abyss.darkgui.GUIManager;
import abyss.math.PetriNetData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class AbyssWriter {

	public void write(String sciezka) {
		// Tu kiedys bylo pole testów parserów do XMLa. Polegly 2 z nich
		// (Xstream i simpleXML). Programisto (nieszczsniku) który tu zagladasz,
		// masz lepsze rzeczy do robienia ni¿ babranie sie z nimi, czy z
		// zawartoscia klasy tej.
		
		//tu bylem, Tony Halik

		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("petriNet", PetriNetData.class);

		try {

			String xml = xstream.toXML(GUIManager.getDefaultGUIManager()
					.getWorkspace().getProject().getData());
			if(sciezka.contains(".abyss")) {
				if(sciezka.indexOf(".abyss") == sciezka.length()-6) {
					sciezka = sciezka.replace(".abyss", "");
				}
			}
			PrintWriter zapis = new PrintWriter(sciezka + ".abyss");

			zapis.println(xml);
			zapis.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}
}
