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
import javax.swing.JOptionPane;
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
	private JMenu netMenu;
	private JMenu invariantsOperationsMenu;
	private JMenu mctOperationsMenu;
	private JMenu clustersOperationsMenu;
	
	//private JMenu analysisMenu;
	private JMenu sheetsMenu;
	//private JMenu mctMenu;
	//private JMenu invMenu;
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
		this.add(xMenu);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************    FILE MENU    *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		// Build the File menu.
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription("The File Menu");
		this.add(fileMenu);

		// New Project
		JMenuItem projectMenuItem = new JMenuItem("New Project",  KeyEvent.VK_N);
		//projectMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_newProject.png"));
		projectMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/menu/menu_newProject.png")));
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
		sheetMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/menu/menu_newTab.png")));
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
		
		// save file
		JMenuItem saveMenuItem = new JMenuItem("Save...", KeyEvent.VK_S);
		saveMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_save.png"));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.saveAsAbyssFile();  //test
			}
		});
		fileMenu.add(saveMenuItem);

		// Export net as .pnt file
		JMenuItem exportMenuItem = new JMenuItem("Export network...", KeyEvent.VK_E);
		exportMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_exportNet.png"));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK));
		exportMenuItem.getAccessibleContext().setAccessibleDescription("Export project");
		exportMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.exportAsPNT();
			}
		});
		fileMenu.add(exportMenuItem);

		// Export image file
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

		// Exit program Item
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		exitMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.ALT_MASK));
		exitMenuItem.getAccessibleContext().setAccessibleDescription("Exit the application");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(null, "Are you sure you want to close the program?", "Really Closing?", 
			            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
			        	{
			            	GUIManager.getDefaultGUIManager().getConsoleWindow().saveLogToFile(null);
			            	System.exit(0);
			        	}
			}
			
		});
		fileMenu.add(exitMenuItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  WINDOWS MENU   *****************************************
		//***********************************                 *****************************************
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
		//***********************************                 *****************************************
		//***********************************  NET PROPERTIES *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		netMenu = new JMenu("Net");
		netMenu.setMnemonic(KeyEvent.VK_N);
		netMenu.getAccessibleContext().setAccessibleDescription("Net menu");
		this.add(netMenu);
		
		// Net properties
		JMenuItem propItem = new JMenuItem("Net properties", KeyEvent.VK_1);
		propItem.setPreferredSize(new Dimension(150, 38));
		//propItem.setIcon(new ImageIcon("resources/icons/menu/menu_INA_invGen.png"));
		propItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		propItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		propItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showNetPropertiesWindow();
				//GUIManager.getDefaultGUIManager().showNetworkProperties();
				//GUIManager.getDefaultGUIManager().generateINAinvariants();
			}
		});
		netMenu.add(propItem);	
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//*********************************** INVARIANTS MENU *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
			
		
		//INVARIANTS MENU
		invariantsOperationsMenu = new JMenu("Invariants");
		invariantsOperationsMenu.setMnemonic(KeyEvent.VK_I);
		invariantsOperationsMenu.getAccessibleContext().setAccessibleDescription("Invariants - Menu");
		this.add(invariantsOperationsMenu);

		// The JMenuItem for INA generator
		JMenuItem genINAinvItem = new JMenuItem("Generate INA invariants", KeyEvent.VK_1);
		genINAinvItem.setIcon(new ImageIcon("resources/icons/menu/menu_INA_invGen.png"));
		genINAinvItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		genINAinvItem.getAccessibleContext().setAccessibleDescription("Generate invariants using INA external program");
		genINAinvItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateINAinvariants();
			}
		});
		invariantsOperationsMenu.add(genINAinvItem);		

		// The JMenuItem for external analysis
		JMenuItem importInvMenuItem = new JMenuItem("Import invariants", KeyEvent.VK_1);
		importInvMenuItem.setIcon(new ImageIcon("resources/icons/menu/menu_InvImport.png"));
		importInvMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		importInvMenuItem.getAccessibleContext().setAccessibleDescription("Load analysis result from external source");
		importInvMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().loadExternalAnalysis();
			}
		});
		invariantsOperationsMenu.add(importInvMenuItem);
		
		// The JMenuItem for exportInvariants
		JMenuItem exportInv = new JMenuItem("Export invariants", KeyEvent.VK_2);
		exportInv.setIcon(new ImageIcon("resources/icons/menu/menu_InvExp.png"));
		exportInv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,ActionEvent.ALT_MASK));
		exportInv.getAccessibleContext().setAccessibleDescription("Export generated invariants");
		exportInv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().exportGeneratedInvariants();
			}
		});
		invariantsOperationsMenu.add(exportInv);
		
		// The JMenuItem for external analysis
		JMenuItem genTInvItem = new JMenuItem("Generate and analyze T-invariants", KeyEvent.VK_3);
		genTInvItem.setIcon(new ImageIcon("resources/icons/menu/menu_InvGen.png"));
		genTInvItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,ActionEvent.ALT_MASK));
		genTInvItem.getAccessibleContext().setAccessibleDescription("Generate Invariants");
		genTInvItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().tInvariantsAnalyse();
			}
		});
		invariantsOperationsMenu.add(genTInvItem);
		
		// The JMenuItem for invariants simulation
		JMenuItem invSimul = new JMenuItem("Start Invariants Simulation", KeyEvent.VK_4);
		invSimul.setIcon(new ImageIcon("resources/icons/menu/menu_invSim.png"));
		invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		invSimul.getAccessibleContext().setAccessibleDescription("Start Invariants Simulation");
		invSimul.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// poniższa linia:   ╯°□°）╯︵  ┻━━┻
					GUIManager.getDefaultGUIManager().startInvariantsSimulation(
						Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox()
								.getCurrentDockWindow().group.getSelection().getActionCommand())
						,(Integer)GUIManager.getDefaultGUIManager().getInvSimBox()
								.getCurrentDockWindow().spiner.getValue());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
				}
			}
		});
		invariantsOperationsMenu.add(invSimul);

		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************    MCT MENU     *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		//MCT MENU
				
		mctOperationsMenu = new JMenu("MCT");
		mctOperationsMenu.setMnemonic(KeyEvent.VK_M);
		mctOperationsMenu.getAccessibleContext().setAccessibleDescription("MCT Operations menu");
		this.add(mctOperationsMenu);
		
		// The JMenuItem for external analysis
		JMenuItem genMCTGroups = new JMenuItem("Generate MCT Groups", KeyEvent.VK_1);
		genMCTGroups.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		genMCTGroups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		genMCTGroups.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		genMCTGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateMCT();
			}
		});
		mctOperationsMenu.add(genMCTGroups);
		
		// The JMenuItem for simple MCT file
		JMenuItem createMCTFile = new JMenuItem("Create simple MCT file", KeyEvent.VK_2);
		createMCTFile.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		createMCTFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		createMCTFile.getAccessibleContext().setAccessibleDescription("Create simple MCT file");
		createMCTFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateSimpleMCTFile();
			}
		});
		mctOperationsMenu.add(createMCTFile);

		//mct sub menu
		JMenu mctSubMenu = new JMenu("MCT Files");
		mctSubMenu.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		mctSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		mctOperationsMenu.add(mctSubMenu);
		
		JMenuItem mctItem = new JMenuItem("MCT", KeyEvent.VK_3);
		mctItem.setIcon(new ImageIcon("resources/icons/menu/menu_genMCT.png"));
		mctItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		mctItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					//TODO anything
					//GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().group.getSelection().getActionCommand()),(Integer)GUIManager.getDefaultGUIManager().getInvSimBox().getProperties().spiner.getValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mctItem.setEnabled(false);
		mctSubMenu.add(mctItem);
		
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  CLUSTERS MENU  *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		//CLUSTERS MENU
		
		clustersOperationsMenu = new JMenu("Clusters");
		clustersOperationsMenu.setMnemonic(KeyEvent.VK_C);
		clustersOperationsMenu.getAccessibleContext().setAccessibleDescription("Clusters menu");
		this.add(clustersOperationsMenu);
		
		JMenuItem showClustersItem = new JMenuItem("Show clusters window", KeyEvent.VK_1);
		showClustersItem.setIcon(new ImageIcon("resources/icons/menu/menu_InvariantsMenu.png"));
		showClustersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,ActionEvent.ALT_MASK));
		showClustersItem.getAccessibleContext().setAccessibleDescription("Show clusters window");
		showClustersItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showClusterWindow(); 
			}
		});
		clustersOperationsMenu.add(showClustersItem);
		
		
		
		JMenu aboutMenu = new JMenu("Help");
		aboutMenu.setMnemonic(KeyEvent.VK_H);
		aboutMenu.getAccessibleContext().setAccessibleDescription("Help");
		this.add(aboutMenu);
		
		JMenuItem aboutItem = new JMenuItem("About...");
		aboutItem.setIcon(new ImageIcon("resources/icons/menu/menu_InvGen.png"));
		aboutItem.getAccessibleContext().setAccessibleDescription("About the program");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().createAboutWindow();
			}
		});
		aboutMenu.add(aboutItem);
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
