package abyss.windows;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

import abyss.darkgui.GUIManager;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.StateSimulator;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa okna modułu symulatora stanów sieci.
 * @author MR
 *
 */
public class AbyssStateSimulator extends JFrame {
	private static final long serialVersionUID = 5287992734385359453L;
	private StateSimulator ssim;
	private boolean maximumMode = false;
	
	private JProgressBar progressBar;
	private int simSteps = 1000; // ile kroków symulacji
	private ArrayList<ArrayList<Integer>> placesRawData; //dane o historii miejsc z symulatora
	private ArrayList<ArrayList<Integer>> transitionsRawData; //j.w. : dla tranzycji
	private ArrayList<Integer> transitionsCompactData; // suma odpaleń tranzycji
	private int transInterval = 10;
	private ArrayList<Integer> placesInChart;
	private ArrayList<String> placesInChartStr;
	
	private XYSeriesCollection placesSeriesDataSet = null;
	private XYSeriesCollection transitionsSeriesDataSet = null;
	private JFreeChart placesChart;
	private JFreeChart transitionsChart;
	private int transChartType = 0; //suma odpaleń, 1=konkretne tranzycje
	
	private boolean listenerStart = false;
	private JPanel placesChartPanel;
	private JPanel placesChartOptionsPanel;
	private JPanel transitionsChartPanel;
	private JPanel transChartOptionsPanel;
	private JPanel dataToolsPanel;
	private JTabbedPane tabbedPane; //zakładki
	private JFrame ego;
	private JSpinner transIntervalSpinner;
	private JComboBox<String> placesCombo = null;
	private JComboBox<String> transitionsCombo = null;
	
	public AbyssStateSimulator() {
		ego = this;
		ssim = new StateSimulator();
		placesRawData = new ArrayList<ArrayList<Integer>>();
		transitionsRawData = new ArrayList<ArrayList<Integer>>();
		transitionsCompactData = new ArrayList<Integer>();
		placesInChart = new ArrayList<Integer>();
		placesInChartStr = new ArrayList<String>();
		
		initializeComponents();
	}

	/**
	 * Metoda pomocnica konstuktora, odpowiada za utworzenie elementów graficznych okna.
	 */
	private void initializeComponents() {
		setVisible(false);
		setTitle("State Simulator");
		setLocation(30,30);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(1000, 800));
		//setResizable(false);
		
		JPanel main = new JPanel(null); //główny panel okna
		add(main);
		
		dataToolsPanel = crateToolsPanel(); //panel opcji i przycisków
		main.add(dataToolsPanel);

		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, dataToolsPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-dataToolsPanel.getHeight()-40);
		ImageIcon icon = Tools.getResIcon16("images/middle.gif");
		tabbedPane.addTab("Places dynamics", icon, createPlacesTabPanel(), "Places dynamics");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Transitions dynamics", icon, createTransitionsTabPanel(), "Transistions dynamics");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		main.add(tabbedPane);
		initiateListeners(); //all hail Sithis
		repaint();
	}

	/**
	 * Metoda ta tworzy panel dla zakładki miejsc.
	 * @return JPanel - panel
	 */
	private JPanel createPlacesTabPanel() {
		JPanel result = new JPanel(null);
		result.setBounds(0, 0, this.getWidth()-20, 180);
		
		placesChartOptionsPanel = new JPanel(null);
		placesChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Chart options"));
		placesChartOptionsPanel.setBounds(0, 0, 600, 80);
		int posXchart = 10;
		int posYchart = 20;
		
		JLabel label1 = new JLabel("Places:");
		label1.setBounds(posXchart, posYchart, 70, 20);
		placesChartOptionsPanel.add(label1);
		
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
		placesChartOptionsPanel.add(placesCombo);
		
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
					
					addNewPlaceSeries(selected, name);
					updatePlacesGraphicChart();
				}
			}
		});
		placesChartOptionsPanel.add(addPlaceButton);
		
		JButton removePlaceButton = new JButton("Remove");
		removePlaceButton.setBounds(posXchart+120, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		removePlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					String name = placesCombo.getSelectedItem().toString();
					placesInChart.set(selected, 0);
					placesInChartStr.set(selected, "");
					
					removePlaceSeries(name);
				}
			}
		});
		placesChartOptionsPanel.add(removePlaceButton);
		
		JButton clearChartButton = new JButton("Clear");
		clearChartButton.setBounds(posXchart+240, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		clearChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearPlacesChart();
			}
		});
		placesChartOptionsPanel.add(clearChartButton);
		
		JButton saveChartButton = new JButton("Save");
		saveChartButton.setBounds(posXchart+400, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		saveChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				savePlacesChartImage();
			}
		});
		placesChartOptionsPanel.add(saveChartButton);
		result.add(placesChartOptionsPanel);
				
		placesChartPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
		 	//my zmieniać wymiary jeśli całe okno ma zmieniane w dowolnej chwili
		placesChartPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
		placesChartPanel.setBounds(0, placesChartOptionsPanel.getHeight(), this.getWidth()-30, 
				tabbedPane.getHeight() - placesChartOptionsPanel.getHeight()-40);
		placesChartPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
		result.add(placesChartPanel);
		
		return result;
	}
	
	/**
	 * Metoda ta tworzy panel dla zakładki tranzycji.
	 * @return JPanel - panel
	 */
	private JPanel createTransitionsTabPanel() {
		JPanel result = new JPanel(null);
		result.setBounds(0, 0, this.getWidth()-20, 180);
		
		transChartOptionsPanel = new JPanel(null);
		transChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Chart options"));
		transChartOptionsPanel.setBounds(0, 0, 600, 120);
		int posXchart = 10;
		int posYchart = 20;
		
		JButton showAllButton = new JButton("Show all");
		showAllButton.setBounds(posXchart, posYchart, 100, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		showAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showAllTransData();
			}
		});
		transChartOptionsPanel.add(showAllButton);
		
		JLabel label1 = new JLabel("Interval:");
		label1.setBounds(posXchart+110, posYchart, 80, 20);
		transChartOptionsPanel.add(label1);
		
		SpinnerModel intervSpinnerModel = new SpinnerNumberModel(100, 0, Integer.MAX_VALUE, 100);
		transIntervalSpinner = new JSpinner(intervSpinnerModel);
		transIntervalSpinner.setBounds(posXchart +170, posYchart, 60, 20);
		transIntervalSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				transInterval = (int) spinner.getValue();
			}
		});
		transChartOptionsPanel.add(transIntervalSpinner);

		posYchart += 30;
		
		JLabel label2 = new JLabel("Transition:");
		label2.setBounds(posXchart, posYchart, 70, 20);
		transChartOptionsPanel.add(label2);
		
		String[] dataP = { "---" };
		transitionsCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		transitionsCombo.setLocation(posXchart + 75, posYchart+2);
		transitionsCombo.setSize(500, 20);
		transitionsCombo.setSelectedIndex(0);
		transitionsCombo.setMaximumRowCount(6);
		transitionsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//int selected = placesCombo.getSelectedIndex();
			}
		});
		transChartOptionsPanel.add(transitionsCombo);
		
		posYchart += 20;
		
		JButton addPlaceButton = new JButton("Add to chart");
		addPlaceButton.setBounds(posXchart, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		addPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					String name = transitionsCombo.getSelectedItem().toString();

					addNewTransitionSeries(selected, name);
					//updateTransitionsGraphicChart();
				}
			}
		});
		transChartOptionsPanel.add(addPlaceButton);
		
		result.add(transChartOptionsPanel);
		
		transitionsChartPanel = new JPanel(new BorderLayout());
		transitionsChartPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart"));
		transitionsChartPanel.setBounds(0, transChartOptionsPanel.getHeight(), this.getWidth()-20, 
				tabbedPane.getHeight() - transChartOptionsPanel.getHeight()-40);
		
		transitionsChartPanel.add(createTransChartPanel(), BorderLayout.CENTER);
		result.add(transitionsChartPanel);

		return result;
	}
	
	/**
	 * Metoda tworzy panel opcji okna symulatora stanów sieci.
	 * @return JPanel - panel opcji pobrania danych
	 */
	private JPanel crateToolsPanel() {
		JPanel result = new JPanel(null);
		result.setBounds(0, 0, this.getWidth()-20, 100);
		
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
		
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(simSteps, 0, Integer.MAX_VALUE, 100);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setBounds(posXda +120, posYda, 80, 30);
		tokenSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				simSteps = val;
				
				//update spinner przerw/srednich dla tranzycji:
				int cVal = simSteps / 100;
				int mVal = simSteps / 5;
				if(cVal < 1)
					cVal = 1;
				if(cVal > mVal) {
					cVal = 1;
					mVal = 20;
				}
				try {
					SpinnerNumberModel spinnerClustersModel = new SpinnerNumberModel(cVal, 1, mVal, 1);
					transIntervalSpinner.setModel(spinnerClustersModel);
					transInterval = cVal;
				} catch (Exception ex) {
					GUIManager.getDefaultGUIManager().log("spinnerClustersModel error", "warning", true);
				}
			}
		});
		dataAcquisitionPanel.add(tokenSpinner);
		
		final JComboBox<String> simMode = new JComboBox<String>(new String[] {"Maximum mode", "50/50 mode"});
		simMode.setBounds(posXda+250, posYda, 80, 30);
		simMode.setSelectedIndex(1);
		simMode.setMaximumRowCount(6);
		simMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = simMode.getSelectedIndex();
				if(selected == 0)
					maximumMode = true;
				else
					maximumMode = false;
			}
		});
		dataAcquisitionPanel.add(simMode);
		
		posYda += 40;
		progressBar = new JProgressBar();
		progressBar.setBounds(posXda, posYda-7, 550, 40);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
	    dataAcquisitionPanel.add(progressBar);
		result.add(dataAcquisitionPanel);
		
		return result;
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************     MIEJSCA      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda ta tworzy wykres liniowy dla miejsc.
	 * @return JPanel - panel z wykresem
	 */
	private JPanel createPlacesChartPanel() {
		String chartTitle = "Places dynamics";
	    String xAxisLabel = "Step";
	    String yAxisLabel = "Tokens";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
	    
		placesSeriesDataSet = new XYSeriesCollection();
	    placesChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, placesSeriesDataSet, 
	    		PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

	    ChartPanel res = new ChartPanel(placesChart);
	    return res;
	}
	
	/**
	 * Metoda uaktualniania stylu wyświetlania
	 */
	private void updatePlacesGraphicChart() {
		XYPlot plot = placesChart.getXYPlot();
		//XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		//renderer.setSeriesLinesVisible(0, true);
        //renderer.setSeriesShapesVisible(0, false);
        //renderer.setSeriesLinesVisible(1, false);
        //renderer.setSeriesShapesVisible(1, true); 
		 //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    //plot.setRenderer(renderer);
	    //plot.setBackgroundPaint(Color.DARK_GRAY);
		
		int count =  placesChart.getXYPlot().getSeriesCount();
		
		for(int i=0; i<count; i++) {
			plot.getRenderer().setSeriesStroke(i, new BasicStroke(1.0f));
		}
		//plot.setRenderer(renderer);
	}
	
	/**
	 * Metoda dodaje nową linię do wykresu.
	 * @param selPlaceID int - indeks miejsca
	 * @param name String - nazwa miejsca
	 */
	private void addNewPlaceSeries(int selPlaceID, String name) {
		@SuppressWarnings("unchecked")
		List<XYSeries> x = placesSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name))
				return;
		}
		
		XYSeries series = new XYSeries(name);
		//ArrayList<Integer> test = new ArrayList<Integer>();
		for(int step=0; step<placesRawData.size(); step++) {
			int value = placesRawData.get(step).get(selPlaceID);
			//test.add(value);
			series.add(step, value);
		}
		placesSeriesDataSet.addSeries(series);
	}
	
	/**
	 * Metoda ta usuwa z wykresu dane o wskazanym miejscu.
	 * @param name String - nazwa miejsca (unikalna)
	 */
	private void removePlaceSeries(String name) {
		@SuppressWarnings("unchecked")
		List<XYSeries> x = placesSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name)) {
				placesSeriesDataSet.removeSeries(xys);
				return;
			}
		}
	}
	
	/**
	 * Metoda zapisująca aktualnie wyświetlany wykres do pliku.
	 */
	private void savePlacesChartImage() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Portable Network Graphics (.png)", new String[] { "PNG" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "");
		if(selectedFile.equals(""))
			return;
		
		if(!selectedFile.contains(".png"))
			selectedFile += ".png";
		
		File imageFile = new File(selectedFile);
		int width = 1024;
		int height = 768;
		 
		try {
		    ChartUtilities.saveChartAsPNG(imageFile, placesChart, width, height);
		} catch (IOException ex) {
		    System.err.println(ex);
		}
	}
	
	/**
	 * Metoda czyszcząca dane okna.
	 */
	private void clearPlacesChart() {
		placesSeriesDataSet.removeAllSeries();
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************    TRANZYCJE     ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda ta tworzy wykres liniowy dla tranzycji.
	 * @return JPanel - panel z wykresem
	 */
	private JPanel createTransChartPanel() {
		String chartTitle = "Transitions dynamics";
	    String xAxisLabel = "Transition";
	    String yAxisLabel = "Firing";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
	    
		transitionsSeriesDataSet = new XYSeriesCollection();
		transitionsChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, transitionsSeriesDataSet,
			PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
	    ChartPanel res = new ChartPanel(transitionsChart);
	    return res;
	}
	
	/**
	 * Metoda dodaje nowy wykres dla tranzycji.
	 * @param selTransID int - indeks tranzycji
	 * @param name String - nazwa tranzycji
	 */
	private void addNewTransitionSeries(int selTransID, String name) {
		if(transChartType == 0) { //replace chart
			transitionsChartPanel.removeAll();
			transitionsChartPanel.add(createTransChartPanel(), BorderLayout.CENTER);
			transitionsChartPanel.revalidate();
			transitionsChartPanel.repaint();
			transChartType = 1;
			
			transitionsSeriesDataSet.removeAllSeries(); // ??
		}
		
		@SuppressWarnings("unchecked")
		List<XYSeries> x = transitionsSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name))
				return;
		}
		
		XYSeries series = new XYSeries(name);
		//ArrayList<Integer> test = new ArrayList<Integer>();
		int counter = 0;
		for(int step=0; step<transitionsRawData.size(); step++) {
			//TODO: check interval
			
			int value = 0; //suma odpaleń w przedziale czasu
			for(int i=0; i<transInterval; i++) {
				try {
					value += transitionsRawData.get(step+i).get(selTransID);
				} catch (Exception e) {
					
				}
			}
			
			
			//test.add(value);
			series.add(step, value);
			step += transInterval;
			counter++;
		}
		transitionsSeriesDataSet.addSeries(series);
	}
	
	/**
	 * Metoda ta pokazuje na wykresie tranzycji wszystkie odpalenia tranzycji dla obliczonej
	 * liczby kroków symulacji.
	 */
	private void showAllTransData() {
		if(transitionsCompactData.size() == 0)
			return;

		transChartType = 0;
		int steps = transitionsRawData.size();
		
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    for(int t=0; t<transitionsCompactData.size(); t++) {
			String tName = "t"+t+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(t).getName();
			int value = transitionsCompactData.get(t);
			dataset.addValue(value, "Firing", tName);
			dataset.addValue(steps-value, "NotFiring", tName);
		}
	    
	    String chartTitle = "Transitions dynamics";
	    String xAxisLabel = "Transition";
	    String yAxisLabel = "Firing";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
		transitionsChart = ChartFactory.createStackedBarChart(chartTitle, xAxisLabel, yAxisLabel, dataset,
			PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		CategoryPlot plot = (CategoryPlot) transitionsChart.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		Paint p1 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, Color.red);
        renderer.setSeriesPaint(0, p1);
        Paint p2 = new GradientPaint(0.0f, 0.0f, Color.lightGray, 0.0f, 0.0f, Color.lightGray);
        renderer.setSeriesPaint(1, p2);
        plot.setRenderer(renderer);

		transitionsChartPanel.removeAll();
		transitionsChartPanel.add(new ChartPanel(transitionsChart), BorderLayout.CENTER);
		transitionsChartPanel.revalidate();
		transitionsChartPanel.repaint();
	}

	/**
	 * Metoda czyszcząca dane okna.
	 */
	private void clearTransitionsChart() {
		transitionsSeriesDataSet.removeAllSeries();
		
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************      OGÓLNE      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda wypełnia komponenty rozwijalne danymi o miejscach i tranzycjach.
	 */
	private void fillPlacesAndTransitionsData() {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places== null || places.size() == 0)
			return;
		
		placesCombo.removeAllItems();
		placesCombo.addItem("---");
		for(int p=0; p < places.size(); p++) {
			placesCombo.addItem("p"+(p)+"."+places.get(p).getName());
		}
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions== null || transitions.size() == 0)
			return;
		
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		for(int t=0; t < transitions.size(); t++) {
			transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
		}
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania. Przedw wszystkim
	 * chodzi o reakcję na powiększanie / pomniejszanie okna głównego.
	 */
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillPlacesAndTransitionsData();
  	  	    }  
    	});
    	
    	listenerStart = true; //na wszelki wypadek
		addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	if(!listenerStart)
            		return;
            	tabbedPane.setBounds(0, dataToolsPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-dataToolsPanel.getHeight()-40);
            	placesChartPanel.setBounds(0, 80, ego.getWidth()-30, 
            			tabbedPane.getHeight() - placesChartOptionsPanel.getHeight()-40);
            	transitionsChartPanel.setBounds(0, transChartOptionsPanel.getHeight(), ego.getWidth()-30, 
            			tabbedPane.getHeight() - transChartOptionsPanel.getHeight()-40);
            }
            public void componentMoved(ComponentEvent e) {
            	if(!listenerStart)
            		return;
            	tabbedPane.setBounds(0, dataToolsPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-dataToolsPanel.getHeight()-40);
            	placesChartPanel.setBounds(0, 80, ego.getWidth()-30, 
            			tabbedPane.getHeight() - placesChartOptionsPanel.getHeight()-40);
            	transitionsChartPanel.setBounds(0, transChartOptionsPanel.getHeight(), ego.getWidth()-30, 
            			tabbedPane.getHeight() - transChartOptionsPanel.getHeight()-40);
            }
        });
    }

    /**
     * Metoda wywoływana przyciskiem, symuluje daną liczbę kroków sieci, a następnie
     * pobiera tablice i wektory danych z obiektu symulatora do struktur wewnętrznych
     * obiektu klasy AbyssStateSimulator - czyli podokna programu głównego.
     */
	private void acquireData() {
		clearTransitionsChart();
		clearPlacesChart();
		
		progressBar.setMaximum(simSteps);
		ssim.initiateSim(NetType.BASIC, maximumMode);
		ssim.simulateNet(simSteps, progressBar);
		
		placesRawData.clear();
		transitionsRawData.clear();
		
		placesRawData = ssim.getPlacesData();
		transitionsRawData = ssim.getTransitionsData();
		transitionsCompactData = ssim.getTransitionsCompactData();
		
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
	}

	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        //panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        panel.setBounds(0, 0, 640, 480);
        return panel;
    }
}
