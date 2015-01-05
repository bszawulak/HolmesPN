package abyss.darkgui;

import abyss.analyzer.DarkAnalyzer;
import abyss.clusters.ClusterDataPackage;
import abyss.darkgui.dockable.DeleteAction;
import abyss.darkgui.properties.AbyssDockWindow;
import abyss.darkgui.properties.PetriNetTools;
import abyss.darkgui.properties.AbyssDockWindow.DockWindowType;
import abyss.darkgui.toolbar.Toolbar;
import abyss.math.InvariantTransition;
import abyss.math.PetriNet;
import abyss.math.Transition;
import abyss.settings.SettingsManager;
import abyss.utilities.Tools;
import abyss.windows.AbyssAbout;
import abyss.windows.AbyssConsole;
import abyss.windows.AbyssClusters;
import abyss.windows.AbyssProperties;
import abyss.windows.AbyssSearch;
import abyss.workspace.ExtensionFileFilter;
import abyss.workspace.Workspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import com.javadocking.DockingManager;
import com.javadocking.component.DefaultSwComponentFactory;
import com.javadocking.dock.CompositeTabDock;
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
 * G��wna klasa programu odpowiedzialna za w�a�ciwie wszystko. Zaczyna od utworzenia element�w
 * graficznych programu, a dalej jako� tak si� samo ju� wszystko toczy. Albo wywala.
 * @author students - kto� musia� zacz�� :)
 * @author MR - Metody, Metody. Nowe Metody. Podpisano: Cyryl
 */
public class GUIManager extends JPanel implements ComponentListener {
	private static final long serialVersionUID = -817072868916096442L;
	// Static fields.
	private static GUIManager guiManager;
	/**
	 * Obiekt klasy GUIOperations, minion od czarnej roboty.
	 */
	public GUIOperations io;
	
	private Dimension screenSize; 		// praca w maksymalizacji
	private Dimension smallScreenSize;	// praca poza maksymalizowanym oknem
	private FloatDockModel dockModel;
	
	// settings
	private SettingsManager settingsManager;
	
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
	
	// podokna dokowalne g��wnego okna Abyss:
	private AbyssDockWindow simulatorBox;	//podokno przycisk�w symulator�w sieci
	private AbyssDockWindow selectionBox;	//podokno zaznaczonych element�w sieci
	private AbyssDockWindow mctBox;			//podokno MCT
	private AbyssDockWindow invariantsBox;	//podokno inwariant�w
	private AbyssDockWindow selElementBox;  //podokno klikni�tego elementu sieci
	private AbyssDockWindow clustersBox;	//podokno pod�wietlania klastr�w
	
	//UNUSED
	private AbyssDockWindow invSimBox;
	
	// docking listener
	private DarkDockingListener dockingListener;
	private Toolbar shortcutsBar;

	// main frame
	private JFrame frame;	//g��wna ramka okna programu, tworzona w Main()
	// other components
	private DarkMenu menu;	//komponent menu programu

	// inne wa�ne zmienne
	private String lastPath;	// ostatnia otwarta �cie�ka
	private String abyssPath; 	// scie�ka dost�pu do katalogu g��wnego programu
	private String tmpPath;		// �cie�ka dost�pu do katalogu plik�w tymczasowych
	private String toolPath;	// �cie�ka dost�pu do katalogu narzedziowego
	private String logPath;
	
	// okna niezale�ne:
	public AbyssClusters windowClusters; //okno tabeli 
	private AbyssConsole windowConsole; //konsola log�w
	private AbyssProperties windowNetProperties; //okno w�a�ciwo�ci sieci
	private AbyssAbout windowAbout; //okno About...
	private AbyssSearch windowSearch;
	
	private boolean rReady = false; // true, je�li program dost�p do pliku Rscript.exe
	/**
	 * Konstruktor obiektu klasy GUIManager.
	 * @param frejm JFrame - g��wna ramka kontener programu
	 */
	public GUIManager(JFrame frejm) {
		super(new BorderLayout());
		guiManager = this;
		io = new GUIOperations(this); //rise, my minion!!
		
		setFrame(frejm);
		try {	
			//TE TRZY PIERD... LINIJKI KOSZTOWA�Y MNIE PRAWIE 2 GODZINY SZUKANIA PO NECIE
			//Po Eclipsem oczywi�cie getClass().getResource dzia�a jak marzenie
			//przy eksporcie do Jar, okazuje si� �e nic nie dzia�a a wyj�tek kt�ry
			//wyskakuje jest NIEMO�LIWE DO OBS�U�ENIA przez nawet catch (Exception e )
			//po prostu k... super, kochamy Jave
			//linijki, zast�pione dalej wywo�aniem metody statycznej:
			//ImageIcon x = Tools.getResIcon16("/icons/blackhole.png"); 
			//Image y = x.getImage();
			//frame.setIconImage(y);
			
			frame.setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
	
		}
		
		frame.getContentPane().add(this);
		frame.addComponentListener(this);
		getFrame().getContentPane().add(this);
		getFrame().addComponentListener(this);
		
		createHiddenConsole(); //tworzy ukryte okno konsoli logowania zdarze�
		createClusterWindow(); //niewidoczne na starcie okno tabeli klastr�w
		createNetPropertiesWindow(); //niewidoczne na starcie okno w�a�ciwo�ci sieci
		createSearchWindow();
		
		initializeEnvironment(); //wczytuje ustawienia, ustawia wewn�trzne zmienne programu
		
		// Set the frame properties and show it.
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();

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
		setPropertiesBox(new AbyssDockWindow(DockWindowType.EDITOR));
		setSimulatorBox(new AbyssDockWindow(DockWindowType.SIMULATOR));
		setSelectionBox(new AbyssDockWindow(DockWindowType.SELECTOR));
		setInvariantsBox(new AbyssDockWindow(DockWindowType.InvANALYZER));
		setClusterSelectionBox(new AbyssDockWindow(DockWindowType.ClusterSELECTOR));
		
			//aktywuj obiekt podokna wy�wietlania zbior�w MCT
		setMctBox(new AbyssDockWindow(DockWindowType.MctANALYZER)); 
		setInvSim(new AbyssDockWindow(DockWindowType.InvSIMULATOR));
		
		// create menu
		setMenu(new DarkMenu());
		getFrame().setJMenuBar(getMenu());

		// create workspace
		setWorkspace(new Workspace(this)); // default workspace dock
		getDockingListener().setWorkspace(workspace);

		//leftTabDock.setHeaderPosition(Position.BOTTOM);
		leftTabDock.addChildDock(getToolBox(), new Position(0));
		
		leftTabDock.addChildDock(getSimulatorBox(), new Position(1));
		leftTabDock.setSelectedDock(getToolBox());
		topRightTabDock.addChildDock(getPropertiesBox(), new Position(0));
		topRightTabDock.addChildDock(getSelectionBox(), new Position(1));
		topRightTabDock.setSelectedDock(getPropertiesBox());
		//bottomRightTabDock.addChildDock(getSimulatorBox(), new Position(0));
		bottomRightTabDock.addChildDock(getInvariantsBox(), new Position(1));
		bottomRightTabDock.addChildDock(getMctBox(), new Position(2));
		bottomRightTabDock.addChildDock(getClusterSelectionBox(), new Position(3));
		bottomRightTabDock.addChildDock(getInvSimBox(), new Position(4));

		// create the split docks
		leftSplitDock = new SplitDock();
		leftSplitDock.addChildDock(leftTabDock, new Position(Position.LEFT));
		leftSplitDock.addChildDock(getWorkspace().getWorkspaceDock(),new Position(Position.CENTER));
		//leftSplitDock.setDividerLocation((int) screenSize.getWidth() / 10);
		leftSplitDock.setDividerLocation(180);

		rightSplitDock = new SplitDock();
		rightSplitDock.addChildDock(topRightTabDock, new Position(Position.TOP));
		rightSplitDock.addChildDock(bottomRightTabDock, new Position(Position.BOTTOM));
		rightSplitDock.setDividerLocation((int) (screenSize.getHeight() * 2 / 5));

		totalSplitDock = new SplitDock();
		totalSplitDock.addChildDock(leftSplitDock, new Position(Position.LEFT));
		totalSplitDock.addChildDock(rightSplitDock,new Position(Position.RIGHT));
		totalSplitDock.setDividerLocation((int) screenSize.getWidth() - (int) screenSize.getWidth() / 6);
		
		// // Add root dock
		getDockModel().addRootDock("totalSplitDock", totalSplitDock, getFrame());
		add(totalSplitDock, BorderLayout.CENTER);

		// save docking paths
		DockingManager.getDockingPathModel().add(
				DefaultDockingPath.createDockingPath(getToolBox().getDockable()));
		DockingManager.getDockingPathModel().add(
				DefaultDockingPath.createDockingPath(getPropertiesBox().getDockable()));

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
		smallScreenSize = new Dimension((int) (screenSize.getWidth() * 0.9),
				(int) (screenSize.getHeight() * 0.9));
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
		        if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the program?", "Really Closing?", 
		            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		        	{
		        		log("Exiting program","text",true);
		            	windowConsole.saveLogToFile(null);
		            	System.exit(0);
		        	}
		    	}
		});
	}
	/**
	 * Metoda pomocnicza konstruktora. Ustawia g��wne zmienne programu, wczytuje plik
	 * w�a�ciwo�ci, itd.
	 */
	private void initializeEnvironment() {
		// ustawienie �cie�ek dost�pu
		lastPath = null;
		abyssPath = System.getProperty("user.dir");
		tmpPath = abyssPath+"\\tmp\\";
		toolPath = abyssPath+"\\tools\\";
		logPath = abyssPath+"\\log\\";
		
		settingsManager = new SettingsManager();
		settingsManager.loadSettings();
		//settingsManager.restoreDefaultSetting();
		
		File checkFile = new File(tmpPath);
		if (!checkFile.exists()) checkFile.mkdirs();
		checkFile = new File(logPath);
		if (!checkFile.exists()) checkFile.mkdirs();
		
		// Katalog /tools i pliki INY:
		File checkFileINA0 = new File(toolPath);
		File checkFileINA1 = new File(toolPath+"//INAwin32.exe");
		File checkFileINA2 = new File(toolPath+"//COMMAND.ina");
		File checkFileINA3 = new File(toolPath+"//ina.bat");
		if (!checkFileINA0.exists() || !checkFileINA1.exists() 
				|| !checkFileINA2.exists() || !(checkFileINA2.length() == 80)
				|| !checkFileINA3.exists() || !(checkFileINA3.length() == 30) ) {
			
			log("Something wrong with the INA tools directory.", "warning",true);
			if(!checkFileINA0.exists()) {
				checkFileINA0.mkdirs();
				logNoEnter("Tools directory does not exist. ", "error",true);
				log("Fixed", "italic", false);
				
				JOptionPane.showMessageDialog(null, "Warning! Tools directory does not exists. INAwin32.exe required \n"
						+ "there in order to work properly!",
						"Tool directory empty.",
						JOptionPane.WARNING_MESSAGE);
			}
			
			if(!checkFileINA2.exists() || !(checkFileINA2.length() == 80)) { //COMMAND.ina
				try {
					PrintWriter pw = new PrintWriter(checkFileINA2.getPath());
					pw.print(settingsManager.getValue("ina_COMMAND1")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND2")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND3")+"\r");
					pw.print(settingsManager.getValue("ina_COMMAND4"));
					pw.close();
					logNoEnter("File COMMAND.ina does not exist or is corrupted. ", "error",true);
					log("Fixed", "italic", false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Unable to recreate COMMAND.ina. Invariants generator will work"
							+ "in manual mode only.","Error - COMMAND.ina", JOptionPane.ERROR_MESSAGE);
					log("Unable to recreate COMMAND.ina. Invariants generator will work in manual mode only.", "error", true);
				}
			} 
			
			if(!checkFileINA3.exists() || !(checkFileINA3.length() == 30)) { //ina.bat
				try {
					PrintWriter pw = new PrintWriter(checkFileINA3.getPath());
					pw.print(settingsManager.getValue("ina_bat"));
					pw.close();
					logNoEnter("File ina.bat does not exist or is corrupted. ", "error",true);
					log("Fixed", "italic", false);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Unable to recreate ina.bat. This is a critical error, possible write"
							+ " protection issues in program directory. All in all, invariants generation using INAwin32 will"
							+ " most likely fail.","Critical error - writing", JOptionPane.ERROR_MESSAGE);
					log("Critical error, unable to recreate ina.bat file. Invariants generator will not work.", "error", true);
				}
			} 
			
			if(!checkFileINA1.exists()) { //no INAwin32.exe
				String msg = "INAwin32.exe missing in\n "+checkFileINA0.getPath()+"directory.\n Please download manually from and put in the right directory.";
				JOptionPane.showMessageDialog(null, msg, "Error - no INAwin32.exe", JOptionPane.ERROR_MESSAGE);
				log("INAwin32.exe missing in "+checkFileINA0+"directory. Please download "
						+ "manually from www2.informatik.hu-berlin.de/~starke/ina.html and put in the right directory.", "error", true);
			}
		}
		
		//check status of Rscript.exe location:
		r_env_missing();
	}

	/**
	 * Metoda uruchamiana na starcie programu oraz wtedy, gdy chcemy uzyska� dost�p do lokalizacji
	 * pliku Rscript.exe.
	 */
	public void r_env_missing() {
		rReady = true;
		String Rpath = settingsManager.getValue("r_path");
		File rF = new File(Rpath);
		if(!rF.exists()) {
			rReady = false;
			log("Invalid path ("+Rpath+") to Rscript executable file.", "error", true);
			
			Object[] options = {"Manually locate Rscript.exe", "R not installed",};
			int n = JOptionPane.showOptionDialog(null,
					"Rscript.exe missing in path "+Rpath,
					"Missing executable", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (n == 0) {
				
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter(".exe - Rscript",  new String[] { "EXE" });
				String selectedFile = Tools.selectFileDialog("", filters, "Select Rscript.exe", 
						"Please select Rscript exe, usually located in R/Rx.x.x/bin directory.");
				if(selectedFile.equals("")) {
					log("Rscript executable file inaccessible. Some features will be disabled.", "error", true);
				} else {
					//File f = new File(choosenDir);
					//if(!f.exists()) return;
					if(!selectedFile.contains("x64")) { //je�li wskazano 64b
						
						//File test = new File(selectedFile);
						//String dest = test.getAbsolutePath();
						String dest = selectedFile.substring(0,selectedFile.lastIndexOf(File.separator));
						dest += "\\x64\\Rscript.exe";
						settingsManager.setValue("r_path64", dest);
					} else {
						settingsManager.setValue("r_path64", selectedFile);
					}
					
					settingsManager.setValue("r_path", selectedFile);
					settingsManager.saveSettings();
					rReady = true;
					log("Rscript.exe manually located in "+selectedFile+". Settings file updated.", "text", true);
				
				}
				/*
				JFileChooser fc = new JFileChooser();
				FileFilter exeFile = new ExtensionFileFilter(".exe - Rscript",  new String[] { "EXE" });
				fc.setFileFilter(exeFile);
				fc.addChoosableFileFilter(exeFile);
				fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if(file.getName().equals("Rscript.exe")) {
						settingsManager.setValue("r_path", file.getPath());
						settingsManager.saveSettings();
						rReady = true;
						log("Rscript.exe manually located at "+file.getPath()+". Settings file updated.", "text", true);
					} else {
						log("Rscript executable file inaccessible. Some features will be disabled.", "error", true);
					}
				}
				*/
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za ustalenie domy�lnych lokalizacji pask�w zmiany rozmiaru
	 * podokien programu (Dividers).
	 */
	private void resetSplitDocks() {
		if (getFrame().getExtendedState() == JFrame.MAXIMIZED_BOTH) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//leftSplitDock.setDividerLocation((int) screenSize.getWidth() / 10);
			leftSplitDock.setDividerLocation(180);
			totalSplitDock.setDividerLocation((int) (screenSize.getWidth() * 5.6 / 7));
		} else {
			smallScreenSize = getFrame().getSize();
			//leftSplitDock.setDividerLocation((int) smallScreenSize.getWidth() / 8);
			leftSplitDock.setDividerLocation(180);
			totalSplitDock.setDividerLocation((int) (smallScreenSize.getWidth() * 5.6 / 7));
		}
	}

	/**
	 * Metoda odpowiedzialna za dodawanie nowych ikonek w prawy g�rnym roku ka�dego podokna
	 * programu.
	 * @param dockable - okno do przystrojenia dodatkowymi okienkami
	 * @param deletable - true, je�li dodajemy ikon� usuwania (g��wne podokno arkuszy sieci)
	 * @return Dockable - nowe okno po dodaniu element�w
	 */
	public Dockable decorateDockableWithActions(Dockable dockable, boolean deletable) {
		Dockable wrapper = new StateActionDockable(dockable,
				new DefaultDockableStateActionFactory(), new int[0]);
		int[] states = { DockableState.NORMAL, DockableState.MINIMIZED,
				DockableState.MAXIMIZED, DockableState.EXTERNALIZED, DockableState.CLOSED };
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
	 * Metoda zwraca �cie�k� do ostatio u�ywanego katalagu.
	 * @return String - �cie�ka do katalogu
	 */
	public String getLastPath() {
		return lastPath;
	}
	
	/**
	 * Metoda ustawia now� �cie�k� do ostatio u�ywanego katalagu.
	 * @return String - �cie�ka do katalogu
	 */
	public void setLastPath(String path) {
		lastPath = path;
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	/**
	 * Diabli wiedz� co i kiedy wywo�uje t� metod�, tym niemniej zleca ona innej ustalenie
	 * domy�lnych lokalizacji pask�w zmiany rozmiar�w podokien (Dividers).
	 */
	public void componentResized(ComponentEvent arg0) {
		resetSplitDocks();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}
	
	/**
	 * Metoda zwraca podokno dedykowane do wy�wietlania zbior�w MCT.
	 * @return AbyssDockWindow - okno wyboru MCT
	 */
	public AbyssDockWindow getMctBox() {
		return mctBox;
	}

	/**
	 * Metoda ta ustawia nowe podokno dedykowane do wy�wietlania zbior�w MCT.
	 * @param mctBox AbyssDockWindow - nowe okno wyboru MCT
	 */
	public void setMctBox(AbyssDockWindow mctBox) {
		this.mctBox = mctBox;
	}
	
	/**
	 * Metoda ustawia nowe okno w�a�ciwo�ci symulatora inwariant�w.
	 * @param invSim AbyssDockWindow - okno w�a�ciwo�ci symulatora inwariant�w
	 */
	public void setInvSim(AbyssDockWindow invSim)
	{
		this.invSimBox = invSim;
	}
	
	/**
	 * Metoda zwraca aktywne okno w�a�ciwo�ci symulatora inwariant�w.
	 * @param invSim AbyssDockWindow - okno w�a�ciwo�ci symulatora inwariant�w
	 */
	public AbyssDockWindow getInvSimBox() {
		return invSimBox;
	}

	/**
	 * Metoda odpowiedzialna za zwr�cenie obiektu obszaru roboczego.
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
	 * Metoda zwraca obiekt paska narz�dziowego.
	 * @return PetriNetTools - pasek przycisk�w
	 */
	public PetriNetTools getToolBox() {
		return toolBox;
	}

	/**
	 * Metoda ustawia nowy obiekt paska narz�dziowego.
	 * @param toolBox PetriNetTools - pasek przycisk�w
	 */
	private void setToolBox(PetriNetTools toolBox) {
		this.toolBox = toolBox;
	}

	/**
	 * Metoda zwraca obiekt podokna do wy�wietlania w�a�ciwo�ci klikni�tego elementu sieci.
	 * @return AbyssDockWindow - podokno w�a�ciwo�ci
	 */
	public AbyssDockWindow getPropertiesBox() {
		return selElementBox;
	}

	/**
	 * Metoda ustawia nowy obiekt podokna do wy�wietlania w�a�ciwo�ci klikni�tego elementu sieci.
	 * @param propertiesBox AbyssDockWindow - podokno w�a�ciwo�ci
	 */
	private void setPropertiesBox(AbyssDockWindow propertiesBox) {
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
	 * Metoda zwraca obiekt - referencj� swojej klasy.
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
	 * Zwraca obiekt przycisku powi�kszaj�cego okno do rozmiar�w ekranu.
	 * @return SingleMaximizer
	 */
	public SingleMaximizer getMaximizer() {
		return maximizer;
	}

	/**
	 * Ustawia obiekt przycisku powi�kszaj�cego okno do rozmiar�w ekranu.
	 * @param maximizer SingleMaximizer
	 */
	private void setMaximizer(SingleMaximizer maximizer) {
		this.maximizer = maximizer;
	}

	/**
	 * Zwraca obiekt przycisku pomniejszaj�cego okno do paska zada�.
	 * @return LineMinimizer
	 */
	public LineMinimizer getMinimizer() {
		return minimizer;
	}

	/**
	 * Ustawia obiekt przycisku pomniejszaj�cego okno do paska zada�.
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
	public AbyssDockWindow getSimulatorBox() {
		return simulatorBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna symulatora sieci.
	 * @param simulatorBox Properties - okno symulatora sieci
	 */
	private void setSimulatorBox(AbyssDockWindow simulatorBox) {
		this.simulatorBox = simulatorBox;
	}

	/**
	 * Metoda ta zwraca obiekt okna wyboru element�w.
	 * @return AbyssDockWindow - okno wyboru element�w
	 */
	public AbyssDockWindow getSelectionBox() {
		return selectionBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna wyboru element�w.
	 * @param selectionBox AbyssDockWindow - okno wyboru element�w
	 */
	private void setSelectionBox(AbyssDockWindow selectionBox) {
		this.selectionBox = selectionBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna przycisk�w programu.
	 * @return Toolbar - obiekt okna przycisk�w
	 */
	public Toolbar getShortcutsBar() {
		return shortcutsBar;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna przycisk�w programu.
	 * @param shortcutsBar Toolbar - obiekt okna przycisk�w
	 */
	private void setShortcutsBar(Toolbar shortcutsBar) {
		this.shortcutsBar = shortcutsBar;
	}
	
	/**
	 * Opis: I have no idea...
	 * @param dockable Dockable
	 * @param listener DockingListener
	 * @return Dockable
	 */
	public static Dockable externalWithListener(Dockable dockable, DockingListener listener) {
		Dockable wrapper = guiManager.decorateDockableWithActions(dockable, false);
		wrapper.addDockingListener(listener);
		return wrapper;
	}

	/**
	 * Metoda zwracaj�ca obiekt nas�uchuj�cy zdarzenia dokowania podokna.
	 * @return DarkDockingListener - obiekt nas�uchuj�cy
	 */
	public DarkDockingListener getDockingListener() {
		return dockingListener;
	}

	/**
	 * Metoda ustawiaj�ca nowy obiekt nas�uchuj�cy zdarzenia dokowania podokna.
	 * @param dockingListener DarkDockingListener - nowy obiekt nas�uchuj�cy
	 */
	public void setDockingListener(DarkDockingListener dockingListener) {
		this.dockingListener = dockingListener;
	}

	/**
	 * Metoda zwraca obiekt podokna dla pod�wietlania inwariant�w sieci.
	 * @return AbyssDockWindow - podokno inwariant�w
	 */
	public AbyssDockWindow getInvariantsBox() {
		return invariantsBox;
	}

	/**
	 * Metoda ta ustawia obiekt podokna dla pod�wietlania inwariant�w sieci.
	 * @param analyzerBox AbyssDockWindow - podokno inwariant�w
	 */
	public void setInvariantsBox(AbyssDockWindow analyzerBox) {
		this.invariantsBox = analyzerBox;
	}
	
	/**
	 * Metoda zwraca obiekt podokna wyboru klastr�w do pod�wietlania.
	 * @return AbyssDockWindow - obiekt z w�a�ciwo�ciani sieci
	 */
	public AbyssDockWindow getClusterSelectionBox() {
		return clustersBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt podokna wyboru klastr�w do pod�wietlania.
	 * @param clusterBox AbyssDockWindow - obiekt z w�a�ciwo�ciami sieci
	 */
	public void setClusterSelectionBox(AbyssDockWindow clusterBox) {
		this.clustersBox = clusterBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna konsoli programu.
	 * @return AbyssConsole - obiekt konsoli
	 */
	public AbyssConsole getConsoleWindow() {
		return windowConsole;
	}
	
	/**
	 * Metoda zwraca �cie�k� do katalogu narzedzi.
	 * @return String
	 */
	public String getToolPath() {
		return toolPath;
	}
	
	/**
	 * Metoda zwraca �cie�k� do katalogu programu.
	 * @return String
	 */
	public String getAbyssPath() {
		return abyssPath;
	}
	
	/**
	 * Metoda zwraca �cie�k� do katalogu plik�w tymczasowych.
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
	 * Metoda odpowiedzialna za przywr�cenie widoku domy�lnego.
	 */
	public void restoreDefaultVisuals() {
		getSimulatorBox().createSimulatorProperties();
		
		//getInvSimBox().createInvSimulatorProperties();
		// // repaint all sheets in workspace
		// for (WorkspaceSheet sheet : workspace.getSheets()) {
		// sheet.getGraphPanel().repaint();
		// }
		// // redock all sheets
		// workspace.redockSheets();
		// // recreate dock structure
		// getDockModel().RootDock(totalSplitDock);
		//
		// leftTabDock.emptyChild(getToolBox());
		// topRightTabDock.emptyChild(getPropertiesBox());
		// setToolBox(new Tools());
		// setPropertiesBox(new Properties());
		//
		// leftTabDock = new CompositeTabDock(); // default Toolbox dock
		// topRightTabDock = new CompositeTabDock(); // default propertiesdock
		// leftTabDock.addChildDock(getToolBox(), new Position(0));
		// topRightTabDock.addChildDock(getPropertiesBox(), new Position(0));
		//
		// // create the split docks
		// leftSplitDock = new SplitDock();
		// leftSplitDock.addChildDock(leftTabDock, new Position(Position.LEFT));
		// leftSplitDock.addChildDock(getWorkspace().getWorkspaceDock(),
		// new Position(Position.CENTER));
		// leftSplitDock.setDividerLocation((int) screenSize.getWidth() / 8);
		//
		// rightSplitDock = new SplitDock();
		// rightSplitDock.addChildDock(topRightTabDock, new Position(
		// Position.RIGHT));
		//
		// totalSplitDock = new SplitDock();
		// totalSplitDock.addChildDock(leftSplitDock, new
		// Position(Position.LEFT));
		// totalSplitDock.addChildDock(rightSplitDock,
		// new Position(Position.RIGHT));
		// totalSplitDock.setDividerLocation((int) screenSize.getWidth()
		// - (int) screenSize.getWidth() / 6);
		//
		// // // Add root dock
		// getDockModel().addRootDock("totalSplitDock", totalSplitDock, frame);
		// add(totalSplitDock, BorderLayout.CENTER);
		//
		// // save docking paths
		// DockingManager.getDockingPathModel().add(
		// DefaultDockingPath
		// .createDockingPath(getToolBox().getDockable()));
		// DockingManager.getDockingPathModel().add(
		// DefaultDockingPath.createDockingPath(getPropertiesBox()
		// .getDockable()));
		// this.repaint();
	}
	
	/**
	 * Metoda ta mia�a na celu rozpocz�cia analizy sieci Petriego w ramach
	 * pracy in�ynierskiej. Plan by� dobry, aczkolwiek w ramach pracy magisterskiej
	 * zacz�a w ko�cu dzia�a� i uruchamia analizator inwariant�w. Nie dzia�aj�cy
	 * tak jak nale�y, ale w sumie pewien post�p jest... (MR 8.12.2014)
	 */
	public void tInvariantsAnalyse(){
		PetriNet project = workspace.getProject();
		project.tInvariantsAnalyze();
		//System.out.println("*%^$(*$)$ Barona!!!!!!!!!!!");	
		//powy�sza metoda przekazuje na konsol� wyrazy uznania od jednego ze student�w
		//dla kolegi, kt�ry implementowa� inne cz�ci programu (MR)
	}

	/**
	 * Metoda odpowiedzialna za rozpocz�cie generowania zbior�w MCT.
	 */
	public void generateMCT() {
		DarkAnalyzer analyzer = getWorkspace().getProject().getAnalyzer();
		
		ArrayList<ArrayList<InvariantTransition>> invTr = analyzer.gettInvariants();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT(invTr);
		
		getMctBox().showMCT(mct);
	}
	
	/**
	 * Metoda zleca wy�wietlenie podokna pod�wietlania klastrowania
	 */
	public void showClusterSelectionBox(ClusterDataPackage data){
		getClusterSelectionBox().showClusterSelector(data);
	}
	
	/**
	 * Metoda rozpoczyna symulacj� uruchamiania inwariant�w.
	 * @param type int - 0-basic, 1- time
	 * @param value - warto��
	 * @throws CloneNotSupportedException
	 */
	public void startInvariantsSimulation(int type, int value) throws CloneNotSupportedException{
		this.getWorkspace().getProject().startInvSim(type, value);
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy nowe okno tabeli klastr�w.
	 */
	private void createClusterWindow() {
		windowClusters = new AbyssClusters(0);
		//windowClusters.setLocationRelativeTo(this);
	}
	
	/**
	 * Metoda s�u�y do wy�wietlenia okna klastr�w.
	 */
	public void showClusterWindow() {
		if(windowClusters != null) {
			windowClusters.setVisible(true);
		}
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy okno w�a�ciwo�ci sieci.
	 */
	private void createNetPropertiesWindow() {
		windowNetProperties = new AbyssProperties();
		windowNetProperties.setLocationRelativeTo(frame);
	}
	
	/**
	 * Metoda s�u�y do wy�wietlenia okna w�a�ciwo�ci sieci.
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
		windowAbout = new AbyssAbout(frame);
	}
	
	/**
	 * Metoda s�u�y do wy�wietlenia okna informacji o programie.
	 */
	public void showAboutWindow() {
		if(windowAbout != null) {
			windowAbout.setVisible(true);
		}
	}
	
	/**
	 * Metoda ta tworzy nowe okno szukania element�w sieci.
	 */
	private void createSearchWindow() {
		windowSearch = new AbyssSearch();
	}
	
	/**
	 * Metoda wy�wietla okno szukania element�w sieci na ekranie.
	 */
	public void showSearchWindow() {
		if(windowSearch != null) {
			windowSearch.setVisible(true);
		}
	}
	
	/**
	 * Metoda pomocnicza konstruktora, s�u�y do tworzenia ukrytego okna konsoli log�w.
	 */
	private void createHiddenConsole() {
		windowConsole = new AbyssConsole();
		windowConsole.setLocationRelativeTo(this);
	}
	
	/**
	 * Metoda s�u�y do pokazywania lub chowania okna konsoli.
	 */
	public void showConsole(boolean value) {
		if(windowConsole != null) {
			windowConsole.setVisible(value);
		}
	}
	
	/**
	 * Metoda zapisuj�ca nowe zdarzenie w oknie log�w.
	 * @param text String - tekst zdarzenia
	 * @param mode String - tryb zapisu w oknie
	 * @param time boolean - true, je�li ma by� podany czas zdarzenia
	 */
	public void log(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, true);
		if(mode.equals("error"))
			windowConsole.setVisible(true);
	}
	/**
	 * Jak wy�ej, tylko bez entera.
	 */
	public void logNoEnter(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, false);
		if(mode.equals("error"))
			windowConsole.setVisible(true);
	}
	
	/**
	 * Metoda zwraca status �rodowiska narz�dziowego R.
	 * @return boolean - true, je�li Abyss dysponuje prawid�ow� �cie�k� dost�pu do Rscript.exe
	 */
	public boolean getRStatus() {
		return rReady;
	}
	
	/**
	 * Metoda zwraca obiekt kontroluj�cy ustawienia programu.
	 * @return SettingsManager
	 */
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}
}


//Rprotocols rp = new Rprotocols();
//rp.setR1(settingsManager.getValue("r_path"), dir_path, "cluster.csv", "scripts\\f_clusters.r", "scripts\\f_clusters_run.r", c_number);
//rp.setType(1);

//rp.RClusteringAll(settingsManager.getValue("r_path"), dir_path, "cluster.csv", "scripts\\f_clusters.r", "scripts\\f_clusters_run.r", c_number);
//rp.RClusteringAll(settingsManager.getValue("r_path"), dir_path, "cluster.csv", "scripts\\f_clusers_Pearson.r", "scripts\\f_clusers_Pearson_run.r", c_number);

//rp.RClusteringAll(settingsManager.getValue("r_path"), "tmp/", "cluster.csv", 
//		"scripts\\f_clusters.r", "scripts\\f_clusters_run.r", c_number);
//rp.RClusteringAll(settingsManager.getValue("r_path"), "tmp/", "cluster.csv", 
//		"scripts\\f_clusers_Pearson.r", "scripts\\f_clusers_Pearson_run.r", c_number);


//Konkretne klastrowanie, wersja FUNKCJA 1: Biblioteki: cluster; generuje listy inwariantow
//runner.RClusteringSingle("c:\\Program Files\\R\\R-3.1.2\\bin\\Rscript.exe", "tmp/", "cluster.csv", "tools\\Function2.r", "binary", "average", 20);
//Konkretne klastrowanie, wersja FUNKCJA 4: Biblioteki: amap, cluster; funkcja umozliwiajaca analize, uzywajaca miary Pearsona; generuje liste inwariantow
//runner.RClusteringSingle("c:\\Program Files\\R\\R-3.1.2\\bin\\Rscript.exe", "tmp/", "cluster.csv", "tools\\Function3.r", "pearson", "average", 20);

	
	
