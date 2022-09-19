package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serial;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import holmes.darkgui.GUIManager;
import holmes.darkgui.settings.SettingsManager;
import holmes.utilities.Tools;

/**
 * Klasa okna ustawień programu.
 */
public class HolmesProgramProperties extends JFrame {
	@Serial
	private static final long serialVersionUID = 2831478312283009975L;
	@SuppressWarnings("unused")
	private JFrame parentFrame;
	private SettingsManager sm; //= new SettingsManager();
	private HolmesProgramPropertiesActions action;
	private boolean noAction = false;
	
	/**
	 * Główny konstruktor klasy HolmesProgramProperties.
	 * @param parent JFrame - GUIManager
	 */
	public HolmesProgramProperties(JFrame parent) {
		parentFrame = parent;
		sm = GUIManager.getDefaultGUIManager().getSettingsManager();
		action = new HolmesProgramPropertiesActions(sm);
		
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error (688341781) | Exception:  "+ex.getMessage(), "error", true);
		}
		
		try {
			initialize_components();
		} catch (Exception e) {
			String msg = e.getMessage();
			GUIManager.getDefaultGUIManager().log("Critical error, cannot create Holmes properties window:" , "error", true);
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
		}
	}

	/**
	 * Główna metoda tworząca wszystkie zakładki oraz wypełniająca je zawartością przy
	 * użyciu odpowiednich metod pomocniczych.
	 */
	private void initialize_components() {
		this.setLocation(20, 20);
		setTitle("Settings");
		setLayout(new BorderLayout());
		setSize(new Dimension(600, 500));
		setResizable(false);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 600, 500);

		//zakładka głównych opcji programu:
		tabbedPane.addTab("System", Tools.getResIcon32("/icons/propertiesWindow/systemIcon.png"), makeSysPanel(), "Holmes main options.");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		tabbedPane.addTab("Editor", Tools.getResIcon32("/icons/propertiesWindow/editorIcon.png"), makeEditorPanel(), "Editor options");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		tabbedPane.addTab("Simulator", Tools.getResIcon32("/icons/propertiesWindow/simulationIcon.png"), makeSimulatorPanel(), "Simulator options");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_3);

		tabbedPane.addTab("Analyzer", Tools.getResIcon32("/icons/propertiesWindow/analysisIcon.png"), makeAnalysisPanel(), "Does twice as much nothing");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_4);

		JComponent panel5 = makeTextPanel("Other options.");
		tabbedPane.addTab("Other", Tools.getResIcon32("/icons/propertiesWindow/otherIcon.png"), panel5, "Does nothing at all");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_5);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBounds(0, 0, 800, 600);
		mainPanel.add(tabbedPane);
		add(mainPanel);
		repaint();
	}
	
//**********************************************************************************************************************
//*********************************************      SYSTEM TAB    *****************************************************
//**********************************************************************************************************************
	
	/**
	 * Metoda tworzy panel dla zakładki ogólnych ustawień programu.
	 * @return JPanel - panel
	 */
	private JPanel makeSysPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 500);
		
		//Panel opcji środowiska R
		panel.add(createRoptionsSystemPanel(0, 0, 590, 90)); //dodaj podpanel ustawień R

		//Panel wczytywania sieci
		panel.add(createSnoopyReadSystemPanel(0, 90, 590, 150));

		//Panel wczytywania sieci
		panel.add(createOtherOptionsPanel(0, 240, 590, 200));

		panel.repaint();
		return panel;
	}

	/**
	 * Metoda tworząca podpanel opcji Snoopiego.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createSnoopyReadSystemPanel(int x, int y, int w, int h) {
		JPanel ioPanel = new JPanel(null);
		ioPanel.setBorder(BorderFactory.createTitledBorder("I/O operations"));
		ioPanel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 15;
		noAction = true;
		
		JLabel labelIO1 = new JLabel("(Snoopy) Resize net when loaded:");
		labelIO1.setBounds(posX, posY, 200, 20);
		ioPanel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton resize80Button = new JRadioButton("80%");
		resize80Button.setBounds(posX, posY+=20, 60, 20);
		resize80Button.setActionCommand("0");
		resize80Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "80", false);
		});
		group.add(resize80Button);
		ioPanel.add(resize80Button);
		
		
		JRadioButton resize100Button = new JRadioButton("100%");
		resize100Button.setBounds(posX+60, posY, 60, 20);
		resize100Button.setActionCommand("1");
		resize100Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "100", true);
		});
		group.add(resize100Button);
		ioPanel.add(resize100Button);
		
		JRadioButton resize120Button = new JRadioButton("120%");
		resize120Button.setBounds(posX+120, posY, 60, 20);
		resize120Button.setActionCommand("2");
		resize120Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "120", false);
		});
		group.add(resize120Button);
		ioPanel.add(resize120Button);
		
		JRadioButton resize140Button = new JRadioButton("140%");
		resize140Button.setBounds(posX, posY+=20, 60, 20);
		resize140Button.setActionCommand("3");
		resize140Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "140", false);
		});
		group.add(resize140Button);
		ioPanel.add(resize140Button);
		
		JRadioButton resize160Button = new JRadioButton("160%");
		resize160Button.setBounds(posX+60, posY, 60, 20);
		resize160Button.setActionCommand("4");
		resize160Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "160", false);
		});
		group.add(resize160Button);
		ioPanel.add(resize160Button);
		
		JRadioButton resize180Button = new JRadioButton("180%");
		resize180Button.setBounds(posX+120, posY, 60, 20);
		resize180Button.setActionCommand("5");
		resize180Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "180", false);
		});
		group.add(resize180Button);
		ioPanel.add(resize180Button);
		
		String netExtFactorValue = GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programSnoopyLoaderNetExtFactor");
		switch (netExtFactorValue) {
			case "80" -> group.setSelected(resize80Button.getModel(), true);
			case "120" -> group.setSelected(resize120Button.getModel(), true);
			case "140" -> group.setSelected(resize140Button.getModel(), true);
			case "160" -> group.setSelected(resize160Button.getModel(), true);
			case "180" -> group.setSelected(resize180Button.getModel(), true);
			default -> group.setSelected(resize100Button.getModel(), true);
		}
		
		JCheckBox alignGridWhenSavedCheckBox = checkboxWizard("(Snoopy) Align to grid when saved", posX+200, posY-20, 240, 20, 
				"editorGridAlignWhenSaved", true);
		ioPanel.add(alignGridWhenSavedCheckBox);
		
		JCheckBox useOffsetsCheckBox = checkboxWizard("(Snoopy) Use Snoopy offsets for names", posX+200, posY, 260, 20, 
				"editorUseSnoopyOffsets", true);
		ioPanel.add(useOffsetsCheckBox);
		
		JCheckBox useOldLoaderCheckBox = checkboxWizard("(UNSAFE) Use old Snoopy loader (PN, extPN, TPN/DPN *ONLY*)", posX, posY+=20, 400, 20, 
				"programUseOldSnoopyLoaders", true);
		ioPanel.add(useOldLoaderCheckBox);

		JCheckBox checkSaveCheckBox = checkboxWizard("Warnings concerning wrong save format", posX, posY+=20, 300, 20, 
				"editorExportCheckAndWarning", true);
		ioPanel.add(checkSaveCheckBox);
		
		JCheckBox simpleEditorCheckBox = checkboxWizard("Use simple notepad (restart required)", posX, posY+=20, 300, 20,
				"programUseSimpleEditor", true);
		ioPanel.add(simpleEditorCheckBox);
		
		noAction = false;
		return ioPanel;
	}
	
	//
	/**
	 * Metoda tworząca podpanel innych opcji.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createOtherOptionsPanel(int x, int y, int w, int h) {
		JPanel ioPanel = new JPanel(null);
		ioPanel.setBorder(BorderFactory.createTitledBorder("Other options"));
		ioPanel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 20;
		noAction = true;
		
		JCheckBox alignGridWhenSavedCheckBox = checkboxWizard("Debug mode", posX, posY, 240, 20, 
				"programDebugMode", true);
		ioPanel.add(alignGridWhenSavedCheckBox);

		noAction = false;
		return ioPanel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji środowiska R.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createRoptionsSystemPanel(int x, int y, int w, int h) {
		JPanel rOptionsPanel = new JPanel(null);
		rOptionsPanel.setBorder(BorderFactory.createTitledBorder("R settings"));
		rOptionsPanel.setBounds(x, y, w, h);
		
		JLabel labelR_1 = new JLabel("R path:");
		labelR_1.setBounds(10, 16, 60, 20);
		rOptionsPanel.add(labelR_1);
		final JTextArea textR_1 = new JTextArea(sm.getValue("r_path"));
		textR_1.setBounds(75, 18, 300, 20);
		textR_1.setOpaque(false);
		textR_1.setEditable(false);
		rOptionsPanel.add(textR_1);
		
		JLabel labelR_2 = new JLabel("Rx64 path:");
		labelR_2.setBounds(10, 36, 60, 20);
		rOptionsPanel.add(labelR_2);
		final JTextArea textR_2 = new JTextArea(sm.getValue("r_path64"));
		textR_2.setBounds(75, 38, 300, 20);
		textR_2.setOpaque(false);
		textR_2.setEditable(false);
		rOptionsPanel.add(textR_2);
		
		JButton rSetPath = new JButton("Set R path");
		rSetPath.setName("setRpath");
		rSetPath.setBounds(10, 60, 120, 20);
		rSetPath.setToolTipText("Manually set path to Rscript.exe");
		rSetPath.addActionListener(actionEvent -> {
			action.setRPath();
			textR_1.setText(sm.getValue("r_path"));
			textR_2.setText(sm.getValue("r_path64"));
		});
		rOptionsPanel.add(rSetPath);
		
		JCheckBox forceRcheckBox = checkboxWizard("Force R localization on startup", 140, 60, 210, 20, "programAskForRonStartup", true);
		rOptionsPanel.add(forceRcheckBox);
		
		return rOptionsPanel;
	}
	
//**********************************************************************************************************************
//*********************************************      EDITOR TAB    *****************************************************
//**********************************************************************************************************************

	/**
	 * Metoda tworzy panel dla zakładki ogólnych ustawień programu.
	 * @return JPanel - panel
	 */
	private JPanel makeEditorPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 500);
		
		//Panel opcji graficznych edytora
		panel.add(createGraphicalEditorPanel(0, 0, 590, 150));

		//Panel opcji ogólnych edytora
		panel.add(createGeneralEditorPanel(0, 150, 590, 150));
		
		panel.repaint();
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji graficznych.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createGraphicalEditorPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Graphical settings"));
		panel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 15;
		noAction = true;
		
		// ARC SIZE:
		JLabel labelIO1 = new JLabel("Default arc thickness:");
		labelIO1.setBounds(posX, posY, 200, 20);
		panel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton size1Button = new JRadioButton("1");
		size1Button.setBounds(posX, posY+20, 40, 20);
		size1Button.setActionCommand("0");
		size1Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editorGraphArcLineSize", "1", true);
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		group.add(size1Button);
		panel.add(size1Button);

		JRadioButton size2Button = new JRadioButton("2");
		size2Button.setBounds(posX+40, posY+20, 40, 20);
		size2Button.setActionCommand("1");
		size2Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editorGraphArcLineSize", "2", true);
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		group.add(size2Button);
		panel.add(size2Button);

		JRadioButton size3Button = new JRadioButton("3");
		size3Button.setBounds(posX+80, posY+20, 40, 20);
		size3Button.setActionCommand("2");
		size3Button.addActionListener(actionEvent -> {
			if(noAction) return;
			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editorGraphArcLineSize", "3", true);
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		group.add(size3Button);
		panel.add(size3Button);
		
		String thickValue = GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorGraphArcLineSize");
		switch (thickValue) {
			case "1" -> group.setSelected(size1Button.getModel(), true);
			case "2" -> group.setSelected(size2Button.getModel(), true);
			case "3" -> group.setSelected(size3Button.getModel(), true);
		}

		//FONT SIZE:
		JLabel labelFontSize = new JLabel("Font size:");
		labelFontSize.setBounds(posX+150, posY, 200, 20);
		panel.add(labelFontSize);
		
		JCheckBox boldCheckBox = checkboxWizard("Bold", posX+210, posY, 60, 20, 
				"editorGraphFontBold", true);
		panel.add(boldCheckBox);
		
		JCheckBox mctNameCheckBox = checkboxWizard("MCT names", posX+270, posY, 110, 20, 
				"mctNameShow", true);
		panel.add(mctNameCheckBox);
		
		
		SpinnerModel fontSizeSpinnerModel = new SpinnerNumberModel(
				Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorGraphFontSize")), 7, 30, 1);
		JSpinner fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
		fontSizeSpinner.setBounds(posX+150, posY+=20, 80, 20);
		fontSizeSpinner.addChangeListener(e -> {
			if(noAction) return;
			JSpinner spinner = (JSpinner) e.getSource();
			int val = (int) spinner.getValue();

			GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editorGraphFontSize", ""+val, true);
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		panel.add(fontSizeSpinner);

		JCheckBox useShortNamesCheckBox = checkboxWizard("(Editor) Show short default names only", posX, posY+=20, 260, 20, 
				"editorShowShortNames", true);
		panel.add(useShortNamesCheckBox);

		JCheckBox useShortNamesLowerIndexCheckBox = checkboxWizard("(Editor) Show short names with lower index", posX, posY+=20, 300, 20,
				"editorShortNameLowerIndex", true);
		panel.add(useShortNamesLowerIndexCheckBox);
	
		JCheckBox view3dCheckBox = checkboxWizard("(Editor) Petri net elements 3d view", posX, posY+=20, 260, 20, 
				"editor3Dview", true);
		panel.add(view3dCheckBox);
		
		JCheckBox snoopyStyleCheckBox = checkboxWizard("(Editor) Show Snoopy-styled graphics", posX, posY+=20, 260, 20, 
				"editorSnoopyStyleGraphic", true);
		panel.add(snoopyStyleCheckBox);
		
		JCheckBox snoopyColorsBox = checkboxWizard("(Editor) Show non default T/P colors", posX+270, posY, 260, 20, 
				"editorSnoopyColors", true);
		panel.add(snoopyColorsBox);
		
		noAction = false;
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji graficznych.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createGeneralEditorPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("General settings"));
		panel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 15;
		noAction = true;
		
		JCheckBox snoopyCompatibilityCheckBox = new JCheckBox("(Snoopy/Holmes) Allow only Snoopy-compatible options", true);
		snoopyCompatibilityCheckBox.setBounds(posX, posY, 400, 20);
		snoopyCompatibilityCheckBox.addActionListener(actionEvent -> {
			if(noAction) return;

			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editorSnoopyCompatibleMode", "1", true);
			} else {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editorSnoopyCompatibleMode", "0", true);
			}
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		snoopyCompatibilityCheckBox.setSelected(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorSnoopyCompatibleMode").equals("1"));
		panel.add(snoopyCompatibilityCheckBox);
		
		JCheckBox subnetCompressionCheckBox = checkboxWizard("Use meta-arcs compression for metanodes", posX, posY+=20, 350, 20, 
				"editorSubnetCompressMode", true);
		panel.add(subnetCompressionCheckBox);
		
		noAction = false;
		return panel;
	}
	
	//**********************************************************************************************************************
	//*********************************************    SIMULATOR TAB   *****************************************************
	//**********************************************************************************************************************

	/**
	 * Metoda tworzy główny panel opcji symulatora
	 * @return JPanel - panel
	 */
	public JPanel makeSimulatorPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 500);
		
		panel.add(createSimPanel(0, 0, 590, 190));
		
		panel.add(createSimGraphic(0, 190, 590, 150));
		
		panel.repaint();
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji inwariantów.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createSimPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Simulator engine options"));
		panel.setBounds(x, y, w, h);
		int posX = 10;
		int posY = 15;
		noAction = true;

		JCheckBox readArcReservCheckBox = checkboxWizard("Transitions reserve tokens in place via read-arcs", 
				posX, posY, 360, 20, "simTransReadArcTokenReserv", true);
		panel.add(readArcReservCheckBox);
		
		JCheckBox singleMaxModeCheckBox = checkboxWizard("Single-maximum mode (single-50/50 when unchecked)", 
				posX, posY+=20, 360, 20, "simSingleMode", true);
		panel.add(singleMaxModeCheckBox);
		
		JCheckBox simTDPNrunTimeCheckBox = checkboxWizard("TDPN transition acts like DPN when TPN internal clock = EFT", 
				posX, posY+=20, 380, 20, "simTDPNrunWhenEft", true);
		panel.add(simTDPNrunTimeCheckBox);

		JCheckBox placesColorsCheckBox = checkboxWizard("Places change colors during simulation", 
				posX, posY+=20, 360, 20, "simPlacesColors", true);
		panel.add(placesColorsCheckBox);

		JCheckBox XTPNsimMassActionCheckBox = checkboxWizard("(XTPN) Globally use mass-action kinetics law for simulation",
				posX, posY+=20, 400, 20, "simXTPNmassAction", true);
		panel.add(XTPNsimMassActionCheckBox);

		JCheckBox XTPNsimReadArcTokenCheckBox = checkboxWizard("(XTPN) Read arcs preserve tokens lifetime",
				posX, posY+=20, 360, 20, "simXTPNreadArcTokens", true);
		panel.add(XTPNsimReadArcTokenCheckBox);

		noAction = false;
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji inwariantów.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createSimGraphic(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Simulator graphical options"));
		panel.setBounds(x, y, w, h);
		int io_x = 10;
		int io_y = 15;
		noAction = true;

		JLabel transDelayLabel = new JLabel("Transition firing delay:");
		transDelayLabel.setBounds(io_x, io_y, 200, 20);
		panel.add(transDelayLabel);
		
		JLabel arcDelayLabel = new JLabel("Arc token delay:");
		arcDelayLabel.setBounds(io_x+280, io_y, 200, 20);
		panel.add(arcDelayLabel);
		
		final JSlider arcDelaySlider = new JSlider(JSlider.HORIZONTAL, 5, 85, 25);
		arcDelaySlider.setBounds(io_x+280, io_y+=20, 250, 50);
		arcDelaySlider.setMinorTickSpacing(2);
		arcDelaySlider.setMajorTickSpacing(10);
		arcDelaySlider.setPaintTicks(true);
		arcDelaySlider.setPaintLabels(true);
		arcDelaySlider.setLabelTable(arcDelaySlider.createStandardLabels(10));
		arcDelaySlider.addChangeListener(e -> {
			JSlider s = (JSlider) e.getSource();
			int val = s.getValue();
			int reference = GUIManager.getDefaultGUIManager().simSettings.getTransitionGraphicDelay();
			if(val <= reference) {
				arcDelaySlider.setValue(val);
				GUIManager.getDefaultGUIManager().simSettings.setArcGraphicDelay(val);
			} else {
				s.setValue(reference);
			}
		});
	    panel.add(arcDelaySlider);
	    
		final JSlider transDelaySlider = new JSlider(JSlider.HORIZONTAL, 5, 85, 25);
		transDelaySlider.setBounds(io_x, io_y, 250, 50);
	    transDelaySlider.setMinorTickSpacing(2);
	    transDelaySlider.setMajorTickSpacing(10);
	    transDelaySlider.setPaintTicks(true);
	    transDelaySlider.setPaintLabels(true);
	    transDelaySlider.setLabelTable(transDelaySlider.createStandardLabels(10));
	    transDelaySlider.addChangeListener(new ChangeListener() {
	    	private JSlider anotherSlider = null;
            public void stateChanged(ChangeEvent e) {
            	JSlider s = (JSlider) e.getSource();
            	int value = s.getValue();
                transDelaySlider.setValue(value);
                GUIManager.getDefaultGUIManager().simSettings.setTransitionGraphicDelay(value);
                if(value <  GUIManager.getDefaultGUIManager().simSettings.getArcGraphicDelay()) {
                	anotherSlider.setValue(value);
                }
            }
            private ChangeListener yesWeCan(JSlider slider){
            	anotherSlider = slider;
		        return this;
		    }
		}.yesWeCan(arcDelaySlider) );
	    panel.add(transDelaySlider);

		
		
		noAction = false;
		return panel;
	}
	
	//**********************************************************************************************************************
	//*********************************************     ANALYSIS TAB   *****************************************************
	//**********************************************************************************************************************

	/**
	 * Metoda tworzy główny panel opcji analizatora.
	 * @return JPanel - panel
	 */
	public JPanel makeAnalysisPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 500);
		
		panel.add(createClustersOptionsPanel(0, 0, 590, 60));
		//panel.add(createClusteringPanel(0, 90, 590, 90));
		panel.add(createMCSPanel(0, 60, 590, 60));
		panel.repaint();
		return panel;
		
	}
	
	/**
	 * Metoda tworząca podpanel opcji inwariantów.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createClustersOptionsPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Clusters options"));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = 15;
		noAction = true;

		JCheckBox binaryTinvCheckBox = checkboxWizard("Save t-invariants in CSV as binary vectors.", io_x, io_y, 360, 20, 
				"analysisBinaryCSVInvariants", false);
		panel.add(binaryTinvCheckBox);
		
		// Self-propelled read-arc regions ignored or not in feasible invariants algorith
		JCheckBox feasInvSelfPropCheckBox = checkboxWizard("Allow presence of self-propelled readarc regions", io_x, io_y+=20, 360, 20, 
				"analysisFeasibleSelfPropAccepted", true);
		panel.add(feasInvSelfPropCheckBox);
		
		noAction = false;
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji dla klastrów.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createClusteringPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Cluster algorithms options"));
		panel.setBounds(x, y, w, h);
		
		//int io_x = 10;
		//int io_y = 15;
		noAction = true;
		
		
		
		noAction = false;
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji MCS.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createMCSPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("MCS generator"));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = 15;
		noAction = true;
		
		JCheckBox cleanMCSusingStructureCheckBox = checkboxWizard("Eliminate MCS sets non directly connected with objR transition.", 
				io_x, io_y, 400, 20, "analysisMCSReduction", true);
		panel.add(cleanMCSusingStructureCheckBox);
		
		
		noAction = false;
		return panel;
	}
	
//**********************************************************************************************************************
//*********************************************     DEFAULT TAB    *****************************************************
//**********************************************************************************************************************

	
	/**
	 * Wypełnianie pustą zawartością
	 * @param text (<b>String</b>)
	 * @return JComponent
	 */
	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        //panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        panel.setBounds(0, 0, 640, 480);
        return panel;
    }
	
	//**********************************************************************************************************************
	//**********************************************************************************************************************
	//**********************************************************************************************************************
	
	/**
	 * Tworzenie predefiniowanego obiektu JCheckBox.
	 * @param checkBName String - nazwa wyświetlana w oknie
	 * @param xPos int - pozycja X
	 * @param yPos int - pozycja Y
	 * @param width int - szerokość
	 * @param height int - wysokość
	 * @param propName String - nazwa właściwości
	 * @param autosave boolean - true, jeśli zmiana ma być zapisywana do pliku config
	 * @return JCheckBox - obiekt
	 */
	private JCheckBox checkboxWizard(String checkBName, int xPos, int yPos, int width, int height, String propName, boolean autosave) {
		JCheckBox view3dCheckBox = new JCheckBox(checkBName, true);
		view3dCheckBox.setBounds(xPos, yPos, width, height);
		view3dCheckBox.addActionListener(new ActionListener() {
			private String propName = "";
			private boolean autoSave;
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue(propName, "1", autoSave);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue(propName, "0", autoSave);
				}
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
			private ActionListener yesWeCan(String name, boolean autosave) {
				this.propName = name;
				this.autoSave = autosave;
		        return this;
		    }
		}.yesWeCan(propName, autosave));

		view3dCheckBox.setSelected(GUIManager.getDefaultGUIManager().getSettingsManager().getValue(propName).equals("1"));
		return view3dCheckBox;
	}
}
