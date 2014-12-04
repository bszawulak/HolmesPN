package abyss.darkgui;

import abyss.analyzer.DarkAnalyzer;
//import abyss.analyzer.EarlyInvariantsAnalyzer;
import abyss.analyzer.NetPropAnalyzer;
import abyss.darkgui.box.Properties;
import abyss.darkgui.box.Tools;
import abyss.darkgui.box.Properties.PropertiesType;
import abyss.darkgui.dockable.DeleteAction;
import abyss.darkgui.shortcuts.ShortcutsBar;
import abyss.math.PetriNet;
import abyss.settings.SettingsManager;
import abyss.workspace.ExtensionFileFilter;
import abyss.workspace.Workspace;
//import abyss.workspace.WorkspaceSheet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
	private Properties propertiesBox, simulatorBox, selectionBox, analyzerBox, propAnalyzerBox, 
			mctBox, invSimBox;

	// docking listener
	private DarkDockingListener dockingListener;

	private ShortcutsBar shortcutsBar;

	// SplitDocks
	private SplitDock leftSplitDock, rightSplitDock, totalSplitDock;

	// main frame
	private JFrame frame;

	// other components
	private DarkMenu menu;
	
	private String lastPath;

	// Constructor.

	public GUIManager(JFrame frejm) {
		super(new BorderLayout());
		guiManager = this;
		lastPath = null;

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

		setShortcutsBar(new ShortcutsBar());

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

	public void resetDefaultView() {

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

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Tools getToolBox() {
		return toolBox;
	}

	private void setToolBox(Tools toolBox) {
		this.toolBox = toolBox;
	}

	public Properties getPropertiesBox() {
		return propertiesBox;
	}

	private void setPropertiesBox(Properties propertiesBox) {
		this.propertiesBox = propertiesBox;
	}

	public DarkMenu getMenu() {
		return menu;
	}

	private void setMenu(DarkMenu menu) {
		this.menu = menu;
	}

	public FloatDockModel getDockModel() {
		return dockModel;
	}

	private void setDockModel(FloatDockModel dockModel) {
		this.dockModel = dockModel;
	}

	public static GUIManager getDefaultGUIManager() {
		return guiManager;
	}

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

	public void importProject() {
		JFileChooser fc;
		if(lastPath==null) {
			fc = new JFileChooser();
		} else {
			fc = new JFileChooser(lastPath);	
		}
		FileFilter snoopyFilter = new ExtensionFileFilter(
			".spped - Snoopy Files", new String[] { "SPPED" });
		FileFilter inaFilter = new ExtensionFileFilter(".pnt - INA Files",
				new String[] { "PNT" });
		fc.setFileFilter(snoopyFilter);
		fc.addChoosableFileFilter(snoopyFilter);
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

	public void openProject() {
		JFileChooser fc;
		if(lastPath==null)
		{
		fc = new JFileChooser();
		}
		else
		{
		fc = new JFileChooser(lastPath);	
		}
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
		// getDockModel().removeRootDock(totalSplitDock);
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

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public Properties getSimulatorBox() {
		return simulatorBox;
	}

	private void setSimulatorBox(Properties simulatorBox) {
		this.simulatorBox = simulatorBox;
	}

	public Properties getSelectionBox() {
		return selectionBox;
	}

	private void setSelectionBox(Properties selectionBox) {
		this.selectionBox = selectionBox;
	}

	public void exportProject() {
		JFileChooser fc;
		if(lastPath==null)
		{
		fc = new JFileChooser();
		}
		else
		{
		fc = new JFileChooser(lastPath);	
		}
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
			workspace.getProject().saveToFile(file.getPath() + fileExtension);
			lastPath = file.getParentFile().getPath();
		}
	}
	
	public void exportGeneratedInvariants() {
		JFileChooser fc;
		if(lastPath==null)
		{
		fc = new JFileChooser();
		}
		else
		{
		fc = new JFileChooser(lastPath);	
		}
		FileFilter inaFilter = new ExtensionFileFilter(".inv - INA Invariants Files",
				new String[] { "INV" });
		String fileExtension = ".inv";
		fc.setFileFilter(inaFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			workspace.getProject().writeInvariantsToInaFormat(file.getPath() + fileExtension);
			lastPath = file.getParentFile().getPath();
		}
	}

	public void exportProjectToImage() {
		JFileChooser fc;
		if(lastPath==null)
		{
		fc = new JFileChooser();
		}
		else
		{
		fc = new JFileChooser(lastPath);	
		}
		FileFilter pngFilter = new ExtensionFileFilter(
				".png - Portable Network Graphics", new String[] { "png" });
		FileFilter bmpFilter = new ExtensionFileFilter(
				".bmp -  Bitmap Image File", new String[] { "bmp" });
		FileFilter jpegFilter = new ExtensionFileFilter(
				".jpeg - JPEG Image File", new String[] { "jpeg" });
		fc.setFileFilter(pngFilter);
		fc.addChoosableFileFilter(pngFilter);
		fc.addChoosableFileFilter(bmpFilter);
		fc.addChoosableFileFilter(jpegFilter);
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
			if (extension.contains(".jpeg")) {
				ext = ".jpeg";
			}
			int index = 0;
			for (BufferedImage bi : workspace.getProject()
					.getImagesFromGraphPanels()) {
				try {
					ImageIO.write(bi, ext.substring(1), new File(file.getPath()
							+ "_" + Integer.toString(index) + ext));
					index++;
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			lastPath = file.getParentFile().getPath();
		}
	}

	public void saveProject() {
		JFileChooser fc;
		if(lastPath==null)
		{
		fc = new JFileChooser();
		}
		else
		{
		fc = new JFileChooser(lastPath);	
		}
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

	public ShortcutsBar getShortcutsBar() {
		return shortcutsBar;
	}

	private void setShortcutsBar(ShortcutsBar shortcutsBar) {
		this.shortcutsBar = shortcutsBar;
	}

	public static Dockable externalWithListener(Dockable dockable,
			DockingListener listener) {
		Dockable wrapper = guiManager.decorateDockableWithActions(dockable,
				false);
		wrapper.addDockingListener(listener);
		return wrapper;
	}

	public DarkDockingListener getDockingListener() {
		return dockingListener;
	}

	public void setDockingListener(DarkDockingListener dockingListener) {
		this.dockingListener = dockingListener;
	}

	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	@SuppressWarnings("unused")
	private void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}

	public Properties getAnalyzerBox() {
		return analyzerBox;
	}

	public void setAnalyzerBox(Properties analyzerBox) {
		this.analyzerBox = analyzerBox;
	}
	
	public Properties getPropAnalyzerBox() {
		return propAnalyzerBox;
	}

	public void setPropAnalyzerBox(Properties analyzerBox) {
		this.propAnalyzerBox = analyzerBox;
	}
	
	/*
	 * Wczytywanie inwariantow z INA
	 */
	public void loadExternalAnalysis() {
		JFileChooser fc;
		if(lastPath==null)
		{
		fc = new JFileChooser();
		}
		else
		{
		fc = new JFileChooser(lastPath);	
		}
		FileFilter inaFilter = new ExtensionFileFilter(
				".inv - INA Invariants Files", new String[] { "INV" });
		fc.setFileFilter(inaFilter);
		fc.addChoosableFileFilter(inaFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			PetriNet project = workspace.getProject();
			project.loadInvariantsFromFile(file.getPath());
			getAnalyzerBox().showExternalInvariants(project.getInvariants());
			lastPath = file.getParentFile().getPath();
		}
		getSimulatorBox().createSimulatorProperties();
	}
	
	public void tInvariantsAnalyse(){
		
		PetriNet project = workspace.getProject();
		project.tInvariantsAnalyze();
		System.out.println("Jebac Barona!!!!!!!!!!!");	
	}

	public Properties getMctBox() {
		return mctBox;
	}

	public void setMctBox(Properties mctBox) {
		this.mctBox = mctBox;
	}
	
	public void setInvSim(Properties invSim)
	{
		this.invSimBox = invSim;
	}
	
	public Properties getInvSimBox() {
		return invSimBox;
	}

	public void generateMCT() {
		DarkAnalyzer analyzer = getWorkspace().getProject().getAnalyzer();
		getMctBox().showMCT(analyzer.generateMCT(analyzer.gettInvariants()));
	}
	public void generateNetProps(){
		NetPropAnalyzer analyzer = getWorkspace().getProject().getNetPropAnal();
		getPropAnalyzerBox().showNetProperties(analyzer.propAnalyze());
	}
	
	public void startInvariantsSimulation(int type, int value) throws CloneNotSupportedException{
		this.getWorkspace().getProject().startInvSim(type, value);
		
	}
}
