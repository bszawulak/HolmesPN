package abyss.darkgui;

import abyss.adam.mct.Runner;
import abyss.analyzer.DarkAnalyzer;
import abyss.analyzer.NetPropAnalyzer;
import abyss.darkgui.dockable.DeleteAction;
import abyss.darkgui.properties.Properties;
import abyss.darkgui.properties.PetriNetTools;
import abyss.darkgui.properties.Properties.PropertiesType;
import abyss.darkgui.toolbar.Toolbar;
import abyss.files.io.Rprotocols;
import abyss.math.PetriNet;
import abyss.settings.SettingsManager;
import abyss.utilities.Tools;
import abyss.windows.AbyssConsole;
import abyss.windows.AbyssClusters;
import abyss.workspace.ExtensionFileFilter;
import abyss.workspace.Workspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
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
 * G³ówna klasa programu odpowiedzialna za w³aœciwie wszystko. Zaczyna od utworzenia elementów
 * graficznych programu, a dalej jakoœ tak siê samo ju¿ toczy wszystko...
 * @author students
 *
 */
public class GUIManager extends JPanel implements ComponentListener {
	private static final long serialVersionUID = -817072868916096442L;
	// Static fields.
	private static GUIManager guiManager;
	private Dimension screenSize; // praca w maksymalizacji
	private Dimension smallScreenSize; // praca poza maksymalizowanym oknem
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
	private Properties propertiesBox, simulatorBox, selectionBox, analyzerBox, propAnalyzerBox, mctBox, invSimBox;
	// docking listener
	private DarkDockingListener dockingListener;
	private Toolbar shortcutsBar;

	// main frame
	private JFrame frame;
	// other components
	private DarkMenu menu;

	// inne wa¿ne zmienne
	private String lastPath;	// ostatnia otwarta œcie¿ka
	private String abyssPath; 	// scie¿ka dostêpu do katalogu g³ównego programu
	private String tmpPath;		// œcie¿ka dostêpu do katalogu plików tymczasowych
	private String toolPath;	// œcie¿ka dostêpu do katalogu narzedziowego
	private String logPath;
	
	// okna niezale¿ne (o tyle o ile):
	private AbyssClusters windowClusters; //okno tabeli 
	private AbyssConsole windowConsole; //konsola logów
	private boolean rReady = false; // true, jeœli program dostêp do pliku Rscript.exe
	/**
	 * Konstruktor obiektu klasy GUIManager.
	 * @param frejm JFrame - g³ówna ramka kontener programu
	 */
	public GUIManager(JFrame frejm) {
		super(new BorderLayout());
		guiManager = this;
	
		frame = frejm;
		frame.getContentPane().add(this);
		frame.addComponentListener(this);
		setFrame(frejm);
		getFrame().getContentPane().add(this);
		getFrame().addComponentListener(this);
		
		
		createHiddenConsole();//tworzy ukryte okno konsoli logowania zdarzeñ
		createClusterWindow();
		initializeEnvironment(); //wczytuje ustawienia, ustawia wewnêtrzne zmienne programu

		
		
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
		setPropertiesBox(new Properties(PropertiesType.EDITOR));
		setSimulatorBox(new Properties(PropertiesType.SIMULATOR));
		setSelectionBox(new Properties(PropertiesType.SELECTOR));
		setAnalyzerBox(new Properties(PropertiesType.InvANALYZER));
		setPropAnalyzerBox(new Properties(PropertiesType.PropANALYZER));
		setMctBox(new Properties(PropertiesType.MctANALYZER));
		setInvSim(new Properties(PropertiesType.InvSIMULATOR));
		
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
		bottomRightTabDock.addChildDock(getAnalyzerBox(), new Position(1));
		bottomRightTabDock.addChildDock(getMctBox(), new Position(2));
		bottomRightTabDock.addChildDock(getPropAnalyzerBox(), new Position(3));
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
		            	windowConsole.saveLogToFile(null);
		            	System.exit(0);
		        	}
		    	}
		});
	}

	/**
	 * Metoda pomocnicza konstruktora. Ustawia g³ówne zmienne programu, wczytuje plik
	 * w³aœciwoœci, itd.
	 */
	private void initializeEnvironment() {
		// ustawienie œcie¿ek dostêpu
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
					JOptionPane.showMessageDialog(null, "Unable to recreate ina.bat. This is critical error, possible write"
							+ "protection issues in program directory. All in all, invariants generation using INAwin32 will"
							+ "most likely fail.","Error - writing", JOptionPane.ERROR_MESSAGE);
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
	 * Metoda uruchamiana na starcie programu oraz wtedy, gdy chcemy uzyskaæ dostêp do lokalizacji
	 * pliku Rscript.exe.
	 */
	private void r_env_missing() {
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
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za ustalenie domyœlnych lokalizacji pasków zmiany rozmiaru
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
			totalSplitDock
					.setDividerLocation((int) (smallScreenSize.getWidth() * 5.6 / 7));
		}
	}

	/**
	 * Metoda odpowiedzialna za dodawanie nowych ikonek w prawy górnym roku ka¿dego podokna
	 * programu.
	 * @param dockable - okno do przystrojenia dodatkowymi okienkami
	 * @param deletable - true, jeœli dodajemy ikonê usuwania (g³ówne podokno arkuszy sieci)
	 * @return Dockable - nowe okno po dodaniu elementów
	 */
	public Dockable decorateDockableWithActions(Dockable dockable, boolean deletable) {
		Dockable wrapper = new StateActionDockable(dockable,
				new DefaultDockableStateActionFactory(), new int[0]);
		int[] states = { DockableState.NORMAL, DockableState.MINIMIZED,
				DockableState.MAXIMIZED, DockableState.EXTERNALIZED, DockableState.CLOSED };
		wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), states);

		if (deletable) {
			DeleteAction deleteAction = new DeleteAction(this, "Delete",
					new ImageIcon("resources/icons/page_white_delete.png"));
			Action[][] actions = new Action[1][];
			actions[0] = new Action[1];
			actions[0][0] = deleteAction;
			wrapper = new ActionDockable(wrapper, actions);
			deleteAction.setDockable(wrapper);
		}

		return wrapper;
	}
	
	/**
	 * Metoda zwraca œcie¿kê do ostatio u¿ywanego katalagu.
	 * @return String - œcie¿ka do katalogu
	 */
	public String getLastPath() {
		return lastPath;
	}
	
	/**
	 * Metoda ustawia now¹ œcie¿kê do ostatio u¿ywanego katalagu.
	 * @return String - œcie¿ka do katalogu
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
	 * Diabli wiedz¹ co i kiedy wywo³uje tê metodê, tym niemniej zleca ona innej ustalenie
	 * domyœlnych lokalizacji pasków zmiany rozmiarów podokien (Dividers).
	 */
	public void componentResized(ComponentEvent arg0) {
		resetSplitDocks();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}
	
	/**
	 * Metoda zwraca okno w³aœciwoœci dla zbiorów MCT.
	 * @return Properties - okno w³aœciwoœci MCT
	 */
	public Properties getMctBox() {
		return mctBox;
	}

	/**
	 * Metoda ta ustawia nowe okno w³aœciwoœci dla zbiorów MCT.
	 * @param mctBox Properties - nowe okno w³aœciwoœci MCT
	 */
	public void setMctBox(Properties mctBox) {
		this.mctBox = mctBox;
	}
	
	/**
	 * Metoda ustawia nowe okno w³aœciwoœci symulatora inwariantów.
	 * @param invSim Properties - okno w³aœciwoœci symulatora inwariantów
	 */
	public void setInvSim(Properties invSim)
	{
		this.invSimBox = invSim;
	}
	
	/**
	 * Metoda zwraca aktywne okno w³aœciwoœci symulatora inwariantów.
	 * @param invSim Properties - okno w³aœciwoœci symulatora inwariantów
	 */
	public Properties getInvSimBox() {
		return invSimBox;
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
	 * Metoda zwraca obiekt paska narzêdziowego.
	 * @return Tools - pasek przycisków
	 */
	public PetriNetTools getToolBox() {
		return toolBox;
	}

	/**
	 * Metoda ustawia nowy obiekt paska narzêdziowego.
	 * @param toolBox Tools - pasek przycisków
	 */
	private void setToolBox(PetriNetTools toolBox) {
		this.toolBox = toolBox;
	}

	/**
	 * Metoda zwraca obiekt podokna w³aœciwoœci.
	 * @return Properties - podokno w³aœciwoœci
	 */
	public Properties getPropertiesBox() {
		return propertiesBox;
	}

	/**
	 * Metoda ustawia nowy obiekt podokna w³aœciwoœci.
	 * @param propertiesBox Properties - podokno w³aœciwoœci
	 */
	private void setPropertiesBox(Properties propertiesBox) {
		this.propertiesBox = propertiesBox;
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

	public FloatDockModel getDockModel() {
		return dockModel;
	}

	private void setDockModel(FloatDockModel dockModel) {
		this.dockModel = dockModel;
	}

	/**
	 * Metoda zwraca obiekt - referencjê swojej klasy.
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

	public SingleMaximizer getMaximizer() {
		return maximizer;
	}

	private void setMaximizer(SingleMaximizer maximizer) {
		this.maximizer = maximizer;
	}

	public LineMinimizer getMinimizer() {
		return minimizer;
	}

	private void setMinimizer(LineMinimizer minimizer) {
		this.minimizer = minimizer;
	}

	public FloatExternalizer getExternalizer() {
		return externalizer;
	}

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
	public Properties getSimulatorBox() {
		return simulatorBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna symulatora sieci.
	 * @param simulatorBox Properties - okno symulatora sieci
	 */
	private void setSimulatorBox(Properties simulatorBox) {
		this.simulatorBox = simulatorBox;
	}

	/**
	 * Metoda ta zwraca obiekt okna wyboru elementów.
	 * @return Properties - okno wyboru elementów
	 */
	public Properties getSelectionBox() {
		return selectionBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna wyboru elementów.
	 * @param selectionBox Properties - okno wyboru elementów
	 */
	private void setSelectionBox(Properties selectionBox) {
		this.selectionBox = selectionBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna przycisków programu.
	 * @return Toolbar - obiekt okna przycisków
	 */
	public Toolbar getShortcutsBar() {
		return shortcutsBar;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna przycisków programu.
	 * @param shortcutsBar Toolbar - obiekt okna przycisków
	 */
	private void setShortcutsBar(Toolbar shortcutsBar) {
		this.shortcutsBar = shortcutsBar;
	}
	
	public static Dockable externalWithListener(Dockable dockable, DockingListener listener) {
		Dockable wrapper = guiManager.decorateDockableWithActions(dockable, false);
		wrapper.addDockingListener(listener);
		return wrapper;
	}

	/**
	 * Metoda zwracaj¹ca obiekt nas³uchuj¹cy zdarzenia dokowania podokna.
	 * @return DarkDockingListener - obiekt nas³uchuj¹cy
	 */
	public DarkDockingListener getDockingListener() {
		return dockingListener;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt nas³uchuj¹cy zdarzenia dokowania podokna.
	 * @param dockingListener DarkDockingListener - nowy obiekt nas³uchuj¹cy
	 */
	public void setDockingListener(DarkDockingListener dockingListener) {
		this.dockingListener = dockingListener;
	}

	/**
	 * Metoda ta zwraca obiekt okna ustawieñ.
	 * @return SettingsManager - obiekt okna ustawieñ
	 */
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ta ustawia nowy obiekt okna ustawieñ.
	 * @param settingsManager SettingsManager - obiekt okna ustawieñ
	 */
	private void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}

	/**
	 * Metoda zwraca obiekt w³aœciwoœci analizatora sieci.
	 * @return Properties - obiekt w³aœciwoœci analizatora
	 */
	public Properties getAnalyzerBox() {
		return analyzerBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt w³aœciwoœci analizatora sieci.
	 * @param analyzerBox Properties - obiekt w³aœciwoœci analizatora
	 */
	public void setAnalyzerBox(Properties analyzerBox) {
		this.analyzerBox = analyzerBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna z w³aœciwoœciami sieci.
	 * @return Properties - obiekt z w³aœciwoœciani sieci
	 */
	public Properties getPropAnalyzerBox() {
		return propAnalyzerBox;
	}

	/**
	 * Metoda ta ustawia nowy obiekt okna z w³aœciwoœciami sieci.
	 * @param analyzerBox Properties - obiekt z w³aœciwoœciami sieci
	 */
	public void setPropAnalyzerBox(Properties analyzerBox) {
		this.propAnalyzerBox = analyzerBox;
	}
	
	/**
	 * Metoda zwraca obiekt okna konsoli programu.
	 * @return AbyssConsole - obiekt konsoli
	 */
	public AbyssConsole getConsoleWindow() {
		return windowConsole;
	}
	
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************
// ************************************************************************************************

	/**
	 * Metoda odpowiedzialna za import projektu z plików programów zewnêtrznych.
	 * Obs³uguje miêdzy innymi sieci zwyk³e i czasowe programu Snoopy oraz sieci
	 * w formacie programu INA.
	 */
	public void importProject() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter snoopyFilter = new ExtensionFileFilter(
			".spped - Snoopy PN Files", new String[] { "SPPED" });
		FileFilter snoopyFilterTime = new ExtensionFileFilter(
				".sptpt - Snoopy TPN Files", new String[] { "SPTPT" });
		FileFilter inaFilter = new ExtensionFileFilter(".pnt - INA Files",
				new String[] { "PNT" });
		fc.setFileFilter(snoopyFilter);
		fc.addChoosableFileFilter(snoopyFilter);
		fc.addChoosableFileFilter(snoopyFilterTime);
		fc.addChoosableFileFilter(inaFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			workspace.getProject().loadFromFile(file.getPath());
			setLastPath(file.getParentFile().getPath());
		}
		getSimulatorBox().createSimulatorProperties();
	}

	/**
	 * Metoda odpowiedzialna za otwieranie pliku z zapisan¹ sieci¹ w formacie .abyss.
	 */
	public void openProject() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter fileFilter = new ExtensionFileFilter(
				".abyss - Abyss Petri Net Files", new String[] { "ABYSS" });
		fc.setFileFilter(fileFilter);
		fc.addChoosableFileFilter(fileFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			workspace.getProject().loadFromFile(file.getPath());
			lastPath = file.getParentFile().getPath();
		}
		getSimulatorBox().createSimulatorProperties();
	}

	/**
	 * Metoda odpowiedzialna za przywrócenie widoku domyœlnego.
	 */
	public void restoreDefaultVisuals() {
		getSimulatorBox().createSimulatorProperties();
		getInvSimBox().createInvSimulatorProperties();
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
	 * Metoda odpowiedzialna za eksport sieci do pliku w formacie programu INA.
	 */
	public void exportProject() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);	

		// FileFilter snoopyFilter = new ExtensionFileFilter(
		// ".spped - Snoopy Files", new String[] { "SPPED" });
		FileFilter inaFilter = new ExtensionFileFilter(".pnt - INA Files",
				new String[] { "PNT" });
		String fileExtension = ".pnt";
		fc.setFileFilter(inaFilter);
		// fc.addChoosableFileFilter(snoopyFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			
			if(file.getPath().contains(".pnt"))
				fileExtension = "";
					
			workspace.getProject().saveToFile(file.getPath() + fileExtension);
			setLastPath(file.getParentFile().getPath());
		}
	}
	
	/**
	 * Metoda odpowiedzialna za zapis wygenerowanych inwariantów do pliku programu INA.
	 */
	public void exportGeneratedInvariants() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter inaFilter = new ExtensionFileFilter(".inv - INA Invariants File", new String[] { "INV" });
		FileFilter charlieFilter = new ExtensionFileFilter(".inv - Charlie Invariants File", new String[] { "INV" });
		FileFilter csvFilter = new ExtensionFileFilter(".csv - Comma Separated Values", new String[] { "CSV" });
		String fileExtension;
		fc.setFileFilter(inaFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.addChoosableFileFilter(charlieFilter);
		fc.addChoosableFileFilter(csvFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String description = fc.getFileFilter().getDescription();
			if (description.contains("INA")) {
				if(file.getPath().contains(".inv"))
					fileExtension = "";
				else
					fileExtension = ".inv";
				workspace.getProject().saveInvariantsToInaFormat(file.getPath() + fileExtension);
				setLastPath(file.getParentFile().getPath());
			} else if (description.contains("Comma")) {
				if(file.getPath().contains(".csv"))
					fileExtension = "";
				else
					fileExtension = ".csv";
				workspace.getProject().saveInvariantsToCSV(file.getPath() + fileExtension, false);
				setLastPath(file.getParentFile().getPath());
			} else if (description.contains("Charlie")) {
				if(file.getPath().contains(".inv"))
					fileExtension = "";
				else
					fileExtension = ".inv";
				workspace.getProject().saveInvariantsToCharlie(file.getPath() + fileExtension);
				setLastPath(file.getParentFile().getPath());
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za eksport projektu do pliku graficznego w okreœlonym formacie.
	 */
	public void exportProjectToImage() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter pngFilter = new ExtensionFileFilter(".png - Portable Network Graphics", new String[] { "png" });
		FileFilter bmpFilter = new ExtensionFileFilter(".bmp -  Bitmap Image File", new String[] { "bmp" });
		FileFilter jpegFilter = new ExtensionFileFilter(".jpeg - JPEG Image File", new String[] { "jpeg" });
		FileFilter jpgFilter = new ExtensionFileFilter(".jpg - JPEG Image File", new String[] { "jpg" });
		fc.setFileFilter(pngFilter);
		fc.addChoosableFileFilter(pngFilter);
		fc.addChoosableFileFilter(bmpFilter);
		fc.addChoosableFileFilter(jpegFilter);
		fc.addChoosableFileFilter(jpgFilter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String ext = "";
			String extension = fc.getFileFilter().getDescription();
			if (extension.contains(".png")) {
				ext = ".png";
			}
			if (extension.contains(".bmp")) {
				ext = ".bmp";
			}
			if (extension.contains(".jpeg") || extension.contains(".jpg")) {
				ext = ".jpeg";
			}
			int index = 0;
			for (BufferedImage bi : workspace.getProject().getImagesFromGraphPanels()) {
				try {
					String ext2 = "";
					String path = file.getPath();
					if(ext.equals(".png") && !(path.contains(".png"))) ext2 = ".png";
					if(ext.equals(".bmp") && !file.getPath().contains(".bmp")) ext2 = ".bmp";
					if(ext.equals(".jpeg") && !file.getPath().contains(".jpeg")) ext2 = ".jpeg";
					if(ext.equals(".jpeg") && !file.getPath().contains(".jpg")) ext2 = ".jpg";
					String fileName = file.getName();
					int pos = fileName.lastIndexOf(".");
					if (pos > 0) {
						fileName = fileName.substring(0, pos);
					}
					fileName += "_" + Integer.toString(index) + ext2;
					ImageIO.write(bi, ext.substring(1), new File(file.getPath()
							+ "_" + Integer.toString(index) + ext2));
					index++;
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			setLastPath(file.getParentFile().getPath());
		}
	}

	/**
	 * Metoda odpowiedzialna za zapis projektu sieci do pliku natywnego aplikacji.
	 */
	public void saveProject() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		// FileFilter snoopyFilter = new ExtensionFileFilter(
		// ".spped - Snoopy Files", new String[] { "SPPED" });
		FileFilter fileFilter = new ExtensionFileFilter(
				".abyss - Abyss Petri Net files", new String[] { "ABYSS" });
		String fileExtension = ".abyss";
		fc.setFileFilter(fileFilter);
		// fc.addChoosableFileFilter(snoopyFilter);
		fc.addChoosableFileFilter(fileFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			workspace.getProject().saveToFile(file.getPath() + fileExtension);
			setLastPath(file.getParentFile().getPath());
		}
	}
	
	/**
	 * Metoda odpowiedzialna za wczytywanie inwariantów z pliku wygenerowanego programem INA.
	 */
	public void loadExternalAnalysis() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);	

		FileFilter inaFilter = new ExtensionFileFilter(".inv - INA Invariants Files", new String[] { "INV" });
		fc.setFileFilter(inaFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			PetriNet project = workspace.getProject();
			project.loadInvariantsFromFile(file.getPath());
			getAnalyzerBox().showExternalInvariants(project.getInaInvariants());
			//project.genInvariants = project.getCommunicator().getInvariantsList();
			
			lastPath = file.getParentFile().getPath();
		}
		getSimulatorBox().createSimulatorProperties();
	}
	
	/**
	 * Metoda ta mia³a na celu rozpoczêcia analizy sieci Petriego w ramach
	 * pracy in¿ynierskiej. Plan by³ dobry, aczkolwiek w ramach pracy magisterskiej
	 * zaczê³a w koñcu dzia³aæ i uruchamia analizator inwariantów. Nie dzia³aj¹cy
	 * tak jak nale¿y, ale w sumie pewien postêp jest... (MR 8.12.2014)
	 */
	public void tInvariantsAnalyse(){
		PetriNet project = workspace.getProject();
		project.tInvariantsAnalyze();
		//System.out.println("*%^$(*$)$ Barona!!!!!!!!!!!");	
		//powy¿sza metoda przekazuje na konsolê wyrazy uznania od jednego ze studentów
		//dla kolegi, który implementowa³ inne czêœci programu (MR)
	}

	/**
	 * Metoda odpowiedzialna za rozpoczêcie generowania zbiorów MCT.
	 */
	public void generateMCT() {
		DarkAnalyzer analyzer = getWorkspace().getProject().getAnalyzer();
		getMctBox().showMCT(analyzer.generateMCT(analyzer.gettInvariants()));
	}
	
	/**
	 * Metoda zleca wyœwietlenie w³aœciwoœci sieci.
	 */
	public void generateNetProps(){
		NetPropAnalyzer analyzer = getWorkspace().getProject().getNetPropAnal();
		getPropAnalyzerBox().showNetProperties(analyzer.propAnalyze());
	}
	
	/**
	 * Metoda rozpoczyna symulacjê uruchamiania inwariantów.
	 * @param type int - 0-basic, 1- time
	 * @param value - wartoœæ
	 * @throws CloneNotSupportedException
	 */
	public void startInvariantsSimulation(int type, int value) throws CloneNotSupportedException{
		this.getWorkspace().getProject().startInvSim(type, value);
	}
	
	//********************************************************************
	

	
	/**
	 * Metoda uruchamia sekwencjê zdarzeñ prowadz¹ca do wygenerowania inwariantów
	 * za pomoc¹ programu INA dzia³aj¹cego jako niezale¿na aplikacja. Zaleca siê
	 * nie zagl¹danie jak i co ona robi (metoda, nie INA), gdy¿ mo¿e to doprawadziæ
	 * do s³abszych duchem programistów do rozstroju nerwowego, szczególnie w kontekscie
	 * operacji na plikach.
	 */
	public void generateINAinvariants() {
		String stars = "************************************************************************************************";
		//showConsole(true);
		File tmpPNTfile = new File(toolPath+"siec.pnt");
		String x = tmpPNTfile.getPath();
		workspace.getProject().saveToFile(x);
		//zakoñczono zapis do pliku .pnt
		long size = tmpPNTfile.length(); //124 dla nieistniej¹cej (pustej) sieci
		if(size <154) {
			String msg = "Net saving into .pnt file failed. There is a possibility that for the\n"
					+ "moment there is no network drawn. Please check file: \n"+x;
			JOptionPane.showMessageDialog(null, msg, "Missing net or file", JOptionPane.ERROR_MESSAGE);
			log(msg, "error", true);
			return;
		}
		
		File inaExe = new File(toolPath+"INAwin32.exe");
		File batFile = new File(toolPath+"ina.bat");
		File commandFile = new File(toolPath+"COMMAND.ina");
		if(inaExe.exists() && commandFile.exists()) {
			try {
				JOptionPane.showMessageDialog(null, "INAwin32.exe will now start. Click OK and please wait.", "Patience is a virtue", JOptionPane.INFORMATION_MESSAGE);
				log(stars, "text", false);
				log("Activating INAwin32.exe. Please wait, this may take a few seconds due to OS delays.", "text", true);
				//kopiowanie plików:
				Tools.copyFileByPath(inaExe.getPath(), abyssPath+"\\INAwin32.exe");
				Tools.copyFileByPath(batFile.getPath(), abyssPath+"\\ina.bat");
				Tools.copyFileByPath(commandFile.getPath(), abyssPath+"\\COMMAND.ina");
				Tools.copyFileByPath(tmpPNTfile.getPath(), abyssPath+"\\siec.pnt");
				
				String[] command = {"ina.bat"};
			    ProcessBuilder b = new ProcessBuilder(command);
			    Process proc;
			
				proc = b.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while (in.readLine() != null) ; 
				//while (in.readLine() != null) ;
				Thread.sleep(200);
				proc.destroy();
				
				new File(abyssPath+"\\INAwin32.exe").delete();
				new File(abyssPath+"\\ina.bat").delete();
				new File(abyssPath+"\\COMMAND.ina").delete();
				new File(abyssPath+"\\siec.pnt").delete();
				
				File t1 = new File(abyssPath+"\\SESSION.ina");
				File t2 = new File(abyssPath+"\\OPTIONS.ina");
				File t3 = new File(abyssPath+"\\INVARI.hlp");
				if(t1.exists())
					t1.delete();
				if(t2.exists())
					t2.delete();
				if(t3.exists())
					t3.delete();
				log("INAwin32.exe process terminated. Reading results into network now.", "text",true);
			} catch (Exception e) {
				String msg = "I/O operation: activating INA process failed.";
				JOptionPane.showMessageDialog(null, msg, "Critical error", JOptionPane.ERROR_MESSAGE);
				log(msg, "error", true);
				log(stars, "text", false);
				return;
			}
			
			
			// check whether the file with T-invariants has been generated
			File invariantsFile = new File("siec.inv");
			if (!invariantsFile.exists())  
			{
				String msg = "No invariants file - creating using INAwin32.exe unsuccessful.";
				JOptionPane.showMessageDialog(null,msg,	"Critical error",JOptionPane.ERROR_MESSAGE);
				log(msg, "error", true);
				return;
			}
			
			//wczytywanie inwariantów do systemu:
			PetriNet project = workspace.getProject();
			project.loadInvariantsFromFile(invariantsFile.getPath());
			//project.genInvariants = project.getCommunicator().getInvariantsList();
			getAnalyzerBox().showExternalInvariants(project.getInaInvariants());
			getSimulatorBox().createSimulatorProperties();
		
			//co dalej z plikiem?
			Object[] options = {"Save .inv file", "No, thanks",};
			int n = JOptionPane.showOptionDialog(null,
							"Do you want to save generated .inv file?",
							"Save the invariants?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) { //save the file
				JFileChooser fc;
				if(lastPath==null)
					fc = new JFileChooser();
				else
					fc = new JFileChooser(lastPath);
				
				FileFilter inaFilter = new ExtensionFileFilter(".inv - INA Invariants Files", 
						new String[] { "INV" });
				fc.setFileFilter(inaFilter);
				fc.addChoosableFileFilter(inaFilter);
				fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String ext = "";
					if(!file.getPath().contains(".inv"))
						ext = ".inv";
					File properName = new File(file.getPath()+ext);
					Tools.copyFileDirectly(invariantsFile, properName);
					//workspace.getProject().writeInvariantsToInaFormat(file.getPath() + fileExtension);
					setLastPath(file.getParentFile().getPath());
				}
			}
			log("Invariants generation successful.", "text", true);
			log(stars, "text", false);
			invariantsFile.delete();
			//showConsole(false);
		} else { //brak plikow
			String msg = "Missing executables in the tools directory. Required: INAwin32.exe, ina.bat and COMMAND.ina";
			JOptionPane.showMessageDialog(null,msg,	"Missing files",JOptionPane.ERROR_MESSAGE);
			log(msg, "error", true);
		}
	}

	/**
	 * Metoda generuj¹ca najbardziej postawow¹ wersjê pliku zbiorów MCT.
	 */
	public void generateSimpleMCTFile() {
		String filePath = tmpPath + "input.csv";
		int result = workspace.getProject().saveInvariantsToCSV(filePath, true);
		if(result == -1) {
			String msg = "Saving CSV file failed.";
			JOptionPane.showMessageDialog(null,msg,	"Write error",JOptionPane.ERROR_MESSAGE);
			log(msg, "error", true);
			return;
		}
		log("Starting MCT generator.","text",true);
		Runner mctRunner = new Runner();
		String[] args = new String[1];
		args[0] = filePath;
		try {
			mctRunner.activate(args);
			
			JFileChooser fc;
			if(lastPath==null)
				fc = new JFileChooser();
			else
				fc = new JFileChooser(lastPath);
			
			FileFilter mctFilter = new ExtensionFileFilter(".mct - MCT sets",  new String[] { "MCT" });
			fc.setFileFilter(mctFilter);
			fc.addChoosableFileFilter(mctFilter);
			fc.setAcceptAllFileFilterUsed(false);
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String ext = "";
				if(!fc.getSelectedFile().getPath().contains(".mct"))
					ext = ".mct";
				File properName = new File(fc.getSelectedFile().getPath()+ext);
				File generatedMCT = new File(tmpPath+"input.csv.analysed.txt");
				Tools.copyFileDirectly(generatedMCT, properName);
				
				generatedMCT.delete();
				File csvFile = new File(filePath);
				csvFile.delete();
				setLastPath(fc.getSelectedFile().getParentFile().getPath());
			}
			JOptionPane.showMessageDialog(null,"MCT file created","Operation successful.",JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File operation failed when creating MCT sets.", 
					"MCT generator error",JOptionPane.ERROR_MESSAGE);
			log("MCT generator failed: "+e.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda pomocnicza konstruktora, tworzy nowe okno tabeli klastrów.
	 */
	public void createClusterWindow() {
		windowClusters = new AbyssClusters(0);
		windowClusters.setLocationRelativeTo(this);
	}
	
	/**
	 * Metoda s³u¿y do pokazywania lub chowania okna klastrów.
	 */
	public void showClusterWindow(boolean value) {
		if(windowClusters != null) {
			windowClusters.setVisible(value);
		}
	}
	
	/**
	 * Metoda pomocnicza konstruktora, s³u¿y do tworzenia ukrytego okna konsoli logów.
	 */
	private void createHiddenConsole() {
		windowConsole = new AbyssConsole();
		windowConsole.setLocationRelativeTo(this);
	}
	
	/**
	 * Metoda s³u¿y do pokazywania lub chowania okna konsoli.
	 */
	public void showConsole(boolean value) {
		if(windowConsole != null) {
			windowConsole.setVisible(value);
		}
	}
	
	/**
	 * Metoda zapisuj¹ca nowe zdarzenie w oknie logów.
	 * @param text String - tekst zdarzenia
	 * @param mode String - tryb zapisu w oknie
	 * @param time boolean - true, jeœli ma byæ podany czas zdarzenia
	 */
	public void log(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, true);
	}
	/**
	 * Jak wy¿ej, tylko bez entera.
	 */
	public void logNoEnter(String text, String mode, boolean time) {
		windowConsole.addText(text, mode, time, false);
	}
	
	/**
	 * Metoda odpowiedzialna za generowanie klastrowañ na podstawie sieci.
	 */
	public void generateClusters() {
		showConsole(true);
		if(!rReady) { //sprawdŸ, czy Rscript.exe jest na miejscu
			r_env_missing(); // zapytanie gdzie siê podziewa Rscript.exe
			if(!rReady) { //jeœli wci¹¿...
				return;
			}
		}
		
		//generowanie CSV, uda siê, jeœli inwarianty istniej¹
		String filePath = tmpPath + "cluster.csv";
		int result = workspace.getProject().saveInvariantsToCSV(filePath, true);
		if(result == -1) {
			String msg = "Saving CSV file failed. Cluster procedure cannot begin without invariants.";
			JOptionPane.showMessageDialog(null,msg,	"CSV export error",JOptionPane.ERROR_MESSAGE);
			log(msg, "error", true);
			return;
		}
		
		try{
			//TODO: a mo¿e w osobnym oknie? wybór klastrów, metod, itd.
			int c_number = 20;
			String dir_path = "";
			
			Object[] options = {"Select cluster directory", "Use temporary directory",};
			int n = JOptionPane.showOptionDialog(null,
					"Multiple cluster files can we written into default temporary directory (inadvised) or into\n"
					+ "the selected one. What to do?",
					"Directory selection", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				JFileChooser fc;// = new JFileChooser();
				if(lastPath == null)
					fc = new JFileChooser();
				else
					fc = new JFileChooser(lastPath);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File dir = fc.getSelectedFile();
					dir_path = dir.getPath() + "//";
					
					Tools.copyFileByPath(tmpPath + "cluster.csv", dir_path+"cluster.csv");
					log("Cluster files will be put into the "+dir_path, "text", true);
					
				} else { //default one
					dir_path = tmpPath;
					log("Cluster files will be put into the "+dir_path, "text", true);
				}
			} else { //default one
				dir_path = tmpPath;
				log("Cluster files will be put into the "+dir_path, "text", true);
			}
			
			dir_path = dir_path.replace("\\", "/");
			
			log("Cluster files will be put into the ", "text", true);
			log("Cluster files will be put into the ", "text", true);
			log("Cluster files will be put into the ", "text", true);
			log("Cluster files will be put into the ", "text", true);
			log("Cluster files will be put into the ", "text", true);
			
			
			Runnable runnable = new Rprotocols();
			((Rprotocols)runnable).setForAllClusters(settingsManager.getValue("r_path"), dir_path, "cluster.csv", 
					"scripts\\f_clusters.r", "scripts\\f_clusters_run.r", 
					"scripts\\f_clusters_Pearson.r", "scripts\\f_clusters_Pearson_run.r", c_number);
			((Rprotocols)runnable).setType(0);
            Thread thread = new Thread(runnable);
            thread.start();
            
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
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void fillClusterTable() {
		showClusterWindow(true);
		windowClusters.setClusterPath(tmpPath+"cl50");
	}
	
	/*
	public void saveInvCSV() {
		JFileChooser fc;
		if(lastPath==null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		FileFilter csvFilter = new ExtensionFileFilter(".csv - Comma Separated Value", new String[] { "CSV" });
		String fileExtension = ".csv";
		fc.setFileFilter(csvFilter);
		fc.addChoosableFileFilter(csvFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(file.getPath().contains(".csv"))
				fileExtension = "";
			workspace.getProject().saveInvariantsToCSV(file.getPath() + fileExtension);
			setLastPath(file.getParentFile().getPath());
		}
	}
	*/
}
