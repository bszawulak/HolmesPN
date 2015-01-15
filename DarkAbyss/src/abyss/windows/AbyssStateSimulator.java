package abyss.windows;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import abyss.darkgui.GUIManager;
import abyss.math.Place;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.StateSimulator;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

public class AbyssStateSimulator extends JFrame {
	private static final long serialVersionUID = -5478185512282936410L;
	//private NetSimulator sim;
	private JComboBox<String> placesCombo = null;
	private JComboBox<String> transitionsCombo = null;
	private StateSimulator ssim;
	
	private JProgressBar progressBar;
	private int simSteps = 200; // ile kroków symulacji
	private ArrayList<ArrayList<Integer>> placesRawData;
	private ArrayList<ArrayList<Integer>> transitionsRawData;

	private ArrayList<Integer> placesInChart;
	private ArrayList<String> placesInChartStr;
	
	private XYSeriesCollection placesSeriesDataSet = null;
	private XYSeriesCollection transitionsSeriesDataSet = null;
	private JFreeChart placesChart;
	private JFreeChart transitionsChart;
	
	private boolean listenerStart = false;
	private JPanel mainChartPanel;
	private JPanel toolPanel;
	private JTabbedPane tabbedPane; //zakładki
	private JFrame ego;
	
	public AbyssStateSimulator() {
		ego = this;
		ssim = new StateSimulator();
		placesRawData = new ArrayList<ArrayList<Integer>>();
		transitionsRawData = new ArrayList<ArrayList<Integer>>();
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
		
		toolPanel = crateToolsPanel(); //panel opcji i przycisków
		main.add(toolPanel);

		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, toolPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-toolPanel.getHeight()-40);
		ImageIcon icon = Tools.getResIcon16("images/middle.gif");
		tabbedPane.addTab("Places dynamics", icon, createPlacesTabPanel(), "Places dynamics");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Transistions dynamics", icon, makeTextPanel("ghgxhxgh"), "Transistions dynamics");
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
		
		JPanel chartOptionsPanel = new JPanel(null);
		chartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Chart options"));
		chartOptionsPanel.setBounds(0, 0, 600, 80);
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
					
					addNewPlaceSeries(selected, name);
					updateGraphic();
				}
			}
		});
		chartOptionsPanel.add(addPlaceButton);
		
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
		chartOptionsPanel.add(removePlaceButton);
		
		JButton clearChartButton = new JButton("Clear");
		clearChartButton.setBounds(posXchart+240, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		clearChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearAllData();
			}
		});
		chartOptionsPanel.add(clearChartButton);
		
		JButton saveChartButton = new JButton("Save");
		saveChartButton.setBounds(posXchart+400, posYchart+2, 110, 20);
		//acqDataButton.setIcon(Tools.getResIcon32(""));
		saveChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveChartImage();
			}
		});
		chartOptionsPanel.add(saveChartButton);
		result.add(chartOptionsPanel);
				
		
		mainChartPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
		 	//my zmieniać wymiary jeśli całe okno ma zmieniane w dowolnej chwili
		mainChartPanel.setBorder(BorderFactory.createTitledBorder("Charts"));
		mainChartPanel.setBounds(0, 80, this.getWidth()-20, this.getHeight()-toolPanel.getHeight()-40);
		mainChartPanel.add(createLineChartPanel(), BorderLayout.CENTER);
		result.add(mainChartPanel);
		
		return result;
	}
	
	/**
	 * Metoda tworzy panel opcji okna symulatora stanów sieci.
	 * @return JPanel - panel
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
		
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(200, 0, Integer.MAX_VALUE, 10);
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
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
	    dataAcquisitionPanel.add(progressBar);
		result.add(dataAcquisitionPanel);
		
		return result;
	}

	/**
	 * Metoda zapisująca aktualnie wyświetlany wykres do pliku.
	 */
	private void saveChartImage() {
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
	 * Metoda wypełnia okno danymi.
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
	
	/**
	 * Metoda ta tworzy wykres liniowy dla miejsc.
	 * @return JPanel - panel z wykresem
	 */
	private JPanel createLineChartPanel() {
		createSimpleDataset();
		
	    String chartTitle = "Places dynamics";
	    String xAxisLabel = "Tokens";
	    String yAxisLabel = "Step";
	    
	    boolean showLegend = true;
	    boolean createURL = false;
	    boolean createTooltip = true;

	    placesChart = ChartFactory.createXYLineChart(chartTitle,
	            xAxisLabel, yAxisLabel, placesSeriesDataSet,
	            PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

	    ChartPanel res = new ChartPanel(placesChart);
	    return res;
	}
	
	/**
	 * Metoda uaktualniania stylu wyświetlania
	 */
	private void updateGraphic() {
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
			plot.getRenderer().setSeriesStroke(i, new BasicStroke(2.0f));
		}
		//plot.setRenderer(renderer);
	}
	
	/**
	 * Metoda dodaje nową linię do wykresu.
	 * @param selected int - indeks miejsca
	 * @param name String - nazwa miejsca
	 */
	private void addNewPlaceSeries(int selected, String name) {
		@SuppressWarnings("unchecked")
		List<XYSeries> x = placesSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name))
				return;
		}
		
		XYSeries series = new XYSeries(name);
		ArrayList<Integer> test = new ArrayList<Integer>();
		for(int step=0; step<placesRawData.size(); step++) {
			int value = placesRawData.get(step).get(selected);
			test.add(value);
			series.add(step, value);
		}
		placesSeriesDataSet.addSeries(series);
	}
	
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
	
	private XYDataset createSimpleDataset() {
		placesSeriesDataSet = new XYSeriesCollection();
		return placesSeriesDataSet;
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
    	
    	listenerStart = true;
		addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	if(!listenerStart)
            		return;
            	tabbedPane.setBounds(0, toolPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-toolPanel.getHeight()-40);
            	mainChartPanel.setBounds(0, 80, ego.getWidth()-30, ego.getHeight()-toolPanel.getHeight()-150);
            }
            public void componentMoved(ComponentEvent e) {
            	if(!listenerStart)
            		return;
            	tabbedPane.setBounds(0, toolPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-toolPanel.getHeight()-40);
            	mainChartPanel.setBounds(0, 80, ego.getWidth()-30, ego.getHeight()-toolPanel.getHeight()-150);
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
		
		placesRawData.clear();
		transitionsRawData.clear();
		
		placesRawData = ssim.getPlacesData();
		transitionsRawData = ssim.getTransitionsData();
		
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
	}

	/**
	 * Metoda czyszcząca dane okna.
	 */
	private void clearAllData() {
		placesSeriesDataSet.removeAllSeries();
		
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
