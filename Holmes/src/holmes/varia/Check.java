package holmes.varia;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet.GlobalFileNetType;
import holmes.petrinet.data.PetriNet.GlobalNetType;
import holmes.petrinet.elements.*;
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
    	ArrayList<ArrayList<Integer>> invariants;
    	if(t_inv)
    		invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
    	else
    		invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix();
		if(invariants == null || invariants.isEmpty())
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
				if(InvariantsTools.isDoubleArc(a)) {
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
			//else if(a.arcXTPNbox.isXTPN())
			//	xtpnArc++;
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
    public static ArrayList<Integer> identifyNetElements() {
    	ArrayList<Integer> result = new ArrayList<Integer>();
    	ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
    	
		int places = 0;
		int tTrans = 0;
		int trans = 0;
		int meta = 0;
		int functions = 0;
		int stochastics = 0;
		int placesXTPN = 0;
		int transitionsXTPN = 0;
		
		for(Node node : nodes) {
			if (node.getType() == PetriNetElementType.PLACE) {
				places++;
				if( node instanceof PlaceXTPN ) {
					placesXTPN++;
				}
			} else if (node.getType() == PetriNetElementType.TRANSITION ) {
				trans++;
				if (((Transition)node).getTransType() == TransitionType.TPN) {
					tTrans++;
				}
				if (((Transition)node).fpnExtension.isFunctional()) {
					functions++;
				}
				if (((Transition)node).getTransType() == TransitionType.XTPN) {
					transitionsXTPN++;
				}
				if (((Transition)node).getTransType() == TransitionType.SPN) {
					stochastics++;
				}
			} else if (node.getType() == PetriNetElementType.META) {
				meta++;
			}
		}
		
		result.add(places); //0
		result.add(trans); //1
		result.add(tTrans); //2
		result.add(meta); //3
		result.add(functions); //4
		result.add(stochastics); //5
		result.add(placesXTPN); //6
		result.add(transitionsXTPN); //7
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
     * Metoda zwraca liczbowy identyfikator najlepiej pasującego formatu zapisu.
     * @return (<b>GlobalNetType</b>) - typ sieci, PN, TPN, XTPN, SPN, HYBRID
     */
	public static GlobalNetType getSuggestedNetType() {
    	ArrayList<Integer> netElements = identifyNetElements();
    	ArrayList<Integer> arcClasses = getArcClassCount();

		int placesXTPN = netElements.get(6); //miejsca XTPN
		int transitionsXTPN = netElements.get(7); //tranzycje XTPN
		if(placesXTPN > 0 || transitionsXTPN > 0) {
			return GlobalNetType.XTPN; // po prostu, idźcie dalej, Symulator rozpozna swoich.
		}

    	//int places = netElements.get(0); //miejsce
    	//int transitions = netElements.get(1); //tranzycje
    	int timeTransitions = netElements.get(2); //tranzycje czasowa
    	////int metaNodes = netElements.get(3); //meta-węzły
    	int functionalTransitions = netElements.get(4); //tranzycje funkcyjna
    	//int stochasticTransitions = netElements.get(5); //tranzycje stochastyczne

    	//int arc = arcClasses.get(0); //łuk
    	//int readArc = arcClasses.get(1); //łuk odczytu
    	int inhibitorArc = arcClasses.get(2); //łuk blokujący
    	int resetA = arcClasses.get(3); //łuk resetujacy
    	int equalArc = arcClasses.get(4); //łuk równościowy
    	//int doubleArc = arcClasses.get(5); //łuk podwójny (ukryty łuk odczytu)
    	//int metaArc = arcClasses.get(6); //meta-łuk
    	
    	boolean extMarker = false; 
    	if(inhibitorArc>0 || resetA>0 || equalArc>0 ) //zwykłe łuki, ale moga być łuki odczytu
    		extMarker = true;
    	
    	if(timeTransitions==0 && functionalTransitions==0) { //klasyczne węzły
    		if(functionalTransitions > 0) { //funkcyjna
    			if(extMarker) {
        			return GlobalNetType.FPN_extArcs;
        		} else {
        			return GlobalNetType.FPN;
        		}
    		} else {
    			if(extMarker) {
        			return GlobalNetType.PN_extArcs;
        		} else {
        			return GlobalNetType.PN;
        		}
    		}
    	}
    	
    	if(timeTransitions>0 ) { //czasowa
    		if(functionalTransitions > 0) { //funkcyjna
    			if(extMarker) { //ext
        			return GlobalNetType.timeFPN_extArcs;
        		} else {
        			return GlobalNetType.timeFPN;
        		}
    		} else {
    			if(extMarker) { //ext
        			return GlobalNetType.TPN_extArcs;
        		} else {
        			return GlobalNetType.TPN;
        		}
    		}
    	}
    	
    	if(functionalTransitions>0) {
    		if(extMarker) {
    			return GlobalNetType.FPN_extArcs;
    		} else {
    			return GlobalNetType.FPN;
    		}
    	}
    	return null;
    }
    
    /**
     * Metoda dla wykrytego typu sieci zwraca sugerowany typ zapisu.
     * @param netType (<b>GlobalNetType</b>) typ sieci.
     * @return (<b>GlobalNetType</b>) - sugerowany format zapisu.
     */
    public static GlobalFileNetType suggestesFileFormat(GlobalNetType netType) {
		return switch (netType) {
			case PN -> GlobalFileNetType.SPPED;
			case PN_extArcs -> GlobalFileNetType.SPEPT;
			case TPN -> GlobalFileNetType.SPTPT;
			case TPN_extArcs, FPN, timeFPN, FPN_extArcs, timeFPN_extArcs, SPN, functionalSPN -> GlobalFileNetType.HOLMESPROJECT;
			default -> GlobalFileNetType.HOLMESPROJECT;
		};
	}
    
    public static String getNetName(GlobalNetType netType) {
		return switch (netType) {
			case PN -> "Classical Petri Net";
			case PN_extArcs -> "Extended Petri Net";
			case TPN -> "Time(d) Petri Net";
			case TPN_extArcs -> "Extended Time(d) Petri Net (hybrid)";
			case FPN -> "Functional Petri Net";
			case timeFPN -> "Time(d) Functional Petri Net (hybrid)";
			case FPN_extArcs -> "Extended Functional Petri Net";
			case timeFPN_extArcs -> "Extended Time(d) Functional Petri Net (VERY hybrid)";
			case SPN -> "Stochastic Petri Net";
			case functionalSPN -> "Stochastic Functional Petri Net (VERY hybrid)";
			default -> null;
		};
	}
    
    /**
     * Metoda zwraca rozszerzenie na bazie typu formatu sieci.
     * @param netFileFormat (<b>GlobalFileNetType</b>) wykryty format sieci.
     * @return (<b>String</b>) - rozszerzenie do zapisu.
     */
    public static String getExtension(GlobalFileNetType netFileFormat) {
		return switch (netFileFormat) {
			case SPEPT -> "SPEPT";
			case SPPED -> "SPPED";
			case SPTPT -> "SPTPT";
			case HOLMESPROJECT -> "project";
		};
	}
}
