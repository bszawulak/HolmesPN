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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.xy.XYSeriesCollection;

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.NetSimulationData;
import abyss.petrinet.data.PetriNet;
import abyss.utilities.Tools;

/**
 * Klasa okna przeglądania danych symulacji knockout.
 * 
 * @author MR
 *
 */
public class AbyssStateSimKnockVis extends JFrame {
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
	private ChartProperties chartDetails;
	
	private JComboBox<String> referencesCombo = null;
	private JComboBox<String> dataCombo = null;
	
	/**
	 * Konstruktor obiektu klast AbyssStateSimKnockVis.
	 * @param boss AbyssStateSim - główne okno symulatora
	 */
	public AbyssStateSimKnockVis(AbyssStateSim boss) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.boss = boss;
		chartDetails = new ChartProperties();
		
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
		setSize(new Dimension(800, 600));
		
		JPanel main = new JPanel(new BorderLayout()); //główny panel okna
		add(main);
		
		
		main.add(getActionsPanel(), BorderLayout.NORTH);
		
		JPanel mainTabbedPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("PCharts", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createPlacesChartsTabPanel(), "Places");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("TCharts", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createTransChartsTabPanel(), "Transition");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Tables", Tools.getResIcon16("/icons/stateSim/transDyn.png"), createTablesTabPanel(), "Tables");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
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
		referencesCombo.setBounds(posXda+80, posYda, 400, 20);
		referencesCombo.setMaximumRowCount(12);
		referencesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
				
				//int selected = referencesCombo.getSelectedIndex();
			}
			
		});
		result.add(referencesCombo);
		
		JButton showChartButton = new JButton("Show ref-chart");
		showChartButton.setBounds(posXda+490, posYda, 120, 20);
		showChartButton.setMargin(new Insets(0, 0, 0, 0));
		showChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/g.png"));
		showChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showCharts(true);
			}
		});
		result.add(showChartButton);
		
		JLabel label2 = new JLabel("Data sets:");
		label2.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label2);
		
		String[] data2 = { " ----- " };
		dataCombo = new JComboBox<String>(data2); //final, aby listener przycisku odczytał wartość
		dataCombo.setBounds(posXda+80, posYda, 400, 20);
		dataCombo.setMaximumRowCount(12);
		dataCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
				
				//int selected = dataCombo.getSelectedIndex();
			}
			
		});
		result.add(dataCombo);
		
		JButton showChartKnockButton = new JButton("Show knock-chart");
		showChartKnockButton.setBounds(posXda+490, posYda, 120, 20);
		showChartKnockButton.setMargin(new Insets(0, 0, 0, 0));
		showChartKnockButton.setIcon(Tools.getResIcon16("/icons/stateSim/g.png"));
		showChartKnockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showCharts(false);
			}
		});
		result.add(showChartKnockButton);
		
		return result;
	}
	
	protected void showCharts(boolean showRef) {
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
		
		int places = data.placesNumber;
		StatisticalCategoryDataset datasetPlaces = createPlacesDataset(data);

        CategoryAxis xAxisPlaces = new CategoryAxis("Type");
        xAxisPlaces.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisPlaces.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisPlaces.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Value");

        CategoryItemRenderer rendererPlaces = new StatisticalBarRenderer();
        CategoryPlot plotPlaces = new CategoryPlot(datasetPlaces, xAxisPlaces, yAxisPlaces, rendererPlaces);
        
        //CategoryItemRenderer renderer = new CustomBarRenderer(data.placeZeroTokens);
        //plotPlaces.setRenderer(renderer);
        
        placesChartPanel.removeAll();
        placesChart = new JFreeChart("Statistical Bar Chart Demo", new Font("Helvetica", Font.BOLD, 14), plotPlaces, true);
		ChartPanel chartPanelPlaces = new ChartPanel(placesChart);
		chartPanelPlaces.setPreferredSize(new Dimension(places*40, 270)); 
		chartPanelPlaces.setMaximumDrawWidth(places*40);
		
	    JScrollPane sPanePlaces = new JScrollPane(chartPanelPlaces); 
	    placesChartPanel.add(sPanePlaces, BorderLayout.CENTER);
	    placesChartPanel.revalidate();
	    placesChartPanel.repaint();


	    //TRANSITIONS:
	    int transitions = data.transNumber;
		StatisticalCategoryDataset datasetTrans = createTransitionsDataset(data);

        CategoryAxis xAxisTrans = new CategoryAxis("Type");
        xAxisTrans.setLowerMargin(0.01d); // percentage of space before first bar
       	xAxisTrans.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisTrans.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisTrans = new NumberAxis("Value");

        // define the plot
        CategoryItemRenderer rendererTrans = new StatisticalBarRenderer();
        CategoryPlot plotTrans = new CategoryPlot(datasetTrans, xAxisTrans, yAxisTrans, rendererTrans);
        
        transitionsChartPanel.removeAll();
        transitionsChart = new JFreeChart("Statistical Bar Chart Demo", new Font("Helvetica", Font.BOLD, 14), plotTrans, true);
		ChartPanel chartPanelTrans = new ChartPanel(transitionsChart);
		chartPanelTrans.setPreferredSize(new Dimension(transitions*30, 270)); 
		chartPanelTrans.setMaximumDrawWidth(transitions*30);
		
	    JScrollPane sPaneTrans = new JScrollPane(chartPanelTrans); 
	    transitionsChartPanel.add(sPaneTrans, BorderLayout.CENTER);
	    transitionsChartPanel.revalidate();
	    transitionsChartPanel.repaint();
	}
	
	private StatisticalCategoryDataset createPlacesDataset(NetSimulationData data) {
        final DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
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
        return result;
    }
	
	private StatisticalCategoryDataset createTransitionsDataset(NetSimulationData data) {
        final DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
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
        return result;
    }

	/**
	 * Tworzenie panelu wykresu miejsc.
	 * JPanel - okrętu się pan spodziewałeś?
	 */
	private JPanel createPlacesChartsTabPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
		result.setBorder(BorderFactory.createTitledBorder("Charts"));
		result.setPreferredSize(new Dimension(670, 500));
	
		//int posXda = 10;
		//int posYda = 10;
		
		placesChartPanel = new JPanel(new BorderLayout());
		placesChartPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
		placesChartPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
		result.add(placesChartPanel, BorderLayout.CENTER);

		
		return result;
	}
	
	/**
	 * Tworzenie panelu wykresu tranzycji.
	 * JPanel - okrętu się pan spodziewałeś?
	 */
	private JPanel createTransChartsTabPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
		result.setBorder(BorderFactory.createTitledBorder("Charts"));
		result.setPreferredSize(new Dimension(670, 500));
		
		transitionsChartPanel = new JPanel(new BorderLayout());
		transitionsChartPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart"));
		transitionsChartPanel.add(createTransChartPanel(), BorderLayout.CENTER);
		result.add(transitionsChartPanel, BorderLayout.CENTER);
		
		return result;
	}
	
	/**
	 * Tworzy panel wykresu miejsc.
	 * @return JPanel - okrętu się pan spodziewałeś?
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
	
	    ChartPanel placesChartPanel = new ChartPanel(placesChart);
	    return placesChartPanel;
	}

	/**
	 * Tworzy panel wykresu tranzycji.
	 * @return JPanel - okrętu się pan spodziewałeś?
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
	 * Tworzy panel tablic
	 * @return JPanel - okrętu się pan spodziewałeś?
	 */
	private Component createTablesTabPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Tables"));
		result.setPreferredSize(new Dimension(670, 500));
	
		int posXda = 10;
		int posYda = 10;
		
		
		
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
				String name = "Ref:"+r+" Date: "+references.get(r).date+" NetMode:"+references.get(r).netSimType+
						" MaxMode:"+references.get(r).maxMode;
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
		doNotUpdate = true;
		int oldKnockSelected = 0;
		oldKnockSelected = dataCombo.getSelectedIndex();
		dataCombo.removeAllItems();
		dataCombo.addItem(" ----- ");
		if(knockSize > 0) {
			for(int r=0; r<knockSize; r++) {
				String name = "Data set:"+r+" Date: "+knockout.get(r).date+" NetMode:"+knockout.get(r).netSimType+
						" MaxMode:"+knockout.get(r).maxMode;
				dataCombo.addItem(name);
			}
	
			if(oldKnockSelected < dataCombo.getItemCount())
				dataCombo.setSelectedIndex(oldKnockSelected);
			else
				dataCombo.setSelectedIndex(0);
		}
	}

    /**
     * Właściwości wykresów.
     * @author MR
     *
     */
	private class ChartProperties {
		public float p_StrokeWidth = 1.0f;
		public float t_StrokeWidth = 1.0f;
		
		public ChartProperties() {}
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
}
