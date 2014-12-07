package abyss.math.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author students - szkielet klasy, nazwa, 4 metody, ogólnie nic :)
 * @author Rince - ca³a reszta, czyli w zasadzie wszystko tutaj
 *
 */
public class NetHandler_Extended extends NetHandler {

	/**
	 * Metoda wykrywaj¹ca rozpoczêcie nowego elementu.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 * @param attributes - atrybut elementu
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
	}
	
	/**
	 * Metoda wykrywaj¹ca koniec bie¿¹cego. To w niej po wczytaniu elementu i
	 * wszystkich jego w³asnoœci, zostaje uruchomiony k¹kretny konstruktor
	 * odpowiedzialny za utworzenie nowego wierzcho³ka, lub ³uku.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
	}

	/**
	 * Metoda odczytuj¹ca zawartoœæ elementu.
	 * @param ch[] - tablica wczytanych znaków
	 * @param start - indeks pocz¹tkowy
	 * @param length - iloœæ wczytanych znaków
	 */
	public void characters(char ch[], int start, int length) throws SAXException {
		// Wyluskiwanie zawartosci <![CDATA[]]>
	}
	
	/**
	 * Metoda s³u¿¹ca do wy³apywania i ignorowania pustych przestrzeni.
	 * @param ch[] - tablica wczytanych znaków
	 * @param start - indeks pocz¹tkowy
	 * @param length - wielkoœæ pustej przestrzeni
	 */
	public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException {
	}
}
