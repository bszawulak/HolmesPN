package abyss.darkgui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import abyss.darkgui.dockable.DockableMenuItem;
import abyss.utilities.Tools;

import com.javadocking.dockable.Dockable;

/**
 * Klasa implementująca metody tworzenia i obsługi głównego menu programu.
 * @author students
 * @author MR (2015: mostly)
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
	//private JMenu clustersOperationsMenu;
	private JMenu sheetsMenu;

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
		projectMenuItem.setEnabled(true);
		projectMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_newProject.png"));
		projectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
		projectMenuItem.getAccessibleContext().setAccessibleDescription("New project");
		projectMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().reset.newProjectInitiated();
			}
		});
		fileMenu.add(projectMenuItem);

		// The New Tab for File
		JMenuItem sheetMenuItem = new JMenuItem("New Sheet", KeyEvent.VK_T);
		sheetMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_newTab.png"));
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
		openMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_open.png"));
		//openMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		openMenuItem.getAccessibleContext().setAccessibleDescription("Open project");
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.io.openAbyssProject();
			}
		});
		fileMenu.add(openMenuItem);
		
		// import from file
		JMenuItem importMenuItem = new JMenuItem("Import network...", KeyEvent.VK_I);
		importMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_importNet.png"));
		//importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,ActionEvent.CTRL_MASK));
		importMenuItem.getAccessibleContext().setAccessibleDescription("Import project");
		importMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.io.importProject();
			}
		});
		fileMenu.add(importMenuItem);
		
		fileMenu.addSeparator();
		
		// save file
		JMenuItem saveMenuItem = new JMenuItem("Save...", KeyEvent.VK_S);
		saveMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_save.png"));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.io.saveAsAbyssFile();
			}
		});
		fileMenu.add(saveMenuItem);
		
		// saveAs file
		JMenuItem saveAsMenuItem = new JMenuItem("Save As...", KeyEvent.VK_S);
		saveAsMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_saveAs.png"));
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveAsMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.io.saveAsGlobal();
			}
		});
		fileMenu.add(saveAsMenuItem);

		// Export net as .pnt file
		JMenuItem exportMenuItem = new JMenuItem("Export as PNT...", KeyEvent.VK_E);
		exportMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportNet.png"));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK));
		exportMenuItem.getAccessibleContext().setAccessibleDescription("Export project to PNT file");
		exportMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.io.exportAsPNT();
			}
		});
		fileMenu.add(exportMenuItem);

		// Export image file
		JMenuItem expImgMenuItem = new JMenuItem("Export to image...", KeyEvent.VK_E);
		expImgMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportPicture.png"));
		expImgMenuItem.getAccessibleContext().setAccessibleDescription("Export project to image");
		expImgMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.io.exportProjectToImage();
			}
		});
		fileMenu.add(expImgMenuItem);
		
		fileMenu.addSeparator();

		// Exit program Item
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		exitMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
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
		windowMenu = new JMenu("Windows");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		windowMenu.getAccessibleContext().setAccessibleDescription("The Window Menu");
		this.add(windowMenu);
		
		sheetsMenu = new JMenu("Project");
		sheetsMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_Sheets.png"));
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
				Tools.getResIcon32("/icons/menu/menu_WindowTools.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getPropertiesBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowProperties.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getSimulatorBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowSimulator.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getInvariantsBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowsAnalysis.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getMctBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowMCT.png")));
		
		windowMenu.addSeparator();
		
		JMenuItem consoleItem = new JMenuItem("Log Console", KeyEvent.VK_1);
		consoleItem.setIcon(Tools.getResIcon32("/icons/menu/menu_console.png"));
		consoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		consoleItem.getAccessibleContext().setAccessibleDescription("Show Abyss log window");
		consoleItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showConsole(true);
			}
		});
		windowMenu.add(consoleItem);
		
		JMenuItem propertiesItem = new JMenuItem("Properties", KeyEvent.VK_1);
		propertiesItem.setIcon(Tools.getResIcon32("/icons/menu/menu_properties.png"));
		propertiesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		propertiesItem.getAccessibleContext().setAccessibleDescription("Show Abyss properties window");
		propertiesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showPropertiesWindow();
			}
		});
		windowMenu.add(propertiesItem);
		
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
		propItem.setIcon(Tools.getResIcon32("/icons/menu/menu_NetProp.png"));
		propItem.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		propItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		propItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showNetPropertiesWindow();
			}
		});
		netMenu.add(propItem);
		
		JMenuItem searchItem = new JMenuItem("Search node...", KeyEvent.VK_2);
		searchItem.setIcon(Tools.getResIcon32("/icons/menu/menu_search.png"));
		searchItem.setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		searchItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		searchItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showSearchWindow();
			}
		});
		netMenu.add(searchItem);
		
		JMenuItem netTablesItem = new JMenuItem("Net data tables", KeyEvent.VK_4);
		netTablesItem.setIcon(Tools.getResIcon32("/icons/menu/menu_netTables.png"));
		netTablesItem.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		netTablesItem.getAccessibleContext().setAccessibleDescription("Show net data tables window");
		netTablesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showNetTablesWindow();
			}
		});
		netMenu.add(netTablesItem);
		
		JMenu texSubMenu = new JMenu("Tex Export");
		texSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		texSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		netMenu.add(texSubMenu);
		
		JMenuItem exportTexPTItem = new JMenuItem("Places and transitions table...", KeyEvent.VK_1);
		exportTexPTItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		exportTexPTItem.getAccessibleContext().setAccessibleDescription("Export places and transitions tables");
		exportTexPTItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().tex.writePlacesTransitions();
			}
		});
		texSubMenu.add(exportTexPTItem);	
		
		JMenuItem exportTexInvItem = new JMenuItem("Invariants table...", KeyEvent.VK_2);
		exportTexInvItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		exportTexInvItem.getAccessibleContext().setAccessibleDescription("Export invariants into table");
		exportTexInvItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().tex.writeInvariants();
			}
		});
		texSubMenu.add(exportTexInvItem);	
		
		JMenuItem exportTexMCTItem = new JMenuItem("MCT table...", KeyEvent.VK_3);
		exportTexMCTItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		exportTexMCTItem.getAccessibleContext().setAccessibleDescription("Export MCT table");
		exportTexMCTItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().tex.writeMCT();
			}
		});
		texSubMenu.add(exportTexMCTItem);	
		
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  ANALYSIS MENU  *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
			
		
		//INVARIANTS MENU
		invariantsOperationsMenu = new JMenu("Analysis");
		invariantsOperationsMenu.setMnemonic(KeyEvent.VK_I);
		invariantsOperationsMenu.getAccessibleContext().setAccessibleDescription("Invariants - Menu");
		this.add(invariantsOperationsMenu);
		
		// Invariants window
		JMenuItem invWindowItem = new JMenuItem("Invariants generator...", KeyEvent.VK_1);
		invWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_analysis_invariants.png"));
		invWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		invWindowItem.getAccessibleContext().setAccessibleDescription("Invariants generator and tools");
		invWindowItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showInvariantsWindow();
			}
		});
		invariantsOperationsMenu.add(invWindowItem);
		
		// MCS window
		JMenuItem mcsWindowItem = new JMenuItem("Minimal Cutting Sets...", KeyEvent.VK_2);
		mcsWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_analysis_MCS.png"));
		mcsWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		mcsWindowItem.getAccessibleContext().setAccessibleDescription("MCS generator and tools");
		mcsWindowItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showMCSWindow();
			}
		});
		invariantsOperationsMenu.add(mcsWindowItem);
		
		// Knockout window
		JMenuItem knockoutWindowItem = new JMenuItem("Knockout analysis...", KeyEvent.VK_3);
		knockoutWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		knockoutWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		knockoutWindowItem.getAccessibleContext().setAccessibleDescription("Knockout analysis tools");
		knockoutWindowItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().createKnockoutWindow();
				GUIManager.getDefaultGUIManager().showKnockoutWindow();
			}
		});
		invariantsOperationsMenu.add(knockoutWindowItem);
		
		JMenuItem showClustersItem = new JMenuItem("Cluster analysis...", KeyEvent.VK_4);
		showClustersItem.setIcon(Tools.getResIcon32("/icons/menu/menu_ClustersAnalysis.png"));
		showClustersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
		showClustersItem.getAccessibleContext().setAccessibleDescription("Show clusters window");
		showClustersItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showClusterWindow(); 
			}
		});
		invariantsOperationsMenu.add(showClustersItem);
		
		JMenuItem netSimItem = new JMenuItem("State Simulator...", KeyEvent.VK_5);
		netSimItem.setIcon(Tools.getResIcon32("/icons/menu/menu_stateSim.png"));
		netSimItem.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		netSimItem.getAccessibleContext().setAccessibleDescription("Show state simulator window");
		netSimItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showStateSimulatorWindow();
			}
		});
		invariantsOperationsMenu.add(netSimItem);
		
		// The JMenuItem for invariants simulation
		JMenuItem invSimul = new JMenuItem("Invariants Simulation", KeyEvent.VK_6);
		invSimul.setIcon(Tools.getResIcon32("/icons/menu/menu_invSim.png"));
		//invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		invSimul.getAccessibleContext().setAccessibleDescription("Start Invariants Simulation");
		invSimul.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// poniższa 'linia':   ╯°□°）╯︵  ┻━━┻
					/*
					GUIManager.getDefaultGUIManager().startInvariantsSimulation(
						Integer.valueOf(GUIManager.getDefaultGUIManager().getInvSimBox().getCurrentDockWindow().group.getSelection().getActionCommand())
						,(Integer)GUIManager.getDefaultGUIManager().getInvSimBox().getCurrentDockWindow().spiner.getValue());
					*/
					JOptionPane.showMessageDialog(null, "Not implemented yet.", 
							"Subsystem offline", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
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
		genMCTGroups.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		genMCTGroups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,ActionEvent.CTRL_MASK));
		genMCTGroups.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		genMCTGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().generateMCT();
			}
		});
		mctOperationsMenu.add(genMCTGroups);
		
		// The JMenuItem for simple MCT file
		JMenuItem createMCTFile = new JMenuItem("Create simple MCT file", KeyEvent.VK_2);
		createMCTFile.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//createMCTFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		createMCTFile.getAccessibleContext().setAccessibleDescription("Create simple MCT file");
		createMCTFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().io.generateSimpleMCTFile();
			}
		});
		mctOperationsMenu.add(createMCTFile);

		//mct sub menu
		JMenu mctSubMenu = new JMenu("MCT Files");
		mctSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		mctSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		mctOperationsMenu.add(mctSubMenu);
		
		JMenuItem mctItem = new JMenuItem("MCT", KeyEvent.VK_3);
		mctItem.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//mctItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
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
		/*
		clustersOperationsMenu = new JMenu("Clusters");
		clustersOperationsMenu.setMnemonic(KeyEvent.VK_C);
		clustersOperationsMenu.getAccessibleContext().setAccessibleDescription("Clusters menu");
		this.add(clustersOperationsMenu);
		
		JMenuItem showClustersItem = new JMenuItem("Show clusters window", KeyEvent.VK_1);
		showClustersItem.setIcon(Tools.getResIcon32("/icons/menu/menu_ClustersAnalysis.png"));
		showClustersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
		showClustersItem.getAccessibleContext().setAccessibleDescription("Show clusters window");
		showClustersItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GUIManager.getDefaultGUIManager().showClusterWindow(); 
			}
		});
		clustersOperationsMenu.add(showClustersItem);
		*/
		
		JMenu aboutMenu = new JMenu("Help");
		aboutMenu.setMnemonic(KeyEvent.VK_H);
		aboutMenu.getAccessibleContext().setAccessibleDescription("Help");
		this.add(aboutMenu);
		
		JMenuItem aboutItem = new JMenuItem("About Abyss IPNE");
		aboutItem.setIcon(Tools.getResIcon32("/icons/menu/menu_about.png"));
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
