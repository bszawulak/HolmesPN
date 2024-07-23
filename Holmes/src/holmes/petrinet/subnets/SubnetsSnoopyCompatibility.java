package holmes.petrinet.subnets;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.windows.HolmesNotepad;

/**
 * Odjechana klasa posiadająca narzędzia do weryfikacji czy sieć jest kompatybilna z hierachicznością
 * w rozumieniu twórców Snoopiego czy nie, oraz do przywrócenia takiej kompatybilności.
 */
public class SubnetsSnoopyCompatibility {
	private static LanguageManager lang = GUIManager.getLanguageManager();
	GUIManager gui = null;
	PetriNet pn = null;
	
	/**
	 * Zwykły konstruktor, nie ma tu nic ciekawego do oglądania, proszę przechodzić dalej...
	 */
	public SubnetsSnoopyCompatibility() {
		gui = GUIManager.getDefaultGUIManager();
		pn = gui.getWorkspace().getProject();
	}
	
	/**
	 * Metoda sprawdza czy każdy metanode ma tylko 1 lokalizację oraz czy ich typu to T lub P (tylko).
	 * @return ArrayList[ArrayList[Integer]] - 2 wektory wskazujące na problamatyczne podsieci lub null gdy wszystko ok
	 */
	public ArrayList<ArrayList<Integer>> macroCheck() {
		//czy podsieci są kanoniczne, czy każdy metawęzeł ma tylko 1 ElementLocation, takie tam
		ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>();
		boolean problems = false;
		ArrayList<MetaNode> metanodes = pn.getMetaNodes();
		int size = metanodes.size();
		ArrayList<Integer> problemMultiEL = new ArrayList<Integer>();
		ArrayList<Integer> problemWrongType = new ArrayList<Integer>();
		for(int i=0; i<size; i++) {
			problemMultiEL.add(0);
			problemWrongType.add(0);
		}
		
		
		for(int i=0; i<size; i++) {
			MetaNode meta = metanodes.get(i);
			if(meta.getElementLocations().size() > 1) {
				problems = true;
				problemMultiEL.set(i, 1);
			}
			
			if(meta.getMetaType() == MetaType.UNKNOWN || meta.getMetaType() == MetaType.SUBNET) {
				problems = true;
				problemWrongType.set(i, 1);
			}
		}
		
		if(problems) {
			results.add(problemMultiEL);
			results.add(problemWrongType);
			return results;
		} else {
			return null;
		}
	}
	
	/**
	 * Metoda wyszukująca problemy z liczbą meta-łuków i (opcja) naprawiająca je
	 * @param showResults boolean - true jeśli pokazać okno raportu
	 * @return boolean - true, jeśli naprawiono
	 */
	public boolean checkAndFix(boolean showResults) {
		boolean fixedAll = true;
		ArrayList<Node> nodes = pn.getNodes();
		ArrayList<MetaNode> metanodes = pn.getMetaNodes();
		ArrayList<Arc> arcs = pn.getArcs();
		//int subnet = gui.getWorkspace().accessSheetsIDtable().size();
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		
		
		for(Node node : nodes) {
			try {
				if(!node.isPortal() && !(node instanceof MetaNode))
					continue;
				
				ArrayList<Integer> subnets = SubnetsTools.getSubnets(node);
				if(subnets.size() == 1) { //nie jest interfejsem
					continue;
				}
				
				//!!!!!!!!!!!!!!!!!!!!
				//IN / OUT W STOSUNKU DO METANODE!!! CZYLI ILE NP. IN-METAARCS TRZEBA DODAĆ DO METANODE DANEJ SIECI
				ArrayList<Integer> requiredINmArcs = new ArrayList<Integer>();
				ArrayList<Integer> requiredOUTmArcs = new ArrayList<Integer>();
				int maxID = GUIManager.getDefaultGUIManager().getWorkspace().getMaximumSubnetID();
				for(int i=0; i<maxID+1; i++) {
					requiredINmArcs.add(0);
					requiredOUTmArcs.add(0);
				}
				//policz wartości początkowe metałuków IN i OUT skierowanych w metanode'y podsieci w których jest node
				countINandOUTmArcs(node, subnets, requiredINmArcs, requiredOUTmArcs, metanodes);

				//teraz dla każdej podsieci okresl zapotrzebowanie na meta-łuki:
				for(int subnetID : subnets) {
					//ArrayList<Integer> pathway = getPathway(node, subnetID, metanodes, subnets);
					
					ArrayList<Integer> pathway = getPathway(node, subnetID, metanodes, subnets);
					int pathNetID = pathway.get(0); //ścieżka służy do określenia które (poza pierwszą) podsieci zostaną
					//zaktualizowane o (minus) liczbę łuków interfejsów w pierwszej na liście pathway (nevermind: działa)
					
					int inInterfaces = SubnetsTools.countInterfaceInArcs(node, pathNetID, false); //tyle potrzeba metaArcsIN
					int outInterfaces = SubnetsTools.countInterfaceOutArcs(node, pathNetID, false);
					//tyle będzie potrzebne metaIN i metaOUT we wszystkich dalszych sieciach w pathway
					
					if(inInterfaces == 0 && outInterfaces == 0) {
						continue;
					}
					
					int indexStart = pathway.indexOf(pathNetID); //poza ostatnią, która nie potrzebuje metanode'ów (nawet jeśli ma)
					for(int sub=indexStart; sub<pathway.size()-1; sub++) {
						int subID = pathway.get(sub);
						
						MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, subID);
						int repNetID = meta.getRepresentedSheetID();
						
						
						int valIN = requiredINmArcs.get(repNetID);
						valIN -= inInterfaces;
						requiredINmArcs.set(repNetID, valIN);
						
						int valOUT = requiredOUTmArcs.get(repNetID);
						valOUT -= outInterfaces;
						requiredOUTmArcs.set(repNetID, valOUT);
					}
				}

				//FIXING:
				boolean inFix = false;
				boolean outFix = false;
				int totalINadded = 0;
				int totalOUTadded = 0;
				if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorSubnetCompressMode").equals("1")) {
					for(int net=0; net<requiredINmArcs.size(); net++) {
						int howMany = requiredINmArcs.get(net);
						if(howMany < 0) { //tyle brakuje
							MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, net);
							ElementLocation pattern = getAnyELfromSubnet(node, meta.getFirstELoc().getSheetID());
							
							gui.subnetsHQ.addAllMissingInMetaArcsCompression(meta.getFirstELoc(), pattern, -howMany, arcs);
							inFix = true;
							totalINadded += (-howMany);
						}
					}
					for(int net=0; net<requiredOUTmArcs.size(); net++) {
						int howMany = requiredOUTmArcs.get(net);
						if(howMany < 0) { //tyle brakuje
							MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, net);
							ElementLocation pattern = getAnyELfromSubnet(node, meta.getFirstELoc().getSheetID());
							
							gui.subnetsHQ.addAllMissingOutMetaArcs(meta.getFirstELoc(), pattern, -howMany, arcs);
							outFix = true;
							totalOUTadded += (-howMany);
						}
					}
				} else {
					for(int net=0; net<requiredINmArcs.size(); net++) {
						int howMany = requiredINmArcs.get(net);
						if(howMany < 0) { //tyle brakuje
							MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, net);
							ElementLocation pattern = getAnyELfromSubnet(node, meta.getFirstELoc().getSheetID());
							
							gui.subnetsHQ.addAllMissingInMetaArcs(meta.getFirstELoc(), pattern, -howMany, arcs);
							inFix = true;
							totalINadded += (-howMany);
						}
					}
					for(int net=0; net<requiredOUTmArcs.size(); net++) {
						int howMany = requiredOUTmArcs.get(net);
						if(howMany < 0) { //tyle brakuje
							MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, net);
							ElementLocation pattern = getAnyELfromSubnet(node, meta.getFirstELoc().getSheetID());
							
							gui.subnetsHQ.addAllMissingOutMetaArcsCompression(meta.getFirstELoc(), pattern, -howMany, arcs);
							outFix = true;
							totalOUTadded += (-howMany);
						}
					}
				}
				
				if(inFix || outFix) {
					
					notePad.addTextLineNL(lang.getText("SSC_entry001")+" "+node.getName(), "text");
					String strB = String.format(lang.getText("SSC_entry002"), totalINadded, totalOUTadded);
					notePad.addTextLineNL(strB, "text");
				}
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00420exception")+" "
						+node.getName()+"\n"+e.getMessage(), "error", true);
				fixedAll = false;
			}
		}
		
		if(showResults) {
			notePad.setVisible(true);
		} else {
			notePad.dispose();
		}

		
		return fixedAll; //I did what I could sir...
	}

	/**
	 * Metoda zwraca pierwszy znaleziony EL węzła w danej podsieci.
	 * @param node Node - węzeł sieci
	 * @param subnet int - ID podsieci
	 * @return ElementLocation - znaleziony EL węzła
	 */
	private ElementLocation getAnyELfromSubnet(Node node, int subnet) {
		for(ElementLocation el : node.getElementLocations()) {
			if(el.getSheetID() == subnet)
				return el;
		}
		return null;
	}
	
	/**
	 * Dla danego node i danego ID podsieci odtwarza ścieżkę 'w dół' po podsieciach zawierających portale node.
	 * @param node Node - węzeł sprawdzany
	 * @param subnetID int - podsieć początkowa
	 * @param metanodes ArrayList[MetaNode] - wektor meta-węzłów
	 * @param subnets ArrayList[Integer] - ID podsieci z portalami node
	 * @return ArrayList[Integer] - ścieżka do podsieci początkowej do ostatniej zawierającej portale node
	 */
	private ArrayList<Integer> getPathway(Node node, int subnetID, ArrayList<MetaNode> metanodes, ArrayList<Integer> subnets) {
		ArrayList<Integer> pathway = new ArrayList<Integer>();
		
		boolean proceed = true;
		int thisSubnet = subnetID;
		pathway.add(thisSubnet);
		while(proceed) {
			//znajdź reprezentujący metanode:
			MetaNode metaRep = SubnetsTools.getMetaForSubnet(metanodes, thisSubnet);
			if(metaRep == null)
				return pathway; //podsieć 0
			
			int lowerSubnet = metaRep.getFirstELoc().getSheetID();
			
			if(subnets.contains(lowerSubnet)) {// ???
				pathway.add(lowerSubnet);
				thisSubnet = lowerSubnet;
			} else {
				return pathway;
			}
		}
		return pathway;
	}
	
	/**
	 * Metoda określa dla każdej podsieci węzła node, ile jest meta-łuków IN i OUT skierowanych w metanode
	 * reprezentujący daną podsieć
	 * @param node Node - węzeł
	 * @param subnets ArrayList[Integer] - ID podsieci w których są portale node
	 * @param requiredINmArcs ArrayList[Integer] - wektor liczby meta-łuków IN dla każdej podsieci z node
	 * @param requiredOUTmArcs ArrayList[Integer] - wektor liczby meta-łuków OUT dla każdej podsieci z node
	 */
	private void countINandOUTmArcs(Node node, ArrayList<Integer> subnets,
			ArrayList<Integer> requiredINmArcs, ArrayList<Integer> requiredOUTmArcs, ArrayList<MetaNode> metanodes) {
		
		for(int netID : subnets) { //dla każdej podsieci w której jest node
			//sprawdzamy ile meta-arcs IN i OUT posiada z/od danego węzła METANODE reprezentujący podsieć
			//dzięki temu potem będziemy wiedzieć, ile brakuje (potencjalnie)
			int totalInMetaArcs = 0;
			int totalOutMetaArcs = 0;
			
			MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, netID);
			if(meta == null) //sieć 0
				continue;
			
			ElementLocation metaEL = meta.getFirstELoc();
			int repNet = meta.getRepresentedSheetID();
			for(Arc arc : metaEL.accessMetaInArcs()) {
				if(arc.getStartNode().equals(node)) {
					totalInMetaArcs++;
				}
			}
			for(Arc arc : metaEL.accessMetaOutArcs()) {
				if(arc.getEndNode().equals(node)) {
					totalOutMetaArcs++;
				}
			}
			
			requiredINmArcs.set(repNet, totalInMetaArcs);
			requiredOUTmArcs.set(repNet, totalOutMetaArcs);
		}
	}

	/**
	 * Metoda zwraca identyfikatory sieci najbardziej zagnieżdżonych dla danego węzła node (tj. nie ma w takich
	 * już meta-węzłów od/do portali węzła node przyłączonych do innych podsieci (przez metanode) w których jest node.
	 * @param node Node - sprawdzany węzeł
	 * @param metanodes ArrayList[MetaNode] - wektor wszystkich meta-węzłow
	 * @param subnets ArrayList[Integer] - ID wszystkich podsieci gdzie są elementy węzła
	 * @return
	 */
	@SuppressWarnings("unused")
	private ArrayList<Integer> getDeepestSubnet(Node node, ArrayList<MetaNode> metanodes, ArrayList<Integer> subnets) {
		//znajdź taki element, w którym NIE MA metanodes reprezentujących podsieci w których Node jest
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Integer> subTested = new ArrayList<Integer>();

		for(ElementLocation el : node.getElementLocations()) {
			int subnet = el.getSheetID();
			if(!subTested.contains(subnets))
				subTested.add(subnet);
			else
				continue;
			
			ArrayList<Integer> includedSubnets = SubnetsTools.getMetanodesRepNetsInSubnet(metanodes, subnet);
			ArrayList<Integer> subTest = new ArrayList<Integer>(subnets);
			subTest.retainAll(includedSubnets);
			
			if(subTest.size() == 0) { //znaleziono
				if(!result.contains(el.getSheetID()))
					result.add(el.getSheetID());
			} else {
				continue;
			}
		}
		
		return result;
	}

	
	//********************************************************************************************************************
	//********************************************************************************************************************
	//********************************************************************************************************************
	
	class FixPackage {
		public Node node = null;
		public boolean inMeta = false; // true, jeśli outMeta DO DODANIA
		public MetaNode metanode = null; //metanode do którego trzeba dodać łuk
	}
}

















