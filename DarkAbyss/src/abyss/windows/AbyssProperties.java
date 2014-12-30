package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import abyss.analyzer.NetPropertiesAnalyzer;
import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa odpowiedzialna za wyświetlanie okna informacji o sieci, jej właściwościach, itd.
 * @author MR
 *
 */
public class AbyssProperties extends JFrame {
	private static final long serialVersionUID = -4382182770445745847L;
	private JFrame ego;
	
	//Wiemy wszystko o wszystkim:
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<ArrayList<Integer>> invariantsMatrix;
	
	//komponenty do ustawienia:
	JLabel label_netName;
	JLabel label_nodesNumber;
	JLabel label_transitionsNumber;
	JLabel label_placesNumber;
	JLabel label_arcNumber;
	JLabel label_invNumber;
	
	JPanel staticPropertiesPanel; //panel właściwości
	
	/**
	 * Konstruktor domyślny okna klasy AbyssProperties.
	 */
	public AbyssProperties() {
		ego = this;
		//this.parentFrame = parent;
		this.setTitle("Petri net general information and properties");
		//parentFrame.setEnabled(false);
		
		setLayout(new BorderLayout());
		setSize(new Dimension(650, 500));
		setLocation(50, 50);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		
		
		JPanel main = createMainPanel();
		JPanel buttonsPanel = createButtonsPanel();
		
		add(main, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.EAST);

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//parentFrame.setEnabled(true);
		    }
		});
		
		//setLocationRelativeTo(null);
		setVisible(false);
		initiateListeners();
	}
	
	/**
	 * Metoda ta odświeża okno o najnowsze informacje na temat załadowanej sieci.
	 */
	private void fillData() {
		//PetriNetData data = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getData();
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		
		label_netName.setText(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getName()+"");
		label_nodesNumber.setText(nodes.size()+"");
		label_transitionsNumber.setText(transitions.size()+"");
		label_placesNumber.setText(places.size()+"");
		label_arcNumber.setText(arcs.size()+"");
		if(invariantsMatrix == null)
			label_invNumber.setText(0+"");
		else
			label_invNumber.setText(invariantsMatrix.size()+"");
		
		fillStaticProperties();
	}
	
	/**
	 * Absolute positioning. Of absolute everything here. 
	 * Nie obchodzi mnie, co o tym myślisz. Idź w layout i nie wracaj. ┌∩┐(◣_◢)┌∩┐
	 * @return JPanel - panel z danymi
	 * @author MR - tak się kiedyś, dzieci, programowało. Tak powstał Unix. I Linux. Więc mordy w kubeł.
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);  /** (ノಠ益ಠ)ノ彡┻━━━┻ |   */
		panel.setBorder(BorderFactory.createTitledBorder("General Petri net informations:"));
		
		int currentXAxis = 10;
		int currentYAxis = 15;
		int spacing = 20;
		int numberLabelWidth = 70;
		
		//Poniższe idą od lewego górnego rogu panelu
		//JLabel label1 = new JLabel("General Petri net informations:");
		//label1.setBounds(currentXAxis, currentYAxis, 200, 20);
		//panel.add(label1);
		//NET NAME:
		JLabel label1 = new JLabel("Project name:");
		label1.setBounds(currentXAxis, currentYAxis, 100, 20);
		panel.add(label1);
		label_netName = new JLabel("N/A");
		label_netName.setBounds(currentXAxis+label1.getWidth()+10, label1.getLocation().y, 400, 20);
		panel.add(label_netName);
		//NET NODES:
		JLabel label2 = new JLabel("Nodes:");
		label2.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label2);
		label_nodesNumber = new JLabel("N/A");
		label_nodesNumber.setBounds(currentXAxis+label2.getWidth()+10, label2.getLocation().y, numberLabelWidth, 20);
		panel.add(label_nodesNumber);
		//NET TRANSITION:
		JLabel label3 = new JLabel("Transitions:");
		label3.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label3);
		label_transitionsNumber = new JLabel("N/A");
		label_transitionsNumber.setBounds(currentXAxis+label3.getWidth()+10, label3.getLocation().y, numberLabelWidth, 20);
		panel.add(label_transitionsNumber);
		//NET PLACES:
		JLabel label4 = new JLabel("Places:");
		label4.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label4);
		label_placesNumber = new JLabel("N/A");
		label_placesNumber.setBounds(currentXAxis+label4.getWidth()+10, label4.getLocation().y, numberLabelWidth, 20);
		panel.add(label_placesNumber);
		//NET PLACES:
		JLabel label5 = new JLabel("Arcs:");
		label5.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label5);
		label_arcNumber = new JLabel("N/A");
		label_arcNumber.setBounds(currentXAxis+label5.getWidth()+10, label5.getLocation().y, numberLabelWidth, 20);
		panel.add(label_arcNumber);
		//NET INVARIANTS:
		JLabel label6 = new JLabel("Invariants:");
		label6.setBounds(currentXAxis, currentYAxis+=spacing, 100, 20);
		panel.add(label6);
		label_invNumber = new JLabel("N/A");
		label_invNumber.setBounds(currentXAxis+label6.getWidth()+10, label6.getLocation().y, numberLabelWidth, 20);
		panel.add(label_invNumber);
		
		//JLabel label11 = new JLabel("Net static properties:");
		//label11.setBounds(label1.getLocation().x+400, label1.getLocation().y, 200, 20);
		//panel.add(label11);
		//Panel właściwości
		staticPropertiesPanel = new JPanel(new FlowLayout());
		staticPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Net static properties:"));
		staticPropertiesPanel.setBounds(label2.getLocation().x+200, label2.getLocation().y, 300, 100);
		panel.add(staticPropertiesPanel);
		
		panel.repaint();
		return panel;
	}
	
	/**
	 * Metoda odpowiedzialna za wyświetlenie (w formie przycisków) właściwości statycznych
	 * sieci. Celem ich obliczenia wywoływany jest obiekt klasy NetPropertiesAnalyzer.
	 */
	private void fillStaticProperties() {
		//JPanel rowPanel = new JPanel();
		//rowPanel.setLayout(new BoxLayout(rowPanel,BoxLayout.X_AXIS));
		//ArrayList<Object> row = new ArrayList<Object>();
		staticPropertiesPanel.removeAll();
		staticPropertiesPanel.revalidate();
		
		NetPropertiesAnalyzer analyzer = new NetPropertiesAnalyzer();
		ArrayList<ArrayList<Object>> prop = analyzer.propAnalyze();
		
		for(ArrayList<Object> pr : prop) { //for each property
			JButton pButton = new JButton();
			pButton.setPreferredSize(new Dimension(66,20));
			if (pr.size() < 2) {
				pButton.setBackground(Color.GRAY);
			} else {
				if ((Boolean) pr.get(1) == true) {
					pButton.setBackground(Color.green);
					pButton.setForeground(Color.WHITE);
				} else {
					pButton.setBackground(Color.red);
				}
				String[] dataTxt = (String[])pr.get(2);
				pButton.setToolTipText(dataTxt[0]);
				
				// anonimowy action listener przyjmujący zmienną non-final (⌐■_■)
				pButton.addActionListener(new ActionListener() {
				    private String[] yesWeCan;
				    public void actionPerformed(ActionEvent e) {
				    	JOptionPane.showMessageDialog(ego, 
				    			"Petri net meaning:\n"
				    			+yesWeCan[1]
				    			+"\n\nBiological interpretation:\n"
				    			+yesWeCan[2],
				    			yesWeCan[0],JOptionPane.INFORMATION_MESSAGE);
				    }
				    private ActionListener goForthMyMinions(String[] codeInjection){
				    	yesWeCan = codeInjection;
				        return this;
				    }
				}.goForthMyMinions(dataTxt)  );
				/*
				pButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						CustomDialog txt = new CustomDialog(ego, dataTxt[0], "");
					}
				});
				*/
			}
			pButton.setText(pr.get(0).toString());
			pButton.setVisible(true);
			staticPropertiesPanel.add(pButton);
		}
	}
	
	/**
	 * Metoda odpowiedzialna za tworzenia panelu bocznego z przyciskami okna.
	 * @return JPanel - panel boczny przycisków
	 */
	private JPanel createButtonsPanel() {
		int buttonX = 100;
		int buttonY = 40;
		int spaceY = 10;
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Options"));
		JPanel inPanel = new JPanel();
		inPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); //panel z odstępami
		inPanel.setLayout(new BoxLayout(inPanel, BoxLayout.Y_AXIS));
		
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.setMinimumSize(new Dimension(buttonX, buttonY)); 
		refreshButton.setMaximumSize(new Dimension(buttonX, buttonY));
		//refreshButton.setPreferredSize(new Dimension(80,40)); //BoxLayout ma to gdzieś, dlatego patrz wyżej
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		inPanel.add(refreshButton);
		inPanel.add(Box.createVerticalStrut(spaceY));
		 
		JButton staticPropButton = new JButton("Properties");
		staticPropButton.setMinimumSize(new Dimension(buttonX, buttonY)); 
		staticPropButton.setMaximumSize(new Dimension(buttonX, buttonY));
		inPanel.add(staticPropButton);
		inPanel.add(Box.createVerticalStrut(spaceY));
		
		JButton saveButton = new JButton("Save to file");
		saveButton.setMinimumSize(new Dimension(buttonX, buttonY)); 
		saveButton.setMaximumSize(new Dimension(buttonX, buttonY));
		inPanel.add(saveButton);
		inPanel.add(Box.createVerticalStrut(spaceY));
		
		panel.add(inPanel);
		return panel;
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna właściwości.
	 */
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillData();
  	  	    }  
    	});
    }
}
