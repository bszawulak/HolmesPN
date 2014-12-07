package abyss.math.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author students - szkielet klasy, nazwa, 4 metody, og�lnie nic :)
 * @author Rince - ca�a reszta, czyli w zasadzie wszystko tutaj
 *
 */
public class NetHandler_Extended extends NetHandler {

	/**
	 * Metoda wykrywaj�ca rozpocz�cie nowego elementu.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 * @param attributes - atrybut elementu
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
	}
	
	/**
	 * Metoda wykrywaj�ca koniec bie��cego. To w niej po wczytaniu elementu i
	 * wszystkich jego w�asno�ci, zostaje uruchomiony k�kretny konstruktor
	 * odpowiedzialny za utworzenie nowego wierzcho�ka, lub �uku.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
	}

	/**
	 * Metoda odczytuj�ca zawarto�� elementu.
	 * @param ch[] - tablica wczytanych znak�w
	 * @param start - indeks pocz�tkowy
	 * @param length - ilo�� wczytanych znak�w
	 */
	public void characters(char ch[], int start, int length) throws SAXException {
		// Wyluskiwanie zawartosci <![CDATA[]]>
	}
	
	/**
	 * Metoda s�u��ca do wy�apywania i ignorowania pustych przestrzeni.
	 * @param ch[] - tablica wczytanych znak�w
	 * @param start - indeks pocz�tkowy
	 * @param length - wielko�� pustej przestrzeni
	 */
	public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException {
	}
}
