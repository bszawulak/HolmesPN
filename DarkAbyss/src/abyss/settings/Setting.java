package abyss.settings;

/**
 * Klasa s�ownikowa, s�u�y do przechowywania w�a�ciwo�ci programu w formie par ID/warto��.
 * @author students
 *
 */
public class Setting {
	private String ID;
	private String value;
	
	/**
	 * Konstruktor obiektu klasy Setting.
	 * @param ID String - identyfikator
	 * @param value String - warto�� w�a�ciwo�ci
	 */
	public Setting(String ID, String value) {
		setID(ID);
		setValue(value);
	}

	/**
	 * Metoda zwraca aktualn� warto�� w�a�ciwo�ci.
	 * @return String - w�a�ciwo��
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Metoda ustawia now� dla warto�� w�a�ciwo�ci.
	 * @param value String - w�a�ciwo��
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Metoda zwraca aktualn� warto�� identyfikatora.
	 * @return String - identyfikator w�a�ciwo�ci
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Metoda ustawia now� dla warto�� identyfikatora.
	 * @param iD String - identyfikator
	 */
	public void setID(String iD) {
		ID = iD;
	}
}
