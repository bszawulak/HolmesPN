package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import abyss.darkgui.GUIManager;
import abyss.darkgui.settings.SettingsManager;
import abyss.utilities.Tools;

/**
 * Klasa okna ustawień programu.
 * 
 * @author MR
 *
 */
public class AbyssProgramProperties extends JFrame {
	private static final long serialVersionUID = 2831478312283009975L;
	@SuppressWarnings("unused")
	private JFrame parentFrame;
	private SettingsManager sm; //= new SettingsManager();
	private AbyssProgramPropertiesActions action;
	private boolean noAction = false;
	
	/**
	 * Główny konstruktor klasy AbyssProgramProperties.
	 * @param parent
	 */
	public AbyssProgramProperties(JFrame parent) {
		parentFrame = parent;
		sm = GUIManager.getDefaultGUIManager().getSettingsManager();
		action = new AbyssProgramPropertiesActions(sm);
		
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		
		try {
			initialize_components();
		} catch (Exception e) {
			String msg = e.getMessage();
			GUIManager.getDefaultGUIManager().log("Critical error, cannot create Abyss properties Window:", "error", true);
			GUIManager.getDefaultGUIManager().log(msg, "error", false);
		}
	}

	/**
	 * Główna metoda tworząca wszystkie zakładki oraz wypełniająca je zawartością przy
	 * użyciu odpowiednich metod pomocniczych.
	 */
	private void initialize_components() {
		//setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(20, 20);
		setTitle("Settings");
		setLayout(new BorderLayout());
		setSize(new Dimension(600, 500));
		setResizable(false);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 600, 500);

		//zakładka głównych opcji programu:
		tabbedPane.addTab("System", Tools.getResIcon32("/icons/propertiesWindow/systemIcon.png"), makeSysPanel(), "Abyss main options.");
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
	private JPanel createSnoopyReadSystemPanel(int x, int y, int w, int h) {
		JPanel ioPanel = new JPanel(null);
		ioPanel.setBorder(BorderFactory.createTitledBorder("I/O operations"));
		ioPanel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = -5;
		noAction = true;
		
		JLabel labelIO1 = new JLabel("(Snoopy) Resize net when loaded:");
		labelIO1.setBounds(io_x, io_y+=20, 200, 20);
		ioPanel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton resize80Button = new JRadioButton("80%");
		resize80Button.setBounds(io_x, io_y+=20, 60, 20);
		resize80Button.setActionCommand("0");
		resize80Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "80", false);
			}
		});
		group.add(resize80Button);
		ioPanel.add(resize80Button);
		
		
		JRadioButton resize100Button = new JRadioButton("100%");
		resize100Button.setBounds(io_x+60, io_y, 60, 20);
		resize100Button.setActionCommand("1");
		resize100Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "100", false);
			}
		});
		group.add(resize100Button);
		ioPanel.add(resize100Button);
		
		JRadioButton resize120Button = new JRadioButton("120%");
		resize120Button.setBounds(io_x+120, io_y, 60, 20);
		resize120Button.setActionCommand("2");
		resize120Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "120", false);
			}
		});
		group.add(resize120Button);
		ioPanel.add(resize120Button);
		
		JRadioButton resize140Button = new JRadioButton("140%");
		resize140Button.setBounds(io_x, io_y+=20, 60, 20);
		resize140Button.setActionCommand("3");
		resize140Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "140", false);
			}
		});
		group.add(resize140Button);
		ioPanel.add(resize140Button);
		
		JRadioButton resize160Button = new JRadioButton("160%");
		resize160Button.setBounds(io_x+60, io_y, 60, 20);
		resize160Button.setActionCommand("4");
		resize160Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "160", false);
			}
		});
		group.add(resize160Button);
		ioPanel.add(resize160Button);
		
		JRadioButton resize180Button = new JRadioButton("180%");
		resize180Button.setBounds(io_x+120, io_y, 60, 20);
		resize180Button.setActionCommand("5");
		resize180Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "180", false);
			}
		});
		group.add(resize180Button);
		ioPanel.add(resize180Button);
		
		String netExtFactorValue = GUIManager.getDefaultGUIManager().getSettingsManager().getValue("netExtFactor");	
		if(netExtFactorValue.equals("80")) group.setSelected(resize80Button.getModel(), true);
		else if(netExtFactorValue.equals("120")) group.setSelected(resize120Button.getModel(), true);
		else if(netExtFactorValue.equals("140")) group.setSelected(resize140Button.getModel(), true);
		else if(netExtFactorValue.equals("160")) group.setSelected(resize160Button.getModel(), true);
		else if(netExtFactorValue.equals("180")) group.setSelected(resize180Button.getModel(), true);
		else group.setSelected(resize100Button.getModel(), true);
		
		JCheckBox alignGridWhenSavedCheckBox = new JCheckBox("(Snoopy) Align to grid when saved", true);
		alignGridWhenSavedCheckBox.setBounds(io_x+200, io_y-20, 240, 20);
		alignGridWhenSavedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("gridAlignWhenSaved", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("gridAlignWhenSaved", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("gridAlignWhenSaved").equals("1")) 
			alignGridWhenSavedCheckBox.setSelected(true);
		else
			alignGridWhenSavedCheckBox.setSelected(false);
		ioPanel.add(alignGridWhenSavedCheckBox);
		
		JCheckBox useOffsetsCheckBox = new JCheckBox("(Snoopy) Use Snoopy offsets for names", true);
		useOffsetsCheckBox.setBounds(io_x+200, io_y, 260, 20);
		useOffsetsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("usesSnoopyOffsets", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("usesSnoopyOffsets", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("usesSnoopyOffsets").equals("1")) 
			useOffsetsCheckBox.setSelected(true);
		else
			useOffsetsCheckBox.setSelected(false);
		ioPanel.add(useOffsetsCheckBox);
		
		JCheckBox useOldLoaderCheckBox = new JCheckBox("(UNSAFE) Use old Snoopy loader (PN, extPN, TPN/DPN *ONLY*)", true);
		useOldLoaderCheckBox.setBounds(io_x, io_y+=20, 400, 20);
		useOldLoaderCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("usesOldSnoopyLoaders", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("usesOldSnoopyLoaders", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("usesOldSnoopyLoaders").equals("1")) 
			useOldLoaderCheckBox.setSelected(true);
		else
			useOldLoaderCheckBox.setSelected(false);
		ioPanel.add(useOldLoaderCheckBox);
		
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
		rSetPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				action.setRPath();
				textR_1.setText(sm.getValue("r_path"));
				textR_2.setText(sm.getValue("r_path64"));
			}
		});
		rOptionsPanel.add(rSetPath);
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
		panel.add(createGraphicalEditorPanel(0, 0, 590, 120));

		//Panel opcji ogólnych edytora
		panel.add(createGeneralEditorPanel(0, 120, 590, 150));
		
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
	private JPanel createGraphicalEditorPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Graphical settings"));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = -5;
		noAction = true;
		
		// ARC SIZE:
		JLabel labelIO1 = new JLabel("Default arc thickness:");
		labelIO1.setBounds(io_x, io_y+=20, 200, 20);
		panel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton size1Button = new JRadioButton("1");
		size1Button.setBounds(io_x, io_y+=20, 40, 20);
		size1Button.setActionCommand("0");
		size1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("graphArcLineSize", "1", true);
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		group.add(size1Button);
		panel.add(size1Button);

		JRadioButton size2Button = new JRadioButton("2");
		size2Button.setBounds(io_x+40, io_y, 40, 20);
		size2Button.setActionCommand("1");
		size2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("graphArcLineSize", "2", true);
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		group.add(size2Button);
		panel.add(size2Button);

		JRadioButton size3Button = new JRadioButton("3");
		size3Button.setBounds(io_x+80, io_y, 40, 20);
		size3Button.setActionCommand("2");
		size3Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("graphArcLineSize", "3", true);
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		group.add(size3Button);
		panel.add(size3Button);
		
		String thickValue = GUIManager.getDefaultGUIManager().getSettingsManager().getValue("graphArcLineSize");	
		if(thickValue.equals("1")) group.setSelected(size1Button.getModel(), true);
		else if(thickValue.equals("2")) group.setSelected(size2Button.getModel(), true);
		else if(thickValue.equals("3")) group.setSelected(size3Button.getModel(), true);

		//FONT SIZE:
		JLabel labelFontSize = new JLabel("Font size:");
		labelFontSize.setBounds(io_x+150, io_y-20, 200, 20);
		panel.add(labelFontSize);
		SpinnerModel fontSizeSpinnerModel = new SpinnerNumberModel(
				Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("graphFontSize")), 8, 15, 1);
		JSpinner fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
		fontSizeSpinner.setBounds(io_x+150, io_y, 80, 20);
		fontSizeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(noAction) return;
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("graphFontSize", ""+val, true);
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		panel.add(fontSizeSpinner);
		//BOLD:
		boolean bold = true;
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("graphFontBold").equals("0"))
			bold = false;
			
		JCheckBox boldCheckBox = new JCheckBox("Bold", bold);
		boldCheckBox.setBounds(io_x+210, io_y-20, 60, 20);
		boldCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(noAction) return;
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("graphFontBold", "1", true);
				else
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("graphFontBold", "0", true);
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		panel.add(boldCheckBox);
		
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("graphFontBold").equals("1")) 
			boldCheckBox.setSelected(true);
		else
			boldCheckBox.setSelected(false);
		
		
		JCheckBox mctNameCheckBox = new JCheckBox("MCT names", bold);
		mctNameCheckBox.setBounds(io_x+270, io_y-20, 110, 20);
		mctNameCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(noAction) return;
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("mctNameShow", "1", true);
				else
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("mctNameShow", "0", true);
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		panel.add(mctNameCheckBox);
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("mctNameShow").equals("1")) 
			mctNameCheckBox.setSelected(true);
		else
			mctNameCheckBox.setSelected(false);
		
		JCheckBox useShortNamesCheckBox = new JCheckBox("(Abyss) Show short default names only", true);
		useShortNamesCheckBox.setBounds(io_x, io_y+=20, 260, 20);
		useShortNamesCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction == true) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("showShortNames", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("showShortNames", "0", true);
				}
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("showShortNames").equals("1")) 
			useShortNamesCheckBox.setSelected(true);
		else
			useShortNamesCheckBox.setSelected(false);
		panel.add(useShortNamesCheckBox);
		
		JCheckBox view3dCheckBox = new JCheckBox("(Abyss) Petri net elements 3d view", true);
		view3dCheckBox.setBounds(io_x, io_y+=20, 260, 20);
		view3dCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction == true) return;
				
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editor3Dview", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("editor3Dview", "0", true);
				}
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editor3Dview").equals("1")) 
			view3dCheckBox.setSelected(true);
		else
			view3dCheckBox.setSelected(false);
		panel.add(view3dCheckBox);
		
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
	private JPanel createGeneralEditorPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("General settings"));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = -5;
		noAction = true;
		
		JCheckBox snoopyCompatibilityCheckBox = new JCheckBox("(Snoopy/Abyss) Allow only Snoopy-compatible options", true);
		snoopyCompatibilityCheckBox.setBounds(io_x, io_y+=20, 350, 20);
		snoopyCompatibilityCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction == true) return;
				
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("snoopyCompatibleMode", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("snoopyCompatibleMode", "0", true);
				}
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("snoopyCompatibleMode").equals("1")) 
			snoopyCompatibilityCheckBox.setSelected(true);
		else
			snoopyCompatibilityCheckBox.setSelected(false);
		panel.add(snoopyCompatibilityCheckBox);
		
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
		
		panel.add(createSimPanel1(0, 0, 590, 190));
		
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
	private JPanel createSimPanel1(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Petri net general options"));
		panel.setBounds(x, y, w, h);
		int io_x = 10;
		int io_y = -5;
		noAction = true;

		JCheckBox readArcReservCheckBox = new JCheckBox("Transitions reserve tokens in place via read-arcs", true);
		readArcReservCheckBox.setBounds(io_x, io_y+=20, 360, 20);
		readArcReservCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("simTransReadArcTokenReserv", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("simTransReadArcTokenReserv", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("simTransReadArcTokenReserv").equals("1")) 
			readArcReservCheckBox.setSelected(true);
		else
			readArcReservCheckBox.setSelected(false);
		panel.add(readArcReservCheckBox);
		
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
		
		panel.add(createInvariantsPanel(0, 0, 590, 90));
		panel.add(createFeasibleInvPanel(0, 90, 590, 90));
		panel.add(createMCSPanel(0, 180, 590, 90));
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
	private JPanel createInvariantsPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Invariants generator"));
		panel.setBounds(x, y, w, h);
		
		noAction = true;

		noAction = false;
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel opcji wykonalnych inwariantów.
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	private JPanel createFeasibleInvPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("Feasible invariants options"));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = 15;
		noAction = true;
		
		// Self-propelled read-arc regions ignored or not in feasible invariants algorith
		JCheckBox feasInvSelfPropCheckBox = new JCheckBox("Allow presence of self-propelled readarc regions", true);
		feasInvSelfPropCheckBox.setBounds(io_x, io_y, 360, 20);
		feasInvSelfPropCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisFeasibleSelfPropAccepted", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisFeasibleSelfPropAccepted", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisFeasibleSelfPropAccepted").equals("1")) 
			feasInvSelfPropCheckBox.setSelected(true);
		else
			feasInvSelfPropCheckBox.setSelected(false);
		panel.add(feasInvSelfPropCheckBox);
		
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
	private JPanel createMCSPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("MCS generator"));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = 15;
		noAction = true;
		
		JCheckBox cleanMCSusingStructureCheckBox = new JCheckBox("Eliminate MCS sets non directly connected with objR transition.", true);
		cleanMCSusingStructureCheckBox.setBounds(io_x, io_y, 400, 20);
		cleanMCSusingStructureCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(noAction) return;
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisMCSReduction", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisMCSReduction", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisMCSReduction").equals("1")) 
			cleanMCSusingStructureCheckBox.setSelected(true);
		else
			cleanMCSusingStructureCheckBox.setSelected(false);
		panel.add(cleanMCSusingStructureCheckBox);
		
		
		noAction = false;
		return panel;
	}
	
//**********************************************************************************************************************
//*********************************************     DEFAULT TAB    *****************************************************
//**********************************************************************************************************************

	
	/**
	 * Wypełnianie pustą zawartością
	 * @param text
	 * @return
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
}
