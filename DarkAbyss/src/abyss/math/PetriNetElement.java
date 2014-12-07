package abyss.math;

import java.io.Serializable;

import org.simpleframework.xml.Element;

/**
 * Wszystkie elementy sieci Petriego, na poziomie logiki programu, s� klasami 
 * dziedzicz�cymi po tej klasie. Zapewnia ona im konieczne elementy wsp�lne - 
 * generowanie unikalnych (w obr�bie wszystkich element�w, a nie jednej klasy) 
 * numery identyfikacyjne, przechowywanie nazw i komentarzy.
 * @author students
 *
 */
public class PetriNetElement implements Serializable {
	private static final long serialVersionUID = 3428968829261305581L;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIA� NAZW, DODAWA� LUB USUWA� P�L TEJ KLASY
	 * (przestanie by� mo�liwe wczytywanie zapisanych proejkt�w .abyss)
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
	 * Metoda pozwala pobra� typ elementu sieci Petriego.
	 * @return PetriNetElementType - obiekt elementu sieci 
	 */
	public PetriNetElementType getType() {
		return this.petriNetElementType;
	}

	/**
	 * Metoda pozwala ustawi� typ elementu sieci Petriego.
	 * @param petriNetElementType PetriNetElementType - typ elementu sieci Petriego  
	 */
	public void setType(PetriNetElementType petriNetElementType) {
		this.petriNetElementType = petriNetElementType;
	}

	/**
	 * Metoda pozwala pobra� identyfikator elementu sieci Petriego.
	 * @return int - identyfikator przypisany do tego elementu sieci Petriego
	 */
	public int getID() {
		return this.ID;
	}

	/**
	 * Metoda pozwala ustawi� identyfikator elementu sieci Petriego.
	 * @param iD int - identyfikator elementu sieci Petriego
	 */
	protected void setID(int iD) {
		this.ID = iD;
	}

	/**
	 * Metoda pozwala pobra� komentarz do elementu sieci Petriego.
	 * @return String - tekst komentarza do elementu sieci Petriego
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Metoda pozwala ustawi� komentarz do elementu sieci Petriego.
	 * @param comment String - komentarz do elementu sieci Petriego
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Metoda pozwala pobra� nazw� elementu sieci Petriego.
	 * @return String - nazwa elementu sieci Petriego
	 */
	public String getName() {
		return name;
	}

	/**
	 * Metoda pozwala ustawi� nazw� elementu sieci Petriego.
	 * @param name String - nazwa elementu sieci Petriego
	 */
	public void setName(String name) {
		this.name = name;
	}
}
