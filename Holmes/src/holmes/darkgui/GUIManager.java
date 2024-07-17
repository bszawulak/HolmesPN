package holmes.darkgui;

import holmes.analyse.MCTCalculator;
import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.dockwindows.HolmesDockWindow;
import holmes.darkgui.dockwindows.PetriNetTools;
import holmes.darkgui.dockwindows.HolmesDockWindow.DockWindowType;
import holmes.darkgui.settings.SettingsManager;
import holmes.darkgui.toolbar.Toolbar;
import holmes.files.io.TexExporter;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.subnets.SubnetsControl;
import holmes.petrinet.subnets.SubnetsGraphics;
import holmes.utilities.Tools;
import holmes.windows.*;
import holmes.windows.clusters.HolmesClusters;
import holmes.windows.decompositions.*;
import holmes.windows.ssim.HolmesSim;
import holmes.windows.xtpn.HolmesSimXTPN;
import holmes.workspace.ExtensionFileFilter;
import holmes.workspace.Workspace;
import holmes.workspace.WorkspaceSheet;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;


/**
 * Główna klasa programu odpowiedzialna za właściwie wszystko. Zaczyna od utworzenia elementów
 * graficznych programu, a dalej jakoś tak samo już się wszystko toczy. Albo wywala.
 * 
 * @author students - ktoś musiał zacząć.
 * @author MR - Metody, Metody. Nowe Metody. сделал: Цириль
 */
public class GUIManager extends JPanel implements ComponentListener {
	@Serial
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

	// settings
	private final SettingsManager settingsManager;
	
	// main Docks
	private Workspace workspace;
	private JTabbedPane tabbedWorkspace = new JTabbedPane();

	private PetriNetTools toolBox;
	
	// podokna głównego okna programu
	private HolmesDockWindow simulatorBox;	//podokno przycisków symulatorów sieci
	private HolmesDockWindow selectionBox;	//podokno zaznaczonych elementów sieci //06.2023: ni ma. Widział ktoś??
	private HolmesDockWindow mctBox;			//podokno MCT
	private HolmesDockWindow t_invariantsBox;	//podokno t-inwariantów
	private HolmesDockWindow p_invariantsBox;	//p-inwarianty
	private HolmesDockWindow selElementBox;  //podokno klikniętego elementu sieci
	private HolmesDockWindow clustersBox;	//podokno podświetlania klastrów
	private HolmesDockWindow mcsBox;	//minimal cuttting sets
	private HolmesDockWindow fixBox;
	private HolmesDockWindow knockoutBox;	//symulator knockout
	private HolmesDockWindow quickSimBox;  //szybki symulator
	//-//private HolmesDockWindow decompositionBox;
	
	//UNUSED
	// docking listener
	//private DarkDockingListener dockingListener;
	private Toolbar shortcutsBar;

	// main frame
	private JFrame frame;	//główna ramka okna programu, tworzona w Main()
	// other components
	private DarkMenu menu;	//komponent menu programu

	// inne ważne zmienne
	private String lastPath;	// ostatnia otwarta scieżka
	private String holmesPath; 	// scieżka dostępu do katalogu głównego programu
	private String tmpPath;		// ścieżka dostępu do katalogu plików tymczasowych
	private String toolPath;	// ścieżka dostępu do katalogu narzedziowego
	private String logPath;
	
	// okna niezależne:
	private HolmesClusters windowClusters; //okno tabeli
	private HolmesConsole windowConsole; //konsola logów
	private HolmesNetProperties windowNetProperties; //okno właściwości sieci
	private HolmesAbout windowAbout; //okno About...
	private HolmesSearch windowSearch; //okno wyszukiwania elementów sieci
	private HolmesProgramProperties windowProperties; //okno właściwości sieci
	private HolmesSim windowStateSim; //okno symulatora stanów
	private HolmesSimXTPN windowStateSimXTPN; //okno symulatora stanów
	private HolmesNetTables windowNetTables; //okno tabel sieci
	private HolmesNotepad windowSimulationLog; //okno logów symulatora
	private HolmesInvariantsGenerator windowInvariants; //okno generatora inwariantów
	private HolmesMCS windowMCS; //okno generatora MCS
	private HolmesKnockout windowsKnockout;
	private HolmesDecomposition windowsDeco;
	private HolmesGraphlets windowsGraphlet;
	private HolmesLabelComparison labelComparison;
	private HolmesBranchVerticesPrototype windowsBranch;
	//private HolmesSubnetComparison windowsComp;
	private HolmesComparisonModule windowsComp;
	private HolmesReductionPrototype windowReduction;

	private boolean rReady = false; // true, jeżeli program ma dostęp do pliku Rscript.exe
	private boolean inaReady = true;

	static private boolean isXTPNmode = false;

	/**
	 * NONE, NAME, ALPHA, BETA, GAMMA, TAU
	 */
	public enum locationMoveType {NONE, NAME, ALPHA, BETA, GAMMA, TAU}
	private locationMoveType nameLocChangeMode = locationMoveType.NONE;
	private Node nameSelectedNode = null;
	private ElementLocation nameNodeEL = null;

	public JPanel propericeTMPBox;
	public JTabbedPane analysisTabs;
	
	/**
	 * Konstruktor obiektu klasy GUIManager.
	 * @param frejm JFrame - główna ramka kontener programu
	 */
	public GUIManager(JFrame frejm) {
		super(new BorderLayout());
		/*
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look and feel.
		}

		 */

		//JavaDocking wysypuje się jeśli numer wersji nie posiada przynajmniej jednej .
		//Piękny był to fuckup, nie zapomnę go nigdy [MR].
		// [2024]Już nie ważne, ale zostawmy na pamiątkę.
		//if(!System.getProperty("java.version").contains("."))
		//	System.setProperty("java.version",System.getProperty("java.version")+".0");

		guiManager = this;
		io = new GUIOperations(this); //obiekt klasy operacji głównych
		tex = new TexExporter(); //obiekt zarządzający eksportem tabel do formatu latex
		reset = new GUIReset(); //obiekt odpowiadający za resetowanie danych / kasowanie / czyszczenie
		subnetsGraphics = new SubnetsGraphics(); //obiekt z metodami graficznymi dla sieci hierarchicznych
		subnetsHQ = new SubnetsControl(this); //obiekt z metodami zarządzania sieciami hierarchicznymi
		simSettings = new SimulatorGlobals(); //opcje symulatora
		
		setFrame(frejm);
		try {	
			frame.setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {
			System.out.println(e.getMessage());
		}
		
		//frame.getContentPane().add(this);
		frame.addComponentListener(this);
		//getFrame().getContentPane().add(this);
		getFrame().addComponentListener(this);
		
		createHiddenConsole(); // okno konsoli logowania zdarzeń
		
		settingsManager = new SettingsManager();
		settingsManager.loadSettings();
		frame.setTitle("Holmes "+settingsManager.getValue("holmes_version"));
		
		
		createClusterWindow(); // okno tabeli klastrów
		createNetPropertiesWindow(); // okno właściwości sieci
		createSearchWindow(); // okno wyszukiwania elementów sieci
		createNetTablesWindow(); // okno tabel sieci
		createInvariantsWindow(); // okno generatora inwariantów
		
		
		initializeEnvironment(); //wczytuje ustawienia, ustawia wewnętrzne zmienne programu
		
		// Set the frame properties and show it.
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		//JFrame.setDefaultLookAndFeelDecorated(true);
		
		getFrame().setLocation((int) (screenSize.width * 0.1) / 2, (int) (screenSize.height * 0.1) / 2);
		getFrame().setSize((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));
		//getFrame().setExtendedState(getFrame().getExtendedState() | JFrame.MAXIMIZED_BOTH);
		getFrame().setVisible(true);

		boolean startMaximized = Boolean.parseBoolean(settingsManager.getValue("mainWindowStartMaximized"));
		startMaximized = true; // fot the moment

		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;

		float aspect = (float)screenWidth / (float)screenHeight;
		if(aspect > 2.0) {
			getFrame().setSize(screenHeight*2, screenHeight-40);
			getFrame().setExtendedState(getFrame().getExtendedState() | JFrame.NORMAL);
			getFrame().setLocation(-7, -1);
		} else {
			if(startMaximized) {
				getFrame().setExtendedState(getFrame().getExtendedState() | JFrame.MAXIMIZED_BOTH);
			} else {
				int width = Integer.parseInt(settingsManager.getValue("mainWindowWidth"));
				int height = Integer.parseInt(settingsManager.getValue("mainWindowHeight"));

				getFrame().setSize(width, height);
				getFrame().setExtendedState(getFrame().getExtendedState() | JFrame.NORMAL);
			}
		}

		getFrame().setVisible(true);

		// set docking listener
		//setDockingListener(new DarkDockingListener());
		setToolBox(new PetriNetTools());

		// create workspace
		createSimLogWindow(); // okno logów symulatora

		setWorkspace(new Workspace(this));
		getWorkspace().newTab(false, new Point(0,0), 1, MetaNode.MetaType.SUBNET);
		getTabbedWorkspace().setPreferredSize(new Dimension(1300,400));

		// create sub panels for main frame:
		setPropertiesBox(new HolmesDockWindow(DockWindowType.EDITOR));
		setSimulatorBox(new HolmesDockWindow(DockWindowType.SIMULATOR));
		setSelectionBox(new HolmesDockWindow(DockWindowType.SELECTOR));
		setT_invBox(new HolmesDockWindow(DockWindowType.T_INVARIANTS));
		setP_invSim(new HolmesDockWindow(DockWindowType.P_INVARIANTS));
		setClusterSelectionBox(new HolmesDockWindow(DockWindowType.ClusterSELECTOR));
		setMctBox(new HolmesDockWindow(DockWindowType.MctANALYZER)); //aktywuj obiekt podokna wyświetlania zbiorów MCT
		setMCSBox(new HolmesDockWindow(DockWindowType.MCSselector));
		setKnockoutBox(new HolmesDockWindow(DockWindowType.Knockout));
		setQuickSimBox(new HolmesDockWindow(DockWindowType.QuickSim));
		setFixBox(new HolmesDockWindow(DockWindowType.FIXNET));
		//setDecompositionBox(new HolmesDockWindow(DockWindowType.DECOMPOSITION));

		// create menu
		setMenu(new DarkMenu());
		getFrame().setJMenuBar(getMenu());

		// default screen size unmaximized
		smallScreenSize = new Dimension((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));
		setShortcutsBar(new Toolbar());
		getFrame().add(shortcutsBar,BorderLayout.PAGE_START);

		JPanel mainpanel = new JPanel();
		//JScrollPane jsp = getWorkspace().getSelectedSheet().getScrollPane();
		//mainpanel.add(jsp);	//03072023 do not work
		mainpanel.add(getWorkspace().getSelectedSheet());	//03072023 old code

		mainpanel.setLayout(new BorderLayout());
		mainpanel.setSize(200,200);

		//getFrame().add(jsp,  BorderLayout.CENTER); //03072023 do not work
		getFrame().add(getWorkspace().getSelectedSheet(),  BorderLayout.CENTER); //03072023 old code


		// create tools
		//getFrame().add(getToolBox().getTree(),BorderLayout.LINE_START);
		//getFrame().add(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getPanel());
		propericeTMPBox = GUIManager.getDefaultGUIManager().getPropertiesBox().getCurrentDockWindow();
		propericeTMPBox.setPreferredSize(new Dimension(200, 400));

		//getFrame().add(propericeTMPBox,BorderLayout.LINE_END);
		JTabbedPane leftCentralPanel = new JTabbedPane();
		leftCentralPanel.add(getToolBox().getTree());
		leftCentralPanel.setTabComponentAt(0, new JLabel("Toolbox"));
		leftCentralPanel.add(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getPanel());
		leftCentralPanel.setTabComponentAt(1, new JLabel("Simulator"));
		leftCentralPanel.setPreferredSize(new Dimension(200,400));

		analysisTabs = new JTabbedPane();
		analysisTabs.add(GUIManager.getDefaultGUIManager().getQuickSimBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(0, new JLabel("QuickSim"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getT_invBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(1, new JLabel("T-Inv"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getP_invBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(2, new JLabel("P-Inv"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getMctBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(3, new JLabel("MCT"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getMCSBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(4, new JLabel("MCS"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getKnockoutBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(5, new JLabel("Knockout"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getFixBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(6, new JLabel("NetFix"));
		analysisTabs.add(GUIManager.getDefaultGUIManager().getClusterSelectionBox().getCurrentDockWindow().getPanel());
		analysisTabs.setTabComponentAt(7, new JLabel("Clusters"));
		analysisTabs.setPreferredSize(new Dimension(200,200));

		JSplitPane simAndworkspacePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCentralPanel , getTabbedWorkspace());
		leftCentralPanel.setPreferredSize(new Dimension(200, 400));
		simAndworkspacePanel.setPreferredSize(new Dimension(1200,200));
		simAndworkspacePanel.setDividerLocation(180);
		JSplitPane rightPanelSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, propericeTMPBox, analysisTabs);
		JSplitPane uberMainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, simAndworkspacePanel, rightPanelSplit);

		if(aspect > 2.0) {
			uberMainPanel.setDividerLocation(screenHeight*2 - 330); //pasek okien po prawej
		} else {
			uberMainPanel.setDividerLocation(this.screenSize.width - 350); //normal
		}

		rightPanelSplit.setDividerLocation(340); //podział pionowy prawego panelu

		getFrame().add(uberMainPanel,BorderLayout.CENTER);

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyManager(this));

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	boolean status = GUIManager.getDefaultGUIManager().getNetChangeStatus();
				if(status) {
					Object[] options = {"Exit", "Save and exit", "Cancel",};
					int n = JOptionPane.showOptionDialog(null,
									"Net or its data have been changed since last save. Exit, save&exit or do not exit now?",
									"Project has been modified", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					//cancel
					if (n == 0) {
						log("Exiting program","text",true);
						windowConsole.saveLogToFile(null);
						System.exit(0);
					} else if (n == 1) { //try to save
						boolean savingStatus = io.saveAsGlobal();
						if(!savingStatus) {
							return;
						} else {
							log("Exiting program","text",true);
			            	windowConsole.saveLogToFile(null);
			            	System.exit(0);
						}
					}
				} else if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the program?", "Exit?", 
		            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
		        		log("Exiting program","text",true);
		            	windowConsole.saveLogToFile(null);
		            	System.exit(0);
		        	}
		    	}
		});
		
		//na samym końcu, gdy już wszystko 'działa'
		//createPropertiesWindow();
		createStateSimulatorWindow();
		createStateSimulatorXTPNWindow();
		createMCSWindow(); // okno generatora MCS

		String path = settingsManager.getValue("lastOpenedPath");
		File f = new File(path);
		if(f.exists())
			lastPath = path;	

		//getSimulatorBox().createSimulatorProperties(false);
		getFrame().repaint();
	}

	/**
	 * Metoda pomocnicza konstruktora. Ustawia główne zmienne programu, wczytuje plik
	 * właściwości, itd.
	 */
	private void initializeEnvironment() {
		// ustawienie ścieżek dostępu
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
			
			//log("Something wrong with the INA tools directory.", "warning", true);
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
							+ " protection issues in program directory. Invariants generation using INAwin32 will"
							+ " most likely fail.","Critical error - writing", JOptionPane.ERROR_MESSAGE);
					inaReady = false;
					log("Critical error, unable to recreate ina.bat file. Invariants generator will not work.", "warning", true);
				}
			} 
			
			if(!checkFileINA1.exists()) { //no INAwin32.exe
				//log("INAwin32.exe missing in "+checkFileINA0+"directory. Please download "
				//		+ "manually from www2.informatik.hu-berlin.de/~starke/ina.html and put into the \\Tools directory.", "warning", true);
				inaReady = false;
			}
		}
		
		//check status of Rscript.exe location:
		checkRlangStatus(false);
		
		if(settingsManager.getValue("programDebugMode").equals("1"))
			debug = true;
	}

	/**
	 * Metoda uruchamiana na starcie programu oraz wtedy, gdy chcemy uzyskać dostęp do lokalizacji
	 * pliku Rscript.exe.
	 */
	public void checkRlangStatus(boolean forceCheck) {
		rReady = true;
		String Rpath = settingsManager.getValue("r_path");
		File rF = new File(Rpath);
		if(!rF.exists()) {
			rReady = false;
			log("Invalid path ("+Rpath+") to Rscript executable file.", "warning", true);
			if(!forceCheck) { //jeśli nie jest wymuszone sprawdzanie, sprawdź status settings
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
				if(selectedFile.isEmpty()) {
					log("Rscript executable file inaccessible. Some features (clusters operations) will be disabled.", "error", true);
				} else {
					if(!selectedFile.contains("x64")) { //jeśli wskazano 64b
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
	 * Metoda odpowiedzialna za ustalenie domyślnych lokalizacji pasków zmiany rozmiaru
	 * podokien programu (Dividers).
	 */
	private void resetSplitDocks() {
		if (getFrame().getExtendedState() == JFrame.MAXIMIZED_BOTH) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		} else {
			smallScreenSize = getFrame().getSize();
		}
	}

	/**
	 * Metoda zwraca ścieżki do ostatio używanego katalagu.
	 * @return String - ścieżka do katalogu
	 */
	public String getLastPath() {
		return lastPath;
	}
	
	/**
	 * Metoda ustawia nową ścieżkę do ostatnio używanego katalagu. Zapisuje ją do pliku ustawień programu.
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
	 * Czyszczenie modułów
	 */
	public void resetModuls(){
		windowsComp = new HolmesComparisonModule();
		windowsBranch = new HolmesBranchVerticesPrototype();
		windowsGraphlet = new HolmesGraphlets();
		windowsDeco = new HolmesDecomposition();
		windowsKnockout = new HolmesKnockout();
	}

	/**
	 * Diabli wiedzą co i kiedy wywołuje tą metodę, tym niemniej zleca ona innej ustalenie
	 * domyślnych lokalizacji pasków zmiany rozmiarów podokien (Dividers).
	 */
	public void componentResized(ComponentEvent arg0) {
		resetSplitDocks();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}
	
	/**
	 * Metoda zwraca obiekt podokna do wyświetlania zbiorów MCT.
	 * @return HolmesDockWindow - okno wyboru MCT
	 */
	public HolmesDockWindow getMctBox() {
		return mctBox;
	}

	/**
	 * Metoda ustawia nowe podokno dedykowane do wyświetlania zbiorów MCT.
	 * @param mctBox HolmesDockWindow - nowe okno wyboru MCT
	 */
	private void setMctBox(HolmesDockWindow mctBox) {
		this.mctBox = mctBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna wyświetlania zbiorów MCS.
	 * @return HolmesDockWindow - okno wyboru MCS
	 */
	public HolmesDockWindow getMCSBox() {
		return mcsBox;
	}
	/**
	 * Metoda ustawia nowe podokno dedykowane do wyświetlania zbiorów MCS.
	 * @param mcsBox HolmesDockWindow - nowe okno wyboru MCS
	 */
	private void setMCSBox(HolmesDockWindow mcsBox) {
		this.mcsBox = mcsBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna naprawy sieci.
	 * @return HolmesDockWindow - okno naprawcze
	 */
	public HolmesDockWindow getFixBox() {
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
	 * Metoda ustawia nowe podokno dedykowane do wyświetlania zbiorów knockout.
	 * @param knockoutBox HolmesDockWindow - nowe okno wyboru knockoutBox
	 */
	private void setKnockoutBox(HolmesDockWindow knockoutBox) {
		this.knockoutBox = knockoutBox;
	}

	/**
	 * Metoda zwraca obiekt podokna wyświetlania zbiorów knockout.
	 * @return HolmesDockWindow - okno wyboru knockoutBox
	 */
	public HolmesDockWindow getKnockoutBox() {
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
	 * Metoda zwraca obiekt podokna symulatora QuickSim.
	 * @return HolmesDockWindow - okno QuickSim
	 */
	private HolmesDockWindow getQuickSimBox() {
		return quickSimBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna dla podświetlania inwariantów sieci.
	 * @return HolmesDockWindow - podokno inwariantów
	 */
	public HolmesDockWindow getT_invBox() {
		return t_invariantsBox;
	}

	/**
	 * Metoda ta ustawia obiekt podokna dla podświetlania inwariantów sieci.
	 * @param invariantsBox HolmesDockWindow - podokno inwariantów
	 */
	private void setT_invBox(HolmesDockWindow invariantsBox) {
		this.t_invariantsBox = invariantsBox;
	}
	
	/**
	 * Metoda ustawia nowe okno właściwości p-inwariantów.
	 * @param invSim HolmesDockWindow - okno p-inwariantów
	 */
	private void setP_invSim(HolmesDockWindow invSim)
	{
		this.p_invariantsBox = invSim;
	}
	
	/**
	 * Metoda zwraca aktywne okno właściwości p-inwariantów.
	 */
	public HolmesDockWindow getP_invBox() {
		return p_invariantsBox;
	}

	/**
	 * Metoda odpowiedzialna za zwrócenie obiektu obszaru roboczego.
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
	 * Metoda zwraca obiekt paska narzędziowego.
	 * @return PetriNetTools - pasek przycisków
	 */
	public PetriNetTools getToolBox() {
		return toolBox;
	}

	/**
	 * Metoda ustawia nowy obiekt paska narzędziowego.
	 * @param toolBox PetriNetTools - pasek przycisków
	 */
	private void setToolBox(PetriNetTools toolBox) {
		this.toolBox = toolBox;
	}

	/**
	 * Metoda zwraca obiekt podokna do wyświetlania właściwości klikniętego elementu sieci.
	 * @return HolmesDockWindow - podokno właściwości
	 */
	public HolmesDockWindow getPropertiesBox() {
		return selElementBox;
	}

	/**
	 * Metoda ustawia nowy obiekt podokna do wyświetlania właściwości klikniętego elementu sieci.
	 * @param propertiesBox HolmesDockWindow - podokno właściwości
	 */
	public void setPropertiesBox(HolmesDockWindow propertiesBox) {
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

	/*
	 * Opis: I have no idea...
	 * @return FloatDockModel
	 */
	//public FloatDockModel getDockModel() {
	//	return dockModel;
	//}

	/*
	 * Opis: I have no idea...
	 * @param dockModel FloatDockModel
	 */
	//private void setDockModel(FloatDockModel dockModel) {
	//	this.dockModel = dockModel;
	//}

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
	 * Metoda ta zwraca obiekt okna wyboru elementów.
	 * @return HolmesDockWindow - okno wyboru elementów
	 */
	public HolmesDockWindow getSelectionBox() {
		return selectionBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna wyboru elementów.
	 * @param selectionBox HolmesDockWindow - okno wyboru elementów
	 */
	private void setSelectionBox(HolmesDockWindow selectionBox) {
		this.selectionBox = selectionBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna przycisków programu.
	 * @return Toolbar - obiekt okna przycisków
	 */
	private Toolbar getShortcutsBar() {
		return shortcutsBar;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna przycisków programu.
	 * @param shortcutsBar Toolbar - obiekt okna przycisków
	 */
	private void setShortcutsBar(Toolbar shortcutsBar) {
		this.shortcutsBar = shortcutsBar;
	}
	
	/**
	 * Metoda zwraca obiekt podokna wyboru klastrów do podświetlania.
	 * @return HolmesDockWindow - obiekt z właściwościani sieci
	 */
	public HolmesDockWindow getClusterSelectionBox() {
		return clustersBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt podokna wyboru klastrów do podświetlania.
	 * @param clusterBox HolmesDockWindow - obiekt z właściwościami sieci
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
	 * Metoda zwraca ścieżkę do katalogu narzedzi.
	 * @return String
	 */
	public String getToolPath() {
		return toolPath;
	}
	
	/**
	 * Metoda zwraca ścieżkę do katalogu programu.
	 * @return String
	 */
	public String getHolmesPath() {
		return holmesPath;
	}
	
	/**
	 * Metoda zwraca ścieżkę do katalogu plików tymczasowych.
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
	 * Główna metoda odpowiedzialna za generowanie zbiorów MCT.
	 */
	public void generateMCT() {
		MCTCalculator analyzer = getWorkspace().getProject().getMCTanalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		getWorkspace().getProject().setMCTMatrix(mct, true);
		getMctBox().showMCT(mct); //sortowanie
	}
	
	/**
	 * Metoda wywołuje funkcję odświeżenia zawartości okna MCS.
	 */
	public void showMCS() {
		getMCSBox().showMCS();
	}
	
	/**
	 * Metoda pokazuje podokienko zbiorów Knockout.
	 */
	public void showKnockout(ArrayList<ArrayList<Integer>> knockoutData) {
		getKnockoutBox().showKnockout(knockoutData);
	}
	
	/**
	 * Metoda zleca wyświetlenie podokna podświetlania klastrowania
	 */
	public void showClusterSelectionBox(ClusterDataPackage data){
		getClusterSelectionBox().showClusterSelector(data);
		GUIManager.getDefaultGUIManager().reset.setClustersStatus(true); //status klastrów: wczytane
	}
	
	/**
	 * Metoda rozpoczyna symulację uruchamiania inwariantów.
	 * @param type int - 0-basic, 1- time
	 * @param value - wartość
	 * @throws CloneNotSupportedException ex
	 */
	public void startInvariantsSimulation(int type, int value) throws CloneNotSupportedException{
		this.getWorkspace().getProject().startInvSim(type, value);
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy nowe okno tabeli klastrów.
	 */
	private void createClusterWindow() {
		windowClusters = new HolmesClusters(0);
	}
	
	/**
	 * Metoda służy do wyświetlania okna klastrów.
	 */
	public void showClusterWindow() {
		if(windowClusters != null) {
			windowClusters.setVisible(true);
		}
	}
	
	/**
	 * Dostęp do okna tabel klastrów.
	 * @return HolmesClusters - obiekt okna
	 */
	public HolmesClusters accessClusterWindow() {
		return windowClusters;
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy okno właściwości sieci.
	 */
	private void createNetPropertiesWindow() {
		windowNetProperties = new HolmesNetProperties();
		windowNetProperties.setLocationRelativeTo(frame);
	}
	
	/**
	 * Metoda służy do wyświetlania okna właściwości sieci.
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
	 * Metoda ta tworzy nowe okno szukania elementów sieci.
	 */
	private void createSearchWindow() {
		windowSearch = new HolmesSearch();
	}
	
	/**
	 * Metoda wyświetla okno szukania elementów sieci na ekranie.
	 */
	public void showSearchWindow() {
		if(windowSearch != null) {
			windowSearch.setVisible(true);
		}
	}
	
	/**
	 * Metoda ta zwraca obiekt okna wyszukiwania elementów, używana przez StateSimulator.
	 * @return HolmesSearch - okno wyszukiwania elementów
	 */
	public HolmesSearch getSearchWindow() {
		return windowSearch;
	}
	
	/**
	 * Metoda tworzy nowe okno właściwości programu.
	 */
	private void createPropertiesWindow() {
		windowProperties = new HolmesProgramProperties(frame);
	}
	
	/**
	 * Metoda pokazuje okno właściwości programu.
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
	 * Metoda tworzy nowe okno symulatora stanów programu.
	 */
	private void createStateSimulatorWindow() {
		windowStateSim = new HolmesSim(this);
	}
	
	/**
	 * Metoda zwraca obiekt okna symulatora.
	 * @return HolmesStateSimulator - obiekt okna symulatora.
	 */
	public HolmesSim accessStateSimulatorWindow() {
		return windowStateSim;
	}
	
	/**
	 * Metoda pokazuje okno symulatora stanów programu.
	 */
	public void showStateSimulatorWindow() {
		if(windowStateSim != null) {
			windowStateSim.setVisible(true);
			//this.getFrame().setEnabled(false);
		}
	}

	/**
	 * Metoda tworzy nowe okno symulatora stanów XTPN programu.
	 */
	private void createStateSimulatorXTPNWindow() {
		windowStateSimXTPN = new HolmesSimXTPN(this);
	}

	/**
	 * Metoda zwraca obiekt okna symulatora XTPN.
	 * @return HolmesStateSimulatorXTPN - obiekt symulatora XTPN.
	 */
	public HolmesSimXTPN accessStateSimulatorXTPNWindow() {
		return windowStateSimXTPN;
	}

	/**
	 * Metoda pokazuje okno symulatora stanów XTPN programu.
	 */
	public void showStateSimulatorWindowXTPN() {
		if(windowStateSimXTPN != null) {
			windowStateSimXTPN.setVisible(true);
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
				if(!reset.isXTPNSimulatorActiveWarning("Warning: XTPN simulator active. Cannot proceed until manually stopped."
						, "Net simulator working")) {
					windowNetTables.setVisible(true);
				}
			}
		}
	}
	
	/**
	 * Metoda pozwala na dostęp do obiektu okna tabel sieci.
	 * @return HolmesNetTables - obiekt okna danych (tabel) sieci
	 */
	public HolmesNetTables accessNetTablesWindow() {
		return windowNetTables;
	}
	
	/**
	 * Metoda tworzy nowe okno logów symulatora sieci (na bazie okna notatnika programu).
	 */
	private void createSimLogWindow() {
		windowSimulationLog = new HolmesNotepad(900,600);
		windowSimulationLog.setVisible(false);
	}
	
	/**
	 * Metoda pokazuje okno logów symulatora sieci (okno na bazie okna notatnika programu).
	 */
	public void showSimLogWindow() {
		if(windowSimulationLog != null) {
			windowSimulationLog.setVisible(true);
		}
	}
	
	/**
	 * Metoda zwraca obiekt notatnika logów symulatora programu.
	 * @return HolmesNotepad - obiekt okna symulatora
	 */
	public HolmesNotepad getSimLog() {
		return windowSimulationLog;
	}
	
	/**
	 * Metoda pomocnicza konstruktora, służy do tworzenia ukrytego okna konsoli logów.
	 */
	private void createHiddenConsole() {
		windowConsole = new HolmesConsole();
		windowConsole.setLocationRelativeTo(this);
	}
	
	/**
	 * Metoda służy do pokazywania lub chowania okna konsoli.
	 */
	public void showConsole(boolean value) {
		if(windowConsole != null) {
			windowConsole.setVisible(value);
		}
	}
	
	/**
	 * Metoda tworząca okno generatora inwariantów.
	 */
	public void createInvariantsWindow() {
		windowInvariants = new HolmesInvariantsGenerator();
		windowInvariants.setVisible(false);
	}
	
	/**
	 * Metoda pokazująca okno generatora inwariantów.
	 */
	public void showInvariantsWindow() {
		if(windowInvariants != null)
			windowInvariants.setVisible(true);
	}
	
	/**
	 * Metoda umożliwa dostęp do okna generatora.
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
	 * Metoda wywołująca okno generatora MCS.
	 */
	public void createMCSWindow() {
		if(windowMCS == null) {
			windowMCS = new HolmesMCS();
			windowMCS.setVisible(false);
		}
	}
	
	/**
	 * Metoda pokazująca okno generatora zbiorów MCS.
	 */
	public void showMCSWindow() {
		if(windowMCS != null)
			windowMCS.setVisible(true);
	}
	
	/**
	 * Metoda umożliwia dostęp do obiektu okna narzędzi MCS.
	 * @return (<b>HolmesMCS</b>) obiekt okna MCS.
	 */
	public HolmesMCS accessMCSWindow() {
		if(windowMCS != null)
			return windowMCS;
		else
			return null;
	}
	
	/**
	 * Metoda tworzy nowe okno analizatora wykluczeń.
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
	 * Metoda tworzy nowe okno analizatora graphletów.
	 */
	public void createGraphletsWindow() {
		if(windowsGraphlet == null) {
			windowsGraphlet = new HolmesGraphlets();
		}
	}

	/**
	 * Metoda tworzy nowe okno porównywania z użyciem Labeli.
	 */
	public void createLabelComparisonWindow() {
		if(labelComparison == null) {
			labelComparison = new HolmesLabelComparison();
		}
	}

	/**
	 * Metoda tworzy prototypowe okno dla branchowych wierzchołków.
	 */
	public void createBranchWindow() {
		if(windowsBranch == null) {
			windowsBranch = new HolmesBranchVerticesPrototype();
		}
	}

	/**
	 * Metoda tworzy prototypowe okno porównywarki sieci.
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
	 * Metoda pokazuje okno analizatora wykluczeń.
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
	 * Metoda zapisująca nowe zdarzenie w oknie logów.
	 * @param text (<b>String</b>) tekst zdarzenia.
	 * @param mode (<b>String</b>) tryb zapisu w oknie: <b>warning</b>, <b>error</b>, <b>text</b>, <b>italic</b>, <b>bold</b>
	 * @param time (<b>boolean</b>) true, jeśli ma być podany czas zdarzenia
	 */
	public void log(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, true);
		if(mode.equals("error"))
			windowConsole.setVisible(true);
	}
	
	/**
	 * Metoda zapisująca nowe zdarzenie w oknie logów - <b>bez Enter na końcu</b>.
	 * @param text (<b>String</b>) tekst zdarzenia.
	 * @param mode (<b>String</b>) tryb zapisu w oknie: <b>warning</b>, <b>error</b>, <b>text</b>, <b>italic</b>, <b>bold</b>
	 * @param time (<b>boolean</b>) true, jeśli ma być podany czas zdarzenia
	 */
	public void logNoEnter(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, false);
		if(mode.equals("error"))
			windowConsole.setVisible(true);
	}
	
	/**
	 * Metoda zwraca status środowiska narzędziowego R.
	 * @return boolean - true, jeśli Holmes dysponuje prawidłową ścieżką dostępu do Rscript.exe
	 */
	public boolean getRStatus() {
		return rReady;
	}
	
	/**
	 * Zwraca status gotowości programu INAwin32.exe
	 * @return boolean - true, jeśli wszystko ok (było, na starcie programu Holmes).
	 */
	public boolean getINAStatus() {
		return inaReady;
	}
	
	/**
	 * Metoda ustawie flagę gotowości środowiska R.
	 * @param status boolean - true, jeśli znana jest ścieżka dostępu do Rscript.exe
	 */
	public void setRStatus(boolean status) {
		rReady = status;
	}
	
	/**
	 * Metoda zwraca obiekt kontrolujący ustawienia programu.
	 * @return SettingsManager
	 */
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}
	
	/**
	 * Metoda ustawia lub resetuje tryb zmieniania lokalizacji napisu dla klikniętego wierzchołka.
	 * Faktyczna realizacja tego trybu odbywa się w GraphPanel.MouseWheelHandler.mouseWheelMoved(MouseWheelEvent e)
	 * @param n (<b>Node</b>) wybrany wierzchołek.
	 * @param el (<b>ElementLocation</b>) wybrana lokalizacja wierzchołka (portal).
	 * @param mode (<b>locationMoveType</b>) NONE, ALPHA, BETA, GAMMA, TAU.
	 */
	public void setNameLocationChangeMode(Node n, ElementLocation el, locationMoveType mode) {
		this.nameSelectedNode = n;
		this.nameNodeEL = el;
		this.nameLocChangeMode = mode;
	}
	
	/**
	 * Metoda zwraca wartość flagi dla trybu zmiany lokalizacji nazwy wybranego wierzchołka sieci.
	 * @return (<b>locationMoveType</b>) - NONE, NAME, ALPHA, BETA, GAMMA or TAU
	 */
	public locationMoveType getNameLocChangeMode() {
		return nameLocChangeMode;
	}
	
	/**
	 * Metoda zwraca wierzchołek dla którego trwa zmiana lokalizacji nazwy.
	 * @return Node - wierzchołek sieci
	 */
	public Node getNameLocChangeNode() {
		return nameSelectedNode;
	}
	
	/**
	 * Metoda zwraca lokalizację wierzchołka dla którego trwa zmiana położenia jego nazwy.
	 * @return ElementLocation - lokalizacja wierzchołka
	 */
	public ElementLocation getNameLocChangeEL() {
		return nameNodeEL;
	}
	
	/**
	 * Metoda ustawia flagę w projekcie na true, oznaczającą jakąkolwiek zmianę od czasu ostatniego zapisu.
	 * Używana do ostrzeżeń przed wyjściem z programu / czyszczeniem projektu.
	 */
	public void markNetChange() {
		getWorkspace().getProject().anythingChanged = true;
	}
	
	/**
	 *  Metoda ustawia flagę w projekcie na false, oznaczającą że właśnie zapisano sieć do pliku.
	 *  Używana do ostrzeżeń przed wyjściem z programu / czyszczeniem projektu.
	 */
	public void markNetSaved() {
		getWorkspace().getProject().anythingChanged = false;
	}
	
	/**
	 * Metoda zwraca wartość flagi zmiany sieci.
	 * @return (<b>boolean</b>) - jeśli true, to znaczy, że sieć się zmieniło od ostatniego zapisu.
	 */
	public boolean getNetChangeStatus() {
		return getWorkspace().getProject().anythingChanged;
	}

	public static boolean isXTPN_simMode() {  //jeżeli true - XTPN
		return isXTPNmode;
	}

	public static void setXTPN_simMode(boolean simMode) { //jeżeli false = PN, true = XTPN
		GUIManager.isXTPNmode = simMode;
	}

	public JTabbedPane getTabbedWorkspace() {
		return GUIManager.getDefaultGUIManager().getWorkspace().getTablePane();
	}

	public void setTabbedWorkspace(JTabbedPane tabbedWorkspace) {
		this.tabbedWorkspace = tabbedWorkspace;
	}

	public void testRemovePanel(int sheetID) {
		WorkspaceSheet begone = null;
		for(WorkspaceSheet ws : getWorkspace().getSheets()) {
			if(ws.getId() == sheetID) {
				begone = ws;
				break;
			}
		}

		if(begone != null) {
			getWorkspace().deleteSheetFromArrays(begone);
		}
	}
}
