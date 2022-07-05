package holmes.varia;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet.GlobalFileNetType;
import holmes.petrinet.data.PetriNet.GlobalNetType;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;

public final class Check {
	private static GUIManager overlord = GUIManager.getDefaultGUIManager();
	private Check() {}

	/**
     * Metoda zwraca macierz inwariantów jeśli istnieje i nie jest pusta.
     * @param t_inv boolean - true, jeśli chodzi o t-inwarianty, false dla p-inwariantów
     * @return ArrayList[ArrayList[Integer]] - macierz inwariantów; null - jeśli pusta lub jej nie ma
     */
    public static ArrayList<ArrayList<Integer>> invExists(boolean t_inv) {
    	ArrayList<ArrayList<Integer>> invariants = null;
    	if(t_inv)
    		invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
    	else
    		invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix();
		if(invariants == null || invariants.size() < 1)
    		return null;
    	else
    		return invariants;
    }
    
    /**
     * Metoda zwraca macierz inwariantów jeśli istnieje i nie jest pusta. W przeciwnym wypadku zwraca null i
     * wyświetla odpowiedni komunikat.
     * @param t_inv boolean - true, jeśli chodzi o t-inwarianty, false dla p-inwariantów
     * @return ArrayList[ArrayList[Integer]] - macierz inwariantów; null - jeśli pusta lub jej nie ma
     */
    public static ArrayList<ArrayList<Integer>> invExistsWithWarning(boolean t_inv) {
    	ArrayList<ArrayList<Integer>> invariants = invExists(t_inv);
    	String symbol = "T-";
    	if(!t_inv)
    		symbol = "P-";
		if(invariants == null) {
			JOptionPane.showMessageDialog(null, symbol+"invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return null;
		} else {
			return invariants;
		}
    }
    
    /**
     * Metoda zwraca wektor z informacjami o liczności 5 klas łuków, odpowiednio: NORMAL, READARC, INHIBITOR,
     * RESET, EQUAL i META_ARC
     * @return ArrayList[Integer] - wektor liczności klas łuków, w kolejności: get(0): NORMAL, następnie READARC,
     *  INHIBITOR, RESET, EQUAL, DOUBLE_ARC, META_ARC
     */
    public static ArrayList<Integer> getArcClassCount() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		int normal = 0;
		int readArc = 0;
		int inhibitor = 0;
		int reset = 0;
		int equal = 0;
		int meta = 0;
		int doubleArc = 0;
		int xtpnArc = 0;
		ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();
		for(Arc a : arcs) {
			if(a.getArcType() == TypeOfArc.NORMAL) {
				if(InvariantsTools.isDoubleArc(a) == true) {
					doubleArc++;
				} else
					normal++;
			} else if(a.getArcType() == TypeOfArc.READARC)
				readArc++;
			else if(a.getArcType() == TypeOfArc.INHIBITOR)
				inhibitor++;
			else if(a.getArcType() == TypeOfArc.RESET)
				reset++;
			else if(a.getArcType() == TypeOfArc.EQUAL)
				equal++;
			else if(a.getArcType() == TypeOfArc.META_ARC)
				meta++;
			else if(a.getArcType() == TypeOfArc.XTPN)
				xtpnArc++;
		}
		result.add(normal);
		result.add(readArc);
		result.add(inhibitor);
		result.add(reset);
		result.add(equal);
		result.add(doubleArc);
		result.add(meta);
		result.add(xtpnArc);
		return result;
	}
    
    /**
     * Metoda zwraca wektor z licznością elementów sieci.
     * @return ArrayList[Integer] - zawartość w kolejności: miejsca, tranzycje, tranzycje czasowe, meta-węzły,
     * 		tranzycje funcyjne, tranzycje stochastyczne
     */
    public static ArrayList<Integer> identifiNetElements() {
    	ArrayList<Integer> result = new ArrayList<Integer>();
    	ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
    	
		int places = 0;
		int tTrans = 0;
		int trans = 0;
		int meta = 0;
		int functions = 0;
		int stochastics = 0;
		
		for(Node node : nodes) {
			if (node.getType() == PetriNetElementType.PLACE) {
				places++;
			} else if (node.getType() == PetriNetElementType.TRANSITION ) {
				trans++;
				if (((Transition)node).getTransType() == TransitionType.TPN) {
					tTrans++;
				}
				if (((Transition)node).isFunctional()) {
					functions++;
				}
			} else if (node.getType() == PetriNetElementType.META) {
				meta++;
			}
		}
		
		result.add(places);
		result.add(trans);
		result.add(tTrans);
		result.add(meta);
		result.add(functions);
		result.add(stochastics);
		return result;
	}
    
    /**
     * Metoda zwraca informację czy sieć jest hierarchiczna.
     * @return boolean - true, jeśli jest, false, jeśli nie jest
     */
    public static boolean isHierarchical() {
    	ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
    	for(Node node : nodes) {
    		if (node.getType() == PetriNetElementType.META)
    			return true;
    	}
    	return false;
    }
    
    /**
     * Metoda zwraca liczbowy identyfikator najlepiej pasującego formatu zapisu
     * @return
     */
    @SuppressWarnings("unused")
	public static GlobalNetType getSuggestedNetType() {
    	ArrayList<Integer> netElements = identifiNetElements();
    	ArrayList<Integer> arcClasses = getArcClassCount();
    	
    	int p = netElements.get(0); //miejsce
    	int t = netElements.get(1); //tranzycje
    	int tt = netElements.get(2); //tranzycje czasowa
    	int m = netElements.get(3); //meta-węzły
    	int ft = netElements.get(4); //tranzycje funkcyjna
    	int st = netElements.get(5); //tranzycje stochastyczne
    	
    	int a = arcClasses.get(0); //łuk
    	int ra = arcClasses.get(1); //łuk odczytu
    	int ia = arcClasses.get(2); //łuk blokujący
    	int resA = arcClasses.get(3); //łuk resetujacy
    	int ea = arcClasses.get(4); //łuk równościowy
    	int da = arcClasses.get(5); //łuk podwójny (ukryty łuk odczytu)
    	int ma = arcClasses.get(6); //meta-łuk
    	
    	boolean extMarker = false; 
    	if(ia>0 || resA>0 || ea>0 ) //zwykłe łuki, ale moga być łuki odczytu
    		extMarker = true;
    	
    	if(tt==0 && ft==0) { //klasyczne węzły
    		if(ft > 0) { //funkcyjna
    			if(extMarker) {
        			return GlobalNetType.funcExtPN;
        		} else {
        			return GlobalNetType.funcPN;
        		}
    		} else {
    			if(extMarker) {
        			return GlobalNetType.extPN;
        		} else {
        			return GlobalNetType.PN;
        		}
    		}
    	}
    	
    	if(tt>0 ) { //czasowa
    		if(ft > 0) { //funkcyjna
    			if(extMarker) { //ext
        			return GlobalNetType.timeFuncExtPN;
        		} else {
        			return GlobalNetType.timeFuncPN;
        		}
    		} else {
    			if(extMarker) { //ext
        			return GlobalNetType.timeExtPN;
        		} else {
        			return GlobalNetType.timePN;
        		}
    		}
    	}
    	
    	if(ft>0) {
    		if(extMarker) {
    			return GlobalNetType.funcExtPN;
    		} else {
    			return GlobalNetType.funcPN;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Metoda dla wykrytego typu sieci zwraca sugerowany typ zapisu.
     * @param netType GlobalNetType - typ sieci
     * @return GlobalFileNetType - sugerowany format zapisu
     */
    public static GlobalFileNetType suggestesFileFormat(GlobalNetType netType) {
    	switch (netType) {
    		case PN:
    			return GlobalFileNetType.SPPED;
    		case extPN:
    			return GlobalFileNetType.SPEPT;
    		case timePN:
    			return GlobalFileNetType.SPTPT;
    		case timeExtPN:
    			return GlobalFileNetType.HOLMESPROJECT;
    		case funcPN:
    			return GlobalFileNetType.HOLMESPROJECT;
    		case timeFuncPN:
    			return GlobalFileNetType.HOLMESPROJECT;
    		case funcExtPN:
    			return GlobalFileNetType.HOLMESPROJECT;
    		case timeFuncExtPN:
    			return GlobalFileNetType.HOLMESPROJECT;	
    		case stochasticPN:
    			return GlobalFileNetType.HOLMESPROJECT;	
    		case stochasticFuncPN:
    			return GlobalFileNetType.HOLMESPROJECT;	
    	}
		return null;
    }
    
    public static String getNetName(GlobalNetType netType) {
    	switch (netType) {
		case PN:
			return "Classical Petri Net";
		case extPN:
			return "Extended Petri Net";
		case timePN:
			return "Time(d) Petri Net";
		case timeExtPN:
			return "Extended Time(d) Petri Net (hybrid)";
		case funcPN:
			return "Functional Petri Net";
		case timeFuncPN:
			return "Time(d) Functional Petri Net (hybrid)";
		case funcExtPN:
			return "Extended Functional Petri Net";
		case timeFuncExtPN:
			return "Extended Time(d) Functional Petri Net (VERY hybrid)";
		case stochasticPN:
			return "Stochastic Petri Net";
		case stochasticFuncPN:
			return "Stochastic Functional Petri Net (VERY hybrid)";
	}
	return null;
}
    
    /**
     * Metoda zwraca rozszerzenie na bazie typu formatu sieci.
     * @param netFileFormat GlobalFileNetType - wykryty format sieci
     * @return String - rozszerzenie do zapisu
     */
    public static String getExtension(GlobalFileNetType netFileFormat) {
    	switch(netFileFormat) {
    		case SPEPT:
    			return "SPEPT";
    		case SPPED:
    			return "SPPED";
    		case SPTPT:
    			return "SPTPT";
    		case HOLMESPROJECT:
    			return "project";
    	}
    	return "HOLMESPROJECT";
    }
}
