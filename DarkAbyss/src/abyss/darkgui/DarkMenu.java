package abyss.darkgui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
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

/**
 * Klasa implementująca metody tworzenia i obsługi głównego menu programu.
 * @author students
 *
 */
public class DarkMenu extends JMenuBar {
	private static final long serialVersionUID = -1671996309149490657L;

	// GUI
	private GUIManager guiManager;
	// menus
	private JMenu fileMenu;
	private JMenu windowMenu;
	private JMenu analysisMenu;
	private JMenu sheetsMenu;
	private JMenu mctMenu;
	private JMenu invMenu;
	// dockable
	private ArrayList<Dockable> dockables;
	private ArrayList<DockableMenuItem> sheetItems;

	/**
	 * Konstruktor domyślny obiektu klasy DarkMenu.
	 */
	public DarkMenu() {
		guiManager = GUIManager.getDefaultGUIManager();
		sheetItems = new ArrayList<DockableMenuItem>();
		dockables = new ArrayList<Dockable>();
		
		JMenu xMenu = new JMenu(" ");
		xMenu.setEnabled(false);
		//fileMenu.getAccessibleContext().setAccessibleDescription("The File Menu");
		this.add(xMenu);
		// Build the File menu.
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription("The File Menu");
		this.add(fileMenu);

		// New Project
		JMenuItem projectMenuItem = new JMenuItem("New Project",  KeyEvent.VK_N);
		projectMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_newProject.png"));
		projectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
		projectMenuItem.getAccessibleContext().setAccessibleDescription("New project");
		projectMenuItem.addActionListener(new ActionListener() {
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
				
				workspace.newTab();
				
				//workspace = guiManager.getWorkspace();
				//dockablesToRemove = new ArrayList<Dockable>();
				//for (Dockable dockable : dockables)
				//	dockablesToRemove.add(dockable);
				//for (Dockable dockable : dockablesToRemove)
				//	deleteSheetItem(dockable);
				//workspace.newTab();
				//lastIndex = workspace.getSheets().size() - 1;
				//sheetsToRemove = new ArrayList<WorkspaceSheet>();
				//for (WorkspaceSheet sheet : workspace.getSheets()) {
				//	if (guiManager.IDtoIndex(sheet.getId()) != lastIndex) {
				//		sheetsToRemove.add(sheet);
				//	}
				//}
				//for (WorkspaceSheet sheet : sheetsToRemove) {
				//	workspace.deleteSheetFromArrays(sheet);
				//}
				//workspace.getProject().setArcs(new ArrayList<Arc>());
				//workspace.getProject().setNodes(new ArrayList<Node>());
			}
		});
		fileMenu.add(projectMenuItem);

		// The New Tab for File
		JMenuItem sheetMenuItem = new JMenuItem("New Sheet", KeyEvent.VK_T);
		sheetMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_newTab.png"));
		sheetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.ALT_MASK));
		sheetMenuItem.getAccessibleContext().setAccessibleDescription("Create a new sheet (tab) in workspace");
		sheetMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.getWorkspace().newTab();
			}
		});
		fileMenu.add(sheetMenuItem);

		fileMenu.addSeparator();
		
		// open file
		JMenuItem openMenuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		openMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_open.png"));
		//openMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		openMenuItem.getAccessibleContext().setAccessibleDescription("Open project");
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.openProject();
			}
		});
		fileMenu.add(openMenuItem);
		
		// import from file
		JMenuItem importMenuItem = new JMenuItem("Import network...", KeyEvent.VK_I);
		importMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_importNet.png"));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
		importMenuItem.getAccessibleContext().setAccessibleDescription("Import project");
		importMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.importProject();
			}
		});
		fileMenu.add(importMenuItem);
		
		fileMenu.addSeparator();
		
		//openMenu = new JMenu("Open");
		//openMenu.setMnemonic(KeyEvent.VK_O);
		//openMenu.getAccessibleContext().setAccessibleDescription("Open - Menu");
		//fileMenu.add(openMenu);
		
		// save file
		JMenuItem saveMenuItem = new JMenuItem("Save...", KeyEvent.VK_S);
		saveMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_save.png"));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.saveProject();
			}
		});
		fileMenu.add(saveMenuItem);

		//saveMenu = new JMenu("Save");
		//saveMenu.setMnemonic(KeyEvent.VK_S);
		//saveMenu.getAccessibleContext().setAccessibleDescription("Save - Menu");
		//fileMenu.add(saveMenu);

		// export file
		JMenuItem exportMenuItem = new JMenuItem("Export network...", KeyEvent.VK_E);
		exportMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_exportNet.png"));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK));
		exportMenuItem.getAccessibleContext().setAccessibleDescription("Export project");
		exportMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.exportProject();
			}
		});
		fileMenu.add(exportMenuItem);

		// export image file
		JMenuItem expImgMenuItem = new JMenuItem("Export to image...", KeyEvent.VK_E);
		expImgMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_exportPicture.png"));
		expImgMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
		expImgMenuItem.getAccessibleContext().setAccessibleDescription("Export project to image");
		expImgMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.exportProjectToImage();
			}
		});
		fileMenu.add(expImgMenuItem);
		
		fileMenu.addSeparator();

		// The JMenuItem for File
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		exitMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.ALT_MASK));
		exitMenuItem.getAccessibleContext().setAccessibleDescription("Exit the application");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);
		
		//*********************************************************************************************
		//*********************************************************************************************
		//*********************************************************************************************
		
		// Build the Window menu.
		windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		windowMenu.getAccessibleContext().setAccessibleDescription("The Window Menu");
		this.add(windowMenu);
		
		sheetsMenu = new JMenu("Project");
		sheetsMenu.setIcon(new ImageIcon("resources/icons/menu/menu_Sheets.png"));
		sheetsMenu.setMnemonic(KeyEvent.VK_P);
		sheetsMenu.getAccessibleContext().setAccessibleDescription("The Project Sheets Menu");
		windowMenu.add(sheetsMenu);

		windowMenu.addSeparator();
		
		// // The JMenuItems for the dockables.
		// for (int index = 0; index < dockables.size(); index++) {
		// // Create the check box menu for the dockable.
		// sheetItems.add(new DockableMenuItem(dockables.get(index)));
		// sheetsMenu.add(sheetItems.get(index));
		// }
		windowMenu.add(new DockableMenuItem(guiManager.getToolBox().getDockable(),
				new ImageIcon("resources/icons/menu/menu_WindowTools.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getPropertiesBox().getDockable(),
				new ImageIcon("resources/icons/menu/menu_WindowProperties.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getSimulatorBox().getDockable(),
				new ImageIcon("resources/icons/menu/menu_WindowSimulator.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getAnalyzerBox().getDockable(),
				new ImageIcon("resources/icons/menu/menu_WindowsAnalysis.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getMctBox().getDockable(),
				new ImageIcon("resources/icons/menu/menu_WindowMCT.png")));
				
		
		//*********************************************************************************************
		//*********************************************************************************************
		//*********************************************************************************************
	
		
		// build the Analysis menu
		analysisMenu = new JMenu("Analysis");
		analysisMenu.setMnemonic(KeyEvent.VK_A);
		analysisMenu.getAccessibleContext().setAccessibleDescription("The Analysis Menu");
		
		invMenu = new JMenu("Invariants");
		invMenu.setIcon(new ImageIcon("resources/icons/menu/menu_InvariantsMenu.png"));
		invMenu.setMnemonic(KeyEvent.VK_S);
		invMenu.getAccessibleContext().setAccessibleDescription("Invariants - Menu");
		analysisMenu.add(invMenu);

		this.add(analysisMenu);
		

		// The JMenuItem for external analysis
		JMenuItem genTInvItem = new JMenuItem("Generate and analyze T-invariants", KeyEvent.VK_1);
		genTInvItem.setIcon(new ImageIcon("resources/icons/menu/menu_InvGen.png"));
		genTInvItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		genTInvItem.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		genTInvItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().tInvariantsAnalyse();
			}
		});
		invMenu.add(genTInvItem);

		// The JMenuItem for external analysis
		JMenuItem importInvMenuItem = new JMenuItem("Import invariants", KeyEvent.VK_2);
		importInvMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_InvImport.png"));
		importInvMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,ActionEvent.ALT_MASK));
		importInvMenuItem.getAccessibleContext().setAccessibleDescription("Load analysis result from external source");
		importInvMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().loadExternalAnalysis();
			}
		});
		invMenu.add(importInvMenuItem);
		
		// The JMenuItem for exportInvariants
		JMenuItem exportInv = new JMenuItem("Export invariants", KeyEvent.VK_3);
		exportInv.setIcon(new ImageIcon("resources/icons/menu/menu_InvExp.png"));
		exportInv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,ActionEvent.ALT_MASK));
		exportInv.getAccessibleContext().setAccessibleDescription("Export generated invariants");
		exportInv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().exportGeneratedInvariants();
			}
		});
		invMenu.add(exportInv);

		// The JMenuItem for external analysis
		JMenuItem genMCTGroups = new JMenuItem("Generate MCT Groups", KeyEvent.VK_4);
		genMCTGroups.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		genMCTGroups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		genMCTGroups.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		genMCTGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateMCT();
			}
		});
		analysisMenu.add(genMCTGroups);
		
		// The JMenuItem for invariants simulation
		JMenuItem invSimul = new JMenuItem("Start Invariants Simulation", KeyEvent.VK_5);
		invSimul.setIcon(new ImageIcon("resources/icons/menu/menu_invSim.png"));
		invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,ActionEvent.ALT_MASK));
		invSimul.getAccessibleContext().setAccessibleDescription("Start Invariants Simulation");
		invSimul.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// poniższa linia:   ╯°□°）╯︵ ┻━━┻
					GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().group.getSelection().getActionCommand()),(Integer)GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().spiner.getValue());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		analysisMenu.add(invSimul);

		
		mctMenu = new JMenu("MCT Files");
		mctMenu.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		mctMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		analysisMenu.add(mctMenu);
		
		JMenuItem mctItem = new JMenuItem("MCT");
		mctItem.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		mctItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					//GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().group.getSelection().getActionCommand()),(Integer)GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().spiner.getValue());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		mctMenu.add(mctItem);
		
	}

	/**
	 * Metoda odpowiedzialna za dodanie do menu zakładek w Windows->Project... nazwy nowo utworzonego arkusza.
	 * @param dockableItem Dockable - obiekt menu do osadzenia.
	 */
	public void addSheetItem(Dockable dockableItem) {
		dockables.add(dockableItem);
		DockableMenuItem menuItem = new DockableMenuItem(dockableItem, null);
		sheetItems.add(menuItem);
		sheetsMenu.add(menuItem);
	}

	/**
	 * Metoda odpowiedzialna za usuwanie z menu Windows->Project wpisu o arkuszu.
	 * @param dockableItem Dockable - obiekt menu do usunięcia
	 */
	public void deleteSheetItem(Dockable dockableItem) {
		int index = dockables.indexOf(dockableItem);
		dockables.remove(index);
		sheetItems.remove(index);
		if (index > 0)
			sheetsMenu.remove(index);
	}
}
