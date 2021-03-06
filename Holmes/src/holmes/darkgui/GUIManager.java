package holmes.darkgui;

import holmes.analyse.MCTCalculator;
import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.dockable.DeleteAction;
import holmes.darkgui.dockwindows.HolmesDockWindow;
import holmes.darkgui.dockwindows.PetriNetTools;
import holmes.darkgui.dockwindows.HolmesDockWindow.DockWindowType;
import holmes.darkgui.settings.SettingsManager;
import holmes.darkgui.toolbar.Toolbar;
import holmes.files.io.TexExporter;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.subnets.SubnetsControl;
import holmes.petrinet.subnets.SubnetsGraphics;
import holmes.utilities.Tools;
import holmes.windows.*;
import holmes.windows.ssim.HolmesSim;
import holmes.workspace.ExtensionFileFilter;
import holmes.workspace.Workspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import com.javadocking.DockingManager;
import com.javadocking.component.DefaultSwComponentFactory;
import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.CompositeTabDock;
import com.javadocking.dock.Dock;
import com.javadocking.dock.FloatDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.factory.LeafDockFactory;
import com.javadocking.dockable.ActionDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import com.javadocking.dockable.action.DefaultPopupMenuFactory;
import com.javadocking.event.DockingListener;
import com.javadocking.model.DefaultDockingPath;
import com.javadocking.model.FloatDockModel;
import com.javadocking.visualizer.FloatExternalizer;
import com.javadocking.visualizer.LineMinimizer;
import com.javadocking.visualizer.SingleMaximizer;

/**
 * G????wna klasa programu odpowiedzialna za w??a??ciwie wszystko. Zaczyna od utworzenia element??w
 * graficznych programu, a dalej jako?? tak samo ju?? si?? wszystko toczy. Albo wywala.
 * 
 * @author students - kto?? musia?? zacz????.
 * @author MR - Metody, Metody. Nowe Metody. Podpisano: Cyryl
 */
public class GUIManager extends JPanel implements ComponentListener {
	private static final long serialVersionUID = -817072868916096442L;
	// Static fields.
	private static GUIManager guiManager;
	public boolean debug = false;
	public Random randGen = new Random(System.currentTimeMillis());
	public GUIOperations io;
	public TexExporter tex;
	public GUIReset reset;
	public SubnetsGraphics subnetsGraphics;
	public SubnetsControl subnetsHQ;
	public SimulatorGlobals simSettings;
	
	private Dimension screenSize; 		// praca w maksymalizacji
	@SuppressWarnings("unused")
	private Dimension smallScreenSize;	// praca poza maksymalizowanym oknem
	private FloatDockModel dockModel;
	
	// settings
	private final SettingsManager settingsManager;
	
	// visualizers
	private LineMinimizer minimizer;
	private SingleMaximizer maximizer;
	private FloatExternalizer externalizer;
	
	// main Docks
	private Workspace workspace;
	private CompositeTabDock leftTabDock;
	//private CompositeTabDock bottomLeftTabDock;
	private CompositeTabDock topRightTabDock;
	private CompositeTabDock bottomRightTabDock;
	// SplitDocks
	private SplitDock leftSplitDock;
	private SplitDock rightSplitDock;
	private SplitDock totalSplitDock;
	
	private PetriNetTools toolBox;
	
	// podokna dokowalne g????wnego okna Holmes:
	private HolmesDockWindow simulatorBox;	//podokno przycisk??w symulator??w sieci
	private HolmesDockWindow selectionBox;	//podokno zaznaczonych element??w sieci
	private HolmesDockWindow mctBox;			//podokno MCT
	private HolmesDockWindow t_invariantsBox;	//podokno t-inwariant??w
	private HolmesDockWindow p_invariantsBox;
	private HolmesDockWindow selElementBox;  //podokno klikni??tego elementu sieci
	private HolmesDockWindow clustersBox;	//podokno pod??wietlania klastr??w
	private HolmesDockWindow mcsBox;
	private HolmesDockWindow fixBox;
	private HolmesDockWindow knockoutBox;
	private HolmesDockWindow quickSimBox;
	//-//private HolmesDockWindow decompositionBox;
	
	//UNUSED
	
	
	// docking listener
	private DarkDockingListener dockingListener;
	private Toolbar shortcutsBar;

	// main frame
	private JFrame frame;	//g????wna ramka okna programu, tworzona w Main()
	// other components
	private DarkMenu menu;	//komponent menu programu

	// inne wa??ne zmienne
	private String lastPath;	// ostatnia otwarta scie??ka
	private String holmesPath; 	// scie??ka dost??pu do katalogu g????wnego programu
	private String tmpPath;		// ??cie??ka dost??pu do katalogu plik??w tymczasowych
	private String toolPath;	// ??cie??ka dost??pu do katalogu narzedziowego
	private String logPath;
	
	// okna niezale??ne:
	private HolmesClusters windowClusters; //okno tabeli 
	private HolmesConsole windowConsole; //konsola log??w
	private HolmesNetProperties windowNetProperties; //okno w??a??ciwo??ci sieci
	private HolmesAbout windowAbout; //okno About...
	private HolmesSearch windowSearch; //okno wyszukiwania element??w sieci
	private HolmesProgramProperties windowProperties; //okno w??a??ciwo??ci sieci
	private HolmesSim windowStateSim; //okno symulatora stan??w
	private HolmesNetTables windowNetTables; //okno tabel sieci
	private HolmesNotepad windowSimulationLog; //okno log??w symulatora
	private HolmesInvariantsGenerator windowInvariants; //okno generatora inwariant??w
	private HolmesMCS windowMCS; //okno generatora MCS
	private HolmesKnockout windowsKnockout;
	private HolmesDecomposition windowsDeco;
	private HolmesGraphlets windowsGraphlet;
	private HolmesLabelComparison labelComparison;
	private HolmesBranchVerticesPrototype windowsBranch;
	//private HolmesSubnetComparison windowsComp;
	private HolmesComparisonModule windowsComp;
	private HolmesReductionPrototype windowReduction;


	private boolean rReady = false; // true, je??eli program ma dost??p do pliku Rscript.exe
	private boolean inaReady = true;
	
	private boolean nameLocChangeMode = false; //je??li true, zmieniamy offset napisu
	private Node nameSelectedNode = null;
	private ElementLocation nameNodeEL = null;
	
	public ArrayList<Dockable> globalSheetsList = new ArrayList<>();
	
	/**
	 * Konstruktor obiektu klasy GUIManager.
	 * @param frejm JFrame - g????wna ramka kontener programu
	 */
	public GUIManager(JFrame frejm) {
		super(new BorderLayout());

		//JavaDocking wysypuje si?? je??li numer wersji nie posiada przynajmniej jednej .
		if(!System.getProperty("java.version").contains("."))
			System.setProperty("java.version",System.getProperty("java.version")+".0");

		guiManager = this;
		io = new GUIOperations(this); //obiekt klasy operacji g????wnych
		tex = new TexExporter(); //obiekt zarz??dzaj??cy eksportem tabel do formatu latex
		reset = new GUIReset(); //obiekt odpowiadaj??cy za resetowanie danych / kasowanie / czyszczenie
		subnetsGraphics = new SubnetsGraphics(); //obiekt z metodami graficznymi dla sieci hierarchicznych
		subnetsHQ = new SubnetsControl(this); //obiekt z metodami zarz??dzania sieciami hierarchicznymi
		simSettings = new SimulatorGlobals(); //opcje symulatora
		
		setFrame(frejm);
		try {	
			frame.setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {
			System.out.println(e.getMessage());
		}
		
		frame.getContentPane().add(this);
		frame.addComponentListener(this);
		getFrame().getContentPane().add(this);
		getFrame().addComponentListener(this);
		
		createHiddenConsole(); // okno konsoli logowania zdarze??
		
		settingsManager = new SettingsManager();
		settingsManager.loadSettings();
		frame.setTitle("Holmes "+settingsManager.getValue("holmes_version"));
		
		
		createClusterWindow(); // okno tabeli klastr??w
		createNetPropertiesWindow(); // okno w??a??ciwo??ci sieci
		createSearchWindow(); // okno wyszukiwania element??w sieci
		createNetTablesWindow(); // okno tabel sieci
		createInvariantsWindow(); // okno generatora inwariant??w
		
		
		initializeEnvironment(); //wczytuje ustawienia, ustawia wewn??trzne zmienne programu
		
		// Set the frame properties and show it.
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		//JFrame.setDefaultLookAndFeelDecorated(true);
		
		getFrame().setLocation((int) (screenSize.width * 0.1) / 2, (int) (screenSize.height * 0.1) / 2);
		getFrame().setSize((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));
		getFrame().setVisible(true);
		getFrame().setExtendedState(getFrame().getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		
		// Create the dock model for the docks.
		setDockModel(new FloatDockModel());
		getDockModel().addOwner("frame0", getFrame());

		FloatDock floatDock = getDockModel().getFloatDock(getFrame());
		floatDock.setChildDockFactory(new LeafDockFactory(false));

		// setfactories
		DefaultPopupMenuFactory popupMenuFactory = new DefaultPopupMenuFactory();
		popupMenuFactory.setPopupActions(DefaultPopupMenuFactory.DOCKABLE_ACTIONS
				| DefaultPopupMenuFactory.CLOSE_ALL_ACTION
				| DefaultPopupMenuFactory.CLOSE_OTHERS_ACTION);
		DefaultSwComponentFactory componentFactory = new DefaultSwComponentFactory();
		componentFactory.setPopupMenuFactory(popupMenuFactory);
		DockingManager.setComponentFactory(componentFactory);

		// Give the dock model to the docking manager.
		DockingManager.setDockModel(getDockModel());

		// Create the composite tab docks.
		leftTabDock = new CompositeTabDock(); // default Toolbox dock
		topRightTabDock = new CompositeTabDock(); // default Properties dock
		bottomRightTabDock = new CompositeTabDock(); // default Simulator dock

		// set docking listener
		setDockingListener(new DarkDockingListener());
		setToolBox(new PetriNetTools());
		setPropertiesBox(new HolmesDockWindow(DockWindowType.EDITOR));
		setSimulatorBox(new HolmesDockWindow(DockWindowType.SIMULATOR));
		setSelectionBox(new HolmesDockWindow(DockWindowType.SELECTOR));
		setT_invBox(new HolmesDockWindow(DockWindowType.T_INVARIANTS));
		setP_invSim(new HolmesDockWindow(DockWindowType.P_INVARIANTS));
		setClusterSelectionBox(new HolmesDockWindow(DockWindowType.ClusterSELECTOR));
		setMctBox(new HolmesDockWindow(DockWindowType.MctANALYZER)); //aktywuj obiekt podokna wy??wietlania zbior??w MCT
		setMCSBox(new HolmesDockWindow(DockWindowType.MCSselector));
		setKnockoutBox(new HolmesDockWindow(DockWindowType.Knockout));
		setQuickSimBox(new HolmesDockWindow(DockWindowType.QuickSim));
		//setDecompositionBox(new HolmesDockWindow(DockWindowType.DECOMPOSITION));

		// create menu
		setMenu(new DarkMenu());
		getFrame().setJMenuBar(getMenu());

		// create workspace
		setWorkspace(new Workspace(this)); // default workspace dock
		getDockingListener().setWorkspace(workspace);
		
		setFixBox(new HolmesDockWindow(DockWindowType.FIXNET));

		//leftTabDock.setHeaderPosition(Position.BOTTOM);
		leftTabDock.addChildDock(getToolBox(), new Position(0));
		leftTabDock.addChildDock(getSimulatorBox(), new Position(1));
		leftTabDock.setSelectedDock(getToolBox());

		topRightTabDock.addChildDock(getPropertiesBox(), new Position(0));
		topRightTabDock.setSelectedDock(getPropertiesBox());
		
		bottomRightTabDock.addChildDock(getT_invBox(), new Position(1));
		bottomRightTabDock.addChildDock(getP_invBox(), new Position(2));
		bottomRightTabDock.addChildDock(getMctBox(), new Position(3));
		bottomRightTabDock.addChildDock(getMCSBox(), new Position(4));
		bottomRightTabDock.addChildDock(getClusterSelectionBox(), new Position(5));
		bottomRightTabDock.addChildDock(getKnockoutBox(), new Position(6));
		bottomRightTabDock.addChildDock(getQuickSimBox(), new Position(7));
		bottomRightTabDock.addChildDock(getFixBox(), new Position(8));
		//bottomRightTabDock.addChildDock(getDecompositionBox(), new Position(9));

		// create the split docks
		//leftSplitDock = new SplitDock();
		//leftSplitDock.addChildDock(leftTabDock, new Position(Position.LEFT));
		//leftSplitDock.addChildDock(getWorkspace().getWorkspaceDock(), new Position(Position.CENTER));
				
		leftSplitDock = new SplitDock();
		leftSplitDock.addChildDock(leftTabDock, new Position(Position.LEFT));
		
		SplitDock workspaceSplit = new SplitDock();
		workspaceSplit.addChildDock(getWorkspace().getWorkspaceDock(), new Position(Position.CENTER));
		if(debug) {
			workspaceSplit.addChildDock(getSelectionBox(), new Position(Position.BOTTOM));
		} else {
			topRightTabDock.addChildDock(getSelectionBox(), new Position(1));
			topRightTabDock.setSelectedDock(getPropertiesBox());
		}
		workspaceSplit.setDividerLocation((int) (screenSize.getHeight() * 7 / 10));
		leftSplitDock.addChildDock(workspaceSplit, new Position(Position.CENTER));
		leftSplitDock.setDividerLocation(180);
		
		rightSplitDock = new SplitDock();
		rightSplitDock.addChildDock(topRightTabDock, new Position(Position.TOP));
		rightSplitDock.addChildDock(bottomRightTabDock, new Position(Position.BOTTOM));
		rightSplitDock.setDividerLocation((int) (screenSize.getHeight() * 2 / 5));

		totalSplitDock = new SplitDock();
		totalSplitDock.addChildDock(leftSplitDock, new Position(Position.LEFT));
		totalSplitDock.addChildDock(rightSplitDock,new Position(Position.RIGHT));
		//totalSplitDock.setDividerLocation(400);
		totalSplitDock.setDividerLocation((int) screenSize.getWidth() - (int) screenSize.getWidth() / 6);
		
		// // Add root dock
		getDockModel().addRootDock("totalSplitDock", totalSplitDock, getFrame());
		add(totalSplitDock, BorderLayout.CENTER);

		// save docking paths
		DockingManager.getDockingPathModel().add(DefaultDockingPath.createDockingPath(getToolBox().getDockable()));
		DockingManager.getDockingPathModel().add(DefaultDockingPath.createDockingPath(getPropertiesBox().getDockable()));

		// Create an externalizer.
		setExternalizer(new FloatExternalizer(getFrame()));
		dockModel.addVisualizer("externalizer", getExternalizer(), getFrame());

		// Create a minimizer.
		setMinimizer(new LineMinimizer(totalSplitDock));
		dockModel.addVisualizer("minimizer", getMinimizer(), getFrame());

		// Create a maximizer.
		setMaximizer(new SingleMaximizer(getMinimizer()));
		dockModel.addVisualizer("maximizer", getMaximizer(), getFrame());
		this.add(getMaximizer(), BorderLayout.CENTER);

		// default screen size unmaximized
		smallScreenSize = new Dimension((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));
		setShortcutsBar(new Toolbar());

		// Add the shortcuts bar also as root dock to the dock model.
		dockModel.addRootDock("toolBarBorderDock", getShortcutsBar().getToolBarBorderDock(), frame);

		// Add the shortcuts bar to this panel.
		this.add(getShortcutsBar().getToolBarBorderDock(), BorderLayout.CENTER);
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyManager(this));

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	boolean status = GUIManager.getDefaultGUIManager().getNetChangeStatus();
				if(status) {
					Object[] options = {"Exit", "Save and exit", "Cancel",};
					int n = JOptionPane.showOptionDialog(null,
									"Network or its data have been changed since last save. Exit, save&exit or do not exit now?",
									"Project has been modified", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					//cancel
					if (n == 2) return;
					else if (n == 1) { //try to save
						boolean savingStatus = io.saveAsGlobal();
						if(!savingStatus) return;
						else {
							log("Exiting program","text",true);
			            	windowConsole.saveLogToFile(null);
			            	System.exit(0);
						}
					} else { // n == 0
						log("Exiting program","text",true);
		            	windowConsole.saveLogToFile(null);
		            	System.exit(0);
					}
				} else if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the program?", "Exit?", 
		            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
		        		log("Exiting program","text",true);
		            	windowConsole.saveLogToFile(null);
		            	System.exit(0);
		        	}
		    	}
		});
		
		//na samym ko??cu, gdy ju?? wszystko 'dzia??a'
		//createPropertiesWindow();
		createStateSimulatorWindow();
		createMCSWindow(); // okno generatora MCS
		createSimLogWindow(); // okno log??w symulatora
		
		String path = settingsManager.getValue("lastOpenedPath");
		File f = new File(path);
		if(f.exists())
			lastPath = path;	

		getSimulatorBox().createSimulatorProperties();
	}

	/**
	 * Metoda pomocnicza konstruktora. Ustawia g????wne zmienne programu, wczytuje plik
	 * w??a??ciwo??ci, itd.
	 */
	private void initializeEnvironment() {
		// ustawienie ??cie??ek dost??pu
		lastPath = null;
		holmesPath = System.getProperty("user.dir");
		tmpPath = holmesPath+"\\tmp\\";
		toolPath = holmesPath+"\\tools\\";
		logPath = holmesPath+"\\log\\";

		File checkFile = new File(tmpPath);
		if (!checkFile.exists()) checkFile.mkdirs();
		checkFile = new File(logPath);
		if (!checkFile.exists()) checkFile.mkdirs();
		
		// Katalog /tools i pliki INY:
		File checkFileINA0 = new File(toolPath);
		File checkFileINA1 = new File(toolPath+"//INAwin32.exe");
		File checkFileINA2 = new File(toolPath+"//COMMAND.ina");
		File checkFileINA2p = new File(toolPath+"//COMMANDp.ina");
		File checkFileINA3 = new File(toolPath+"//ina.bat");
		if (!checkFileINA0.exists() || !checkFileINA1.exists() 
				|| !checkFileINA2.exists() || !(checkFileINA2.length() == 80)
				|| !checkFileINA3.exists() || !(checkFileINA3.length() == 30)
				|| !checkFileINA2p.exists() || !(checkFileINA2p.length() == 77)) {
			
			log("Something wrong with the INA tools directory.", "warning", true);
			if(!checkFileINA0.exists()) {
				checkFileINA0.mkdirs();
				logNoEnter("Tools directory does not exist: ", "warning", true);
				log("fixed", "italic", false);
			}
			
			if(!checkFileINA2.exists() || !(checkFileINA2.length() == 80)) { //COMMAND.ina
				try {
					PrintWriter pw = new PrintWriter(checkFileINA2.getPath());
					pw.print(settingsManager.getValue("ina_COMMAND1")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND2")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND3")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND4"));
					pw.close();
					logNoEnter("File COMMAND.ina does not exist or is corrupted. ", "warning", true);
					log("Fixed", "italic", false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Unable to recreate COMMAND.ina.","Error - COMMAND.ina", JOptionPane.ERROR_MESSAGE);
					inaReady = false;
					log("Unable to recreate COMMAND.ina. Invariants generator will work in Holmes mode only.", "warning", true);
				}
			} 
			
			if(!checkFileINA2p.exists() || !(checkFileINA2p.length() == 77)) { //COMMANDp.ina  //p-inv
				try {
					PrintWriter pw = new PrintWriter(checkFileINA2p.getPath());
					pw.print(settingsManager.getValue("ina_COMMAND1")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND2p")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND4"));
					pw.close();
					logNoEnter("File COMMANDp.ina does not exist or is corrupted. ", "warning", true);
					log("Fixed", "italic", false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Unable to recreate COMMANDp.ina.","Error - COMMANDp.ina", JOptionPane.ERROR_MESSAGE);
					inaReady = false;
					log("Unable to recreate COMMANDp.ina. Invariants generator will work in Holmes mode only.", "warning", true);
				}
			} 
			
			if(!checkFileINA3.exists() || !(checkFileINA3.length() == 30)) { //ina.bat
				try {
					PrintWriter pw = new PrintWriter(checkFileINA3.getPath());
					pw.print(settingsManager.getValue("ina_bat"));
					pw.close();
					logNoEnter("File ina.bat did not exist or was corrupted: ", "warning", true);
					log("fixed", "italic", false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Unable to recreate ina.bat. This is a critical error, possible write"
							+ " protection issues in program directory. All in all, invariants generation using INAwin32 will"
							+ " most likely fail.","Critical error - writing", JOptionPane.ERROR_MESSAGE);
					inaReady = false;
					log("Critical error, unable to recreate ina.bat file. Invariants generator will not work.", "warning", true);
				}
			} 
			
			if(!checkFileINA1.exists()) { //no INAwin32.exe
				//String msg = "INAwin32.exe missing in\n "+checkFileINA0.getPath()+"directory.\n"
				//		+ "Please download manually from and put in the right directory.";
				//JOptionPane.showMessageDialog(null, msg, "Error - no INAwin32.exe", JOptionPane.ERROR_MESSAGE);
				log("INAwin32.exe missing in "+checkFileINA0+"directory. Please download "
						+ "manually from www2.informatik.hu-berlin.de/~starke/ina.html and put into the \\Tools directory.", "warning", true);
				inaReady = false;
			}
		}
		
		//check status of Rscript.exe location:
		checkRlangStatus(false);
		
		if(settingsManager.getValue("programDebugMode").equals("1"))
			debug = true;
	}

	/**
	 * Metoda uruchamiana na starcie programu oraz wtedy, gdy chcemy uzyska?? dost??p do lokalizacji
	 * pliku Rscript.exe.
	 */
	public void checkRlangStatus(boolean forceCheck) {
		rReady = true;
		String Rpath = settingsManager.getValue("r_path");
		File rF = new File(Rpath);
		if(!rF.exists()) {
			rReady = false;
			log("Invalid path ("+Rpath+") to Rscript executable file.", "warning", true);
			if(!forceCheck) { //je??li nie jest wymuszone sprawdzanie, sprawd?? status settings
				if(getSettingsManager().getValue("programAskForRonStartup").equals("0"))
					return;
			}

			Object[] options = {"Manually locate Rscript.exe", "R not installed",};
			int n = JOptionPane.showOptionDialog(null,
					"Rscript.exe missing in path "+Rpath,
					"Missing executable", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (n == 0) {
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter(".exe - Rscript",  new String[] { "EXE" });
				String selectedFile = Tools.selectFileDialog("", filters, "Select Rscript.exe", 
						"Please select Rscript exe, usually located in R/Rx.x.x/bin directory.", "");
				if(selectedFile.equals("")) {
					log("Rscript executable file inaccessible. Some features (clusters operations) will be disabled.", "error", true);
				} else {
					if(!selectedFile.contains("x64")) { //je??li wskazano 64b
						String dest = selectedFile.substring(0,selectedFile.lastIndexOf(File.separator));
						dest += "\\x64\\Rscript.exe";
						if(Tools.ifExist(dest))
							settingsManager.setValue("r_path64", dest, true);
						else
							settingsManager.setValue("r_path64", "", true);
					} else {
						settingsManager.setValue("r_path64", selectedFile, true);
					}
					
					if(Tools.ifExist(selectedFile)) {
						settingsManager.setValue("r_path", selectedFile, true);
						settingsManager.saveSettings();
						setRStatus(true);
						log("Rscript.exe manually located in "+selectedFile+". Settings file updated.", "text", true);
					
					} else {
						settingsManager.setValue("r_path", "", true);
						setRStatus(false);
						log("Rscript.exe location unknown. Clustering procedures will not work.", "error", true);	
					}
				}
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za ustalenie domy??lnych lokalizacji pask??w zmiany rozmiaru
	 * podokien programu (Dividers).
	 */
	private void resetSplitDocks() {
		if (getFrame().getExtendedState() == JFrame.MAXIMIZED_BOTH) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//leftSplitDock.setDividerLocation((int) screenSize.getWidth() / 10);
			leftSplitDock.setDividerLocation(180);
			//
			//int width = this.getWidth();
			//totalSplitDock.setDividerLocation(600);
			//totalSplitDock.setDividerLocation((int) (screenSize.getWidth() * 5.6 / 7));
			totalSplitDock.setDividerLocation((int) (screenSize.getWidth() - 350));
		} else {
			smallScreenSize = getFrame().getSize();
			//leftSplitDock.setDividerLocation((int) smallScreenSize.getWidth() / 8);
			leftSplitDock.setDividerLocation(180);
			//totalSplitDock.setDividerLocation((int) (smallScreenSize.getWidth() * 5.6 / 7));
			totalSplitDock.setDividerLocation((int) (screenSize.getWidth() - 350));
		}
	}

	/**
	 * Metoda odpowiedzialna za dodawanie nowych ikonek w prawy g??rnym roku ka??dego podokna programu.
	 * @param dockable - okno do przystrojenia dodatkowymi okienkami
	 * @param deletable - true, je??li dodajemy ikon?? usuwania (g????wne podokno arkuszy sieci)
	 * @return Dockable - nowe okno po dodaniu element??w
	 */
	public Dockable decorateDockableWithActions(Dockable dockable, boolean deletable) {
		Dockable wrapper = new StateActionDockable(dockable, new DefaultDockableStateActionFactory(), new int[0]);
		int[] states = { DockableState.NORMAL, DockableState.MINIMIZED, DockableState.MAXIMIZED, DockableState.EXTERNALIZED, DockableState.CLOSED };
		wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), states);

		if (deletable) {
			DeleteAction deleteAction = new DeleteAction(this, "Delete", Tools.getResIcon16("/icons/page_white_delete.png"));
			Action[][] actions = new Action[1][];
			actions[0] = new Action[1];
			actions[0][0] = deleteAction;
			wrapper = new ActionDockable(wrapper, actions);
			deleteAction.setDockable(wrapper);
		}

		return wrapper;
	}
	
	/**
	 * Metoda zwraca ??cie??ki do ostatio u??ywanego katalagu.
	 * @return String - ??cie??ka do katalogu
	 */
	public String getLastPath() {
		return lastPath;
	}
	
	/**
	 * Metoda ustawia now?? ??cie??k?? do ostatnio u??ywanego katalagu. Zapisuje j?? do pliku ustawie?? programu.
	 * @return String - ??cie??ka do katalogu
	 */
	public void setLastPath(String path) {
		lastPath = path;
		settingsManager.setValue("lastOpenedPath", lastPath, true);
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	/**
	 * Czyszczenie modu????w
	 */
	public void resetModuls(){
		windowsComp = new HolmesComparisonModule();
		windowsBranch = new HolmesBranchVerticesPrototype();
		windowsGraphlet = new HolmesGraphlets();
		windowsDeco = new HolmesDecomposition();
		windowsKnockout = new HolmesKnockout();
	}

	/**
	 * Diabli wiedz?? co i kiedy wywo??uje t?? metod??, tym niemniej zleca ona innej ustalenie
	 * domy??lnych lokalizacji pask??w zmiany rozmiar??w podokien (Dividers).
	 */
	public void componentResized(ComponentEvent arg0) {
		resetSplitDocks();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}
	
	/**
	 * Metoda zwraca obiekt podokna do wy??wietlania zbior??w MCT.
	 * @return HolmesDockWindow - okno wyboru MCT
	 */
	public HolmesDockWindow getMctBox() {
		return mctBox;
	}

	/**
	 * Metoda ustawia nowe podokno dedykowane do wy??wietlania zbior??w MCT.
	 * @param mctBox HolmesDockWindow - nowe okno wyboru MCT
	 */
	private void setMctBox(HolmesDockWindow mctBox) {
		this.mctBox = mctBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna wy??wietlania zbior??w MCS.
	 * @return HolmesDockWindow - okno wyboru MCS
	 */
	private HolmesDockWindow getMCSBox() {
		return mcsBox;
	}
	/**
	 * Metoda ustawia nowe podokno dedykowane do wy??wietlania zbior??w MCS.
	 * @param mcsBox HolmesDockWindow - nowe okno wyboru MCS
	 */
	private void setMCSBox(HolmesDockWindow mcsBox) {
		this.mcsBox = mcsBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna naprawy sieci.
	 * @return HolmesDockWindow - okno naprawcze
	 */
	private HolmesDockWindow getFixBox() {
		return this.fixBox;
	}
	/**
	 * Metoda ustawia nowe podokno naprawcze sieci.
	 * @param fixBox HolmesDockWindow - nowe okno naprawy
	 */
	private void setFixBox(HolmesDockWindow fixBox) {
		this.fixBox = fixBox;
	}
	
	/**
	 * Metoda ustawia nowe podokno dedykowane do wy??wietlania zbior??w knockout.
	 * @param knockoutBox HolmesDockWindow - nowe okno wyboru knockoutBox
	 */
	private void setKnockoutBox(HolmesDockWindow knockoutBox) {
		this.knockoutBox = knockoutBox;
	}

	/**
	 * Metoda zwraca obiekt podokna wy??wietlania zbior??w knockout.
	 * @return HolmesDockWindow - okno wyboru knockoutBox
	 */
	private HolmesDockWindow getKnockoutBox() {
		return knockoutBox;
	}
	
	/**
	 * Metoda ustawia nowe podokno symulatora QuickSim.
	 * @param quickSimBox HolmesDockWindow - nowe okno QuickSim
	 */
	private void setQuickSimBox(HolmesDockWindow quickSimBox) {
		this.quickSimBox = quickSimBox;
	}

	/**
	 * Metoda ustawia nowe podokno dekompozycji.
	 * @param deompositionBox HolmesDockWindow - nowe okno dekompozycji
	 */
	//-//
	/*
	public void setDecompositionBox(HolmesDockWindow deompositionBox){
		this.decompositionBox = deompositionBox;
	}
*/
	/**
	 * Metoda zwraca obiekt podokna dekompozycji.
	 * @return HolmesDockWindow - okno dekompozycji
	 */
	/*
	public HolmesDockWindow getDecompositionBox() {
		return decompositionBox;
	}
*/
	/**
	 * Metoda zwraca obiekt podokna symulatora QuickSim.
	 * @return HolmesDockWindow - okno QuickSim
	 */
	private HolmesDockWindow getQuickSimBox() {
		return quickSimBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna dla pod??wietlania inwariant??w sieci.
	 * @return HolmesDockWindow - podokno inwariant??w
	 */
	public HolmesDockWindow getT_invBox() {
		return t_invariantsBox;
	}

	/**
	 * Metoda ta ustawia obiekt podokna dla pod??wietlania inwariant??w sieci.
	 * @param invariantsBox HolmesDockWindow - podokno inwariant??w
	 */
	private void setT_invBox(HolmesDockWindow invariantsBox) {
		this.t_invariantsBox = invariantsBox;
	}
	
	/**
	 * Metoda ustawia nowe okno w??a??ciwo??ci p-inwariant??w.
	 * @param invSim HolmesDockWindow - okno p-inwariant??w
	 */
	private void setP_invSim(HolmesDockWindow invSim)
	{
		this.p_invariantsBox = invSim;
	}
	
	/**
	 * Metoda zwraca aktywne okno w??a??ciwo??ci p-inwariant??w.
	 */
	public HolmesDockWindow getP_invBox() {
		return p_invariantsBox;
	}

	/**
	 * Metoda odpowiedzialna za zwr??cenie obiektu obszaru roboczego.
	 * @return Workspace - obszar roboczy
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Metoda odpowiedzialna za ustawianie nowego obszaru roboczego.
	 * @param workspace Workspace - obszar roboczy
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Metoda zwraca obiekt paska narz??dziowego.
	 * @return PetriNetTools - pasek przycisk??w
	 */
	public PetriNetTools getToolBox() {
		return toolBox;
	}

	/**
	 * Metoda ustawia nowy obiekt paska narz??dziowego.
	 * @param toolBox PetriNetTools - pasek przycisk??w
	 */
	private void setToolBox(PetriNetTools toolBox) {
		this.toolBox = toolBox;
	}

	/**
	 * Metoda zwraca obiekt podokna do wy??wietlania w??a??ciwo??ci klikni??tego elementu sieci.
	 * @return HolmesDockWindow - podokno w??a??ciwo??ci
	 */
	public HolmesDockWindow getPropertiesBox() {
		return selElementBox;
	}

	/**
	 * Metoda ustawia nowy obiekt podokna do wy??wietlania w??a??ciwo??ci klikni??tego elementu sieci.
	 * @param propertiesBox HolmesDockWindow - podokno w??a??ciwo??ci
	 */
	private void setPropertiesBox(HolmesDockWindow propertiesBox) {
		this.selElementBox = propertiesBox;
	}

	/**
	 * Metoda zwraca obiekt menu programu.
	 * @return DarkMenu - obiekt z pozycjami menu
	 */
	public DarkMenu getMenu() {
		return menu;
	}

	/**
	 * Metoda ustawia nowy obiekt menu programu.
	 * @param menu DarkMenu - obiekt z pozycjami menu
	 */
	private void setMenu(DarkMenu menu) {
		this.menu = menu;
	}

	/**
	 * Opis: I have no idea...
	 * @return FloatDockModel
	 */
	public FloatDockModel getDockModel() {
		return dockModel;
	}

	/**
	 * Opis: I have no idea...
	 * @param dockModel FloatDockModel
	 */
	private void setDockModel(FloatDockModel dockModel) {
		this.dockModel = dockModel;
	}

	/**
	 * Metoda zwraca obiekt - referencji swojej klasy.
	 * @return GUIManager - obiekt managaera
	 */
	public static GUIManager getDefaultGUIManager() {
		return guiManager;
	}

	/**
	 * Metoda zwraca identyfikator obszaru roboczego.
	 * @param id int - id
	 * @return int - id
	 */
	public int IDtoIndex(int id) {
		return workspace.getIndexOfId(id);
	}

	/**
	 * Zwraca obiekt przycisku powi??kszaj??cego okno do rozmiar??w ekranu.
	 * @return SingleMaximizer
	 */
	public SingleMaximizer getMaximizer() {
		return maximizer;
	}

	/**
	 * Ustawia obiekt przycisku powi??kszaj??cego okno do rozmiar??w ekranu.
	 * @param maximizer SingleMaximizer
	 */
	private void setMaximizer(SingleMaximizer maximizer) {
		this.maximizer = maximizer;
	}

	/**
	 * Zwraca obiekt przycisku pomniejszaj??cego okno do paska zada??.
	 * @return LineMinimizer
	 */
	public LineMinimizer getMinimizer() {
		return minimizer;
	}

	/**
	 * Ustawia obiekt przycisku pomniejszaj??cego okno do paska zada??.
	 * @param minimizer LineMinimizer
	 */
	private void setMinimizer(LineMinimizer minimizer) {
		this.minimizer = minimizer;
	}

	public FloatExternalizer getExternalizer() {
		return externalizer;
	}

	/**
	 * Opis: I have no idea...
	 * @param externalizer FloatExternalizer
	 */
	private void setExternalizer(FloatExternalizer externalizer) {
		this.externalizer = externalizer;
	}
	
	/**
	 * Metoda zwraca obiekt ramki.
	 * @return JFrame - ramka
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Metoda ta ustawia nowy obiekt ramki.
	 * @param frame JFrame - ramka
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * Metoda zwraca obiekt okna symulatora sieci.
	 * @return Properties - okno symulatora sieci
	 */
	public HolmesDockWindow getSimulatorBox() {
		return simulatorBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna symulatora sieci.
	 * @param simulatorBox Properties - okno symulatora sieci
	 */
	private void setSimulatorBox(HolmesDockWindow simulatorBox) {
		this.simulatorBox = simulatorBox;
	}

	/**
	 * Metoda ta zwraca obiekt okna wyboru element??w.
	 * @return HolmesDockWindow - okno wyboru element??w
	 */
	public HolmesDockWindow getSelectionBox() {
		return selectionBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna wyboru element??w.
	 * @param selectionBox HolmesDockWindow - okno wyboru element??w
	 */
	private void setSelectionBox(HolmesDockWindow selectionBox) {
		this.selectionBox = selectionBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna przycisk??w programu.
	 * @return Toolbar - obiekt okna przycisk??w
	 */
	private Toolbar getShortcutsBar() {
		return shortcutsBar;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna przycisk??w programu.
	 * @param shortcutsBar Toolbar - obiekt okna przycisk??w
	 */
	private void setShortcutsBar(Toolbar shortcutsBar) {
		this.shortcutsBar = shortcutsBar;
	}
	
	/**
	 * Metoda pomocnicza wywo??ywana w trakcie tworzenia podokien Holmes (inwarianty, mct, inne, tak??e 
	 * narzedzia do rysowania).
	 * @param dockable Dockable
	 * @param listener DockingListener
	 * @return Dockable - obiekt podokna zaboxowany w obiekcie dokowalnym
	 */
	public static Dockable externalWithListener(Dockable dockable, DockingListener listener) {
		Dockable wrapper = guiManager.decorateDockableWithActions(dockable, false);
		wrapper.addDockingListener(listener);
		return wrapper;
	}

	/**
	 * Metoda zwracaj??ca obiekt nas??uchuj??cy zdarzenia dokowania podokna.
	 * @return DarkDockingListener - obiekt nas??uchuj??cy
	 */
	public DarkDockingListener getDockingListener() {
		return dockingListener;
	}

	/**
	 * Metoda ustawiaj??ca nowy obiekt nas??uchuj??cy zdarzenia dokowania podokna.
	 * @param dockingListener DarkDockingListener - nowy obiekt nas??uchuj??cy
	 */
	public void setDockingListener(DarkDockingListener dockingListener) {
		this.dockingListener = dockingListener;
	}
	
	/**
	 * Metoda zwraca obiekt podokna wyboru klastr??w do pod??wietlania.
	 * @return HolmesDockWindow - obiekt z w??a??ciwo??ciani sieci
	 */
	public HolmesDockWindow getClusterSelectionBox() {
		return clustersBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt podokna wyboru klastr??w do pod??wietlania.
	 * @param clusterBox HolmesDockWindow - obiekt z w??a??ciwo??ciami sieci
	 */
	public void setClusterSelectionBox(HolmesDockWindow clusterBox) {
		this.clustersBox = clusterBox;
	}

	/**
	 * Metoda zwraca obiekt okna konsoli programu.
	 * @return HolmesConsole - obiekt konsoli
	 */
	public HolmesConsole getConsoleWindow() {
		return windowConsole;
	}
	
	/**
	 * Metoda zwraca ??cie??k?? do katalogu narzedzi.
	 * @return String
	 */
	public String getToolPath() {
		return toolPath;
	}
	
	/**
	 * Metoda zwraca ??cie??k?? do katalogu programu.
	 * @return String
	 */
	public String getHolmesPath() {
		return holmesPath;
	}
	
	/**
	 * Metoda zwraca ??cie??k?? do katalogu plik??w tymczasowych.
	 * @return String
	 */
	public String getTmpPath() {
		return tmpPath;
	}
	
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************


	/**
	 * G????wna metoda odpowiedzialna za generowanie zbior??w MCT.
	 */
	public void generateMCT() {
		MCTCalculator analyzer = getWorkspace().getProject().getMCTanalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		getWorkspace().getProject().setMCTMatrix(mct, true);
		getMctBox().showMCT(mct); //sortowanie
	}
	
	/**
	 * Metoda pokazuje podokienko zbior??w MCS.
	 */
	public void showMCS() {
		getMCSBox().showMCS();
	}
	
	/**
	 * Metoda pokazuje podokienko zbior??w Knockout.
	 */
	public void showKnockout(ArrayList<ArrayList<Integer>> knockoutData) {
		getKnockoutBox().showKnockout(knockoutData);
	}
	
	/**
	 * Metoda zleca wy??wietlenie podokna pod??wietlania klastrowania
	 */
	public void showClusterSelectionBox(ClusterDataPackage data){
		getClusterSelectionBox().showClusterSelector(data);
		GUIManager.getDefaultGUIManager().reset.setClustersStatus(true); //status klastr??w: wczytane
	}
	
	/**
	 * Metoda rozpoczyna symulacj?? uruchamiania inwariant??w.
	 * @param type int - 0-basic, 1- time
	 * @param value - warto????
	 * @throws CloneNotSupportedException
	 */
	public void startInvariantsSimulation(int type, int value) throws CloneNotSupportedException{
		this.getWorkspace().getProject().startInvSim(type, value);
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy nowe okno tabeli klastr??w.
	 */
	private void createClusterWindow() {
		windowClusters = new HolmesClusters(0);
	}
	
	/**
	 * Metoda s??u??y do wy??wietlania okna klastr??w.
	 */
	public void showClusterWindow() {
		if(windowClusters != null) {
			windowClusters.setVisible(true);
		}
	}
	
	/**
	 * Dost??p do okna tabel klastr??w.
	 * @return HolmesClusters - obiekt okna
	 */
	public HolmesClusters accessClusterWindow() {
		return windowClusters;
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy okno w??a??ciwo??ci sieci.
	 */
	private void createNetPropertiesWindow() {
		windowNetProperties = new HolmesNetProperties();
		windowNetProperties.setLocationRelativeTo(frame);
	}
	
	/**
	 * Metoda s??u??y do wy??wietlania okna w??a??ciwo??ci sieci.
	 */
	public void showNetPropertiesWindow() {
		if(windowNetProperties != null) {
			windowNetProperties.setVisible(true);
		}
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy okno informacji o programie.
	 */
	public void createAboutWindow() {
		windowAbout = new HolmesAbout(frame);
	}
	
	/**
	 * Metoda s??u??y do wy??wietlenia okna informacji o programie.
	 */
	public void showAboutWindow() {
		if(windowAbout != null) {
			windowAbout.setVisible(true);
		}
	}
	
	/**
	 * Metoda ta tworzy nowe okno szukania element??w sieci.
	 */
	private void createSearchWindow() {
		windowSearch = new HolmesSearch();
	}
	
	/**
	 * Metoda wy??wietla okno szukania element??w sieci na ekranie.
	 */
	public void showSearchWindow() {
		if(windowSearch != null) {
			windowSearch.setVisible(true);
		}
	}
	
	/**
	 * Metoda ta zwraca obiekt okna wyszukiwania element??w, u??ywana przez StateSimulator.
	 * @return HolmesSearch - okno wyszukiwania element??w
	 */
	public HolmesSearch getSearchWindow() {
		return windowSearch;
	}
	
	/**
	 * Metoda tworzy nowe okno w??a??ciwo??ci programu.
	 */
	private void createPropertiesWindow() {
		windowProperties = new HolmesProgramProperties(frame);
	}
	
	/**
	 * Metoda pokazuje okno w??a??ciwo??ci programu.
	 */
	public void showPropertiesWindow() {
		
		if(windowProperties != null) {
			windowProperties.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			//((JFrame)windowProperties).dispose();
			createPropertiesWindow();
		} else {
			createPropertiesWindow();
		}
		windowProperties.setVisible(true);
	}
	
	/**
	 * Metoda tworzy nowe okno symulatora stan??w programu.
	 */
	private void createStateSimulatorWindow() {
		windowStateSim = new HolmesSim(this);
	}
	
	/**
	 * Metoda zwraca obiekt okna symulatora.
	 * @return HolmesStateSimulator - obiekt
	 */
	public HolmesSim accessStateSimulatorWindow() {
		return windowStateSim;
	}
	
	/**
	 * Metoda pokazuje okno symulatora stan??w programu.
	 */
	public void showStateSimulatorWindow() {
		if(windowStateSim != null) {
			windowStateSim.setVisible(true);
			//this.getFrame().setEnabled(false);
		}
	}
	
	/**
	 * Metoda tworzy nowe okno tabel sieci.
	 */
	private void createNetTablesWindow() {
		windowNetTables = new HolmesNetTables(frame);
	}
	
	/**
	 * Metoda pokazuje okno tabel sieci.
	 */
	public void showNetTablesWindow() {
		if(windowNetTables != null) {
			if(!reset.isSimulatorActiveWarning("Warning: simulator active. Cannot proceed until manually stopped."
					, "Net simulator working")) {
				windowNetTables.setVisible(true);
			}
		}
	}
	
	/**
	 * Metoda pozwala na dost??p do obiektu okna tabel sieci.
	 * @return HolmesNetTables - obiekt okna danych (tabel) sieci
	 */
	public HolmesNetTables accessNetTablesWindow() {
		return windowNetTables;
	}
	
	/**
	 * Metoda tworzy nowe okno log??w symulatora sieci (na bazie okna notatnika programu).
	 */
	private void createSimLogWindow() {
		windowSimulationLog = new HolmesNotepad(900,600);
		windowSimulationLog.setVisible(false);
	}
	
	/**
	 * Metoda pokazuje okno log??w symulatora sieci (okno na bazie okna notatnika programu).
	 */
	public void showSimLogWindow() {
		if(windowSimulationLog != null) {
			windowSimulationLog.setVisible(true);
		}
	}
	
	/**
	 * Metoda zwraca obiekt notatnika log??w symulatora programu.
	 * @return HolmesNotepad - obiekt okna symulatora
	 */
	public HolmesNotepad getSimLog() {
		return windowSimulationLog;
	}
	
	/**
	 * Metoda pomocnicza konstruktora, s??u??y do tworzenia ukrytego okna konsoli log??w.
	 */
	private void createHiddenConsole() {
		windowConsole = new HolmesConsole();
		windowConsole.setLocationRelativeTo(this);
	}
	
	/**
	 * Metoda s??u??y do pokazywania lub chowania okna konsoli.
	 */
	public void showConsole(boolean value) {
		if(windowConsole != null) {
			windowConsole.setVisible(value);
		}
	}
	
	/**
	 * Metoda tworz??ca okno generatora inwariant??w.
	 */
	public void createInvariantsWindow() {
		windowInvariants = new HolmesInvariantsGenerator();
		windowInvariants.setVisible(false);
	}
	
	/**
	 * Metoda pokazuj??ca okno generatora inwariant??w.
	 */
	public void showInvariantsWindow() {
		if(windowInvariants != null)
			windowInvariants.setVisible(true);
	}
	
	/**
	 * Metoda umo??liwa dost??p do okna generatora.
	 */
	public HolmesInvariantsGenerator accessInvariantsWindow() {
		if(windowInvariants != null)
			return windowInvariants;
		else
			return null;
	}

	public HolmesComparisonModule accessComparisonWindow() {
		if(windowsComp!= null)
			return windowsComp;
		else
			return null;
	}

	/**
	 * Metoda wywo??uj??ca okno generatora MCS.
	 */
	public void createMCSWindow() {
		if(windowMCS == null) {
			windowMCS = new HolmesMCS();
			windowMCS.setVisible(false);
		}
	}
	
	/**
	 * Metoda pokazuj??ca okno generatora zbior??w MCS.
	 */
	public void showMCSWindow() {
		if(windowMCS != null)
			windowMCS.setVisible(true);
	}
	
	/**
	 * Metoda umo??liwia dost??p do obiektu okna narzedzi MCS
	 * @return
	 */
	public HolmesMCS accessMCSWindow() {
		if(windowMCS != null)
			return windowMCS;
		else
			return null;
	}
	
	/**
	 * Metoda tworzy nowe okno analizatora wyklucze??.
	 */
	public void createKnockoutWindow() {
		if(windowsKnockout == null) {
			windowsKnockout = new HolmesKnockout();
		}
	}

	/**
	 * Metoda tworzy nowe okno analizatora dekompozycji.
	 */
	public void createDecompositionWindow() {
		if(windowsDeco == null) {
			windowsDeco = new HolmesDecomposition();
		}
	}

	/**
	 * Metoda tworzy nowe okno analizatora graphlet??w.
	 */
	public void createGraphletsWindow() {
		if(windowsGraphlet == null) {
			windowsGraphlet = new HolmesGraphlets();
		}
	}

	/**
	 * Metoda tworzy nowe okno por??wnywania z u??yciem Labeli.
	 */
	public void createLabelComparisonWindow() {
		if(labelComparison == null) {
			labelComparison = new HolmesLabelComparison();
		}
	}

	/**
	 * Metoda tworzy prototypowe okno dla branchowych wierzcho??k??w.
	 */
	public void createBranchWindow() {
		if(windowsBranch == null) {
			windowsBranch = new HolmesBranchVerticesPrototype();
		}
	}

	/**
	 * Metoda tworzy prototypowe okno por??wnywarki sieci.
	 */
	public void createComparisonnWindow() {
		if(windowsComp == null) {
			windowsComp = new HolmesComparisonModule();
		}
	}

	/**
	 * Metoda tworzy prototypowe okno do redukcji sieci.
	 */
	public void createReductionWindow() {
		if(windowReduction == null) {
			windowReduction = new HolmesReductionPrototype();
		}
	}


	/**
	 * Metoda pokazuje okno analizatora wyklucze??.
	 */
	public void showKnockoutWindow() {
		if(windowsKnockout != null) {
			windowsKnockout.setVisible(true);
		}
	}

	public void showDecoWindow() {
		if(windowsDeco != null) {
			windowsDeco.setVisible(true);
		}
	}

	public void showGraphletsWindow() {
		if(windowsGraphlet != null) {
			windowsGraphlet.setVisible(true);
		}
	}

	public void showReductionsWindow() {
		if(windowReduction != null) {
			windowReduction.setVisible(true);
		}
	}

	public void showBranchWindow() {
		if(windowsBranch != null) {
			windowsBranch.setVisible(true);
		}
	}

	public void showCompWindow() {
		if(windowsComp != null) {
			windowsComp.setVisible(true);
		}
	}

	public void showLabelCompWindow() {
		if(labelComparison != null) {
			labelComparison.setVisible(true);
		}
	}

	/**
	 * Metoda zapisuj??ca nowe zdarzenie w oknie log??w.
	 * @param text String - tekst zdarzenia
	 * @param mode String - tryb zapisu w oknie
	 * @param time boolean - true, je??li ma by?? podany czas zdarzenia
	 */
	public void log(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, true);
		if(mode.equals("error"))
			windowConsole.setVisible(true);
	}
	
	/**
	 * Metoda zapisuj??ca nowe zdarzenie w oknie log??w - bez Enter na ko??cu.
	 * @param text String - tekst zdarzenia
	 * @param mode String - tryb zapisu w oknie
	 * @param time boolean - true, je??li ma by?? podany czas zdarzenia
	 */
	public void logNoEnter(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, false);
		if(mode.equals("error"))
			windowConsole.setVisible(true);
	}
	
	/**
	 * Metoda zwraca status ??rodowiska narz??dziowego R.
	 * @return boolean - true, je??li Holmes dysponuje prawid??ow?? ??cie??k?? dost??pu do Rscript.exe
	 */
	public boolean getRStatus() {
		return rReady;
	}
	
	/**
	 * Zwraca status gotowo??ci programu INAwin32.exe
	 * @return boolean - true, je??li wszystko ok (by??o, na starcie programu Holmes).
	 */
	public boolean getINAStatus() {
		return inaReady;
	}
	
	/**
	 * Metoda ustawie flag?? gotowo??ci ??rodowiska R.
	 * @param status boolean - true, je??li znana jest ??cie??ka dost??pu do Rscript.exe
	 */
	public void setRStatus(boolean status) {
		rReady = status;
	}
	
	/**
	 * Metoda zwraca obiekt kontroluj??cy ustawienia programu.
	 * @return SettingsManager
	 */
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}
	
	/**
	 * Metoda ustawia lub resetuje tryb zmieniania lokalizacji napisu dla klikni??tego wierzcho??ka.
	 * Faktyczna realizacja tego trybu odbywa si?? w GraphPanel.MouseWheelHandler.mouseWheelMoved(MouseWheelEvent e)
	 * @param n Node - wybrany wierzcho??ek
	 * @param el ElementLocation - wybrana lokalizacja wierzcho??ka (portal)
	 * @param mode boolean - tryb: true je??li przesuwamy.
	 */
	public void setNameLocationChangeMode(Node n, ElementLocation el, boolean mode) {
		this.nameSelectedNode = n;
		this.nameNodeEL = el;
		this.nameLocChangeMode = mode;
	}
	
	/**
	 * Metoda zwraca warto???? flagi dla trybu zmiany lokalizacji nazwy wybranego wierzcho??ka sieci.
	 * @return boolean - true, je??li w????czony tryb zmiany lokalizacji nazwy
	 */
	public boolean getNameLocChangeMode() {
		return nameLocChangeMode;
	}
	
	/**
	 * Metoda zwraca wierzcho??ek dla kt??rego trwa zmiana lokalizacji nazwy.
	 * @return Node - wierzcho??ek sieci
	 */
	public Node getNameLocChangeNode() {
		return nameSelectedNode;
	}
	
	/**
	 * Metoda zwraca lokalizacj?? wierzcho??ka dla kt??rego trwa zmiana po??o??enia jego nazwy.
	 * @return ElementLocation - lokalizacja wierzcho??ka
	 */
	public ElementLocation getNameLocChangeEL() {
		return nameNodeEL;
	}
	
	/**
	 * Metoda ustawia flag?? w projekcie na true, oznaczaj??c?? jak??kolwiek zmian?? od czasu ostatniego zapisu.
	 * U??ywana do ostrze??e?? przed wyj??ciem z programu / czyszczeniem projektu.
	 */
	public void markNetChange() {
		getWorkspace().getProject().anythingChanged = true;
	}
	
	/**
	 *  Metoda ustawia flag?? w projekcie na false, oznaczaj??c?? ??e w??a??nie zapisano sie?? do pliku.
	 *  U??ywana do ostrze??e?? przed wyj??ciem z programu / czyszczeniem projektu.
	 */
	public void markNetSaved() {
		getWorkspace().getProject().anythingChanged = false;
	}
	
	/**
	 * Metoda zwraca warto???? flagi zmiany sieci.
	 * @return boolean - je??li true, to znaczy ??e sie?? si?? zmieni???? od ostatniego zapisu.
	 */
	public boolean getNetChangeStatus() {
		return getWorkspace().getProject().anythingChanged;
	}
	
	/**
	 * Metoda sprawdza do jakiego komponentu nadrz??dnego nale??y przes??any w parametrze i go
	 * stamt??d usuwa.
	 * @param x Dockable - komponent
	 */
	public void cleanLostOne(Dockable x) {
		Dock xxx = x.getDock();
		CompositeDock yyy = xxx.getParentDock();
		yyy.emptyChild(xxx);
	}
	
	/**
	 * Metoda usuwa wszystkie panele podsieci nie b??d??ce zadokowanymi w oknie worksheet.
	 */
	public void cleanDockables() {
		ArrayList<Dockable> activeSheets = getWorkspace().getDockables();
		
		for(Dockable d : globalSheetsList){
			if(!activeSheets.contains(d))
				cleanLostOne(d);
		}
	}
}
