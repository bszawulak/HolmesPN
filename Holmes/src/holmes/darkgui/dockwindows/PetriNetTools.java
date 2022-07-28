package holmes.darkgui.dockwindows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.io.Serial;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.utilities.Tools;

import com.javadocking.dock.SingleDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;

/**
 * Klasa odpowiedzialna za tworzenie okna narzędziowego, w którym składowane
 * są komponenty możliwe do dodania w różnych rodzajach sieci Petriego.
 * @author students
 * @author MR - wszystkie dodatkowe elementy sieci poza typowymi Place/Transition/Arc
 */
public class PetriNetTools extends SingleDock implements TreeSelectionListener {
	@Serial
	private static final long serialVersionUID = 5385847227073467035L;
	private Dockable dockable;
	private JPanel panel;
	private Point position;
	private GUIManager guiManager;
	private JTree toolTree;
	private DefaultMutableTreeNode pointerNode;

	/**
	 * Konstruktor domyślny obiektu klasy Tools.
	 */
	public PetriNetTools() {
		guiManager = GUIManager.getDefaultGUIManager();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Tools");
		label.setVisible(true);
		panel.add(label);

		setDockable(new DefaultDockable("Tools", panel, "Tools"));
		setDockable(GUIManager.externalWithListener(getDockable(),guiManager.getDockingListener()));

		DefaultMutableTreeNode miscNode = new DefaultMutableTreeNode("Misc");
		pointerNode = new DefaultMutableTreeNode("Pointer");
		miscNode.add(pointerNode);
		miscNode.add(new DefaultMutableTreeNode("Eraser"));

		DefaultMutableTreeNode basicPetriNetsNode = new DefaultMutableTreeNode("Petri net elements");
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Place"));
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Transition"));
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Arc"));
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Read Arc"));

		DefaultMutableTreeNode xTPNNode = new DefaultMutableTreeNode("extended Time PN");
		xTPNNode.add(new DefaultMutableTreeNode("xPlace"));
		xTPNNode.add(new DefaultMutableTreeNode("xTransition"));
		xTPNNode.add(new DefaultMutableTreeNode("xArc"));
		xTPNNode.add(new DefaultMutableTreeNode("xInhibitor"));

		DefaultMutableTreeNode extArcsNode = new DefaultMutableTreeNode("extended Arcs");
		extArcsNode.add(new DefaultMutableTreeNode("Inhibitor Arc"));
		extArcsNode.add(new DefaultMutableTreeNode("Reset Arc"));
		extArcsNode.add(new DefaultMutableTreeNode("Equal Arc"));
		//basicPetriNetsNode.add(new DefaultMutableTreeNode("Modifier Arc"));

		DefaultMutableTreeNode otherTransNode = new DefaultMutableTreeNode("TPN, SPN");
		otherTransNode.add(new DefaultMutableTreeNode("(TPN) Time"));
		//otherTransNode.add(new DefaultMutableTreeNode("(FPN) Functional"));
		otherTransNode.add(new DefaultMutableTreeNode("(SPN) Stochastic"));

		DefaultMutableTreeNode colorNetsNode = new DefaultMutableTreeNode("Experimental");
		colorNetsNode.add(new DefaultMutableTreeNode("C-Place"));
		colorNetsNode.add(new DefaultMutableTreeNode("C-Transition"));
		colorNetsNode.add(new DefaultMutableTreeNode("C-Arc"));
		//otherPetriNetsNode.add(new DefaultMutableTreeNode("(SPN) Immediate"));
		//otherPetriNetsNode.add(new DefaultMutableTreeNode("(SPN) Deterministic"));
		//otherPetriNetsNode.add(new DefaultMutableTreeNode("(SPN) Scheduled"));
		
		DefaultMutableTreeNode hierachicalNode = new DefaultMutableTreeNode("Subsets");
		hierachicalNode.add(new DefaultMutableTreeNode("Subnet T-type"));
		hierachicalNode.add(new DefaultMutableTreeNode("Subnet P-type"));
		hierachicalNode.add(new DefaultMutableTreeNode("General Subnet"));

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tools");
		rootNode.add(miscNode);
		rootNode.add(basicPetriNetsNode);
		rootNode.add(xTPNNode);
		rootNode.add(extArcsNode);
		rootNode.add(otherTransNode);
		rootNode.add(colorNetsNode);
		rootNode.add(hierachicalNode);
		
		// Create the tree model.
		TreeModel treeModel = new DefaultTreeModel(rootNode);

		// Create the JTree from the tree model.
		toolTree = new JTree(treeModel);
		toolTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes.
		toolTree.addTreeSelectionListener(this);

		// Expand the tree.
		for (int row = 0; row < toolTree.getRowCount(); row++) {
			toolTree.expandRow(row);
		}

		toolTree.setCellRenderer(new LeafRenderer());

		// Add the tree in a scroll pane.
		panel.setLayout(new BorderLayout());
		panel.add(new JScrollPane(toolTree), BorderLayout.CENTER);

		position = new Point(0, 0);
		this.addDockable(getDockable(), position, position);

	}

	/**
	 * Metoda zwracająca podokno dokowalne narzędzi intefejsu programu.
	 * @return Dockable - obiekt dokowalny
	 */
	public Dockable getDockable() {
		return dockable;
	}

	/**
	 * Metoda ustawiająca podokno dokowalne narzędzi intefejsu programu.
	 */
	private void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}
	
	/**
	 * Metoda ustawiająca i podświetlająca wybór wskaźnika typu POINTER
	 */
	public void selectPointer() {
		//TreeNode[] nodes = model.getPathToRoot(nextNode);
		//toolTree.setSelectionPath(new TreePath(pointerNode));
		//int index = toolTree.getRowForPath(new TreePath(pointerNode));
		
		toolTree.setSelectionRow(2);
	}
	
	/**
	 * Metoda zwracająca obiekt drzewa narzedzi rysowania sieci.
	 * @return JTree
	 */
	public JTree getTree() {
		return toolTree;
	}

	/**
	 * Przeciążona metoda odpowiedzialna za reakcję na zmianę narzędzia rysowania sieci.
	 * @param e TreeSelectionEvent - zdarzenie wybrania węzła w drzewie narzędzi
	 */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)toolTree.getLastSelectedPathComponent();
		if (node == null)
			return;
		
		if(!node.getUserObject().toString().equals("Pointer")) {
			if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
					"Only Pointer is available when simulator is working.", "Warning")) {
				guiManager.getWorkspace().setGraphMode(DrawModes.POINTER);
				return;
			}
			if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
					"Only Pointer is available when XTPN simulator is working.", "Warning")) {
				guiManager.getWorkspace().setGraphMode(DrawModes.POINTER);
				return;
			}
		}

		
		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			String description = (String) nodeInfo;
			switch (description) {
				case "Pointer" -> guiManager.getWorkspace().setGraphMode(DrawModes.POINTER);
				case "Eraser" -> guiManager.getWorkspace().setGraphMode(DrawModes.ERASER);
				case "Place" -> guiManager.getWorkspace().setGraphMode(DrawModes.PLACE);
				case "Transition" -> guiManager.getWorkspace().setGraphMode(DrawModes.TRANSITION);
				case "(TPN) Time" -> guiManager.getWorkspace().setGraphMode(DrawModes.TIMETRANSITION);
				//case "(FPN) Functional" -> guiManager.getWorkspace().setGraphMode(DrawModes.FUNCTIONALTRANS);
				case "(SPN) Stochastic" -> guiManager.getWorkspace().setGraphMode(DrawModes.STOCHASTICTRANS);
				case "(SPN) Immediate" -> guiManager.getWorkspace().setGraphMode(DrawModes.IMMEDIATETRANS);
				case "(SPN) Deterministic" -> guiManager.getWorkspace().setGraphMode(DrawModes.DETERMINISTICTRANS);
				case "(SPN) Scheduled" -> guiManager.getWorkspace().setGraphMode(DrawModes.SCHEDULEDTRANS);
				case "Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.ARC);
				case "Read Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.READARC);
				case "Inhibitor Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.ARC_INHIBITOR);
				case "Reset Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.ARC_RESET);
				case "Equal Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.ARC_EQUAL);
				case "Modifier Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.ARC_MODIFIER);
				case "xPlace" -> guiManager.getWorkspace().setGraphMode(DrawModes.XPLACE);
				case "xArc" -> guiManager.getWorkspace().setGraphMode(DrawModes.XARC);
				case "xInhibitor" -> guiManager.getWorkspace().setGraphMode(DrawModes.XINHIBITOR);
				case "xTransition" -> guiManager.getWorkspace().setGraphMode(DrawModes.XTRANSITION);
				case "Subnet T-type" -> guiManager.getWorkspace().setGraphMode(DrawModes.SUBNET_T);
				case "Subnet P-type" -> guiManager.getWorkspace().setGraphMode(DrawModes.SUBNET_P);
				case "General Subnet" -> guiManager.getWorkspace().setGraphMode(DrawModes.SUBNET_PT);
				case "C-Place" -> guiManager.getWorkspace().setGraphMode(DrawModes.CPLACE);
				case "C-Transition" -> guiManager.getWorkspace().setGraphMode(DrawModes.CTRANSITION);
				case "C-Arc" -> guiManager.getWorkspace().setGraphMode(DrawModes.CARC);
			}
		}
	}

	/**
	 * Klasa wewnętrzna odpowiedzialna za kształt węzłów drzewa narzędziowego.
	 * @author students
	 */
	private static class LeafRenderer extends DefaultTreeCellRenderer {
		@Serial
		private static final long serialVersionUID = 3169140404884453079L;
		private ImageIcon placeIcon, transitionIcon, pointerIcon,
				eraserIcon, timeTransitionIcon, functionalTransIcon,
				stochasticTrans, immediateTransIcon, deterministicTransIcon, scheduledTransIcon;

		private ImageIcon arcIcon, arcIconRead, arcIconInh, arcIconRst, arcIconEql, arcIconModifier;

		private ImageIcon xPlaceIcon, xTransIcon, xArcIcon, xInhArcIcon;
		
		private ImageIcon subnetT, subnetP, subnetPT;
		
		private ImageIcon cplaceIcon, ctransitionIcon, carcIcon;

		/**
		 * Konstruktor domyślny obiektu klasy wewnętrznej LeafRenderer. Tworzy ikony
		 * narzędzi rysowania sieci Petriego.
		 */
		public LeafRenderer() {
			placeIcon = Tools.getResIcon16("/icons/place.gif");
			transitionIcon = Tools.getResIcon16("/icons/transition.gif");
			timeTransitionIcon = Tools.getResIcon16("/icons/timeTransition.gif");
			functionalTransIcon = Tools.getResIcon16("/icons/funcTransition.gif");
			stochasticTrans = Tools.getResIcon16("/icons/stochasticTransition.gif");
			immediateTransIcon = Tools.getResIcon16("/icons/immediateTransition.gif");
			deterministicTransIcon = Tools.getResIcon16("/icons/deterministicTransition.gif");
			scheduledTransIcon = Tools.getResIcon16("/icons/scheduledTransition.gif");
			
			pointerIcon = Tools.getResIcon16("/icons/pointer.gif");
			eraserIcon = Tools.getResIcon16("/icons/eraser.gif");
			
			arcIcon = Tools.getResIcon16("/icons/arc.gif");
			arcIconRead = Tools.getResIcon16("/icons/arcRead.gif");
			arcIconInh = Tools.getResIcon16("/icons/arcInh.gif");
			arcIconRst = Tools.getResIcon16("/icons/arcReset.gif");
			arcIconEql = Tools.getResIcon16("/icons/arcEqual.gif");
			arcIconModifier = Tools.getResIcon16("/icons/arcModifier.gif");

			xPlaceIcon= Tools.getResIcon16("/icons/xPlaceIcon.gif");
			xTransIcon = Tools.getResIcon16("/icons/xTransIcon.gif");
			xArcIcon = Tools.getResIcon16("/icons/xArcIcon.gif");
			xInhArcIcon = Tools.getResIcon16("/icons/xArcInhIcon.gif");
			
			subnetT = Tools.getResIcon16("/icons/subnetT.gif");
			subnetP = Tools.getResIcon16("/icons/subnetP.gif");
			subnetPT = Tools.getResIcon16("/icons/subnetPT.gif");
			
			cplaceIcon = Tools.getResIcon16("/icons/cplace.gif");
			ctransitionIcon = Tools.getResIcon16("/icons/ctransition.gif");
			carcIcon = Tools.getResIcon16("/icons/carc.gif");
		}

		/**
		 * Metoda odpowiedzialna za obsługę wyboru węzła w drzewie narzędziowym.
		 * @param tree JTree - drzewo narzędzi programu
		 * @param value Object - wartość
		 * @param sel boolean - czy wybrany
		 * @param expanded boolean - czy węzeł otwarty
		 * @param leaf boolean - czy jest liściem
		 * @param row int - nr wiersza
		 * @param hasFocus boolean - czy jest nad nim kursor
		 * @return Component - komponent wybrany z drzewa
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			String nodeInfo = (String) (node.getUserObject());
			if (leaf) {
				switch (nodeInfo) {
					case "Pointer" -> {
						setIcon(pointerIcon);
						setToolTipText("Allows to point at and move net components on the sheet.");
					}
					case "Eraser" -> {
						setIcon(eraserIcon);
						setToolTipText("Allows to delete net components.");
					}
					case "Place" -> {
						setIcon(placeIcon);
						setToolTipText("Allows you to place a Place on the sheet.");
					}
					case "Transition" -> {
						setIcon(transitionIcon);
						setToolTipText("Allows you to place a Transition on the sheet.");
					}
					case "(TPN) Time" -> {
						setIcon(timeTransitionIcon);
						setToolTipText("Allows you to place a Time Transition on the sheet.");
					}
					//case "(FPN) Functional" -> {
					//	setIcon(functionalTransIcon);
					//	setToolTipText("Allows you to place a Functional Transition on the sheet.");
					//}
					case "(SPN) Stochastic" -> {
						setIcon(stochasticTrans);
						setToolTipText("Allows you to place a Stochastic Transition on the sheet.");
					}
					case "(SPN) Immediate" -> {
						setIcon(immediateTransIcon);
						setToolTipText("Allows you to place a Immediate Transition on the sheet.");
					}
					case "(SPN) Deterministic" -> {
						setIcon(deterministicTransIcon);
						setToolTipText("Allows you to place a Deterministic Transition on the sheet.");
					}
					case "(SPN) Scheduled" -> {
						setIcon(scheduledTransIcon);
						setToolTipText("Allows you to place a Scheduled Transition on the sheet.");
					}
					case "Arc" -> {
						setIcon(arcIcon);
						setToolTipText("Allows you to place an Arc on the sheet.");
					}
					case "Read Arc" -> {
						setIcon(arcIconRead);
						setToolTipText("Allows you to place an Readarc on the sheet.");
					}
					case "Inhibitor Arc" -> {
						setIcon(arcIconInh);
						setToolTipText("Allows you to place an Inhibitor Arc on the sheet.");
					}
					case "Reset Arc" -> {
						setIcon(arcIconRst);
						setToolTipText("Allows you to place a Reset Arc on the sheet.");
					}
					case "Equal Arc" -> {
						setIcon(arcIconEql);
						setToolTipText("Allows you to place an Equal Arc on the sheet.");
					}
					case "Modifier Arc" -> {
						setIcon(arcIconModifier);
						setToolTipText("Allows you to place an Modifier Arc on the sheet.");
					}
					case "xPlace" -> {
						setIcon(xPlaceIcon);
						setToolTipText("xTPN place with time range.");
					}
					case "xTransition" -> {
						setIcon(xTransIcon);
						setToolTipText("xTPN transition with TPN and DPN-like time ranges.");
					}
					case "xArc" -> {
						setIcon(xArcIcon);
						setToolTipText("xTPN new arc");
					}
					case "xInhibitor" -> {
						setIcon(xInhArcIcon);
						setToolTipText("xTPN new inhibitor arc");
					}
					case "Subnet T-type" -> {
						setIcon(subnetT);
						setToolTipText("Allows you to create subnet with place-interfaces.");
					}
					case "Subnet P-type" -> {
						setIcon(subnetP);
						setToolTipText("Allows you to create subnet with transition-interfaces.");
					}
					case "General Subnet" -> {
						setIcon(subnetPT);
						setToolTipText("Allows you to create subnet with P/T interfaces.");
					}
					case "C-Place" -> {
						setIcon(cplaceIcon);
						setToolTipText("Allows you to create colored place.");
					}
					case "C-Transition" -> {
						setIcon(ctransitionIcon);
						setToolTipText("Allows you to create colored transition.");
					}
					case "C-Arc" -> {
						setIcon(carcIcon);
						setToolTipText("Allows you to create arc for colored net.");
					}
				}
			} else {
				setToolTipText(null); // no tool tip
			}

			return this;
		}
	}
}