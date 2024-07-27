package holmes.darkgui;

import java.awt.Dimension;
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
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.utilities.Tools;
import holmes.windows.HolmesInvariantsViewer;
import holmes.windows.HolmesSubnetsInfo;
import holmes.windows.decompositions.HolmesMergeNets;
import holmes.windows.managers.HolmesSPNmanager;
import holmes.windows.managers.HolmesSSAwindowManager;
import holmes.windows.managers.HolmesStatesManager;


/**
 * Klasa implementująca metody tworzenia i obsługi głównego menu programu.
 */
public class DarkMenu extends JMenuBar {
	@Serial
	private static final long serialVersionUID = -1671996309149490657L;
	// GUI
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JMenu sheetsMenu;
	private ArrayList<DockableMenuItem> sheetItems;

	/**
	 * Konstruktor domyślny obiektu klasy DarkMenu.
	 */
	public DarkMenu() {
		sheetItems = new ArrayList<DockableMenuItem>();
		
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
		JMenu fileMenu = new JMenu(lang.getText("DM_menuFile"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.getAccessibleContext().setAccessibleDescription(lang.getText("DM_menuFileDesc"));
		this.add(fileMenu);

		// New Project
		JMenuItem projectMenuItem = new JMenuItem(lang.getText("DM_menuFileProject"),  KeyEvent.VK_N);
		projectMenuItem.setEnabled(true);
		projectMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_newProject.png"));
		projectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		//projectMenuItem.getAccessibleContext().setAccessibleDescription("Create new project");
		projectMenuItem.addActionListener(arg0 -> overlord.reset.newProjectInitiated());
		fileMenu.add(projectMenuItem);
		
		fileMenu.addSeparator();
		
		// open file
		JMenuItem openMenuItem = new JMenuItem(lang.getText("DM_menuFileOpenProject"), KeyEvent.VK_O);
		openMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_open.png"));
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		//openMenuItem.getAccessibleContext().setAccessibleDescription("Open project");
		openMenuItem.addActionListener(arg0 -> overlord.io.selectAndOpenHolmesProject());
		fileMenu.add(openMenuItem);
		
		// import from file
		JMenuItem importMenuItem = new JMenuItem(lang.getText("DM_menuFileImportNet"), KeyEvent.VK_I);
		importMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_importNet.png"));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		//importMenuItem.getAccessibleContext().setAccessibleDescription("Import project");
		importMenuItem.addActionListener(arg0 -> overlord.io.importNetwork());
		fileMenu.add(importMenuItem);
		
		// merge from file
		JMenuItem mergeMenuItem = new JMenuItem(lang.getText("DM_menuFileMerge"));
		mergeMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_MergeIcon.png"));
		//mergeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		//mergeMenuItem.getAccessibleContext().setAccessibleDescription("Merge projects");
		mergeMenuItem.addActionListener(arg0 -> new HolmesMergeNets());
		fileMenu.add(mergeMenuItem);
		
		fileMenu.addSeparator();
		
		// save file
		JMenuItem saveMenuItem = new JMenuItem(lang.getText("DM_menuFileSavePr"), KeyEvent.VK_S);
		saveMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_save.png"));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		//saveMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveMenuItem.addActionListener(arg0 -> overlord.io.saveAsAbyssFile());
		fileMenu.add(saveMenuItem);
		
		// saveAs file
		JMenuItem saveAsMenuItem = new JMenuItem(lang.getText("DM_menuFileExport"));
		saveAsMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_saveAs.png"));
		saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		//saveAsMenuItem.getAccessibleContext().setAccessibleDescription("Save project");
		saveAsMenuItem.addActionListener(arg0 -> overlord.io.saveAsGlobal());
		fileMenu.add(saveAsMenuItem);

		// Export net as .pnt file
		JMenuItem exportMenuItem = new JMenuItem(lang.getText("DM_menuFileExportPNT"));
		exportMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportNet.png"));
		//exportMenuItem.getAccessibleContext().setAccessibleDescription("Export project to PNT file");
		exportMenuItem.addActionListener(arg0 -> overlord.io.exportAsPNT());
		fileMenu.add(exportMenuItem);

		// Export image file
		JMenuItem expImgMenuItem = new JMenuItem(lang.getText("DM_menuFileExportImg"));
		expImgMenuItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportPicture.png"));
		expImgMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		//expImgMenuItem.getAccessibleContext().setAccessibleDescription("Export project to image");
		expImgMenuItem.addActionListener(arg0 -> overlord.io.exportProjectToImage());
		fileMenu.add(expImgMenuItem);
		
		JMenu texSubMenu = new JMenu(lang.getText("DM_menuFileExportTex"));
		texSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		//texSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		fileMenu.add(texSubMenu);
		
		JMenuItem exportTexPTItem = new JMenuItem(lang.getText("DM_menuFileExportTexPT"), KeyEvent.VK_1);
		exportTexPTItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		//exportTexPTItem.getAccessibleContext().setAccessibleDescription("Export places and transitions tables");
		exportTexPTItem.addActionListener(arg0 -> overlord.tex.writePlacesTransitions());
		texSubMenu.add(exportTexPTItem);	
		
		JMenuItem exportTexInvItem = new JMenuItem(lang.getText("DM_menuFileExportTexInv"), KeyEvent.VK_2);
		exportTexInvItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		//exportTexInvItem.getAccessibleContext().setAccessibleDescription("Export invariants into table");
		exportTexInvItem.addActionListener(arg0 -> overlord.tex.writeInvariants());
		texSubMenu.add(exportTexInvItem);	
		
		JMenuItem exportTexMCTItem = new JMenuItem(lang.getText("DM_menuFileExportTexMCT"), KeyEvent.VK_3);
		exportTexMCTItem.setIcon(Tools.getResIcon32("/icons/menu/menu_exportTex.png"));
		//exportTexMCTItem.getAccessibleContext().setAccessibleDescription("Export MCT table");
		exportTexMCTItem.addActionListener(arg0 -> overlord.tex.writeMCT());
		texSubMenu.add(exportTexMCTItem);	
		
		fileMenu.addSeparator();

		// Exit program Item
		JMenuItem exitMenuItem = new JMenuItem(lang.getText("DM_menuFileExit"), KeyEvent.VK_E);
		exitMenuItem.setPreferredSize(new Dimension(WIDTH, 38));
		//exitMenuItem.getAccessibleContext().setAccessibleDescription("Exit the application");
		exitMenuItem.addActionListener(arg0 -> {
			if (JOptionPane.showConfirmDialog(null, lang.getText("DM_menuFileExitQ1"), lang.getText("DM_menuFileExitQ2"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					{
						overlord.getConsoleWindow().saveLogToFile(null);
						System.exit(0);
					}
		});
		fileMenu.add(exitMenuItem);

		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************     VIEW MENU   *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************

		JMenu viewMenu = new JMenu(lang.getText("DM_menuView"));
		viewMenu.setMnemonic(KeyEvent.VK_V);
		//viewMenu.getAccessibleContext().setAccessibleDescription("View menu");
		this.add(viewMenu);

		// Net properties
		JMenuItem resetZoomItem = new JMenuItem(lang.getText("DM_menuViewZoom100"), KeyEvent.VK_1);
		resetZoomItem.setIcon(Tools.getResIcon32("/icons/menu/menu_zoom_reset.png"));
		resetZoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_DOWN_MASK));
		//resetZoomItem.getAccessibleContext().setAccessibleDescription("Reset zoom to 100%");
		resetZoomItem.addActionListener(arg0 -> {
			for(GraphPanel gp : overlord.getWorkspace().getProject().getGraphPanels()) {
				gp.setZoom(100, gp.getZoom());
			}
		});
		viewMenu.add(resetZoomItem);

		JMenuItem incZoomItem = new JMenuItem(lang.getText("DM_menuViewZoomIn"), KeyEvent.VK_2);
		incZoomItem.setIcon(Tools.getResIcon32("/icons/menu/menu_zoom_inc.png"));
		incZoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK));
		//incZoomItem.getAccessibleContext().setAccessibleDescription("Increase zoom 10%");
		incZoomItem.addActionListener(arg0 -> {
			for(GraphPanel gp : overlord.getWorkspace().getProject().getGraphPanels()) {
				gp.setZoom((int)(gp.getZoom() * 1.1), gp.getZoom());
			}
		});
		viewMenu.add(incZoomItem);

		JMenuItem decZoomItem = new JMenuItem(lang.getText("DM_menuViewZoomOut"), KeyEvent.VK_2);
		decZoomItem.setIcon(Tools.getResIcon32("/icons/menu/menu_zoom_dec.png"));
		decZoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_DOWN_MASK));
		//decZoomItem.getAccessibleContext().setAccessibleDescription("Decrease zoom 10%");
		decZoomItem.addActionListener(arg0 -> {
			for(GraphPanel gp : overlord.getWorkspace().getProject().getGraphPanels()) {
				gp.setZoom((int)(gp.getZoom() * 0.9), gp.getZoom());
			}
		});
		viewMenu.add(decZoomItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  WINDOWS MENU   *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		// Build the Window menu.
		JMenu windowMenu = new JMenu(lang.getText("DM_menuWindows"));
		windowMenu.setMnemonic(KeyEvent.VK_W);
		//windowMenu.getAccessibleContext().setAccessibleDescription("The Window Menu");
		this.add(windowMenu);
		
		sheetsMenu = new JMenu(lang.getText("DM_menuWindowsProject"));
		sheetsMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_Sheets.png"));
		sheetsMenu.setMnemonic(KeyEvent.VK_P);
		//sheetsMenu.getAccessibleContext().setAccessibleDescription("The Project Sheets Menu");
		windowMenu.add(sheetsMenu);

		windowMenu.addSeparator();

		//TODO przyciski
		/*
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
		*/
		
		windowMenu.addSeparator();
		
		JMenuItem consoleItem = new JMenuItem(lang.getText("DM_menuWindowsConsole"), KeyEvent.VK_1);
		consoleItem.setIcon(Tools.getResIcon32("/icons/menu/menu_console.png"));
		consoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		//consoleItem.getAccessibleContext().setAccessibleDescription("Show Holmes log window");
		consoleItem.addActionListener(arg0 -> overlord.showConsole(true));
		windowMenu.add(consoleItem);
		
		JMenuItem propertiesItem = new JMenuItem(lang.getText("DM_menuWindowsProperties"), KeyEvent.VK_1);
		propertiesItem.setIcon(Tools.getResIcon32("/icons/menu/menu_properties.png"));
		propertiesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		//propertiesItem.getAccessibleContext().setAccessibleDescription("Show Holmes properties window");
		propertiesItem.addActionListener(arg0 -> overlord.showPropertiesWindow());
		windowMenu.add(propertiesItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  NET PROPERTIES *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************

		JMenu netMenu = new JMenu(lang.getText("DM_menuNetData"));
		netMenu.setMnemonic(KeyEvent.VK_N);
		//netMenu.getAccessibleContext().setAccessibleDescription("Net menu");
		this.add(netMenu);
		
		// Net properties
		JMenuItem propItem = new JMenuItem(lang.getText("DM_menuNetDataProperties"), KeyEvent.VK_1);
		propItem.setIcon(Tools.getResIcon32("/icons/menu/menu_NetProp.png"));
		propItem.setAccelerator(KeyStroke.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK));
		//propItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		propItem.addActionListener(arg0 -> overlord.showNetPropertiesWindow());
		netMenu.add(propItem);
		
		JMenuItem searchItem = new JMenuItem(lang.getText("DM_menuNetDataSearch"), KeyEvent.VK_2);
		searchItem.setIcon(Tools.getResIcon32("/icons/menu/menu_search.png"));
		searchItem.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK));
		//searchItem.getAccessibleContext().setAccessibleDescription("Show net properties");
		searchItem.addActionListener(arg0 -> overlord.showSearchWindow());
		netMenu.add(searchItem);
		
		JMenuItem netTablesItem = new JMenuItem(lang.getText("DM_menuNetDataTable"), KeyEvent.VK_4);
		netTablesItem.setIcon(Tools.getResIcon32("/icons/menu/menu_netTables.png"));
		//netTablesItem.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK));
		//netTablesItem.getAccessibleContext().setAccessibleDescription("Show net data tables window");
		netTablesItem.addActionListener(arg0 -> overlord.showNetTablesWindow());
		netMenu.add(netTablesItem);
		
		JMenuItem invViewItem = new JMenuItem(lang.getText("DM_menuNetDataInvariants"), KeyEvent.VK_5);
		invViewItem.setIcon(Tools.getResIcon32("/icons/menu/menu_invViewer.png"));
		//invViewItem.setAccelerator(KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK));
		//invViewItem.getAccessibleContext().setAccessibleDescription("Show invariants information window");
		invViewItem.addActionListener(arg0 -> new HolmesInvariantsViewer());
		netMenu.add(invViewItem);
		
		JMenuItem netStatessItem = new JMenuItem(lang.getText("DM_menuNetDataPStateM"), KeyEvent.VK_6);
		netStatessItem.setIcon(Tools.getResIcon32("/icons/menu/menu_statesViewer.png"));
		netStatessItem.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK));
		//netStatessItem.getAccessibleContext().setAccessibleDescription("Show net data states tables window");
		netStatessItem.addActionListener(arg0 -> {
			if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, lang.getText("DM_menuNetDataPStateMQ1"),
						lang.getText("DM_menuNetDataPStateMQ2"), JOptionPane.WARNING_MESSAGE);
			} else {
				new HolmesStatesManager();
			}
		});
		netMenu.add(netStatessItem);

		JMenuItem netTransFreqItem = new JMenuItem(lang.getText("DM_menuNetDataSPNM"), KeyEvent.VK_6);
		netTransFreqItem.setIcon(Tools.getResIcon32("/icons/menu/menu_firingRates.png"));
		///icons/toolbar/firingRates.png
		netTransFreqItem.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
		//netTransFreqItem.getAccessibleContext().setAccessibleDescription("Show transitions firing rates manager window");
		netTransFreqItem.addActionListener(arg0 -> {
			if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, lang.getText("DM_menuNetDataPStateMQ1"),
						lang.getText("DM_menuNetDataPStateMQ2"), JOptionPane.WARNING_MESSAGE);
			} else {
				new HolmesSPNmanager(overlord.getFrame());
				//new HolmesStatesManager();
			}
		});
		netMenu.add(netTransFreqItem);

		JMenuItem netSSAmanagerItem = new JMenuItem(lang.getText("DM_menuNetDataSSA"), KeyEvent.VK_6);
		netSSAmanagerItem.setIcon(Tools.getResIcon32("/icons/menu/menu_SSAmanager.png"));
		///icons/toolbar/firingRates.png
		netSSAmanagerItem.setAccelerator(KeyStroke.getKeyStroke('U', InputEvent.CTRL_DOWN_MASK));
		//netSSAmanagerItem.getAccessibleContext().setAccessibleDescription("Show SSA values for places manager window");
		netSSAmanagerItem.addActionListener(arg0 -> {
			if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, lang.getText("DM_menuNetDataPStateMQ1"),
						lang.getText("DM_menuNetDataPStateMQ2"), JOptionPane.WARNING_MESSAGE);
			} else {
				new HolmesSSAwindowManager(overlord.getFrame());
			}
		});
		netMenu.add(netSSAmanagerItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  HIERARCHY MENU *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
		
		// Build the File menu.
		JMenu hierachyMenu = new JMenu(lang.getText("DM_menuSubnets"));
		//hierachyMenu.getAccessibleContext().setAccessibleDescription("Subnets tools");
		this.add(hierachyMenu);
		
		// Invariants window
		JMenuItem collapseNetItem = new JMenuItem(lang.getText("DM_menuSubnetsCompr"), KeyEvent.VK_1);
		collapseNetItem.setIcon(Tools.getResIcon32("/icons/menu/menu_subnetCompress.png"));
		//collapseNetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		//collapseNetItem.getAccessibleContext().setAccessibleDescription("Compress subnets (remove all empty sheets)");
		collapseNetItem.addActionListener(arg0 -> {
			overlord.subnetsGraphics.collapseSubnets();
			overlord.subnetsGraphics.resizePanels();
		});
		hierachyMenu.add(collapseNetItem);
		
		JMenuItem alignElementsItem = new JMenuItem(lang.getText("DM_menuSubnetsAlign"), KeyEvent.VK_1);
		alignElementsItem.setIcon(Tools.getResIcon32("/icons/menu/menu_subnetAlignUpLeft.png"));
		//alignElementsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		//alignElementsItem.getAccessibleContext().setAccessibleDescription("All net elements will be aligned to left upper corner");
		alignElementsItem.addActionListener(arg0 -> overlord.subnetsGraphics.realignElements());
		hierachyMenu.add(alignElementsItem);
		
		JMenuItem resizePanelsItem = new JMenuItem(lang.getText("DM_menuSubnetsResize"), KeyEvent.VK_1);
		resizePanelsItem.setIcon(Tools.getResIcon32("/icons/menu/menu_subnetResize.png"));
		//alignElementsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		//resizePanelsItem.getAccessibleContext().setAccessibleDescription("Resize all panels to better fit the contained subnet");
		resizePanelsItem.addActionListener(arg0 -> overlord.subnetsGraphics.resizePanels());
		hierachyMenu.add(resizePanelsItem);

		JMenuItem showDetailsItem = new JMenuItem(lang.getText("DM_menuSubnetsShowDet"), KeyEvent.VK_1);
		showDetailsItem.setIcon(Tools.getResIcon32("/icons/menu/menu_search.png"));
		showDetailsItem.getAccessibleContext().setAccessibleDescription("Show details about current subnet");
		showDetailsItem.addActionListener(arg0 -> HolmesSubnetsInfo.open());
		hierachyMenu.add(showDetailsItem);
		
		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************  ANALYSIS MENU  *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************
			
		
		//INVARIANTS MENU
		JMenu analysisMenu = new JMenu(lang.getText("DM_menuAnalysis"));
		analysisMenu.setMnemonic(KeyEvent.VK_A);
		//analysisMenu.getAccessibleContext().setAccessibleDescription("Net analysis");
		this.add(analysisMenu);

		JMenu invSubMenu = new JMenu(lang.getText("DM_menuAnalysisInvariants"));
		invSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_InvariantsMenu.png"));
		//invSubMenu.setMnemonic(KeyEvent.VK_P);
		//invSubMenu.getAccessibleContext().setAccessibleDescription("Invariants-based analysis");
		analysisMenu.add(invSubMenu);
		
		// Invariants window
		JMenuItem invWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisInvGen"), KeyEvent.VK_1);
		invWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_analysis_invariants.png"));
		invWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
		//invWindowItem.getAccessibleContext().setAccessibleDescription("Invariants generator and tools");
		invWindowItem.addActionListener(arg0 -> overlord.showInvariantsWindow());
		invSubMenu.add(invWindowItem);
		
		// MCS window
		JMenuItem mcsWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisMCS"), KeyEvent.VK_2);
		mcsWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_analysis_MCS.png"));
		mcsWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
		//mcsWindowItem.getAccessibleContext().setAccessibleDescription("MCS generator and tools");
		mcsWindowItem.addActionListener(arg0 -> overlord.showMCSWindow());
		invSubMenu.add(mcsWindowItem);
		
		// Knockout window
		JMenuItem knockoutWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisKnockout"), KeyEvent.VK_3);
		knockoutWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_knockout.png"));
		knockoutWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
		//knockoutWindowItem.getAccessibleContext().setAccessibleDescription("Knockout analysis tools");
		knockoutWindowItem.addActionListener(arg0 -> {
			overlord.createKnockoutWindow();
			overlord.showKnockoutWindow();
		});
		invSubMenu.add(knockoutWindowItem);
		
		JMenuItem showClustersItem = new JMenuItem(lang.getText("DM_menuAnalysisClusters"), KeyEvent.VK_4);
		showClustersItem.setIcon(Tools.getResIcon32("/icons/menu/menu_ClustersAnalysis.png"));
		showClustersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		//showClustersItem.getAccessibleContext().setAccessibleDescription("Show clusters window");
		showClustersItem.addActionListener(arg0 -> overlord.showClusterWindow());
		invSubMenu.add(showClustersItem);

		JMenu simulatorSubMenu = new JMenu(lang.getText("DM_menuAnalysisSim"));
		simulatorSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_simulators.png"));
		simulatorSubMenu.getAccessibleContext().setAccessibleDescription("Simulator");
		analysisMenu.add(simulatorSubMenu);
		
		JMenuItem netSimItem = new JMenuItem(lang.getText("DM_menuAnalysisStateSim"), KeyEvent.VK_5);
		netSimItem.setIcon(Tools.getResIcon32("/icons/menu/menu_stateSim.png"));
		netSimItem.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));
		//netSimItem.getAccessibleContext().setAccessibleDescription("Show state simulator window");
		netSimItem.addActionListener(arg0 -> overlord.showStateSimulatorWindow());
		simulatorSubMenu.add(netSimItem);

		JMenuItem netSimXTPNItem = new JMenuItem(lang.getText("DM_menuAnalysisXTPNSim"), KeyEvent.VK_5);
		netSimXTPNItem.setIcon(Tools.getResIcon32("/icons/menu/menu_XTPNsimulator.png"));
		netSimXTPNItem.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		//netSimXTPNItem.getAccessibleContext().setAccessibleDescription("Show state simulator window");
		netSimXTPNItem.addActionListener(arg0 -> overlord.showStateSimulatorWindowXTPN());
		simulatorSubMenu.add(netSimXTPNItem);

		JMenu decompSubMenu = new JMenu(lang.getText("DM_menuAnalysisNetDeco"));
		decompSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_decompSubmenu.png"));
		//decompSubMenu.getAccessibleContext().setAccessibleDescription("Decomposition modules for Petri nets");
		analysisMenu.add(decompSubMenu);


		JMenuItem decoWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisNetDecoA"), KeyEvent.VK_6);
		decoWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_decompModule.png"));
		decoWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		//decoWindowItem.getAccessibleContext().setAccessibleDescription("Decomposition analysis tools");
		decoWindowItem.addActionListener(arg0 -> {
			overlord.createDecompositionWindow();
			overlord.showDecoWindow();
		});
		decompSubMenu.add(decoWindowItem);

		// Branch window
		JMenuItem branchWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisNetDecoB"), KeyEvent.VK_7);
		branchWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_decompBranch.png"));
		branchWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		//branchWindowItem.getAccessibleContext().setAccessibleDescription("Branch prototype");
		branchWindowItem.addActionListener(arg0 -> {
			overlord.createBranchWindow();
			overlord.showBranchWindow();
		});
		decompSubMenu.add(branchWindowItem);

		// Comparison window
		JMenuItem compWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisNetDecoC"), KeyEvent.VK_8);
		compWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_decompNetComp.png"));
		compWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		compWindowItem.getAccessibleContext().setAccessibleDescription("Comparison prototype");
		compWindowItem.addActionListener(arg0 -> {
			overlord.createComparisonnWindow();
			overlord.showCompWindow();
		});
		decompSubMenu.add(compWindowItem);

		// Knockout window
		JMenuItem graphletWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisNetDecoD"), KeyEvent.VK_0);
		graphletWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_decompGraphlets.png"));
		graphletWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		//graphletWindowItem.getAccessibleContext().setAccessibleDescription("Decomposition analysis tools");
		graphletWindowItem.addActionListener(arg0 -> {
			overlord.createGraphletsWindow();
			overlord.showGraphletsWindow();
		});
		decompSubMenu.add(graphletWindowItem);


		// reduction window
		JMenuItem reductionWindowItem = new JMenuItem(lang.getText("DM_menuAnalysisNetDecoE"), KeyEvent.VK_0);
		reductionWindowItem.setIcon(Tools.getResIcon32("/icons/menu/menu_decompReduction.png"));
		reductionWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		//reductionWindowItem.getAccessibleContext().setAccessibleDescription("Decomposition analysis tools");
		reductionWindowItem.addActionListener(arg0 -> {
			overlord.createReductionWindow();
			overlord.showReductionsWindow();
		});
		decompSubMenu.add(reductionWindowItem);


		//*********************************************************************************************
		//***********************************                 *****************************************
		//***********************************    OTHER MENU   *****************************************
		//***********************************                 *****************************************
		//*********************************************************************************************

		JMenu otherMenu = new JMenu(lang.getText("DM_menuOther"));
		otherMenu.setMnemonic(KeyEvent.VK_O);
		//otherMenu.getAccessibleContext().setAccessibleDescription("Other, unchecked or under-construction methods");
		this.add(otherMenu);

		JMenu mctOperationsMenu = new JMenu(lang.getText("DM_menuOtherMCT"));
		mctOperationsMenu.setMnemonic(KeyEvent.VK_M);
		mctOperationsMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//mctOperationsMenu.getAccessibleContext().setAccessibleDescription("MCT Operations menu");
		mctOperationsMenu.setPreferredSize(new Dimension(WIDTH, 38));
		otherMenu.add(mctOperationsMenu);
		
		JMenuItem genMCTGroups = new JMenuItem(lang.getText("DM_menuOtherMCTGen"), KeyEvent.VK_1);
		genMCTGroups.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//genMCTGroups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,ActionEvent.CTRL_MASK));
		//genMCTGroups.getAccessibleContext().setAccessibleDescription("Generate MCT Groups");
		genMCTGroups.addActionListener(arg0 -> overlord.generateMCT());
		mctOperationsMenu.add(genMCTGroups);
		
		// The JMenuItem for simple MCT file
		JMenuItem createMCTFile = new JMenuItem(lang.getText("DM_menuOtherMCTCre"), KeyEvent.VK_2);
		createMCTFile.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//createMCTFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		//createMCTFile.getAccessibleContext().setAccessibleDescription("Create simple MCT file");
		createMCTFile.addActionListener(arg0 -> overlord.io.generateSimpleMCTFile());
		mctOperationsMenu.add(createMCTFile);

		//mct sub menu
		JMenu mctSubMenu = new JMenu(lang.getText("DM_menuOtherMCTFiles"));
		mctSubMenu.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		mctSubMenu.getAccessibleContext().setAccessibleDescription("MCT Generator");
		mctOperationsMenu.add(mctSubMenu);
		
		JMenuItem mctItem = new JMenuItem(lang.getText("DM_menuOtherMCTFilesA"), KeyEvent.VK_3);
		mctItem.setIcon(Tools.getResIcon32("/icons/menu/menu_genMCT.png"));
		//mctItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		mctItem.addActionListener(arg0 -> {
			try {
				JOptionPane.showMessageDialog(null, lang.getText("unimplemented"),
						lang.getText("unimplementedTitle"), JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ignored) {
			}
		});
		mctItem.setEnabled(false);
		mctSubMenu.add(mctItem);
		
		// The JMenuItem for invariants simulation
		JMenuItem invSimul = new JMenuItem(lang.getText("DM_menuOtherInvGen"), KeyEvent.VK_6);
		invSimul.setIcon(Tools.getResIcon32("/icons/menu/menu_invSim.png"));
		//invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		//invSimul.getAccessibleContext().setAccessibleDescription("Start Invariants Simulation");
		invSimul.addActionListener(arg0 -> {
			try {
				// poniższa 'linia':   ╯°□°）╯︵  ┻━━┻
				/*
				overlord.startInvariantsSimulation(
					Integer.valueOf(overlord.getInvSimBox().getCurrentDockWindow().group.getSelection().getActionCommand())
					,(Integer)overlord.getInvSimBox().getCurrentDockWindow().spiner.getValue());
				*/
				JOptionPane.showMessageDialog(null, lang.getText("unimplemented"),
						lang.getText("unimplementedTitle"), JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ignored) {
			}
		});
		otherMenu.add(invSimul);
		
		JMenuItem fixNet = new JMenuItem(lang.getText("DM_menuOtherFixArcs"), KeyEvent.VK_7);
		fixNet.setIcon(Tools.getResIcon32("/icons/menu/menu_properties.png"));
		//invSimul.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		//fixNet.getAccessibleContext().setAccessibleDescription("Fix arc problems");
		fixNet.addActionListener(arg0 -> {
			try {
				// poniższa 'linia':   ╯°□°）╯︵  ┻━━┻
				overlord.io.fixArcsProblem();
			} catch (Exception e) {
				overlord.log("Error: (437454427)" + e.getMessage(), "error", true);
			}
		});
		otherMenu.add(fixNet);
		
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
				overlord.showClusterWindow(); 
			}
		});
		clustersOperationsMenu.add(showClustersItem);
		*/
		
		JMenu aboutMenu = new JMenu(lang.getText("DM_menuHelp"));
		aboutMenu.setMnemonic(KeyEvent.VK_H);
		aboutMenu.getAccessibleContext().setAccessibleDescription(lang.getText("DM_menuHelp"));
		this.add(aboutMenu);
		
		JMenuItem aboutItem = new JMenuItem(lang.getText("DM_menuHelpAbout"));
		aboutItem.setIcon(Tools.getResIcon32("/icons/menu/menu_about.png"));
		aboutItem.getAccessibleContext().setAccessibleDescription(lang.getText("DM_menuHelpAbout"));
		aboutItem.addActionListener(arg0 -> overlord.createAboutWindow());
		aboutMenu.add(aboutItem);
	}
}
