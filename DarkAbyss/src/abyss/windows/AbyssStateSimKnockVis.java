package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.tools.SimpleJavaFileObject;

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

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.NetSimulationData;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.tables.PTITableRenderer;
import abyss.tables.PlacesTableModel;
import abyss.tables.SimKnockPlacesTableModel;
import abyss.tables.SimKnockTableRenderer;
import abyss.tables.SimKnockTransTableModel;
import abyss.utilities.Tools;

/**
 * Klasa okna przeglądania danych symulacji knockout.
 * 
 * @author MR
 *
 */
public class AbyssStateSimKnockVis extends JFrame {
	private static final long serialVersionUID = 3020186160500907678L;
	private GUIManager overlord;
	private PetriNet pn;
	private boolean doNotUpdate = false;
	private AbyssStateSim boss;
	private XYSeriesCollection placesSeriesDataSet = null;
	private XYSeriesCollection transitionsSeriesDataSet = null;
	private JFreeChart placesChart;
	private JFreeChart transitionsChart;
	private JPanel placesChartPanel;
	private JPanel transitionsChartPanel;
	
	private JComboBox<String> referencesCombo = null;
	private JComboBox<String> dataCombo = null;
	
	private boolean singleMode = true; //tylko jeden pakiet z combobox; false = porównywanie
	private ArrayList<Place> places;
	private ArrayList<Transition> transitions;
	
	//tablice:
	private DefaultTableModel model;
	private SimKnockPlacesTableModel modelPlaces;
	private SimKnockTransTableModel modelTrans;
	private SimKnockTableRenderer tableRenderer;
	private JTable placesTable;
	private JTable transTable;
	/**
	 * Konstruktor obiektu klast AbyssStateSimKnockVis.
	 * @param boss AbyssStateSim - główne okno symulatora
	 */
	public AbyssStateSimKnockVis(AbyssStateSim boss) {
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
		tabbedPane.addTab("Charts", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createPlacesChartsPanel(), "Charts");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Place Table", Tools.getResIcon16("/icons/stateSim/transDyn.png"), createPlacesTablePanel(), "Place Table");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		tabbedPane.addTab("Transition Table", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createTransTablePanel(), "Transition Table");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		
		mainTabbedPanel.add(tabbedPane);
		
		main.add(mainTabbedPanel, BorderLayout.CENTER);
		
		this.boss.setEnabled(false);
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
		referencesCombo.setBounds(posXda+80, posYda, 500, 20);
		referencesCombo.setMaximumRowCount(12);
		referencesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
				
				showSingleKnockoutCharts(true);
				createPlacesTable(true);
				createTransTable(true);
			}
			
		});
		result.add(referencesCombo);
		
		JLabel label2 = new JLabel("Data sets:");
		label2.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label2);
		
		String[] data2 = { " ----- " };
		dataCombo = new JComboBox<String>(data2); //final, aby listener przycisku odczytał wartość
		dataCombo.setBounds(posXda+80, posYda, 500, 20);
		dataCombo.setMaximumRowCount(12);
		dataCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
				
				showSingleKnockoutCharts(false);
				createPlacesTable(false);
				createTransTable(false);
				//int selected = dataCombo.getSelectedIndex();
			}
			
		});
		result.add(dataCombo);
		
		JButton showChartKnockButton = new JButton("Compare");
		showChartKnockButton.setBounds(posXda+600, posYda-20, 120, 40);
		showChartKnockButton.setMargin(new Insets(0, 0, 0, 0));
		showChartKnockButton.setIcon(Tools.getResIcon16("/icons/stateSim/g.png"));
		showChartKnockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showCompareCharts();
			}
		});
		result.add(showChartKnockButton);
		
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

	/**
	 * Tworzy panel tablic
	 * @return JPanel - okrętu się pan spodziewałeś?
	 */
	private Component createTablesTabPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Tables"));
		result.setPreferredSize(new Dimension(670, 500));
	
		//int posXda = 10;
		//int posYda = 10;

		return result;
	}
	
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	
	/**
	 * Metoda wypełnia wykresy danymi.
	 * @param showRef
	 */
	protected void showSingleKnockoutCharts(boolean showRef) {
		singleMode = true;
		NetSimulationData data = getCorrectSet(showRef);
		
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
		singleMode = true;
		
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
	
	
	private JPanel createPlacesTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Tables:"));
		
		model = new DefaultTableModel();
		placesTable = new JTable(model);
		tableRenderer = new SimKnockTableRenderer(placesTable);

		placesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(placesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		
		return tablesSubPanel;
	}

	/**
	 * Metoda tworząca tabelę miejsc
	 * @param ref  boolean - true, jeśli wywołane przez zbiór referencyjny
	 */
    private void createPlacesTable(boolean ref) {
    	modelPlaces = new SimKnockPlacesTableModel();
    	placesTable.setModel(modelPlaces);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	
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
    	placesTable.getColumnModel().getColumn(7).setHeaderValue("S1 %");
    	placesTable.getColumnModel().getColumn(7).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(7).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(7).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(8).setHeaderValue("S2 %");
    	placesTable.getColumnModel().getColumn(8).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(8).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(8).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(9).setHeaderValue("S3 %");
    	placesTable.getColumnModel().getColumn(9).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(9).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(9).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(10).setHeaderValue("S4 %");
    	placesTable.getColumnModel().getColumn(10).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(10).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(10).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(11).setHeaderValue("S5 %");
    	placesTable.getColumnModel().getColumn(11).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(11).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(11).setMaxWidth(cellSize);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(placesTable.getModel());
    	placesTable.setRowSorter(sorter);
        
    	placesTable.setName("PlacesTable");
        //tableRenderer.setMode(0); //mode: places
    	placesTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	placesTable.setDefaultRenderer(Object.class, tableRenderer);

    	NetSimulationData data = getCorrectSet(ref);
    	int index = -1;
    	try {
    		if(places.size() == 0) {
        		for(int p=0; p<data.placesNumber; p++) 
        			modelPlaces.addNew(data, p, null);
        	} else {
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
    
    private JPanel createTransTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Tables:"));
		
		model = new DefaultTableModel();
		transTable = new JTable(model);
		tableRenderer = new SimKnockTableRenderer(transTable);

		transTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(transTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		
		return tablesSubPanel;
	}
    
    /**
	 * Metoda tworząca tabelę tranzycji
	 * @param ref  boolean - true, jeśli wywołane przez zbiór referencyjny
	 */
    private void createTransTable(boolean ref) {
    	modelTrans = new SimKnockTransTableModel();
    	transTable.setModel(modelTrans);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	
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
    	transTable.getColumnModel().getColumn(7).setHeaderValue("S1 %");
    	transTable.getColumnModel().getColumn(7).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(7).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(7).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(8).setHeaderValue("S2 %");
    	transTable.getColumnModel().getColumn(8).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(8).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(8).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(9).setHeaderValue("S3 %");
    	transTable.getColumnModel().getColumn(9).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(9).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(9).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(10).setHeaderValue("S4 %");
    	transTable.getColumnModel().getColumn(10).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(10).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(10).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(11).setHeaderValue("S5 %");
    	transTable.getColumnModel().getColumn(11).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(11).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(11).setMaxWidth(cellSize);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(transTable.getModel());
    	transTable.setRowSorter(sorter);
        
    	transTable.setName("TransitionsTable");
        //tableRenderer.setMode(0); //mode: places
    	transTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
    	transTable.setDefaultRenderer(Object.class, tableRenderer);

    	NetSimulationData data = getCorrectSet(ref);
    	int index = -1;
    	try {
    		if(transitions.size() == 0) {
        		for(int p=0; p<data.transNumber; p++) 
        			modelTrans.addNew(data, p, null);
        	} else {
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
		
		doNotUpdate = false;
	}
	
	class CustomBarRenderer extends BarRenderer {
		private static final long serialVersionUID = 8580637960798892821L;
		ArrayList<Integer> deadColumns;
		public CustomBarRenderer(ArrayList<Integer> deadColumns) {
			this.deadColumns = deadColumns;
		}
		public Paint getItemPaint(final int row, final int column) {
			if(deadColumns.get(column) == 0)
				return Color.green;
			else
				return Color.darkGray;
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
