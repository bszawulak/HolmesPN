package abyss.darkgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import abyss.darkgui.dockable.DockableMenuItem;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.workspace.Workspace;
import abyss.workspace.WorkspaceSheet;

import com.javadocking.dockable.Dockable;

public class DarkMenu extends JMenuBar {
	private static final long serialVersionUID = -1671996309149490657L;

	// GUI
	private GUIManager guiManager;

	// menus
	private JMenu fileMenu, windowMenu, analysisMenu, sheetsMenu, openMenu,saveMenu, newMenu, invMenu;

	// dockable
	private ArrayList<Dockable> dockables;
	private ArrayList<DockableMenuItem> sheetItems;

	public DarkMenu() {
		guiManager = GUIManager.getDefaultGUIManager();

		// Build the File menu.
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription("The File Menu");
		this.add(fileMenu);

		// Build the Window menu.
		windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		windowMenu.getAccessibleContext().setAccessibleDescription("The Window Menu");
		this.add(windowMenu);

		// build the Analysis menu
		analysisMenu = new JMenu("Analysis");
		analysisMenu.setMnemonic(KeyEvent.VK_A);
		analysisMenu.getAccessibleContext().setAccessibleDescription("The Analysis Menu");
		
		invMenu = new JMenu("Invariants");
		invMenu.setMnemonic(KeyEvent.VK_S);
		invMenu.getAccessibleContext().setAccessibleDescription("Invariants - Menu");
		analysisMenu.add(invMenu);

		this.add(analysisMenu);
		
		sheetsMenu = new JMenu("Project");
		sheetsMenu.setMnemonic(KeyEvent.VK_P);
		sheetsMenu.getAccessibleContext().setAccessibleDescription("The Project Sheets Menu");
		windowMenu.add(sheetsMenu);

		openMenu = new JMenu("Open");
		openMenu.setMnemonic(KeyEvent.VK_O);
		openMenu.getAccessibleContext().setAccessibleDescription("Open - Menu");
		fileMenu.add(openMenu);

		saveMenu = new JMenu("Save");
		saveMenu.setMnemonic(KeyEvent.VK_S);
		saveMenu.getAccessibleContext().setAccessibleDescription("Save - Menu");
		fileMenu.add(saveMenu);

		newMenu = new JMenu("New");
		newMenu.setMnemonic(KeyEvent.VK_N);
		newMenu.getAccessibleContext().setAccessibleDescription("New - Menu");
		fileMenu.add(newMenu);

		JMenuItem menuItem;

		// new Project
		menuItem = new JMenuItem("Project", KeyEvent.VK_N);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("New project");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Workspace workspace = guiManager.getWorkspace();
				ArrayList<Dockable> dockablesToRemove = new ArrayList<Dockable>();
				for (Dockable dockable : dockables)
					dockablesToRemove.add(dockable);
				for (Dockable dockable : dockablesToRemove)
					deleteSheetItem(dockable);
				workspace.newTab();
				int lastIndex = workspace.getSheets().size() - 1;
				ArrayList<WorkspaceSheet> sheetsToRemove = new ArrayList<WorkspaceSheet>();
				for (WorkspaceSheet sheet : workspace.getSheets()) {
					if (guiManager.IDtoIndex(sheet.getId()) != lastIndex) {
						sheetsToRemove.add(sheet);
					}
				}
				for (WorkspaceSheet sheet : sheetsToRemove) {
					workspace.deleteSheetFromArrays(sheet);
				}
				workspace.getProject().setArcs(new ArrayList<Arc>());
				workspace.getProject().setNodes(new ArrayList<Node>());
				workspace = guiManager.getWorkspace();
				dockablesToRemove = new ArrayList<Dockable>();
				for (Dockable dockable : dockables)
					dockablesToRemove.add(dockable);
				for (Dockable dockable : dockablesToRemove)
					deleteSheetItem(dockable);
				workspace.newTab();
				lastIndex = workspace.getSheets().size() - 1;
				sheetsToRemove = new ArrayList<WorkspaceSheet>();
				for (WorkspaceSheet sheet : workspace.getSheets()) {
					if (guiManager.IDtoIndex(sheet.getId()) != lastIndex) {
						sheetsToRemove.add(sheet);
					}
				}
				for (WorkspaceSheet sheet : sheetsToRemove) {
					workspace.deleteSheetFromArrays(sheet);
				}
				workspace.getProject().setArcs(new ArrayList<Arc>());
				workspace.getProject().setNodes(new ArrayList<Node>());
			}
		});
		newMenu.add(menuItem);

		// The New Tab for File
		menuItem = new JMenuItem("Sheet", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Create a new sheet (tab) in workspace");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.getWorkspace().newTab();
			}
		});
		newMenu.add(menuItem);

		// open file
		menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Open project");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.openProject();
			}
		});
		openMenu.add(menuItem);

		// import from file
		menuItem = new JMenuItem("Import...", KeyEvent.VK_I);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Import project");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.importProject();
			}
		});
		openMenu.add(menuItem);

		// save file
		menuItem = new JMenuItem("Save...", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Save project");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.saveProject();
			}
		});
		saveMenu.add(menuItem);

		// export file
		menuItem = new JMenuItem("Export...", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Export project");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.exportProject();
			}
		});
		saveMenu.add(menuItem);

		// export image file
		menuItem = new JMenuItem("Export to image", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Export project to image");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.exportProjectToImage();
			}
		});
		saveMenu.add(menuItem);

		// The JMenuItem for File
		menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Exit the application");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.add(menuItem);

		sheetItems = new ArrayList<DockableMenuItem>();
		dockables = new ArrayList<Dockable>();

		// // The JMenuItems for the dockables.
		// for (int index = 0; index < dockables.size(); index++) {
		// // Create the check box menu for the dockable.
		// sheetItems.add(new DockableMenuItem(dockables.get(index)));
		// sheetsMenu.add(sheetItems.get(index));
		// }
		windowMenu.add(new DockableMenuItem(guiManager.getToolBox().getDockable()));
		windowMenu.add(new DockableMenuItem(guiManager.getPropertiesBox().getDockable()));
		windowMenu.add(new DockableMenuItem(guiManager.getSimulatorBox().getDockable()));
		windowMenu.add(new DockableMenuItem(guiManager.getAnalyzerBox().getDockable()));
		windowMenu.add(new DockableMenuItem(guiManager.getMctBox().getDockable()));

		// The JMenuItem for external analysis
		menuItem = new JMenuItem("Generate and analyze T-invariants", KeyEvent.VK_1);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().tInvariantsAnalyse();
			}
		});
		invMenu.add(menuItem);

		// The JMenuItem for external analysis
		menuItem = new JMenuItem("Import invariants", KeyEvent.VK_2);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load analysis result from external source");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().loadExternalAnalysis();
			}
		});
		invMenu.add(menuItem);

		// The JMenuItem for external analysis
		menuItem = new JMenuItem("Generate MCT Groups", KeyEvent.VK_4);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateMCT();
			}
		});
		analysisMenu.add(menuItem);
		
		// The JMenuItem for invariants simulation
		menuItem = new JMenuItem("Start Invariants Simulation", KeyEvent.VK_5);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Start Invariants Simulation");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().group.getSelection().getActionCommand()),(Integer)GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().spiner.getValue());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		analysisMenu.add(menuItem);

		menuItem = new JMenuItem("fdfdfdf");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().group.getSelection().getActionCommand()),(Integer)GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().spiner.getValue());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		analysisMenu.add(menuItem);
		
		// The JMenuItem for exportInvariants
		menuItem = new JMenuItem("Export invariants", KeyEvent.VK_3);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Export generated invariants");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().exportGeneratedInvariants();
			}
		});
		invMenu.add(menuItem);
	}

	public void addSheetItem(Dockable dockableItem) {
		dockables.add(dockableItem);
		DockableMenuItem menuItem = new DockableMenuItem(dockableItem);
		sheetItems.add(menuItem);
		sheetsMenu.add(menuItem);
	}

	public void deleteSheetItem(Dockable dockableItem) {
		int index = dockables.indexOf(dockableItem);
		dockables.remove(index);
		sheetItems.remove(index);
		if (index > 0)
			sheetsMenu.remove(index);
	}
}
