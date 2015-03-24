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
import java.util.Set;

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
import abyss.files.io.MCSoperations;
import abyss.math.MinCutSetData;
import abyss.math.Transition;
import abyss.utilities.Tools;

/**
 * Klasa tworząca okno narzedzi generowania i analizy zbiorów MCS.
 * @author MR
 *
 */
public class AbyssMCS extends JFrame {
	private static final long serialVersionUID = -5765964470006303431L;
	private ArrayList<Transition> transitions;
	private int maxCutSize = 0;
	private int maximumMCS = 0;
	private boolean generateAll = true;
	
	private MCSCalculator mcsGenerator = null;
	private boolean isMCSGeneratorWorking = false;
	
	private JComboBox<String> transitionsCombo;
	private JComboBox<String> transitionsResultsCombo;
	private boolean listenerAllowed = true;
	private JSpinner mcsSpinner;
	private JTextArea logField;
	private JTextArea reactionSetsTextField;

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
				MCSoperations.loadSingleMCS();
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
				MCSoperations.loadAllMCS();
			}
		});
		loadAllButton.setFocusPainted(false);
		panel.add(loadAllButton);
		
		JButton saveAllButton = new JButton();
		saveAllButton.setText("<html>Save all<br />MCS</html>");
		saveAllButton.setBounds(posX+360, posY+55, 110, 32);
		saveAllButton.setMargin(new Insets(0, 0, 0, 0));
		saveAllButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/saveMCS.png"));
		saveAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				MCSoperations.saveAllMCS(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore());
				//fillData();
			}
		});
		saveAllButton.setFocusPainted(false);
		panel.add(saveAllButton);
		
		// SETS OPERATIONS
		
		JButton addToButton = new JButton();
		addToButton.setText("Add");
		addToButton.setBounds(posX+500, posY, 65, 20);
		addToButton.setMargin(new Insets(0, 0, 0, 0));
		addToButton.setIcon(Tools.getResIcon16("/icons/mcsWindow/add.png"));
		addToButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//reactionSetsTextField
				int selected = transitionsCombo.getSelectedIndex();
				if(selected == 0)
					return;
				selected--;
				String msg = "t"+selected+",";
				if(reactionSetsTextField.getText().contains(msg) == false)
					reactionSetsTextField.append(msg);
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
				int selected = transitionsCombo.getSelectedIndex();
				if(selected == 0)
					return;
				selected--;
				String msg = "t"+selected+",";
				String text = reactionSetsTextField.getText();
				text = text.replace(msg, "");
				reactionSetsTextField.setText(text);
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
				reactionSetsTextField.setText("");
			}
		});
		clearButton.setFocusPainted(false);
		panel.add(clearButton);
		
		reactionSetsTextField = new JTextArea();
		reactionSetsTextField.setLineWrap(true);
		reactionSetsTextField.setEditable(false);
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(reactionSetsTextField),BorderLayout.CENTER);
        logFieldPanel.setBounds(posX+500, posY+22, 215, 30);
        panel.add(logFieldPanel);
        
        JCheckBox allCheckBox = new JCheckBox("Compute all MCS", true);
		allCheckBox.setBounds(posX+690, posY+55, 140, 20);
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
        
        
        JButton calcAllButton = new JButton();
        calcAllButton.setText("<html>Comp.<br />select.<br />MCSs</html>");
        calcAllButton.setBounds(posX+725, posY, 100, 50);
        calcAllButton.setMargin(new Insets(0, 0, 0, 0));
        calcAllButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/computeSet.png"));
        calcAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				calculateAllAction();
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

		int posX = 10;
		int posY = 20;
		
		JLabel mcsLabel1 = new JLabel("ObjR MCSs:");
		mcsLabel1.setBounds(posX, posY, 80, 20);
		panel.add(mcsLabel1);
		
		String[] dataT = { "---" };
		transitionsResultsCombo = new JComboBox<String>(dataT);
		transitionsResultsCombo.setBounds(posX+90, posY, 400, 20);
		transitionsResultsCombo.setSelectedIndex(0);
		transitionsResultsCombo.setMaximumRowCount(6);
		transitionsResultsCombo.removeAllItems();
		transitionsResultsCombo.addItem("---");
		if(transitions != null && transitions.size()>0) {
			for(int t=0; t < transitions.size(); t++) {
				transitionsResultsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
			}
		}
		transitionsResultsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(listenerAllowed == false) 
					return;
				int selected = transitionsResultsCombo.getSelectedIndex();
				if(selected > 0) {
					showMCSData(selected-1);
				}
			}
			
		});
		panel.add(transitionsResultsCombo);
		
		JButton saveButton = new JButton();
		saveButton.setText("<html>Save this<br />objR MCS</html>");
		saveButton.setBounds(posX, posY+25, 110, 32);
		saveButton.setMargin(new Insets(0, 0, 0, 0));
		saveButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/saveMCS.png"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = transitionsResultsCombo.getSelectedIndex();
				if(selected == 0) {
					return;
				}
				selected--;
				String name = (String) transitionsResultsCombo.getSelectedItem();
				MCSoperations.saveSingleMCS(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore(), selected, name);
			}
		});
		saveButton.setFocusPainted(false);
		panel.add(saveButton);
		
		return panel;
	}

	/**
	 * Metoda odpowiedzialna za generowanie zbioru MCS i dodanie go do bazy programu.
	 */
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
			
			selectionObjR--;
			
			int minCutSize = (int) mcsSpinner.getValue();
			if(minCutSize == 0) {
				JOptionPane.showMessageDialog(null, "MCSs maximal cardinality too low!", 
						"Warning", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			int MCSdatacoreSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore().getSize();
			
			if(transitions.size() != MCSdatacoreSize) {
				if(MCSdatacoreSize == 0) {
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore().initiateMCS();
				} else {
					//co dalej?
					Object[] options = {"Yes", "No"};
					int decision = JOptionPane.showOptionDialog(null,
									"MCS list detected with different size than current cardinality of the transition set.\nClean old MCS list?",
									"Net change detected", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[1]);
					if (decision == 1) {
						return;
					} else {
						GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore().initiateMCS();
					}
				}
			}
			
			mcsGenerator = new MCSCalculator(selectionObjR, invariants, transitions, minCutSize, this, true);
			Thread myThread = new Thread(mcsGenerator);
			setGeneratorStatus(true);
			myThread.start();
		}
	}
	
	protected void calculateAllAction() {
		ArrayList<Integer> objReactions = new ArrayList<Integer>();
		if(generateAll) {
			int transSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
			
			for(int t=0; t<transSize; t++) {
				objReactions.add(t);
			}
		} else {
			String all = reactionSetsTextField.getText();
			
			if(all.length() > 0) {
				all = all.trim();
				String[] splittedSets = all.split(",");
				if(splittedSets.length > 0) {
					for(String s : splittedSets) {
						try {
							s = s.replace("t", "");
							int next = Integer.parseInt(s);
							objReactions.add(next);
						} catch (Exception e) {}
					}
					
					
				}
			}
		}
		
		launchMCSanalysis(objReactions);
	}
	
	protected void launchMCSanalysis(ArrayList<Integer> objReactions) {
		if(objReactions.size() == 0)
			return;
		
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

			
			int minCutSize = (int) mcsSpinner.getValue();
			if(minCutSize == 0) {
				JOptionPane.showMessageDialog(null, "MCSs maximal cardinality too low!", 
						"Warning", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			int MCSdatacoreSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore().getSize();
			
			if(transitions.size() != MCSdatacoreSize) {
				if(MCSdatacoreSize == 0) {
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore().initiateMCS();
				} else {
					//co dalej?
					Object[] options = {"Yes", "No"};
					int decision = JOptionPane.showOptionDialog(null,
									"MCS list detected with different size than current cardinality of the transition set.\nClean old MCS list?",
									"Net change detected", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[1]);
					if (decision == 1) {
						return;
					} else {
						GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore().initiateMCS();
					}
				}
			}
			
			for(int el : objReactions) {
				
				while(getGeneratorStatus() == true) {
					try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
				}
				
				logField.append("Starting calculations for reaction: "+el+"\n");
				mcsGenerator = new MCSCalculator(el, invariants, transitions, minCutSize, this, false);
				Thread myThread = new Thread(mcsGenerator);
				setGeneratorStatus(true);
				myThread.start();
			}
		}
	}
	
	/**
	 * Metoda wyświetla informacje i zbiory MCS dla wskazanej tranzycji.
	 * @param selected
	 */
	protected void showMCSData(int selected) {	
		MinCutSetData mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
		
		if(mcsd.getSize() == 0)
			return;
		
		ArrayList<Set<Integer>> dataVector = mcsd.getMCSlist(selected);
		
		if(dataVector == null)
			return;
		
		Transition objR = transitions.get(selected);
		logField.append("==========================================================\n");
		logField.append("Transition/objR: "+objR.getName()+"\n");
		logField.append("Minimal Cuttin Sets list size: "+dataVector.size()+"\n");
		
		int counter = 0;
		String msg = "";
		for(Set<Integer> set : dataVector) {
			logField.append("MSC#"+counter+" ");
			msg = "[";
			for(int el : set) {
				msg += el+", ";
			}
			msg += "]";
			msg = msg.replace(", ]", "]");
			logField.append(msg+"\n");
			counter++;
		}
		
		logField.append("==========================================================\n");
		logField.append("\n");
	}

	/**
	 * Metoda ustawia status generatora MCS - działa / nie działa.
	 * @param status boolean - true, jeśli włączony w swoim wątku
	 */
	public void setGeneratorStatus(boolean status) {
		isMCSGeneratorWorking = status;
	}

	/**
	 * Metoda zwraca stan generatora - ON/OFF.
	 * @return boolean - true, jeśli trwa generowanie
	 */
	public boolean getGeneratorStatus() {
		return isMCSGeneratorWorking;
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
		
		listenerAllowed = false; //aby nie wywoływać wyświetlania
		transitionsResultsCombo.removeAllItems();
		transitionsResultsCombo.addItem("---");
		for(int t=0; t < transitions.size(); t++) {
			transitionsResultsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
		}
		listenerAllowed = true;
		
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
