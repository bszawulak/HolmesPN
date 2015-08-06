package abyss.petrinet.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa odpowiedzialna za zarządzanie danymi symulacji knockout.
 * 
 * @author MR
 */
public class NetSimulationDataCore implements Serializable {
	private static final long serialVersionUID = -2180386709205258057L;
	private ArrayList<NetSimulationData> referenceSets = new ArrayList<NetSimulationData>();
	private ArrayList<NetSimulationData> knockoutSets = new ArrayList<NetSimulationData>();
	
	public NetSimulationDataCore() {
		
	}
	
	public boolean addNewReferenceSet(NetSimulationData refSet) {
		referenceSets.add(refSet);
		return true;
	}
	
	public ArrayList<NetSimulationData> getReferenceSets() {
		return this.referenceSets;
	}
}
