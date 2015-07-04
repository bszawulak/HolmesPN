package abyss.math;

import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Wszystkie elementy sieci Petriego, na poziomie logiki programu, są klasami 
 * dziedziczącymi po tej klasie. Zapewnia ona im konieczne elementy wspólne - 
 * generowanie unikalnych (w obrębie wszystkich elementów, a nie jednej klasy) 
 * numery identyfikacyjne, przechowywanie nazw i komentarzy.
 * @author students
 *
 */
public class PetriNetElement implements Serializable {
	//BACKUP: 3428968829261305581L; (nie zmieniać poniższej zmiennej)
	private static final long serialVersionUID = 3428968829261305581L;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAĆ NAZW, DODAWAĆ LUB USUWAĆ PÓL TEJ KLASY
	 * (przestanie być możliwe wczytywanie zapisanych projektów .abyss)
	 */
	public enum PetriNetElementType { ARC, PLACE, TRANSITION, UNKNOWN }
	@Element
	protected int ID = -1;
	@Element
	private String name = "";
	@Element
	protected String comment = "";
	protected PetriNetElementType petriNetElementType;

	/**
	 * Metoda pozwala pobrać typ elementu sieci Petriego.
	 * @return PetriNetElementType - obiekt elementu sieci 
	 */
	public PetriNetElementType getType() {
		return this.petriNetElementType;
	}

	/**
	 * Metoda pozwala ustawić typ elementu sieci Petriego.
	 * @param petriNetElementType PetriNetElementType - typ elementu sieci Petriego  
	 */
	public void setType(PetriNetElementType petriNetElementType) {
		this.petriNetElementType = petriNetElementType;
	}

	/**
	 * Metoda pozwala pobrać identyfikator elementu sieci Petriego.
	 * @return int - identyfikator przypisany do tego elementu sieci Petriego
	 */
	public int getID() {
		return this.ID;
	}

	/**
	 * Metoda pozwala ustawić identyfikator elementu sieci Petriego.
	 * @param iD int - identyfikator elementu sieci Petriego
	 */
	protected void setID(int iD) {
		this.ID = iD;
	}

	/**
	 * Metoda pozwala pobrać komentarz do elementu sieci Petriego.
	 * @return String - tekst komentarza do elementu sieci Petriego
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Metoda pozwala ustawić komentarz do elementu sieci Petriego.
	 * @param comment String - komentarz do elementu sieci Petriego
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Metoda pozwala pobrać nazwę elementu sieci Petriego.
	 * @return String - nazwa elementu sieci Petriego
	 */
	public String getName() {
		return name;
	}

	/**
	 * Metoda pozwala ustawić nazwę elementu sieci Petriego.
	 * @param name String - nazwa elementu sieci Petriego
	 */
	public void setName(String name) {
		if(name.length() == 0)
			return;
		
		this.name = normalizeName(name);
	}
	
	/**
	 * Metoda pomocnicza zapewniająca, że nazwa nie zawiera spacji oraz nie zaczyna się od cyfry.
	 * @param name String - nowa nazwa
	 * @return String - znormalizowana nazwa
	 */
	protected String normalizeName(String name) {
		name = name.replace(" ", "_");
		String letter = name.substring(0, 1);
		if(letter.matches("\\d+"))
			name = "_"+name;
		
		return name;
	}
}
