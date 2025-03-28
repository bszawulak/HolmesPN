package holmes.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
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
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private SettingsManager sm; //= new SettingsManager();
	private HolmesProgramPropertiesActions action;
	private boolean noAction = false;
	
	/**
	 * Główny konstruktor klasy HolmesProgramProperties.
	 * @param parent JFrame - GUIManager
	 */
	public HolmesProgramProperties(JFrame parent) {
		parentFrame = parent;
		sm = overlord.getSettingsManager();
		action = new HolmesProgramPropertiesActions(sm);
		
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00500exception")+"\n"+ex.getMessage(), "error", true);
		}
		
		try {
			initialize_components();
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00501exception")+"\n"+ex.getMessage(), "error", true);
		}
	}

	/**
	 * Główna metoda tworząca wszystkie zakładki oraz wypełniająca je zawartością przy
	 * użyciu odpowiednich metod pomocniczych.
	 */
	private void initialize_components() {
		Point pPoint = parentFrame.getLocation();
		this.setLocation(pPoint.x+300, pPoint.y+150);
		//this.setLocation(20, 20);
		setTitle(lang.getText("HPPwin_entry001title"));
		setLayout(new BorderLayout());
		setSize(new Dimension(600, 500));
		setResizable(false);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 600, 500);

		tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
			@Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 32;
			}
			@Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
				super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
			}
		});

		//zakładka głównych opcji programu:
		tabbedPane.addTab(lang.getText("HPPwin_entry002"), Tools.getResIcon32("/icons/propertiesWindow/systemIcon.png")
				, makeSysPanel(), lang.getText("HPPwin_entry002t")); //System
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		tabbedPane.addTab(lang.getText("HPPwin_entry003"), Tools.getResIcon32("/icons/propertiesWindow/editorIcon.png")
				, makeEditorPanel(), lang.getText("HPPwin_entry003t")); //Editor
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		tabbedPane.addTab(lang.getText("HPPwin_entry004"), Tools.getResIcon32("/icons/propertiesWindow/simulationIcon.png")
				, makeSimulatorPanel(), lang.getText("HPPwin_entry004t")); //Simulator
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_3);

		tabbedPane.addTab(lang.getText("HPPwin_entry005"), Tools.getResIcon32("/icons/propertiesWindow/analysisIcon.png")
				, makeAnalysisPanel(), lang.getText("HPPwin_entry005t")); //Analyzer
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_4);

		JComponent panel5 = makeTextPanel("Other options."); //Other
		tabbedPane.addTab(lang.getText("HPPwin_entry006"), Tools.getResIcon32("/icons/propertiesWindow/otherIcon.png")
				, panel5, lang.getText("HPPwin_entry006t"));
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
		panel.add(createLanguagesSelectionPanel(0, 240, 590, 100));
		
		panel.add(createOtherOptionsPanel(0, 340, 590, 100));

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
		ioPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSYS_entry007"))); //I/O operations
		ioPanel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 15;
		noAction = true;
		
		JLabel labelIO1 = new JLabel(lang.getText("HPPwinSYS_entry008")); //(Snoopy) Resize net when loaded:
		labelIO1.setBounds(posX, posY, 200, 20);
		ioPanel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton resize80Button = new JRadioButton("80%");
		resize80Button.setBounds(posX, posY+=20, 60, 20);
		resize80Button.setActionCommand("0");
		resize80Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "80", false);
		});
		group.add(resize80Button);
		ioPanel.add(resize80Button);
		
		
		JRadioButton resize100Button = new JRadioButton("100%");
		resize100Button.setBounds(posX+60, posY, 60, 20);
		resize100Button.setActionCommand("1");
		resize100Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "100", true);
		});
		group.add(resize100Button);
		ioPanel.add(resize100Button);
		
		JRadioButton resize120Button = new JRadioButton("120%");
		resize120Button.setBounds(posX+120, posY, 60, 20);
		resize120Button.setActionCommand("2");
		resize120Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "120", false);
		});
		group.add(resize120Button);
		ioPanel.add(resize120Button);
		
		JRadioButton resize140Button = new JRadioButton("140%");
		resize140Button.setBounds(posX, posY+=20, 60, 20);
		resize140Button.setActionCommand("3");
		resize140Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "140", false);
		});
		group.add(resize140Button);
		ioPanel.add(resize140Button);
		
		JRadioButton resize160Button = new JRadioButton("160%");
		resize160Button.setBounds(posX+60, posY, 60, 20);
		resize160Button.setActionCommand("4");
		resize160Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "160", false);
		});
		group.add(resize160Button);
		ioPanel.add(resize160Button);
		
		JRadioButton resize180Button = new JRadioButton("180%");
		resize180Button.setBounds(posX+120, posY, 60, 20);
		resize180Button.setActionCommand("5");
		resize180Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("programSnoopyLoaderNetExtFactor", "180", false);
		});
		group.add(resize180Button);
		ioPanel.add(resize180Button);
		
		String netExtFactorValue = overlord.getSettingsManager().getValue("programSnoopyLoaderNetExtFactor");
		switch (netExtFactorValue) {
			case "80" -> group.setSelected(resize80Button.getModel(), true);
			case "120" -> group.setSelected(resize120Button.getModel(), true);
			case "140" -> group.setSelected(resize140Button.getModel(), true);
			case "160" -> group.setSelected(resize160Button.getModel(), true);
			case "180" -> group.setSelected(resize180Button.getModel(), true);
			default -> group.setSelected(resize100Button.getModel(), true);
		}
		
		JCheckBox alignGridWhenSavedCheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry009"), posX+220, posY-20, 350, 20, 
				"editorGridAlignWhenSaved", true); //(Snoopy) Align to grid when saved
		ioPanel.add(alignGridWhenSavedCheckBox);
		
		JCheckBox useOffsetsCheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry010"), posX+220, posY, 350, 20, 
				"editorUseSnoopyOffsets", true); //(Snoopy) Use Snoopy offsets for names
		ioPanel.add(useOffsetsCheckBox);
		
		JCheckBox useOldLoaderCheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry011"), posX, posY+=20, 560, 20, 
				"programUseOldSnoopyLoaders", true); //(UNSAFE) Use old Snoopy loader (PN, extPN, TPN/DPN *ONLY*)
		ioPanel.add(useOldLoaderCheckBox);

		JCheckBox checkSaveCheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry012"), posX, posY+=20, 500, 20, 
				"editorExportCheckAndWarning", true); //Warnings concerning wrong save format
		ioPanel.add(checkSaveCheckBox);
		
		JCheckBox simpleEditorCheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry013"), posX, posY+=20, 500, 20,
				"programUseSimpleEditor", true); //Use simple notepad (restart required)
		ioPanel.add(simpleEditorCheckBox);
		
		noAction = false;
		return ioPanel;
	}
	
	/**
	 * Metoda tworząca podpanel zmiany języka
	 * @param x int - współrzędna X
	 * @param y int - współrzedna Y
	 * @param w int - szerokość
	 * @param h int - wysokość
	 * @return JPanel - panel
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createLanguagesSelectionPanel(int x, int y, int w, int h) {
		JPanel langSelectionPanel = new JPanel(null);
		langSelectionPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSYS_entry050"))); //Other options
		langSelectionPanel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 20;
		noAction = true;

		//JPanel langPanel = new JPanel(null);
		//langPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSYS_entry050"))); //Language:
		//langPanel.setBounds(10, 20, 200, 110);
		//langSelectionPanel.add(langPanel);

		//PRZYCISK ANGIELSKI:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/english.png"));
			JLabel flagLabelEN = new JLabel(new ImageIcon(image));
			flagLabelEN.setBounds(posX, posY, 38, 20);
			langSelectionPanel.add(flagLabelEN);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}
		
		ButtonGroup group = new ButtonGroup();
		JRadioButton englishRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry051")); //English
		englishRadioButton.setBounds(posX+=40, posY, 95, 20);
		englishRadioButton.setActionCommand("0");
		englishRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;
			
			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry051"), oldLang);
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("English", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(englishRadioButton);
		langSelectionPanel.add(englishRadioButton);

		//PRZYCISK POLSKI:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/poland.png"));
			JLabel flagLabelPL = new JLabel(new ImageIcon(image));
			flagLabelPL.setBounds(posX+=100, posY, 38, 20);
			langSelectionPanel.add(flagLabelPL);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}
		
		JRadioButton polishRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry052")); //Polish
		polishRadioButton.setBounds(posX+=40, posY, 95, 20);
		polishRadioButton.setActionCommand("1");
		polishRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB	= "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry052"), oldLang);
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true); //Polish
			}
			lang.setLanguage("Polish", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(polishRadioButton);
		langSelectionPanel.add(polishRadioButton);

		//PRZYCISK NIEMIECKI:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/germany.png"));
			JLabel flagLabelMNT = new JLabel(new ImageIcon(image));
			flagLabelMNT.setBounds(posX+=100, posY, 38, 20);
			langSelectionPanel.add(flagLabelMNT);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}

		JRadioButton germanRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry055")); //German
		germanRadioButton.setBounds(posX+=40, posY, 95, 20);
		germanRadioButton.setActionCommand("1");
		germanRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry055"), oldLang); //German
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("German", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(germanRadioButton);
		langSelectionPanel.add(germanRadioButton);

		//PRZYCISK HISZPANSKI:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/spain.png"));
			JLabel flagLabelMNT = new JLabel(new ImageIcon(image));
			flagLabelMNT.setBounds(posX+=100, posY, 38, 20);
			langSelectionPanel.add(flagLabelMNT);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}

		JRadioButton spainRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry056")); //Spanish
		spainRadioButton.setBounds(posX+=40, posY, 95, 20);
		spainRadioButton.setActionCommand("1");
		spainRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry056"), oldLang); //Spanish
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("Spanish", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(spainRadioButton);
		langSelectionPanel.add(spainRadioButton);

		posX = 10;
		posY+=40;

		//PRZYCISK UKRAINSKI:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/ukraine.png"));
			JLabel flagLabelMNT = new JLabel(new ImageIcon(image));
			flagLabelMNT.setBounds(posX, posY, 38, 20);
			langSelectionPanel.add(flagLabelMNT);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}

		JRadioButton ukrainianRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry057")); //Ukrainian
		ukrainianRadioButton.setBounds(posX+=40, posY, 95, 20);
		ukrainianRadioButton.setActionCommand("1");
		ukrainianRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry057"), oldLang); //Ukrainian
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("Ukrainian", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(ukrainianRadioButton);
		langSelectionPanel.add(ukrainianRadioButton);

		//PRZYCISK FRANCUSKI:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/france.png"));
			JLabel flagLabelMNT = new JLabel(new ImageIcon(image));
			flagLabelMNT.setBounds(posX+=100, posY, 38, 20);
			langSelectionPanel.add(flagLabelMNT);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}

		JRadioButton frenchRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry058")); //French
		frenchRadioButton.setBounds(posX+=40, posY, 95, 20);
		frenchRadioButton.setActionCommand("1");
		frenchRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry058"), oldLang); //French
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("French", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(frenchRadioButton);
		langSelectionPanel.add(frenchRadioButton);


		//PRZYCISK WŁOSKI
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/italy.png"));
			JLabel flagLabelMNT = new JLabel(new ImageIcon(image));
			flagLabelMNT.setBounds(posX+=100, posY, 38, 20);
			langSelectionPanel.add(flagLabelMNT);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}

		JRadioButton italianRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry059")); //Italian
		italianRadioButton.setBounds(posX+=40, posY, 95, 20);
		italianRadioButton.setActionCommand("1");
		italianRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry059"), oldLang); //Italian
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("Italian", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(italianRadioButton);
		langSelectionPanel.add(italianRadioButton);


		//PRZYCISK TWOJ JEZYK:
		try {
			BufferedImage image = ImageIO.read(getClass().getResource("/icons/propertiesWindow/yourlang.png"));
			JLabel flagLabelMNT = new JLabel(new ImageIcon(image));
			flagLabelMNT.setBounds(posX+=100, posY, 38, 20);
			langSelectionPanel.add(flagLabelMNT);
		} catch (Exception ex) {
			overlord.log(ex.getMessage(), "error", true);
		}

		JRadioButton yourLangRadioButton = new JRadioButton(lang.getText("HPPwinSYS_entry054")); //YourLanguage
		yourLangRadioButton.setBounds(posX+=40, posY, 95, 20);
		yourLangRadioButton.setActionCommand("1");
		yourLangRadioButton.addActionListener(actionEvent -> {
			if(noAction) return;

			String oldLang = changeKeyWordIntoLangWord(lang.getSelectedLanguage());
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HPPwinSYS_entry053"), lang.getText("HPPwinSYS_entry054"), oldLang); //YourLanguage
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" HPPwinSYS_entry053", "error", true);
			}
			lang.setLanguage("YourLang", false);
			JOptionPane.showMessageDialog(null, strB, lang.getText("information"),
					JOptionPane.INFORMATION_MESSAGE);
		});
		group.add(yourLangRadioButton);
		langSelectionPanel.add(yourLangRadioButton);
		
		String current = lang.getSelectedLanguage();
        switch (current) {
            case "English" -> group.setSelected(englishRadioButton.getModel(), true);
            case "Polish" -> group.setSelected(polishRadioButton.getModel(), true);
            case "German" -> group.setSelected(germanRadioButton.getModel(), true);
            case "Spanish" -> group.setSelected(spainRadioButton.getModel(), true);
            case "Ukrainian" -> group.setSelected(ukrainianRadioButton.getModel(), true);
            case "French" -> group.setSelected(frenchRadioButton.getModel(), true);
            case "Italian" -> group.setSelected(italianRadioButton.getModel(), true);
            default -> group.setSelected(yourLangRadioButton.getModel(), true);
        }
		
		noAction = false;
		return langSelectionPanel;
	}

	/**
	 * 	Metoda dostaje na wejściu nazwę języka w takiej postaci jak wartości zmiennych programu, zwraca
	 * 	nazwę języka z aktualnego słownika.
	 * @param selectedLanguage String - nazwa języka w postaci zmiennej programu
	 * @return String - nazwa języka w postaci z aktualnego słownika
	 */
	private String changeKeyWordIntoLangWord(String selectedLanguage) {
        return switch (selectedLanguage) {
            case "English" -> lang.getText("HPPwinSYS_entry051");
            case "Polish" -> lang.getText("HPPwinSYS_entry052");
            case "German" -> lang.getText("HPPwinSYS_entry055");
            case "Spanish" -> lang.getText("HPPwinSYS_entry056");
            case "Ukrainian" -> lang.getText("HPPwinSYS_entry057");
            case "France" -> lang.getText("HPPwinSYS_entry058");
            case "Italian" -> lang.getText("HPPwinSYS_entry059");
            default -> lang.getText("HPPwinSYS_entry054");
        };
	}

	private Component createOtherOptionsPanel(int x, int y, int w, int h) {
		JPanel otherOptPanel = new JPanel(null);
		otherOptPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSYS_entry049"))); //Other options
		otherOptPanel.setBounds(x, y, w, h);

		int posX = 10;
		int posY = 20;
		noAction = true;
		
		JCheckBox alignGridWhenSavedCheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry014"), posX, posY, 120, 20,
				"programDebugMode", true); //Debug mode
		otherOptPanel.add(alignGridWhenSavedCheckBox);

		//********************************
		
		JSeparator sep = new JSeparator();
		sep.setOrientation(SwingConstants.VERTICAL);
		sep.setBounds(posX+140, posY, 2, 60);
		otherOptPanel.add(sep);

		JLabel testUILabel = new JLabel("HIGHLY experimental UI change");
		testUILabel.setBounds(posX+150, posY, 200, 20);
		otherOptPanel.add(testUILabel);

		String strWarning = "IMPORTANT! This change will take effect after restart.\n" +
				"\nEVEN MORE IMPORTANT: if after this Holmes do not start, either delete" +
				"\nholmes.cfg file and restart OR (more subtle), set systemUI setting in" +
				"\nholmes.cfg file into 0 (instead of any other number).";
		
		ButtonGroup group = new ButtonGroup();
		JRadioButton uiButton0 = new JRadioButton("Standard");
		uiButton0.setBounds(posX+150, posY+=20, 90, 20);
		uiButton0.setActionCommand("0");
		uiButton0.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("systemUI", "0", true);
			JOptionPane.showMessageDialog(null, strWarning, lang.getText("warning"),
					JOptionPane.WARNING_MESSAGE);
		});
		group.add(uiButton0);
		otherOptPanel.add(uiButton0);
		
		JRadioButton uiButton1 = new JRadioButton("Nimbus");
		uiButton1.setBounds(posX+245, posY, 90, 20);
		uiButton1.setActionCommand("1");
		uiButton1.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("systemUI", "1", true);
			JOptionPane.showMessageDialog(null, strWarning, lang.getText("warning"),
					JOptionPane.WARNING_MESSAGE);
		});
		group.add(uiButton1);
		otherOptPanel.add(uiButton1);

		JRadioButton uiButton2 = new JRadioButton("CDE/Motif");
		uiButton2.setBounds(posX+340, posY, 90, 20);
		uiButton2.setActionCommand("2");
		uiButton2.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("systemUI", "2", true);
			JOptionPane.showMessageDialog(null, strWarning, lang.getText("warning"),
					JOptionPane.WARNING_MESSAGE);
		});
		group.add(uiButton2);
		otherOptPanel.add(uiButton2);

		JRadioButton uiButton3 = new JRadioButton("Windows");
		uiButton3.setBounds(posX+150, posY+20, 90, 20);
		uiButton3.setActionCommand("3");
		uiButton3.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("systemUI", "3", true);
			JOptionPane.showMessageDialog(null, strWarning, lang.getText("warning"),
					JOptionPane.WARNING_MESSAGE);
		});
		group.add(uiButton3);
		otherOptPanel.add(uiButton3);

		JRadioButton uiButton4 = new JRadioButton("Windows classic");
		uiButton4.setBounds(posX+245, posY+20, 140, 20);
		uiButton4.setActionCommand("4");
		uiButton4.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("systemUI", "4", true);
			JOptionPane.showMessageDialog(null, strWarning, lang.getText("warning"),
					JOptionPane.WARNING_MESSAGE);
		});
		group.add(uiButton4);
		otherOptPanel.add(uiButton4);

		String current = overlord.getSettingsManager().getValue("systemUI");
        switch (current) {
            case "0" -> group.setSelected(uiButton0.getModel(), true);
            case "1" -> group.setSelected(uiButton1.getModel(), true);
            case "2" -> group.setSelected(uiButton2.getModel(), true);
            case "3" -> group.setSelected(uiButton3.getModel(), true);
            default -> group.setSelected(uiButton4.getModel(), true);
        }
		
		noAction = false;
		return otherOptPanel;
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
		rOptionsPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSYS_entry015"))); //R settings
		rOptionsPanel.setBounds(x, y, w, h);
		
		JLabel labelR_1 = new JLabel(lang.getText("HPPwinSYS_entry016")); //label
		labelR_1.setBounds(10, 16, 90, 20);
		rOptionsPanel.add(labelR_1);
		final JTextArea textR_1 = new JTextArea(sm.getValue("r_path"));
		textR_1.setBounds(105, 18, 360, 20);
		textR_1.setOpaque(false);
		textR_1.setEditable(false);
		rOptionsPanel.add(textR_1);
		
		JLabel labelR_2 = new JLabel(lang.getText("HPPwinSYS_entry017")); //Rx64 path
		labelR_2.setBounds(10, 36, 90, 20);
		rOptionsPanel.add(labelR_2);
		final JTextArea textR_2 = new JTextArea(sm.getValue("r_path64"));
		textR_2.setBounds(105, 38, 360, 20);
		textR_2.setOpaque(false);
		textR_2.setEditable(false);
		rOptionsPanel.add(textR_2);
		
		JButton rSetPath = new JButton(lang.getText("HPPwinSYS_entry018")); //Set R path
		rSetPath.setName("setRpath");
		rSetPath.setBounds(10, 60, 140, 20);
		rSetPath.setToolTipText(lang.getText("HPPwinSYS_entry018t"));
		rSetPath.addActionListener(actionEvent -> {
			action.setRPath();
			textR_1.setText(sm.getValue("r_path"));
			textR_2.setText(sm.getValue("r_path64"));
		});
		rOptionsPanel.add(rSetPath);
		
		JCheckBox forceRcheckBox = checkboxWizard(lang.getText("HPPwinSYS_entry019"), 160, 60, 300, 20, "programAskForRonStartup", true);
		rOptionsPanel.add(forceRcheckBox); //Force R localization on startup
		
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
		panel.add(createGraphicalEditorPanel(0, 0, 590, 210));

		//Panel opcji ogólnych edytora
		panel.add(createGeneralEditorPanel(0, 210, 590, 90));
		
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
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinEDIT_entry020"))); //Graphical settings
		panel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 15;
		noAction = true;
		
		// ARC SIZE:
		JLabel labelIO1 = new JLabel(lang.getText("HPPwinEDIT_entry021")); //Default arc thickness
		labelIO1.setBounds(posX, posY, 240, 20);
		panel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton size1Button = new JRadioButton("1");
		size1Button.setBounds(posX, posY+20, 40, 20);
		size1Button.setActionCommand("0");
		size1Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("editorGraphArcLineSize", "1", true);
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		group.add(size1Button);
		panel.add(size1Button);

		JRadioButton size2Button = new JRadioButton("2");
		size2Button.setBounds(posX+40, posY+20, 40, 20);
		size2Button.setActionCommand("1");
		size2Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("editorGraphArcLineSize", "2", true);
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		group.add(size2Button);
		panel.add(size2Button);

		JRadioButton size3Button = new JRadioButton("3");
		size3Button.setBounds(posX+80, posY+20, 40, 20);
		size3Button.setActionCommand("2");
		size3Button.addActionListener(actionEvent -> {
			if(noAction) return;
			overlord.getSettingsManager().setValue("editorGraphArcLineSize", "3", true);
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		group.add(size3Button);
		panel.add(size3Button);
		
		String thickValue = overlord.getSettingsManager().getValue("editorGraphArcLineSize");
		switch (thickValue) {
			case "1" -> group.setSelected(size1Button.getModel(), true);
			case "2" -> group.setSelected(size2Button.getModel(), true);
			case "3" -> group.setSelected(size3Button.getModel(), true);
		}

		//FONT SIZE:
		JLabel labelFontSize = new JLabel(lang.getText("HPPwinEDIT_entry022")); //Font size
		labelFontSize.setBounds(posX+210, posY, 100, 20);
		panel.add(labelFontSize);
		
		JCheckBox boldCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry023"), posX+320, posY, 110, 20, 
				"editorGraphFontBold", true); //Bold
		panel.add(boldCheckBox);
		
		JCheckBox mctNameCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry024"), posX+320, posY+20, 110, 20, 
				"mctNameShow", true); //MCT names
		mctNameCheckBox.setToolTipText(lang.getText("HPPwinEDIT_entry024t"));
		panel.add(mctNameCheckBox);
		
		
		SpinnerModel fontSizeSpinnerModel = new SpinnerNumberModel(
				Integer.parseInt(overlord.getSettingsManager().getValue("editorGraphFontSize")), 7, 30, 1);
		JSpinner fontSizeSpinner = new JSpinner(fontSizeSpinnerModel);
		fontSizeSpinner.setBounds(posX+210, posY+=20, 80, 20);
		fontSizeSpinner.addChangeListener(e -> {
			if(noAction) return;
			JSpinner spinner = (JSpinner) e.getSource();
			int val = (int) spinner.getValue();

			overlord.getSettingsManager().setValue("editorGraphFontSize", ""+val, true);
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		panel.add(fontSizeSpinner);

		JCheckBox useShortNamesCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry025"), posX, posY+=20, 420, 20, 
				"editorShowShortNames", true); //(Editor) Show short default names only
		panel.add(useShortNamesCheckBox);

		JCheckBox useShortNamesLowerIndexCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry026"), posX, posY+=20, 420, 20,
				"editorShortNameLowerIndex", true); //(Editor) Show short names with lower index
		panel.add(useShortNamesLowerIndexCheckBox);
	
		JCheckBox view3dCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry027"), posX, posY+=20, 420, 20, 
				"editor3Dview", true); //(Editor) Petri net elements 3d view
		panel.add(view3dCheckBox);
		
		JCheckBox snoopyStyleCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry028"), posX, posY+=20, 420, 20, 
				"editorSnoopyStyleGraphic", true); //(Editor) Show Snoopy-styled graphics
		panel.add(snoopyStyleCheckBox);

		JCheckBox portalLinesBox = checkboxWizard(lang.getText("HPPwinEDIT_entry030"), posX, posY+=20, 420, 20,
				"editorPortalLines", true); //(Editor) Show lines between portal locations
		panel.add(portalLinesBox);

		JCheckBox snoopyColorsBox = checkboxWizard(lang.getText("HPPwinEDIT_entry029"), posX, posY+=20, 420, 20,
				"editorSnoopyColors", true); //(Editor) Show non default T/P colors
		panel.add(snoopyColorsBox);

		JCheckBox portalNewVersionBox = checkboxWizard(lang.getText("HPPwinEDIT_entry127"), posX, posY+=20, 420, 20,
				"editorNewPortalPlace", true); //(Editor) Petri net elements 3d view
		panel.add(portalNewVersionBox);
		
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
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinEDIT_entry031"))); //General settings
		panel.setBounds(x, y, w, h);
		
		int posX = 10;
		int posY = 15;
		noAction = true;
		
		JCheckBox snoopyCompatibilityCheckBox = new JCheckBox(lang.getText("HPPwinEDIT_entry032"), true); //(Snoopy/Holmes) Allow only Snoopy-compatible options
		snoopyCompatibilityCheckBox.setBounds(posX, posY, 500, 20);
		snoopyCompatibilityCheckBox.addActionListener(actionEvent -> {
			if(noAction) return;

			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				overlord.getSettingsManager().setValue("editorSnoopyCompatibleMode", "1", true);
			} else {
				overlord.getSettingsManager().setValue("editorSnoopyCompatibleMode", "0", true);
			}
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		snoopyCompatibilityCheckBox.setSelected(overlord.getSettingsManager().getValue("editorSnoopyCompatibleMode").equals("1"));
		panel.add(snoopyCompatibilityCheckBox);
		
		JCheckBox subnetCompressionCheckBox = checkboxWizard(lang.getText("HPPwinEDIT_entry033"), posX, posY+=20, 500, 20, 
				"editorSubnetCompressMode", true); //Use meta-arcs compression for metanodes
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
		
		panel.add(createSimPanel(0, 0, 590, 150));
		
		panel.add(createSimGraphic(0, 150, 590, 100));
		
		panel.add(createSimXTPNPanel(0, 250, 590, 100));
		
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
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSIM_entry034"))); //Simulator engine options
		panel.setBounds(x, y, w, h);
		int posX = 10;
		int posY = 15;
		noAction = true;

		JCheckBox readArcReservCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry035"), 
				posX, posY, 500, 20, "simTransReadArcTokenReserv", true); //Transitions reserve tokens in place via read-arcs
		panel.add(readArcReservCheckBox);
		
		JCheckBox singleMaxModeCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry036"), 
				posX, posY+=20, 500, 20, "simSingleMode", true); //Single-maximum mode (single-50/50 when unchecked)
		panel.add(singleMaxModeCheckBox);
		
		JCheckBox simTDPNrunTimeCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry037"), 
				posX, posY+=20, 500, 20, "simTDPNrunWhenEft", true); //TDPN transition acts like DPN when TPN internal clock = EFT
		panel.add(simTDPNrunTimeCheckBox);

		JCheckBox placesColorsCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry038"), 
				posX, posY+=20, 500, 20, "simPlacesColors", true); //Places change colors during simulation
		panel.add(placesColorsCheckBox);

		JCheckBox isSimulatorLoggedCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry044"),
				posX, posY+=20, 500, 20, "simLogEnabled", true); //(XTPN) Read arcs preserve tokens lifetime
		panel.add(isSimulatorLoggedCheckBox);

		noAction = false;
		return panel;
	}

	private JPanel createSimXTPNPanel(int x, int y, int w, int h) {
		JPanel panel = new JPanel(null);
		panel.setBorder(BorderFactory.createTitledBorder("XTPN")); //Simulator engine options
		panel.setBounds(x, y, w, h);
		int posX = 10;
		int posY = 15;
		noAction = true;

		JCheckBox XTPNsimMassActionCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry039"),
				posX, posY, 500, 20, "simXTPNmassAction", true); //(XTPN) Globally use mass-action kinetics law for simulation
		panel.add(XTPNsimMassActionCheckBox);

		JCheckBox XTPNsimReadArcTokenCheckBox = checkboxWizard(lang.getText("HPPwinSIM_entry040"),
				posX, posY+=20, 500, 20, "simXTPNreadArcTokens", true); //(XTPN) Read arcs preserve tokens lifetime
		panel.add(XTPNsimReadArcTokenCheckBox);

		JCheckBox XTPNsimReadArcDoNotTakeTokensCheckBox = checkboxWizard("(XTPN) Read arcs do not take tokens",
				posX, posY+=20, 500, 20, "simXTPNreadArcDoNotTakeTokens", true); 
		panel.add(XTPNsimReadArcDoNotTakeTokensCheckBox);
		

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
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinSIM_entry041"))); //Simulator graphical options
		panel.setBounds(x, y, w, h);
		int io_x = 10;
		int io_y = 15;
		noAction = true;

		JLabel transDelayLabel = new JLabel(lang.getText("HPPwinSIM_entry042")); //Transition firing delay
		transDelayLabel.setBounds(io_x, io_y, 200, 20);
		panel.add(transDelayLabel);
		
		JLabel arcDelayLabel = new JLabel(lang.getText("HPPwinSIM_entry043")); //Arc token delay
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
			int reference = overlord.simSettings.getTransitionGraphicDelay();
			if(val <= reference) {
				arcDelaySlider.setValue(val);
				overlord.simSettings.setArcGraphicDelay(val);
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
				overlord.simSettings.setTransitionGraphicDelay(value);
                if(value <  overlord.simSettings.getArcGraphicDelay()) {
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
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinANAL_entry044")));
		panel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = 15;
		noAction = true;

		JCheckBox binaryTinvCheckBox = checkboxWizard(lang.getText("HPPwinANAL_entry045"), io_x, io_y, 450, 20, 
				"analysisBinaryCSVInvariants", false); //Save t-invariants in CSV as binary vectors.
		panel.add(binaryTinvCheckBox);
		
		// Self-propelled read-arc regions ignored or not in feasible invariants algorith
		JCheckBox feasInvSelfPropCheckBox = checkboxWizard(lang.getText("HPPwinANAL_entry046"), io_x, io_y+=20, 450, 20, 
				"analysisFeasibleSelfPropAccepted", true); //Allow presence of self-propelled readarc regions
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
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HPPwinANAL_entry047"))); //Cluster algorithms options
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
		
		JCheckBox cleanMCSusingStructureCheckBox = checkboxWizard(lang.getText("HPPwinANAL_entry048"), 
				io_x, io_y, 510, 20, "analysisMCSReduction", true); //Eliminate MCS sets non directly connected with objR transition.
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
					overlord.getSettingsManager().setValue(propName, "1", autoSave);
				} else {
					overlord.getSettingsManager().setValue(propName, "0", autoSave);
				}
				overlord.getWorkspace().repaintAllGraphPanels();
			}
			private ActionListener yesWeCan(String name, boolean autosave) {
				this.propName = name;
				this.autoSave = autosave;
		        return this;
		    }
		}.yesWeCan(propName, autosave));

		view3dCheckBox.setSelected(overlord.getSettingsManager().getValue(propName).equals("1"));
		return view3dCheckBox;
	}
}
