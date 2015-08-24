package holmes.petrinet.data;

import java.util.ArrayList;

/**
 * Klasa zarządzająca stanem sieci klasycznej, tj. liczbą tokenów w miejsach.
 * 
 * @author MR
 *
 */
public class PlacesStateVector {
	private ArrayList<Double> stateVector;
	
	public PlacesStateVector() {
		stateVector = new ArrayList<>();
	}
	
	public void addPlace(double value) {
		stateVector.add(value);
	}
	
	
	public boolean removePlace(int index) {
		if(index >= stateVector.size())
			return false;
		
		stateVector.remove(index);
		
		return true;
	}
	
	public int getSize() {
		return stateVector.size();
	}
	
	public ArrayList<Double> accessVector() {
		return this.stateVector;
	}
}
