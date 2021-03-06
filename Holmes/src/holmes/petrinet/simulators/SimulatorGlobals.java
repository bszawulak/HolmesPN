package holmes.petrinet.simulators;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.NetSimulator.NetType;

/**
 * Globalne ustawienia symulatora.
 * 
 * @author MR
 *
 */
public class SimulatorGlobals {
	private GUIManager overlord;
	
	private int ARC_STEP_DELAY = 25;
	private int TRANS_FIRING_DELAY = 25;
	
	private boolean maxMode = false;
	private boolean singleMode = false;
	private NetType refNetType = NetType.BASIC;	
	private boolean emptySteps = false; // 0 - bez pustych kroków, 1 - z pustymi krokami
	private int simSteps = 1000;			//liczba kroków dla zbioru referencyjnego
	private int simReps = 100;

	public long currentStep = 0;
	public double currentTime = 0;
	private int simulatorType = 0; // 0 -standard, 1 - SSPN, 2 - Gillespie SSA
	private int generatorType = 0; // 0 - Random (Java), 1 - HighQualityRandomGenerator
	
	private int SPNimmediateMode = 2;
	private boolean SPNdetRemove = true;
	
	private boolean ssaMassActionKineticsEnabled = true;
	
	public boolean quickSimToken = false;
	
	/**
	 * Konstruktor obiektu SimulatorGlobals.
	 */
	public SimulatorGlobals() {
		this.overlord = GUIManager.getDefaultGUIManager();
	}
	
	/**
	 * Metoda ustawia status trybu maximum.
	 * @param value boolean - true, jeśli tryb włączony
	 */
	public void setMaxMode(boolean value) {
		this.maxMode = value;
	}
	
	/**
	 * Metoda zwraca status trybu maximum.
	 * @return boolean - true, jeśli włączony
	 */
	public boolean isMaxMode() {
		return this.maxMode;
	}
	
	/**
	 * Metoda ustawia status trybu pustych kroków symulacji.
	 * @param value boolean - true, jeśli tryb włączony
	 */
	public void setEmptySteps(boolean value) {
		this.emptySteps = value;
	}
	
	/**
	 * Metoda zwraca status trybu pustych kroków symulacji.
	 * @return boolean - true, jeśli włączony - tj. krok bez odpalania tranzycji jest dozwolony
	 */
	public boolean isEmptySteps() {
		return this.emptySteps;
	}
	
	/**
	 * Ustawia tryb pojedynczego odpalania.
	 * @param value boolean - true, jeśli tylko 1 tranzycja ma odpalić na turę. Czy maxMode włączony, to
	 * zależy od wybranej konfiguracji programu. Jeśli wyłączany jest singleMode, zawsze wyłączany jest także
	 * maxMode (ustawiany na false, nie ważne czy wcześniej był włączony czy nie)
	 */
	public void setSingleMode(boolean value) {
		this.singleMode = value;
		//if(value == false)
		//	this.maxMode = false;
		
		if(singleMode == true)
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("simSingleMode").equals("1")) {
				setMaxMode(true);
			}
	}

	/**
	 * Zwraca status trybu pojedynczego odpalania.
	 * @return boolean - true, jeśli tylko 1 tranzycja odpala na turę
	 */
	public boolean isSingleMode() {
		return this.singleMode;
	}
	
	/**
	 * Zwraca aktualnie ustawiony typ sieci.
	 * @return
	 */
	public NetType getNetType() {
		return this.refNetType;
	}
	
	/**
	 * Metoda ustawiająca tryb sieci do symulacji.
	 * @param type int - typ sieci:<br> 0 - PN;<br> 1 - TPN;<br> 2 - Hybrid mode
	 * @return int - faktyczny ustawiony tryb: 0 - PN, 1 - TPN, 2 - Hybrid, -1 : crash mode
	 */
	public int setNetType(int typeID) {
		int res = checkSimulatorNetType(typeID);
		
		switch(res) {
			case 0:
				refNetType = NetType.BASIC;
				break;
			case 1:
				refNetType = NetType.TIME;
				break;
			case 2:
				refNetType = NetType.HYBRID;
				break;
		}
		return res;
	}
	
	public int setNetType(NetType netType) {
		int typeID = 0;
		if(netType == NetType.BASIC)
			typeID = 0;
		else if(netType == NetType.TIME)
			typeID = 1;
		else if(netType == NetType.HYBRID)
			typeID = 2;
				
		int res = checkSimulatorNetType(typeID);
		
		switch(res) {
			case 0:
				refNetType = NetType.BASIC;
				break;
			case 1:
				refNetType = NetType.TIME;
				break;
			case 2:
				refNetType = NetType.HYBRID;
				break;
		}
		
		return res;
	}
	
	/**
	 * Metoda sprawdzająca poprawność trybu sieci do symulacji.
	 * @param type int - typ sieci:<br> 0 - PN;<br> 1 - TPN;<br> 2 - Hybrid mode
	 * @return int - faktyczny ustawiony tryb: 0 - PN, 1 - TPN, 2 - Hybrid
	 */
	public int checkSimulatorNetType(int type) {
		if(type == 0) { //sprawdzenie poprawności trybu, zakładamy że Basic działa zawsze
			return 0;
		} else if(type == 1) {
			ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
			for(Node n : nodes) {
				if(n instanceof Place) { //miejsca ignorujemy
					continue;
				}
				
				if(n instanceof Transition) {
					if(!(((Transition)n).getTransType() == TransitionType.TPN)) {
						JOptionPane.showMessageDialog(null, "Current net is not pure Time Petri Net.\nSimulator switched to hybrid mode.",
								"Invalid mode", JOptionPane.ERROR_MESSAGE);
						GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(2);
						return 2;
					}
				}
			}
			return 1;
		} else if (type == 2) {
			return 2;
		} else if (type == 3) {
			return 3;
		}
		return 1;
	}
	
	/**
	 * Metoda ustawia liczbę kroków symulacji.
	 * @param value int - nowa wartość
	 */
	public void setSimSteps(int value) {
		this.simSteps = value;
	}
	
	/**
	 * Zwraca liczbę kroków symulacji.
	 * @return int - liczba kroków
	 */
	public int getSimSteps() {
		return this.simSteps;
	}
	
	/**
	 * Metoda ustawia liczbę powtórzeń symulacji.
	 * @param value int - nowa wartość
	 */
	public void setRepetitions(int value) {
		this.simReps = value;
	}
	
	/**
	 * Zwraca liczbę powtórzeń symulacji.
	 * @return int - liczba powtórzeń
	 */
	public int getRepetitions() {
		return this.simReps;
	}
	
	/**
	 * Metoda ustawia typ używanego symulatora.
	 * @param type int:<br>
	 * 		0 - standardowy symulator przepływu tokenów<br>
	 * 		1 - SSA (stochastic simulation algorithm)<br>
	 * 		2 - Gillespie SSA
	 */
	public void setSimulatorType(int type) {
		this.simulatorType = type;
	}
	
	/**
	 * Zwraca typ ustawionego symulatora.
	 * @return int - typ symulatora:<br>
	 * 		0 - standardowy symulator przepływu tokenów<br>
	 * 		1 - SSA (stochastic simulation algorithm)<br>
	 * 		2 - Gillespie SSA
	 */
	public int getSimulatorType() {
		return this.simulatorType;
	}
	
	/**
	 * Metoda ustawia typ używanego generatora liczb psudolosowych.
	 * @param type int:<br>
	 * 		0 - Random (Java)<br>
	 * 		1 - HiqhQualityRandomGenerator class<br>
	 */
	public void setGeneratorType(int type) {
		this.generatorType = type;
	}
	
	/**
	 * Zwraca typ używanego generatora liczb psudolosowych.
	 * @return int - typ symulatora:<br>
	 * 		0 - Random (Java)<br>
	 * 		1 - HiqhQualityRandomGenerator class<br>
	 */
	public int getGeneratorType() {
		return this.generatorType;
	}
	
	/**
	 * Metoda ustawia liczbę przystanków na drodze tokenu (grafika)
	 * @param value int - nowa wartość, im mniej (min=5), tym szybciej
	 */
	public void setArcDelay(int value) {
		if(value < 5)
			this.ARC_STEP_DELAY = 5;
		
		this.ARC_STEP_DELAY = value;
	}
	
	/**
	 * Zwraca liczbę przystanków na drodze rysowania tokenu
	 * @return int
	 */
	public int getArcDelay() {
		return ARC_STEP_DELAY;
	}
	
	/**
	 * Metoda ustawia opóźnienie odpalenia tranzycji (grafika)
	 * @param value int - nowa wartość, im mniej (min=10), tym szybciej
	 */
	public void setTransDelay(int value) {
		if(value < 10)
			this.TRANS_FIRING_DELAY = 10;
		
		this.TRANS_FIRING_DELAY = value;
	}
	
	/**
	 * Zwraca wartość opóźnienia tranzycji
	 * @return int
	 */
	public int getTransDelay() {
		return TRANS_FIRING_DELAY;
	}
	
	/**
	 * Reset do ustawień początkowych.
	 */
	public void reset() {
		ARC_STEP_DELAY = 25;
		TRANS_FIRING_DELAY = 25;
		
		maxMode = false;
		singleMode = false;
		refNetType = NetType.BASIC;	
		emptySteps = false;
		simSteps = 1000;
		simReps = 100;

		currentStep = 0;
	}
	
	/**
	 * Zwraca tryb działania tranzycji IMMEDIATE w SPN
	 * @return int: <br>
	 * 		0 - tylko 1, najwyższy priorytet<br>
	 * 		1 - lista aktywnych w danym kroku<br>
	 * 		2 - priotetet = prawdopodobobieństwo
	 */
	public int getSPNimmediateMode() {
		return this.SPNimmediateMode;
	}
	
	/**
	 * Ustawia tryb działania tranzycji IMMEDIATE w SPN
	 * @param type int: <br>
	 * 		0 - tylko 1, najwyższy priorytet<br>
	 * 		1 - lista aktywnych w danym kroku<br>
	 * 		2 - priotetet = prawdopodobobieństwo
	 */
	public void setSPNimmediateMode(int type) {
		if(type < 0 || type > 2)
			type = 2;
		
		this.SPNimmediateMode = type;
	}
	
	public boolean isSPNdetRemoveMode() {
		return this.SPNdetRemove;
	}
	
	public void setSPNdetRemoveMode(boolean mode) {
		this.SPNdetRemove = mode;
	}
	
	/**
	 * Ustawia status flagi mass action kinetics.
	 * @param value boolean - true, jeśli włączone (symulator SSA)
	 */
	public void setSSAmassAction(boolean value) {
		this.ssaMassActionKineticsEnabled = value;
	}
	
	/**
	 * Zwraca status flagi mass action kinetics.
	 *@ return boolean - true, jeśli włączone (symulator SSA)
	 */
	public boolean isSSAMassAction() {
		return this.ssaMassActionKineticsEnabled;
	}
}
