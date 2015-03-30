package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

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

import abyss.darkgui.GUIManager;
import abyss.graphpanel.MauritiusMapPanel;
import abyss.math.MauritiusMapBT;
import abyss.math.MauritiusMapBT.BTNode;
import abyss.math.Transition;
import abyss.utilities.Tools;

/**
 * Klasa implementująca okno analizy poprzez mechanizm knockout oraz ścieżki Mauritiusa.:
 * 
 * "Petri net modelling of gene regulation of the Duchenne muscular dystrophy"
 * Stefanie Grunwald, Astrid Speer, Jorg Ackermann, Ina Koch
 * BioSystems, 2008, 92, pp.189-205
 * 
 * 
 * @author MR
 *
 */
public class AbyssKnockout extends JFrame {
	private static final long serialVersionUID = -9038958824842964847L;

	private JComboBox<String> transitionsCombo;
	private MauritiusMapPanel mmp;
	private int intX=0;
	private int intY=0;
	private JPanel mainPanel;
	private JPanel buttonPanel;
	private JPanel logMainPanel;
	private JScrollPane scroller;
	private MauritiusMapBT mmCurrentObject;
	
	private ArrayList<Integer> knockOutDataFailed = null;
	private ArrayList<Integer> knockOutDataObjR = null;
	
	/**
	 * Konstruktor obiektu klasy AbyssKnockout
	 */
	public AbyssKnockout() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Knockout analysis");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 700));
		setLocation(15, 15);
		
		mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComboBoxData();
  	  	    }  
    	});
		
		/*
		addComponentListener(this);
		addWindowStateListener(new WindowAdapter() {
			public void windowStateChanged(WindowEvent e) {
				if(e.getNewState() == JFrame.MAXIMIZED_BOTH) {
					//ego.setExtendedState(JFrame.NORMAL);
					//resizeComponents();
				}
			}
		});
		*/
	}



	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		//Panel wyboru opcji szukania
		buttonPanel = createUpperButtonPanel(0, 0, 900, 90);
		logMainPanel = createGraphPanel(0, 90, 900, 600);
		
		panel.add(buttonPanel, BorderLayout.NORTH);
		panel.add(logMainPanel, BorderLayout.CENTER);
		panel.repaint();
		return panel;
	}

	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Search options"));
		//panel.setBounds(x, y, width, height);
		panel.setLocation(x, y);
		panel.setPreferredSize(new Dimension(width, height));
		
		int posX = 10;
		int posY = 20;
		
		JLabel mcsLabel1 = new JLabel("Obj. reaction:");
		mcsLabel1.setBounds(posX, posY, 80, 20);
		panel.add(mcsLabel1);
		
		String[] dataT = { "---" };
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
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
		
		JButton generateButton = new JButton("Generate");
		generateButton.setBounds(posX, posY+30, 110, 30);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				mmCurrentObject = generateMap();
				paintMap();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		JButton showKnockoutButton = new JButton("Show...");
		showKnockoutButton.setBounds(posX+120, posY+30, 110, 30);
		showKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		showKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/showNotepad.png"));
		showKnockoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
				MauritiusMapBT infoMap = generateMap();
				if(infoMap != null) {
					AbyssNotepad notePad = new AbyssNotepad(900,600);
					notePad.setVisible(true);
					getKnockoutInfo(infoMap, notePad);
				}
			}
		});
		showKnockoutButton.setFocusPainted(false);
		panel.add(showKnockoutButton);
		
		JButton toNetKnockoutButton = new JButton("Show Net");
		toNetKnockoutButton.setBounds(posX+240, posY+30, 110, 30);
		toNetKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		toNetKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/sendToNet.png"));
		toNetKnockoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
				MauritiusMapBT infoMap = generateMap();
				if(infoMap != null) {
					getKnockoutInfoToNet(infoMap);
				}
			}
		});
		toNetKnockoutButton.setFocusPainted(false);
		panel.add(toNetKnockoutButton);
		
		JCheckBox shortTextCheckBox = new JCheckBox("Show full names");
		shortTextCheckBox.setBounds(posX+490, posY, 130, 20);
		shortTextCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					mmp.setFullName(true);
				} else {
					mmp.setFullName(false);
				}
				mmp.repaint();
			}
		});
		shortTextCheckBox.setSelected(true);
		panel.add(shortTextCheckBox);
		
		return panel;
	}
	
	/**
	 * Metoda tworząca główmu panel mapy.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	private JPanel createGraphPanel(int x, int y, int width, int height) {
		JPanel gr_panel = new JPanel(new BorderLayout());
		//gr_panel.setBounds(x, y, width, height);
		
		gr_panel.setLocation(x, y);
		gr_panel.setPreferredSize(new Dimension(width, height));
		
		mmp = new MauritiusMapPanel();
		
		intX = width - 10;
		intY = height - 25;
		
		scroller = new JScrollPane(mmp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		gr_panel.add(scroller, BorderLayout.CENTER);

		return gr_panel;
	}

	/**
	 * Metoda wyświetla w notatniku dwa zbiory: zbiór tranzycji o tym samym stopniu występowania w inwariantach
	 * co objectiveR, oraz zbiór reakcji które są zależne od objR.
	 * @param infoMap MauritiusMapBT - obiekt danych mapy
	 * @param notePad AbyssNotepad - obiekt notatnika dla wyników
	 */
	protected void getKnockoutInfo(MauritiusMapBT infoMap, AbyssNotepad notePad) {
		ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(infoMap);
		
		//int noteValue = infoMap.getRoot().transFrequency;
		//knockOutDataFailed = new ArrayList<Integer>();
		//knockOutDataObjR = new ArrayList<Integer>();
		//collectInfo(infoMap.getRoot(), noteValue);
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		notePad.addTextLineNL("Reaction knocked out: "+infoMap.getRoot().transName, "text");
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Reaction common maximum set: ", "text");
		Collections.sort(dataMatrix.get(0));
		for(int element : dataMatrix.get(0)) {
			notePad.addTextLineNL("["+element+"] : "+transitions.get(element).getName(), "text");
		}
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Chain reaction fail cascade: ", "text");
		
		Collections.sort(dataMatrix.get(1));
		for(int element : dataMatrix.get(1)) {
			notePad.addTextLineNL("["+element+"] : "+transitions.get(element).getName(), "text");
		}
		//knockOutData
	}
	
	protected void getKnockoutInfoToNet(MauritiusMapBT infoMap) {
		ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(infoMap);
		
		try {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().setColorClusterToNeutral();

			Transition trans_TMP;// = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(0);
			
			for(int id : dataMatrix.get(0)) {
				trans_TMP= GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(id);
				trans_TMP.setColorWithNumber(true, Color.black, false, -1);
			}
			for(int id : dataMatrix.get(1)) {
				trans_TMP= GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(id);
				trans_TMP.setColorWithNumber(true, Color.blue, false, -1);
			}
			
			int rootID = infoMap.getRoot().transLocation;
			trans_TMP= GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(rootID);
			trans_TMP.setColorWithNumber(true, Color.red, false, -1);
			
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Metoda uruchamia przeglądanie mapy a następnie agreguje wynik do obiektu wyjściowego.
	 * @param infoMap MauritiusMapBT - obiekt mapu
	 * @return ArrayList[ArrayList[Integer]] - pierszy zbiór .get(0) to reakcje zależne od objR, drugi zbiór .get(1)
	 * 		to reakcje o tej samej frekwencji co objR
	 */
	private ArrayList<ArrayList<Integer>> collectMapData(MauritiusMapBT infoMap) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		
		int noteValue = infoMap.getRoot().transFrequency;
		knockOutDataFailed = new ArrayList<Integer>();
		knockOutDataObjR = new ArrayList<Integer>();
		collectInfo(infoMap.getRoot(), noteValue);
		
		ArrayList<Integer> set1 = new ArrayList<Integer>(knockOutDataFailed);
		ArrayList<Integer> set2 = new ArrayList<Integer>(knockOutDataObjR);
		result.add(set1);
		result.add(set2);
		return result;
	}

	/**
	 * Rekurencyjna metoda przeszukująca mapę i tworząca zbiory reakcji zależnych i niezależnych od
	 * objR (korzeń drzewa)
	 * @param node BTNode - węzeł drzewa
	 * @param startSetValue int - frequency dla objR
	 */
	private void collectInfo(BTNode node, int startSetValue) {
		int freq = node.transFrequency;
		int transID = node.transLocation;
		if(freq == startSetValue) {
			if(knockOutDataObjR.contains(transID) == false) {
				knockOutDataObjR.add(transID);
			}
		} else {
			if(knockOutDataFailed.contains(transID) == false) {
				knockOutDataFailed.add(transID);
			}
		}
		
		if(node.rightChild != null) {
			collectInfo(node.rightChild, startSetValue);
    	}
    	
    	if(node.leftChild != null) {
    		collectInfo(node.leftChild, startSetValue);
    	}
	}

	/**
	 * Metoda odpowiedzialna za wygenerowanie mapy.
	 * @return MauritiusMapBT - obiekt mapy
	 */
	private MauritiusMapBT generateMap() {
		int selection = transitionsCombo.getSelectedIndex();
		if(selection == 0) {
			JOptionPane.showMessageDialog(null, "Please choose main reaction for Mauritius Map.", 
					"No selection", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		
		selection--;
		
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		if(invariants == null || invariants.size() < 1) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		MauritiusMapBT mm = new MauritiusMapBT(invariants, selection);
		return mm;
	}
	
	/**
	 * Metoda uruchamiania przerysowanie wyliczonej mapy.
	 */
	private void paintMap() {
		mmp.addNewMap(mmCurrentObject);
		mmp.repaint();
	}
	
	/**
	 * Metoda odpowiedzialna za dopasowywanie elementów okna.
	 */
	protected void resizeComponents() {
		buttonPanel.setBounds(0, 0, mainPanel.getWidth(), 90);
		logMainPanel.setBounds(0, 90, mainPanel.getWidth(), mainPanel.getHeight()-90);
		
		intX = logMainPanel.getWidth();
		intY = logMainPanel.getHeight();
		//scroller.setBounds(0, 0, intX, intY);
		scroller.setPreferredSize(new Dimension(intX, intY));
	}
	
	/**
	 * Metoda ustawia odpowiednie wartości komponentów okna za każdym razem gdy okno jest aktywowane.
	 */
	protected void fillComboBoxData() {
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		int selection = transitionsCombo.getSelectedIndex();
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		for(int t=0; t < transitions.size(); t++) {
			transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
		}
		if(selection < transitions.size()+1)
			transitionsCombo.setSelectedIndex(selection);
		
	}
}