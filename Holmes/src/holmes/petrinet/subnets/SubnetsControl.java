package holmes.petrinet.subnets;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.windows.HolmesNotepad;
import holmes.workspace.Workspace;
import holmes.workspace.WorkspaceSheet;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Klasa odpowiedzialna za metody pomagające w kontrolowaniu sieci hierarchicznych. Albo przynajmniej
 * sprytnym udawaniu że taka kontrola w ogóle istnieje.
 */
public class SubnetsControl {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();

	/**
	 * Konstruktor obiektu klasy SubnetsControl.
	 */
	public SubnetsControl() {
	}
	
	/**
	 * Metoda obsługuje funkcję dodawania nowego portalu do podsieci (poprzez metanode).
	 * @param startPTLocation ElementLocation - miejsce lub tranzycja
	 * @param endMetanodeLocation ElementLocation - metanode
	 * @param semiArc Arc - łuk z pozycją miejsca/tranzycji
	 * @return boolean - true, jeśli udało się dodać nowy portal
	 */
	public boolean addArcToMetanode(ElementLocation startPTLocation, ElementLocation endMetanodeLocation, Arc semiArc) {
		Workspace workspace = overlord.getWorkspace();
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

		boolean askStupidQuestions = !( overlord.getSettingsManager().getValue("editorSnoopyCompatibleMode").equals("1") );
		
		boolean hasMetaArc = checkIfInMetaArcExists(startPTLocation, metanode);
		boolean addIdAlready = false;
		if(!hasMetaArc) { //jeśli nie ma meta-łuku, dodaj bez zbędnych pytań
			addIdAlready = true;
		} else if(howManyExists > 0 && askStupidQuestions) { //jeśli jest, pytaj
			Object[] options = {lang.getText("SC_entry001a"), lang.getText("SC_entry001b"),}; //Add another portal ; Don't add new arc/portal
			
			String strB = "err.";
			try {
				strB = String.format(lang.getText(("SC_entry002")), subnetID, howManyExists, startNode.getName());
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"SC_entry002", "error", true);
			}
			
			int n = JOptionPane.showOptionDialog(null,
					strB,lang.getText("SC_entry002t"), JOptionPane.YES_NO_OPTION,
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
			startNode.getTextsLocations(GUIManager.locationMoveType.NAME).add(newNameEL);
			startNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(newNameEL);
			startNode.getTextsLocations(GUIManager.locationMoveType.BETA).add(newNameEL);
			startNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(newNameEL);
			startNode.getTextsLocations(GUIManager.locationMoveType.TAU).add(newNameEL);
			startNode.setPortal(true);
			
			
			//if(!hasMetaArc) { //utwórz meta łuk, bo go jeszcze nie ma z tego węzła (od startLocation)
				Arc arc = new Arc(IdGenerator.getNextId(), semiArc.getStartLocation(), endMetanodeLocation, TypeOfArc.META_ARC);
				arcs.add(arc);
			//}
			
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
		Workspace workspace = overlord.getWorkspace();
		ArrayList<Arc> arcs = workspace.getProject().getArcs();
		MetaNode metanode = (MetaNode) startMetanode.getParentNode();
		//int startingSheet =  endPTNode.getSheetID(); 
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
		if(!hasMetaArc) { //jeśli nie ma meta-łuku, dodaj bez zbędnych pytań
			addIdAlready = true;
		} else if(howManyExists > 0) {
			Object[] options = {lang.getText("SC_entry003a"), lang.getText("SC_entry003b"),}; //Add another portal ; Don't add new arc/portal
			String strB = "err.";
			try {
				strB = String.format(lang.getText("SC_entry004"), subnetID, howManyExists, endNode.getName());
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"SC_entry004", "error", true);
			}
			int n = JOptionPane.showOptionDialog(null,
					strB,lang.getText("SC_entry004t"), JOptionPane.YES_NO_OPTION,
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
			endNode.getTextsLocations(GUIManager.locationMoveType.NAME).add(newNameEL);
			endNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(newNameEL);
			endNode.getTextsLocations(GUIManager.locationMoveType.BETA).add(newNameEL);
			endNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(newNameEL);
			endNode.getTextsLocations(GUIManager.locationMoveType.TAU).add(newNameEL);
			endNode.setPortal(true);
			
			//if(!hasMetaArc) { //utwórz meta łuk, bo go jeszcze nie ma do tego węzła (od startLocation)
				Arc arc = new Arc(IdGenerator.getNextId(), startMetanode, endPTNode, TypeOfArc.META_ARC);
				arcs.add(arc);
			//}

			int index = workspace.getIndexOfId(subnetID);
			workspace.getSheets().get(index).getGraphPanel().repaint();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Dodaj brakujące łuki meta dla właśnie dodanego normalnego łuku w podsieci
	 * @param arc Arc - nowy łuk podsieci
	 */
	public void addMetaArc(Arc arc) {
		Random gen = new Random();
		ArrayList<MetaNode> metanodes = overlord.getWorkspace().getProject().getMetaNodes();
		ElementLocation startEL = arc.getStartLocation();
		ElementLocation endEL = arc.getEndLocation();
		
		//określ czy jest interfejsem (któryś z EL łuku) i jakim:
		ElementLocation ourPatient = null;
		boolean isInterfIN = false;
		if(SubnetsTools.isInterface(startEL, metanodes) > 0) {
			ourPatient = startEL; //interface IN
			isInterfIN = true;
		} else if(SubnetsTools.isInterface(endEL, metanodes) > 0) {
			ourPatient = endEL; //interface OUT
		} else {
			return; //nic tu po nas
		}
		
		//zidentyfikuj metaNode
		int subSheetID = ourPatient.getSheetID();
		MetaNode metanode = null;
		for(MetaNode meta : metanodes) {
			if(meta.getRepresentedSheetID() == subSheetID) {
				metanode = meta;
				break;
			}
		}
		if(metanode == null) {
			overlord.log("Error: no metanode found.", "error", true);
		}
		
		ElementLocation metaEL = metanode.getElementLocations().get(0);
		int sheetID = metaEL.getSheetID();
		Point point = metaEL.getPosition();
		Node parent = ourPatient.getParentNode();
		
		//sprawdź ile jest metałuków związanych z nodem
		int metaArcs = 0;
		int interfArc = 0;
		if(isInterfIN) { //sprawdź metałuki wchodzące w metanode
			metaArcs = SubnetsTools.countInMetaArcs(parent, metanode);
			interfArc = SubnetsTools.countInterfaceInArcs(parent, ourPatient.getSheetID(), true);
		} else {
			metaArcs = SubnetsTools.countOutMetaArcs(parent, metanode);
			interfArc = SubnetsTools.countInterfaceOutArcs(parent, ourPatient.getSheetID(), true);
		}
		
		if(interfArc <= metaArcs) {
			return;
		}
		
		boolean uncompressed = overlord.getSettingsManager().getValue("editorSubnetCompressMode").equals("1");
		if(uncompressed)  { //kompresja
			ElementLocation nexus = SubnetsTools.getNexusEL(parent, metanode);
			if(nexus == null) {
				uncompressed = false;
			} else {
				Arc newArc = null;
				if(isInterfIN) {
					newArc = new Arc(IdGenerator.getNextId(), nexus, metaEL, TypeOfArc.META_ARC);
				} else {
					newArc = new Arc(IdGenerator.getNextId(), metaEL, nexus, TypeOfArc.META_ARC);
				}
				overlord.getWorkspace().getProject().getArcs().add(newArc);
			}
		}
		
		if(!uncompressed) {
			//dodaj nowy element	
			int newX = gen.nextInt(160) - 80; //dodajemy (lewo-prawo)
			int newY = gen.nextInt(100)+15; //odejmujemy (w górę)
			if(!isInterfIN)
				newY *= -1;
			
			ElementLocation newEL = new ElementLocation(sheetID, new Point(point.x+newX, point.y-newY), parent);
			ElementLocation newNameEL = new ElementLocation(sheetID, new Point(0, 0), parent);
			parent.getElementLocations().add(newEL);
			parent.getTextsLocations(GUIManager.locationMoveType.NAME).add(newNameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(newNameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.BETA).add(newNameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(newNameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.TAU).add(newNameEL);

			parent.setPortal(true);
			
			Arc newArc = null;
			if(isInterfIN) {
				newArc = new Arc(IdGenerator.getNextId(), newEL, metaEL, TypeOfArc.META_ARC);
			} else {
				newArc = new Arc(IdGenerator.getNextId(), metaEL, newEL, TypeOfArc.META_ARC);
			}
			overlord.getWorkspace().getProject().getArcs().add(newArc);
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
	 * @param endLocation (ElementLocation) - EL P/T końcowego węzła.
	 * @param metanode (MetaNode) węzeł docelowy łuku.
	 * @return (boolean) - true, jeśli meta-łuk istnieje, false jeśli jeszcze nie
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
	
	/**
	 * Metoda służąca do zmiany rodzaju podsieci.
	 * @param metanode Metanode - meta-węzeł podsieci
	 * @param desiredType MetaType - typ do zmiany
	 * @return boolean - true, jeśli zmiana była możliwa
	 */
	public boolean changeSubnetType(MetaNode metanode, MetaType desiredType) {
		if(overlord.getSettingsManager().getValue("editorSnoopyCompatibleMode").equals("1")) {
			if(desiredType == MetaType.SUBNET) {
				JOptionPane.showMessageDialog(null, lang.getText("SC_entry005"), 
						lang.getText("SC_entry005t"), JOptionPane.INFORMATION_MESSAGE);
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
				if(!el.getParentNode().isPortal())
					continue;
				
				if(el.getParentNode() instanceof Transition) //t-portale w Subnet type-P są dozwolone (i tylko one)
					continue;
				
				if(el.getParentNode() instanceof Place) {
					//sprawdzić, czy to nie jest tylko prywatny portal wewnętrzny
					for(ElementLocation internalEL : el.getParentNode().getElementLocations()) {
						if(internalEL.getSheetID() != sheetID) {
							JOptionPane.showMessageDialog(null, lang.getText("SC_entry006"), 
									lang.getText("SC_entry006t"), JOptionPane.INFORMATION_MESSAGE);
							return false;
						}
					}
				}
			}
		} else if(desiredType == MetaType.SUBNETTRANS) {
			ArrayList<ElementLocation> elements = getSubnetElementLocations(sheetID);
			for(ElementLocation el : elements) {
				if(!el.getParentNode().isPortal())
					continue;
				
				if(el.getParentNode() instanceof Place) //p-portale w Subnet type-T są dozwolone (i tylko one)
					continue;
				
				if(el.getParentNode() instanceof Transition) {
					//sprawdzić, czy to nie jest tylko prywatny portal wewnętrzny
					for(ElementLocation internalEL : el.getParentNode().getElementLocations()) {
						if(internalEL.getSheetID() != sheetID) {
							JOptionPane.showMessageDialog(null, lang.getText("SC_entry007"), 
									lang.getText("SC_entry007t"), JOptionPane.INFORMATION_MESSAGE);
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
	 * Metoda zwraca opcjonalny meta-węzeł reprezentujący podaną podsieć
	 * @param subnetID int - id podsieci
	 * @return Optional[MetaNode] - opcjonalny meta-węzeł
	 */
	public Optional<MetaNode> getMetanode(int subnetID) {
		/*
		Java18
			return overlord.getWorkspace().getProject().getNodes().stream()
				.filter(node -> node instanceof MetaNode m && m.getRepresentedSheetID() == subnetID)
				.map(MetaNode.class::cast)
				.findAny();
		 */
		return overlord.getWorkspace().getProject().getNodes().stream()
				.filter(node -> node instanceof MetaNode && ((MetaNode)node).getRepresentedSheetID() == subnetID)
				.map(MetaNode.class::cast)
				.findAny();
	}

	public GraphPanel getGraphPanel(int sheetID) {
		return overlord.getWorkspace().getSheetById(sheetID).getGraphPanel();
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
					SubnetsGraphics.updateVector(result, sheetID - shNumber + 1, 0);
				}
				
				int value = result.get(sheetID) + 1;
				result.set(sheetID, value);
			}
		}
		int sheetsNumber = overlord.getWorkspace().getSheets().size();
		int shNumber = result.size();
		if(sheetsNumber > shNumber) {
			SubnetsGraphics.updateVector(result, sheetsNumber - shNumber, 0);
		}
		
		return result;
	}
	
	/**
	 * Metoda odpowiedzialna za usuwanie meta-węzła danej podsieci (arkusza).
	 * @param sheetID int - nr arkusza
	 */
	public void removeMetaNode(int sheetID) {
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<MetaNode> metanodes = pn.getMetaNodes();
		ArrayList<Arc> arcs = pn.getArcs();
		ArrayList<Node> nodes = pn.getNodes();
		boolean removed = false;
		boolean found = false;
		
		if(metanodes.isEmpty()) {
			//overlord.log("Metanodes vector already empty. Nothing to remove.", "text", true);
			return;
		}
		
		for(MetaNode node : metanodes) {
			if(node.getRepresentedSheetID() == sheetID) {
				found = true;
				if(!node.getInputArcs().isEmpty() || !node.getOutputArcs().isEmpty()) {
					overlord.log(lang.getText("LOGentry00417critErr"), "error", true);
					
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
			String strB = "err.";
			try {
				strB = String.format(lang.getText("SC_entry008"), sheetID);
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"SC_entry008", "error", true);
			}
			overlord.log(strB, "warning", true);
		} else if(!removed) {
			String strB = "err.";
			try {
				strB = String.format(lang.getText("SC_entry009"), sheetID);
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"SC_entry009", "error", true);
			}
			overlord.log(strB, "error", true);
		}
	}

	/**
	 * Dla każdej posieci w wektorze wejściowym metoda weryfikuje i poprawia liczbę metałuków przyporządkowanych
	 * odpowiednim meta-węzłom
	 * @param sheetModified ArrayList[Integer] - wektor ID podsieci
	 * @param forceFix boolean - jeśli true, będzie działać także na sheet0
	 * @param doNotRemove boolean - jeśli true, działa tylko w trybie dodawania łuków
	 */
	public void validateMetaArcs(ArrayList<Integer> sheetModified, boolean forceFix, boolean doNotRemove) {
		boolean compressMetaArcs = overlord.getSettingsManager().getValue("editorSubnetCompressMode").equals("1");

		ArrayList<MetaNode> metanodes = overlord.getWorkspace().getProject().getMetaNodes();
		ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();
		for(int sheetID : sheetModified) {
			if(sheetID == 0 && !forceFix)
				continue;
			
			ArrayList<ElementLocation> subnetElements = getSubnetElementLocations(sheetID);
			
			//określ metodanode:
			MetaNode metanodeRepresSubnet = null;
			for(MetaNode meta : metanodes) {
				if(meta.getRepresentedSheetID() == sheetID) {
					metanodeRepresSubnet = meta;
					break;
				}
			}
			if(metanodeRepresSubnet == null) {
				
				overlord.log(lang.getText("LOGentry00418critErr")+" "+sheetID, "error", true);
				break;
			}
	
			//dla każdego Node (poprzez ElementLocation) sprawdź & napraw
			ArrayList<Node> alreadyChecked = new ArrayList<Node>();
			for(ElementLocation el : subnetElements) {
				Node nodeToFix = el.getParentNode();
				if(!nodeToFix.isPortal()) // tylko portale mogą być interfejsem podsieci
					continue;
				
				if(el.getParentNode() instanceof MetaNode)
					continue;
	
				if(alreadyChecked.contains(nodeToFix)) //każdy Node sprawdzany/poprawiany tylko raz
					continue;
				else
					alreadyChecked.add(nodeToFix);
				
				int inInterfaceLinks = 0;
				int outInterfaceLinks = 0;
				
				//policz wejścia i wyjścia węzła podsieci:
				for(ElementLocation element : nodeToFix.getElementLocations()) {
					if(element.getSheetID() == sheetID) {
						
						outInterfaceLinks += element.getInArcs().size();
						inInterfaceLinks += element.getOutArcs().size();
						//dodaj meta-łuki (dla wielopoziomowych)
						outInterfaceLinks += element.accessMetaInArcs().size();
						inInterfaceLinks += element.accessMetaOutArcs().size();
					}
				}
				
				ElementLocation metaEL = metanodeRepresSubnet.getElementLocations().get(0);
				int metaSheet = metaEL.getSheetID();
				
				ArrayList<Arc> inMetaArcs = new ArrayList<Arc>();
				ArrayList<Arc> outMetaArcs = new ArrayList<Arc>();
				for(Arc arc : metaEL.accessMetaInArcs()) { //wchodzące metałuki w metanode
					if(arc.getStartNode().equals(nodeToFix))
						inMetaArcs.add(arc);
				}
				for(Arc arc : metaEL.accessMetaOutArcs()) { //wchodzące metałuki w metanode
					if(arc.getEndNode().equals(nodeToFix))
						outMetaArcs.add(arc);
				}
				
				//inMetaArcs = metaEL.accessMetaInArcs();
				//outMetaArcs = metaEL.accessMetaOutArcs();
				
				//dodaj / usuń brakujące meta-łuki - najpierw łuki prowadzące DO metanode
				if(inInterfaceLinks > inMetaArcs.size()) {
					//szukaj choć jednego EL z sieci gdzie jest metanode:
					ElementLocation pattern = null;
					for(ElementLocation cand : nodeToFix.getElementLocations()) {
						if(cand.getSheetID() == metaSheet) {
							pattern = cand;
							break;
						}
					}
					//jeśli nie będzie chociaż jednego, to znaczy, że tracimy czas, bo ten nodeToFix nie ma w ogóle
					//połączenia z siecią gdzie znajduje się aktualny EL metanode'a
					if(pattern != null) {  //czyli w ogóle ma sens dodawanie czegokolwiek...
						if(compressMetaArcs) {
							addAllMissingInMetaArcsCompression(metaEL, pattern, inInterfaceLinks-inMetaArcs.size(), arcs);
						} else {
							addAllMissingInMetaArcs(metaEL, pattern, inInterfaceLinks-inMetaArcs.size(), arcs);
						}
					}
					
				} else if(inInterfaceLinks < inMetaArcs.size() && !doNotRemove) { // == nas nie interesuje
					int toRemove = inMetaArcs.size() - inInterfaceLinks;
					//zidentyfikuj EL z tylko jednym połączeniem (tym, którego właśnie chcemy się i tak pozbyć)
					ArrayList<Arc> removeList = new ArrayList<Arc>();
					
					for(int r=inMetaArcs.size()-1; r>=inInterfaceLinks; r--) {
						ElementLocation cand = inMetaArcs.get(r).getStartLocation();
						if(cand.accessMetaOutArcs().size() == 1 && cand.accessMetaInArcs().isEmpty() &
                                cand.getInArcs().isEmpty() && cand.getOutArcs().isEmpty()) {
							removeList.add(inMetaArcs.get(r));
						}
						if(removeList.size() == toRemove)
							break;
					}
					
					int elNumber = 0; //ile EL właściwie w ogóle jest w sieci z metanode
					for(Arc arc : outMetaArcs) {
						if(arc.getEndNode().equals(nodeToFix))
							elNumber++;
					}
					
					for(Arc arc : removeList) {
						arc.unlinkElementLocations();
						arcs.remove(arc);
						toRemove--;
						if(elNumber > 1) {
							ElementLocation lonely = arc.getStartLocation();
							Node parent = lonely.getParentNode();
							parent.getElementLocations().remove(lonely);
							elNumber--;
						}
					}

					removeList.clear();
					for(int r=inMetaArcs.size()-1; r>=0; r--) {
						if(toRemove > 0)
							removeList.add(inMetaArcs.get(r));
						
						toRemove--;
					}
					
					for(Arc arc : removeList) {
						arc.unlinkElementLocations();
						arcs.remove(arc);
					}
				} // koniec sekcji inInterfaces
				
				//dodaj / usuń brakujące meta-łuki - łuki prowadzące Z metanode
				if(outInterfaceLinks > outMetaArcs.size()) {
					//szukaj choć jednego EL z sieci gdzie jest metanode:
					ElementLocation pattern = null;
					for(ElementLocation cand : nodeToFix.getElementLocations()) {
						if(cand.getSheetID() == metaSheet) {
							pattern = cand;
							break;
						}
					}
					
					if(pattern != null) {  //czyli w ogóle ma sens dodawanie czegokolwiek...
						if(compressMetaArcs) {
							addAllMissingOutMetaArcsCompression(metaEL, pattern, outInterfaceLinks-outMetaArcs.size(), arcs);
						} else {
							addAllMissingOutMetaArcs(metaEL, pattern, outInterfaceLinks-outMetaArcs.size(), arcs);
						}
					}

				} else if(outInterfaceLinks < outMetaArcs.size() && !doNotRemove) { // == nas nie interesuje
					int toRemove = outMetaArcs.size() - outInterfaceLinks;
					//najpierw pojedyncze
					ArrayList<Arc> removeList = new ArrayList<Arc>();
					//zidentyfikuj EL z tylko jednym połączeniem (tym, którego właśnie chcemy się i tak pozbyć)
					for(int r=outMetaArcs.size()-1; r>=outInterfaceLinks; r--) {
						ElementLocation cand = outMetaArcs.get(r).getEndLocation();
						if(cand.accessMetaInArcs().size() == 1 && cand.accessMetaOutArcs().isEmpty() &
                                cand.getInArcs().isEmpty() && cand.getOutArcs().isEmpty()) {
							removeList.add(outMetaArcs.get(r));
						}
						if(removeList.size() == toRemove)
							break;
					}
					
					int elNumber = 0; //ile EL właściwie w ogóle jest w sieci z metanode
					for(Arc arc : outMetaArcs) {
						if(arc.getEndNode().equals(nodeToFix))
							elNumber++;
					}
					
					for(Arc arc : removeList) {
						arc.unlinkElementLocations();
						arcs.remove(arc);
						toRemove--;
						if(elNumber > 1) {
							ElementLocation lonely = arc.getEndLocation();
							Node parent = lonely.getParentNode();
							parent.getElementLocations().remove(lonely);
							elNumber--;
						}
					}

					removeList.clear();
					for(int r=outMetaArcs.size()-1; r>=0; r--) {
						if(toRemove > 0)
							removeList.add(outMetaArcs.get(r));
						toRemove--;
					}
					
					for(Arc arc : removeList) {
						arc.unlinkElementLocations();
						arcs.remove(arc);
					}
				} // koniec sekcji outInterfaces
			} //pętla dla każdego unikalnego Node (poprzez jeden z jego EL)
		} //ile podsieci do naprawy
	}
	
	/**
	 * Metoda dodaje nowe meta-łuki DO metanode, wraz z nowymi portalami węzła okreslonego przez pattern (EL).
	 * @param metanodeEL ElementLocation - metanode, końcowy
	 * @param pattern ElementLocation - node w tej samej sieci co metanode
	 * @param howMany int - ile nowych portali dodać wraz z łukami
	 * @param arcs ArrayList[Arc] - lista łuków sieci
	 */
	public void addAllMissingInMetaArcs(ElementLocation metanodeEL, ElementLocation pattern, int howMany, ArrayList<Arc> arcs) {
		Random gen = new Random();
		Node parent = pattern.getParentNode();
		int sheetID = pattern.getSheetID();
		Point refMetaPoint = metanodeEL.getPosition();
		
		for(int i=0; i<howMany; i++) {
			ElementLocation nameEL = new ElementLocation(sheetID, new Point(0, 0), parent);
			int newX = gen.nextInt(160) - 80; //dodajemy (lewo-prawo)
			int newY = gen.nextInt(100)+15; //odejmujemy (w górę)
			Point point = new Point(refMetaPoint.x + newX, refMetaPoint.y - newY);
			ElementLocation newPortalEL = new ElementLocation(sheetID, point, parent);
			parent.setPortal(true);
			parent.getElementLocations().add(newPortalEL);
			parent.getTextsLocations(GUIManager.locationMoveType.NAME).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.BETA).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.TAU).add(nameEL);
			
			Arc arc = new Arc(IdGenerator.getNextId(), newPortalEL, metanodeEL, TypeOfArc.META_ARC);
			arcs.add(arc);
		}
	}
	
	/**
	 * Metoda dodaje nowe meta-łuki DO metanode (kompresja, 1 EL)
	 * @param metanodeEL ElementLocation - metanode, końcowy
	 * @param pattern ElementLocation - node w tej samej sieci co metanode
	 * @param howMany int - ile nowych portali dodać wraz z łukami
	 * @param arcs ArrayList[Arc] - lista łuków sieci
	 */
	public void addAllMissingInMetaArcsCompression(ElementLocation metanodeEL, ElementLocation pattern, int howMany, ArrayList<Arc> arcs) {
		//zidentyfikuj ElementLocation z max liczbą wejść do Meta
		//skoro tu jesteśmy, tzn. że jakieś są (pattern)
		Node nodeTP = pattern.getParentNode();
		ElementLocation nexus = null;
		int currentMax = 0;
		ArrayList<Arc> inMetaArcsFromNodeTP = new ArrayList<Arc>();
		int howManyElements = 0;
		for(Arc arc : metanodeEL.accessMetaInArcs()) {
			if(arc.getStartNode().equals(nodeTP)) {
				howManyElements++;
				inMetaArcsFromNodeTP.add(arc);
				int outArcs = arc.getStartLocation().accessMetaOutArcs().size();
				if(outArcs > currentMax) {
					currentMax = outArcs;
					nexus = arc.getStartLocation();
				}
			}
		}
		//znajdź NAJBLIŻSZY EL do metanode
		Point p1 = metanodeEL.getPosition();
		double distance = 99999999;
		if(nexus == null) {
			for(ElementLocation anyone : nodeTP.getElementLocations()) {
				if(anyone.getSheetID() == metanodeEL.getSheetID()) {
					if(nexus == null) { //we love you unconditionally!
						nexus = anyone;
					} else { //seek better chick
						Point p2 = nexus.getPosition();
						double currDist = Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2) + Math.pow((p2.getY() - p1.getY()), 2));
						if(currDist < distance) {
							distance = currDist;
							nexus = anyone;
						}
					}
				}
			}
		}
		
		//teraz w nexus jest EL z największą liczbą OutArcs (wiemy, że istnieje choć 1 taki - vide: pattern)
		for(Arc arc : inMetaArcsFromNodeTP) {
			ElementLocation other = arc.getStartLocation();
			if(other.equals(nexus))
				continue;
			
			//przekieruj łuki z other do nexus:
			for(Arc arcSwitch : other.accessMetaOutArcs()) {
				arcSwitch.modifyStartLocation(nexus); //zmień startowe EL łuku na naxus
				nexus.accessMetaOutArcs().add(arcSwitch); //poinformuj nexus, że ma nowy łuk
			}
			other.accessMetaOutArcs().clear(); //wyczyść wszystkie
			//sprawdź, czy other się do czegokolwiek nadaje:
			if(howManyElements > 1)
				if(other.accessMetaInArcs().isEmpty() && other.getInArcs().isEmpty() && other.getOutArcs().isEmpty()) {
					nodeTP.getElementLocations().remove(other);
					howManyElements--;
				}
		}
		
		//teraz w końcu dodaj do nexus brakujące łuki (a raczej Z niego do META)
		for(int i=0; i<howMany; i++) {
			Arc arc = new Arc(IdGenerator.getNextId(), nexus, metanodeEL, TypeOfArc.META_ARC);
			arcs.add(arc);
		}
	}
	
	/**
	 * Metoda dodaje nowe meta-łuki Z metanode do nowych portalami węzła okreslonego przez pattern (EL).
	 * @param metanodeEL ElementLocation - metanode, startowy
	 * @param pattern ElementLocation - node, końcowy
	 * @param howMany int - ile nowych portali dodać wraz z łukami
	 * @param arcs ArrayList[Arc] - lista łuków sieci
	 */
	public void addAllMissingOutMetaArcs(ElementLocation metanodeEL, ElementLocation pattern, int howMany, ArrayList<Arc> arcs) {
		Random gen = new Random();
		Node parent = pattern.getParentNode();
		int sheetID = pattern.getSheetID();
		Point refMetaPoint = metanodeEL.getPosition();
		
		for(int i=0; i<howMany; i++) {
			ElementLocation nameEL = new ElementLocation(sheetID, new Point(0, 0), parent);
			int newX = gen.nextInt(160) - 80; //dodajemy (lewo-prawo)
			int newY = gen.nextInt(100)+15; //dodajemy (w dół)
			Point point = new Point(refMetaPoint.x + newX, refMetaPoint.y + newY);
			ElementLocation newPortalEL = new ElementLocation(sheetID, point, parent);
			parent.setPortal(true);
			parent.getElementLocations().add(newPortalEL);
			parent.getTextsLocations(GUIManager.locationMoveType.NAME).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.BETA).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(nameEL);
			parent.getTextsLocations(GUIManager.locationMoveType.TAU).add(nameEL);

			Arc arc = new Arc(IdGenerator.getNextId(), metanodeEL, newPortalEL, TypeOfArc.META_ARC);
			arcs.add(arc);
		}
	}
	
	/**
	 * Metoda dodaje nowe meta-łuki Z metanode DO portalu
	 * @param metanodeEL ElementLocation - metanode, startowy
	 * @param pattern ElementLocation - node, końcowy
	 * @param howMany int - ile nowych portali dodać wraz z łukami
	 * @param arcs ArrayList[Arc] - lista łuków sieci
	 */
	public void addAllMissingOutMetaArcsCompression(ElementLocation metanodeEL, ElementLocation pattern, int howMany, ArrayList<Arc> arcs) {
		//zidentyfikuj ElementLocation z max liczbą wejść Z Meta
		//skoro tu jesteśmy, tzn. że jakieś są (pattern)
		Node nodeTP = pattern.getParentNode();
		ElementLocation nexus = null;
		int currentMax = 0;
		ArrayList<Arc> outMetaArcsToNodeTP = new ArrayList<Arc>();
		int howManyElements = 0;
		for(Arc arc : metanodeEL.accessMetaOutArcs()) {
			if(arc.getEndNode().equals(nodeTP)) {
				howManyElements++;
				outMetaArcsToNodeTP.add(arc);
				int inArcs = arc.getEndLocation().accessMetaInArcs().size();
				if(inArcs > currentMax) {
					currentMax = inArcs;
					nexus = arc.getEndLocation();
				}
			}
		}
		//znajdź NAJBLIŻSZY EL do metanode
		Point p1 = metanodeEL.getPosition();
		double distance = 99999999;
		if(nexus == null) {
			for(ElementLocation anyone : nodeTP.getElementLocations()) {
				if(anyone.getSheetID() == metanodeEL.getSheetID()) {
					if(nexus == null) { //we love you unconditionally!
						nexus = anyone;
					} else { //seek better chick
						Point p2 = nexus.getPosition();
						double currDist = Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2) + Math.pow((p2.getY() - p1.getY()), 2));
						if(currDist < distance) {
							distance = currDist;
							nexus = anyone;
						}
					}
				}
			}
		}
		
		//teraz w nexus jest EL z największą liczbą inArcs (wiemy, że istnieje choć 1 taki - vide: pattern)
		for(Arc arc : outMetaArcsToNodeTP) {
			ElementLocation other = arc.getEndLocation();
			if(other.equals(nexus))
				continue;
			
			//przekieruj łuki z other do nexus:
			for(Arc arcSwitch : other.accessMetaInArcs()) {
				arcSwitch.modifyEndLocation(nexus); //zmień startowe EL łuku na naxus
				nexus.accessMetaInArcs().add(arcSwitch); //poinformuj nexus, że ma nowy łuk
			}
			other.accessMetaInArcs().clear(); //wyczyść wszystkie
			//sprawdź, czy other się do czegokolwiek nadaje:
			if(howManyElements > 1) {
				if(other.accessMetaOutArcs().isEmpty() && other.getInArcs().isEmpty() && other.getOutArcs().isEmpty()) {
					nodeTP.getElementLocations().remove(other);
					howManyElements--;
				}
			}
		}
		
		//teraz w końcu dodaj do nexus brakujące łuki (a raczej Z niego do META)
		for(int i=0; i<howMany; i++) {
			Arc arc = new Arc(IdGenerator.getNextId(), metanodeEL, nexus, TypeOfArc.META_ARC);
			arcs.add(arc);
		}
	}
	
	/**
	 * Metoda służy do usuwania wszystkich połączeń matałukami węzła node z meta-węzłem sieci subnet.
	 * @param node Node - węzeł którego połączenia z podsiecią usuwamy
	 * @param subnet int - ID podsieci
	 */
	public void clearAllMetaArcs(Node node, int subnet) {
		ArrayList<MetaNode> metanodes = overlord.getWorkspace().getProject().getMetaNodes();
		MetaNode meta = SubnetsTools.getMetaForSubnet(metanodes, subnet);
		
		meta.removeAllInConnectionsWith(node);
		meta.removeAllOutConnectionsWith(node);
	}

	/**
	 * Metoda zwraca status sieci.
	 * @return boolean - true, jeśli kompatybilna ze Snoopiem
	 */
	public boolean checkSnoopyCompatibility() {
		boolean status = false;
		SubnetsSnoopyCompatibility sc = new SubnetsSnoopyCompatibility();
		ArrayList<ArrayList<Integer>> results = sc.macroCheck();
		if(results == null) {
			return true;
		} else {
			status = sc.checkAndFix(true);
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			
			ArrayList<Integer> problemMultiEL = results.get(0);
			ArrayList<Integer> problemWrongType = results.get(1);
			ArrayList<MetaNode> metanodes = overlord.getWorkspace().getProject().getMetaNodes();
			int size = problemWrongType.size();
			notePad.addTextLineNL(lang.getText("SC_entry010"), "text");
			for(int i=0; i<size; i++) {
				if(problemMultiEL.get(i) != 0) {
					String strB = "err.";
					try {
						strB = String.format(lang.getText("SC_entry011"), metanodes.get(i).getName(), metanodes.get(i).getRepresentedSheetID());
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"SC_entry011", "error", true);
					}
					notePad.addTextLineNL(strB, "text");
				}
			}
			notePad.addTextLineNL(" ------ ", "text");
			notePad.addTextLineNL(lang.getText("SC_entry012"), "text");
			for(int i=0; i<size; i++) {
				if(problemWrongType.get(i) != 0) {
					String strB = "err.";
					try {
						strB = String.format(lang.getText("SC_entry013")
								, metanodes.get(i).getName(), metanodes.get(i).getRepresentedSheetID(), metanodes.get(i).getMetaType());
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"SC_entry013", "error", true);
					}
					notePad.addTextLineNL(strB, "text");
				}
			}
			notePad.addTextLineNL(" ------ ", "text");
			notePad.setVisible(true);
			return status;
		}
	}

	/** 
	 * Sprawdzanie, czy EL posiada meta-łuki. Jeśli tak, to w odppowiednich podsieciach nie może już być
	 * innych EL tego node'a, aby jego EL tu posłany był zdatny do usunięcia.
	 * @param el ElementLocation - el
	 * @return boolean - true, jeśli można usunąć
	 * 
	 * MULTI-ELEMENTS META READY
	 */
	public boolean checkIfExpendable(ElementLocation el) {
		Node parent = el.getParentNode();
		if(!el.accessMetaInArcs().isEmpty()) {
			//sprawdź, czy są inne w tej podsieci z meta-łukiem
			ArrayList<MetaNode> metanodesInSubnet = new ArrayList<MetaNode>();
			//do jakich meta-węzłów prowadzą łuki MetaIN
			for(Arc arc : el.accessMetaInArcs()) {
				MetaNode meta = (MetaNode) arc.getStartNode();
				if(!metanodesInSubnet.contains(meta))
					metanodesInSubnet.add(meta);
			}
			//czy są inne EL prowadzące do tych meta-węzłów?
			int size = metanodesInSubnet.size();
			for(int m=0; m<size; m++) {
				MetaNode meta = metanodesInSubnet.get(m);
				for(ElementLocation metaEL : meta.getElementLocations()) {
					boolean found = false;
					for(Arc arc : metaEL.accessMetaOutArcs()) {
						if(arc.getEndNode().equals(parent) && !arc.getEndLocation().equals(el)) {
							found = true;
							break;
							//znaleziono inne połączenie z danym metanode (z daną podsiecią)
						}
					}
					if(found) {
						metanodesInSubnet.remove(meta);
						m--;
						size--;
						break;
					}
				}
			}
			
			if(!metanodesInSubnet.isEmpty()) {
				//dla tych podsieci sprawdzić, czy jest tam w ogóle jakiś EL od parent
				for(MetaNode meta : metanodesInSubnet) {
					int repID = meta.getRepresentedSheetID();
					
					for(ElementLocation element : parent.getElementLocations()) {
						if(element.getSheetID() == repID)
							return false; //nie można usunąć, nie ważne co by było dalej
					}
				}
			} 
		}
		
		if(!el.accessMetaOutArcs().isEmpty()) {
			//sprawdź, czy są inne w tej podsieci z meta-łukiem
			ArrayList<MetaNode> metanodesOutSubnet = new ArrayList<MetaNode>();
			//do jakich meta-węzłów prowadzą łuki MetaIN
			for(Arc arc : el.accessMetaOutArcs()) {
				MetaNode meta = (MetaNode) arc.getEndNode();
				if(!metanodesOutSubnet.contains(meta))
					metanodesOutSubnet.add(meta);
			}
			//czy są inne EL prowadzące Z tych meta-węzłów?
			int size = metanodesOutSubnet.size();
			for(int m=0; m<size; m++) {
				MetaNode meta = metanodesOutSubnet.get(m);
				for(ElementLocation metaEL : meta.getElementLocations()) {
					boolean found = false;
					for(Arc arc : metaEL.accessMetaInArcs()) {
						if(arc.getStartNode().equals(parent) && !arc.getStartLocation().equals(el)) {
							found = true;
							break;
							//znaleziono inne połączenie z danym metanode (z daną podsiecią)
						}
					}
					if(found) {
						metanodesOutSubnet.remove(meta);
						m--;
						size--;
						break;
					}
				}
			}
			
			if(!metanodesOutSubnet.isEmpty()) {
				//dla tych podsieci sprawdzić, czy jest tam w ogóle jakiś EL od parent
				for(MetaNode meta : metanodesOutSubnet) {
					int repID = meta.getRepresentedSheetID();
					
					for(ElementLocation element : parent.getElementLocations()) {
						if(element.getSheetID() == repID)
							return false; //nie można usunąć, nie ważne co by było dalej
					}
				}
			}
		}
		
		return true;
	}

	/**
	 * Metoda przenosi zaznaczone elementy do wybranej podsieci.
	 * @param graphPanel GraphPanel - arkusz z zaznaczonymi elementami
	 * @param subnetSheetId int - id podsieci
	 * @param createMetaArcs boolean - czy do otoczenia przenoszonych elementów powinny zostać dodane meta-łuki
	 */
	public void moveSelectedElementsToSubnet(GraphPanel graphPanel, int subnetSheetId, boolean createMetaArcs) {
		List<ElementLocation> elements = graphPanel.getSelectionManager().getSelectedElementLocations();
		changeNodesSheetID(elements, subnetSheetId);
		clearMetaArcs(elements);
		createPortalsAndMetaArcs(elements, getMetanode(subnetSheetId).orElseThrow(), createMetaArcs);
		graphPanel.getSelectionManager().deselectAllElements();
	}

	/**
	 * Metoda zmienia sheetID przekazanych elementów.
	 * @param elements List[ElementLocation] - elementy na których operacja ma zostać wykonana
	 * @param subnetSheetId int - id podsieci (sheetID)
	 */
	private void changeNodesSheetID(List<ElementLocation> elements, int subnetSheetId) {
		for (ElementLocation element : elements) {
			element.setSheetID(subnetSheetId);
			Node parent = element.getParentNode();

			int index = parent.getElementLocations().indexOf(element);
			ElementLocation textLocation = parent.getTextsLocations(GUIManager.locationMoveType.NAME).get(index);
			textLocation.setSheetID(subnetSheetId);
		}
	}

	/**
	 * Metoda kopiuje zaznaczone elementy do wybranej podsieci bez odwzorowania łuków.
	 * @param graphPanel GraphPanel - arkusz z zaznaczonymi elementami
	 * @param subnetSheetId int - id podsieci
	 */
	public void copySelectedElementsToSubnet(GraphPanel graphPanel, int subnetSheetId) {
		List<ElementLocation> elements = graphPanel.getSelectionManager().getSelectedElementLocations();
		for (ElementLocation location : elements) {
			cloneNodeIntoPortal(location, subnetSheetId);
		}
	}

	/**
	 * Metoda usuwa wszystkie meta-łuki w przekazanych elementach.
	 * @param elements List[ElementLocation] - elementy na których operacja ma zostać wykonana
	 */
	public void clearMetaArcs(List<ElementLocation> elements) {
		for (ElementLocation element : elements) {
			for (Arc arc : element.accessMetaInArcs()) {
				arc.getStartLocation().accessMetaOutArcs().remove(arc);
				overlord.getWorkspace().getProject().getArcs().remove(arc);
			}
			element.accessMetaInArcs().clear();
			for (Arc arc : element.accessMetaOutArcs()) {
				arc.getEndLocation().accessMetaInArcs().remove(arc);
				overlord.getWorkspace().getProject().getArcs().remove(arc);
			}
			element.accessMetaOutArcs().clear();
		}
	}

	/**
	 * Metoda odtwarza otoczenie elementów, które zostały przeniesione do podsieci
	 * i opcjonalnie dodaje meta-łuki do oryginalnego otoczenia.
	 * @param elements List[ElementLocation] - przeniesione elementy na których operacja ma zostać wykonana
	 * @param subnetNode MetaNode - meta-węzeł reprezentujący podsieć do której zostały przeniesione elementy
	 * @param createMetaArcs boolean - czy do oryginalnego otoczenia przeniesionych elementów powinny zostać dodane meta-łuki
	 */
	private void createPortalsAndMetaArcs(List<ElementLocation> elements, MetaNode subnetNode, boolean createMetaArcs) {
		int currentSheetId = subnetNode.getMySheetID();
		int subnetSheetId = subnetNode.getRepresentedSheetID();
		ElementLocation subnetElementLocation = subnetNode.getFirstELoc();
		Map<ElementLocation, ElementLocation> locationToPortal = new HashMap<>();
		Set<ElementLocation> elementToMetanode = new HashSet<>();
		Set<ElementLocation> elementFromMetanode = new HashSet<>();

		for (ElementLocation element : elements) {
			for (Arc arc : element.getInArcs()) {
				int sheetID = arc.getStartLocation().getSheetID();
				if (sheetID == currentSheetId) {
					if (createMetaArcs && !elementToMetanode.contains(arc.getStartLocation())) {
						Arc newArc = new Arc(IdGenerator.getNextId(), arc.getStartLocation(), subnetElementLocation, Arc.TypeOfArc.META_ARC);
						overlord.getWorkspace().getProject().addArc(newArc);
						elementToMetanode.add(arc.getStartLocation());
					}

					ElementLocation portal;
					if (locationToPortal.containsKey(arc.getStartLocation())) {
						portal = locationToPortal.get(arc.getStartLocation());
					} else {
						portal = cloneNodeIntoPortal(arc.getStartLocation(), subnetSheetId);
						locationToPortal.put(arc.getStartLocation(), portal);
					}

					arc.getStartLocation().getOutArcs().remove(arc);
					portal.getOutArcs().add(arc);
					arc.modifyStartLocation(portal);
				}
			}

			for (Arc arc : element.getOutArcs()) {
				int sheetID = arc.getEndLocation().getSheetID();
				if (sheetID == currentSheetId) {
					if (createMetaArcs && !elementFromMetanode.contains(arc.getEndLocation())) {
						Arc newArc = new Arc(IdGenerator.getNextId(), subnetElementLocation, arc.getEndLocation(), Arc.TypeOfArc.META_ARC);
						overlord.getWorkspace().getProject().addArc(newArc);
						elementFromMetanode.add(arc.getEndLocation());
					}

					ElementLocation portal;
					if (locationToPortal.containsKey(arc.getEndLocation())) {
						portal = locationToPortal.get(arc.getEndLocation());
					} else {
						portal = cloneNodeIntoPortal(arc.getEndLocation(), subnetSheetId);
						locationToPortal.put(arc.getEndLocation(), portal);
					}

					arc.getEndLocation().getInArcs().remove(arc);
					portal.getInArcs().add(arc);
					arc.modifyEndLocation(portal);
				}
			}
		}
	}

	/**
	 * Metoda tworzy portal elementu w wybranej podsieci.
	 * @param element ElementLocation - element, który ma zostać skopiowany
	 * @param subnetSheetId int - id podsieci
	 * @return ElementLocation - utworzony portal
	 */
	public ElementLocation cloneNodeIntoPortal(ElementLocation element, int subnetSheetId) {
		Node parent = element.getParentNode();

		ElementLocation newGraphicsEL = new ElementLocation(subnetSheetId, new Point(element.getPosition().x, element.getPosition().y), parent);
		ElementLocation newNameEL = new ElementLocation(subnetSheetId, new Point(0, 0), parent);

		parent.getElementLocations().add(newGraphicsEL);
		parent.getTextsLocations(GUIManager.locationMoveType.NAME).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.BETA).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.TAU).add(newNameEL);
		parent.setPortal(true);

		return newGraphicsEL;
	}

	/**
	 * Metoda przesuwa wybrane elementy w dolną lewą część arkusza względem pozostałych elementów.
	 * @param elementsToAlign List[ElementLocation] - wybrane elementy do przesunięcia
	 * @param otherElements List[ElementLocation] - pozostałe elementy
	 */
	public void realignElements(List<ElementLocation> elementsToAlign, List<ElementLocation> otherElements) {
		int bottom = otherElements.stream()
				.map(location -> location.getPosition().y)
				.max(Comparator.naturalOrder())
				.orElse(0);

		int offsetX = elementsToAlign.stream()
				.map(location -> location.getPosition().x)
				.min(Comparator.naturalOrder())
				.orElseThrow();

		int offsetY = elementsToAlign.stream()
				.map(location -> location.getPosition().y)
				.min(Comparator.naturalOrder())
				.orElseThrow();

		final int margin = 50;

		for (ElementLocation location : elementsToAlign) {
			Point position = location.getPosition();
			position.setLocation(position.x - offsetX + margin, position.y - offsetY + bottom + margin);
		}
	}

	/**
	 * Metoda usuwa wybraną podsieć.
	 * @param metaNode MetaNode - meta-węzeł reprezentujący podsieć do usunięcia
	 */
	public void deleteSubnet(MetaNode metaNode) {
		WorkspaceSheet subnetSheet = overlord.getWorkspace().getSheetById(metaNode.getRepresentedSheetID());

		for (ElementLocation location : getSubnetElementLocations(metaNode.getRepresentedSheetID())) {
			/*
			//Java18
			if (location.getParentNode() instanceof MetaNode m) {..
			 */
			if (location.getParentNode() instanceof MetaNode) {
				deleteSubnet((MetaNode) location.getParentNode());
			} else {
				subnetSheet.getGraphPanel().getSelectionManager().deleteElementLocation(location);
			}
		}
		removeMetaNode(metaNode.getRepresentedSheetID());
		overlord.getWorkspace().deleteSheetFromArrays(subnetSheet);
		overlord.getWorkspace().repaintAllGraphPanels();
	}

	/**
	 * Metoda rozpakowuje zaznaczoną podsieć w sieci nadrzędnej.
	 * @param graphPanel GraphPanel - arkusz na którym został zaznaczony meta-węzeł
	 */
	public void unwrapSubnet(GraphPanel graphPanel) {
		MetaNode metaNode = graphPanel.getSelectionManager().getSelectedMetanode();
		WorkspaceSheet subnetSheet = overlord.getWorkspace().getSheetById(metaNode.getRepresentedSheetID());

		List<ElementLocation> subnetElements = List.copyOf(getSubnetElementLocations(metaNode.getRepresentedSheetID()));
		List<ElementLocation> parentNetElements = getSubnetElementLocations(metaNode.getMySheetID()).stream()
						.filter(location -> location.getParentNode() != metaNode).toList();
		changeNodesSheetID(subnetElements, metaNode.getMySheetID());
		realignElements(subnetElements, parentNetElements);
		graphPanel.adjustOriginSize();

		removeMetaNode(metaNode.getRepresentedSheetID());
		overlord.getWorkspace().deleteSheetFromArrays(subnetSheet);
		overlord.getWorkspace().repaintAllGraphPanels();
	}

	/**
	 * Metoda tworzy podsieć z zaznaczonych elementów.
	 * @param graphPanel GraphPanel - arkusz z zaznaczonymi elementami
	 */
	public void createSubnetFromSelectedElements(GraphPanel graphPanel) {
		int newSheetId = overlord.getWorkspace().newTab(
				true,
				graphPanel.getSelectionManager().getMeanSelectionPoint(),
				graphPanel.getSheetId(),
				MetaNode.MetaType.SUBNET
		);
		moveSelectedElementsToSubnet(graphPanel, newSheetId, true);

		realignElements(getSubnetElementLocations(newSheetId), List.of());
		getGraphPanel(newSheetId).adjustOriginSize();
		overlord.getWorkspace().repaintAllGraphPanels();
		overlord.markNetChange();
	}

	/**
	 * Metoda scala portale tych samych węzłów.
	 * @param clickedELoc GraphPanel - kliknięty element, do którego zostaną dodane łuki z pozostałych portali
	 * @param selectedELoc List[ElementLocation] - pozostałe portale
	 */
	public void mergePortals(ElementLocation clickedELoc, List<ElementLocation> selectedELoc) {
		GraphPanel graphPanel = getGraphPanel(clickedELoc.getSheetID());
		selectedELoc.remove(clickedELoc);

		List<Arc> inArcs = new ArrayList<>();
		List<Arc> inMetaArcs = new ArrayList<>();
		List<Arc> outArcs = new ArrayList<>();
		List<Arc> outMetaArcs = new ArrayList<>();
		for (ElementLocation location : selectedELoc) {
			inArcs.addAll(location.getInArcs());
			location.getInArcs().clear();
			outArcs.addAll(location.getOutArcs());
			location.getOutArcs().clear();
			inMetaArcs.addAll(location.accessMetaInArcs());
			location.accessMetaInArcs().clear();
			outMetaArcs.addAll(location.accessMetaOutArcs());
			location.accessMetaOutArcs().clear();
		}

		for (Arc arc : inArcs) {
			arc.modifyEndLocation(clickedELoc);
		}

		for (Arc arc : outArcs) {
			arc.modifyStartLocation(clickedELoc);
		}

		for (Arc arc : inMetaArcs) {
			arc.modifyEndLocation(clickedELoc);
		}

		for (Arc arc : outMetaArcs) {
			arc.modifyStartLocation(clickedELoc);
		}

		clickedELoc.getInArcs().addAll(inArcs);
		clickedELoc.getOutArcs().addAll(outArcs);
		clickedELoc.accessMetaInArcs().addAll(inMetaArcs);
		clickedELoc.accessMetaOutArcs().addAll(outMetaArcs);

		graphPanel.getSelectionManager().deselectElementLocation(clickedELoc);
		graphPanel.getSelectionManager().deleteAllSelectedElements();
		graphPanel.getSelectionManager().selectElementLocation(clickedELoc);
		overlord.markNetChange();
	}

	/**
	 * Metoda tworzy portal elementu w pobliżu meta-węzła reprezentującego wybraną podsieć.
	 * @param element ElementLocation - element, który ma zostać skopiowany
	 * @param subnetID int - id wybranej podsieci
	 * @return ElementLocation - utworzony portal
	 */
	public ElementLocation cloneLocationNearMetanode(ElementLocation element, int subnetID) {
		Random gen = new Random();
		int angle = gen.nextInt(360);
		int radius = gen.nextInt(40) + 60;
		MetaNode metanode = getMetanode(subnetID).orElseThrow();
		Point p = metanode.getFirstELoc().getPosition();
		int x = Math.toIntExact(Math.round(radius * Math.cos(Math.toRadians(angle)) + p.x));
		int y = Math.toIntExact(Math.round(radius * Math.sin(Math.toRadians(angle)) + p.y));

		ElementLocation newLocation = overlord.subnetsHQ.cloneNodeIntoPortal(element, metanode.getMySheetID());
		newLocation.setPosition(new Point(x, y));
		return newLocation;
	}

	/**
	 * Metoda naprawia meta-łuki wybranego meta-węzła poprzez dodanie w jego pobliżu wraz z meta-łukami portali
	 * węzłów, które znajdują się zarówno w podsieci oraz nadsieci.
	 * @param metaNode MetaNode - meta-węzeł, którego meta-łuki mają zostać naprawione
	 */
	public void fixMetaArcsNumber(MetaNode metaNode) {
		Set<Node> nodes = getSubnetElementLocations(metaNode.getMySheetID()).stream()
				.map(ElementLocation::getParentNode).collect(Collectors.toSet());
		Set<Node> subnetNodes = getSubnetElementLocations(metaNode.getRepresentedSheetID()).stream()
				.map(ElementLocation::getParentNode).collect(Collectors.toSet());

		nodes.retainAll(subnetNodes);

		for (Node node : nodes) {
			AtomicBoolean hasInArcs = new AtomicBoolean(false);
			AtomicBoolean hasOutArcs = new AtomicBoolean(false);

			node.getNodeLocations(metaNode.getRepresentedSheetID()).forEach(location -> {
				if (!location.getInArcs().isEmpty()) {
					hasInArcs.set(true);
				}
				if (!location.getOutArcs().isEmpty()) {
					hasOutArcs.set(true);
				}
			});

			boolean alreadyConnected = node.getNodeLocations(metaNode.getMySheetID()).stream().anyMatch(location -> {
				boolean inArcExists = location.accessMetaInArcs().stream().anyMatch(arc -> arc.getStartLocation().equals(metaNode.getFirstELoc()));
				boolean outArcExists = location.accessMetaOutArcs().stream().anyMatch(arc -> arc.getEndLocation().equals(metaNode.getFirstELoc()));
				return inArcExists || outArcExists;
			});

			if (alreadyConnected || (!hasInArcs.get() && !hasOutArcs.get())) {
				continue;
			}

			ElementLocation newLocation = cloneLocationNearMetanode(node.getLastLocation(), metaNode.getRepresentedSheetID());
			if (hasInArcs.get()) {
				Arc newArc = new Arc(IdGenerator.getNextId(), metaNode.getFirstELoc(), newLocation, Arc.TypeOfArc.META_ARC);
				overlord.getWorkspace().getProject().addArc(newArc);
			}
			if (hasOutArcs.get()) {
				Arc newArc = new Arc(IdGenerator.getNextId(), newLocation, metaNode.getFirstELoc(), Arc.TypeOfArc.META_ARC);
				overlord.getWorkspace().getProject().addArc(newArc);
			}
		}
	}
}
