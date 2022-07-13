package holmes.darkgui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import holmes.darkgui.dockable.DockableMenuItem;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.utilities.Tools;
import holmes.windows.HolmesInvariantsViewer;
import holmes.windows.HolmesMergeNets;
import holmes.windows.managers.HolmesStatesManager;

import com.javadocking.dockable.Dockable;

/**
 * Klasa implementująca metody tworzenia i obsługi głównego menu programu.
 * @author students - już by tej klasy nie poznali...
 * @author MR (2015: totalna rearanżacja)
 *
 */
public class DarkMenu extends JMenuBar {
	@Serial
	private static final long serialVersionUID = -1671996309149490657L;

	// GUI
	private GUIManager guiManager;
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
		// menus
		JMenu fileMenu = new JMenu("File");
		
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription("The File Menu");
		this.add(fileMenu);

		// New Project
		JMenuItem projectMenuItem = new JMenuItem("New Project",  KeyEvent.VK_N);
		projectMenuItem.setEnabled(true);
		projectMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_newProject.png"));
		projectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		projectMenuItem.getAccessibleContext().setAccessibleDescription("New project");
		projectMenuItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().reset.newProjectInitiated());
		fileMenu.add(projectMenuItem);

		/*
		// The New Tab for File
		JMenuItem sheetMenuItem = new JMenuItem("New Sheet", KeyEvent.VK_T);
		sheetMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_newTab.png"));
		sheetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.ALT_MASK));
		sheetMenuItem.getAccessibleContext().setAccessibleDescription("Create a new sheet (tab) in workspace");
		sheetMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guiManager.getWorkspace().newTab(true);
			}
		});
		fileMenu.add(sheetMenuItem);
		 */
		
		fileMenu.addSeparator();
		
		// open file
		JMenuItem openMenuItem = new JMenuItem("Open project...", KeyEvent.VK_O);
		openMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_open.png"));
		openMenuItem.getAccessibleContext().setAccessibleDescription("Open project");
		openMenuItem.addActionListener(arg0 -> guiManager.io.selectAndOpenHolmesProject());
		fileMenu.add(openMenuItem);
		
		// import from file
		JMenuItem importMenuItem = new JMenuItem("Import network...", KeyEvent.VK_I);
		importMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_importNet.png"));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		importMenuItem.getAccessibleContext().setAccessibleDescription("Import project");
		importMenuItem.addActionListener(arg0 -> guiManager.io.importNetwork());
		fileMenu.add(importMenuItem);
		
		// merge from file
		JMenuItem mergeMenuItem = new JMenuItem("Merge nets...");
		mergeMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_MergeIcon.png"));
		//mergeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		mergeMenuItem.getAccessibleContext().setAccessibleDescription("Merge projects");
		mergeMenuItem.addActionListener(arg0 -> new HolmesMergeNets());
		fileMenu.add(mergeMenuItem);
		
		fileMenu.addSeparator();
		
		// save file
		JMenuItem saveMenuItem = new JMenuItem("Save project...", KeyEvent.VK_S);
		saveMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_save.png"));
		//saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveMenuItem.addActionListener(arg0 -> guiManager.io.saveAsAbyssFile());
		fileMenu.add(saveMenuItem);
		
		// saveAs file
		JMenuItem saveAsMenuItem = new JMenuItem("Export network...", KeyEvent.VK_S);
		saveAsMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_saveAs.png"));
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveAsMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveAsMenuItem.addActionListener(arg0 -> guiManager.io.saveAsGlobal());
		fileMenu.add(saveAsMenuItem);

		// Export net as .pnt file
		JMenuItem exportMenuItem = new JMenuItem("Export as PNT...", KeyEvent.VK_E);
		exportMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportNet.png"));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.CTRL_MASK));
		exportMenuItem.getAccessibleContext().setAccessibleDescription("Export project to PNT file");
		exportMenuItem.addActionListener(arg0 -> guiManager.io.exportAsPNT());
		fileMenu.add(exportMenuItem);

		// Export image file
		JMenuItem expImgMenuItem = new JMenuItem("Export to image...", KeyEvent.VK_E);
		expImgMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportPicture.png"));
		expImgMenuItem.getAccessibleContext().setAccessibleDescription("Export project to image");
		expImgMenuItem.addActionListener(arg0 -> guiManager.io.exportProjectToImage());
		fileMenu.add(expImgMenuItem);
		
		JMenu texSubMenu = new JMenu("Tex Export");
		texSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		texSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		fileMenu.add(texSubMenu);
		
		JMenuItem exportTexPTItem = new JMenuItem("Places and transitions table...", KeyEvent.VK_1);
		exportTexPTItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		exportTexPTItem.getAccessibleContext().setAccessibleDescription("Export places and transitions tables");
		exportTexPTItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().tex.writePlacesTransitions());
		texSubMenu.add(exportTexPTItem);	
		
		JMenuItem exportTexInvItem = new JMenuItem("Invariants table...", KeyEvent.VK_2);
		exportTexInvItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		exportTexInvItem.getAccessibleContext().setAccessibleDescription("Export invariants into table");
		exportTexInvItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().tex.writeInvariants());
		texSubMenu.add(exportTexInvItem);	
		
		JMenuItem exportTexMCTItem = new JMenuItem("MCT table...", KeyEvent.VK_3);
		exportTexMCTItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		exportTexMCTItem.getAccessibleContext().setAccessibleDescription("Export MCT table");
		exportTexMCTItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().tex.writeMCT());
		texSubMenu.add(exportTexMCTItem);	
		
		fileMenu.addSeparator();

		// Exit program Item
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		exitMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
		exitMenuItem.getAccessibleContext().setAccessibleDescription("Exit the application");
		exitMenuItem.addActionListener(arg0 -> {
			if (JOptionPane.showConfirmDialog(null, "Are you sure you want to close the program?", "Really Closing?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					{
						GUIManager.getDefaultGUIManager().getConsoleWindow().saveLogToFile(null);
						System.exit(0);
					}
		});
		fileMenu.add(exitMenuItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  WINDOWS MENU   *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		// Build the Window menu.
		JMenu windowMenu = new JMenu("Windows");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		windowMenu.getAccessibleContext().setAccessibleDescription("The Window Menu");
		this.add(windowMenu);
		
		sheetsMenu = new JMenu("Project");
		sheetsMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_Sheets.png"));
		sheetsMenu.setMnemonic(KeyEvent.VK_P);
		sheetsMenu.getAccessibleContext().setAccessibleDescription("The Project Sheets Menu");
		windowMenu.add(sheetsMenu);

		windowMenu.addSeparator();
		
		windowMenu.add(new DockableMenuItem(guiManager.getToolBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowTools.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getPropertiesBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowProperties.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getSimulatorBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowSimulator.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getT_invBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowsAnalysis.png")));
		windowMenu.add(new DockableMenuItem(guiManager.getMctBox().getDockable(),
				Tools.getResIcon32("/icons/menu/menu_WindowMCT.png")));
		
		windowMenu.addSeparator();
		
		JMenuItem consoleItem = new JMenuItem("Log Console", KeyEvent.VK_1);
		consoleItem.setIcon(Tools.getResIcon32("/icons/menu/menu_console.png"));
		consoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		consoleItem.getAccessibleContext().setAccessibleDescription("Show Holmes log window");
		consoleItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showConsole(true));
		windowMenu.add(consoleItem);
		
		JMenuItem propertiesItem = new JMenuItem("Properties", KeyEvent.VK_1);
		propertiesItem.setIcon(Tools.getResIcon32("/icons/menu/menu_properties.png"));
		propertiesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		propertiesItem.getAccessibleContext().setAccessibleDescription("Show Holmes properties window");
		propertiesItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showPropertiesWindow());
		windowMenu.add(propertiesItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  NET PROPERTIES *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************

		JMenu netMenu = new JMenu("Net");
		netMenu.setMnemonic(KeyEvent.VK_N);
		netMenu.getAccessibleContext().setAccessibleDescription("Net menu");
		this.add(netMenu);
		
		// Net properties
		JMenuItem propItem = new JMenuItem("Net properties...", KeyEvent.VK_1);
		propItem.setIcon(Tools.getResIcon32("/icons/menu/menu_NetProp.png"));
		propItem.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		propItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		propItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showNetPropertiesWindow());
		netMenu.add(propItem);
		
		JMenuItem searchItem = new JMenuItem("Search node...", KeyEvent.VK_2);
		searchItem.setIcon(Tools.getResIcon32("/icons/menu/menu_search.png"));
		searchItem.setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		searchItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		searchItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showSearchWindow());
		netMenu.add(searchItem);
		
		JMenuItem netTablesItem = new JMenuItem("Net data tables...", KeyEvent.VK_4);
		netTablesItem.setIcon(Tools.getResIcon32("/icons/menu/menu_netTables.png"));
		netTablesItem.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		netTablesItem.getAccessibleContext().setAccessibleDescription("Show net data tables window");
		netTablesItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showNetTablesWindow());
		netMenu.add(netTablesItem);
		
		JMenuItem invViewItem = new JMenuItem("Invariants Viewer...", KeyEvent.VK_5);
		invViewItem.setIcon(Tools.getResIcon32("/icons/menu/menu_invViewer.png"));
		invViewItem.setAccelerator(KeyStroke.getKeyStroke('J', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		invViewItem.getAccessibleContext().setAccessibleDescription("Show invariants information window");
		invViewItem.addActionListener(arg0 -> new HolmesInvariantsViewer());
		netMenu.add(invViewItem);
		
		JMenuItem netStatessItem = new JMenuItem("Net m0 states...", KeyEvent.VK_6);
		netStatessItem.setIcon(Tools.getResIcon32("/icons/menu/menu_statesViewer.png"));
		netStatessItem.setAccelerator(KeyStroke.getKeyStroke('M', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		netStatessItem.getAccessibleContext().setAccessibleDescription("Show net data states tables window");
		netStatessItem.addActionListener(arg0 -> {
			if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, "Net simulator must be stopped in order to access state manager.",
						"Simulator working", JOptionPane.WARNING_MESSAGE);
			} else {
				new HolmesStatesManager();
			}
		});
		netMenu.add(netStatessItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  HIERARCHY MENU *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		// Build the File menu.
		JMenu hierachyMenu = new JMenu("Subnets");
		hierachyMenu.setMnemonic(KeyEvent.VK_F);
		hierachyMenu.getAccessibleContext().setAccessibleDescription("Subnets tools");
		this.add(hierachyMenu);
		
		// Invariants window
		JMenuItem collapseNetItem = new JMenuItem("Compress subnets", KeyEvent.VK_1);
		collapseNetItem.setIcon(Tools.getResIcon32("/icons/menu/menu_subnetCompress.png"));
		//collapseNetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		collapseNetItem.getAccessibleContext().setAccessibleDescription("Compress subnets (remove all empty sheets)");
		collapseNetItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().subnetsGraphics.collapseSubnets();
			GUIManager.getDefaultGUIManager().subnetsGraphics.resizePanels();
		});
		hierachyMenu.add(collapseNetItem);
		
		JMenuItem alignElementsItem = new JMenuItem("Align to upper left", KeyEvent.VK_1);
		alignElementsItem.setIcon(Tools.getResIcon32("/icons/menu/menu_subnetAlignUpLeft.png"));
		//alignElementsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		alignElementsItem.getAccessibleContext().setAccessibleDescription("All net elements will be aligned to left upper corner");
		alignElementsItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().subnetsGraphics.realignElements());
		hierachyMenu.add(alignElementsItem);
		
		JMenuItem resizePanelsItem = new JMenuItem("Resize panels", KeyEvent.VK_1);
		resizePanelsItem.setIcon(Tools.getResIcon32("/icons/menu/menu_subnetResize.png"));
		//alignElementsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		resizePanelsItem.getAccessibleContext().setAccessibleDescription("Resize all panels to better fit the contained subnet");
		resizePanelsItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().subnetsGraphics.resizePanels());
		hierachyMenu.add(resizePanelsItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  ANALYSIS MENU  *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
			
		
		//INVARIANTS MENU
		JMenu analysisMenu = new JMenu("Analysis");
		analysisMenu.setMnemonic(KeyEvent.VK_I);
		analysisMenu.getAccessibleContext().setAccessibleDescription("Net analysis");
		this.add(analysisMenu);
		
		// Invariants window
		JMenuItem invWindowItem = new JMenuItem("Invariants generator...", KeyEvent.VK_1);
		invWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_analysis_invariants.png"));
		invWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		invWindowItem.getAccessibleContext().setAccessibleDescription("Invariants generator and tools");
		invWindowItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showInvariantsWindow());
		analysisMenu.add(invWindowItem);
		
		// MCS window
		JMenuItem mcsWindowItem = new JMenuItem("Minimal Cutting Sets...", KeyEvent.VK_2);
		mcsWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_analysis_MCS.png"));
		mcsWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		mcsWindowItem.getAccessibleContext().setAccessibleDescription("MCS generator and tools");
		mcsWindowItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showMCSWindow());
		analysisMenu.add(mcsWindowItem);
		
		// Knockout window
		JMenuItem knockoutWindowItem = new JMenuItem("Knockout analysis...", KeyEvent.VK_3);
		knockoutWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		knockoutWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		knockoutWindowItem.getAccessibleContext().setAccessibleDescription("Knockout analysis tools");
		knockoutWindowItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().createKnockoutWindow();
			GUIManager.getDefaultGUIManager().showKnockoutWindow();
		});
		analysisMenu.add(knockoutWindowItem);
		
		JMenuItem showClustersItem = new JMenuItem("Cluster analysis...", KeyEvent.VK_4);
		showClustersItem.setIcon(Tools.getResIcon32("/icons/menu/menu_ClustersAnalysis.png"));
		showClustersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
		showClustersItem.getAccessibleContext().setAccessibleDescription("Show clusters window");
		showClustersItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showClusterWindow());
		analysisMenu.add(showClustersItem);
		
		JMenuItem netSimItem = new JMenuItem("State Simulator...", KeyEvent.VK_5);
		netSimItem.setIcon(Tools.getResIcon32("/icons/menu/menu_stateSim.png"));
		netSimItem.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		netSimItem.getAccessibleContext().setAccessibleDescription("Show state simulator window");
		netSimItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().showStateSimulatorWindow());
		analysisMenu.add(netSimItem);

		// Knockout window
		JMenuItem decoWindowItem = new JMenuItem("Decomposition analysis...", KeyEvent.VK_6);
		decoWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		decoWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		decoWindowItem.getAccessibleContext().setAccessibleDescription("Decomposition analysis tools");
		decoWindowItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().createDecompositionWindow();
			GUIManager.getDefaultGUIManager().showDecoWindow();
		});
		analysisMenu.add(decoWindowItem);

		// Branch window
		JMenuItem branchWindowItem = new JMenuItem("Branch analysis...", KeyEvent.VK_7);
		branchWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		branchWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		branchWindowItem.getAccessibleContext().setAccessibleDescription("Branch prototype");
		branchWindowItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().createBranchWindow();
			GUIManager.getDefaultGUIManager().showBranchWindow();
		});
		analysisMenu.add(branchWindowItem);

		// Comparison window
		JMenuItem compWindowItem = new JMenuItem("Net comparison", KeyEvent.VK_8);
		compWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		compWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		compWindowItem.getAccessibleContext().setAccessibleDescription("Comparison prototype");
		compWindowItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().createComparisonnWindow();
			GUIManager.getDefaultGUIManager().showCompWindow();
		});
		analysisMenu.add(compWindowItem);

		// Knockout window
		JMenuItem graphletWindowItem = new JMenuItem("Graphlets", KeyEvent.VK_0);
		graphletWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		graphletWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		graphletWindowItem.getAccessibleContext().setAccessibleDescription("Decomposition analysis tools");
		graphletWindowItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().createGraphletsWindow();
			GUIManager.getDefaultGUIManager().showGraphletsWindow();
		});
		analysisMenu.add(graphletWindowItem);


		// reduction window
		JMenuItem reductionWindowItem = new JMenuItem("Reduction", KeyEvent.VK_0);
		reductionWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		reductionWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		reductionWindowItem.getAccessibleContext().setAccessibleDescription("Decomposition analysis tools");
		reductionWindowItem.addActionListener(arg0 -> {
			GUIManager.getDefaultGUIManager().createReductionWindow();
			GUIManager.getDefaultGUIManager().showReductionsWindow();
		});
		analysisMenu.add(reductionWindowItem);


		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************    OTHER MENU   *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************

		JMenu otherMenu = new JMenu("Other");
		otherMenu.setMnemonic(KeyEvent.VK_O);
		otherMenu.getAccessibleContext().setAccessibleDescription("Other, unchecked or under-construction methods");
		this.add(otherMenu);

		JMenu mctOperationsMenu = new JMenu("MCT");
		mctOperationsMenu.setMnemonic(KeyEvent.VK_M);
		mctOperationsMenu.getAccessibleContext().setAccessibleDescription("MCT Operations menu");
		mctOperationsMenu.setPreferredSize(new Dimension(WIDTH, 38));
		mctOperationsMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		otherMenu.add(mctOperationsMenu);

		
		JMenuItem genMCTGroups = new JMenuItem("Generate MCT Groups", KeyEvent.VK_1);
		genMCTGroups.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//genMCTGroups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,ActionEvent.CTRL_MASK));
		genMCTGroups.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		genMCTGroups.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().generateMCT());
		mctOperationsMenu.add(genMCTGroups);
		
		// The JMenuItem for simple MCT file
		JMenuItem createMCTFile = new JMenuItem("Create simple MCT file", KeyEvent.VK_2);
		createMCTFile.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//createMCTFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		createMCTFile.getAccessibleContext().setAccessibleDescription("Create simple MCT file");
		createMCTFile.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().io.generateSimpleMCTFile());
		mctOperationsMenu.add(createMCTFile);

		//mct sub menu
		JMenu mctSubMenu = new JMenu("MCT Files");
		mctSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		mctSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		mctOperationsMenu.add(mctSubMenu);
		
		JMenuItem mctItem = new JMenuItem("MCT", KeyEvent.VK_3);
		mctItem.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//mctItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		mctItem.addActionListener(arg0 -> {
			try {
				JOptionPane.showMessageDialog(null, "Not implemented yet.",
						"Subsystem offline", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		mctItem.setEnabled(false);
		mctSubMenu.add(mctItem);
		
		// The JMenuItem for invariants simulation
		JMenuItem invSimul = new JMenuItem("Invariants Simulation", KeyEvent.VK_6);
		invSimul.setIcon(Tools.getResIcon32("/icons/menu/menu_invSim.png"));
		//invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		invSimul.getAccessibleContext().setAccessibleDescription("Start Invariants Simulation");
		invSimul.addActionListener(arg0 -> {
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
		});
		otherMenu.add(invSimul);
		
		JMenuItem fixNet = new JMenuItem("FixArcs", KeyEvent.VK_7);
		fixNet.setIcon(Tools.getResIcon32("/icons/menu/aaa.png"));
		//invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		fixNet.getAccessibleContext().setAccessibleDescription("Fix arc problems");
		fixNet.addActionListener(arg0 -> {
			try {
				// poniższa 'linia':   ╯°□°）╯︵  ┻━━┻
				GUIManager.getDefaultGUIManager().io.fixArcsProblem();
			} catch (Exception e) {
				e.printStackTrace();
				GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
			}
		});
		//otherMenu.add(fixNet);
		
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
		
		JMenuItem aboutItem = new JMenuItem("About Holmes");
		aboutItem.setIcon(Tools.getResIcon32("/icons/menu/menu_about.png"));
		aboutItem.getAccessibleContext().setAccessibleDescription("About the program");
		aboutItem.addActionListener(arg0 -> GUIManager.getDefaultGUIManager().createAboutWindow());
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
		if(index == -1)
			return;
		
		dockables.remove(index);
		sheetItems.remove(index);
		if (index > 0)
			sheetsMenu.remove(index);
	}
}
