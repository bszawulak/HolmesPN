package abyss.math.simulator;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.utilities.Tools;
import abyss.windows.AbyssNotepad;

/**
 * Klasa użytkowa, odpowiedzialna za wypisywanie w odpowiednio dobrym stylu lawiny komunikatów
 * głównego symulatora sieci.
 * @author MR
 *
 */
public class NetSimulatorLogger {
	private AbyssNotepad log = GUIManager.getDefaultGUIManager().getSimLog();
	private String stars = "**********************************************************************************************************";
	/**
	 * Konstruktor domyślny obiektu klasy NetSimulatorLogger.
	 */
	public NetSimulatorLogger() {
		
	}
	
	/**
	 * Metoda wypisująca komunikat startu symulacji sieci.
	 * @param simulationType NetType - tryb pracy
	 * @param writeHistory boolean - true jeśli zapisywana jest historia stanów
	 * @param sm SimulatorMode - rodzaj wciśniętego przycisku startu pracy
	 * @param maximumMode boolean - true jeśli maximum mode
	 */
	public void logStart(NetType simulationType, boolean writeHistory, SimulatorMode sm, boolean maximumMode) {
		String message = "";
		String histWrite = "yes";
		if(writeHistory == false)
			histWrite = "no";
		
		String max = "maximum";
		if(maximumMode == false)
			max = "50/50";
		
		message = "Simulation started. Net sim model: "; //+simulationType.toString()+ " Steps history saved: "+histWrite + "";
		log.addText(message, "t", true, false); //czas na początku
		message = ""+simulationType.toString();
		log.addText(message, "b", false, false);
		
		message =  " mode: ";	//maximum lub inny
		log.addText(message, "t", false, false);
		log.addText(max, "b", false, false);
		log.addText(" / ", "t", false, false);
		log.addText(sm.toString(), "b", false, false);
		
		message =  " steps history saved: ";
		log.addText(message, "t", false, false);
		log.addText(histWrite, "b", false, true); //enter na końcu
	}
	
	/**
	 * Komunikat o zatrzymaniu symulatora.
	 * @param step long - krok w którym zatrzymano symulator
	 */
	public void logSimStopped(long step) {
		log.addText("Simulator stopped in step: ", "t", true, false);
		log.addText(step+"", "t", false, true);
	}
	
	/**
	 * Główna metoda raportująca przebieg kroku symulacji.
	 * @param launchingTransitions ArrayList[Transition] - wektor tranzycji uruchamianych
	 * @param details boolean - true jeśli włączono dokładne raportowanie
	 */
	public void logSimStepFinished(ArrayList<Transition> launchingTransitions, boolean details) {
		//ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		//ArrayList<TimeTransition> timeTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTimeTransitions();
		//ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		//if(allTransitions == null || allTransitions.size() == 0)
		//	return;
		
		for (Transition transition : launchingTransitions) {
			ArrayList<Arc> inArcs = transition.getInArcs();
			ArrayList<Arc> outArcs = transition.getOutArcs();
			ArrayList<Node> prePlaces = new ArrayList<Node>();
			ArrayList<Node> postPlaces = new ArrayList<Node>();
			
			ArrayList<String> prePlacesInfo = new ArrayList<String>();
			ArrayList<Integer> prePlacesInfoTokens = new ArrayList<Integer>();
			ArrayList<String> postPlacesInfo = new ArrayList<String>();
			ArrayList<Integer> postPlacesInfoTokens = new ArrayList<Integer>();
			int tokensTaken = 0;
			int tokensProduced = 0;
			
			for(Arc a : inArcs) { //pobierz miejsca wejściowe do danej tramzycji
				Node p = a.getStartNode();
				if(prePlaces.contains(p) == false)
					prePlaces.add(p);
				else
					log.addText("Warning, multi-arc detected between transition: "+transition.getName()+ " and place: "+p.getName(), "warning", false, true);
				
				tokensTaken += a.getWeight();
				prePlacesInfo.add(p.getName());
				prePlacesInfoTokens.add(a.getWeight());
			}
			for(Arc a : outArcs) { //pobierz miejsca docelowe danej tranzycji
				Node p = a.getEndNode();
				if(postPlaces.contains(p) == false)
					postPlaces.add(p);
				else
					log.addText("Warning, multi-arc detected between transition: "+transition.getName()+ " and place: "+p.getName(), "warning", false, true);
				
				tokensProduced += a.getWeight();
				postPlacesInfo.add(p.getName());
				postPlacesInfoTokens.add(a.getWeight());
			}
			
			log.addText("  *Transition: ", "t", false, false);
			log.addText(transition.getName(), "b", false, false);
			log.addText("  tokens consumed: ", "t", false, false);
			log.addText(""+tokensTaken, "b", false, false);
			log.addText("  tokens produced: ", "t", false, false);
			log.addText(""+tokensProduced, "b", false, false);
			log.addText("  pre-Places: ", "t", false, false); //lista miejsc wejściowych
			log.addText(prePlacesInfo.size()+"", "b", false, false);
			log.addText("", "t", false, false);
			log.addText("  post-Places: ", "t", false, false);
			log.addText(postPlacesInfo.size()+"", "b", false, false);
			log.addText("", "t", false, true);
			
			if(details == true) {
				int m = getMaxLength(prePlacesInfo, 0);
				m = getMaxLength(postPlacesInfo, m);
				
				if(prePlacesInfo.size() > 0) {
					for(int i=0; i<prePlacesInfo.size(); i++) {
						log.addText("     (preP) ","t",false,false);
						log.addText(Tools.setToSize(prePlacesInfo.get(i),m+2,false)+": ","nodeName",false,false);
						log.addText("-"+prePlacesInfoTokens.get(i), "b", false, true);
					}
				} else {
					log.addText("     (preP) "+Tools.setToSize("---",m+2,false)+": ", "t", false, false);
					log.addText("input transition", "b", false, true);
				}

				if(postPlacesInfo.size() > 0) {
					for(int i=0; i<postPlacesInfo.size(); i++) {
						log.addText("     (postP)","t",false,false);
						log.addText(Tools.setToSize(postPlacesInfo.get(i),m+2,false)+": ","nodeName",false,false);
						log.addText("+"+postPlacesInfoTokens.get(i), "b", false, true);
					}
				} else {
					log.addText("     (postP)"+Tools.setToSize("---",m+2,false)+": ", "t", false, false);
					log.addText("output transition", "b", false, true);
				}
			}
			
			
		}
	}
	
	/**
	 * Zwraca wartość najdłuższej nazwy miejsca na liście.
	 * @param prePlacesInfo ArrayList[String] - lista miejsc
	 * @return int - długość najdłuższej nazwy
	 */
	private int getMaxLength(ArrayList<String> prePlacesInfo, int currentMax) {
		int max = currentMax;
		for(int i=0; i<prePlacesInfo.size(); i++) {
			if(prePlacesInfo.get(i).length() > max)
				max = prePlacesInfo.get(i).length();
		}
		return max;
	}
	
	

	/**
	 * Krótki komunikat o utworzeniu kopii stanu m0.
	 */
	public void logBackupCreated() {
		log.addText("   State m0 backup created", "t", false, true); //czas na początku
	}
	
	/**
	 * Metoda wywoływana kiedy przywracany jest stan m0.
	 */
	public void logSimReset() {
		log.addText("Simulator reset confirmed, state m0 restored. Simulation state cleaned.", "b", true, true);
		log.addText(stars, "b", false, true);
	}
	
	/**
	 * Metoda raportująca stan pauzy symulatora.
	 * @param pause boolean - true, jeśli pauza właśnia została włączona
	 */
	public void logSimPause(boolean pause) {
		if(pause) {
			log.addText("   Simulator paused.", "t", false, true);
		} else {
			log.addText("   Simulator restarted after pause.", "t", false, true);
		}
	}
}
