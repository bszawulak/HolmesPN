package holmes.tables;

/**
 * Klasa kontener dla danych wiersza tabeli inwariantów.
 * @author MR
 *
 */
public class InvariantContainer {
	public int ID = 0;
	public int transNumber = 0;
	public boolean minimal = false;
	public boolean feasible = false;
	public int pureInTransitions = 0;
	public int inTransitions = 0;
	public int outTransitions = 0;
	public int readArcs = 0;
	public int inhibitors = 0;
	
	public boolean sur = false;
	public boolean sub = false;
	public boolean normalInv = false; 
	public boolean canonical = false;
	public String name = "";
	
	public InvariantContainer() {
		
	}
}