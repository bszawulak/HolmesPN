package abyss.varia;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import abyss.analyse.InvariantsTools;
import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.Arc.TypesOfArcs;

public final class Check {
	private Check() {}

	/**
     * Metoda zwraca macierz inwariantów jeśli istnieje i nie jest pusta.
     * @return ArrayList[ArrayList[Integer]] - macierz inwariantów; null - jeśli pusta lub jej nie ma
     */
    public static ArrayList<ArrayList<Integer>> invExists() {
    	ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix();
		if(invariants == null || invariants.size() < 1)
    		return null;
    	else
    		return invariants;
    }
    
    /**
     * Metoda zwraca macierz inwariantów jeśli istnieje i nie jest pusta. W przeciwnym wypadku zwraca null i
     * wyświetla odpowiedni komunikat.
     * @return ArrayList[ArrayList[Integer]] - macierz inwariantów; null - jeśli pusta lub jej nie ma
     */
    public static ArrayList<ArrayList<Integer>> invExistsWithWarning() {
    	ArrayList<ArrayList<Integer>> invariants = invExists();
		if(invariants == null) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
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
     *  INHIBITOR, RESET, EQUAL, META_ARC
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
		ArrayList<Arc> arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		for(Arc a : arcs) {
			if(a.getArcType() == TypesOfArcs.NORMAL) {
				if(InvariantsTools.isDoubleArc(a) == true) {
					doubleArc++;
				} else
					normal++;
			} else if(a.getArcType() == TypesOfArcs.READARC)
				readArc++;
			else if(a.getArcType() == TypesOfArcs.INHIBITOR)
				inhibitor++;
			else if(a.getArcType() == TypesOfArcs.RESET)
				reset++;
			else if(a.getArcType() == TypesOfArcs.EQUAL)
				equal++;
			else if(a.getArcType() == TypesOfArcs.META_ARC)
				meta++;
		}
		result.add(normal);
		result.add(readArc);
		result.add(inhibitor);
		result.add(reset);
		result.add(equal);
		result.add(doubleArc);
		result.add(meta);
		return result;
	}
}
