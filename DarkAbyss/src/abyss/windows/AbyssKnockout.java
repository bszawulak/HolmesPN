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
 * @author Rince
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
		setLocation(50, 50);
		
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
					//resizeComponents();
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
		logMainPanel = createLogMainPanel(0, 90, 900, 600);
		
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
				generateMap();
				
				//MauritiusMapBT kc = new MauritiusMapBT();
				//mmp.addMMBT(kc);
				//mmp.repaint();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		JButton generate2Button = new JButton("CLEAN");
		generate2Button.setBounds(posX+120, posY+30, 110, 30);
		generate2Button.setMargin(new Insets(0, 0, 0, 0));
		generate2Button.setIcon(Tools.getResIcon32("/icons/stateSim/aaa.png"));
		generate2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//MauritiusMapBT kc = new MauritiusMapBT();
				
				//mmp = new MauritiusMapPanel(kc);
				mmp.addMMBT(null);
				mmp.repaint();
			}
		});
		generate2Button.setFocusPainted(false);
		panel.add(generate2Button);
		
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
	private JPanel createLogMainPanel(int x, int y, int width, int height) {
		JPanel gr_panel = new JPanel();
		gr_panel.setLayout(null);
		gr_panel.setBounds(x, y, width, height);
		
		mmp = new MauritiusMapPanel();
		
		intX = width-10;
		intY = height-25;
		
		scroller = new JScrollPane(mmp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setBounds(0, 0, intX, intY);
		gr_panel.add(scroller);
		
		@SuppressWarnings("unused")
		Dimension size = mmp.getSize();

		return gr_panel;
	}
	
	/**
	 * Metoda odpowiedzialna za wygenerowanie mapy.
	 */
	private void generateMap() {
		int selection = transitionsCombo.getSelectedIndex();
		if(selection == 0) {
			JOptionPane.showMessageDialog(null, "Please choose main reaction for Mauritius Map.", 
					"No selection", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		selection--;
		
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		if(invariants == null || invariants.size() < 1) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		MauritiusMapBT mm = new MauritiusMapBT(invariants, selection);
		mmp.addMMBT(mm);
		mmp.repaint();
	}
	
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
