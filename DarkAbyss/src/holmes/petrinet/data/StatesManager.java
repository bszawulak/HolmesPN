package holmes.petrinet.data;

import holmes.darkgui.GUIManager;

public class StatesManager {
	private GUIManager overlord;
	private PetriNet pn;
	
	public StatesManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
	}
}
