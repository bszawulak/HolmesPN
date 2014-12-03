package abyss.math;

import java.io.Serializable;

import org.simpleframework.xml.Element;

public class PetriNetElement implements Serializable {
	private static final long serialVersionUID = 3428968829261305581L;

	public enum PetriNetElementType {
		ARC, PLACE, TRANSITION, UNKNOWN, TIMETRANSITION
	}

	@Element
	protected int ID = -1;
	@Element
	private String name = "";
	@Element
	protected String comment = "";
	protected PetriNetElementType petriNetElementType;

	public PetriNetElementType getType() {
		return this.petriNetElementType;
	}

	public void setType(PetriNetElementType petriNetElementType) {
		this.petriNetElementType = petriNetElementType;
	}

	public int getID() {
		return this.ID;
	}

	protected void setID(int iD) {
		this.ID = iD;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
