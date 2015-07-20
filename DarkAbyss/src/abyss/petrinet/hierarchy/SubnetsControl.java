package abyss.petrinet.hierarchy;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.MetaNode.MetaType;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;

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
	
	public boolean addMetaArc(MetaNode node, Node n) {
		
		
		
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
