package holmes.files.io.snoopy;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author students - szkielet klasy, nazwa, 4 metody, ogólnie nic :)
 * @author MR - cała reszta, czyli w zasadzie też jeszcze nic
 *
 */
public class NetHandler_Colored extends NetHandler {
	/**
	 * Metoda wykrywająca rozpoczęcie nowego elementu.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 * @param attributes - atrybut elementu
	 */
	public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {
	
	}
	
	/**
	 * Metoda wykrywająca koniec bieżącego. To w niej po wczytaniu elementu i
	 * wszystkich jego własności, zostaje uruchomiony konkretny konstruktor
	 * odpowiedzialny za utworzenie nowego wierzchołka, lub łuku.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
	}

	/**
	 * Metoda odczytująca zawartość elementu.
	 * @param ch - tablica wczytanych znaków
	 * @param start - indeks początkowy
	 * @param length - ilość wczytanych znaków
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		// Wyluskiwanie zawartosci <![CDATA[]]>
	}
	
	/**
	 * Metoda służąca do wyłapywania i ignorowania pustych przestrzeni.
	 * @param ch - tablica wczytanych znaków
	 * @param start - indeks początkowy
	 * @param length - wielkość pustej przestrzeni
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}
}
