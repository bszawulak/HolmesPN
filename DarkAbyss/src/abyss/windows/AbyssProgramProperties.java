package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private JFrame parentFrame;
	private SettingsManager sm; //= new SettingsManager();
	AbyssProgramPropertiesActions action;
	
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
		
		//PANEL OPCJI ŚRODOWISKA R
		JPanel rOptionsPanel = new JPanel(null);
		rOptionsPanel.setBorder(BorderFactory.createTitledBorder("R settings"));
		rOptionsPanel.setBounds(0, 0, 590, 90);
		
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
		panel.add(rOptionsPanel); //dodaj podpanel ustawień R
		
		//*********************************************************************************************
		//*********************************************************************************************
		//*********************************************************************************************
		
		//Panel wczytywania sieci
		JPanel ioPanel = new JPanel(null);
		ioPanel.setBorder(BorderFactory.createTitledBorder("File operations"));
		ioPanel.setBounds(0, 90, 590, 150);
		
		
		
		panel.add(ioPanel);
		
		panel.repaint();
		return panel;
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
