package abyss.windows;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatter;

import abyss.darkgui.GUIManager;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.utilities.Tools;

public class AbyssNodeInfo extends JFrame {
	private static final long serialVersionUID = 2651328691434039125L;
	private Place place;
	private Transition transition;
	private JPanel mainPanel;
	private JFrame parentFrame;
	private AbyssNodeInfoAction action = new AbyssNodeInfoAction(this);
	
	public AbyssNodeInfo(Place place, JFrame papa) {
		parentFrame = papa;
		this.place = place;
		
		initializeCommon();
		initializePlaceInfo();
	}
	
	public AbyssNodeInfo(Transition transition, JFrame papa) {
		parentFrame = papa;
		this.transition = transition;
		
		initializeCommon();
		initializeTransitionInfo();
	}
	
	private void initializeCommon() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		
		parentFrame.setEnabled(false);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
	}

	private void initializePlaceInfo() {
		this.setLocation(20, 20);
		setSize(new Dimension(500, 400));

		mainPanel = new JPanel(null);
		mainPanel.setBounds(0, 0, 500, 400);
		
		int mPanelX = 0;
		int mPanelY = 0;
		
		//panel informacji podstawowych
		JPanel infoPanel = new JPanel(null);
		infoPanel.setBounds(mPanelX, mPanelY, 490, 100);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Place general information:"));
		
		int infPanelX = 10;
		int infPanelY = 20;
		
		JLabel labelID = new JLabel("ID:");
		labelID.setBounds(infPanelX, infPanelY, 20, 20);
		infoPanel.add(labelID);
		
		int id = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().indexOf(place);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
		idTextBox.setEditable(false);
		infoPanel.add(idTextBox);
		
		JLabel labelName = new JLabel("Name:");
		labelName.setBounds(infPanelX+60, infPanelY, 40, 20);
		infoPanel.add(labelName);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(infPanelX+100, infPanelY);
		nameField.setSize(360, 20);
		nameField.setValue(place.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				
				action.changeName(place, newName);
				action.parentTableUpdate(parentFrame, newName);
			}
		});
		infoPanel.add(nameField);
		
		mainPanel.add(infoPanel);

		add(mainPanel);
	}
	
	private void initializeTransitionInfo() {
		this.setLocation(20, 20);
		setSize(new Dimension(640, 480));

		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		
		

		add(mainPanel);
		
	}
}
