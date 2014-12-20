package abyss.settings;

/**
 * Klasa s³ownikowa, s³u¿y do przechowywania w³aœciwoœci programu w formie par ID/wartoœæ.
 * @author students
 *
 */
public class Setting {
	private String ID;
	private String value;
	
	/**
	 * Konstruktor obiektu klasy Setting.
	 * @param ID String - identyfikator
	 * @param value String - wartoœæ w³aœciwoœci
	 */
	public Setting(String ID, String value) {
		setID(ID);
		setValue(value);
	}

	/**
	 * Metoda zwraca aktualn¹ wartoœæ w³aœciwoœci.
	 * @return String - w³aœciwoœæ
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Metoda ustawia now¹ dla wartoœæ w³aœciwoœci.
	 * @param value String - w³aœciwoœæ
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Metoda zwraca aktualn¹ wartoœæ identyfikatora.
	 * @return String - identyfikator w³aœciwoœci
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Metoda ustawia now¹ dla wartoœæ identyfikatora.
	 * @param iD String - identyfikator
	 */
	public void setID(String iD) {
		ID = iD;
	}
}
