package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
	private StateSimulator ssim;
	
	private JProgressBar progressBar;
	private int simSteps = 100; // ile kroków symulacji
	private ArrayList<ArrayList<Integer>> placesRawData;
	private ArrayList<ArrayList<Integer>> transitionsRawData;

	private ArrayList<Integer> placesInChart;
	private ArrayList<String> placesInChartStr;
	private JPanel mainChartPanel = null;
	
	public AbyssStateSimulator() {
		sim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();
		ssim = new StateSimulator();
		setVisible(false);
		setTitle("State Simulator");
		setLocation(30,30);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(1000, 800));
		setResizable(false);
		
		JPanel main = new JPanel(null); //główny panel okna
		add(main);
		
		JPanel toolPanel = crateToolsPanel(); //panel opcji i przycisków
		main.add(toolPanel);
		
		mainChartPanel = new JPanel(); //panel wykresów
		mainChartPanel.setBorder(BorderFactory.createTitledBorder("Charts"));
		mainChartPanel.setBounds(0, 180, this.getWidth()-20, 600);
		mainChartPanel.add(createChart(createDataset()));
		
		main.add(mainChartPanel);
		initiateListeners(); //all hail Sithis
		repaint();
	}
	
	/**
	 * Metoda tworzy panel opcji okna symulatora stanów sieci.
	 * @return JPanel - panel
	 */
	private JPanel crateToolsPanel() {
		JPanel result = new JPanel(null);
		//result.setBorder(BorderFactory.createTitledBorder("Tools"));
		result.setBounds(0, 0, this.getWidth()-20, 180);
		
		JPanel dataAcquisitionPanel = new JPanel(null);
		dataAcquisitionPanel.setBorder(BorderFactory.createTitledBorder("Data acquisition"));
		dataAcquisitionPanel.setBounds(0, 0, 600, 100);
		int posXda = 10;
		int posYda = 20;
		
		JButton acqDataButton = new JButton("Acquire data");
		acqDataButton.setBounds(posXda, posYda, 110, 30);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				acquireData();
			}
		});
		dataAcquisitionPanel.add(acqDataButton);
		
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(100, 0, Integer.MAX_VALUE, 10);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setBounds(posXda +120, posYda, 80, 30);
		tokenSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				simSteps = val;
			}
		});
		dataAcquisitionPanel.add(tokenSpinner);
		
		posYda += 40;
		progressBar = new JProgressBar();
		progressBar.setBounds(posXda, posYda-7, 550, 40);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(25);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
	    dataAcquisitionPanel.add(progressBar);
	    
		result.add(dataAcquisitionPanel);
		
		//--------------------------------------------------------------------
		JPanel chartOptionsPanel = new JPanel(null);
		chartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Chart options"));
		chartOptionsPanel.setBounds(0, 100, 600, 80);
		int posXchart = 10;
		int posYchart = 20;
		
		JLabel label1 = new JLabel("Places:");
		label1.setBounds(posXchart, posYchart, 70, 20);
		chartOptionsPanel.add(label1);
		
		String[] dataP = { "---" };
		placesCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		placesCombo.setLocation(posXchart + 75, posYchart+2);
		placesCombo.setSize(500, 20);
		placesCombo.setSelectedIndex(0);
		placesCombo.setMaximumRowCount(6);
		placesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//int selected = placesCombo.getSelectedIndex();
			}
		});
		chartOptionsPanel.add(placesCombo);
		
		posYchart += 30;
		
		JButton addPlaceButton = new JButton("Add to chart");
		addPlaceButton.setBounds(posXchart, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		addPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					String name = placesCombo.getSelectedItem().toString();
					placesInChart.set(selected, 1);
					placesInChartStr.set(selected, name);
					
					mainChartPanel.removeAll();
					mainChartPanel.add(createChart(returnUpdatedDataSet()));
				}
			}
		});
		chartOptionsPanel.add(addPlaceButton);
		
		JButton removePlaceButton = new JButton("Remove");
		removePlaceButton.setBounds(posXchart+120, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		removePlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
			}
		});
		chartOptionsPanel.add(removePlaceButton);
		
		JButton clearChartButton = new JButton("Clear");
		clearChartButton.setBounds(posXchart+240, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		clearChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
			}
		});
		chartOptionsPanel.add(clearChartButton);
		
		result.add(chartOptionsPanel);
		
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
	
	private JPanel createChart(DefaultCategoryDataset dataSet) {
		JFreeChart lineChart = ChartFactory.createLineChart(
		         "Place values", "Steps", "Tokens",
		         dataSet, PlotOrientation.VERTICAL, true, true, false);
		
		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 900, 500 ) );
		JPanel result = new JPanel();
		result.add(chartPanel);
		return result;
	}
	
	private DefaultCategoryDataset returnUpdatedDataSet()
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(int p=0; p<placesInChart.size(); p++) {
			if(placesInChart.get(p) == -1)
				continue;
			
			int pId = p;
			String name = placesInChartStr.get(p);
			
			for(int i=0; i<placesRawData.size(); i++) {
				int value = placesRawData.get(i).get(pId);
				dataset.addValue(value, name, i+"");
			}
		}
		return dataset;
	}
	
	private DefaultCategoryDataset createDataset()
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
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

    /**
     * Metoda wywoływana przyciskiem, symuluje daną liczbę kroków sieci.
     */
	private void acquireData() {
		progressBar.setMaximum(simSteps);
		ssim.initiateSim(NetType.BASIC, true);
		ssim.simulateNet(simSteps, progressBar);
		placesRawData = ssim.getPlacesData();
		transitionsRawData = ssim.getTransitionsData();
		
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
	}

}
