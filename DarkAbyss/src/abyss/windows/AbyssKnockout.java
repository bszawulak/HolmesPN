package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import abyss.analyse.InvariantsTools;
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
public class AbyssKnockout extends JFrame implements ComponentListener {
	private static final long serialVersionUID = -9038958824842964847L;
	
	private JFrame ego;
	private JComboBox<String> transitionsCombo;
	private MauritiusMapPanel mmp;
	private int intX=0;
	private int intY=0;
	private JPanel mainPanel;
	private JPanel buttonPanel;
	private JPanel logMainPanel;
	private JScrollPane scroller;
	private MauritiusMapBT mmCurrentObject;
	
	private ArrayList<Integer> knockOutData = null;
	
	/**
	 * Konstruktor obiektu klasy AbyssKnockout
	 */
	public AbyssKnockout() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
			ego = this;
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Knockout analysis");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 700));
		setLocation(15, 15);
		
		//setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//setResizable(false);
		
		mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComboBoxData();
  	  	    }  
    	});
		
		addComponentListener(this);
		addWindowStateListener(new WindowAdapter() {
			public void windowStateChanged(WindowEvent e) {
				if(e.getNewState() == JFrame.MAXIMIZED_BOTH) {
					//ego.setExtendedState(JFrame.NORMAL);
					resizeComponents();
				}
			}
		});
	}



	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		//Panel wyboru opcji szukania
		buttonPanel = createUpperButtonPanel(0, 0, 900, 90);
		logMainPanel = createGraphPanel(0, 90, 900, 600);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
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
		panel.setBounds(x, y, width, height);
		
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
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				mmCurrentObject = generateMap();
				paintMap();
				
				//MauritiusMapBT kc = new MauritiusMapBT();
				//mmp.addMMBT(kc);
				//mmp.repaint();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		JButton showKnockoutButton = new JButton("Show knockout");
		showKnockoutButton.setBounds(posX+120, posY+30, 110, 30);
		showKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		showKnockoutButton.setIcon(Tools.getResIcon32("/icons/stateSim/aaa.png"));
		showKnockoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
				MauritiusMapBT infoMap = generateMap();
				if(infoMap != null) {
					AbyssNotepad notePad = new AbyssNotepad(900,600);
					notePad.setVisible(true);
					getKnockoutInfo(infoMap, notePad);
				}
				
				//mmp = new MauritiusMapPanel(kc);
				//mmp.addMMBT(null);
				//mmp.repaint();
			}
		});
		showKnockoutButton.setFocusPainted(false);
		panel.add(showKnockoutButton);
		
		return panel;
	}
	
	
	protected void getKnockoutInfo(MauritiusMapBT infoMap, AbyssNotepad notePad) {
		collectInfo(infoMap.getRoot());
		//knockOutData
	}



	private void collectInfo(BTNode root) {
		// TODO Auto-generated method stub
		
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
		JPanel gr_panel = new JPanel();
		gr_panel.setLayout(null);
		gr_panel.setBounds(x, y, width, height);
		
		mmp = new MauritiusMapPanel();
		
		intX = width - 10;
		intY = height - 25;
		
		scroller = new JScrollPane(mmp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setBounds(0, 0, intX, intY);
		gr_panel.add(scroller);
		
		@SuppressWarnings("unused")
		Dimension size = mmp.getSize();

		return gr_panel;
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
		mmp.addMMBT(mmCurrentObject);
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
		scroller.setBounds(0, 0, intX, intY);
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

	public void componentResized(ComponentEvent e) {
		resizeComponents();
	}
	public void componentHidden(ComponentEvent e) {} //unused
	public void componentMoved(ComponentEvent e) {} //unused
	public void componentShown(ComponentEvent e) {} //unused
	
}
