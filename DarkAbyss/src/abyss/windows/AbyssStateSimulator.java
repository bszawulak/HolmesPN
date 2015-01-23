package abyss.windows;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Insets;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
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
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
	private ArrayList<Double> transAvgData;
	private ArrayList<Double> placesAvgData;
	private int transInterval = 10;
	private ArrayList<Integer> placesInChart;
	private ArrayList<String> placesInChartStr;
	
	private XYSeriesCollection placesSeriesDataSet = null;
	private XYSeriesCollection transitionsSeriesDataSet = null;
	private JFreeChart placesChart;
	private JFreeChart transitionsChart;
	private int transChartType = 0; //suma odpaleń, 1=konkretne tranzycje
	private int placesChartType = 0; //j.w. dla miejsc
	
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
	
	private ChartProperties chartDetails;
	
	/**
	 * Konstruktor domyślny obiektu klasy StateSimulator (podokna Abyss)
	 */
	public AbyssStateSimulator() {
		ego = this;
		ssim = new StateSimulator();
		chartDetails = new ChartProperties();
		placesRawData = new ArrayList<ArrayList<Integer>>();
		transitionsRawData = new ArrayList<ArrayList<Integer>>();
		transitionsCompactData = new ArrayList<Integer>();
		placesInChart = new ArrayList<Integer>();
		placesInChartStr = new ArrayList<String>();
		transAvgData = new ArrayList<Double>();
		placesAvgData = new ArrayList<Double>();
		
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
		ImageIcon icon = Tools.getResIcon16("/icons/stateSim/placesDyn.png");
		tabbedPane.addTab("Places dynamics", icon, createPlacesTabPanel(), "Places dynamics");
		
		ImageIcon icon2 = Tools.getResIcon16("/icons/stateSim/transDyn.png");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Transitions dynamics", icon2, createTransitionsTabPanel(), "Transistions dynamics");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		main.add(tabbedPane);
		initiateListeners(); //all hail Sithis
		repaint();
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
		
		JButton acqDataButton = new JButton("SimStart");
		acqDataButton.setBounds(posXda, posYda, 110, 30);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				acquireDataFromSimulation();
			}
		});
		dataAcquisitionPanel.add(acqDataButton);
		
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(simSteps, 0, 1000000, 100);
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
		
		JLabel label1 = new JLabel("Mode:");
		label1.setBounds(posXda+210, posYda+5, 50, 20);
		dataAcquisitionPanel.add(label1);
		
		final JComboBox<String> simMode = new JComboBox<String>(new String[] {"Maximum mode", "50/50 mode"});
		simMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
		simMode.setBounds(posXda+250, posYda, 120, 30);
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
		
		JButton clearDataButton = new JButton("Clear");
		clearDataButton.setBounds(posXda+380, posYda, 110, 30);
		clearDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/clearData.png"));
		clearDataButton.setToolTipText("Clear all charts and data vectors. Reset simulator.");
		clearDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearPlacesChart();
				clearTransitionsChart();
				clearAllData();
			}
		});
		dataAcquisitionPanel.add(clearDataButton);
		
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
	 * Metoda ta tworzy panel dla zakładki miejsc.
	 * @return JPanel - panel
	 */
	private JPanel createPlacesTabPanel() {
		JPanel result = new JPanel(null);
		result.setBounds(0, 0, this.getWidth()-20, 180);
		
		placesChartOptionsPanel = new JPanel(null);
		placesChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Places chart options"));
		placesChartOptionsPanel.setBounds(0, 0, 610, 120);
		int posXchart = 10;
		int posYchart = 20;
		
		JButton showAllButton = new JButton("Show all");
		showAllButton.setBounds(posXchart, posYchart, 100, 24);
		showAllButton.setMargin(new Insets(0, 0, 0, 0));
		showAllButton.setIcon(Tools.getResIcon16("/icons/stateSim/showAll.png"));
		showAllButton.setToolTipText("Show average numbers of token in places through simulation steps.");
		showAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showAllPlacesData();
			}
		});
		placesChartOptionsPanel.add(showAllButton);
		posYchart += 30;
		
		JLabel label1 = new JLabel("Places:");
		label1.setBounds(posXchart, posYchart, 70, 20);
		placesChartOptionsPanel.add(label1);
		
		String[] dataP = { "---" };
		placesCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		placesCombo.setLocation(posXchart + 75, posYchart+2);
		placesCombo.setSize(500, 20);
		placesCombo.setSelectedIndex(0);
		placesCombo.setMaximumRowCount(12);
		placesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
			}
		});
		placesChartOptionsPanel.add(placesCombo);
		
		posYchart += 30;
		
		JButton addPlaceButton = new JButton("Add to chart");
		addPlaceButton.setBounds(posXchart, posYchart+2, 110, 24);
		addPlaceButton.setMargin(new Insets(0, 0, 0, 0));
		addPlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/addChart.png"));
		addPlaceButton.setToolTipText("Add data about place tokens to the chart.");
		addPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(placesRawData.size() == 0)
					return;
				
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					String name = placesCombo.getSelectedItem().toString();
					name = trimNodeName(name);
					placesInChart.set(selected, 1);
					placesInChartStr.set(selected, name);
					
					addNewPlaceSeries(selected, name);
					updatePlacesGraphicChart("places");
				}
			}
		});
		placesChartOptionsPanel.add(addPlaceButton);
		
		JButton removePlaceButton = new JButton("Remove");
		removePlaceButton.setBounds(posXchart+120, posYchart+2, 110, 24);
		removePlaceButton.setMargin(new Insets(0, 0, 0, 0));
		removePlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/removeChart.png"));
		removePlaceButton.setToolTipText("Remove data about place tokens from the chart.");
		removePlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(placesRawData.size() == 0)
					return;
				
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					String name = placesCombo.getSelectedItem().toString();
					name = trimNodeName(name);
					placesInChart.set(selected, -1);
					placesInChartStr.set(selected, "");
					
					removePlaceSeries(name);
				}
			}
		});
		placesChartOptionsPanel.add(removePlaceButton);
		
		JButton clearPlacesChartButton = new JButton("Clear chart");
		clearPlacesChartButton.setBounds(posXchart+240, posYchart+2, 110, 24);
		clearPlacesChartButton.setMargin(new Insets(0, 0, 0, 0));
		clearPlacesChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/clearChart.png"));
		clearPlacesChartButton.setToolTipText("Clears the chart.");
		clearPlacesChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearPlacesChart();
			}
		});
		placesChartOptionsPanel.add(clearPlacesChartButton);
		
		JButton savePlacesChartButton = new JButton("Save Image");
		savePlacesChartButton.setBounds(posXchart+360, posYchart+2, 110, 24);
		savePlacesChartButton.setMargin(new Insets(0, 0, 0, 0));
		savePlacesChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/saveImage.png"));
		savePlacesChartButton.setToolTipText("Saves the chart as image file.");
		savePlacesChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveChartImage("places", 1200, 1024);
			}
		});
		placesChartOptionsPanel.add(savePlacesChartButton);
		
		JButton showPlaceButton = new JButton("Find place");
		showPlaceButton.setBounds(posXchart+480, posYchart+2, 110, 24);
		showPlaceButton.setMargin(new Insets(0, 0, 0, 0));
		showPlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/findNode.png"));
		showPlaceButton.setToolTipText("Find selected place within the net.");
		showPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					//String name = placesCombo.getSelectedItem().toString();
					//name = trimNodeName(name);
					GUIManager.getDefaultGUIManager().getSearchWindow().fillComboBoxesData();
					GUIManager.getDefaultGUIManager().getSearchWindow().selectedManually(true, selected);
				}
			}
		});
		placesChartOptionsPanel.add(showPlaceButton);
		result.add(placesChartOptionsPanel);
		
		//************************************************************************************************************
		//************************************************************************************************************
		
		JPanel placesChartGraphicPanel = new JPanel(null);
		placesChartGraphicPanel.setBorder(BorderFactory.createTitledBorder("Chart graphic"));
		placesChartGraphicPanel.setBounds(placesChartOptionsPanel.getWidth(), 
				0, 200, placesChartOptionsPanel.getHeight());
		int posXGchart = 10;
		int posYGchart = 20;
		result.add(placesChartGraphicPanel);
		
		ButtonGroup groupWidth = new ButtonGroup();
		JRadioButton width1 = new JRadioButton("Width 1");
		width1.setBounds(posXGchart, posYGchart, 70, 20);
		width1.setActionCommand("0");
		width1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.p_StrokeWidth = 1.0f;
				updatePlacesGraphicChart("places");
			}
		}});
		placesChartGraphicPanel.add(width1);
		groupWidth.add(width1);
		groupWidth.setSelected(width1.getModel(), true);
		
		JRadioButton width2 = new JRadioButton("Width 2");
		width2.setBounds(posXGchart, posYGchart+20, 70, 20);
		width2.setActionCommand("1");
		width2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.p_StrokeWidth = 2.0f;
				updatePlacesGraphicChart("places");
			}
		}});
		placesChartGraphicPanel.add(width2);
		groupWidth.add(width2);
		
		JRadioButton width3 = new JRadioButton("Width 3");
		width3.setBounds(posXGchart, posYGchart+40, 70, 20);
		width3.setActionCommand("2");
		width3.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.p_StrokeWidth = 3.0f;
				updatePlacesGraphicChart("places");
			}
		}});
		placesChartGraphicPanel.add(width3);
		groupWidth.add(width3);
				
		//************************************************************************************************************
		//************************************************************************************************************
		
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
		transChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart options"));
		transChartOptionsPanel.setBounds(0, 0, 610, 120);
		int posXchart = 10;
		int posYchart = 20;
		
		JButton showAllButton = new JButton("Show all");
		showAllButton.setBounds(posXchart, posYchart, 100, 24);
		showAllButton.setMargin(new Insets(0, 0, 0, 0));
		showAllButton.setIcon(Tools.getResIcon16("/icons/stateSim/showAll.png"));
		showAllButton.setToolTipText("Show average numbers of firings of transitions through simulation steps.");
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
				clearTransitionsChart();
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
		transitionsCombo.setMaximumRowCount(12);
		transitionsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//int selected = placesCombo.getSelectedIndex();
			}
		});
		transChartOptionsPanel.add(transitionsCombo);
		
		posYchart += 30;
		
		JButton addTransitionButton = new JButton("Add to chart");
		addTransitionButton.setBounds(posXchart, posYchart+2, 110, 24);
		addTransitionButton.setMargin(new Insets(0, 0, 0, 0));
		addTransitionButton.setIcon(Tools.getResIcon16("/icons/stateSim/addChart.png"));
		addTransitionButton.setToolTipText("Add data about transition firing to the chart.");
		addTransitionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitionsRawData.size() == 0)
					return;
				
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					String name = transitionsCombo.getSelectedItem().toString();
					name = trimNodeName(name);
					addNewTransitionSeries(selected, name);
					//updateTransitionsGraphicChart();
				}
			}
		});
		transChartOptionsPanel.add(addTransitionButton);
		
		JButton removeTransitionButton = new JButton("Remove");
		removeTransitionButton.setBounds(posXchart+120, posYchart+2, 110, 24);
		removeTransitionButton.setMargin(new Insets(0, 0, 0, 0));
		removeTransitionButton.setIcon(Tools.getResIcon16("/icons/stateSim/removeChart.png"));
		removeTransitionButton.setToolTipText("Remove data about transition firing from the chart.");
		removeTransitionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitionsRawData.size() == 0)
					return;
				
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					//selected--;
					String name = transitionsCombo.getSelectedItem().toString();
					name = trimNodeName(name);
					removeTransitionSeries(name);
				}
			}
		});
		transChartOptionsPanel.add(removeTransitionButton);
		
		JButton clearTransChartButton = new JButton("Clear chart");
		clearTransChartButton.setBounds(posXchart+240, posYchart+2, 110, 24);
		clearTransChartButton.setMargin(new Insets(0, 0, 0, 0));
		clearTransChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/clearChart.png"));
		clearTransChartButton.setToolTipText("Clears the transitions chart.");
		clearTransChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearTransitionsChart();
			}
		});
		transChartOptionsPanel.add(clearTransChartButton);
		
		JButton saveTransitionsChartButton = new JButton("Save Image");
		saveTransitionsChartButton.setBounds(posXchart+360, posYchart+2, 110, 24);
		saveTransitionsChartButton.setMargin(new Insets(0, 0, 0, 0));
		saveTransitionsChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/saveImage.png"));
		saveTransitionsChartButton.setToolTipText("Saves the chart as image file.");
		saveTransitionsChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveChartImage("transitions", 1200, 1024);
			}
		});
		transChartOptionsPanel.add(saveTransitionsChartButton);
		
		JButton showTransButton = new JButton("Find trans.");
		showTransButton.setBounds(posXchart+480, posYchart+2, 110, 24);
		showTransButton.setMargin(new Insets(0, 0, 0, 0));
		showTransButton.setIcon(Tools.getResIcon16("/icons/stateSim/findNode.png"));
		showTransButton.setToolTipText("Find selected transition within the net.");
		showTransButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					selected--;
					//String name = placesCombo.getSelectedItem().toString();
					//name = trimNodeName(name);
					GUIManager.getDefaultGUIManager().getSearchWindow().fillComboBoxesData();
					GUIManager.getDefaultGUIManager().getSearchWindow().selectedManually(false, selected);
				}
			}
		});
		transChartOptionsPanel.add(showTransButton);
		result.add(transChartOptionsPanel);
		
		//************************************************************************************************************
		//************************************************************************************************************
				
		JPanel transChartGraphicPanel = new JPanel(null);
		transChartGraphicPanel.setBorder(BorderFactory.createTitledBorder("Chart graphic"));
		transChartGraphicPanel.setBounds(transChartOptionsPanel.getWidth(), 
				0, 200, transChartOptionsPanel.getHeight());
		int posXGchart = 10;
		int posYGchart = 20;
		result.add(transChartGraphicPanel);
		
		ButtonGroup groupWidth = new ButtonGroup();
		JRadioButton width1 = new JRadioButton("Width 1");
		width1.setBounds(posXGchart, posYGchart, 70, 20);
		width1.setActionCommand("0");
		width1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.t_StrokeWidth = 1.0f;
				updatePlacesGraphicChart("transitions");
			}
		}});
		transChartGraphicPanel.add(width1);
		groupWidth.add(width1);
		groupWidth.setSelected(width1.getModel(), true);
		
		JRadioButton width2 = new JRadioButton("Width 2");
		width2.setBounds(posXGchart, posYGchart+20, 70, 20);
		width2.setActionCommand("1");
		width2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.t_StrokeWidth = 2.0f;
				updatePlacesGraphicChart("transitions");
			}
		}});
		transChartGraphicPanel.add(width2);
		groupWidth.add(width2);
		
		JRadioButton width3 = new JRadioButton("Width 3");
		width3.setBounds(posXGchart, posYGchart+40, 70, 20);
		width3.setActionCommand("2");
		width3.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.t_StrokeWidth = 3.0f;
				updatePlacesGraphicChart("transitions");
			}
		}});
		transChartGraphicPanel.add(width3);
		groupWidth.add(width3);
						
		//************************************************************************************************************
		//************************************************************************************************************
		
		transitionsChartPanel = new JPanel(new BorderLayout());
		transitionsChartPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart"));
		transitionsChartPanel.setBounds(0, transChartOptionsPanel.getHeight(), this.getWidth()-20, 
				tabbedPane.getHeight() - transChartOptionsPanel.getHeight()-40);
		
		transitionsChartPanel.add(createTransChartPanel(), BorderLayout.CENTER);
		result.add(transitionsChartPanel);

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
	 * Metoda dodaje nową linię do wykresu.
	 * @param selPlaceID int - indeks miejsca
	 * @param name String - nazwa miejsca
	 */
	private void addNewPlaceSeries(int selPlaceID, String name) {
		if(placesChartType == 0) { //replace chart
			placesChartPanel.removeAll();
			placesChartPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
			placesChartPanel.revalidate();
			placesChartPanel.repaint();
			placesChartType = 1;
			
			placesSeriesDataSet.removeAllSeries(); // ??
		}
		
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
	
	/**
	 * Metoda ta pokazuje na wykresie średnią liczbę tokenów w miejscach
	 */
	private void showAllPlacesData() {
		if(placesAvgData.size() == 0)
			return;

		placesChartType = 0;
		//int steps = placesRawData.size();
		double max = 0.0;
		for(int p=0; p<placesAvgData.size(); p++) {
			if(placesAvgData.get(p) > max)
				max = placesAvgData.get(p);
		}
		
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    for(int t=0; t<placesAvgData.size(); t++) {
			String tName = "t"+t+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(t).getName();
			double value = placesAvgData.get(t);
			dataset.addValue(value, "Firing", tName);
			dataset.addValue((int)(max-value), "NotFiring", tName);
		}
	    
	    String chartTitle = "Places dynamics";
	    String xAxisLabel = "Place";
	    String yAxisLabel = "Tokens";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
		placesChart = ChartFactory.createStackedBarChart(chartTitle, xAxisLabel, yAxisLabel, dataset,
			PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		
		CategoryPlot plot = (CategoryPlot) placesChart.getPlot();
		LogAxis yAxis = new LogAxis("Tokens");
		yAxis.setLowerBound(0.9);
		yAxis.setUpperBound(max+10);
		yAxis.setBase(10.0);
		yAxis.setMinorTickMarksVisible(true);
		//plot.setRangeAxis(yAxis);
		
	    StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
	    //CategoryItemRenderer renderer = plot.getRenderer();
	    renderer.setBase(1);
		
		//CategoryItemRenderer renderer = plot.getRenderer();
		Paint p1 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, Color.red);
        renderer.setSeriesPaint(0, p1);
        Paint p2 = new GradientPaint(0.0f, 0.0f, Color.lightGray, 0.0f, 0.0f, Color.lightGray);
        renderer.setSeriesPaint(1, p2);
        plot.setRenderer(renderer);

		placesChartPanel.removeAll();
		placesChartPanel.add(new ChartPanel(placesChart), BorderLayout.CENTER);
		placesChartPanel.revalidate();
		placesChartPanel.repaint();
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
		//int counter = 0;
		for(int step=0; step<transitionsRawData.size(); step++) {
			if(transInterval > (transitionsRawData.size()/10)) {
				transInterval = transitionsRawData.size()/10;
				
			}
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
			//counter++;
		}
		transitionsSeriesDataSet.addSeries(series);
	}
	
	/**
	 * Metoda ta usuwa z wykresu dane o wskazanym miejscu.
	 * @param name String - nazwa miejsca (unikalna)
	 */
	private void removeTransitionSeries(String name) {
		@SuppressWarnings("unchecked")
		List<XYSeries> x = transitionsSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name)) {
				transitionsSeriesDataSet.removeSeries(xys);
				return;
			}
		}
	}
	
	/**
	 * Metoda czyszcząca dane okna.
	 */
	private void clearTransitionsChart() {
		if(transitionsSeriesDataSet.getSeriesCount() > 0)
			transitionsSeriesDataSet.removeAllSeries();	
	}
	
	/**
	 * Metoda ta pokazuje na wykresie tranzycji wszystkie odpalenia tranzycji dla obliczonej
	 * liczby kroków symulacji.
	 */
	private void showAllTransData() {
		if(transitionsCompactData.size() == 0)
			return;

		transChartType = 0;
		//int steps = transitionsRawData.size();
		
		double max = 0.0;
		for(int p=0; p<transitionsCompactData.size(); p++) {
			if(transitionsCompactData.get(p) > max)
				max = transitionsCompactData.get(p);
		}
		
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    for(int t=0; t<transitionsCompactData.size(); t++) {
			String tName = "t"+t+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(t).getName();
			int value = transitionsCompactData.get(t);
			dataset.addValue(value, "Firing", tName);
			dataset.addValue(max-value, "NotFiring", tName);
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
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************      OGÓLNE      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda zapisująca aktualnie wyświetlany wykres do pliku.
	 */
	private void saveChartImage(String chartType, int w, int h) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Portable Network Graphics (.png)", new String[] { "PNG" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "");
		if(selectedFile.equals(""))
			return;
		
		if(!selectedFile.contains(".png"))
			selectedFile += ".png";
		
		File imageFile = new File(selectedFile);
		int width = w; //1024;
		int height = h; //768;
		 
		try {
			if(chartType.equals("places")) {
				ChartUtilities.saveChartAsPNG(imageFile, placesChart, width, height);
			} else if(chartType.equals("transitions")) {
				ChartUtilities.saveChartAsPNG(imageFile, transitionsChart, width, height);
			}
		} catch (IOException ex) {
		    System.err.println(ex);
		}
	}
	
	/**
	 * Metoda wypełnia komponenty rozwijalne danymi o miejscach i tranzycjach.
	 */
	private void fillPlacesAndTransitionsData() {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places== null || places.size() == 0)
			return;
		
		placesCombo.removeAllItems();
		placesCombo.addItem("---");
		if(placesAvgData.size() == places.size()) {
			for(int p=0; p < places.size(); p++) {
				placesCombo.addItem("p"+(p)+"."+places.get(p).getName() + " "+formatD(placesAvgData.get(p)));
			}
		} else {
			for(int p=0; p < places.size(); p++) {
				placesCombo.addItem("p"+(p)+"."+places.get(p).getName());
			}
		}
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions== null || transitions.size() == 0)
			return;
		
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		
		if(transAvgData.size() == transitions.size()) {
			for(int t=0; t < transitions.size(); t++) {
				transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName() + " "+formatD(transAvgData.get(t)));
			}
		} else {
			for(int t=0; t < transitions.size(); t++) {
				transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
			}
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
    	
    	//poniżej dynamiczne :) ustalanie wielkości i ustawienia głównych paneli w ramach zakładek
		addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	if(!listenerStart)
            		return;
            	tabbedPane.setBounds(0, dataToolsPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-dataToolsPanel.getHeight()-40);
            	placesChartPanel.setBounds(0, placesChartOptionsPanel.getHeight(), ego.getWidth()-30, 
            			tabbedPane.getHeight() - placesChartOptionsPanel.getHeight()-40);
            	transitionsChartPanel.setBounds(0, transChartOptionsPanel.getHeight(), ego.getWidth()-30, 
            			tabbedPane.getHeight() - transChartOptionsPanel.getHeight()-40);
            }
            public void componentMoved(ComponentEvent e) {
            	if(!listenerStart)
            		return;
            	tabbedPane.setBounds(0, dataToolsPanel.getHeight(), ego.getWidth()-20, ego.getHeight()-dataToolsPanel.getHeight()-40);
            	placesChartPanel.setBounds(0, placesChartOptionsPanel.getHeight(), ego.getWidth()-30, 
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
	private void acquireDataFromSimulation() {
		clearTransitionsChart();
		clearPlacesChart();
		clearAllData();
		
		progressBar.setMaximum(simSteps);
		ssim.initiateSim(NetType.BASIC, maximumMode);
		ssim.simulateNet(simSteps, progressBar);
		
		//pobieranie wektorów danych zebranych w symulacji:
		placesRawData = ssim.getPlacesData();
		placesAvgData = ssim.getPlacesAvgData();
		transitionsRawData = ssim.getTransitionsData();
		transitionsCompactData = ssim.getTransitionsCompactData();
		transAvgData = ssim.getTransitionsAvgData();
		
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
		
		fillPlacesAndTransitionsData();
	}
	
	/**
	 * Metoda czyści wszystkie sturktury danych.
	 */
	private void clearAllData() {
		ssim = new StateSimulator();
		placesRawData.clear();
		placesAvgData.clear();
		transitionsRawData.clear();
		transitionsCompactData.clear();
		transAvgData.clear();
	}
	
	/**
	 * Konwersja liczby double do określonej długości łańcucha znaków.
	 * @param value double - liczba do konwersji
	 * @return String - liczba sformatowana
	 */
	private String formatD(double value) {
        DecimalFormat df = new DecimalFormat("#.#######");
        String txt = "(avg: ";
        txt += df.format(value);
        txt += ")";
        txt = txt.replace(",", ".");
		return txt;
	}
	
	/**
	 * Metoda przycina nazwę węzła, obcinając informacje o średniej liczbie wystąpień
	 * w symulacji stanów.
	 * @param name String - nazwa z comboBox
	 * @return String - przycięta nazwa
	 */
	private String trimNodeName(String name) {
		int i = name.indexOf("(avg");
		if(i<0) {
			return name;
		} else {
			name = name.substring(0, i-1);
			return name;
		}
	}

	/*
	protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        //panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        panel.setBounds(0, 0, 640, 480);
        return panel;
    }
    */
	
	/**
	 * Metoda uaktualniania stylu wyświetlania
	 */
	private void updatePlacesGraphicChart(String chartType) {
		if(chartType.equals("places")) {
			if(placesChartType == 0)
				return;
			
			XYPlot plot = placesChart.getXYPlot();
			int count =  placesChart.getXYPlot().getSeriesCount();
			for(int i=0; i<count; i++) {
				plot.getRenderer().setSeriesStroke(i, new BasicStroke(chartDetails.p_StrokeWidth));
			}
		} else if(chartType.equals("transitions")){
			if(transChartType == 0)
				return;
			
			XYPlot plot = transitionsChart.getXYPlot();
			int count =  transitionsChart.getXYPlot().getSeriesCount();
			for(int i=0; i<count; i++) {
				plot.getRenderer().setSeriesStroke(i, new BasicStroke(chartDetails.t_StrokeWidth));
			}
		}
		
		//XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		//renderer.setSeriesLinesVisible(0, true);
        //renderer.setSeriesShapesVisible(0, false);
        //renderer.setSeriesLinesVisible(1, false);
        //renderer.setSeriesShapesVisible(1, true); 
		 //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    //plot.setRenderer(renderer);
	    //plot.setBackgroundPaint(Color.DARK_GRAY);
		//plot.setRenderer(renderer);
	}
	
	private class ChartProperties {
		public float p_StrokeWidth = 1.0f;
		public float t_StrokeWidth = 1.0f;
		
		public ChartProperties() {
			
		}
	}
}
