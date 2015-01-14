package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import abyss.darkgui.GUIManager;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.StateSimulator;
import abyss.utilities.Tools;

public class AbyssStateSimulator extends JFrame {
	private static final long serialVersionUID = -5478185512282936410L;
	private NetSimulator sim;
	private JComboBox<String> placesCombo = null;
	private JComboBox<String> transitionsCombo = null;
	private boolean listenerAllowed = false;
	private StateSimulator ssim;
	
	ArrayList<ArrayList<Integer>> resultMatrix;
	
	public AbyssStateSimulator() {
		sim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();
		
		ssim = new StateSimulator();
		setVisible(false);
		setTitle("State Simulator");
		setLocation(30,30);
		
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}

		setSize(new Dimension(800, 600));
		
		JPanel main = new JPanel(null); //główny panel okna
		add(main);
		
		JPanel toolPanel = crateToolsPanel(); //panel opcji i przycisków
		main.add(toolPanel);
		
		JPanel chartPanel = new JPanel(); //panel wykresów
		chartPanel.setBorder(BorderFactory.createTitledBorder("Charts"));
		chartPanel.setBounds(0, 150, 800, 450);
		chartPanel.add(createChart());
		
		main.add(chartPanel);
		initiateListeners(); //all hail Sithis
		repaint();
	}
	
	/**
	 * Metoda tworzy panel opcji okna symulatora stanów sieci.
	 * @return JPanel - panel
	 */
	private JPanel crateToolsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Tools"));
		result.setBounds(0, 0, 800, 150);
		
		int posX = 10;
		int posY = 15;
		
		JLabel label1 = new JLabel("Places:");
		label1.setBounds(posX, posY, 70, 20);
		result.add(label1);
		
		String[] dataP = { "---" };
		placesCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		placesCombo.setLocation(posX + 75, posY+2);
		placesCombo.setSize(400, 20);
		placesCombo.setSelectedIndex(0);
		placesCombo.setMaximumRowCount(6);
		placesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(listenerAllowed == false) 
					return;
				int selected = placesCombo.getSelectedIndex();
				if(selected > 0) {
					listenerAllowed = false;
					//transitionsCombo.setSelectedIndex(0);
					listenerAllowed = true;
					//centerOnElement("place", selected -1, null);
				}
			}
		});
		posY += 25;
		result.add(placesCombo);
		
		JButton acqDataButton = new JButton("Acquire data");
		acqDataButton.setBounds(posX, posY, 150, 32);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//showFound("prev");
			}
		});
		result.add(acqDataButton);
		
		JButton addPlaceButton = new JButton("Add place to chart");
		addPlaceButton.setBounds(posX+160, posY, 150, 32);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		addPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ssim.initiateSim(NetType.BASIC, true);
				ssim.simulateNet(100);
			}
		});
		result.add(addPlaceButton);
		
		return result;
	}

	/**
	 * Metoda wypełnia okno danymi
	 */
	private void fillData() {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places== null || places.size() == 0)
			return;
		
		placesCombo.removeAllItems();
		placesCombo.addItem("---");
		for(int p=0; p < places.size(); p++) {
			placesCombo.addItem("p"+(p)+"."+places.get(p).getName());
		}
	}
	
	private JPanel createChart() {
		JFreeChart lineChart = ChartFactory.createLineChart(
		         "TITLE", "Years", "Number of Schools",
		         createDataset(), PlotOrientation.VERTICAL, true, true, false);
		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 750, 350 ) );
		
		JPanel result = new JPanel();
		result.add(chartPanel);
		return result;
	}
	
	
	
	private DefaultCategoryDataset createDataset( )
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		dataset.addValue( 15 , "schools" , "1970" );
		dataset.addValue( 30 , "schools" , "1980" );
		dataset.addValue( 60 , "schools" ,  "1990" );
		dataset.addValue( 120 , "schools" , "2000" );
		dataset.addValue( 240 , "schools" , "2010" );
		dataset.addValue( 300 , "schools" , "2014" );
		return dataset;
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillData();
  	  	    }  
    	});
    }
}
