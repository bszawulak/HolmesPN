package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import abyss.analyse.InvariantsCalculator;
import abyss.analyse.MCSCalculator;
import abyss.darkgui.GUIManager;
import abyss.math.Transition;
import abyss.utilities.Tools;

/**
 * Klasa tworząca okno narzedzi generowania i analizy zbiorów MCS.
 * @author MR
 *
 */
public class AbyssMCS extends JFrame {
	private ArrayList<Transition> transitions;
	private int maxCutSize = 0;
	private int maximumMCS = 0;
	private boolean generateAll = true;
	
	private MCSCalculator mcsGenerator = null;
	private boolean isMCSGeneratorWorking = false;
	
	private JComboBox<String> transitionsCombo;
	private JSpinner mcsSpinner;
	private JTextArea logField;

	/**
	 * Konstruktor obiektu klasy AbyssMCS.
	 */
	public AbyssMCS() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
			transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Minimal Cutting Sets generator");
		
		if(transitions != null && transitions.size()>1) {
			maxCutSize = 2;
			maximumMCS = transitions.size();
		} else {
			maxCutSize = 0;
			maximumMCS = 0;
		}
		
		setLayout(new BorderLayout());
		setSize(new Dimension(850, 650));
		setLocation(50, 50);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		
		JPanel mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//parentFrame.setEnabled(true);
		    }
		});
		
		addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComboBoxData();
  	  	    }  
    	});
	}

	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel okna
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);  /**  ╯°□°）╯︵  ┻━━━┻   */
		
		//Panel wyboru opcji szukania
		JPanel buttonPanel = createUpperButtonPanel(0, 0, 844, 110);
		JPanel logMainPanel = createMainPanel(0, 110, 844, 500);
		//JPanel leftButtonPanel = createLeftButtonPanel(600, 150, 100, 400);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
		//panel.add(leftButtonPanel);
		panel.repaint();
		return panel;
	}
	
	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		JLabel mcsLabel1 = new JLabel("Obj. reaction:");
		mcsLabel1.setBounds(posX, posY, 80, 20);
		panel.add(mcsLabel1);
		
		String[] dataT = { "---" };
		transitionsCombo = new JComboBox<String>(dataT);
		transitionsCombo.setBounds(posX+90, posY, 400, 20);
		transitionsCombo.setSelectedIndex(0);
		transitionsCombo.setMaximumRowCount(6);
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		if(transitions != null && transitions.size()>0) {
			for(int t=0; t < transitions.size(); t++) {
				transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
			}
		} 
		panel.add(transitionsCombo);
		
		JLabel mcsLabel2 = new JLabel("Max. |CutSet|:");
		mcsLabel2.setBounds(posX, posY+25, 80, 20);
		panel.add(mcsLabel2);
        
		SpinnerModel mcsSpinnerModel = new SpinnerNumberModel(maxCutSize, 0, maximumMCS, 1);
		mcsSpinner = new JSpinner(mcsSpinnerModel);
		mcsSpinner.setBounds(posX+90, posY+25, 60, 20);
		mcsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				maxCutSize = (int) spinner.getValue();
			}
		});
		panel.add(mcsSpinner);
		
		JCheckBox allCheckBox = new JCheckBox("Compute all MCS", true);
		allCheckBox.setBounds(posX+160, posY+25, 140, 20);
		allCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					generateAll = true;
				} else {
					generateAll = false;
				}
			}
		});
		panel.add(allCheckBox);
		
		//Generowanie zbiorów
		JButton generateButton = new JButton();
		generateButton.setText("<html>Generate<br />MCS</html>");
		generateButton.setBounds(posX, posY+55, 110, 32);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/mcsWindow/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				launchMCSanalysis();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);
		
		JButton loadButton = new JButton();
		loadButton.setText("<html>Load one<br />objR MCS</html>");
		loadButton.setBounds(posX+120, posY+55, 110, 32);
		loadButton.setMargin(new Insets(0, 0, 0, 0));
		loadButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/loadMCS.png"));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		loadButton.setFocusPainted(false);
		panel.add(loadButton);
		
		JButton loadAllButton = new JButton();
		loadAllButton.setText("<html>Load all<br />MCS</html>");
		loadAllButton.setBounds(posX+240, posY+55, 110, 32);
		loadAllButton.setMargin(new Insets(0, 0, 0, 0));
		loadAllButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/loadAllMCS.png"));
		loadAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		loadAllButton.setFocusPainted(false);
		panel.add(loadAllButton);
		
		JButton saveButton = new JButton();
		saveButton.setText("<html>Save one<br />objR MCS</html>");
		saveButton.setBounds(posX+360, posY+55, 110, 32);
		saveButton.setMargin(new Insets(0, 0, 0, 0));
		saveButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/saveMCS.png"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		saveButton.setFocusPainted(false);
		panel.add(saveButton);
		
		
		// SETS OPERATIONS
		
		JButton addToButton = new JButton();
		addToButton.setText("Add");
		addToButton.setBounds(posX+500, posY, 65, 20);
		addToButton.setMargin(new Insets(0, 0, 0, 0));
		addToButton.setIcon(Tools.getResIcon16("/icons/mcsWindow/add.png"));
		addToButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		addToButton.setFocusPainted(false);
		panel.add(addToButton);
		
		JButton removeButton = new JButton();
		removeButton.setText("Rem.");
		removeButton.setBounds(posX+575, posY, 65, 20);
		removeButton.setMargin(new Insets(0, 0, 0, 0));
		removeButton.setIcon(Tools.getResIcon16("/icons/mcsWindow/remove.png"));
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		removeButton.setFocusPainted(false);
		panel.add(removeButton);
		
		JButton clearButton = new JButton();
		clearButton.setText("Clear");
		clearButton.setBounds(posX+650, posY, 65, 20);
		clearButton.setMargin(new Insets(0, 0, 0, 0));
		clearButton.setIcon(Tools.getResIcon16("/icons/mcsWindow/clear.png"));
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		clearButton.setFocusPainted(false);
		panel.add(clearButton);
		
		JTextArea logField = new JTextArea();
		logField.setLineWrap(true);
		logField.setEditable(false);
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField),BorderLayout.CENTER);
        logFieldPanel.setBounds(posX+500, posY+25, 220, 70);
        panel.add(logFieldPanel);
        
        
        JButton calcAllButton = new JButton();
        calcAllButton.setText("<html>Comp.<br />select.<br />MCSs</html>");
        calcAllButton.setBounds(posX+725, posY+30, 100, 60);
        calcAllButton.setMargin(new Insets(0, 0, 0, 0));
        calcAllButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/computeSet.png"));
        calcAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
        calcAllButton.setFocusPainted(false);
		panel.add(calcAllButton);
		
		return panel;
	}

	/**
	 * Metoda tworząca centralny panel okna MCS.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createMainPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.setBounds(x, y, width, height);
		
		JPanel upperButtons = createSubButtonPanel(2, 2, 844-4, 90);
		panel.add(upperButtons);
		
		
		logField = new JTextArea();
		logField.setLineWrap(true);
		logField.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logField.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField),BorderLayout.CENTER);
        logFieldPanel.setBounds(2, 90, 840, height-94);
        panel.add(logFieldPanel);
		
		return panel;
	}
	
	/**
	 * Metoda tworząca podpanel przycisków.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createSubButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Computed MCS options"));
		panel.setBounds(x, y, width, height); // -6pikseli do rozmiaru <---> (650)

		return panel;
	}
	
	protected void launchMCSanalysis() {
		if(isMCSGeneratorWorking == true) {
			JOptionPane.showMessageDialog(null, "MCS calculation already in progress.", 
					"MCS generator working", JOptionPane.WARNING_MESSAGE);
		} else {
			ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			if(transitions == null || transitions.size() < 2) {
				JOptionPane.showMessageDialog(null, "Not enough transitions in net. Operation cannot start.", 
						"Warning", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
			if(invariants == null || invariants.size() < 1) {
				JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
						"Warning", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			int selectionObjR = transitionsCombo.getSelectedIndex();
			if(selectionObjR == 0) {
				JOptionPane.showMessageDialog(null, "Please select objective reaction (objR).", 
						"Warning", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			int minCutSize = (int) mcsSpinner.getValue();
			if(minCutSize == 0) {
				JOptionPane.showMessageDialog(null, "MCSs maximal cardinality too low!", 
						"Warning", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			mcsGenerator = new MCSCalculator(selectionObjR, invariants, transitions, minCutSize, this);
			Thread myThread = new Thread(mcsGenerator);
			setGeneratorStatus(true);
			myThread.start();
		}
	}

	/**
	 * Metoda ustawia status generatora MCS - działa / nie działa.
	 * @param status boolean - true, jeśli włączony w swoim wątku
	 */
	public void setGeneratorStatus(boolean status) {
		isMCSGeneratorWorking = status;
	}

	/**
	 * Metoda ustawia odpowiednie wartości komponentów okna za każdym razem gdy okno jest aktywowane.
	 */
	protected void fillComboBoxData() {
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		int selection = transitionsCombo.getSelectedIndex();
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		for(int t=0; t < transitions.size(); t++) {
			transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
		}
		
		if(selection < transitions.size()+1)
			transitionsCombo.setSelectedIndex(selection);
		
		int minimumValue = 0;
		
		if(transitions != null && transitions.size() > 1) {
			maxCutSize = 2;
			maximumMCS = transitions.size();
			minimumValue = 1;
		} else {
			maxCutSize = 0;
			maximumMCS = 0;
			minimumValue = 0;
		}
		
		SpinnerModel mcsSpinnerModel = new SpinnerNumberModel(maxCutSize, minimumValue, maximumMCS, 1);
		mcsSpinner.setModel(mcsSpinnerModel);
	}

	/**
	 * Metoda zwraca obiekt komponentu 'notatnika' w podoknie.
	 * @return JTextAres - obiekt logów
	 */
	public JTextArea accessLogField() {
		return logField;
	}
	
	/**
	 * Metoda resetuje połączenie z wątkiem generatora.
	 */
	public void resetMCSGenerator() {
		mcsGenerator = null;
		setGeneratorStatus(false);
	}	
}
