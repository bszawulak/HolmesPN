package abyss.darkgui;

import abyss.analyzer.DarkAnalyzer;
import abyss.analyzer.NetPropAnalyzer;
import abyss.darkgui.box.Properties;
import abyss.darkgui.box.Tools;
import abyss.darkgui.box.Properties.PropertiesType;
import abyss.darkgui.dockable.DeleteAction;
import abyss.darkgui.toolbar.Toolbar;
import abyss.math.PetriNet;
import abyss.settings.SettingsManager;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
 * graficznych programu, a dalej jakoœ to bêdzie... :)
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
	private CompositeTabDock topRightTabDock, bottomRightTabDock;
	private Tools toolBox;
	private Properties propertiesBox, simulatorBox, selectionBox, analyzerBox, propAnalyzerBox, mctBox, invSimBox;
	// docking listener
	private DarkDockingListener dockingListener;
	private Toolbar shortcutsBar;
	// SplitDocks
	private SplitDock leftSplitDock, rightSplitDock, totalSplitDock;
	// main frame
	private JFrame frame;
	// other components
	private DarkMenu menu;

	// inne wa¿ne zmienne
	private String lastPath;	// ostatnia otwarta œcie¿ka
	private String abyssPath; 	// scie¿ka dostêpu do katalogu g³ównego programu
	private String tmpPath;		// œcie¿ka dostêpu do katalogu plików tymczasowych
	private String toolPath;	// œcie¿ka dostêpu do katalogu narzedziowego
	/**
	 * Konstruktor obiektu klasy GUIManager.
	 * @param frejm JFrame - g³ówna ramka kontener programu
	 */
	public GUIManager(JFrame frejm) {
		super(new BorderLayout());
		guiManager = this;
		lastPath = null;
		abyssPath = System.getProperty("user.dir");
		tmpPath = abyssPath+"\\tmp\\";
		toolPath = abyssPath+"\\tools\\";

		SettingsManager settingsManager = new SettingsManager();
		settingsManager.loadSettings();
		/*
		 * Runtime.getRuntime().addShutdownHook(new Thread() { public void run()
		 * { getSettingsManager().saveSettings(); } });
		 */
		frame = frejm;
		frame.getContentPane().add(this);
		frame.addComponentListener(this);
		setFrame(frejm);
		getFrame().getContentPane().add(this);
		getFrame().addComponentListener(this);

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

		setToolBox(new Tools());
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

		leftTabDock.addChildDock(getToolBox(), new Position(0));
		topRightTabDock.addChildDock(getPropertiesBox(), new Position(0));
		topRightTabDock.addChildDock(getSelectionBox(), new Position(1));
		topRightTabDock.setSelectedDock(getPropertiesBox());
		bottomRightTabDock.addChildDock(getSimulatorBox(), new Position(0));
		bottomRightTabDock.addChildDock(getAnalyzerBox(), new Position(1));
		bottomRightTabDock.addChildDock(getMctBox(), new Position(2));
		bottomRightTabDock.addChildDock(getPropAnalyzerBox(), new Position(3));
		bottomRightTabDock.addChildDock(getInvSimBox(), new Position(4));

		// create the split docks
		leftSplitDock = new SplitDock();
		leftSplitDock.addChildDock(leftTabDock, new Position(Position.LEFT));
		leftSplitDock.addChildDock(getWorkspace().getWorkspaceDock(),
				new Position(Position.CENTER));
		leftSplitDock.setDividerLocation((int) screenSize.getWidth() / 8);

		rightSplitDock = new SplitDock();
		rightSplitDock
				.addChildDock(topRightTabDock, new Position(Position.TOP));
		rightSplitDock.addChildDock(bottomRightTabDock, new Position(
				Position.BOTTOM));
		rightSplitDock
				.setDividerLocation((int) (screenSize.getHeight() * 2 / 5));

		totalSplitDock = new SplitDock();
		totalSplitDock.addChildDock(leftSplitDock, new Position(Position.LEFT));
		totalSplitDock.addChildDock(rightSplitDock,
				new Position(Position.RIGHT));
		totalSplitDock.setDividerLocation((int) screenSize.getWidth()
				- (int) screenSize.getWidth() / 6);

		// // Add root dock
		getDockModel().addRootDock("totalSplitDock", totalSplitDock, getFrame());
		add(totalSplitDock, BorderLayout.CENTER);

		// save docking paths
		DockingManager.getDockingPathModel().add(
				DefaultDockingPath
						.createDockingPath(getToolBox().getDockable()));
		DockingManager.getDockingPathModel().add(
				DefaultDockingPath.createDockingPath(getPropertiesBox()
						.getDockable()));

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
		KeyboardFocusManager manager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyManager(this));
	}

	private void resetSplitDocks() {
		if (getFrame().getExtendedState() == JFrame.MAXIMIZED_BOTH) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			leftSplitDock.setDividerLocation((int) screenSize.getWidth() / 8);
			totalSplitDock
					.setDividerLocation((int) (screenSize.getWidth() * 5.6 / 7));
		} else {
			smallScreenSize = getFrame().getSize();
			leftSplitDock
					.setDividerLocation((int) smallScreenSize.getWidth() / 8);
			totalSplitDock
					.setDividerLocation((int) (smallScreenSize.getWidth() * 5.6 / 7));
		}
	}

	public Dockable decorateDockableWithActions(Dockable dockable,
			boolean deletable) {
		Dockable wrapper = new StateActionDockable(dockable,
				new DefaultDockableStateActionFactory(), new int[0]);
		int[] states = { DockableState.NORMAL, DockableState.MINIMIZED,
				DockableState.MAXIMIZED, DockableState.EXTERNALIZED,
				DockableState.CLOSED };
		wrapper = new StateActionDockable(wrapper,
				new DefaultDockableStateActionFactory(), states);

		if (deletable) {
			DeleteAction deleteAction = new DeleteAction(this, "Delete",
					new ImageIcon("resources/images/page_white_delete.png"));
			Action[][] actions = new Action[1][];
			actions[0] = new Action[1];
			actions[0][0] = deleteAction;
			wrapper = new ActionDockable(wrapper, actions);
			deleteAction.setDockable(wrapper);
		}

		return wrapper;
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
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
	public Tools getToolBox() {
		return toolBox;
	}

	/**
	 * Metoda ustawia nowy obiekt paska narzêdziowego.
	 * @param toolBox Tools - pasek przycisków
	 */
	private void setToolBox(Tools toolBox) {
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
			lastPath = file.getParentFile().getPath();
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
			lastPath = file.getParentFile().getPath();
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
		
		FileFilter inaFilter = new ExtensionFileFilter(".inv - INA Invariants Files", new String[] { "INV" });
		
		String fileExtension = ".inv";
		fc.setFileFilter(inaFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			
			if(file.getPath().contains(".inv"))
				fileExtension = "";
			
			workspace.getProject().saveInvariantsToInaFormat(file.getPath() + fileExtension);
			lastPath = file.getParentFile().getPath();
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
			lastPath = file.getParentFile().getPath();
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
			lastPath = file.getParentFile().getPath();
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
			getAnalyzerBox().showExternalInvariants(project.getInvariants());
			project.genInvariants = project.getCommunicator().getInvariantsList();
			
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
	 * Metoda rozpoczyna symulacjê odpalania inwariantów.
	 * @param type int - 0-basic, 1- time
	 * @param value - wartoœæ
	 * @throws CloneNotSupportedException
	 */
	public void startInvariantsSimulation(int type, int value) throws CloneNotSupportedException{
		this.getWorkspace().getProject().startInvSim(type, value);
		
	}
	
	//********************************************************************
	static void copyFileByPath(String source, String target) throws IOException{
    	InputStream inStream = null;
    	OutputStream outStream = null;
 
   	    File file1 =new File(source);
   	    File file2 =new File(target);
 
   	    inStream = new FileInputStream(file1);
   	    outStream = new FileOutputStream(file2);
 
   	    byte[] buffer = new byte[1024];
 
   	    int length;
   	    while ((length = inStream.read(buffer)) > 0){
   	    	outStream.write(buffer, 0, length);
   	    }
 
   	    if (inStream != null)inStream.close();
   	    if (outStream != null)outStream.close();
    }
	
	static void copyFileDirectly(File source, File target) {
    	InputStream inStream = null;
    	OutputStream outStream = null;
 
   	    try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			 
	   	    int length;
	   	    while ((length = inStream.read(buffer)) > 0){
	   	    	outStream.write(buffer, 0, length);
	   	    }
	 
	   	    if (inStream != null)
	   	    	inStream.close();
	   	    if (outStream != null)
	   	    	outStream.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"I/O operation failed for reason unknown. You can now start panicking.\nHave a nice day!",
					"Critical error", JOptionPane.ERROR_MESSAGE);
			return;
		}
    }
	
	/**
	 * Metoda uruchamia sekwencjê zdarzeñ prowadz¹ca do wygenerowania inwariantów
	 * za pomoc¹ programu INA dzia³aj¹cego jako niezale¿na aplikacja. Zaleca siê
	 * nie zagl¹danie jak i co ona robi (metoda, nie INA), gdy¿ mo¿e to doprawadziæ
	 * do s³abszych duchem programistów do rozstroju nerwowego, szczególnie w kontekscie
	 * operacji na plikach.
	 */
	public void generateINAinvariants() {
		File tmpPNTfile = new File(toolPath+"siec.pnt");
		String x = tmpPNTfile.getPath();
		workspace.getProject().saveToFile(x);
		//zakoñczono zapis do pliku .pnt
		long size = tmpPNTfile.length(); //124 dla nieistniej¹cej (pustej) sieci
		if(size <124) {
			JOptionPane.showMessageDialog(null,
					"Net saving into .pnt file failed somehow. Please check file: \n"+x,
					"Missing file",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		/*
		FileWriter batFile;
		try {
			batFile = new FileWriter(toolPath+"ina.bat");
			batFile.write("START INAwin32.exe COMMAND.ina");
			batFile.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		*/
		File inaExe = new File(toolPath+"INAwin32.exe");
		File batFile = new File(toolPath+"ina.bat");
		File commandFile = new File(toolPath+"COMMAND.ina");
		if(inaExe.exists() && commandFile.exists()) {
			try {
				//kopiowanie plików:
				copyFileByPath(inaExe.getPath(), abyssPath+"\\INAwin32.exe");
				copyFileByPath(batFile.getPath(), abyssPath+"\\ina.bat");
				copyFileByPath(commandFile.getPath(), abyssPath+"\\COMMAND.ina");
				copyFileByPath(tmpPNTfile.getPath(), abyssPath+"\\siec.pnt");
				
				String[] command = {"ina.bat"};
			    ProcessBuilder b = new ProcessBuilder(command);
			    Process proc;
			
				proc = b.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while (in.readLine() != null) ; 
				//while (in.readLine() != null) ;
				Thread.sleep(2000);
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
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"I/O operation: activating INA process failed.",
						"Critical error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			
			// check whether the file with T-invariants has been generated
			File invariantsFile = new File("siec.inv");
			if (!invariantsFile.exists())  
			{
				JOptionPane.showMessageDialog(null,
						"Creating invariants using INAwin32.exe failed.",
						"Critical error",JOptionPane.ERROR_MESSAGE);
				return;
				//throw new FileNotFoundException("File with T-invariants has not been created");
			}
			
			//wczytywanie inwariantów do systemu:
			PetriNet project = workspace.getProject();
			project.loadInvariantsFromFile(invariantsFile.getPath());
			project.genInvariants = project.getCommunicator().getInvariantsList();
			getAnalyzerBox().showExternalInvariants(project.getInvariants());
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
					copyFileDirectly(invariantsFile, properName);
					//workspace.getProject().writeInvariantsToInaFormat(file.getPath() + fileExtension);
					lastPath = file.getParentFile().getPath();
				}
			}
			// check if destination path exists
			//if (pathOut.isEmpty()) pathOut = "tmp";
			//File fOut = new File(pathOut);
			//File dirPath = new File(fOut.getAbsolutePath());
			//if (!dirPath.exists()) dirPath.mkdirs();
			//copyFile("siec.inv", dirPath.toString()+"\\siec.inv");
			invariantsFile.delete();
			
		} else { //brak plikow
			JOptionPane.showMessageDialog(null,
					"Missing executables in the tool directory! Needed: INAwin32.exe, ina.bat and COMMAND.ina",
					"Missing programs",JOptionPane.ERROR_MESSAGE);
		}
	}
}
