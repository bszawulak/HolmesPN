package holmes.windows.managers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.Serial;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.SPNdataVector;
import holmes.petrinet.data.SPNtransitionData;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;
import holmes.utilities.Tools;

/**
 * Okno ustawiania rodzaju i danych definiujących tranzycję w modelu SPN.
 */
public class HolmesSPNtransitionEditor extends JFrame {
	@Serial
	private static final long serialVersionUID = 3441899337352923949L;
	private JFrame ego;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private PetriNet pn;
	private JFrame parentWindow;
	private SPNtransitionData myData;
	private Transition transition;
	private boolean doNotUpdate = false;
	
	private JFormattedTextField STfunctionValueEdit;
	private JFormattedTextField IMpriorityValueEdit;
	private JFormattedTextField DTdelayValueEdit;
	private JFormattedTextField SCHstartValueEdit;
	private JFormattedTextField SCHrepValueEdit;
	private JFormattedTextField SCHendValueEdit;
	

	/**
	 * Konstruktor okna ustawień tranzycji w modelu SPN.
	 * @param boss JFrame - okno wywołujące
	 * @param data SPNtransitionData - dane SPN
	 * @param trans Transition - tranzycja
	 * @param xyLoc Point - lokalizacja kliknięcia
	 */
	public HolmesSPNtransitionEditor(JFrame boss, SPNtransitionData data, Transition trans, Point xyLoc) {
		setTitle(lang.getText("HSPNTEwin_entry001title"));
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00522exception")+" "+ex.getMessage(), "error", true);
		}
    	
    	this.ego = this;
		this.pn = overlord.getWorkspace().getProject();
		this.parentWindow = boss;
		this.myData = data;
		this.transition = trans;

    	initalizeComponents();
    	initiateListeners();
    	
    	setLocation(parentWindow.getLocation().x+100, parentWindow.getLocation().x+100);
    	
    	setVisible(true);
    	parentWindow.setEnabled(false);
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * Główna metoda budująca okno.
	 */
	private void initalizeComponents() {
		setLayout(new BorderLayout());
		setSize(new Dimension(405, 260));
		
		setResizable(false);
		setLayout(new BorderLayout());
		
		JPanel main = new JPanel(null);
		
		int posX = 10;
		int posY = 15;
		
		main.setBorder(BorderFactory.createTitledBorder(lang.getText("HSPNTEwin_entry002"))); //SPN transition data vector
		
		JLabel label0 = new JLabel(lang.getText("HSPNTEwin_entry003")); //SPN data vector
		label0.setBounds(posX, posY, 120, 20);
		main.add(label0);
		
		SPNdataVector frVector = pn.accessFiringRatesManager().getCurrentSPNdataVector();
		int frVectorIndex = pn.accessFiringRatesManager().selectedVector;
		
		JLabel label1 = new JLabel(frVectorIndex+" ("+frVector.getDescription()+")");
		label1.setBounds(posX+130, posY, 260, 20);
		main.add(label1);
		
		JLabel label2 = new JLabel(lang.getText("HSPNTEwin_entry004")); //Transition
		label2.setBounds(posX, posY+=25, 120, 20);
		main.add(label2);
		
		DefaultFormatter format = new DefaultFormatter();
		format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setBounds(posX+130, posY, 250, 20);
		nameField.setValue(transition.getName());
		nameField.setEditable(false);
		main.add(nameField);
		
		JLabel label3 = new JLabel(lang.getText("HSPNTEwin_entry005")); //Transition
		label3.setBounds(posX, posY+=25, 120, 20);
		main.add(label3);
		
		JComboBox<String> spnTypeCombo = new JComboBox<String>();
		spnTypeCombo.addItem(lang.getText("HSPNTEwin_entry006")); //Stochastic Transition
		spnTypeCombo.addItem(lang.getText("HSPNTEwin_entry007")); //Immediate Transition
		spnTypeCombo.addItem(lang.getText("HSPNTEwin_entry008")); //Deterministic Transition
		spnTypeCombo.addItem(lang.getText("HSPNTEwin_entry009")); //Scheduled Transition
		
		spnTypeCombo.setBounds(posX+130, posY, 250, 20);
		spnTypeCombo.addActionListener(actionEvent -> {
			@SuppressWarnings("unchecked")
			JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();

			handleSubTypeSelection(comboBox.getSelectedIndex());
		});

		JLabel label4 = new JLabel(lang.getText("HSPNTEwin_entry010")); //Function / value:
		label4.setBounds(posX, posY+=25, 120, 20);
		main.add(label4);
		
		STfunctionValueEdit = new JFormattedTextField(format);
		STfunctionValueEdit.setBounds(posX+130, posY, 250, 20);
		STfunctionValueEdit.setValue(myData.ST_function);
		STfunctionValueEdit.addPropertyChangeListener("value", e -> {
			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				myData.ST_function = field.getText();
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00523exception")+" "+ex.getMessage(), "error", true);
			}
		});
		main.add(STfunctionValueEdit);
		
		JLabel label5 = new JLabel(lang.getText("HSPNTEwin_entry011")); //Priority:
		label5.setBounds(posX, posY+=25, 120, 20);
		main.add(label5);
		
		IMpriorityValueEdit = new JFormattedTextField(format);
		IMpriorityValueEdit.setBounds(posX+130, posY, 80, 20);
		IMpriorityValueEdit.setValue(myData.IM_priority);
		IMpriorityValueEdit.addPropertyChangeListener("value", e -> {
			if(doNotUpdate)
				return;

			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				myData.IM_priority = Integer.parseInt(field.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(ego, lang.getText("HSPNTEwin_entry012"), lang.getText("error"),
						JOptionPane.ERROR_MESSAGE);
				doNotUpdate = true;
				IMpriorityValueEdit.setValue(myData.IM_priority);
				doNotUpdate = false;
			}
		});
		main.add(IMpriorityValueEdit);
		
		JLabel label6 = new JLabel(lang.getText("HSPNTEwin_entry013")); //Delay:
		label6.setBounds(posX, posY+=25, 120, 20);
		main.add(label6);
		
		DTdelayValueEdit = new JFormattedTextField(format);
		DTdelayValueEdit.setBounds(posX+130, posY, 80, 20);
		DTdelayValueEdit.setValue(myData.DET_delay);
		DTdelayValueEdit.addPropertyChangeListener("value", e -> {
			if(doNotUpdate)
				return;

			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				int priority = Integer.parseInt(field.getText());
				if(priority < 1)
					throw new Exception();

				myData.DET_delay = priority;
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(ego, lang.getText("HSPNTEwin_entry014"), lang.getText("error"),
						JOptionPane.ERROR_MESSAGE);
				doNotUpdate = true;
				DTdelayValueEdit.setValue(myData.DET_delay);
				doNotUpdate = false;
			}
		});
		main.add(DTdelayValueEdit);
		
		JLabel label7 = new JLabel(lang.getText("HSPNTEwin_entry015")); //Start / Repetitions / Stop:
		label7.setBounds(posX, posY+=25, 120, 20);
		main.add(label7);
		
		SCHstartValueEdit = new JFormattedTextField(format);
		SCHstartValueEdit.setBounds(posX+130, posY, 80, 20);
		SCHstartValueEdit.setValue(myData.SCH_start);
		SCHstartValueEdit.addPropertyChangeListener("value", e -> {
			if(doNotUpdate)
				return;

			JFormattedTextField field = (JFormattedTextField) e.getSource();
			String data = field.getText();
			try {
				int start = Integer.parseInt(data);
				if(start < 0)
					throw new Exception();

				myData.SCH_start = ""+start;
			} catch (Exception ex) {
				if(data.equals("start")) {
					myData.SCH_start = data;
				} else {
					JOptionPane.showMessageDialog(ego, lang.getText("LOGentry00524exception"), "Error",
							JOptionPane.ERROR_MESSAGE);
					doNotUpdate = true;
					SCHstartValueEdit.setValue(myData.SCH_start);
					doNotUpdate = false;
				}
			}
		});
		main.add(SCHstartValueEdit);
		
		JLabel label8 = new JLabel("/");
		label8.setBounds(posX+210, posY, 10, 20);
		main.add(label8);
		
		SCHrepValueEdit = new JFormattedTextField(format);
		SCHrepValueEdit.setBounds(posX+215, posY, 80, 20);
		SCHrepValueEdit.setValue(myData.SCH_rep);
		SCHrepValueEdit.addPropertyChangeListener("value", e -> {
			if(doNotUpdate)
				return;

			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				int priority = Integer.parseInt(field.getText());
				if(priority < 0)
					throw new Exception();

				myData.SCH_rep = priority;
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(ego, lang.getText("HSPNTEwin_entry016"), lang.getText("error"),
						JOptionPane.ERROR_MESSAGE);
				doNotUpdate = true;
				SCHrepValueEdit.setValue(myData.SCH_rep);
				doNotUpdate = false;
			}
		});
		main.add(SCHrepValueEdit);
		
		JLabel label9 = new JLabel("/");
		label9.setBounds(posX+295, posY, 10, 20);
		main.add(label9);
		
		SCHendValueEdit = new JFormattedTextField(format);
		SCHendValueEdit.setBounds(posX+300, posY, 80, 20);
		SCHendValueEdit.setValue(myData.SCH_end);
		SCHendValueEdit.addPropertyChangeListener("value", e -> {
			if(doNotUpdate)
				return;

			JFormattedTextField field = (JFormattedTextField) e.getSource();
			String data = field.getText();
			try {
				int end = Integer.parseInt(data);
				if(end < 0)
					throw new Exception();

				myData.SCH_end = ""+end;
			} catch (Exception ex) {
				if(data.equals("end")) {
					myData.SCH_end = data;
				} else {
					JOptionPane.showMessageDialog(ego, lang.getText("LOGentry00525exception"), lang.getText("error"),
							JOptionPane.ERROR_MESSAGE);
					doNotUpdate = true;
					SCHendValueEdit.setValue(myData.SCH_end);
					doNotUpdate = false;
				}
			}
		});
		main.add(SCHendValueEdit);

		//KONIECZNIE NA KOŃCU!!!
		main.add(spnTypeCombo);
		if(myData.sType == TransitionSPNExtension.StochaticsType.ST)
			spnTypeCombo.setSelectedIndex(0);
		else if(myData.sType == TransitionSPNExtension.StochaticsType.IM)
			spnTypeCombo.setSelectedIndex(1);
		else if(myData.sType == TransitionSPNExtension.StochaticsType.DT)
			spnTypeCombo.setSelectedIndex(2);
		else
			spnTypeCombo.setSelectedIndex(3);
		

		JButton saveAndExit = new JButton(lang.getText("HSPNTEwin_entry017")); //Change & exit
		saveAndExit.setIcon(Tools.getResIcon16("/icons/fRatesManager/acceptChange.png"));
		saveAndExit.setMargin(new Insets(0, 0, 0, 0));
		saveAndExit.setFocusPainted(false);
		saveAndExit.setBounds(posX+115, posY+=25, 160, 32);
		saveAndExit.setToolTipText(lang.getText("HSPNTEwin_entry017t"));
		saveAndExit.addActionListener(actionEvent -> ego.dispatchEvent(new WindowEvent(ego, WindowEvent.WINDOW_CLOSING)));
		main.add(saveAndExit);
		
		add(main, BorderLayout.CENTER);
	}

	/**
	 * Aktywuje lub deaktywuje pola okna w zależności od typu SPN dla tranzycji.
	 * @param selectedIndex int - wybrany tryb
	 */
	protected void handleSubTypeSelection(int selectedIndex) {
		if(selectedIndex == 0) {
			STfunctionValueEdit.setEnabled(true);
			IMpriorityValueEdit.setEnabled(false);
			DTdelayValueEdit.setEnabled(false);
			SCHstartValueEdit.setEnabled(false);
			SCHrepValueEdit.setEnabled(false);
			SCHendValueEdit.setEnabled(false);
			
			myData.sType = TransitionSPNExtension.StochaticsType.ST;
		} else if(selectedIndex == 1) {
			STfunctionValueEdit.setEnabled(false);
			IMpriorityValueEdit.setEnabled(true);
			DTdelayValueEdit.setEnabled(false);
			SCHstartValueEdit.setEnabled(false);
			SCHrepValueEdit.setEnabled(false);
			SCHendValueEdit.setEnabled(false);
			
			myData.sType = TransitionSPNExtension.StochaticsType.IM;
		}  else if(selectedIndex == 2) {
			STfunctionValueEdit.setEnabled(false);
			IMpriorityValueEdit.setEnabled(false);
			DTdelayValueEdit.setEnabled(true);
			SCHstartValueEdit.setEnabled(false);
			SCHrepValueEdit.setEnabled(false);
			SCHendValueEdit.setEnabled(false);
			
			myData.sType = TransitionSPNExtension.StochaticsType.DT;
		}  else {
			STfunctionValueEdit.setEnabled(false);
			IMpriorityValueEdit.setEnabled(false);
			DTdelayValueEdit.setEnabled(false);
			SCHstartValueEdit.setEnabled(true);
			SCHrepValueEdit.setEnabled(true);
			SCHendValueEdit.setEnabled(true);
			
			myData.sType = TransitionSPNExtension.StochaticsType.SchT;
		}
	}

	/**
	 * HAIL SITHIS!
	 */
	private void initiateListeners() {
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentWindow.setEnabled(true);
		    }
		});
	}
}
