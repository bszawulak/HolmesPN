package holmes.windows.ssim;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;

public class HolmesSimSetup extends JFrame {
	private GUIManager overlord;
	private JFrame parentWindow;
	
	public HolmesSimSetup(JFrame parent) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.parentWindow = parent;
		
		initializeComponents();
		initiateListeners();
	}
	
	/**
	 * Metoda pomocnica konstuktora, odpowiada za utworzenie elementów graficznych okna.
	 */
	private void initializeComponents() {
		setVisible(false);
		setTitle("Simulator settings");
		setLocation(30,30);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(1000, 750));
		
		JPanel main = new JPanel(null); //główny panel okna
		add(main);
		
		main.add(createStdSimulatorSettingsPanel());
		main.add(createStochasticSimSettingsPanel());
		
		
		repaint();
	}
	
	private JPanel createStdSimulatorSettingsPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 500);
		
		int posX = 10;
		int posY = 10;
		
		return panel;
	}

	private JPanel createStochasticSimSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	//fillPlacesAndTransitionsData();
  	  	    }  
    	});
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    	parentWindow.setEnabled(true);
		    }
		});
    }
}
