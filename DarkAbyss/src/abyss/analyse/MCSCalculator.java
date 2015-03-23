package abyss.analyse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import abyss.darkgui.GUIManager;
import abyss.math.Transition;
import abyss.windows.AbyssInvariants;
import abyss.windows.AbyssMCS;

/**
 * Klasa obliczająca zbiory MCS (Mimimal Cut Set) według algorytmu z artykułu:
 * "Mimal cut sets in biochemical reaction networks" Steffen Klamt, Ernst Dieter Gilles, Bioinformatics, 2004, 20, pp. 226-234
 * 
 * @author MR
 *
 */
public class MCSCalculator implements Runnable {
    private ArrayList<ArrayList<Integer>> em_obR;
    private ArrayList<Integer> transitions;
    private List<Set<Integer>> mcs;
    private List<Set<Integer>> precutsets;
    private int maxCutSetSize;
    private boolean ready = false;
    private AbyssMCS masterWindow = null;
    
    private int currentStep = 0;
    
    /**
     * Konstruktor klasy MCSCalculator, odpowiedzialny za przygotowanie struktur danych niezbędnych
     * do dalszych obliczeń.
     * @param objR int - ID tranzycji którą należy wyłączyć jak najmniejszym kosztem
     * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
     * @param transitions ArrayList[Transition] - wektor tranzycji
     * @param MAX_CUTSETSIZE int - maksymalny rozmiar dla zbiorów
     * @param mstWindow AbyssMCS - okno generatora
     */
    public MCSCalculator(int objR, ArrayList<ArrayList<Integer>> invariants, 
    		ArrayList<Transition> transitionsList, int MAX_CUTSETSIZE, AbyssMCS mstWindow) {
    	//ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix(); 
    	if(invariants == null || invariants.size() == 0) { //STEP 1: EM obliczono
    		return;
    	} else {
    		em_obR = new ArrayList<ArrayList<Integer>>();
    		transitions = new ArrayList<Integer>();
        	mcs = new ArrayList<>();
            precutsets = new ArrayList<>();
            
            masterWindow = mstWindow;
    	}
    	//ArrayList<Transition> transitionsList = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

    	//STEP 2: zdefiniowana objR (int, argument funkcji MCSCalculator(int objR)
        for (ArrayList<Integer> inv : invariants) {
            if (transInInvariant(inv, objR) == true) {
            	em_obR.add(binaryInv(inv)); //STEP 3
            }
        }
        
        for(Transition t : transitionsList)
        	transitions.add(transitionsList.indexOf(t));  	
        //transitions = getActiveTransitions(em_obR);

        for (int t : transitions) { //STEP 4
            Set<Integer> tSet = new HashSet<Integer>();
            tSet.add(t);
            if (transitionCoverabilityTest(t) == true) 
            	mcs.add(tSet); //jeśli tranzycja t bierze udział w każdym EM
            else
                precutsets.add(tSet); // jeśli nie w każdym
        }
        maxCutSetSize = MAX_CUTSETSIZE;
        
        ready = true;
    }
    
    /**
     * Główna metoda wywoływana niejawnie przy uruchamianiu osobnego wątku dla obliczeń.
     */
	public void run() {
		try {
			logInternal("Searching MCS started.\n",true);
			findMcs();
			if(masterWindow != null) {
				masterWindow.resetMCSGenerator();
				logInternal("MCS list created.\n",true);
				showMCS();
			}
		} catch (Exception e) {
			
		}
	}
	
	private void showMCS() {
		int mcsSize = mcs.size();
		for(int s=0; s<mcsSize; s++) {
			String msg = "Set "+s+ ": [";
			Set<Integer> mcsSet = mcs.get(s);
			for(int el : mcsSet) {
				msg += el+", ";
			}
			msg += "]\n";
			msg = msg.replace(", ]", "]");
			logInternal(msg, false);
		}
	}

	/**
	 * Metoda wysyłająca komunikaty do podokna logów generatora.
	 * @param msg String - tekst do logów
	 * @param date boolean - true, jeśli ma być podany czas komunikatu
	 */
	private void logInternal(String msg, boolean date) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		if(masterWindow != null) {
			if(date == false) {
				masterWindow.accessLogField().append(msg);
			} else {
				masterWindow.accessLogField().append("["+timeStamp+"] "+msg);
			}
		}
	}
    
    /**
     * Metoda zamienia inwariant na wektor binarny - 1 tam gdzie jest wsparcie inwariantu, 0 gdy brak wsparcia
     * @param inv ArrayList[Integer] - inwariant
     * @return ArrayList[Integer] - inwariant binarny
     */
    private ArrayList<Integer> binaryInv(ArrayList<Integer> inv) {
    	ArrayList<Integer> binaryInvariant = new ArrayList<Integer>();
    	for(int i=0; i<inv.size(); i++) {
    		if(inv.get(i) > 0)
    			binaryInvariant.add(1);
    		else
    			binaryInvariant.add(0);
    	}
		return binaryInvariant;
	}

	/**
     * Metoda sprawdza, czy dana tranzycja znajduje się w każdym inwariancie.
     * @param trans int - ID tranzycji
     * @return boolean - true, jeśli jest w każdym, false w przeciwnym wypadku
     */
    private boolean transitionCoverabilityTest(int trans) {
        for (ArrayList<Integer> invariant : em_obR)
            if (transInInvariant(invariant, trans) == false) //if(invariant.contains(trans) == false)	
                return false;
        return true;
    }
    
    /**
     * Główna metoda odpowiedzialna za szukanie MCS.
     * @param MAX_CUTSETSIZE int - do jakiego rozmiaru (uwaga! EKSPLOZJA stanów >6,7)
     * @return List[Set[Integer]] - zbiory MCS
     */
	public List<Set<Integer>> findMcs() {
    	if(ready == false)
    		return null;
    	
        List<Set<Integer>> newPrecutsets = null;
        int k = 1;
        // TODO: a co, jeśli > 1 reakcja już jest w mcs??
        while (++k <= maxCutSetSize) {
        	currentStep = k;
            newPrecutsets = new ArrayList<>();
            
    		System.out.println();
    		System.out.print("Step: "+currentStep);
    		logInternal("Calculating for set size: "+k+": ", false);
    		
            for (int j : transitions) {
            	System.out.print("*");
            	logInternal("*", false);
            		//5.2.1 usuń z listy zbiorów precutsets, te w których występuje j
            	removeSetsContainingTransition2(j);
                	//5.2.2 czarna magia, odsyłam do artykułu
                List<Set<Integer>> temp_precutsets = calculatePreliminaryCutsets(precutsets, j);
                	//5.2.3 usuń wszystkie zbiory z temp_precutsets które zawierają jakikolwiek zbiór z listy mcs:
                removeNonMinimalSets2(temp_precutsets);
                	//5.2.4 zidentyfikuj zbiory MCS i usuń z precutsets
                newPrecutsets.addAll(identifyNewMCSs2(temp_precutsets));
            }
            logInternal("\n", false);
            
            int sizePre = newPrecutsets.size();
            int sizeMCS = mcs.size();

            logInternal("MCS found: "+sizeMCS+" Precutsets list size:"+sizePre+" \n", false);
            
            if (newPrecutsets.isEmpty() == true)
                break;
            else
                precutsets = newPrecutsets;
        }
        return mcs;
    }
    
    /**
     * Metoda usuwa ze zbioru precutsets wszystkie zbiory, które zawierają tranzycję trans.
     * @param trans int - ID tranzycji
     */
    private void removeSetsContainingTransition2(int trans) {
    	int size = precutsets.size();	
    	Set<Integer> set;
    	for(int s=0; s<size; s++) {
    		set = precutsets.get(s);
    		if (set.contains(trans) == true) {
    			precutsets.remove(s);
    			s--;
    			size--;
            }		
    	}
    }
    
    /**
     * Punkt 5.2.2 z artykułu, znajdź wszystkie takie zbiory z precutsets, 
     * @param precutsets
     * @param trans
     * @return
     */
    private List<Set<Integer>> calculatePreliminaryCutsets(List<Set<Integer>> precutsets, int trans) {
        List<Set<Integer>> newPrecutsets = new ArrayList<>();
        for (Set<Integer> precutset : precutsets) {
            if (intersectsAnyInvariantContaining(trans, precutset) == true)
                continue;
            // powyższe trwa tak długo, aż trafimy na precutset który nie ma części wspólnej z pewnym inwariantem
            // do którego należy tranzycja trans
            Set<Integer> newPrecutset = new HashSet<Integer>(precutset);
            newPrecutset.add(trans); //dodaj do takiego zbioru tranzycję
            newPrecutsets.add(newPrecutset);
        }
 
        return newPrecutsets;
    }
    
    /**
     * TODO: W-implementować w powyższą!!!
     * @param transition
     * @param precutset
     * @return
     */
    private boolean intersectsAnyInvariantContaining(int transition, Set<Integer> precutset) {
        for (ArrayList<Integer> invariant : em_obR) { //dla każdego inwariantu z tablicy:
        	if (transInInvariant(invariant, transition) == true && commonSubset(invariant, precutset).isEmpty() == false) {
            	//jeśli inwariant zawiera tranzycję oraz precutset i inwariant mają jakąś część wspólną
                return true;
            }
            // 5.2.2 'intersekcja' to niby 'cover' ?!!! WTH?!
        }
        return false;
    }
    
    /**
     * Metoda zwraca wspólny zbiór inwariantu i precutsets.
     * @param invariant ArrayList[Integer] - invariant
     * @param precutset Set[Integer] - zbiór precutsets
     * @return Set[Integer] - część wspólna
     */
    private Set<Integer> commonSubset(ArrayList<Integer> invariant, Set<Integer> precutset) {
    	Set<Integer> result = new HashSet<Integer>();
    	for(int el : precutset) {
    		if(invariant.get(el) > 0)
    			result.add(el);
    	}
        return result;
    }
	
	/**
	 * Metoda pozostawia tylko te zbiory precutset, które nie są nadzbiorami już znalezionych mcs.
     * @param precutsets List[Set[Integer]] - precutsets
	 */
	private void removeNonMinimalSets2(List<Set<Integer>> precutsets) {
		int size = precutsets.size();
		Set<Integer> precutset;
		for(int s=0; s<size; s++) {
			precutset = precutsets.get(s);
			for(Set<Integer> minimal : mcs) {
                if (precutset.containsAll(minimal)) {
                	precutsets.remove(s);
                	s--;
                	size--;
                    break;
                }
            }
		}
    }
    
	/**
	 * Metoda identyfikuje zbiory mcs i przenosi je do listy wynikowej. Pozostawia cała resztę.
	 * @param precutsets List[Set[Integer]] - zbiór tmp_precutsets
	 * @return List[Set[Integer]] - tmp_precutsets, tylko, że przycięty o mcs
	 */
    private List<Set<Integer>> identifyNewMCSs2(List<Set<Integer>> precutsets) {
    	int size = precutsets.size();
		Set<Integer> precutset;
		for(int s=0; s<size; s++) {
			precutset = precutsets.get(s);
			if (coversAllTInvariants(precutset) == true) {
                mcs.add(precutset);
                precutsets.remove(s);
                s--;
                size--;
			}
		}
		return precutsets;
    }
    
    /**
     * Metoda sprawdza, czy dany zbiór zawiera się w każdym inwariancie.
     * @param set Set[Integer] - zbiór precutset
     * @return boolean - false, jeśli zbiór nie występuje w chociaż jednym inwariancie istotnym dla obj_R
     */
    private boolean coversAllTInvariants(Set<Integer> set) {
        for (ArrayList<Integer> invariant : em_obR) {
            if (commonSubset(invariant, set).isEmpty() == true)
                return false;
        }
        return true;
    }
    
    /**
     * Metoda sprawdza, czy tranzycja o danym ID wchodzi w skład wsparcia inwariantu.
     * @param invariant ArrayList[Integer] - inwariant
     * @param transition int - ID tranzycji
     * @return boolean - true, jeżeli tranzycja wchodzi w skład wsparcia inwariantu, false w
     * 		przeciwnym przypadku
     */
    private boolean transInInvariant(ArrayList<Integer> invariant, int transition) {
    	if(invariant.get(transition) > 0)
    		return true;
    	else
    		return false;
    }
    
    /**
     * Obsolete
     * @param invMatrix
     * @return
     */
    private ArrayList<Integer> getActiveTransitions(ArrayList<ArrayList<Integer>> invMatrix) { 
    	ArrayList<Integer> trans = new ArrayList<Integer>();
    	for(int i=0; i<invMatrix.get(0).size(); i++) {
    		for(int r=0; r<invMatrix.size(); r++) {
    			if(invMatrix.get(r).get(i) > 0) {
    				trans.add(i);
    				break;
    			}
    		}
    	}
    	return trans;
    }
}
