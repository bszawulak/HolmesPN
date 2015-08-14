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
		tabbedPane.addTab("Charts I", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createPlacesChartsPanel(), "Places");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Tables", Tools.getResIcon16("/icons/stateSim/transDyn.png"), createTablesTabPanel(), "Tables");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		//tabbedPane.addTab("Charts II", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), new JPanel(), "Transition");
		//tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		
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
		NetSimulationData data = null;
		if(showRef) {
			int selectedRef = referencesCombo.getSelectedIndex() - 1;
			if(selectedRef == -1)
				return;
			data = pn.accessSimKnockoutData().getReferenceSet(selectedRef);
		} else {
			int selectedKnockout = dataCombo.getSelectedIndex() - 1;
			if(selectedKnockout == -1)
				return;
			data = pn.accessSimKnockoutData().getKnockoutSet(selectedKnockout);
		}
		if(data == null) {
			overlord.log("Error accessing dataset.", "error", true);
			return;
		}
		
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
	    	String text = "<html>";
	    	if(compareMode) {//bar + ": " + nodeIndex;
	    		if(isPlaces) {
	    			if(simpleMode) {
	    				text += "p"+nodeIndex;
	    			} else {
	    				text += "p"+nodeIndex+"_"+places.get(nodeIndex).getName();
	    			}
	    			double tokensRef = refData.placeTokensAvg.get(nodeIndex);
	    			text += "<br>Reference tokens: "+formatter.format(tokensRef);
	    			text += " StdDev: "+formatter.format(refData.placeStdDev.get(nodeIndex));
	    			
	    			double tokensKnock = knockData.placeTokensAvg.get(nodeIndex);
	    			text += "<br>Knockout tokens: "+formatter.format(tokensKnock);
	    			text += " StdDev: "+formatter.format(knockData.placeStdDev.get(nodeIndex));
	    			
	    			
	    			
	    			if(tokensKnock == 0)
	    				text += "<br><font color=\"red\"> Transition disabled </font> ";
		    	} else {
		    		if(simpleMode) {
		    			text += "t"+nodeIndex;
	    			} else {
	    				text += "t"+nodeIndex+"_"+transitions.get(nodeIndex).getName();
	    			}
		    		double firingRef = refData.transFiringsAvg.get(nodeIndex);
	    			text += "<br>Reference firing: "+formatter.format(firingRef);
	    			text += " StdDev: "+formatter.format(refData.transStdDev.get(nodeIndex));
	    			
	    			double firingKnock = knockData.transFiringsAvg.get(nodeIndex);
	    			text += "<br>Knockout firing: "+formatter.format(firingKnock);
	    			text += " StdDev: "+formatter.format(knockData.transStdDev.get(nodeIndex));
	    			
	    			
		    	}
	    	} else {
	    		if(isPlaces) {
	    			if(simpleMode) {
	    				text += "p"+nodeIndex;
	    			} else {
	    				text += "p"+nodeIndex+"_"+places.get(nodeIndex).getName();
	    			}
	    			double tokensRef = refData.placeTokensAvg.get(nodeIndex);
	    			text += "<br>Tokens: "+formatter.format(tokensRef);
	    			text += " StdDev: "+formatter.format(refData.placeStdDev.get(nodeIndex));
		    	} else {
		    		if(simpleMode) {
		    			text += "t"+nodeIndex;
	    			} else {
	    				text += "t"+nodeIndex+"_"+transitions.get(nodeIndex).getName();
	    			}
		    		double firingRef = refData.transFiringsAvg.get(nodeIndex);
	    			text += "<br>Reference firing: "+formatter.format(firingRef);
	    			text += " StdDev: "+formatter.format(refData.transStdDev.get(nodeIndex));
		    	}
	    	}
	    	text += "</html>";
	    	return text;
	    }
	}
}
