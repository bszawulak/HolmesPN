package abyss.darkgui.dockwindows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.utilities.Tools;

import com.javadocking.dock.SingleDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;

/**
 * Klasa odpowiedzialna za tworzenie okna narzêdziowego, w którym sk³adowane
 * s¹ komponenty mo¿liwe do dodania w ró¿nych rodzajach sieci Petriego.
 * @author students
 *
 */
public class PetriNetTools extends SingleDock implements TreeSelectionListener {
	private static final long serialVersionUID = 5385847227073467035L;
	private Dockable dockable;
	private JPanel panel;
	private Point position;
	private GUIManager guiManager;
	private JTree toolTree;

	// buttons
	JButton place, transition, arc, none;

	/**
	 * Konstruktor domyœlny obiektu klasy Tools.
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
		miscNode.add(new DefaultMutableTreeNode("Pointer"));
		miscNode.add(new DefaultMutableTreeNode("Eraser"));

		DefaultMutableTreeNode basicPetriNetsNode = new DefaultMutableTreeNode("Simple Petri Nets");
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Place"));
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Transition"));
		basicPetriNetsNode.add(new DefaultMutableTreeNode("Arc"));

		DefaultMutableTreeNode timePetriNetsNode = new DefaultMutableTreeNode("Time Petri Nets");
		timePetriNetsNode.add(new DefaultMutableTreeNode("TimeTransition"));		
		
		// Create the root and add the country nodes.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tools");
		rootNode.add(miscNode);
		rootNode.add(basicPetriNetsNode);
		rootNode.add(timePetriNetsNode);
		
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
	 * Metoda zwracaj¹ca podokno dokowalne narzêdzi intefejsu programu.
	 * @return Dockable - obiekt dokowalny
	 */
	public Dockable getDockable() {
		return dockable;
	}

	/**
	 * Metoda ustawiaj¹ca podokno dokowalne narzêdzi intefejsu programu.
	 * @return Dockable - obiekt dokowalny
	 */
	private void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	/**
	 * Przeci¹¿ona metoda odpowiedzialna za reakcjê na zmianê narzêdzia rysowania sieci.
	 * @param e TreeSelectionEvent - zdarzenie wybrania wêz³a w drzewie narzêdzi
	 */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)toolTree.getLastSelectedPathComponent();
		if (node == null)
			return;
		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			String description = (String) nodeInfo;
			switch (description) {
			case "Place":
				guiManager.getWorkspace().setGraphMode(DrawModes.PLACE);
				break;
			case "Transition":
				guiManager.getWorkspace().setGraphMode(DrawModes.TRANSITION);
				break;
			case "Arc":
				guiManager.getWorkspace().setGraphMode(DrawModes.ARC);
				break;
			case "Pointer":
				guiManager.getWorkspace().setGraphMode(DrawModes.POINTER);
				break;
			case "TimeTransition":
				guiManager.getWorkspace().setGraphMode(DrawModes.TIMETRANSITION);
				break;
			case "Eraser":
				guiManager.getWorkspace().setGraphMode(DrawModes.ERASER);
			}
		}
	}

	/**
	 * Klasa wewnêtrzna odpowiedzialna za kszta³t wêz³ów drzewa narzêdziowego.
	 * @author students
	 *
	 */
	private class LeafRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 3169140404884453079L;
		private ImageIcon placeIcon, transitionIcon, arcIcon, pointerIcon,
				eraserIcon, timeTransitionIcon;

		/**
		 * Konstruktor domyœlny obiektu klasy wewnêtrznej LeafRenderer. Tworzy ikony
		 * narzêdzi rysowania sieci Petriego.
		 */
		public LeafRenderer() {
			placeIcon = Tools.getResIcon16("/icons/place.gif");
			transitionIcon = Tools.getResIcon16("/icons/transition.gif");
			timeTransitionIcon = Tools.getResIcon16("/icons/timeTransition.gif");
			arcIcon = Tools.getResIcon16("/icons/arc.gif");
			pointerIcon = Tools.getResIcon16("/icons/pointer.gif");
			eraserIcon = Tools.getResIcon16("/icons/eraser.gif");
		}

		/**
		 * Metoda odpowiedzialna za obs³ugê wyboru wêz³a w drzewie narzêdziowym.
		 * @param tree JTree - drzewo narzêdzi programu
		 * @param value Object - wartoœæ
		 * @param sel boolean - czy wybrany
		 * @param expanded boolean - czy wêze³ otwarty
		 * @param leaf boolean - czy jest liœciem
		 * @param row int - nr wiersza
		 * @param hasFocus boolean - czy jest nad nim kursor
		 * @return Component - komponent wybrany z drzewa
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			String nodeInfo = (String) (node.getUserObject());
			if (leaf) {
				switch (nodeInfo) {
				case "Pointer":
					setIcon(pointerIcon);
					setToolTipText("Allows to point at and move net components on the sheet.");
					break;
				case "Eraser":
					setIcon(eraserIcon);
					setToolTipText("Allows to delete net components.");
					break;
				case "Place":
					setIcon(placeIcon);
					setToolTipText("Allows you to place a Place on the sheet.");
					break;
				case "Transition":
					setIcon(transitionIcon);
					setToolTipText("Allows you to place a Transition on the sheet.");
					break;
				case "TimeTransition":
					setIcon(timeTransitionIcon);
					setToolTipText("Allows you to place a Time Transition on the sheet.");
					break;
				case "Arc":
					setIcon(arcIcon);
					setToolTipText("Allows you to place an Arc on the sheet.");
					break;
				}
			} else {
				setToolTipText(null); // no tool tip
			}

			return this;
		}
	}
}
