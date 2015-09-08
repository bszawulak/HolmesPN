package holmes.windows.ssim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.xy.XYSeriesCollection;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.tables.SimKnockPlacesCompAllTableModel;
import holmes.tables.SimKnockPlacesCompTableModel;
import holmes.tables.SimKnockPlacesTableModel;
import holmes.tables.SimKnockTableRenderer;
import holmes.tables.SimKnockTransCompAllTableModel;
import holmes.tables.SimKnockTransCompTableModel;
import holmes.tables.SimKnockTransTableModel;
import holmes.tables.SimKnockPlacesCompAllTableModel.DetailsPlace;
import holmes.tables.SimKnockTransCompAllTableModel.DetailsTrans;
import holmes.utilities.Tools;
import holmes.windows.HolmesNodeInfo;
import holmes.windows.HolmesNotepad;

/**
 * Klasa okna przeglądania danych symulacji knockout.
 * 
 * @author MR
 */
public class HolmesSimKnockVis extends JFrame {
	private static final long serialVersionUID = 3020186160500907678L;
	private GUIManager overlord;
	private PetriNet pn;
	private static final DecimalFormat formatter1 = new DecimalFormat( "#.#" );
	private static final DecimalFormat formatter2 = new DecimalFormat( "#.##" );
	private static final DecimalFormat formatter3 = new DecimalFormat( "#.###" );
	
	private boolean doNotUpdate = false;
	private HolmesSim boss;
	private XYSeriesCollection placesSeriesDataSet = null;
	private XYSeriesCollection transitionsSeriesDataSet = null;
	private JFreeChart placesChart;
	private JFreeChart transitionsChart;
	private JPanel placesChartPanel;
	private JPanel transitionsChartPanel;
	
	private JComboBox<String> referencesCombo = null;
	private JComboBox<String> dataCombo = null;
	private JComboBox<String> seriesCombo = null;
	
	private ArrayList<Place> places;
	private ArrayList<Transition> transitions;
	
	//tablice:
	private DefaultTableModel model;
	private SimKnockTableRenderer tableRenderer;
	private SimKnockPlacesTableModel modelPlaces;
	private SimKnockPlacesCompTableModel modelPlacesComp;
	private SimKnockPlacesCompAllTableModel modelPlacesCompAll;
	private SimKnockTransTableModel modelTrans;
	private SimKnockTransCompTableModel modelTransComp;
	private SimKnockTransCompAllTableModel modelTransCompAll;
	private JTable placesTable;
	private JTable transTable;
	private JTable placesCompAllTable;
	private JTable transCompAllTable;
	
	
	/**
	 * Konstruktor obiektu klast HolmesStateSimKnockVis.
	 * @param boss HolmesStateSim - główne okno symulatora
	 */
	public HolmesSimKnockVis(HolmesSim boss) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.boss = boss;
		
		places = pn.getPlaces();
		transitions = pn.getTransitions();
		
		initializeComponents();
		initiateListeners();
	}
	
	/**
	 * Klasa budująca okno.
	 */
	private void initializeComponents() {
		setVisible(true);
		setTitle("Knockout data visualisation");
		setLocation(boss.getLocation().x+30,boss. getLocation().y+30);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(1200, 900));
		
		JPanel main = new JPanel(new BorderLayout()); //główny panel okna
		add(main);

		main.add(getActionsPanel(), BorderLayout.NORTH);
		
		JPanel mainTabbedPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Charts", Tools.getResIcon32("/icons/simulationKnockout/visChartsIcon.png"), createPlacesChartsPanel(), 
				"<html>Show charts comparing difference in places na transitions<br>behaviour between reference and data sets</html>");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Places", Tools.getResIcon16("/icons/simulationKnockout/visPlaces.png"), createPlacesTablePanel(), 
				"<html>Show places behaviour for selected dataset OR<br>comparison between reference and data sets</html>");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		tabbedPane.addTab("Transitions", Tools.getResIcon16("/icons/simulationKnockout/visTrans.png"), createTransTablePanel(), 
				"<html>Show transitions behaviour for selected dataset OR<br>comparison between reference and data sets</html>");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		tabbedPane.addTab("Places (series)", Tools.getResIcon16("/icons/simulationKnockout/visPlacesSeries.png"), createPlacesCompAllTablePanel(), 
				"<html>Show places table for knockout series.</html>");
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
		tabbedPane.addTab("Transitions (series)", Tools.getResIcon16("/icons/simulationKnockout/visTransSeries.png"), createTransCompAllTablePanel(), 
				"<html>Show transitions table for knockout series.</html>");
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);
		
		mainTabbedPanel.add(tabbedPane);
		
		main.add(mainTabbedPanel, BorderLayout.CENTER);
		
		this.boss.setEnabled(false);
		GUIManager.getDefaultGUIManager().getFrame().setEnabled(false);
	}
	
	/**
	 * Zwraca panel górny - opcje, comboboxy.
	 * @return JPanel - okrętu się pan spodziewałeś?
	 */
	private JPanel getActionsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Main options panel"));
		result.setPreferredSize(new Dimension(670, 100));
	
		int posXda = 10;
		int posYda = 20;
		
		JLabel label1 = new JLabel("Ref sets:");
		label1.setBounds(posXda, posYda, 70, 20);
		result.add(label1);
		
		String[] data = { " ----- " };
		referencesCombo = new JComboBox<String>(data); //final, aby listener przycisku odczytał wartość
		referencesCombo.setToolTipText("Select reference data (presumably dataset without any transition knockout)");
		referencesCombo.setBounds(posXda+80, posYda, 500, 20);
		referencesCombo.setMaximumRowCount(12);
		referencesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
				
				
			}
			
		});
		result.add(referencesCombo);
		
		JLabel label2 = new JLabel("Data sets:");
		label2.setBounds(posXda, posYda+=25, 70, 20);
		result.add(label2);
		
		String[] data2 = { " ----- " };
		dataCombo = new JComboBox<String>(data2); //final, aby listener przycisku odczytał wartość
		dataCombo.setToolTipText("Select dataset (presumably with some transition(s) knocked out)");
		dataCombo.setBounds(posXda+80, posYda, 500, 20);
		dataCombo.setMaximumRowCount(12);
		dataCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;

				//int selected = dataCombo.getSelectedIndex();
			}
			
		});
		result.add(dataCombo);

		JLabel label3 = new JLabel("Sim. series:");
		label3.setBounds(posXda, posYda+=25, 80, 20);
		result.add(label3);
		
		String[] dataSeries = { " ----- " };
		seriesCombo = new JComboBox<String>(dataSeries); //final, aby listener przycisku odczytał wartość
		seriesCombo.setToolTipText("Select dataset series (presumably with every transition disable one per dataset)");
		seriesCombo.setBounds(posXda+80, posYda, 250, 20);
		seriesCombo.setMaximumRowCount(12);
		seriesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
			}
			
		});
		result.add(seriesCombo);
		
		JButton showRefDataButton = new JButton("<html>Show reference<br/>&nbsp; &nbsp; &nbsp; &nbsp; dataset</html>");
		showRefDataButton.setBounds(posXda+590, 20, 175, 36);
		showRefDataButton.setMargin(new Insets(0, 0, 0, 0));
		showRefDataButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/visShowRefButton.png"));
		showRefDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selRef = referencesCombo.getSelectedIndex();
				if(selRef > 0) {
					showSingleKnockoutCharts(true);
					createPlacesTable(true);
					createTransTable(true);
				}
			}
		});
		result.add(showRefDataButton);
		
		JButton showKnockDataButton = new JButton("<html>Show knockout<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dataset</html>");
		showKnockDataButton.setBounds(posXda+770, 20, 175, 36);
		showKnockDataButton.setMargin(new Insets(0, 0, 0, 0));
		showKnockDataButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visShowDataSetButton.png"));
		showKnockDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = dataCombo.getSelectedIndex();
				if(selected > 0) {
					showSingleKnockoutCharts(false);
					createPlacesTable(false);
					createTransTable(false);
				}
			}
		});
		result.add(showKnockDataButton);
		
		JButton showChartKnockButton = new JButton("<html>Compare reference<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;and knockout</html>");
		showChartKnockButton.setBounds(posXda+950, 20, 175, 36);
		showChartKnockButton.setMargin(new Insets(0, 0, 0, 0));
		showChartKnockButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visCompRefDataButton.png"));
		showChartKnockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selRef = referencesCombo.getSelectedIndex();
				int dataRef = dataCombo.getSelectedIndex();
				if(selRef < 1 || dataRef < 1) {
					JOptionPane.showMessageDialog(null,
						"Please select reference and knockout set!", 
						"Selection needed", JOptionPane.WARNING_MESSAGE);
				} else {
					showCompareCharts();
					createPlacesCompTable();
					createTransCompTable();
				}
			}
		});
		result.add(showChartKnockButton);
		
		JButton showSeriesButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;Full series&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datatables</html>");
		showSeriesButton.setBounds(posXda+590, 60, 175, 36);
		showSeriesButton.setMargin(new Insets(0, 0, 0, 0));
		showSeriesButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visRefDataSeriesButton.png"));
		showSeriesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = seriesCombo.getSelectedIndex() - 1;
				if(selected > -1) {
					createCompareAllTables(selected);
				}
			}
		});
		result.add(showSeriesButton);
		
		JButton showNotepadButton = new JButton("<html>&nbsp; &nbsp;Show notepad<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;summary</html>");
		showNotepadButton.setBounds(posXda+770, 60, 175, 36);
		showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
		showNotepadButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visRefDataSeriesNotepadButton.png"));
		showNotepadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = seriesCombo.getSelectedIndex() - 1;
				if(selected > -1) {
					createCompareAllTablesNotepad(selected);
				}
			}
		});
		result.add(showNotepadButton);
		
		return result;
	}
	
	/**
	 * Tworzenie panelu wykresu miejsc.
	 * JPanel - okrętu się pan spodziewałeś?
	 */
	private JPanel createPlacesChartsPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder("Charts"));
		result.setPreferredSize(new Dimension(670, 500));

		String chartTitle = "Places dynamics";
	    String xAxisLabel = "Step";
	    String yAxisLabel = "Tokens";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
		placesSeriesDataSet = new XYSeriesCollection();
	    placesChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, placesSeriesDataSet, 
	    		PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
	    placesChartPanel = new JPanel(new BorderLayout());
		placesChartPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
		placesChartPanel.add(new ChartPanel(placesChart), BorderLayout.CENTER);

		JPanel result2 = new JPanel(new BorderLayout());
		result2.setBorder(BorderFactory.createTitledBorder("Charts"));
		result2.setPreferredSize(new Dimension(670, 500));
		
		String chartTitle2 = "Transitions dynamics";
	    String xAxisLabel2 = "Transition";
	    String yAxisLabel2 = "Firing";
	    boolean showLegend2 = true;
	    boolean createTooltip2 = true;
	    boolean createURL2 = false;
	    
		transitionsSeriesDataSet = new XYSeriesCollection();
		transitionsChart = ChartFactory.createXYLineChart(chartTitle2, xAxisLabel2, yAxisLabel2, transitionsSeriesDataSet,
			PlotOrientation.VERTICAL, showLegend2, createTooltip2, createURL2);

		transitionsChartPanel = new JPanel(new BorderLayout());
		transitionsChartPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart"));
		transitionsChartPanel.add(new ChartPanel(transitionsChart), BorderLayout.CENTER);

		JSplitPane pane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, placesChartPanel, transitionsChartPanel );
		pane.setDividerLocation(350);
		result.add(pane);
		return result;
	}

	
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	
	/**
	 * Metoda wypełnia wykresy danymi.
	 * @param showRef boolean - true, jeśli chodzi o zbiór referencyjny
	 */
	@SuppressWarnings("deprecation")
	protected void showSingleKnockoutCharts(boolean showRef) {
		NetSimulationData data = getCorrectSet(showRef);
		if(data == null)
			return;
		
		//PLACES:
		StatisticalCategoryDataset datasetPlaces = createPlacesDataset(data, null);

        CategoryAxis xAxisPlaces = new CategoryAxis("Type");
        xAxisPlaces.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisPlaces.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisPlaces.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Value");
        CategoryItemRenderer rendererPlaces = new StatisticalBarRenderer();
        CategoryPlot plotPlaces = new CategoryPlot(datasetPlaces, xAxisPlaces, yAxisPlaces, rendererPlaces);
        
        rendererPlaces.setToolTipGenerator(new CustomToolTipGenerator(true, false, places, transitions, data, null));
        
        placesChartPanel.removeAll();
        placesChart = new JFreeChart("Places statistical data", new Font("Helvetica", Font.BOLD, 14), plotPlaces, true);
		ChartPanel chartPanelPlaces = new ChartPanel(placesChart);
		chartPanelPlaces.setPreferredSize(new Dimension(data.placesNumber*40, 270)); 
		chartPanelPlaces.setMaximumDrawWidth(data.placesNumber*40);
	    JScrollPane sPanePlaces = new JScrollPane(chartPanelPlaces); 
	    placesChartPanel.add(sPanePlaces, BorderLayout.CENTER);
	    placesChartPanel.revalidate();
	    placesChartPanel.repaint();

	    //TRANSITIONS:
		StatisticalCategoryDataset datasetTrans = createTransitionsDataset(data, null);
		
        CategoryAxis xAxisTrans = new CategoryAxis("Type");
        xAxisTrans.setLowerMargin(0.01d); // percentage of space before first bar
       	xAxisTrans.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisTrans.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisTrans = new NumberAxis("Value");
        CategoryItemRenderer rendererTrans = new StatisticalBarRenderer();
        CategoryPlot plotTrans = new CategoryPlot(datasetTrans, xAxisTrans, yAxisTrans, rendererTrans);
        
        rendererTrans.setToolTipGenerator(new CustomToolTipGenerator(false, false, places, transitions, data, null));
        
        transitionsChartPanel.removeAll();
        transitionsChart = new JFreeChart("Transitions statistical data", new Font("Helvetica", Font.BOLD, 14), plotTrans, true);
		ChartPanel chartPanelTrans = new ChartPanel(transitionsChart);
		chartPanelTrans.setPreferredSize(new Dimension(data.transNumber*40, 270)); 
		chartPanelTrans.setMaximumDrawWidth(data.transNumber*40);
	    JScrollPane sPaneTrans = new JScrollPane(chartPanelTrans); 
	    transitionsChartPanel.add(sPaneTrans, BorderLayout.CENTER);
	    transitionsChartPanel.revalidate();
	    transitionsChartPanel.repaint();
	}

	/**
	 * Zwraca odpowiedni zbiór danych: referencyjne lub knockout.
	 * @param showRef boolean - true, jeśli wybrano dane referencyjne
	 * @return NetSimulationData - pakiet danych symulacji
	 */
	private NetSimulationData getCorrectSet(boolean showRef) {
		NetSimulationData data = null;
		if(showRef) {
			int selectedRef = referencesCombo.getSelectedIndex() - 1;
			if(selectedRef == -1)
				return null;
			data = pn.accessSimKnockoutData().getReferenceSet(selectedRef);
		} else {
			int selectedKnockout = dataCombo.getSelectedIndex() - 1;
			if(selectedKnockout == -1)
				return null;
			data = pn.accessSimKnockoutData().getKnockoutSet(selectedKnockout);
		}
		if(data == null) {
			overlord.log("Error accessing dataset.", "error", true);
			return null;
		}
		return data;
	}
	
	/**
	 * Metoda pokazująca na wykresach porównanie zbioru referencyjnego i knockout
	 */
	@SuppressWarnings("deprecation")
	protected void showCompareCharts() {
		NetSimulationData refData = null;
		NetSimulationData knockData = null;
		int selectedRef = referencesCombo.getSelectedIndex() - 1;
		if(selectedRef == -1)
			return;
		refData = pn.accessSimKnockoutData().getReferenceSet(selectedRef);

		int selectedKnockout = dataCombo.getSelectedIndex() - 1;
		if(selectedKnockout == -1)
			return;
		knockData = pn.accessSimKnockoutData().getKnockoutSet(selectedKnockout);
		
		if(refData == null || knockData == null) {
			overlord.log("Error accessing dataset.", "error", true);
			return;
		}
		
		StatisticalCategoryDataset datasetPlaces = createPlacesDataset(refData, knockData);

        CategoryAxis xAxisPlaces = new CategoryAxis("Type");
        xAxisPlaces.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisPlaces.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisPlaces.setCategoryMargin(0.1d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Value");
        CategoryItemRenderer rendererPlaces = new StatisticalBarRenderer();
        CategoryPlot plotPlaces = new CategoryPlot(datasetPlaces, xAxisPlaces, yAxisPlaces, rendererPlaces);
        //tutaj ustawiamy odstęp między paskami TEJ SAMEJ SERII:
        BarRenderer rendererBarPlaces = (BarRenderer) plotPlaces.getRenderer();
        rendererBarPlaces.setItemMargin(0.0);
        
        rendererPlaces.setToolTipGenerator(new CustomToolTipGenerator(true, true, places, transitions, refData, knockData));
        
        placesChartPanel.removeAll();
        placesChart = new JFreeChart("Places statistical data", new Font("Helvetica", Font.BOLD, 14), plotPlaces, true);
		ChartPanel chartPanelPlaces = new ChartPanel(placesChart);
		chartPanelPlaces.setPreferredSize(new Dimension(refData.placesNumber*50, 270)); 
		chartPanelPlaces.setMaximumDrawWidth(refData.placesNumber*50);
	    JScrollPane sPanePlaces = new JScrollPane(chartPanelPlaces); 
	    placesChartPanel.add(sPanePlaces, BorderLayout.CENTER);
	    placesChartPanel.revalidate();
	    placesChartPanel.repaint();


	    //TRANSITIONS:
		StatisticalCategoryDataset datasetTrans = createTransitionsDataset(refData, knockData);
		
        CategoryAxis xAxisTrans = new CategoryAxis("Type");
        xAxisTrans.setLowerMargin(0.01d); // percentage of space before first bar
       	xAxisTrans.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisTrans.setCategoryMargin(0.1d); // percentage of space between categories
        ValueAxis yAxisTrans = new NumberAxis("Value");
        CategoryItemRenderer rendererTrans = new StatisticalBarRenderer();
        CategoryPlot plotTrans = new CategoryPlot(datasetTrans, xAxisTrans, yAxisTrans, rendererTrans);
      //tutaj ustawiamy odstęp między paskami TEJ SAMEJ SERII:
        BarRenderer rendererBarTrans= (BarRenderer) plotTrans.getRenderer();
        rendererBarTrans.setItemMargin(0.0);
        
        rendererTrans.setToolTipGenerator(new CustomToolTipGenerator(false, true, places, transitions, refData, knockData));
        
        transitionsChartPanel.removeAll();
        transitionsChart = new JFreeChart("Transitions statistical data", new Font("Helvetica", Font.BOLD, 14), plotTrans, true);
		ChartPanel chartPanelTrans = new ChartPanel(transitionsChart);
		chartPanelTrans.setPreferredSize(new Dimension(refData.transNumber*50, 270)); 
		chartPanelTrans.setMaximumDrawWidth(refData.transNumber*50);
	    JScrollPane sPaneTrans = new JScrollPane(chartPanelTrans); 
	    transitionsChartPanel.add(sPaneTrans, BorderLayout.CENTER);
	    transitionsChartPanel.revalidate();
	    transitionsChartPanel.repaint();
	}
	
	/**
	 * Metoda tworzy zbiór danych dla wykresu dla miejsc na bazie symulacji knockout.
	 * @param data NetSimulationData - pakiet danych
	 * @param compData NetSimulationData - drugi pakiet (opcjonalnie, do porównania)
	 * @return StatisticalCategoryDataset - dane dla wykresu
	 */
	private StatisticalCategoryDataset createPlacesDataset(NetSimulationData data, NetSimulationData compData) {
        final DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
        
        if(compData == null) {
        	int places = data.placesNumber;
            
            double max = 0;
            for(int p=0; p<places; p++) {
    			double val = data.placeTokensAvg.get(p);
    			if(val > max)
    				max = val;
            }
            
    		for(int p=0; p<places; p++) {
    			double val = data.placeTokensAvg.get(p);
    			if(val > 0)
    				result.add(data.placeTokensAvg.get(p), data.placeStdDev.get(p), "Tokens", "p"+p);
    			else
    				result.add(-(0.05*max), 0, "Tokens", "p"+p);
    		}
        } else { //compare
        	int placesRef = data.placesNumber;
        	int placesKnock = compData.placesNumber;
        	if(placesRef != placesKnock) {
        		overlord.log("Reference set and knockout set have different number of places.", "error", true);
        		return null;
        	}
        	
        	double maxRef = 0;
        	double maxKnock = 0;
            for(int p=0; p<placesRef; p++) {
    			double val = data.placeTokensAvg.get(p);
    			if(val > maxRef)
    				maxRef = val;
    			
    			val = compData.placeTokensAvg.get(p);
    			if(val > maxKnock)
    				maxKnock = val;
            }

            for(int p=0; p<placesRef; p++) {
    			double val = data.placeTokensAvg.get(p);
    			double valKnock = compData.placeTokensAvg.get(p);
    			if(val > 0) {
    				result.add(data.placeTokensAvg.get(p), data.placeStdDev.get(p), "Reference dataset", "p"+p);
    			} else {
    				result.add(-(0.05*maxRef), 0, "Reference dataset", "p"+p);		
    			}
    			
    			if(valKnock > 0) {
    				result.add(compData.placeTokensAvg.get(p), compData.placeStdDev.get(p), "Knockout dataset", "p"+p);
    			} else {
    				result.add(-(0.05*maxRef), 0, "Knockout dataset", "p"+p);		
    			}
    		}
        }
        return result;
    }
	
	/**
	 * Metoda tworzy zbiór danych dla wykresu dla tranzycji na bazie symulacji knockout.
	 * @param data NetSimulationData - pakiet danych
	 * @param knockData NetSimulationData - pakiet do porównania
	 * @return StatisticalCategoryDataset - dane dla wykresu
	 */
	private StatisticalCategoryDataset createTransitionsDataset(NetSimulationData data, NetSimulationData compData) {
        final DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
        
        if(compData == null) { 
        	int transitions = data.transNumber;
            
            double max = 0;
            for(int t=0; t<transitions; t++) {
    			double val = data.transFiringsAvg.get(t);
    			if(val > max)
    				max = val;
            }
            
    		for(int t=0; t<transitions; t++) {
    			double val = data.transFiringsAvg.get(t);
    			if(val > 0) {
    				result.add(data.transFiringsAvg.get(t), data.transStdDev.get(t), "Firing", "t"+t);
    			} else {
    				result.add(-(0.05*max), 0, "Firing", "t"+t);
    			}
    			
    		}
        } else {
        	int transRef = data.transNumber;
        	int transKnock = compData.transNumber;
        	if(transRef != transKnock) {
        		overlord.log("Reference set and knockout set have different number of transitions.", "error", true);
        		return null;
        	}
        	
        	double maxRef = 0;
        	double maxKnock = 0;
            for(int t=0; t<transRef; t++) {
    			double val = data.transFiringsAvg.get(t);
    			if(val > maxRef)
    				maxRef = val;
    			
    			val = compData.transFiringsAvg.get(t);
    			if(val > maxKnock)
    				maxKnock = val;
            }

            for(int t=0; t<transRef; t++) {
    			double val = data.transFiringsAvg.get(t);
    			double valKnock = compData.transFiringsAvg.get(t);
    			if(val > 0) {
    				result.add(data.transFiringsAvg.get(t), data.transStdDev.get(t), "Reference dataset", "t"+t);
    			} else {
    				result.add(-(0.05*maxRef), 0, "Reference dataset", "t"+t);		
    			}
    			
    			if(valKnock > 0) {
    				result.add(compData.transFiringsAvg.get(t), compData.transStdDev.get(t), "Knockout dataset", "t"+t);
    			} else {
    				result.add(-(0.05*maxRef), 0, "Knockout dataset", "t"+t);		
    			}
    		}
        }
        return result;
    }
	
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	
	/**
     * Tworzy podstawową wersję tabeli miejsc.
     * @return JPanel - panel
     */
	private JPanel createPlacesTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Place single knockout data table:"));
		model = new DefaultTableModel();
		placesTable = new JTable(model);
		
		placesTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction(placesTable);
          	    }
          	 }
      	});
		
		
		tableRenderer = new SimKnockTableRenderer(placesTable);
		placesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(placesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		return tablesSubPanel;
	}
	
	/**
     * Tworzy podstawową wersję tabeli miejsc (porównawcza).
     * @return JPanel - panel
     */
	private JPanel createPlacesCompAllTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Places comparison single knockout data table:"));
		model = new DefaultTableModel();
		placesCompAllTable = new JTable(model);
		
		placesCompAllTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction(placesCompAllTable);
          	    }
          	 }
      	});
		
		
		tableRenderer = new SimKnockTableRenderer(placesCompAllTable);
		placesCompAllTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(placesCompAllTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		return tablesSubPanel;
	}

	/**
     * Tworzy podstawową wersję tabeli tranzycji.
     * @return JPanel - panel
     */
    private JPanel createTransTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Transition single knockout data table:"));
		model = new DefaultTableModel();
		transTable = new JTable(model);
		
		transTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction(transTable);
          	    }
          	 }
      	});
		
		tableRenderer = new SimKnockTableRenderer(transTable);
		transTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(transTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		return tablesSubPanel;
	}
    
    /**
     * Tworzy podstawową wersję tabeli tranzycji (porównawcza).
     * @return JPanel - panel
     */
    private JPanel createTransCompAllTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Transition comparison single knockout data table:"));
		model = new DefaultTableModel();
		transCompAllTable = new JTable(model);
		
		transCompAllTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction(transCompAllTable);
          	    }
          	 }
      	});
		
		tableRenderer = new SimKnockTableRenderer(transCompAllTable);
		transCompAllTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(transCompAllTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		return tablesSubPanel;
	}

	/**
	 * Metoda tworząca tabelę miejsc
	 * @param ref  boolean - true, jeśli wywołane przez zbiór referencyjny
	 */
    private void createPlacesTable(boolean ref) {
    	NetSimulationData data = getCorrectSet(ref);
    	if(data == null)
    		return;
    	
    	modelPlaces = new SimKnockPlacesTableModel();
    	placesTable.setModel(modelPlaces);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	int svaluesCell = 35;
    	
    	placesTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	placesTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(1).setHeaderValue("Place name:");
    	placesTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	placesTable.getColumnModel().getColumn(1).setMinWidth(100);
    	placesTable.getColumnModel().getColumn(2).setHeaderValue("AvgT:");
    	placesTable.getColumnModel().getColumn(2).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(2).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(2).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setHeaderValue("MinT:");
    	placesTable.getColumnModel().getColumn(3).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setHeaderValue("MaxT:");
    	placesTable.getColumnModel().getColumn(4).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(5).setHeaderValue("notT:");
    	placesTable.getColumnModel().getColumn(5).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(5).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(5).setMaxWidth(cellSize);
    	
    	placesTable.getColumnModel().getColumn(6).setHeaderValue("stdDev:");
    	placesTable.getColumnModel().getColumn(6).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(6).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(6).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(7).setHeaderValue("S1%");
    	placesTable.getColumnModel().getColumn(7).setPreferredWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(7).setMinWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(7).setMaxWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(8).setHeaderValue("S2%");
    	placesTable.getColumnModel().getColumn(8).setPreferredWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(8).setMinWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(8).setMaxWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(9).setHeaderValue("S3%");
    	placesTable.getColumnModel().getColumn(9).setPreferredWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(9).setMinWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(9).setMaxWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(10).setHeaderValue("S4%");
    	placesTable.getColumnModel().getColumn(10).setPreferredWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(10).setMinWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(10).setMaxWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(11).setHeaderValue("S5%");
    	placesTable.getColumnModel().getColumn(11).setPreferredWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(11).setMinWidth(svaluesCell);
    	placesTable.getColumnModel().getColumn(11).setMaxWidth(svaluesCell);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(placesTable.getModel());
    	placesTable.setRowSorter(sorter);
        
    	placesTable.setName("PlacesTable");
    	placesTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	tableRenderer.setMode(0);
    	placesTable.setDefaultRenderer(Object.class, tableRenderer);

    	try {
    		if(places.size() == 0) {
        		for(int p=0; p<data.placesNumber; p++) 
        			modelPlaces.addNew(data, p, null);
        	} else {
        		int index = -1;
        		for(Place place : places) {
            		index++;
            		modelPlaces.addNew(data, index, place);
            	}
        	}
    	} catch (Exception e) {}
    	
        //action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
    	
        placesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        placesTable.validate();
    }
    
    /**
     * Tworzenie tabeli porównawczej dla miejsc.
     */
    private void createPlacesCompTable() {
    	NetSimulationData dataRef = getCorrectSet(true);
    	NetSimulationData dataComp = getCorrectSet(false);
    	if(dataRef == null || dataComp == null)
    		return;
    	
    	modelPlacesComp = new SimKnockPlacesCompTableModel();
    	placesTable.setModel(modelPlacesComp);

    	int cellSize = 50;
    	int largeCell = 65;
    	//int svaluesCell = 25;
    	
    	placesTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	placesTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(1).setHeaderValue("Place name");
    	placesTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	placesTable.getColumnModel().getColumn(1).setMinWidth(100);
    	placesTable.getColumnModel().getColumn(2).setHeaderValue("AvgTRef");
    	placesTable.getColumnModel().getColumn(2).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(2).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(2).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setHeaderValue("stdDevRef");
    	placesTable.getColumnModel().getColumn(3).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setHeaderValue("AvgTKnock");
    	placesTable.getColumnModel().getColumn(4).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(5).setHeaderValue("stdDevKnock");
    	placesTable.getColumnModel().getColumn(5).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(5).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(5).setMaxWidth(cellSize);
    	
    	placesTable.getColumnModel().getColumn(6).setHeaderValue("diff:");
    	placesTable.getColumnModel().getColumn(6).setPreferredWidth(largeCell+10);
    	placesTable.getColumnModel().getColumn(6).setMinWidth(largeCell+10);
    	placesTable.getColumnModel().getColumn(6).setMaxWidth(largeCell+10);
    	placesTable.getColumnModel().getColumn(7).setHeaderValue("noTok");
    	placesTable.getColumnModel().getColumn(7).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(7).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(7).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(8).setHeaderValue("Sign1");
    	placesTable.getColumnModel().getColumn(8).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(8).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(8).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(9).setHeaderValue("Sign2");
    	placesTable.getColumnModel().getColumn(9).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(9).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(9).setMaxWidth(cellSize);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(placesTable.getModel());
    	placesTable.setRowSorter(sorter);
        
    	placesTable.setName("PlacesCompTable");
    	placesTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	tableRenderer.setMode(1);
    	placesTable.setDefaultRenderer(String.class, tableRenderer);
    	placesTable.setDefaultRenderer(Double.class, tableRenderer);

    	try {
    		if(places.size() == 0) {
        		for(int index=0; index<dataRef.placesNumber; index++) 
        			modelPlacesComp.addNew(dataRef, dataComp, index, null);
        	} else {
        		int index = -1;
        		for(Place place : places) {
            		index++;
            		modelPlacesComp.addNew(dataRef, dataComp, index, place);
            	}
        	}
    	} catch (Exception e) {}
    	
        //action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
    	
        placesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        placesTable.validate();
    }

    /**
	 * Metoda tworząca tabelę tranzycji
	 * @param ref  boolean - true, jeśli wywołane przez zbiór referencyjny
	 */
    private void createTransTable(boolean ref) {
    	NetSimulationData data = getCorrectSet(ref);
    	if(data == null)
    		return;
    	
    	modelTrans = new SimKnockTransTableModel();
    	transTable.setModel(modelTrans);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	int svaluesCell = 35;
    	
    	transTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	transTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(1).setHeaderValue("Place name:");
    	transTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	transTable.getColumnModel().getColumn(1).setMinWidth(100);
    	transTable.getColumnModel().getColumn(2).setHeaderValue("AvgF:");
    	transTable.getColumnModel().getColumn(2).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(2).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(2).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(3).setHeaderValue("MinF:");
    	transTable.getColumnModel().getColumn(3).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(3).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(3).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(4).setHeaderValue("MaxF:");
    	transTable.getColumnModel().getColumn(4).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(4).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(4).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(5).setHeaderValue("notF:");
    	transTable.getColumnModel().getColumn(5).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(5).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(5).setMaxWidth(cellSize);
    	
    	transTable.getColumnModel().getColumn(6).setHeaderValue("stdDev:");
    	transTable.getColumnModel().getColumn(6).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(6).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(6).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(7).setHeaderValue("S1%");
    	transTable.getColumnModel().getColumn(7).setPreferredWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(7).setMinWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(7).setMaxWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(8).setHeaderValue("S2%");
    	transTable.getColumnModel().getColumn(8).setPreferredWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(8).setMinWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(8).setMaxWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(9).setHeaderValue("S3%");
    	transTable.getColumnModel().getColumn(9).setPreferredWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(9).setMinWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(9).setMaxWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(10).setHeaderValue("S4%");
    	transTable.getColumnModel().getColumn(10).setPreferredWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(10).setMinWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(10).setMaxWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(11).setHeaderValue("S5%");
    	transTable.getColumnModel().getColumn(11).setPreferredWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(11).setMinWidth(svaluesCell);
    	transTable.getColumnModel().getColumn(11).setMaxWidth(svaluesCell);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(transTable.getModel());
    	transTable.setRowSorter(sorter);
        
    	transTable.setName("TransitionsTable");
    	transTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	tableRenderer.setMode(0);
    	transTable.setDefaultRenderer(Object.class, tableRenderer);

    	try {
    		if(transitions.size() == 0) {
        		for(int p=0; p<data.transNumber; p++) 
        			modelTrans.addNew(data, p, null);
        	} else {
        		int index = -1;
        		for(Transition trans : transitions) {
            		index++;
            		modelTrans.addNew(data, index, trans);
            	}
        	}
    	} catch (Exception e) {}
    	
        //action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
    	
    	transTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        transTable.validate();
    }
    
    /**
	 * Metoda tworząca tabelę tranzycji w ramach porównania danych.
	 */
    private void createTransCompTable() {
    	NetSimulationData dataRef = getCorrectSet(true);
    	NetSimulationData dataComp = getCorrectSet(false);
    	if(dataRef == null || dataComp == null)
    		return;
    	
    	modelTransComp = new SimKnockTransCompTableModel();
    	transTable.setModel(modelTransComp);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	//int svaluesCell = 25;
    	
    	transTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	transTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(1).setHeaderValue("Place name");
    	transTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	transTable.getColumnModel().getColumn(1).setMinWidth(100);
    	transTable.getColumnModel().getColumn(2).setHeaderValue("AvgFRef");
    	transTable.getColumnModel().getColumn(2).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(2).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(2).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(3).setHeaderValue("stdDevRef");
    	transTable.getColumnModel().getColumn(3).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(3).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(3).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(4).setHeaderValue("AvgFKnock");
    	transTable.getColumnModel().getColumn(4).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(4).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(4).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(5).setHeaderValue("stdDevKnock");
    	transTable.getColumnModel().getColumn(5).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(5).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(5).setMaxWidth(cellSize);
    	
    	transTable.getColumnModel().getColumn(6).setHeaderValue("diff:");
    	transTable.getColumnModel().getColumn(6).setPreferredWidth(largeCell+10);
    	transTable.getColumnModel().getColumn(6).setMinWidth(largeCell+10);
    	transTable.getColumnModel().getColumn(6).setMaxWidth(largeCell+10);
    	transTable.getColumnModel().getColumn(7).setHeaderValue("noFire");
    	transTable.getColumnModel().getColumn(7).setPreferredWidth(largeCell);
    	transTable.getColumnModel().getColumn(7).setMinWidth(largeCell);
    	transTable.getColumnModel().getColumn(7).setMaxWidth(largeCell);
    	transTable.getColumnModel().getColumn(8).setHeaderValue("Sign1");
    	transTable.getColumnModel().getColumn(8).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(8).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(8).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(9).setHeaderValue("Sign2");
    	transTable.getColumnModel().getColumn(9).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(9).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(9).setMaxWidth(cellSize);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(transTable.getModel());
    	transTable.setRowSorter(sorter);
        
    	transTable.setName("TransitionsCompTable");
    	transTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	tableRenderer.setMode(1);
    	transTable.setDefaultRenderer(String.class, tableRenderer);
    	transTable.setDefaultRenderer(Double.class, tableRenderer);
    	

    	try {
    		if(transitions.size() == 0) {
        		for(int index=0; index<dataRef.transNumber; index++) 
        			modelTransComp.addNew(dataRef, dataComp, index, null);
        	} else {
        		int index = -1;
        		for(Transition trans : transitions) {
            		index++;
            		modelTransComp.addNew(dataRef, dataComp, index, trans);
            	}
        	}
    	} catch (Exception e) {}
    	
        //action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
    	
    	transTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        transTable.validate();
    }
    
    /**
     * Metoda tworzy tabele efektu knockout dla T i P dla całej serii tranzycji sieci.
     * @param selected int - ID serii
     */
    private void createCompareAllTables(int selected) {
    	long IDseries = pn.accessSimKnockoutData().accessSeries().get(selected);
    	ArrayList<NetSimulationData> dataPackage = pn.accessSimKnockoutData().getSeriesDatasets(IDseries);
    	int selRef = referencesCombo.getSelectedIndex() - 1;
    	if(selRef == -1) {
    		JOptionPane.showMessageDialog(null,
					"Please select reference set!", 
					"Reference selection", JOptionPane.WARNING_MESSAGE);
    		seriesCombo.setSelectedIndex(0);
    		return;
    	}
    	NetSimulationData refSet = pn.accessSimKnockoutData().getReferenceSet(selRef);
    	if(dataPackage == null) {
    		return;
    	}
    	
    	int transNumber = dataPackage.get(0).transNumber;
    	modelTransCompAll = new SimKnockTransCompAllTableModel(transNumber);
    	transCompAllTable.setModel(modelTransCompAll);
    	transCompAllTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	transCompAllTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    	transCompAllTable.getColumnModel().getColumn(0).setMinWidth(40);
    	transCompAllTable.getColumnModel().getColumn(0).setMaxWidth(40);
    	transCompAllTable.getColumnModel().getColumn(1).setHeaderValue("Transition name");
    	transCompAllTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	transCompAllTable.getColumnModel().getColumn(1).setMinWidth(100);
    	transCompAllTable.getColumnModel().getColumn(2).setHeaderValue("Knocked");
    	transCompAllTable.getColumnModel().getColumn(2).setPreferredWidth(50);
    	transCompAllTable.getColumnModel().getColumn(2).setMinWidth(50);
    	transCompAllTable.getColumnModel().getColumn(2).setMaxWidth(50);
    	for(int i=0; i<transNumber; i++) {
    		transCompAllTable.getColumnModel().getColumn(i+3).setHeaderValue("t"+i);
        }
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(transCompAllTable.getModel());
    	transCompAllTable.setRowSorter(sorter);
        
    	transCompAllTable.setName("TransitionsCompAllTable");
    	transCompAllTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	SimKnockTableRenderer tableRendererTransitions = new SimKnockTableRenderer(transCompAllTable);
    	tableRendererTransitions.setMode(2);
    	transCompAllTable.setDefaultRenderer(String.class, tableRendererTransitions);
    	transCompAllTable.setDefaultRenderer(Double.class, tableRendererTransitions);
    	transCompAllTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	transCompAllTable.validate();
    	resizeColumnWidth(transCompAllTable);

    	
    	int placesNumber = dataPackage.get(0).placesNumber;
    	modelPlacesCompAll = new SimKnockPlacesCompAllTableModel(placesNumber);
    	placesCompAllTable.setModel(modelPlacesCompAll);
    	
    	placesCompAllTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	placesCompAllTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    	placesCompAllTable.getColumnModel().getColumn(0).setMinWidth(40);
    	placesCompAllTable.getColumnModel().getColumn(0).setMaxWidth(40);
    	placesCompAllTable.getColumnModel().getColumn(1).setHeaderValue("Transition name");
    	placesCompAllTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	placesCompAllTable.getColumnModel().getColumn(1).setMinWidth(100);
    	placesCompAllTable.getColumnModel().getColumn(2).setHeaderValue("Disabled");
    	placesCompAllTable.getColumnModel().getColumn(2).setPreferredWidth(50);
    	placesCompAllTable.getColumnModel().getColumn(2).setMinWidth(50);
    	placesCompAllTable.getColumnModel().getColumn(2).setMaxWidth(50);
    	for(int i=0; i<placesNumber; i++) {
    		placesCompAllTable.getColumnModel().getColumn(i+3).setHeaderValue("p"+i);
        }
    	TableRowSorter<TableModel> sorterPlaces  = new TableRowSorter<TableModel>(placesCompAllTable.getModel());
    	placesCompAllTable.setRowSorter(sorterPlaces);
        
    	placesCompAllTable.setName("PlacesCompAllTable");
    	placesCompAllTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	SimKnockTableRenderer tableRendererPlaces = new SimKnockTableRenderer(placesCompAllTable);
    	tableRendererPlaces.setMode(2);
    	placesCompAllTable.setDefaultRenderer(String.class, tableRendererPlaces);
    	placesCompAllTable.setDefaultRenderer(Double.class, tableRendererPlaces);
    	placesCompAllTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	placesCompAllTable.validate();
    	resizeColumnWidth(placesCompAllTable);
    	
    	
    	//TODO:
    	modelTransCompAll.tTableData = new ArrayList<>();
    	modelPlacesCompAll.pTableData = new ArrayList<>();
    	for(int t=0; t<transNumber; t++) {
    		ArrayList<String> pVector = new ArrayList<>();
    		ArrayList<String> tVector = new ArrayList<>();
    		ArrayList<DetailsPlace> pTableVector = new ArrayList<DetailsPlace>();
    		ArrayList<DetailsTrans> tTableVector = new ArrayList<DetailsTrans>();
    		NetSimulationData dataSet = dataPackage.get(t);
    		
    		//ID:
    		pVector.add(""+t);
    		tVector.add(""+t);
    		pTableVector.add(null);
    		tTableVector.add(null);
    		
    		//NAZWY:
    		String name = "";
    		if(transitions.size() == 0) {
    			name = "t"+t;
    		} else {
    			name = "t"+transitions.get(t).getName();
    		}
    		pVector.add(name);
			tVector.add(name);
    		pTableVector.add(null);
    		tTableVector.add(null);
    		
    		//KNOCKED: (liczone później)
    		pVector.add("0");
    		tVector.add("0");
    		pTableVector.add(null);
    		tTableVector.add(null);
    		
    		//DANE:
    		for(int t1=0; t1<transNumber; t1++) {
    			DetailsTrans details = modelTransCompAll.newDetailsInstance();
    			details.knockAvgFiring = dataSet.transFiringsAvg.get(t1);
    			details.refAvgFiring = refSet.transFiringsAvg.get(t1);
    			details.knockDisabled = dataSet.transZeroFiring.get(t1);
    			details.significance1 = getTransSign1(refSet, dataSet, t1);
    			details.significance2 = getTransSign2(refSet, dataSet, t1);
    			details.diff = 0;
    			
    			if(refSet.transFiringsAvg.get(t1) == 0 && dataSet.transFiringsAvg.get(t1) == 0) {
    				tVector.add("999990.0");
    				tTableVector.add(details);
    				continue;
    			}
    			if(refSet.transFiringsAvg.get(t1) == 0 && dataSet.transFiringsAvg.get(t1) > 0) {
    				tVector.add("+999999.0");
    				tTableVector.add(details);
    				continue;
    			}
    			if(refSet.transFiringsAvg.get(t1) > 0 && dataSet.transFiringsAvg.get(t1) == 0) {
    				tVector.add("-999999.0");
    				tTableVector.add(details);
    				continue;
    			}
    			
    			double diff = details.refAvgFiring - details.knockAvgFiring;
    			diff = (diff / details.refAvgFiring)*100;
    			if(diff < 0) { //wzrosło w stos. do ref
    				diff *= -1;
    				details.diff = diff;
    				tVector.add(formatter2.format(diff));
    				tTableVector.add(details);
    			} else {
    				diff *= -1;
    				details.diff = diff;
    				tVector.add(formatter2.format(diff));
    				tTableVector.add(details);
    			}
    		}
    		
    		for(int p=0; p<placesNumber; p++) {
    			DetailsPlace details = modelPlacesCompAll.newDetailsInstance();
    			details.knockAvgTokens = dataSet.placeTokensAvg.get(p);
    			details.refAvgTokens = refSet.placeTokensAvg.get(p);
    			details.knockDisabled = dataSet.placeZeroTokens.get(p);
    			details.significance1 = getPlaceSign1(refSet, dataSet, p);
    			details.significance2 = getPlaceSign2(refSet, dataSet, p);
    			
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				pVector.add("999990.0");
    				pTableVector.add(details);
    				continue;
    			}
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) > 0) {
    				pVector.add("999999.0");
    				pTableVector.add(details);
    				continue;
    			}
    			if(refSet.placeTokensAvg.get(p) > 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				pVector.add("-999999.0");
    				pTableVector.add(details);
    				continue;
    			}
    			
    			double diff = details.refAvgTokens - details.knockAvgTokens;
    			diff = (diff / details.refAvgTokens)*100;
    			if(diff < 0) { //wzrosło w stos. do ref
    				diff *= -1;
    				details.diff = diff;
    				pVector.add(formatter2.format(diff));
    				pTableVector.add(details);
    			} else {
    				diff *= -1;
    				details.diff = diff;
    				pVector.add(formatter2.format(diff));
    				pTableVector.add(details);
    			}
    		}
    		if(tVector.size() != tTableVector.size() || pVector.size() != pTableVector.size()) {
    			@SuppressWarnings("unused")
				int x=1;
    		}
    		
    		modelTransCompAll.addNew(tVector);
    		modelTransCompAll.tTableData.add(tTableVector);
    		modelPlacesCompAll.addNew(pVector);
    		modelPlacesCompAll.pTableData.add(pTableVector);
    	}
    }
    
    /**
     * Tworzy tabele porównania serii danych ze zbiorem referencyjnym - w formie tekstowej.
     * @param selected int - ID serii
     */
    private void createCompareAllTablesNotepad(int selected) {
    	long IDseries = pn.accessSimKnockoutData().accessSeries().get(selected);
    	ArrayList<NetSimulationData> dataPackage = pn.accessSimKnockoutData().getSeriesDatasets(IDseries);
    	int selRef = referencesCombo.getSelectedIndex() - 1;
    	if(selRef == -1) {
    		JOptionPane.showMessageDialog(null,
					"Please select reference set!", 
					"Reference selection", JOptionPane.WARNING_MESSAGE);
    		seriesCombo.setSelectedIndex(0);
    		return;
    	}
    	NetSimulationData refSet = pn.accessSimKnockoutData().getReferenceSet(selRef);
    	if(dataPackage == null) {
    		return;
    	}
    	
    	int transNumber = dataPackage.get(0).transNumber;
    	int placesNumber = dataPackage.get(0).placesNumber;
    	if(transNumber == 0 || placesNumber == 0)
    		return;
    	
    	HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		notePad.addTextLineNL("Data series "+selected+" analysis:", "text");
		notePad.addTextLineNL(" +/-20% treshold for increased/decreased firing of transitions ", "text");
		notePad.addTextLineNL("===============================================================================", "text");
		notePad.addTextLineNL("", "text");
    	//TODO:
    	for(int t=0; t<transNumber; t++) {
    		NetSimulationData dataSet = dataPackage.get(t);

    		notePad.addTextLineNL("*** t"+t+"_"+transitions.get(t).getName()+"    disabled manually. Impact on the net:", "text");
    		ArrayList<Integer> transVector = new ArrayList<>();
    		for(int t1=0; t1<transNumber; t1++) {
    			if(t == t1)
    				transVector.add(0);
    			else
    				transVector.add(1);
    		}
    		
    		for(int t1=0; t1<transNumber; t1++) {
    			if(transVector.get(t1) == 0)
    				continue;

    			if(refSet.transFiringsAvg.get(t1) == 0 && dataSet.transFiringsAvg.get(t1) == 0) {
    				notePad.addTextLineNL("      (DEAD IN REF & SERIES SETS!) t"+t1+"_"+transitions.get(t1).getName()+"", "text");
    				transVector.set(t1, 0);
    				continue;
    			}
    			
    			if(refSet.transFiringsAvg.get(t1) == 0 && dataSet.transFiringsAvg.get(t1) > 0) {
    				double value = dataSet.transFiringsAvg.get(t1) * 100;
    				notePad.addTextLineNL("      (DEAD IN REF, ALIVE IN SERIES) [avg fire chance: "
    						+formatter2.format(value)+"%]  t"+t1+"_"+transitions.get(t1).getName(), "text");
    				transVector.set(t1, 0);
    				continue;
    			}
    			if(refSet.transFiringsAvg.get(t1) > 0 && dataSet.transFiringsAvg.get(t1) == 0) {
    				double value = refSet.transFiringsAvg.get(t1) * 100;
    				notePad.addTextLineNL("      (KNOCKED OUT IN SERIES SET) [avg fired chance in reference: "
    						+formatter2.format(value)+"%]  t"+t1+"_"+transitions.get(t1).getName(), "text");
    				transVector.set(t1, 0);
    				continue;
    			}
    		}
    		
    		TreeMap<Double, String> data = new TreeMap<Double, String>();
    		for(int t1=0; t1<transNumber; t1++) {
    			if(transVector.get(t1) == 0)
    				continue;
    		
    			double diff = refSet.transFiringsAvg.get(t1) - dataSet.transFiringsAvg.get(t1);
    			diff = (diff / refSet.transFiringsAvg.get(t1));
    			if(diff < 0) { //wzrosło w stos. do ref
    				diff *= -1;
    				double value = diff * 100;
					data.put(value, "t"+t1+"_"+transitions.get(t1).getName());
    				transVector.set(t1, 0);
    				continue;
    			} else {
    				diff *= -1;
    				double value = diff * 100;
					data.put(value, "t"+t1+"_"+transitions.get(t1).getName());
    				transVector.set(t1, 0);
    				continue;
    			}
    		}

    		for(Double key: data.keySet()){
    			if(key < -20) {
    				notePad.addTextLineNL("      (DECREASED) [avg fired chance: "+formatter1.format(key)+"%] "
    						+data.get(key), "text");
    			} 
    			
    			if(key > 20) {
    				notePad.addTextLineNL("      (INCREASED) [avg fired chance: +"+formatter1.format(key)+"%] "
    						+data.get(key), "text");
    			}

            }
    			
    			
    		/*
    		
    		for(int p=0; p<placesNumber; p++) {
    			DetailsPlace details = modelPlacesCompAll.newDetailsInstance();
    			details.knockAvgTokens = dataSet.placeTokensAvg.get(p);
    			details.refAvgTokens = refSet.placeTokensAvg.get(p);
    			details.knockDisabled = dataSet.placeZeroTokens.get(p);
    			details.significance1 = getPlaceSign1(refSet, dataSet, p);
    			details.significance2 = getPlaceSign2(refSet, dataSet, p);
    			
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				pVector.add("999990.0");
    				pTableVector.add(details);
    				continue;
    			}
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) > 0) {
    				pVector.add("999999.0");
    				pTableVector.add(details);
    				continue;
    			}
    			if(refSet.placeTokensAvg.get(p) > 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				pVector.add("-999999.0");
    				pTableVector.add(details);
    				continue;
    			}
    			
    			double diff = details.refAvgTokens - details.knockAvgTokens;
    			diff = (diff / details.refAvgTokens)*100;
    			if(diff < 0) { //wzrosło w stos. do ref
    				diff *= -1;
    				details.diff = diff;
    				pVector.add(formatter2.format(diff));
    				pTableVector.add(details);
    			} else {
    				diff *= -1;
    				details.diff = diff;
    				pVector.add(formatter2.format(diff));
    				pTableVector.add(details);
    			}
    		}
    		if(tVector.size() != tTableVector.size() || pVector.size() != pTableVector.size()) {
    			@SuppressWarnings("unused")
				int x=1;
    		}
    		*/
    	}
    	notePad.addTextLineNL("", "text");
    	notePad.addTextLineNL("", "text");
    	notePad.addTextLineNL("", "text");
    	notePad.addTextLineNL("===============================================================================", "text");
		notePad.addTextLineNL(" +/-20% treshold for increased/decreased tokens in places ", "text");
		notePad.addTextLineNL("===============================================================================", "text");
		notePad.addTextLineNL("", "text");
    	//TODO:
    	for(int t=0; t<transNumber; t++) {
    		NetSimulationData dataSet = dataPackage.get(t);

    		notePad.addTextLineNL("*** t"+t+"_"+transitions.get(t).getName()+"    disabled manually. Impact on the net:", "text");
    		ArrayList<Integer> placesVector = new ArrayList<>();
    		for(int p=0; p<placesNumber; p++) {
    			placesVector.add(1);
    		}
    		
    		for(int p=0; p<placesNumber; p++) {
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				notePad.addTextLineNL("      (DEAD IN REF & SERIES SETS!) p"+p+"_"+places.get(p).getName()+"", "text");
    				placesVector.set(p, 0);
    				continue;
    			}
    			
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) > 0) {
    				double value = dataSet.placeTokensAvg.get(p);
    				notePad.addTextLineNL("      (DEAD IN REF, ALIVE IN SERIES) [avg tokens: "
    						+formatter3.format(value)+"]  p"+p+"_"+places.get(p).getName(), "text");
    				placesVector.set(p, 0);
    				continue;
    			}
    			if(refSet.placeTokensAvg.get(p) > 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				double value = refSet.placeTokensAvg.get(p);
    				notePad.addTextLineNL("      (KNOCKED OUT IN SERIES SET) [avg tokens reference: "
    						+formatter3.format(value)+"]  p"+p+"_"+places.get(p).getName(), "text");
    				placesVector.set(p, 0);
    				continue;
    			}
    		}
    		
    		TreeMap<Double, String> data = new TreeMap<Double, String>();
    		for(int p=0; p<placesNumber; p++) {
    			if(placesVector.get(p) == 0)
    				continue;
    		
    			double diff = refSet.placeTokensAvg.get(p) - dataSet.placeTokensAvg.get(p);
    			diff = (diff / refSet.placeTokensAvg.get(p));
    			if(diff < 0) { //wzrosło w stos. do ref
    				diff *= -1;
    				double value = diff * 100;
					data.put(value, "p"+p+"_"+places.get(p).getName());
					placesVector.set(p, 0);
    				continue;
    			} else {
    				diff *= -1;
    				double value = diff * 100;
					data.put(value, "p"+p+"_"+places.get(p).getName());
					placesVector.set(p, 0);
    				continue;
    			}
    		}

    		for(Double key: data.keySet()){
    			if(key < -20) {
    				notePad.addTextLineNL("      (DECREASED) [tokens change: "+formatter2.format(key)+"%] "
    						+data.get(key), "text");
    			} 
    			
    			if(key > 20) {
    				notePad.addTextLineNL("      (INCREASED) [tokens change: +"+formatter2.format(key)+"%] "
    						+data.get(key), "text");
    			}

            }
    	}
    }
    
    /**
     * Obliczanie I stopnia znaczenia danych - min. największego paska > max. niższego dla tranzycji
     * @param refSet NetSimulationData - zbiór referencyjny
     * @param dataSet NetSimulationData - zbiór knockout
     * @param index int - indeks tranzycji
     * @return boolean - true, jeśli min > max
     */
    private boolean getTransSign1(NetSimulationData refSet, NetSimulationData dataSet, int index) {
    	if(refSet.transFiringsAvg.get(index) > dataSet.transFiringsAvg.get(index)) {
    		if(refSet.transFiringsMin.get(index) > dataSet.transFiringsMax.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	} else {
    		if(refSet.transFiringsMax.get(index) < dataSet.transFiringsMin.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    }
    
    /**
     * Obliczanie II stopnia znaczenia danych - stdDev - dla tranzycji
     * @param refSet NetSimulationData - zbiór referencyjny
     * @param dataSet NetSimulationData - zbiór knockout
     * @param index int - indeks tranzycji
     * @return boolean - true, jeśli stdDev się nie nakładają
     */
    private boolean getTransSign2(NetSimulationData refSet, NetSimulationData dataSet, int index) {
    	if(refSet.transFiringsAvg.get(index) > dataSet.transFiringsAvg.get(index)) {
    		if(refSet.transFiringsAvg.get(index) - refSet.transStdDev.get(index) > dataSet.transFiringsAvg.get(index) + dataSet.transStdDev.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	} else { //ref mniejsze niż knock
    		if(refSet.transFiringsAvg.get(index) + refSet.transStdDev.get(index) < dataSet.transFiringsAvg.get(index) - dataSet.transStdDev.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    }
    
    /**
     * Obliczanie I stopnia znaczenia danych - min. największego paska > max. niższego dla miejsc
     * @param refSet NetSimulationData - zbiór referencyjny
     * @param dataSet NetSimulationData - zbiór knockout
     * @param index int - indeks miejsca
     * @return boolean - true, jeśli min > max
     */
    private boolean getPlaceSign1(NetSimulationData refSet, NetSimulationData dataSet, int index) {
    	if(refSet.placeTokensAvg.get(index) > dataSet.placeTokensAvg.get(index)) {
    		if(refSet.placeTokensMin.get(index) > dataSet.placeTokensMax.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	} else {
    		if(refSet.placeTokensMax.get(index) < dataSet.placeTokensMin.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    }
    
    /**
     * Obliczanie II stopnia znaczenia danych - stdDev - dla miejsc
     * @param refSet NetSimulationData - zbiór referencyjny
     * @param dataSet NetSimulationData - zbiór knockout
     * @param index int - indeks miejsca
     * @return boolean - true, jeśli stdDev się nie nakładają
     */
    private boolean getPlaceSign2(NetSimulationData refSet, NetSimulationData dataSet, int index) {
    	if(refSet.placeTokensAvg.get(index) > dataSet.placeTokensAvg.get(index)) {
    		if(refSet.placeTokensAvg.get(index) - refSet.placeStdDev.get(index) > dataSet.placeTokensAvg.get(index) + dataSet.placeStdDev.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	} else {
    		if(refSet.placeTokensAvg.get(index) + refSet.placeStdDev.get(index) < dataSet.placeTokensAvg.get(index) - dataSet.placeStdDev.get(index)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    }
    
    /**
     * Metoda ustawia domyslną szerokość kolumn tabeli.
     * @param table JTable - tablica danych
     */
    public void resizeColumnWidth(JTable table) {
	    TableColumnModel columnModel = table.getColumnModel();
	    for (int column = 3; column < table.getColumnCount(); column++) {
	        columnModel.getColumn(column).setPreferredWidth(65);
	        columnModel.getColumn(column).setMinWidth(65);
	        columnModel.getColumn(column).setMaxWidth(65);
	    }
	}
    
  //******************************************************************************************************
  //******************************************************************************************************
  //******************************************************************************************************
    
	/**
	 * Inicjalizacja agentów nasłuchujących.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComponents();
  	  	    }  
    	});
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	boss.setEnabled(true);
		    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		    }
		});
    }
    
    /**
     * Metoda odpowiedzialna za wypełnianie komponentów okna (aktywacja: actionListener)
     */
    private void fillComponents() {
    	PetriNet pn = overlord.getWorkspace().getProject();
		
		//reference data:
		ArrayList<NetSimulationData> references = pn.accessSimKnockoutData().accessReferenceSets();
		int refSize = references.size();
		doNotUpdate = true;
		int oldSelected = 0;
		oldSelected = referencesCombo.getSelectedIndex();
		referencesCombo.removeAllItems();
		referencesCombo.addItem(" ----- ");
		if(refSize > 0) {
			for(int r=0; r<refSize; r++) {
				String disTxt = "Disabled: ";
				for(int t : references.get(r).disabledTransitionsIDs) {
					disTxt += "t"+t+", ";
				}
				for(int t : references.get(r).disabledMCTids) {
					disTxt += "MCT"+(t+1)+", ";
				}
				disTxt = disTxt.replace(", ", " ");
				
				String name = "Data set:"+r+":    "+disTxt+"     NetMode:"+references.get(r).netSimType+
						"   MaxMode:"+references.get(r).maxMode;
				
				//String name = "Ref:"+r+" Date: "+references.get(r).date+" NetMode:"+references.get(r).netSimType+
				//		" MaxMode:"+references.get(r).maxMode;
				referencesCombo.addItem(name);
			}
			
			if(oldSelected < referencesCombo.getItemCount())
				referencesCombo.setSelectedIndex(oldSelected);
			else
				referencesCombo.setSelectedIndex(0);
		}
		
		
		//knockout data:
		ArrayList<NetSimulationData> knockout = pn.accessSimKnockoutData().accessKnockoutDataSets();
		int knockSize = knockout.size();
		int oldKnockSelected = 0;
		oldKnockSelected = dataCombo.getSelectedIndex();
		dataCombo.removeAllItems();
		dataCombo.addItem(" ----- ");
		if(knockSize > 0) {
			for(int r=0; r<knockSize; r++) {
				
				String disTxt = "Disabled: ";
				for(int t : knockout.get(r).disabledTransitionsIDs) {
					disTxt += "t"+t+", ";
				}
				for(int t : knockout.get(r).disabledMCTids) {
					disTxt += "MCT"+(t+1)+", ";
				}
				disTxt = disTxt.replace(", ", " ");
				
				String name = "Data set:"+r+":    "+disTxt+"     NetMode:"+knockout.get(r).netSimType+
						"   MaxMode:"+knockout.get(r).maxMode;
				
				//String name = "Data set:"+r+" Date: "+knockout.get(r).date+" NetMode:"+knockout.get(r).netSimType+
				//		" MaxMode:"+knockout.get(r).maxMode;
				dataCombo.addItem(name);
			}
	
			if(oldKnockSelected < dataCombo.getItemCount())
				dataCombo.setSelectedIndex(oldKnockSelected);
			else
				dataCombo.setSelectedIndex(0);
		}
		
		
		//Seires daya
		ArrayList<Long> series = pn.accessSimKnockoutData().accessSeries();
		int seriesSize = series.size();
		int oldSeriesSelected = 0;
		oldSeriesSelected = seriesCombo.getSelectedIndex();
		seriesCombo.removeAllItems();
		seriesCombo.addItem(" ----- ");
		if(seriesSize > 0) {
			for(int s=0; s<seriesSize; s++) {
				long IDseries = series.get(s);
				NetSimulationData representant = pn.accessSimKnockoutData().returnSeriesFirst(IDseries);	
				String disTxt = "Package: "+s+" Steps: "+representant.steps+" Reps: "+representant.reps;
				seriesCombo.addItem(disTxt);
			}
	
			if(oldSeriesSelected < seriesCombo.getItemCount())
				seriesCombo.setSelectedIndex(oldSeriesSelected);
			else
				seriesCombo.setSelectedIndex(0);
		}
		
		doNotUpdate = false;
	}


	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	
    protected void cellClickAction(JTable table) {
		try {
			String name = table.getName();
			if(name.contains("Places")) {
				
	  	    	int row = table.getSelectedRow();
	  	    	int index = Integer.parseInt(table.getValueAt(row, 0).toString());
	  	    	
	  	    	HolmesNodeInfo window = new HolmesNodeInfo(
	  	    			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().get(index), this);
	  	    	window.setVisible(true);
			} else if(name.contains("Transitions")){
				int row = table.getSelectedRow();
	  	    	int index = Integer.parseInt(table.getValueAt(row, 0).toString());
	  	    	
	  	    	HolmesNodeInfo window = new HolmesNodeInfo(
	  	    			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(index), this);
	  	    	window.setVisible(true);
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Klasa odpowiedzialna za informacje o wskazanym pasku wykresu
	 * @author MR
	 *
	 */
	public class CustomToolTipGenerator implements CategoryToolTipGenerator  {
		boolean isPlaces = false;
		boolean compareMode = false;
		ArrayList<Place> places;
		ArrayList<Transition> transitions;
		NetSimulationData refData;
		NetSimulationData knockData;
		
		DecimalFormat formatter;
		
		boolean simpleMode = false;
		/**
		 * Konstruktor główny.
		 * @param isPlaces boolean - true, jeśli wykres miejsc
		 * @param compareMode boolean - true, jeśli wykres porównawczy
		 */
		@SuppressWarnings("unchecked")
		public CustomToolTipGenerator(boolean isPlaces, boolean compareMode, Object... blackBox) {
			this.isPlaces = isPlaces;
			this.compareMode = compareMode;
			
			places = (ArrayList<Place>) blackBox[0];
			transitions = (ArrayList<Transition>) blackBox[1];
			refData = (NetSimulationData) blackBox[2];
			knockData = (NetSimulationData) blackBox[3];
			
			formatter = new DecimalFormat("#.###");
			
			if(places.size() != refData.placesNumber || transitions.size() != refData.transNumber) {
				simpleMode = true;
			}
		}
		
		/**
		 * Metoda przeciażona, odpowiedzialna za wyświetlanie informacji po wskazaniu paska wykresu.
		 * @param dataset CategoryDataset - zbió danych wykresu
		 * @param bar int - nr paska
		 * @param nodeIndex int - nr kategorii 
		 */
		@Override
	    public String generateToolTip(CategoryDataset dataset, int bar, int nodeIndex)   {
	    	String text = "<html><font size=\"5\">";
	    	text += "Simulate steps: <b>"+refData.steps+"</b> Repetitions: <b>"+refData.reps+"</b><br>";
	    	if(compareMode) { //compare ref and knockout:
	    		if(isPlaces) {
	    			if(simpleMode) {
	    				text += "Place: p"+nodeIndex;
	    			} else {
	    				text += "Place: p"+nodeIndex+"_"+places.get(nodeIndex).getName();
	    			}
	    			double tokensRef = refData.placeTokensAvg.get(nodeIndex);
	    			text += "<br>Reference tokens: <b>"+formatter.format(tokensRef);
	    			text += "</b> StdDev: <b>"+formatter.format(refData.placeStdDev.get(nodeIndex))+"</b>";
	    			
	    			double tokensKnock = knockData.placeTokensAvg.get(nodeIndex);
	    			text += "<br>Knockout tokens: <b>"+formatter.format(tokensKnock);
	    			text += "</b> StdDev: <b>"+formatter.format(knockData.placeStdDev.get(nodeIndex))+"</b>";
	    			
	    			
		    	} else {
		    		if(simpleMode) {
		    			text += "Transition: t"+nodeIndex;
	    			} else {
	    				text += "Transition: t"+nodeIndex+"_"+transitions.get(nodeIndex).getName();
	    			}
		    		double firingRef = refData.transFiringsAvg.get(nodeIndex);
	    			text += "<br>Reference firing: <b>"+formatter.format(firingRef);
	    			text += "</b> StdDev: <b>"+formatter.format(refData.transStdDev.get(nodeIndex))+"</b>";
	    			
	    			double firingKnock = knockData.transFiringsAvg.get(nodeIndex);
	    			text += "<br>Knockout firing: <b>"+formatter.format(firingKnock);
	    			text += "</b> StdDev: <b>"+formatter.format(knockData.transStdDev.get(nodeIndex))+"</b>";
	    			
	    			//dodatkowe info o wyłączonej tranzycji:
	    			if(bar != 0) {
	    				if(knockData.disabledTotals.contains(nodeIndex)) {
		    				text += "<br><font color=\"green\">Transition disabled manually in simulation</font> ";
		    			} else {
		    				if(firingKnock == 0) {
		    					text += "<br><font color=\"red\">Transition starved because of other disabled transition(s)</font><br><b>";
		    					for(int t : knockData.disabledTotals)
		    						text += "t"+t+" ";
		    					text += "</b>";
		    				}
		    			}
	    			}
		    	}
	    	} else { //single data mode:
	    		if(isPlaces) {
	    			if(simpleMode) {
	    				text += "Place: p"+nodeIndex;
	    			} else {
	    				text += "Place: p"+nodeIndex+"_"+places.get(nodeIndex).getName();
	    			}
	    			double tokensRef = refData.placeTokensAvg.get(nodeIndex);
	    			text += "<br>Tokens: <b>"+formatter.format(tokensRef);
	    			text += "</b> StdDev: <b>"+formatter.format(refData.placeStdDev.get(nodeIndex))+"</b>";
		    	} else {
		    		if(simpleMode) {
		    			text += "Transition: t"+nodeIndex;
	    			} else {
	    				text += "Transition: t"+nodeIndex+"_"+transitions.get(nodeIndex).getName();
	    			}
		    		double firingRef = refData.transFiringsAvg.get(nodeIndex);
	    			text += "<br>Reference firing: <b>"+formatter.format(firingRef);
	    			text += "</b> StdDev: <b>"+formatter.format(refData.transStdDev.get(nodeIndex))+"</b>";
	    			
	    			//dodatkowe info o wyłączonej tranzycji:
	    			if(bar != 0) {
	    				if(refData.disabledTotals.contains(nodeIndex)) {
		    				text += "<br><font color=\"green\">Transition disabled manually in simulation</font> ";
		    			} else {
		    				if(firingRef == 0) {
		    					text += "<br><font color=\"red\">Transition starved because of other disabled transition(s):</font><br><b>";
		    					for(int t : refData.disabledTotals)
		    						text += "t"+t+" ";
		    					text += "</b>";
		    				}
		    			}
	    			}
		    	}
	    	}
	    	text += "</font></html>";
	    	return text;
	    }
	}
}
