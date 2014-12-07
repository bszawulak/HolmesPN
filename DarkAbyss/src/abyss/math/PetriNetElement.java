package abyss.math;

import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Wszystkie elementy sieci Petriego, na poziomie logiki programu, s¹ klasami 
 * dziedzicz¹cymi po tej klasie. Zapewnia ona im konieczne elementy wspólne - 
 * generowanie unikalnych (w obrêbie wszystkich elementów, a nie jednej klasy) 
 * numery identyfikacyjne, przechowywanie nazw i komentarzy.
 * @author students
 *
 */
public class PetriNetElement implements Serializable {
	private static final long serialVersionUID = 3428968829261305581L;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAÆ NAZW, DODAWAÆ LUB USUWAÆ PÓL TEJ KLASY
	 * (przestanie byæ mo¿liwe wczytywanie zapisanych proejktów .abyss)
	 */
	public enum PetriNetElementType { ARC, PLACE, TRANSITION, UNKNOWN, TIMETRANSITION }
	@Element
	protected int ID = -1;
	@Element
	private String name = "";
	@Element
	protected String comment = "";
	protected PetriNetElementType petriNetElementType;

	/**
	 * Metoda pozwala pobraæ typ elementu sieci Petriego.
	 * @return PetriNetElementType - obiekt elementu sieci 
	 */
	public PetriNetElementType getType() {
		return this.petriNetElementType;
	}

	/**
	 * Metoda pozwala ustawiæ typ elementu sieci Petriego.
	 * @param petriNetElementType PetriNetElementType - typ elementu sieci Petriego  
	 */
	public void setType(PetriNetElementType petriNetElementType) {
		this.petriNetElementType = petriNetElementType;
	}

	/**
	 * Metoda pozwala pobraæ identyfikator elementu sieci Petriego.
	 * @return int - identyfikator przypisany do tego elementu sieci Petriego
	 */
	public int getID() {
		return this.ID;
	}

	/**
	 * Metoda pozwala ustawiæ identyfikator elementu sieci Petriego.
	 * @param iD int - identyfikator elementu sieci Petriego
	 */
	protected void setID(int iD) {
		this.ID = iD;
	}

	/**
	 * Metoda pozwala pobraæ komentarz do elementu sieci Petriego.
	 * @return String - tekst komentarza do elementu sieci Petriego
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Metoda pozwala ustawiæ komentarz do elementu sieci Petriego.
	 * @param comment String - komentarz do elementu sieci Petriego
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Metoda pozwala pobraæ nazwê elementu sieci Petriego.
	 * @return String - nazwa elementu sieci Petriego
	 */
	public String getName() {
		return name;
	}

	/**
	 * Metoda pozwala ustawiæ nazwê elementu sieci Petriego.
	 * @param name String - nazwa elementu sieci Petriego
	 */
	public void setName(String name) {
		this.name = name;
	}
}
