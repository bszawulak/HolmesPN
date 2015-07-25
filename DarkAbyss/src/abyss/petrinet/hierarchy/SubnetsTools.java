package abyss.petrinet.hierarchy;

import java.util.ArrayList;

import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Node;

/**
 * Klasa narzędzi pomocnicznych dla sieci hierarchicznych.
 * 
 * @author MR
 */
public final class SubnetsTools {
	/** Prywatny konstruktor. To powinno załatwić problem obiektów. */
	private SubnetsTools() {
	}
	
	/**
	 * Metoda sprawdza, czy wierzchołek o podanym EL jest interfejsem podsieci w której się znajduje.
	 * @param element ElementLocation - sprawdzany element
	 * @param metanodes ArrayList[MetaNode] - wektor meta-węzłów
	 * @return int - liczba EL w sieci gdzie jest metanode
	 */
	public static int isInterface(ElementLocation element, ArrayList<MetaNode> metanodes) {
		Node node = element.getParentNode();
		int subSheet = element.getSheetID();
		int metaSheet = -1;
		for(MetaNode meta : metanodes) {
			if(meta.getRepresentedSheetID() == subSheet) {
				metaSheet = meta.getElementLocations().get(0).getSheetID();
				break;
			}
		}
		if(metaSheet == -1)
			return 0;

		int counter = 0;
		for(ElementLocation el : node.getElementLocations()) {
			if(el.getSheetID() == metaSheet) {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Metoda zwraca liczbę metałuków wchodzących DO metanode Z podanego węzła sieci.
	 * @param node Node - węzeł sieci
	 * @param meta MetaNode - meta-węzeł
	 * @return int - liczba meta-łuków 
	 */
	public static int countInMetaArcs(Node node, MetaNode meta) {
		int counter = 0;
		for(Arc arc : meta.getElementLocations().get(0).accessMetaInArcs()) {
			if(arc.getStartNode().equals(node))
				counter++;
		}
		return counter;
	}
	
	/**
	 * Metoda zwraca liczbę meta-łuków wychodzących Z metanode DO podanego węzła sieci.
	 * @param node Node - węzeł sieci
	 * @param meta MetaNode - meta-węzeł
	 * @return int - liczba meta-łuków 
	 */
	public static int countOutMetaArcs(Node node, MetaNode meta) {
		int counter = 0;
		for(Arc arc : meta.getElementLocations().get(0).accessMetaOutArcs()) {
			if(arc.getEndNode().equals(node))
				counter++;
		}
		return counter;
	}
	
	/**
	 * Metoda zwraca liczbę łuków wychodzących Z interfejsu wejściowego DO innych węzłów podsieci
	 * @param node Node - wierzchołek sieci
	 * @param sheet int - numer podsieci
	 * @param countMeta boolean - true, jeśli liczyć też meta-łuku (sieci wielopoziomowe)
	 * @return int - liczba łuków wychodzących
	 */
	public static int countInterfaceInArcs(Node node, int sheet, boolean countMeta) {
		int counter = 0;
		for(ElementLocation el : node.getElementLocations()) {
			if(el.getSheetID() == sheet) {
				counter += el.getOutArcs().size(); //OK, interface IN, ale łuki z niego wychodzą DO elementów podsieci
				if(countMeta)
					counter += el.accessMetaOutArcs().size();
			}
		}
		return counter;
	}
	
	/**
	 * Metoda zwraca liczbę łuków wchodzących DO interfejsu wyjściowego Z innych węzłów podsieci
	 * @param node Node - wierzchołek sieci
	 * @param sheet int - numer podsieci
	 * @param countMeta boolean - true, jeśli liczyć też meta-łuku (sieci wielopoziomowe)
	 * @return int - liczba łuków wychodzących
	 */
	public static int countInterfaceOutArcs(Node node, int sheet, boolean countMeta) {
		int counter = 0;
		for(ElementLocation el : node.getElementLocations()) {
			if(el.getSheetID() == sheet) {
				counter += el.getInArcs().size(); //OK, interface OUT, ale łuki do niego wchodzą Z elementów podsieci
				if(countMeta)
					counter += el.accessMetaOutArcs().size();
			}
		}
		return counter;
	}
	
	/**
	 * Znajdź EL dla danego node z największą liczbą meta-łuków IN i OUT w sieci gdzie znajduje się metanode
	 * @param node Node - P/T
	 * @param metanode MetaNode - M
	 * @return ElementLocation - z największą liczbą łuków meta (obie strony) z metanode
	 */
	public static ElementLocation getNexusEL(Node node, MetaNode metanode) {
		int metaSheet = metanode.getElementLocations().get(0).getSheetID();
		ElementLocation metaEL = metanode.getElementLocations().get(0);
		ArrayList<Integer> elCounter = new ArrayList<Integer>();
		for(int i=0; i<node.getElementLocations().size(); i++)
			elCounter.add(0);
		
		ElementLocation anyOne = null;
		ArrayList<ElementLocation> checkList = new ArrayList<ElementLocation>();
		for(Arc arc : metaEL.accessMetaInArcs()) {
			if(arc.getStartNode().equals(node)) {
				ElementLocation el = null;
				if((el = arc.getStartLocation()).getSheetID() == metaSheet) {
					if(checkList.contains(el))
						continue;
					else
						checkList.add(el);
					
					anyOne = el;
					int index = node.getElementLocations().indexOf(el);
					int val = elCounter.get(index);
					val += el.accessMetaOutArcs().size();
					elCounter.set(index, val);
				}
			}
		}
		checkList.clear();
		for(Arc arc : metaEL.accessMetaOutArcs()) {
			if(arc.getEndNode().equals(node)) {
				ElementLocation el = null;
				if((el = arc.getEndLocation()).getSheetID() == metaSheet) {
					if(checkList.contains(el))
						continue;
					else
						checkList.add(el);
					
					anyOne = el;
					int index = node.getElementLocations().indexOf(el);
					int val = elCounter.get(index);
					val += el.accessMetaInArcs().size();
					elCounter.set(index, val);
				}
			}
		}
		
		int found = 0;
		int maxValue = 0;
		boolean check = false;
		for(int val : elCounter) {
			if(val > maxValue) {
				found = elCounter.indexOf(val);
				maxValue = val;
				check = true;
			}
		}
		if(check) {
			return node.getElementLocations().get(found);
		} else {
			return anyOne;
		}
	}
}
