package holmes.darkgui.settings;

/**
 * Klasa słownikowa, służy do przechowywania właściwości programu w formie par ID/wartość.
 * @author students
 *
 */
public class Setting {
	private String ID;
	private String value;
	
	/**
	 * Konstruktor obiektu klasy Setting.
	 * @param ID String - identyfikator
	 * @param value String - wartość właściwości
	 */
	public Setting(String ID, String value) {
		setID(ID);
		setValue(value);
	}

	/**
	 * Metoda zwraca aktualną wartość właściwości.
	 * @return String - właściwość
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Metoda ustawia nową dla wartość właściwości.
	 * @param value String - właściwość
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Metoda zwraca aktualną wartość identyfikatora.
	 * @return String - identyfikator właściwości
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Metoda ustawia nową dla wartość identyfikatora.
	 * @param iD String - identyfikator
	 */
	public void setID(String iD) {
		ID = iD;
	}
	
	public String toString() {
		if(ID != null && value != null)
			return "ID: "+ID+" VALUE: "+value;
		else
			return "ID: null VALUE: null";
	}
}
