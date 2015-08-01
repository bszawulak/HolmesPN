package abyss.petrinet.simulators;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Transition.TransitionType;

/**
 * Globalne ustawienia symulatora.
 * 
 * @author MR
 *
 */
public class SimulatorGlobals {
	private int ARC_STEP_DELAY = 25;
	private int TRANS_FIRING_DELAY = 25;
	public long currentStep = 0;
	private GUIManager overlord;
	
	/**
	 * Konstruktor obiektu SimulatorGlobals.
	 */
	public SimulatorGlobals(GUIManager mastah) {
		this.overlord = mastah;
	}
	
	/**
	 * Metoda ustawiająca tryb sieci do symulacji.
	 * @param type int - typ sieci:<br> 0 - PN;<br> 1 - TPN;<br> 2 - Hybrid mode
	 * @return int - faktyczny ustawiony tryb: 0 - PN, 1 - TPN, 2 - Hybrid, -1 : crash mode
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
		}
		return -1;
	}
	
	public void setArcDelay(int value) {
		if(value < 5)
			this.ARC_STEP_DELAY = 5;
		
		this.ARC_STEP_DELAY = value;
	}
	
	public int getArcDelay() {
		return ARC_STEP_DELAY;
	}
	
	public void setTransDelay(int value) {
		if(value < 10)
			this.TRANS_FIRING_DELAY = 10;
		
		this.TRANS_FIRING_DELAY = value;
	}
	
	public int getTransDelay() {
		return TRANS_FIRING_DELAY;
	}
}
