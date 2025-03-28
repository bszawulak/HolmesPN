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
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.utilities.Tools;

/**
 * Klasa odpowiedzialna za tworzenie okna narzędziowego, w którym składowane
 * są komponenty możliwe do dodania w różnych rodzajach sieci Petriego.
 * @author students
 * @author MR - wszystkie dodatkowe elementy sieci poza typowymi Place/Transition/Arc
 */
public class PetriNetTools implements TreeSelectionListener {
	@Serial
	private static final long serialVersionUID = 5385847227073467035L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JPanel panel;
	private Point position;
	private JTree toolTree;
	private DefaultMutableTreeNode pointerNode;

	/**
	 * Konstruktor domyślny obiektu klasy Tools.
	 */
	public PetriNetTools() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Tools");
		label.setVisible(true);
		panel.add(label);

		//setDockable(new DefaultDockable("Tools", panel, "Tools"));
		//setDockable(GUIManager.externalWithListener(getDockable(),guiManager.getDockingListener()));

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
		//hierachicalNode.add(new DefaultMutableTreeNode("Subnet T-type"));
		//hierachicalNode.add(new DefaultMutableTreeNode("Subnet P-type"));
		hierachicalNode.add(new DefaultMutableTreeNode("Subnet"));

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
		//this.addDockable(getDockable(), position, position);

	}

	/**
	 * Metoda ustawiająca i podświetlająca wybór wskaźnika typu POINTER
	 */
	public void selectPointer() {
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
			if(overlord.reset.isSimulatorActiveWarning(
					lang.getText("PNNT_toolTip_entry001"), lang.getText("warning"))) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				return;
			}
			if(overlord.reset.isXTPNSimulatorActiveWarning(
					lang.getText("PNNT_toolTip_entry002"), lang.getText("warning"))) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				return;
			}
		}

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			String description = (String) nodeInfo;
			switch (description) {
				case "Pointer" -> overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				case "Eraser" -> overlord.getWorkspace().setGraphMode(DrawModes.ERASER);
				case "Place" -> overlord.getWorkspace().setGraphMode(DrawModes.PLACE);
				case "Transition" -> overlord.getWorkspace().setGraphMode(DrawModes.TRANSITION);
				case "(TPN) Time" -> overlord.getWorkspace().setGraphMode(DrawModes.TIMETRANSITION);
				//case "(FPN) Functional" -> overlord.getWorkspace().setGraphMode(DrawModes.FUNCTIONALTRANS);
				case "(SPN) Stochastic" -> overlord.getWorkspace().setGraphMode(DrawModes.STOCHASTICTRANS);
				case "(SPN) Immediate" -> overlord.getWorkspace().setGraphMode(DrawModes.IMMEDIATETRANS);
				case "(SPN) Deterministic" -> overlord.getWorkspace().setGraphMode(DrawModes.DETERMINISTICTRANS);
				case "(SPN) Scheduled" -> overlord.getWorkspace().setGraphMode(DrawModes.SCHEDULEDTRANS);
				case "Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.ARC);
				case "Read Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.READARC);
				case "Inhibitor Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.ARC_INHIBITOR);
				case "Reset Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.ARC_RESET);
				case "Equal Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.ARC_EQUAL);
				case "Modifier Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.ARC_MODIFIER);
				case "xPlace" -> overlord.getWorkspace().setGraphMode(DrawModes.XPLACE);
				case "xArc" -> overlord.getWorkspace().setGraphMode(DrawModes.XARC);
				case "xInhibitor" -> overlord.getWorkspace().setGraphMode(DrawModes.XINHIBITOR);
				case "xTransition" -> overlord.getWorkspace().setGraphMode(DrawModes.XTRANSITION);
				case "Subnet T-type" -> overlord.getWorkspace().setGraphMode(DrawModes.SUBNET_T);
				case "Subnet P-type" -> overlord.getWorkspace().setGraphMode(DrawModes.SUBNET_P);
				case "Subnet" -> overlord.getWorkspace().setGraphMode(DrawModes.SUBNET_PT);
				case "C-Place" -> overlord.getWorkspace().setGraphMode(DrawModes.CPLACE);
				case "C-Transition" -> overlord.getWorkspace().setGraphMode(DrawModes.CTRANSITION);
				case "C-Arc" -> overlord.getWorkspace().setGraphMode(DrawModes.CARC);
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
						setToolTipText(lang.getText("PNTT_toolTipPointer"));
					}
					case "Eraser" -> {
						setIcon(eraserIcon);
						setToolTipText(lang.getText("PNTT_toolTipEraser"));
					}
					case "Place" -> {
						setIcon(placeIcon);
						setToolTipText(lang.getText("PNTT_toolTipPlace"));
					}
					case "Transition" -> {
						setIcon(transitionIcon);
						setToolTipText(lang.getText("PNTT_toolTipTrans"));
					}
					case "(TPN) Time" -> {
						setIcon(timeTransitionIcon);
						setToolTipText(lang.getText("PNTT_toolTipTPNTrans"));
					}
					case "(SPN) Stochastic" -> {
						setIcon(stochasticTrans);
						setToolTipText(lang.getText("PNTT_toolTipSPNTrans"));
					}
					case "(SPN) Immediate" -> {
						setIcon(immediateTransIcon);
						setToolTipText(lang.getText("PNTT_toolTipSPNImmed"));
					}
					case "(SPN) Deterministic" -> {
						setIcon(deterministicTransIcon);
						setToolTipText(lang.getText("PNTT_toolTipSPNDeter"));
					}
					case "(SPN) Scheduled" -> {
						setIcon(scheduledTransIcon);
						setToolTipText(lang.getText("PNTT_toolTipSPNSched"));
					}
					case "Arc" -> {
						setIcon(arcIcon);
						setToolTipText(lang.getText("PNTT_toolTipArc"));
					}
					case "Read Arc" -> {
						setIcon(arcIconRead);
						setToolTipText(lang.getText("PNTT_toolTipReadArc"));
					}
					case "Inhibitor Arc" -> {
						setIcon(arcIconInh);
						setToolTipText(lang.getText("PNTT_toolTipInhibArc"));
					}
					case "Reset Arc" -> {
						setIcon(arcIconRst);
						setToolTipText(lang.getText("PNTT_toolTipResetArc"));
					}
					case "Equal Arc" -> {
						setIcon(arcIconEql);
						setToolTipText(lang.getText("PNTT_toolTipEquArc"));
					}
					case "Modifier Arc" -> {
						setIcon(arcIconModifier);
						setToolTipText(lang.getText("PNTT_toolTipModArc"));
					}
					case "xPlace" -> {
						setIcon(xPlaceIcon);
						setToolTipText(lang.getText("PNTT_toolTipXTPNplace"));
					}
					case "xTransition" -> {
						setIcon(xTransIcon);
						setToolTipText(lang.getText("PNTT_toolTipXTPNtrans"));
					}
					case "xArc" -> {
						setIcon(xArcIcon);
						setToolTipText(lang.getText("PNTT_toolTipXTPNarc"));
					}
					case "xInhibitor" -> {
						setIcon(xInhArcIcon);
						setToolTipText(lang.getText("PNTT_toolTipXTPNinhArc"));
					}
					case "Subnet T-type" -> {
						setIcon(subnetT);
						setToolTipText(lang.getText("PNTT_toolTipSubnetP"));
					}
					case "Subnet P-type" -> {
						setIcon(subnetP);
						setToolTipText(lang.getText("PNTT_toolTipSubnetT"));
					}
					case "Subnet" -> {
						setIcon(subnetPT);
						setToolTipText(lang.getText("PNTT_toolTipSubnetPT"));
					}
					case "C-Place" -> {
						setIcon(cplaceIcon);
						setToolTipText(lang.getText("PNTT_toolTipCplace"));
					}
					case "C-Transition" -> {
						setIcon(ctransitionIcon);
						setToolTipText(lang.getText("PNTT_toolTipCtrans"));
					}
					case "C-Arc" -> {
						setIcon(carcIcon);
						setToolTipText(lang.getText("PNTT_toolTipCarc"));
					}
				}
			} else {
				setToolTipText(null); // no tool tip
			}
			return this;
		}
	}
}