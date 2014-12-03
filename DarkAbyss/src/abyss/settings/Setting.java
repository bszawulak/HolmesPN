package abyss.settings;

public class Setting {
	private String ID;
	private int value;
	
	public Setting(String ID, int value) {
		setID(ID);
		setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}
}
