package holmes.windows.ssim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeMap;

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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import holmes.darkgui.LanguageManager;
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
import holmes.tables.simKnock.SimKnockPlacesCompAllTableModel;
import holmes.tables.simKnock.SimKnockPlacesCompTableModel;
import holmes.tables.simKnock.SimKnockPlacesCompTableModel.PlaceCompContainer;
import holmes.tables.simKnock.SimKnockPlacesTableModel;
import holmes.tables.simKnock.SimKnockTableRenderer;
import holmes.tables.simKnock.SimKnockTransCompAllTableModel;
import holmes.tables.simKnock.SimKnockTransCompTableModel;
import holmes.tables.simKnock.SimKnockTransCompTableModel.TransCompContainer;
import holmes.tables.simKnock.SimKnockTransTableModel;
import holmes.tables.simKnock.SimKnockPlacesCompAllTableModel.DetailsPlace;
import holmes.tables.simKnock.SimKnockTransCompAllTableModel.DetailsTrans;
import holmes.utilities.Tools;
import holmes.windows.HolmesNodeInfo;
import holmes.windows.HolmesNotepad;

/**
 * Klasa okna przeglądania danych symulacji knockout.
 */
public class HolmesSimKnockVis extends JFrame {
	@Serial
	private static final long serialVersionUID = 3020186160500907678L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private PetriNet pn;
	private static final DecimalFormat formatter1 = new DecimalFormat( "#.#" );
	private static final DecimalFormat formatter2 = new DecimalFormat( "#.##" );
	private static final DecimalFormat formatter3 = new DecimalFormat( "#.###" );
	
	private boolean doNotUpdate = false;
	private HolmesSim boss;
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
	private JTable placesTable;
	private JTable transTable;
	private JTable placesCompAllTable;
	private JTable transCompAllTable;
	
	protected boolean notepadInfo = false;
	
	/**
	 * Konstruktor obiektu klast HolmesStateSimKnockVis.
	 * @param boss HolmesStateSim - główne okno symulatora
	 */
	public HolmesSimKnockVis(HolmesSim boss) {
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
		setTitle(lang.getText("HSKVwin_entry001title"));
		setLocation(boss.getLocation().x,boss. getLocation().y);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00555exception")+" "+ex.getMessage(), "error", true);
		}
		setSize(new Dimension(1200, 930));
		
		JPanel main = new JPanel(new BorderLayout()); //główny panel okna
		add(main);

		main.add(getActionsPanel(), BorderLayout.NORTH);
		
		JPanel mainTabbedPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
			@Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 32;
			}
			@Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
				super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
			}
		});
		tabbedPane.addTab(lang.getText("HSKVwin_entry002"), Tools.getResIcon32("/icons/simulationKnockout/visChartsIcon.png") //Charts
				, createPlacesChartsPanel(), lang.getText("HSKVwin_entry002t"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab(lang.getText("HSKVwin_entry003"), Tools.getResIcon16("/icons/simulationKnockout/visPlaces.png") //Places
				, createPlacesTablePanel(), lang.getText("HSKVwin_entry003t"));
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		tabbedPane.addTab(lang.getText("HSKVwin_entry004"), Tools.getResIcon16("/icons/simulationKnockout/visTrans.png") //Transitions
				, createTransTablePanel(), lang.getText("HSKVwin_entry004t"));
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		tabbedPane.addTab(lang.getText("HSKVwin_entry005"), Tools.getResIcon16("/icons/simulationKnockout/visPlacesSeries.png") //Places (series)
				, createPlacesCompAllTablePanel(), lang.getText("HSKVwin_entry005t"));
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
		tabbedPane.addTab(lang.getText("HSKVwin_entry006"), Tools.getResIcon16("/icons/simulationKnockout/visTransSeries.png") //Transitions (series)
				, createTransCompAllTablePanel(), lang.getText("HSKVwin_entry006t"));
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);
		
		mainTabbedPanel.add(tabbedPane);
		main.add(mainTabbedPanel, BorderLayout.CENTER);
		//this.boss.setEnabled(false);
		overlord.getFrame().setEnabled(false);
	}
	
	/**
	 * Zwraca panel górny - opcje, comboboxy.
	 * @return JPanel - okrętu się pan spodziewałeś?
	 */
	private JPanel getActionsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry007"))); //Main options panel
		result.setPreferredSize(new Dimension(670, 120));
	
		int posXda = 10;
		int posYda = 20;
		
		JLabel label1 = new JLabel(lang.getText("HSKVwin_entry008")); //Ref sets:
		label1.setBounds(posXda, posYda, 70, 20);
		result.add(label1);
		
		String[] data = { " ----- " };
		referencesCombo = new JComboBox<String>(data); //final, aby listener przycisku odczytał wartość
		referencesCombo.setToolTipText(lang.getText("HSKVwin_entry009t"));
		referencesCombo.setBounds(posXda+80, posYda, 500, 20);
		referencesCombo.setMaximumRowCount(12);
		referencesCombo.addActionListener(actionEvent -> {
			//if(doNotUpdate)
			//	return;
		});
		result.add(referencesCombo);
		
		JLabel label3 = new JLabel(lang.getText("HSKVwin_entry010")); //Sim. series:
		label3.setBounds(posXda+600, posYda, 80, 20);
		result.add(label3);
		
		String[] dataSeries = { " ----- " };
		seriesCombo = new JComboBox<String>(dataSeries); //final, aby listener przycisku odczytał wartość
		seriesCombo.setToolTipText(lang.getText("HSKVwin_entry011t"));
		seriesCombo.setBounds(posXda+680, posYda, 250, 20);
		seriesCombo.setMaximumRowCount(12);
		seriesCombo.addActionListener(actionEvent -> {
			//if(doNotUpdate)
			//	return;
		});
		result.add(seriesCombo);
		
		JLabel label2 = new JLabel(lang.getText("HSKVwin_entry012")); //Data sets:
		label2.setBounds(posXda, posYda+=25, 70, 20);
		result.add(label2);
		
		String[] data2 = { " ----- " };
		dataCombo = new JComboBox<String>(data2); //final, aby listener przycisku odczytał wartość
		dataCombo.setToolTipText(lang.getText("HSKVwin_entry013t"));
		dataCombo.setBounds(posXda+80, posYda, 850, 20);
		dataCombo.setMaximumRowCount(12);
		dataCombo.addActionListener(actionEvent -> {
			//if(doNotUpdate)
			//	return;
			//int selected = dataCombo.getSelectedIndex();
		});
		result.add(dataCombo);
		
		JButton showRefDataButton = new JButton(lang.getText("HSKVwin_entry014")); //Show reference dataset
		showRefDataButton.setBounds(posXda, posYda+=25, 175, 42);
		showRefDataButton.setMargin(new Insets(0, 0, 0, 0));
		showRefDataButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/visShowRefButton.png"));
		showRefDataButton.addActionListener(actionEvent -> {
			int selRef = referencesCombo.getSelectedIndex();
			if(selRef > 0) {
				showSingleKnockoutCharts(true);
				createPlacesTable(true);
				createTransTable(true);
			}
		});
		result.add(showRefDataButton);
		
		JButton showKnockDataButton = new JButton(lang.getText("HSKVwin_entry015")); //Show knockout dataset
		showKnockDataButton.setBounds(posXda+180, posYda, 175, 42);
		showKnockDataButton.setMargin(new Insets(0, 0, 0, 0));
		showKnockDataButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visShowDataSetButton.png"));
		showKnockDataButton.addActionListener(actionEvent -> {
			int selected = dataCombo.getSelectedIndex();
			if(selected > 0) {
				showSingleKnockoutCharts(false);
				createPlacesTable(false);
				createTransTable(false);
			}
		});
		result.add(showKnockDataButton);
		
		JButton showChartKnockButton = new JButton(lang.getText("HSKVwin_entry016")); //Compare reference and knockout
		showChartKnockButton.setBounds(posXda+360, posYda, 175, 42);
		showChartKnockButton.setMargin(new Insets(0, 0, 0, 0));
		showChartKnockButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visCompRefDataButton.png"));
		showChartKnockButton.addActionListener(actionEvent -> {
			int selRef = referencesCombo.getSelectedIndex();
			int dataRef = dataCombo.getSelectedIndex();
			if(selRef < 1 || dataRef < 1) {
				JOptionPane.showMessageDialog(null,
					lang.getText("HSKVwin_entry016msg"), lang.getText("HSKVwin_entry016t"), JOptionPane.WARNING_MESSAGE);
			} else {
				showCompareCharts();
				createPlacesCompTable();
				createTransCompTable();
			}
		});
		result.add(showChartKnockButton);
		
		JButton showSeriesButton = new JButton(lang.getText("HSKVwin_entry017")); //Full series datatables
		showSeriesButton.setBounds(posXda+600, posYda, 175, 42);
		showSeriesButton.setMargin(new Insets(0, 0, 0, 0));
		showSeriesButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visRefDataSeriesButton.png"));
		showSeriesButton.addActionListener(actionEvent -> {
			int selected = seriesCombo.getSelectedIndex() - 1;
			if(selected > -1) {
				createCompareAllTables(selected);
			}
		});
		result.add(showSeriesButton);
		
		JButton showNotepadButton = new JButton(lang.getText("HSKVwin_entry018")); //Show notepad summary
		showNotepadButton.setBounds(posXda+780, posYda, 175, 42);
		showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
		showNotepadButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/visRefDataSeriesNotepadButton.png"));
		showNotepadButton.addActionListener(actionEvent -> {
			int selected = seriesCombo.getSelectedIndex() - 1;
			if(selected > -1) {
				createCompareAllTablesNotepad(selected);
			}
		});
		result.add(showNotepadButton);
		
		JCheckBox sortedCheckBox = new JCheckBox(lang.getText("HSKVwin_entry019")); //Notepad
		sortedCheckBox.setBounds(posXda+960, posYda, 80, 20);
		sortedCheckBox.setSelected(false);
		sortedCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			notepadInfo = abstractButton.getModel().isSelected();
		});
		result.add(sortedCheckBox);
		
		return result;
	}
	
	/**
	 * Tworzenie panelu wykresu miejsc.
	 * JPanel - okrętu się pan spodziewałeś?
	 */
	private JPanel createPlacesChartsPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry020")));
		result.setPreferredSize(new Dimension(670, 500));

		String chartTitle = lang.getText("HSKVwin_entry021"); //Places dynamics
	    String xAxisLabel = "Step"; //Step
	    String yAxisLabel = "Tokens"; //Tokens
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
		XYSeriesCollection placesSeriesDataSet = new XYSeriesCollection();
	    placesChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, placesSeriesDataSet, 
	    		PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
	    placesChartPanel = new JPanel(new BorderLayout());
		placesChartPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry024"))); //Places chart
		placesChartPanel.add(new ChartPanel(placesChart), BorderLayout.CENTER);

		JPanel result2 = new JPanel(new BorderLayout());
		result2.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry025"))); //Charts
		result2.setPreferredSize(new Dimension(670, 500));
		
		String chartTitle2 = lang.getText("HSKVwin_entry026"); //Transitions dynamics
	    String xAxisLabel2 = "Transition"; //Transition
	    String yAxisLabel2 = "Firing"; //Firing
	    boolean showLegend2 = true;
	    boolean createTooltip2 = true;
	    boolean createURL2 = false;

		XYSeriesCollection transitionsSeriesDataSet = new XYSeriesCollection();
		transitionsChart = ChartFactory.createXYLineChart(chartTitle2, xAxisLabel2, yAxisLabel2, transitionsSeriesDataSet,
			PlotOrientation.VERTICAL, showLegend2, createTooltip2, createURL2);

		transitionsChartPanel = new JPanel(new BorderLayout());
		transitionsChartPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry029"))); //Transitions chart
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

        CategoryAxis xAxisPlaces = new CategoryAxis(lang.getText("HSKVwin_entry030")); //Type
        xAxisPlaces.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisPlaces.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisPlaces.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Value");
        CategoryItemRenderer rendererPlaces = new StatisticalBarRenderer();
        CategoryPlot plotPlaces = new CategoryPlot(datasetPlaces, xAxisPlaces, yAxisPlaces, rendererPlaces);
        rendererPlaces.setToolTipGenerator(new CustomToolTipGenerator(true, false, places, transitions, data, null));
        
        placesChartPanel.removeAll();
        placesChart = new JFreeChart(lang.getText("HSKVwin_entry031"), new Font("Helvetica", Font.BOLD, 14), plotPlaces, true); //Places statistical data
		ChartPanel chartPanelPlaces = new ChartPanel(placesChart);
		chartPanelPlaces.setPreferredSize(new Dimension(data.placesNumber*40, 270)); 
		chartPanelPlaces.setMaximumDrawWidth(data.placesNumber*40);
	    JScrollPane sPanePlaces = new JScrollPane(chartPanelPlaces); 
	    placesChartPanel.add(sPanePlaces, BorderLayout.CENTER);
	    placesChartPanel.revalidate();
	    placesChartPanel.repaint();

	    //TRANSITIONS:
		StatisticalCategoryDataset datasetTrans = createTransitionsDataset(data, null);
		
        CategoryAxis xAxisTrans = new CategoryAxis(lang.getText("HSKVwin_entry032")); //Type
        xAxisTrans.setLowerMargin(0.01d); // percentage of space before first bar
       	xAxisTrans.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisTrans.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisTrans = new NumberAxis("Value");
        CategoryItemRenderer rendererTrans = new StatisticalBarRenderer();
        CategoryPlot plotTrans = new CategoryPlot(datasetTrans, xAxisTrans, yAxisTrans, rendererTrans);
        
        rendererTrans.setToolTipGenerator(new CustomToolTipGenerator(false, false, places, transitions, data, null));
        
        transitionsChartPanel.removeAll();
        transitionsChart = new JFreeChart(lang.getText("HSKVwin_entry033"), new Font("Helvetica", Font.BOLD, 14), plotTrans, true); //Transitions statistical data
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
		NetSimulationData data;
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
			overlord.log(lang.getText("LOGentry00556critError"), "error", true);
			return null;
		}
		return data;
	}
	
	/**
	 * Metoda pokazująca na wykresach porównanie zbioru referencyjnego i knockout
	 */
	@SuppressWarnings("deprecation")
	protected void showCompareCharts() {
		NetSimulationData refData;
		NetSimulationData knockData;
		int selectedRef = referencesCombo.getSelectedIndex() - 1;
		if(selectedRef == -1)
			return;
		refData = pn.accessSimKnockoutData().getReferenceSet(selectedRef);

		int selectedKnockout = dataCombo.getSelectedIndex() - 1;
		if(selectedKnockout == -1)
			return;
		knockData = pn.accessSimKnockoutData().getKnockoutSet(selectedKnockout);
		
		if(refData == null || knockData == null) {
			overlord.log(lang.getText("LOGentry00557critError"), "error", true);
			return;
		}
		
		StatisticalCategoryDataset datasetPlaces = createPlacesDataset(refData, knockData);

        CategoryAxis xAxisPlaces = new CategoryAxis(lang.getText("HSKVwin_entry030")); //Type
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
        placesChart = new JFreeChart(lang.getText("HSKVwin_entry031"), new Font("Helvetica", Font.BOLD, 14), plotPlaces, true); //Places statistical data
		ChartPanel chartPanelPlaces = new ChartPanel(placesChart);
		chartPanelPlaces.setPreferredSize(new Dimension(refData.placesNumber*50, 270)); 
		chartPanelPlaces.setMaximumDrawWidth(refData.placesNumber*50);
	    JScrollPane sPanePlaces = new JScrollPane(chartPanelPlaces); 
	    placesChartPanel.add(sPanePlaces, BorderLayout.CENTER);
	    placesChartPanel.revalidate();
	    placesChartPanel.repaint();


	    //TRANSITIONS:
		StatisticalCategoryDataset datasetTrans = createTransitionsDataset(refData, knockData);
		
        CategoryAxis xAxisTrans = new CategoryAxis(lang.getText("HSKVwin_entry032")); //Type
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
        transitionsChart = new JFreeChart(lang.getText("HSKVwin_entry033"), new Font("Helvetica", Font.BOLD, 14), plotTrans, true); //Transitions statistical data
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
        		overlord.log(lang.getText("LOGentry00558critError"), "error", true);
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
	 * @param compData NetSimulationData - pakiet do porównania
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
        		overlord.log(lang.getText("LOGentry00560critError"), "error", true);
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
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry034"))); //Place single knockout data table:
		model = new DefaultTableModel();
		placesTable = new JTable(model);
		
		placesTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
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
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry035"))); //Places comparison single knockout data table:
		model = new DefaultTableModel();
		placesCompAllTable = new JTable(model);
		
		placesCompAllTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
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
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry036"))); //Transition single knockout data table:
		model = new DefaultTableModel();
		transTable = new JTable(model);
		
		transTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
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
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKVwin_entry037"))); //Transition comparison single knockout data table:
		model = new DefaultTableModel();
		transCompAllTable = new JTable(model);
		
		transCompAllTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
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

		SimKnockPlacesTableModel modelPlaces = new SimKnockPlacesTableModel();
    	placesTable.setModel(modelPlaces);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	int svaluesCell = 35;
    	
    	placesTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	placesTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(1).setHeaderValue(lang.getText("HSKVwin_entry038")); //Place name
    	placesTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	placesTable.getColumnModel().getColumn(1).setMinWidth(100);
    	placesTable.getColumnModel().getColumn(2).setHeaderValue("AvgT");
    	placesTable.getColumnModel().getColumn(2).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(2).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(2).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setHeaderValue("MinT");
    	placesTable.getColumnModel().getColumn(3).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(3).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setHeaderValue("MaxT");
    	placesTable.getColumnModel().getColumn(4).setPreferredWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setMinWidth(largeCell);
    	placesTable.getColumnModel().getColumn(4).setMaxWidth(largeCell);
    	placesTable.getColumnModel().getColumn(5).setHeaderValue("notT");
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
    		if(places.isEmpty()) {
        		for(int p=0; p<data.placesNumber; p++) 
        			modelPlaces.addNew(data, p, null);
        	} else {
        		int index = -1;
        		for(Place place : places) {
            		index++;
            		modelPlaces.addNew(data, index, place);
            	}
        	}
    	} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00561exception")+" "+ex.getMessage(), "error", true);
		}
    	
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

		SimKnockPlacesCompTableModel modelPlacesComp = new SimKnockPlacesCompTableModel();
    	placesTable.setModel(modelPlacesComp);

    	int cellSize = 50;
    	int largeCell = 65;
    	//int svaluesCell = 25;
    	
    	placesTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	placesTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	placesTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	placesTable.getColumnModel().getColumn(1).setHeaderValue(lang.getText("HSKVwin_entry039")); //Place name
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
    	
    	placesTable.getColumnModel().getColumn(6).setHeaderValue("diff");
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
		SimKnockTableRenderer tableRendererPlaces = new SimKnockTableRenderer(placesTable);
    	tableRendererPlaces.setMode(2);
    	placesTable.setDefaultRenderer(String.class, tableRendererPlaces);
    	placesTable.setDefaultRenderer(Double.class, tableRendererPlaces);

    	try {
    		if(places.isEmpty()) {
        		for(int index=0; index<dataRef.placesNumber; index++) 
        			modelPlacesComp.addNew(dataRef, dataComp, index, null);
        	} else {
        		int index = -1;
        		for(Place place : places) {
            		index++;
            		modelPlacesComp.addNew(dataRef, dataComp, index, place);
            	}
        	}
    	} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00562exception")+" "+ex.getMessage(), "error", true);
		}
    	
        //action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
    	
        placesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        placesTable.validate();
        
        if(notepadInfo) {
        	HolmesNotepad notepad = new HolmesNotepad(900,600);
        	notepad.setVisible(true);

        	notepad.addTextLine(lang.getText("HSKVwin_entry040"), "text"); //PLACES
    		for(PlaceCompContainer data : modelPlacesComp.accessDataMatrix()) {
    			double diff = data.tokenAvgPercDiff/100;
    			String dif;
    			if(data.tokenAvgPercDiff == -999999.0) {
    				dif = " -inf";
    			} else if(data.tokenAvgPercDiff == 999991.0) {
    				dif = " ---";
    			} else if(data.tokenAvgPercDiff == 999999.0) {
    				dif = " +inf";
    			} else {
    				dif = ""+diff;
    			}
    			
    			String row = data.ID+";"+data.name+";"+data.tokenAvgRef+";"+data.tokenAvgKnock+";"+
    					dif+"\n";
    			row = row.replace(".", ",");
    			
    			notepad.addTextLine(row, "text");
    		}
    		
    		//notePadTrans.addTextLine(";", "text");
    	}
    }

    /**
	 * Metoda tworząca tabelę danych dla tranzycji.
	 * @param ref  boolean - true, jeśli wywołane przez zbiór referencyjny
	 */
    private void createTransTable(boolean ref) {
    	NetSimulationData data = getCorrectSet(ref);
    	if(data == null)
    		return;

		SimKnockTransTableModel modelTrans = new SimKnockTransTableModel();
    	transTable.setModel(modelTrans);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	int svaluesCell = 35;
    	int hugeCell = 80;
    	
    	transTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	transTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(1).setHeaderValue(lang.getText("HSKVwin_entry041")); //Transition name
    	transTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	transTable.getColumnModel().getColumn(1).setMinWidth(100);
    	transTable.getColumnModel().getColumn(2).setHeaderValue("AvgF");
    	transTable.getColumnModel().getColumn(2).setPreferredWidth(hugeCell);
    	transTable.getColumnModel().getColumn(2).setMinWidth(hugeCell);
    	transTable.getColumnModel().getColumn(2).setMaxWidth(hugeCell);
    	transTable.getColumnModel().getColumn(3).setHeaderValue("MinF");
    	transTable.getColumnModel().getColumn(3).setPreferredWidth(hugeCell);
    	transTable.getColumnModel().getColumn(3).setMinWidth(hugeCell);
    	transTable.getColumnModel().getColumn(3).setMaxWidth(hugeCell);
    	transTable.getColumnModel().getColumn(4).setHeaderValue("MaxF");
    	transTable.getColumnModel().getColumn(4).setPreferredWidth(hugeCell);
    	transTable.getColumnModel().getColumn(4).setMinWidth(hugeCell);
    	transTable.getColumnModel().getColumn(4).setMaxWidth(hugeCell);
    	transTable.getColumnModel().getColumn(5).setHeaderValue("notF");
    	transTable.getColumnModel().getColumn(5).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(5).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(5).setMaxWidth(cellSize);
    	
    	transTable.getColumnModel().getColumn(6).setHeaderValue("stdDev");
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
    		if(transitions.isEmpty()) {
        		for(int p=0; p<data.transNumber; p++) 
        			modelTrans.addNew(data, p, null);
        	} else {
        		int index = -1;
        		for(Transition trans : transitions) {
            		index++;
            		modelTrans.addNew(data, index, trans);
            	}
        	}
    	} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00563exception")+" "+ex.getMessage(), "error", true);
		}
    	
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

		SimKnockTransCompTableModel modelTransComp = new SimKnockTransCompTableModel();
    	transTable.setModel(modelTransComp);
        
    	int cellSize = 50;
    	int largeCell = 65;
    	//int svaluesCell = 25;
    	
    	transTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	transTable.getColumnModel().getColumn(0).setPreferredWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMinWidth(cellSize);
    	transTable.getColumnModel().getColumn(0).setMaxWidth(cellSize);
    	transTable.getColumnModel().getColumn(1).setHeaderValue(lang.getText("HSKVwin_entry042"));
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
    	
    	transTable.getColumnModel().getColumn(6).setHeaderValue("diff");
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
		SimKnockTableRenderer tableRendererTrans = new SimKnockTableRenderer(transTable);
    	tableRendererTrans.setMode(1);
    	transTable.setDefaultRenderer(String.class, tableRendererTrans);
    	transTable.setDefaultRenderer(Double.class, tableRendererTrans);
    	

    	try {
    		if(transitions.isEmpty()) {
        		for(int index=0; index<dataRef.transNumber; index++) 
        			modelTransComp.addNew(dataRef, dataComp, index, null);
        	} else {
        		int index = -1;
        		for(Transition trans : transitions) {
            		index++;
            		modelTransComp.addNew(dataRef, dataComp, index, trans);
            	}
        	}
    	} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00564exception")+" "+ex.getMessage(), "error", true);
		}
    	
        //action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
    	
    	transTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        transTable.validate();
        
        if(notepadInfo) {
        	HolmesNotepad notepad = new HolmesNotepad(900,600);
        	notepad.setVisible(true);
        	notepad.addTextLine(lang.getText("HSKVwin_entry043"), "text"); //TRANSITIONS
    		
    		for(TransCompContainer data : modelTransComp.accessDataMatrix()) {
    			double diff = data.firingAvgPercDiff;
    			String dif;
    			if(diff == -999999.0) {
    				dif = " -inf";
    			} else if(diff == 999991.0) {
    				dif = " ---";
    			} else if(diff == 999999.0) {
    				dif = " +inf";
    			} else {
    				dif = ""+diff/100;
    			}
    			
    			String row = data.ID+";"+data.name+";"+data.firingAvgRef/100+";"+data.firingAvgKnock/100+";"+dif+"\n";
    			row = row.replace(".", ",");
    			
    			notepad.addTextLine(row, "text");
    		}
    		
    		//notePadTrans.addTextLine(";", "text");
    	}
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
					lang.getText("HSKVwin_entry044"), lang.getText("HSKVwin_entry044t"), JOptionPane.WARNING_MESSAGE);
    		seriesCombo.setSelectedIndex(0);
    		return;
    	}
    	NetSimulationData refSet = pn.accessSimKnockoutData().getReferenceSet(selRef);
    	if(dataPackage == null) {
    		return;
    	}
    	
    	int transNumber = dataPackage.get(0).transNumber;
		SimKnockTransCompAllTableModel modelTransCompAll = new SimKnockTransCompAllTableModel(transNumber);
    	transCompAllTable.setModel(modelTransCompAll);
    	transCompAllTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	transCompAllTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    	transCompAllTable.getColumnModel().getColumn(0).setMinWidth(40);
    	transCompAllTable.getColumnModel().getColumn(0).setMaxWidth(40);
    	transCompAllTable.getColumnModel().getColumn(1).setHeaderValue(lang.getText("HSKVwin_entry045"));
    	transCompAllTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	transCompAllTable.getColumnModel().getColumn(1).setMinWidth(100);
    	transCompAllTable.getColumnModel().getColumn(2).setHeaderValue(lang.getText("HSKVwin_entry046"));
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
    	tableRendererTransitions.setMode(3);
    	transCompAllTable.setDefaultRenderer(String.class, tableRendererTransitions);
    	transCompAllTable.setDefaultRenderer(Double.class, tableRendererTransitions);
    	transCompAllTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	transCompAllTable.validate();
    	resizeColumnWidth(transCompAllTable);

    	
    	int placesNumber = dataPackage.get(0).placesNumber;
		SimKnockPlacesCompAllTableModel modelPlacesCompAll = new SimKnockPlacesCompAllTableModel(placesNumber);
    	placesCompAllTable.setModel(modelPlacesCompAll);
    	
    	placesCompAllTable.getColumnModel().getColumn(0).setHeaderValue("ID");
    	placesCompAllTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    	placesCompAllTable.getColumnModel().getColumn(0).setMinWidth(40);
    	placesCompAllTable.getColumnModel().getColumn(0).setMaxWidth(40);
    	placesCompAllTable.getColumnModel().getColumn(1).setHeaderValue(lang.getText("HSKVwin_entry047"));
    	placesCompAllTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    	placesCompAllTable.getColumnModel().getColumn(1).setMinWidth(100);
    	placesCompAllTable.getColumnModel().getColumn(2).setHeaderValue(lang.getText("HSKVwin_entry048"));
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
    	tableRendererPlaces.setMode(3);
    	placesCompAllTable.setDefaultRenderer(String.class, tableRendererPlaces);
    	placesCompAllTable.setDefaultRenderer(Double.class, tableRendererPlaces);
    	placesCompAllTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	placesCompAllTable.validate();
    	resizeColumnWidth(placesCompAllTable);
		
    	HolmesNotepad notePadTrans = null;
    	HolmesNotepad notePadPlaces = null;
    	if(notepadInfo) {
    		notePadTrans = new HolmesNotepad(900,600);
    		notePadTrans.setVisible(true);
    		notePadPlaces = new HolmesNotepad(900,600);
    		notePadPlaces.setVisible(true);
    	}
		
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
    		String name;
    		if(transitions.isEmpty()) {
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
    		//if(tVector.size() != tTableVector.size() || pVector.size() != pTableVector.size()) {
			//	int x=1;
    		//}
    		
    		modelTransCompAll.addNew(tVector);
    		modelTransCompAll.tTableData.add(tTableVector);
    		modelPlacesCompAll.addNew(pVector);
    		modelPlacesCompAll.pTableData.add(pTableVector);
    		
    		if(notepadInfo) {
				if(notePadTrans != null) {
					for(String s : tVector) {
						notePadTrans.addTextLine(s+";", "text");
					}

					for(String s : pVector) {
						notePadPlaces.addTextLine(s+";", "text");
					}
					notePadTrans.addTextLineNL("", "text");
					notePadPlaces.addTextLineNL("", "text");
				}
    		}
    	}
    }
    
    /**
     * Tworzy tabele porównania serii danych ze zbiorem referencyjnym - w formie tekstowej.
     * @param selected int - ID serii
     */
    @SuppressWarnings("UnnecessaryContinue")
	private void createCompareAllTablesNotepad(int selected) {
    	long IDseries = pn.accessSimKnockoutData().accessSeries().get(selected);
    	ArrayList<NetSimulationData> dataPackage = pn.accessSimKnockoutData().getSeriesDatasets(IDseries);
    	int selRef = referencesCombo.getSelectedIndex() - 1;
    	if(selRef == -1) {
    		JOptionPane.showMessageDialog(null,
					lang.getText("HSKVwin_entry044"), lang.getText("HSKVwin_entry044t"), JOptionPane.WARNING_MESSAGE);
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
		String strB = "err.";
		try {
			strB = String.format(lang.getText("HSKVwin_entry049"), selected);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry049", "error", true);
		}
		notePad.addTextLineNL(strB, "text");
		notePad.addTextLineNL(" "+lang.getText("HSKVwin_entry050"), "text");
		notePad.addTextLineNL("===============================================================================", "text");
		notePad.addTextLineNL("", "text");

    	for(int t=0; t<transNumber; t++) {
    		NetSimulationData dataSet = dataPackage.get(t);
			strB = "err.";
			try {
				strB = String.format(lang.getText("HSKVwin_entry051"), t, transitions.get(t).getName());
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry051", "error", true);
			}
			
    		notePad.addTextLineNL(strB, "text");
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
					strB = "err.";
					try {
						strB = String.format("      "+lang.getText("HSKVwin_entry052"), t1, transitions.get(t1).getName());
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry052", "error", true);
					}
					
    				notePad.addTextLineNL(strB, "text");
    				transVector.set(t1, 0);
    				continue;
    			}
    			
    			if(refSet.transFiringsAvg.get(t1) == 0 && dataSet.transFiringsAvg.get(t1) > 0) {
    				double value = dataSet.transFiringsAvg.get(t1) * 100;
					strB = "err.";
					try {
						strB = String.format("      "+lang.getText("HSKVwin_entry053"), formatter2.format(value), t1, transitions.get(t1).getName());
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry053", "error", true);
					}
					
    				notePad.addTextLineNL(strB, "text");
    				transVector.set(t1, 0);
    				continue;
    			}
    			if(refSet.transFiringsAvg.get(t1) > 0 && dataSet.transFiringsAvg.get(t1) == 0) {
    				double value = refSet.transFiringsAvg.get(t1) * 100;
					strB = "err.";
					try {
						strB = String.format("      "+lang.getText("HSKVwin_entry054")+" " +formatter2.format(value)+"%]  t%d_%s", t1, transitions.get(t1).getName());
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry054", "error", true);
					}
					
    				notePad.addTextLineNL(strB, "text");
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

				strB = "err.";
				try {
					strB = String.format(lang.getText("HSKVwin_entry055")+" "
							,formatter2.format(refSet.transFiringsAvg.get(t1)*100), formatter2.format(dataSet.transFiringsAvg.get(t1)*100));
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry055", "error", true);
				}
				if(diff < 0) { //wzrosło w stos. do ref		
    				diff *= -1;
    				double value = diff * 100;
					data.put(value, strB+"t"+t1+"_"+transitions.get(t1).getName());
    				transVector.set(t1, 0);
    				continue;
    			} else {
    				diff *= -1;
    				double value = diff * 100;
					data.put(value, strB+"t"+t1+"_"+transitions.get(t1).getName());
    				transVector.set(t1, 0);
    				continue;
    			}
    		}

    		for(Double key: data.keySet()){
    			if(key < -20) {
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry056")+" "+formatter1.format(key)+"%] "
    						+data.get(key), "text");
    			} 
    			
    			if(key > 20) {
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry057")+" "+formatter1.format(key)+"%] "
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
		notePad.addTextLineNL(" "+lang.getText("HSKVwin_entry058"), "text");
		notePad.addTextLineNL("===============================================================================", "text");
		notePad.addTextLineNL("", "text");

    	for(int t=0; t<transNumber; t++) {
    		NetSimulationData dataSet = dataPackage.get(t);
			strB = "err.";
			try {
				strB = String.format("*** t%d_%s    "+lang.getText("HSKVwin_entry059"), t, transitions.get(t).getName());
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry059", "error", true);
			}
    		notePad.addTextLineNL(strB, "text");
    		ArrayList<Integer> placesVector = new ArrayList<>();
    		for(int p=0; p<placesNumber; p++) {
    			placesVector.add(1);
    		}
    		
    		for(int p=0; p<placesNumber; p++) {
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry060")+p+"_"+places.get(p).getName()+"", "text");
    				placesVector.set(p, 0);
    				continue;
    			}
    			
    			if(refSet.placeTokensAvg.get(p) == 0 && dataSet.placeTokensAvg.get(p) > 0) {
    				double value = dataSet.placeTokensAvg.get(p);
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry061")+" "
    						+formatter3.format(value)+"]  p"+p+"_"+places.get(p).getName(), "text");
    				placesVector.set(p, 0);
    				continue;
    			}
    			if(refSet.placeTokensAvg.get(p) > 0 && dataSet.placeTokensAvg.get(p) == 0) {
    				double value = refSet.placeTokensAvg.get(p);
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry062")+" "
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
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry063")+" "+formatter2.format(key)+"%] "
    						+data.get(key), "text");
    			}
    			if(key > 20) {
    				notePad.addTextLineNL("      "+lang.getText("HSKVwin_entry064")+formatter2.format(key)+"%] "
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
			return ( refSet.transFiringsMin.get(index) > dataSet.transFiringsMax.get(index) );
    	} else {
			return ( refSet.transFiringsMax.get(index) < dataSet.transFiringsMin.get(index) );
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
			return ( refSet.transFiringsAvg.get(index) - refSet.transStdDev.get(index) > dataSet.transFiringsAvg.get(index) + dataSet.transStdDev.get(index) );
    	} else { //ref mniejsze niż knock
			return ( refSet.transFiringsAvg.get(index) + refSet.transStdDev.get(index) < dataSet.transFiringsAvg.get(index) - dataSet.transStdDev.get(index) );
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
			return ( refSet.placeTokensMin.get(index) > dataSet.placeTokensMax.get(index) );
    	} else {
			return ( refSet.placeTokensMax.get(index) < dataSet.placeTokensMin.get(index) );
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
			return ( refSet.placeTokensAvg.get(index) - refSet.placeStdDev.get(index) > dataSet.placeTokensAvg.get(index) + dataSet.placeStdDev.get(index) );
    	} else {
			return ( refSet.placeTokensAvg.get(index) + refSet.placeStdDev.get(index) < dataSet.placeTokensAvg.get(index) - dataSet.placeStdDev.get(index) );
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
				overlord.getFrame().setEnabled(true);
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
		int oldSelected;
		oldSelected = referencesCombo.getSelectedIndex();
		referencesCombo.removeAllItems();
		referencesCombo.addItem(" ----- ");
		if(refSize > 0) {
			for(int r=0; r<refSize; r++) {
				StringBuilder disTxt = new StringBuilder(lang.getText("HSKVwin_entry065")+" "); //Disabled: 
				for(int t : references.get(r).disabledTransitionsIDs) {
					disTxt.append("t").append(t).append(", ");
				}
				for(int t : references.get(r).disabledMCTids) {
					disTxt.append("MCT").append(t + 1).append(", ");
				}
				disTxt = new StringBuilder(disTxt.toString().replace(", ", " "));

				String strB = "err.";
				try {
					strB = String.format(lang.getText("HSKVwin_entry066"), r, disTxt, references.get(r).netSimType, references.get(r).maxMode);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry066", "error", true);
				}
				
				referencesCombo.addItem(strB);
			}
			
			if(oldSelected < referencesCombo.getItemCount())
				referencesCombo.setSelectedIndex(oldSelected);
			else
				referencesCombo.setSelectedIndex(0);
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		
		//knockout data:
		ArrayList<NetSimulationData> knockout = pn.accessSimKnockoutData().accessKnockoutDataSets();
		int knockSize = knockout.size();
		int oldKnockSelected = dataCombo.getSelectedIndex();

		dataCombo.removeAllItems();
		dataCombo.addItem(" ----- ");
		if(knockSize > 0) {
			for(int r=0; r<knockSize; r++) {
				
				StringBuilder disTxt = new StringBuilder(lang.getText("HSKVwin_entry065")+" "); //Disabled: 
				for(int t : knockout.get(r).disabledTransitionsIDs) {
					disTxt.append("t").append(t).append(", ");
				}
				for(int t : knockout.get(r).disabledMCTids) {
					disTxt.append("MCT").append(t + 1).append(", ");
				}
				disTxt = new StringBuilder(disTxt.toString().replace(", ", " "));
				
				String transName = lang.getText("HSKVwin_entry067"); //Multiple transitions
				if(knockout.get(r).disabledTransitionsIDs.size() == 1) {
					transName = "("+transitions.get(knockout.get(r).disabledTransitionsIDs.get(0)).getName();
				}
				transName += ")";
				String strB = "err.";
				try {
					strB = String.format(lang.getText("HSKVwin_entry068"), r, disTxt, knockout.get(r).netSimType, knockout.get(r).maxMode, transName);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry068", "error", true);
				}
				dataCombo.addItem(strB);
			}
	
			if(oldKnockSelected < dataCombo.getItemCount())
				dataCombo.setSelectedIndex(oldKnockSelected);
			else
				dataCombo.setSelectedIndex(0);
		}
		
		
		//Seires daya
		ArrayList<Long> series = pn.accessSimKnockoutData().accessSeries();
		int seriesSize = series.size();
		int oldSeriesSelected = seriesCombo.getSelectedIndex();
		seriesCombo.removeAllItems();
		seriesCombo.addItem(" ----- ");
		if(seriesSize > 0) {
			for(int s=0; s<seriesSize; s++) {
				long IDseries = series.get(s);
				NetSimulationData representant = pn.accessSimKnockoutData().returnSeriesFirst(IDseries);
				String strB = "err.";
				try {
					strB = String.format(lang.getText("HSKVwin_entry069"), s, representant.steps, representant.reps);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry069", "error", true);
				}
				seriesCombo.addItem(strB);
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
						overlord.getWorkspace().getProject().getPlaces().get(index), this);
	  	    	window.setVisible(true);
			} else if(name.contains("Transitions")){
				int row = table.getSelectedRow();
	  	    	int index = Integer.parseInt(table.getValueAt(row, 0).toString());
	  	    	
	  	    	HolmesNodeInfo window = new HolmesNodeInfo(
						overlord.getWorkspace().getProject().getTransitions().get(index), this);
	  	    	window.setVisible(true);
			}
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00565exception")+"\n"+ex.getMessage(), "error", true);
		}
	}
	
	/**
	 * Klasa odpowiedzialna za informacje o wskazanym pasku wykresu.
	 */
	public static class CustomToolTipGenerator implements CategoryToolTipGenerator  {
		boolean isPlaces;
		boolean compareMode;
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
	    	StringBuilder text = new StringBuilder("<html><font size=\"5\">");
			String strB = "err.";
			try {
				strB = String.format(lang.getText("HSKVwin_entry070"), refData.steps, refData.reps);
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKVwin_entry070", "error", true);
			}
	    	text.append(strB);
	    	if(compareMode) { //compare ref and knockout:
	    		if(isPlaces) {
	    			if(simpleMode) {
	    				text.append(lang.getText("HSKVwin_entry071")).append(nodeIndex); //Place: p
	    			} else {
	    				text.append(lang.getText("HSKVwin_entry071")).append(nodeIndex).append("_").append(places.get(nodeIndex).getName()); //Place: p
	    			}
	    			double tokensRef = refData.placeTokensAvg.get(nodeIndex);
	    			text.append(lang.getText("HSKVwin_entry072")).append(formatter.format(tokensRef)); //Reference tokens:
	    			text.append(lang.getText("HSKVwin_entry073")).append(formatter.format(refData.placeStdDev.get(nodeIndex))).append("</b>"); // StdDev:
	    			
	    			double tokensKnock = knockData.placeTokensAvg.get(nodeIndex);
	    			text.append(lang.getText("HSKVwin_entry074")).append(formatter.format(tokensKnock)); //Knockout tokens:
	    			text.append(lang.getText("HSKVwin_entry073")).append(formatter.format(knockData.placeStdDev.get(nodeIndex))).append("</b>"); // StdDev:
	    			
	    			
		    	} else {
		    		if(simpleMode) {
		    			text.append(lang.getText("HSKVwin_entry075")).append(nodeIndex); //Transition: t
	    			} else {
	    				text.append(lang.getText("HSKVwin_entry075")).append(nodeIndex).append("_").append(transitions.get(nodeIndex).getName()); //Transition: t
	    			}
		    		double firingRef = refData.transFiringsAvg.get(nodeIndex);
	    			text.append(lang.getText("HSKVwin_entry076")).append(formatter.format(firingRef)); //Reference firing:
	    			text.append(lang.getText("HSKVwin_entry073")).append(formatter.format(refData.transStdDev.get(nodeIndex))).append("</b>"); // StdDev:
	    			
	    			double firingKnock = knockData.transFiringsAvg.get(nodeIndex);
	    			text.append(lang.getText("HSKVwin_entry077")).append(formatter.format(firingKnock)); //Knockout firing:
	    			text.append(lang.getText("HSKVwin_entry073")).append(formatter.format(knockData.transStdDev.get(nodeIndex))).append("</b>"); // StdDev:
	    			
	    			//dodatkowe info o wyłączonej tranzycji:
	    			if(bar != 0) {
	    				if(knockData.disabledTotals.contains(nodeIndex)) {
		    				text.append(lang.getText("HSKVwin_entry078"));
		    			} else {
		    				if(firingKnock == 0) {
		    					text.append(lang.getText("HSKVwin_entry079"));
		    					for(int t : knockData.disabledTotals)
		    						text.append("t").append(t).append(" ");
		    					text.append("</b>");
		    				}
		    			}
	    			}
		    	}
	    	} else { //single data mode:
	    		if(isPlaces) {
	    			if(simpleMode) {
	    				text.append(lang.getText("HSKVwin_entry071")).append(nodeIndex); //Place: p
	    			} else {
	    				text.append(lang.getText("HSKVwin_entry071")).append(nodeIndex).append("_").append(places.get(nodeIndex).getName()); //Place: p
	    			}
	    			double tokensRef = refData.placeTokensAvg.get(nodeIndex);
	    			text.append(lang.getText("HSKVwin_entry080")).append(formatter.format(tokensRef)); //Tokens:
	    			text.append(lang.getText("HSKVwin_entry073")).append(formatter.format(refData.placeStdDev.get(nodeIndex))).append("</b>"); // StdDev:
		    	} else {
		    		if(simpleMode) {
		    			text.append(lang.getText("HSKVwin_entry075")).append(nodeIndex); //Transition: t
	    			} else {
	    				text.append(lang.getText("HSKVwin_entry075")).append(nodeIndex).append("_").append(transitions.get(nodeIndex).getName()); //Transition: t
	    			}
		    		double firingRef = refData.transFiringsAvg.get(nodeIndex);
	    			text.append(lang.getText("HSKVwin_entry076")).append(formatter.format(firingRef)); //Reference firing:
	    			text.append(lang.getText("HSKVwin_entry073")).append(formatter.format(refData.transStdDev.get(nodeIndex))).append("</b>"); // StdDev:
	    			
	    			//dodatkowe info o wyłączonej tranzycji:
	    			if(bar != 0) {
	    				if(refData.disabledTotals.contains(nodeIndex)) {
		    				text.append(lang.getText("HSKVwin_entry081")); //Transition disabled manually in simulation
		    			} else {
		    				if(firingRef == 0) {
		    					text.append(lang.getText("HSKVwin_entry082")); //Transition starved because of other disabled transition(s):
		    					for(int t : refData.disabledTotals)
		    						text.append("t").append(t).append(" ");
		    					text.append("</b>");
		    				}
		    			}
	    			}
		    	}
	    	}
	    	text.append("</font></html>");
	    	return text.toString();
	    }
	}
}
