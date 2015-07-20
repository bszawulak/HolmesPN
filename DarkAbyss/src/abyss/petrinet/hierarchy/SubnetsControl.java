package abyss.petrinet.hierarchy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.IdGenerator;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.petrinet.elements.MetaNode.MetaType;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.workspace.Workspace;

/**
 * Klasa odpowiedzialna za metody pomagające w kontrolowaniu sieci hierarchicznych. Albo przynajmniej
 * udawaniu że taka kontrola istnieje.
 * 
 * @author MR
 */
public class SubnetsControl {
	GUIManager overlord = null;

	/**
	 * Konstruktor obiektu klasy SubnetsControl.
	 * @param boss GUIManager - obiekt okna głównego
	 */
	public SubnetsControl(GUIManager boss) {
		this.overlord = boss;
	}
	
	/**
	 * Metoda obsługuje funkcję dodawania nowego portalu do podsieci (poprzez metanode).
	 * @param startPTLocation ElementLocation - miejsce lub tranzycja
	 * @param endMetanodeLocation ElementLocation - metanode
	 * @param semiArc Arc - łuk z pozycją miejsca/tranzycji
	 * @return boolean - true, jeśli udało się dodać nowy portal
	 */
	public boolean addArcToMetanode(ElementLocation startPTLocation, ElementLocation endMetanodeLocation, Arc semiArc) {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		ArrayList<Arc> arcs = workspace.getProject().getArcs();
		MetaNode metanode = (MetaNode) endMetanodeLocation.getParentNode();
		//int startingSheet =  startPTLocation.getSheetID(); 
		int subnetID = metanode.getRepresentedSheetID();
		
		Node startNode = startPTLocation.getParentNode();
		int howManyExists = 0;
		ElementLocation oneOfMany = null;
		for(ElementLocation el : startNode.getElementLocations()) {
			if(el.getSheetID() == subnetID) { //sprawdź czy istnieją portale
				howManyExists++;
				oneOfMany = el;
			}
		}
		
		boolean hasMetaArc = checkIfInMetaArcExists(startPTLocation, metanode);
		boolean addIdAlready = false;
		if(hasMetaArc == false) { //jeśli nie ma meta-łuku, dodaj bez zbędnych pytań
			addIdAlready = true;
		} else if(howManyExists > 0) { //jeśli jest, pytaj
			Object[] options = {"Add another portal", "Don't add new arc/portal",};
			int n = JOptionPane.showOptionDialog(null,
							"Subnet "+subnetID+" already contains "+howManyExists+" portal(s) of\n"
							+startNode.getName()+".\nAdd another one?",
							
							"Add another portal symbol?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				addIdAlready = true;
			} else {
				return false;
			}
		} else { //jeszcze nie ma połączenia
			addIdAlready = true;
		}
		
		if(addIdAlready) {
			ElementLocation newPortal = null;
			if(oneOfMany == null) {
				newPortal = new ElementLocation(subnetID, new Point(50,50), startNode);
			} else {
				newPortal = new ElementLocation(subnetID,
						new Point(oneOfMany.getPosition().x+15, oneOfMany.getPosition().y+15), startNode);
			}
			ElementLocation newNameEL = new ElementLocation(subnetID, new Point(0, 0), startNode);
			startNode.getElementLocations().add(newPortal);
			startNode.getNamesLocations().add(newNameEL);
			startNode.setPortal(true);
			
			
			if(!hasMetaArc) { //utwórz meta łuk, bo go jeszcze nie ma z tego węzła (od startLocation)
				Arc arc = new Arc(IdGenerator.getNextId(), semiArc.getStartLocation(), endMetanodeLocation, TypesOfArcs.META_ARC);
				endMetanodeLocation.accessMetaInArcs().add(arc);
				startPTLocation.accessMetaOutArcs().add(arc);
				arcs.add(arc);
			}
			
			int index = workspace.getIndexOfId(subnetID);
			workspace.getSheets().get(index).getGraphPanel().repaint();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Metoda obsługuje funkcję dodawania nowego portalu z podsieci (z metanode).
	 * @param endPTNode ElementLocation - miejsce lub tranzycja
	 * @param startMetanode ElementLocation - EL meta-node
	 * @param semiArc Arc - łuk z pozycją metanode
	 * @return boolean - true, jeśli udało się dodać nowy portal
	 */
	public boolean addArcFromMetanode(ElementLocation endPTNode, ElementLocation startMetanode, Arc semiArc) {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		ArrayList<Arc> arcs = workspace.getProject().getArcs();
		MetaNode metanode = (MetaNode) startMetanode.getParentNode();
		int startingSheet =  endPTNode.getSheetID(); 
		int subnetID = metanode.getRepresentedSheetID();
		
		Node endNode = endPTNode.getParentNode();
		int howManyExists = 0;
		ElementLocation oneOfMany = null;
		for(ElementLocation el : endNode.getElementLocations()) {
			if(el.getSheetID() == subnetID) {
				howManyExists++;
				oneOfMany = el;
			}
		}
		
		boolean hasMetaArc = checkIfOutMetaExists(endPTNode, metanode);
		boolean addIdAlready = false;
		if(hasMetaArc == false) { //jeśli nie ma meta-łuku, dodaj bez zbędnych pytań
			addIdAlready = true;
		} else if(howManyExists > 0) {
			Object[] options = {"Add another portal", "Don't add new arc/portal",};
			int n = JOptionPane.showOptionDialog(null,
							"Subnet "+subnetID+" already contains "+howManyExists+" portal(s) of\n"
							+endNode.getName()+".\nAdd another one?",
							
							"Add another portal symbol?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				addIdAlready = true;
			} else {
				return false;
			}
		} else { //jeszcze nie ma połączenia
			addIdAlready = true;
		}
		
		if(addIdAlready) {
			//add new portal in source net (usually main net0)
			ElementLocation newPortal = null;
			if(oneOfMany == null) {
				newPortal = new ElementLocation(subnetID, new Point(50,50), endNode);
			} else {
				newPortal = new ElementLocation(subnetID,
						new Point(oneOfMany.getPosition().x+15, oneOfMany.getPosition().y+15), endNode);
			}
			ElementLocation newNameEL = new ElementLocation(subnetID, new Point(0, 0), endNode);
			endNode.getElementLocations().add(newPortal);
			endNode.getNamesLocations().add(newNameEL);
			endNode.setPortal(true);
			
			if(!hasMetaArc) { //utwórz meta łuk, bo go jeszcze nie ma do tego węzła (od startLocation)
				Arc arc = new Arc(IdGenerator.getNextId(), startMetanode, endPTNode, TypesOfArcs.META_ARC);
				startMetanode.accessMetaOutArcs().add(arc);
				endPTNode.accessMetaInArcs().add(arc);
				arcs.add(arc);
			}
			
			//
			
			int index = workspace.getIndexOfId(subnetID);
			workspace.getSheets().get(index).getGraphPanel().repaint();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Metoda sprawdza, czy z danego węzła (P/T) już wychodzi meta-łuk do podanego meta-węzła.
	 * @param startLocation ElementLocation - EL P/T startowego węzła
	 * @param metanode MetaNode - węzeł docelowy łuku
	 * @return boolean - true, jeśli meta-łuk istnieje, false jeśli jeszcze nie
	 */
	private boolean checkIfInMetaArcExists(ElementLocation startLocation, MetaNode metanode) {
		Node parent = startLocation.getParentNode();
		for(ElementLocation el : parent.getElementLocations()) {
			for(Arc arc : el.accessMetaOutArcs()) {
				if(arc.getEndLocation().getParentNode().equals(metanode)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Metoda sprawdza, czy dodanego węzła (P/T) już wchodzi meta-łuk z podanego meta-węzła.
	 * @param startLocation ElementLocation - EL P/T końcowego węzła
	 * @param metanode MetaNode - węzeł docelowy łuku
	 * @return boolean - true, jeśli meta-łuk istnieje, false jeśli jeszcze nie
	 */
	private boolean checkIfOutMetaExists(ElementLocation endLocation, MetaNode metanode) {
		Node parent = endLocation.getParentNode();
		for(ElementLocation el : parent.getElementLocations()) {
			for(Arc arc : el.accessMetaInArcs()) {
				if(arc.getStartLocation().getParentNode().equals(metanode)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean addPortalFromMetanode(ElementLocation startNode, MetaNode node) {
		
		
		return true;
	}
	
	public boolean changeSubnetType(MetaNode metanode, MetaType desiredType) {
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("snoopyCompatibleMode").equals("1")) {
			if(desiredType == MetaType.SUBNET) {
				JOptionPane.showMessageDialog(null, "Snoopy compatibility mode is activated in program options.\n"
						+ "Dual interface (PT) subnetworks are not allowed.", 
						"Compatibility issue", JOptionPane.INFORMATION_MESSAGE);
				return false;
			} 
		} else {
			if(desiredType == MetaType.SUBNET) {
				metanode.setMetaType(desiredType);
				return true; //nie ma co sprawdzać, aktywuj podsieć dualnego dostępu
			}
		}
		
		int sheetID = metanode.getRepresentedSheetID();
		if(desiredType == MetaType.SUBNETPLACE) {
			ArrayList<ElementLocation> elements = getSubnetElementLocations(sheetID);
			for(ElementLocation el : elements) {
				if(el.getParentNode().isPortal() == false)
					continue;
				
				if(el.getParentNode() instanceof Transition) //t-portale w Subnet type-P są dozwolone (i tylko one)
					continue;
				
				if(el.getParentNode() instanceof Place) {
					//sprawdzić, czy to nie jest tylko prywatny portal wewnętrzny
					for(ElementLocation internalEL : el.getParentNode().getElementLocations()) {
						if(internalEL.getSheetID() != sheetID) {
							JOptionPane.showMessageDialog(null, "Subnet (type T or TP) contains place portals as interfaces to other subnetworks.\n"
									+ "Subnet P-type can only be connected by transition portals.", 
									"Compatibility issue", JOptionPane.INFORMATION_MESSAGE);
							return false;
						}
					}
				}
			}
		} else if(desiredType == MetaType.SUBNETTRANS) {
			ArrayList<ElementLocation> elements = getSubnetElementLocations(sheetID);
			for(ElementLocation el : elements) {
				if(el.getParentNode().isPortal() == false)
					continue;
				
				if(el.getParentNode() instanceof Place) //p-portale w Subnet type-T są dozwolone (i tylko one)
					continue;
				
				if(el.getParentNode() instanceof Transition) {
					//sprawdzić, czy to nie jest tylko prywatny portal wewnętrzny
					for(ElementLocation internalEL : el.getParentNode().getElementLocations()) {
						if(internalEL.getSheetID() != sheetID) {
							JOptionPane.showMessageDialog(null, "Subnet (type P or TP) contains place portals as interfaces to other subnetworks.\n"
									+ "Subnet T-type can only be connected by place portals.", 
									"Compatibility issue", JOptionPane.INFORMATION_MESSAGE);
							return false;
						}
					}
				}
			}
		}
		metanode.setMetaType(desiredType);
		return true;
	}
	
	/**
	 * Metoda zwraca wszystkie obiekty ElementLocation należące do podsieci z podanego arkusza.
	 * @param sheetID int - nr arkusza
	 * @return ArrayList[ElementLocation] - wektor elementów
	 */
	public ArrayList<ElementLocation> getSubnetElementLocations(int sheetID) {
		ArrayList<ElementLocation> result = new ArrayList<ElementLocation>();
		ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
		for(Node n : nodes) {
			for(ElementLocation el : n.getElementLocations()) {
				if(el.getSheetID() == sheetID)
					result.add(el);
			}
		}
		return result;
	}
	
	/**
	 * Metoda zwraca wektor z ilością elementów dla każdej podsieci.
	 * @return ArrayList[Integer] - wektor liczności elementów dla podsieci
	 */
	public ArrayList<Integer> getSubnetsVector() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
		for(Node n : nodes) {
			for(ElementLocation el : n.getElementLocations()) {
				int sheetID = el.getSheetID();
				int shNumber = result.size();
				if(sheetID > shNumber-1) {
					HierarchicalGraphics.updateVector(result, sheetID - shNumber + 1, 0);
				}
				
				int value = result.get(sheetID) + 1;
				result.set(sheetID, value);
			}
		}
		int sheetsNumber = overlord.getWorkspace().getSheets().size();
		int shNumber = result.size();
		if(sheetsNumber > shNumber) {
			HierarchicalGraphics.updateVector(result, sheetsNumber - shNumber, 0);
		}
		
		return result;
	}
	
	/**
	 * Metoda odpowiedzialna za usuwanie meta-węzła danej podsieci (arkusza).
	 * @param sheetID int - nr arkusza
	 */
	public void removeMetaNode(int sheetID) {
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		ArrayList<MetaNode> metanodes = pn.getMetaNodes();
		ArrayList<Arc> arcs = pn.getArcs();
		ArrayList<Node> nodes = pn.getNodes();
		boolean removed = false;
		boolean found = false;
		
		if(metanodes.size() == 0) {
			//GUIManager.getDefaultGUIManager().log("Metanodes vector already empty. Nothing to remove.", "text", true);
			return;
		}
		
		for(MetaNode node : metanodes) {
			if(node.getRepresentedSheetID() == sheetID) {
				found = true;
				if(node.getInArcs().size() > 0 || node.getOutArcs().size() > 0) {
					GUIManager.getDefaultGUIManager().log("Serious internal problem encountered. MetaNode should NEVER have normal arcs."
							+ " Please contact authors. Also, net analysis may be wrong.", "error", true);
					
					for(ElementLocation el : node.getElementLocations()) {
						for (Iterator<Arc> i = el.getInArcs().iterator(); i.hasNext();) {
							Arc a = i.next();
							arcs.remove(a);
							a.unlinkElementLocations();
							if (a.getPairedArc() != null) {
								Arc arc = a.getPairedArc();
								arc.unlinkElementLocations();
								arcs.remove(arc);
							}
							i.remove();
						}
						for (Iterator<Arc> i = el.getOutArcs().iterator(); i.hasNext();) {
							Arc a = i.next();
							arcs.remove(a);
							a.unlinkElementLocations();
							if (a.getPairedArc() != null) {
								Arc arc = a.getPairedArc();
								arc.unlinkElementLocations();
								arcs.remove(arc);
							}
							i.remove();
						}
					}
				}
				
				//usuwanie tylko w elementach P/T
				for(ElementLocation el : node.getElementLocations()) {
					for(Arc arc : el.accessMetaInArcs()) {
						ElementLocation startEL = arc.getStartLocation();
						startEL.accessMetaOutArcs().remove(arc);	
						arcs.remove(arc);
					}
					
					for(Arc arc : el.accessMetaOutArcs()) {
						ElementLocation endEL = arc.getEndLocation();
						endEL.accessMetaInArcs().remove(arc);	
						arcs.remove(arc);
					}
				}
				nodes.remove(node);
				removed = true;
				break;
			}
		}
		if(!found) {
			GUIManager.getDefaultGUIManager().log("Metanode for sheet "+sheetID+" does not exist.", "warning", true);
		} else if(!removed) {
			GUIManager.getDefaultGUIManager().log("Failed to remove metanode for sheet "+sheetID, "error", true);
		}
	}
}
