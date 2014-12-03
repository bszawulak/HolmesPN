package abyss.darkgui.box;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
//import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.math.Arc;
import abyss.math.InvariantTransition;
import abyss.math.Node;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;

import com.javadocking.dock.SingleDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;

public class Properties extends SingleDock {
	private static final long serialVersionUID = -1966643269924197502L;
	private Dockable dockable;
	private Point position;
	private GUIManager guiManager;
	private PropertiesTable properties;
	private SelectionPanel selectionPanel;
	private JScrollPane scrollPane;
	private PropertiesType type;

	public enum PropertiesType {
		EDITOR, SIMULATOR, SELECTOR, InvANALYZER, PropANALYZER, MctANALYZER,InvSIMULATOR
	}

	public Properties(PropertiesType propertiesType) {
		type = propertiesType;

		scrollPane = new JScrollPane();
		
		guiManager = GUIManager.getDefaultGUIManager();

		if (type == PropertiesType.EDITOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Properties", scrollPane,
					"Properties"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == PropertiesType.SIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Simulator", scrollPane,
					"Simulator"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == PropertiesType.SELECTOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Selection", scrollPane,
					"Selection"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == PropertiesType.InvANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants analysis", scrollPane,
					"Invariants analysis"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == PropertiesType.MctANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCT Groups", scrollPane,
					"MCT Groups"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == PropertiesType.PropANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Net properties", scrollPane,
					"Net properties"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == PropertiesType.InvSIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants simulator", scrollPane,
					"Invariants simulator"),GUIManager.getDefaultGUIManager().getDockingListener()));
//		setDockable(GUIManager.getDefaultGUIManager()
//				.decorateDockableWithActions(getDockable(), false));

		position = new Point(0, 0);
		this.addDockable(getDockable(), position, position);

		if (type == PropertiesType.SELECTOR) {
			setSelectionPanel(new SelectionPanel());
			scrollPane.getViewport().add(getSelectionPanel());
		}
	}

	public Dockable getDockable() {
		return dockable;
	}

	private void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	public void createSimulatorProperties() {
		if (type == PropertiesType.SIMULATOR) {
			setProperties(new PropertiesTable(GUIManager.getDefaultGUIManager().getWorkspace()
					.getProject().getSimulator()));
			scrollPane.getViewport().add(getProperties());
		}
	}
	
	public void createInvSimulatorProperties() {
		if (type == PropertiesType.InvSIMULATOR) {
			properties = new PropertiesTable(GUIManager.getDefaultGUIManager().getWorkspace()
					.getProject().getInvSimulator());
			scrollPane.getViewport().add(properties);
		}
	}
	
	public void showExternalInvariants(ArrayList<ArrayList<InvariantTransition>> invariants) {
		if (type == PropertiesType.InvANALYZER) {
			properties = new PropertiesTable(invariants);
			scrollPane.getViewport().add(properties);
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer().settInvariants(invariants); 
		}
	}
	
	public void showInvariants(ArrayList<ArrayList<InvariantTransition>> invariants) {
		if (type == PropertiesType.InvANALYZER) {
			properties = new PropertiesTable(invariants);
			scrollPane.getViewport().add(properties);
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer().settInvariants(invariants); 
		}
	}
	
	public void showNetProperties(ArrayList<ArrayList<Object>> arrayList) {
		if (type == PropertiesType.PropANALYZER) {
			properties = new PropertiesTable(arrayList,true);
			scrollPane.getViewport().add(properties);			
		}
	}
	
	public void showMCT(ArrayList<ArrayList<Transition>> mctGroups) {
		if (type == PropertiesType.MctANALYZER) {
			properties = new PropertiesTable(mctGroups,PropertiesType.MctANALYZER);
			scrollPane.getViewport().add(properties);
		}
	}

	public void updateSimulatorProperties() {
		if (type == PropertiesType.SIMULATOR) {
			getProperties().updateSimulatorProperties();
		}
	}

	public void selectElement(SelectionActionEvent e) {
		if (e.getActionType() == SelectionActionType.SELECTED_ONE) {
			if (e.getElementLocationGroup().size() > 0) {
				Node n = e.getElementLocation().getParentNode();
				if (n.getType() == PetriNetElementType.PLACE)
					setProperties(new PropertiesTable((Place) n,
							e.getElementLocation()));
				else
				{
					@SuppressWarnings("unused")
					PetriNetElementType test =  n.getType();
					
					if (n.getType().equals(PetriNetElementType.TIMETRANSITION))
						setProperties(new PropertiesTable((TimeTransition) n,e.getElementLocation()));
					else
						setProperties(new PropertiesTable((Transition) n,e.getElementLocation()));
				}
				scrollPane.getViewport().add(getProperties());
			} else if (e.getArcGroup().size() > 0) {
				setProperties(new PropertiesTable((Arc) e.getArc()));
				scrollPane.getViewport().add(getProperties());
			}
		} else if (e.getActionType() == SelectionActionType.SELECTED_SHEET) {
			setProperties(new PropertiesTable(guiManager
					.getWorkspace()
					.getSheets()
					.get(guiManager.getWorkspace().getIndexOfId(
							e.getSheetId()))));
			scrollPane.getViewport().add(getProperties());
		}
	}

	public PropertiesTable getProperties() {
		return properties;
	}

	private void setProperties(PropertiesTable properties) {
		this.properties = properties;
	}

	public SelectionPanel getSelectionPanel() {
		return selectionPanel;
	}

	private void setSelectionPanel(SelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
	}
}
