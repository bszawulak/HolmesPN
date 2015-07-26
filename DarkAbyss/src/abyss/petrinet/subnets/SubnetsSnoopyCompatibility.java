package abyss.petrinet.subnets;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.petrinet.elements.MetaNode.MetaType;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;

/**
 * Odjechana klasa posiadająca narzędzia do weryfikacji czy sieć jest kompatybilna z hierachicznością
 * w rozumieniu twórców Snoopiego czy nie, oraz do przywrócenia takiej kompatybilności.
 * 
 * @author MR
 */
public class SubnetsSnoopyCompatibility {
	GUIManager gui = null;
	PetriNet pn = null;
	
	/**
	 * Zwykły konstruktor, nie ma tu nic ciekawego do oglądania, proszę przechodzić dalej...
	 */
	public SubnetsSnoopyCompatibility() {
		gui = GUIManager.getDefaultGUIManager();
		pn = gui.getWorkspace().getProject();
	}
	
	//TODO:
	public boolean macroCheck() {
		//czy podsieci są kanoniczne
		//czy każdy metawęzeł ma tylko 1 ElementLocation, takie tam
		
		return false;
	}
	
	/**
	 * Metoda wyszukująca problemy z liczbą meta-łuków i (opcja) naprawiająca je
	 * @param showResults boolean - true jeśli pokazać okno raportu
	 * @param fix boolean - true, jeśli problemy mają być naprawione
	 * @return boolean - true, jeśli naprawiono
	 */
	public boolean checkAndFix(boolean showResults, boolean fix) {
		fix = true; //TODO: zawsze, inaczej ciężko będzie napisać dla rozgałęzionych wielopoziomowych
		
		ArrayList<Node> nodes = pn.getNodes();
		ArrayList<MetaNode> metanodes = pn.getMetaNodes();
		//int subnet = gui.getWorkspace().accessSheetsIDtable().size();

		for(Node node : nodes) {
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
			
			
			for(int i=0; i<metanodes.size()+1; i++) {
				requiredINmArcs.add(0);
				requiredOUTmArcs.add(0);
			}
			//policz wartości początkowe metałuków IN i OUT we wszystkich podsieciach node
			countINandOUTmArcs(node, subnets, requiredINmArcs, requiredOUTmArcs);
			
			//teraz dla każdej podsieci okresl zapotrzebowanie na meta-łuki:
			for(int subnetID : subnets) {
				ArrayList<Integer> pathway = getPathway(node, subnetID, metanodes, subnets);
				//od pierwszego do ostatniego elementy pathway licz m-ArcsIN i m-ArcsOUT
				for(int pathNet : pathway) {
					int inInterfaces = SubnetsTools.countInterfaceInArcs(node, pathNet, false); //tyle potrzeba metaArcsIN
					int outInterfaces = SubnetsTools.countInterfaceOutArcs(node, pathNet, false);
					//tyle będzie potrzebne metaIN i metaOUT we wszystkich dalszych sieciach w pathway
					
					if(inInterfaces == 0 && outInterfaces == 0) {
						continue;
					}
					
					int indexStart = pathway.indexOf(pathNet) + 1;
					for(int sub=indexStart; sub<pathway.size(); sub++) {
						int subID = pathway.get(sub);
						int valIN = requiredINmArcs.get(subID);
						valIN -= inInterfaces;
						requiredINmArcs.set(subID, valIN);
						
						int valOUT = requiredOUTmArcs.get(subID);
						valOUT -= outInterfaces;
						requiredINmArcs.set(subID, valOUT);
					}
				}
			}
			
			if(fix) {
				int x = 1;
				x = 2;
			}
		}

		return false;
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
	 * Metoda określa dla każdej podsieci węzła node, ile jest meta-łuków IN i OUT w każdej.
	 * @param node Node - węzeł
	 * @param subnets ArrayList[Integer] - ID podsieci w których są portale node
	 * @param requiredINmArcs ArrayList[Integer] - wektor liczby meta-łuków IN dla każdej podsieci z node
	 * @param requiredOUTmArcs ArrayList[Integer] - wektor liczby meta-łuków OUT dla każdej podsieci z node
	 */
	private void countINandOUTmArcs(Node node, ArrayList<Integer> subnets,
			ArrayList<Integer> requiredINmArcs, ArrayList<Integer> requiredOUTmArcs) {
		
		for(int netID : subnets) { //dla każdej podsieci w której jest node
			int totalInMetaArcs = 0;
			int totalOutMetaArcs = 0;
			for(ElementLocation el : node.getElementLocations()) {
				if(el.getSheetID() != netID)
					continue;
				
				for(Arc arc : el.accessMetaInArcs()) {
					int repSubNet = ((MetaNode)arc.getStartNode()).getRepresentedSheetID();
					if(subnets.contains(repSubNet)) { //łuk prowadzi Z którejś podsieci w której są portale node'a
						totalInMetaArcs++;
					}
				}
				for(Arc arc : el.accessMetaOutArcs()) {
					int repSubNet = ((MetaNode)arc.getEndNode()).getRepresentedSheetID();
					if(subnets.contains(repSubNet)) { //łuk prowadzi DO którejś podsieci w której są portale node'a
						totalOutMetaArcs++;
					}
				}
			}
			requiredINmArcs.set(netID, totalOutMetaArcs); //ok, switched
			requiredOUTmArcs.set(netID, totalInMetaArcs);
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

















