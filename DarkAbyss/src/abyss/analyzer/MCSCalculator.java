package abyss.analyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import abyss.darkgui.GUIManager;
import abyss.math.Transition;

public class MCSCalculator {
    private ArrayList<ArrayList<Integer>> em_obR;
    private ArrayList<Integer> transitions;
    private List<Set<Integer>> mcs;
    private List<Set<Integer>> precutsets;
    private boolean ready = false;
    
    public MCSCalculator(int objR) {
    	ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix(); 
    	if(invariants == null || invariants.size() == 0) { //STEP 1: EM obliczono
    		return;
    	} else {
    		em_obR = new ArrayList<ArrayList<Integer>>();
    		transitions = new ArrayList<Integer>();
        	mcs = new ArrayList<>();
            precutsets = new ArrayList<>();
    	}
    	ArrayList<Transition> transitionsList = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
    	for(Transition t : transitionsList)
        	transitions.add(transitionsList.indexOf(t));

    	
        
    	//STEP 2: zdefiniowana objR (int, argument funkcji MCSCalculator(int objR)
        
        for (ArrayList<Integer> inv : invariants) {
            if (transInInvariant(inv, objR) == true) {
            	em_obR.add(binaryInv(inv)); //STEP 3
            }
        }

        for (int t : transitions) { //STEP 4
            Set<Integer> tSet = new HashSet<Integer>();
            tSet.add(t);
            if (transitionCoverabilityTest(t) == true) 
            	mcs.add(tSet); //jeśli tranzycja t bierze udział w każdym EM
            else
                precutsets.add(tSet); // jeśli nie w każdym
        }
        ready = true;
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
    

    public List<Set<Integer>> findMcs(int MAX_CUTSETSIZE) {
    	if(ready == false)
    		return null;
    	
        List<Set<Integer>> newPrecutsets = null;
        int k = 2;
        // TODO: a co, jeśli > 1 reakcja już jest w mcs??
        while (k++ <= MAX_CUTSETSIZE) {
            newPrecutsets = new ArrayList<>();
            
            for (int j : transitions) {
            		//5.2.1 usuń z listy zbiorów precutsets, te w których występuje j
                precutsets = removeSetsContainingTransition(j, precutsets);  
                	//5.2.2 czarna magia, odsyłamy do artykułu
                List<Set<Integer>> temp_precutsets = calculatePreliminaryCutsets(precutsets, j);
                	//5.2.3 usuń wszystkie zbiory z temp_precutsets które zawierają jakikolwiek zbiór z listy mcs:
                temp_precutsets = removeNonMinimalSets(temp_precutsets);
                	//5.2.4
                newPrecutsets.addAll(identifyNewMCSs(temp_precutsets));
            }
            
            if (newPrecutsets.isEmpty() == true)
                break;
            else
                precutsets = newPrecutsets;
        }
        return mcs;
    }

    /**
     * Metoda usuwa ze zbioru precutsets (dany jako argument) wszystkie zbiory, które zawierają tranzycję trans.
     * @param trans int - ID tranzycji
     * @param sets List[Set[Integer]] - zbiór precutsets
     * @return List[Set[Integer]] - nowy zbiór precutsets
     */
    private List<Set<Integer>> removeSetsContainingTransition(int trans, List<Set<Integer>> sets) {
        List<Set<Integer>> result = new ArrayList<>();
        for (Set<Integer> set : sets) {
            if (set.contains(trans) == false) {
            	result.add(set);
            }
        }
        return result;
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
            //if (invariant.contains(transition) && commonSubset(invariant, precutset).isEmpty() == false) {
        	if (transInInvariant(invariant, transition) == true && commonSubset(invariant, precutset).isEmpty() == false) {
            	//jeśli inwariant zawiera tranzycję oraz precutset i inwariant mają jakąś częśc wspólną
                return true;
            }
            //TODO: 5.2.2 intersekcja to niby 'cover' ?!
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
    	
        //result.retainAll(precutset);
        return result;
    }
    
    /**
     * Metoda zwraca tylko te zbiory precutsets (tmp) które nie są nadzbiorami już znalezionych mcs.
     * @param precutsets List[Set[Integer]] - precutsets
     * @return List[Set[Integer]] - jak wyżej, ale już minimalne
     */
	private List<Set<Integer>> removeNonMinimalSets(List<Set<Integer>> precutsets) {
    	List<Set<Integer>> minimalPreCutSets = new ArrayList<>();
        for(Set<Integer> precutset : precutsets) {
            boolean ok = true;
            for(Set<Integer> minimal : mcs) {
                if (precutset.containsAll(minimal)) {
                	ok = false;
                    break;
                }
            }
            if (ok) {
                minimalPreCutSets.add(precutset);
            }
        }
        return minimalPreCutSets;
    }
    
    private List<Set<Integer>> identifyNewMCSs(List<Set<Integer>> minimalPrecutsets) {
        List<Set<Integer>> newPrecutsets = new ArrayList<>();
        for (Set<Integer> precutset : minimalPrecutsets) {
            if (coversAllTInvariants(precutset) == true)
                mcs.add(precutset);
            else
            	newPrecutsets.add(precutset);
        }
        return newPrecutsets;
    }
    
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
}
