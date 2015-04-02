package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import abyss.darkgui.GUIManager;
import abyss.settings.SettingsManager;
import abyss.utilities.Tools;

/**
 * Klasa okna ustawień programu.
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
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(20, 20);
		setTitle("Settings");
		setLayout(new BorderLayout());
		setSize(new Dimension(600, 500));
		setResizable(false);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 600, 500);
		ImageIcon icon = Tools.getResIcon16("images/middle.gif");

		//zakładka głównych opcji programu:
		tabbedPane.addTab("System", icon, makeSysPanel(), "Abyss main options.");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = makeTextPanel("Simulator options will go here.");
		tabbedPane.addTab("Simulator", icon, panel2, "Does nothing");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JComponent panel3 = makeTextPanel("Analyzer options will go here.");
		tabbedPane.addTab("Analyzer", icon, panel3, "Does twice as much nothing");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		JComponent panel4 = makeTextPanel("Other options.");
		tabbedPane.addTab("Other", icon, panel4, "Does nothing at all");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBounds(0, 0, 800, 600);
		mainPanel.add(tabbedPane);
		add(mainPanel);
		repaint();
	}
	
	/**
	 * Metoda tworzy panel dla zakładki ogólnych ustawień programu.
	 * @return JPanel - panel
	 */
	private JPanel makeSysPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 500);
		
		//Panel opcji środowiska R
		panel.add(createRoptionsPanel(0, 0, 590, 90)); //dodaj podpanel ustawień R

		//Panel wczytywania sieci
		panel.add(createSnoopyReadPanel(0, 90, 590, 150));
		
		panel.repaint();
		return panel;
	}

	private JPanel createSnoopyReadPanel(int x, int y, int w, int h) {
		JPanel ioPanel = new JPanel(null);
		ioPanel.setBorder(BorderFactory.createTitledBorder("I/O operations"));
		ioPanel.setBounds(x, y, w, h);
		
		int io_x = 10;
		int io_y = 15;
		noAction = true;
		
		JLabel labelIO1 = new JLabel("(Snoopy) Resize net when loaded:");
		labelIO1.setBounds(io_x, io_y, 200, 20);
		ioPanel.add(labelIO1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton resize80Button = new JRadioButton("80%");
		resize80Button.setBounds(io_x, io_y+20, 60, 20);
		resize80Button.setActionCommand("0");
		resize80Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "80", true);
			}
		});
		group.add(resize80Button);
		ioPanel.add(resize80Button);
		
		
		JRadioButton resize100Button = new JRadioButton("100%");
		resize100Button.setBounds(io_x+60, io_y+20, 60, 20);
		resize100Button.setActionCommand("1");
		resize100Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "100", true);
			}
		});
		group.add(resize100Button);
		ioPanel.add(resize100Button);
		
		JRadioButton resize120Button = new JRadioButton("120%");
		resize120Button.setBounds(io_x+120, io_y+20, 60, 20);
		resize120Button.setActionCommand("2");
		resize120Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "120", true);
			}
		});
		group.add(resize120Button);
		ioPanel.add(resize120Button);
		
		JRadioButton resize140Button = new JRadioButton("140%");
		resize140Button.setBounds(io_x, io_y+40, 60, 20);
		resize140Button.setActionCommand("3");
		resize140Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "140", true);
			}
		});
		group.add(resize140Button);
		ioPanel.add(resize140Button);
		
		JRadioButton resize160Button = new JRadioButton("160%");
		resize160Button.setBounds(io_x+60, io_y+40, 60, 20);
		resize160Button.setActionCommand("4");
		resize160Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "160", true);
			}
		});
		group.add(resize160Button);
		ioPanel.add(resize160Button);
		
		JRadioButton resize180Button = new JRadioButton("180%");
		resize180Button.setBounds(io_x+120, io_y+40, 60, 20);
		resize180Button.setActionCommand("5");
		resize180Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("netExtFactor", "180", true);
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
		alignGridWhenSavedCheckBox.setBounds(io_x+200, io_y+20, 240, 20);
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
		useOffsetsCheckBox.setBounds(io_x+200, io_y+40, 260, 20);
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
		
		noAction = false;
		return ioPanel;
	}

	private JPanel createRoptionsPanel(int x, int y, int w, int h) {
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
