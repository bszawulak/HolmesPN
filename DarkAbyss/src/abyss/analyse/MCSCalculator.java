package abyss.analyse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import abyss.darkgui.GUIManager;
import abyss.math.MCSDataMatrix;
import abyss.math.Node;
import abyss.math.Transition;
import abyss.windows.AbyssMCS;

/**
 * Klasa obliczająca zbiory MCS (Mimimal Cut Set) według algorytmu z artykułu:
 * 
 * "Mimal cut sets in biochemical reaction networks" 
 * Steffen Klamt, Ernst Dieter Gilles
 * Bioinformatics, 2004, 20, pp. 226-234
 * 
 * @author MR
 *
 */
public class MCSCalculator implements Runnable {
    private ArrayList<ArrayList<Integer>> em_obR;
    private ArrayList<Integer> em_obRinvID;
    
    private ArrayList<Integer> transitions;
    private ArrayList<Set<Integer>> mcs;
    private List<Set<Integer>> precutsets;
    private int maxCutSetSize;
    private boolean ready = false;
    private boolean askBeforeAdd = true;
    private AbyssMCS masterWindow = null;
    private int objective_Reaction;
    private int maxSetsNumber;
    private boolean terminate = false;
     
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
    		ArrayList<Transition> transitionsList, int MAX_CUTSETSIZE, int maxNumber, AbyssMCS mstWindow, boolean safe) {
    	//ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix(); 
    	if(invariants == null || invariants.size() == 0) { //STEP 1: EM obliczono
    		return;
    	} else {
    		em_obR = new ArrayList<ArrayList<Integer>>();
    		transitions = new ArrayList<Integer>();
        	mcs = new ArrayList<>();
            precutsets = new ArrayList<>();
            objective_Reaction = objR;
            masterWindow = mstWindow;
            askBeforeAdd = safe;
            maxSetsNumber = maxNumber;
    	}
    	//ArrayList<Transition> transitionsList = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

    	//STEP 2: zdefiniowana objR (int, argument funkcji MCSCalculator(int objR)
    	em_obRinvID = new ArrayList<Integer>();
    	int counter = 0;
        for (ArrayList<Integer> inv : invariants) {
            if (transInInvariant(inv, objective_Reaction) == true) {
            	em_obR.add(binaryInv(inv)); //STEP 3
            	em_obRinvID.add(counter);
            }
            counter++;
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
			ArrayList<Set<Integer>> results = findMcs();
			addNewDataVector(results);
			if(masterWindow != null) {
				logInternal("MCS list created.\n",true);
				showMCS();
				masterWindow.resetMCSGenerator();
			}
		} catch (OutOfMemoryError e) { // pray...
			precutsets = null;
			GUIManager.getDefaultGUIManager().log("Catastrophic error: out of memory. JRE will become unstable.\n"
					+ "Please save your work immediatelly in *separate* files and restart the program."
					+ " Operation terminated unconditionally.", "error", true);
			addNewDataVector(mcs);
			showMCS();
			masterWindow.resetMCSGenerator();
		} catch (Exception e) {
			logInternal("Operation malfuntion. Unknown error. \n", true);
			addNewDataVector(mcs);
			showMCS();
			masterWindow.resetMCSGenerator();
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
	public ArrayList<Set<Integer>> findMcs() {
    	if(ready == false)
    		return null;
    	
        List<Set<Integer>> newPrecutsets = null;
        int k = 1;
        while (++k <= maxCutSetSize) {
            newPrecutsets = new ArrayList<>();
            
    		//System.out.println();
    		//System.out.print("Step: "+currentStep);
    		logInternal("Calculating for set size: "+k+": ", false);
    		
            for (int j : transitions) {
            	
            	
            	if(j==27) {
            		@SuppressWarnings("unused")
            		String tName = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(j).getName();
            		@SuppressWarnings("unused")
					int breakPoint = 1;	
            	}
            	
            	//System.out.print("*");
            	logInternal("*", false);
            	if(terminate) return mcs;
            		//5.2.1 usuń z listy zbiorów precutsets, te w których występuje j
            	removeSetsContainingTransition2(j); 
            	if(terminate) return mcs;
            		//5.2.2 czarna magia, odsyłam do artykułu
                List<Set<Integer>> temp_precutsets = calculatePreliminaryCutsets(precutsets, j);
                if(terminate) return mcs;
                	//5.2.3 usuń wszystkie zbiory z temp_precutsets które zawierają jakikolwiek zbiór z listy mcs:
                removeNonMinimalSets(temp_precutsets);
                if(terminate) return mcs;
                	//5.2.4 zidentyfikuj zbiory MCS i usuń z precutsets
                newPrecutsets.addAll(identifyNewMCSs2(temp_precutsets));
                
                temp_precutsets = null;
            }
            logInternal("\n", false);
            
            int sizePre = newPrecutsets.size();
            int sizeMCS = mcs.size();

            logInternal("MCS found: "+sizeMCS+" Precutsets list size:"+sizePre+" \n", false);
            
            if (newPrecutsets.isEmpty() == true || maxSetsNumber < mcs.size())
                break;
            else
                precutsets = newPrecutsets;
        }
        return mcs;
    }
	
	/**
	 * II algorytm - under construction
	 * @return
	 */
	public HashSet<HashSet<Integer>> findMcs2() {
		HashSet<HashSet<Integer>> result = new HashSet<HashSet<Integer>>();
		int invMatrixSize = em_obR.size();
		int invSize = transitions.size();
		
		int maxSize = 6;
		maxSize =invSize;
		
		for(int i=0; i<invMatrixSize; i++) {
			for(int j=0; j<invSize; j++) {
				int val = em_obR.get(i).get(j);
				if(val != 0) {
					HashSet<Integer> newSet = new HashSet<Integer>();
					
					newSet.add(j);
					result.add(newSet); //duplikatów nie doda
					
					//int resSize = result.size();
					for(HashSet<Integer> test : result) {
						if(test.size() <= maxSize) {
							test.add(j); //duplikatów i tak nie doda
						}
					}	
				}
			}
		}
		HashSet<HashSet<Integer>> minimal = new HashSet<HashSet<Integer>>(); 
		
		boolean found = false;
		for(HashSet<Integer> test : result) {
			found = false;
			for(HashSet<Integer> ref : result) {
				if(test.equals(ref) == false) {
					if(test.containsAll(ref)) {
						found = true;
						break;
					}
					
				}
			}
			if(found == false) {
				minimal.add(test);
			}
		}
		return minimal;
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
        int invNumber = -1;
        int invID = 0;
        for (Set<Integer> precutset : precutsets) {
	        if(terminate) return newPrecutsets;
	        
        	boolean correct = false;
        	invNumber = -1;
        	for (ArrayList<Integer> invariant : em_obR) { //dla każdego inwariantu z tablicy:
        		invNumber++;
        		invID = em_obRinvID.get(invNumber);
        		
        		if (transInInvariant(invariant, trans) == true) {
        			if(commonSubset(invariant, precutset).isEmpty() == true) {
        				correct = true;
                        break;
        			} else {
        				continue;
        			}
        		} else {
        			continue;
        		}
                	//jeśli inwariant zawiera tranzycję oraz precutset i inwariant mają jakąś część wspólną
                    
            	
        		/*
            	if (transInInvariant(invariant, trans) == true && commonSubset(invariant, precutset).isEmpty() == false) {
                	//jeśli inwariant zawiera tranzycję oraz precutset i inwariant mają jakąś część wspólną
                    incorrect = true;
                    break;
                }// 5.2.2 'intersekcja' to niby 'cover' ?!!! WTH?!
            	*/
            }
        	//if(incorrect == true)
        		//continue;
        	if(!correct) continue;
        	
            // powyższe trwa tak długo, aż trafimy na precutset który nie ma części wspólnej z pewnym inwariantem
            // do którego należy tranzycja trans
            Set<Integer> newPrecutset = new HashSet<Integer>(precutset);
            newPrecutset.add(trans); //dodaj do takiego zbioru tranzycję
            newPrecutsets.add(newPrecutset);
        }
 
        return newPrecutsets;
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
	private void removeNonMinimalSets(List<Set<Integer>> precutsets) {
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
     * Metoda służąca do przerywania obliczeń.
     */
    public void emergencyStop() {
    	terminate = true;
    }
    
    /**
     * Metoda służąca do zapisywania w bazie programu kolejnej listy zbiorów MCS. 
     * @param results ArrayList[Set[Integer]] - lista zbiorów MCS
     */
    private void addNewDataVector(ArrayList<Set<Integer>> results) {
    	//TODO:
    	ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
    	ArrayList<ArrayList<Integer>> mcsInfoMatrix = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> mcsDataMatrix = new ArrayList<ArrayList<Integer>>();
		
    	if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisMCSReduction").equals("1")) {	
    		for(Set<Integer> mcsSet : results) {
    			ArrayList<Integer> mcsSetData = new ArrayList<Integer>();
    			for(int element : mcsSet) {
    				mcsSetData.add(element);
    			}
    			Collections.sort(mcsSetData);	
    			ArrayList<Integer> mcsInfo = new ArrayList<Integer>();
    			
    			boolean cancel = false;
    			for(int element : mcsSetData) {
    				Transition setTransition = transitions.get(element);
    				Transition target = transitions.get(objective_Reaction);
    				int distance = InvariantsTools.calculateNodesDistance(setTransition, target, new ArrayList<Node>());
    				if(distance < 0) {
    					cancel = true;
    					break;
    				} else
    					mcsInfo.add(distance);
    			}
    			if(cancel)
    				continue;
    			
    			mcsDataMatrix.add(mcsSetData);
    			mcsInfoMatrix.add(mcsInfo);
    		}
    		
    		logInternal("Infeasible sets reduction completed. Remained: "+mcsDataMatrix.size()+"\n", false);
    	} else { //bez redukcji zbiorów
    		for(Set<Integer> mcsSet : results) {
    			ArrayList<Integer> mcsSetData = new ArrayList<Integer>();
    			for(int element : mcsSet) {
    				mcsSetData.add(element);
    			}
    			Collections.sort(mcsSetData);	
    			ArrayList<Integer> mcsInfo = new ArrayList<Integer>();
    			
    			for(int element : mcsSetData) {
    				Transition setTransition = transitions.get(element);
    				Transition target = transitions.get(objective_Reaction);
    				int distance = InvariantsTools.calculateNodesDistance(setTransition, target, new ArrayList<Node>());
    				mcsInfo.add(distance);
    			}
    			mcsDataMatrix.add(mcsSetData);
    			mcsInfoMatrix.add(mcsInfo);
    		}
    	}
		MCSDataMatrix mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
		mcsd.insertMCS(mcsDataMatrix, mcsInfoMatrix, objective_Reaction, askBeforeAdd);
	}

    /**
     * Metoda służąca do wyświetlania zbiorów w formie tekstu.
     */
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
}
