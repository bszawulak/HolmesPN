package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeriesCollection;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.StateSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.utilities.Tools;

/**
 * Klasa odpowiedzialna za okno właściwości elementu sieci.
 * @author MR
 *
 */
public class AbyssNodeInfo extends JFrame {
	//BACKUP: serialVersionUID = 1476738825515760744L; nie ruszać poniższej zmiennej
	private static final long serialVersionUID = 1476738825515760744L;
	private Place place;
	private Transition transition;
	private JPanel mainPanel;
	private JFrame parentFrame;
	private AbyssNodeInfoActions action = new AbyssNodeInfoActions(this);
	public boolean mainSimulatorActive = false;
	
	private XYSeriesCollection dynamicsSeriesDataSet = null;
	private JFreeChart dynamicsChart;
	
	/**
	 * Konstruktor do tworzenia okna właściwości miejsca.
	 * @param place Place - obiekt miejsca
	 * @param papa JFrame - okno wywołujące
	 */
	public AbyssNodeInfo(Place place, JFrame papa) {
		parentFrame = papa;
		this.place = place;
		setTitle("Node: "+place.getName());

		initializeCommon();
		initializePlaceInfo();
	}
	
	/**
	 * Konstruktor do tworzenia okna właściwości tranzycji.
	 * @param transition Transition - obiekt tranzycji
	 * @param papa JFrame - okno wywołujące
	 */
	public AbyssNodeInfo(Transition transition, JFrame papa) {
		parentFrame = papa;
		this.transition = transition;
		setTitle("Node: "+transition.getName());
		
		initializeCommon();
		initializeTransitionInfo();
	}
	
	/**
	 * Metoda agregująca główne, wspólne elementy interfejsu miejsc/tranzycji.
	 */
	private void initializeCommon() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		
		if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getMode() != SimulatorMode.STOPPED)
			mainSimulatorActive = true;
		
		parentFrame.setEnabled(false);
		setResizable(false);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
	}

	/**
	 * Metoda odpowiedzialna za elementy interfejsu właściwości dla miejsca sieci.
	 */
	private void initializePlaceInfo() {
		this.setLocation(20, 20);
		setSize(new Dimension(600, 500));

		mainPanel = new JPanel(null);
		mainPanel.setBounds(0, 0, 600, 500);
		
		int mPanelX = 0;
		int mPanelY = 0;
		
		//panel informacji podstawowych
		JPanel infoPanel = new JPanel(null);
		infoPanel.setBounds(mPanelX, mPanelY, mainPanel.getWidth()-10, 130);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Place general information:"));
		
		int infPanelX = 10;
		int infPanelY = 20;
		
		//************************* NEWLINE *************************
		
		JLabel labelID = new JLabel("ID:");
		labelID.setBounds(infPanelX, infPanelY, 20, 20);
		infoPanel.add(labelID);
		
		int id = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().indexOf(place);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
		idTextBox.setEditable(false);
		infoPanel.add(idTextBox);
		
		JLabel portalLabel = new JLabel("Portal:");
		portalLabel.setBounds(infPanelX+60, infPanelY, 40, 20);
		infoPanel.add(portalLabel);
		
		String port = "no";
		if(place.isPortal() == true) 
			port = "yes";
		
		JLabel portalLabel2 = new JLabel(port);
		portalLabel2.setBounds(infPanelX+100, infPanelY, 30, 20);
		infoPanel.add(portalLabel2);
		
		JLabel tokenLabel = new JLabel("Tokens:", JLabel.LEFT);
        tokenLabel.setBounds(infPanelX+130, infPanelY, 50, 20);
        infoPanel.add(tokenLabel);
        
        int tok = place.getTokensNumber();
        boolean problem = false;
        if(tok < 0) {
        	GUIManager.getDefaultGUIManager().log("Negative number of tokens in "+place.getName(), "error", true);
        	tok = 0;
        	problem = true;
        }
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok, 0, Integer.MAX_VALUE, 1);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setLocation(infPanelX+180, infPanelY);
		tokenSpinner.setSize(60, 20);
		tokenSpinner.setMaximumSize(new Dimension(60,20));
		tokenSpinner.setMinimumSize(new Dimension(60,20));
		if(mainSimulatorActive || problem) {
			tokenSpinner.setEnabled(false);
		}
		tokenSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokens = (int) spinner.getValue();
				action.setTokens(place, tokens);
			}
		});
		infoPanel.add(tokenSpinner);
		
		
		int inTrans = 0;
		int outTrans = 0;
		for (ElementLocation el : place.getElementLocations()) {
			inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
			outTrans += el.getOutArcs().size();
		}
		
		JLabel inTransLabel = new JLabel("IN-Trans:");
		inTransLabel.setBounds(infPanelX+250, infPanelY, 60, 20);
		infoPanel.add(inTransLabel);
		
		JFormattedTextField inTransTextBox = new JFormattedTextField(inTrans);
		inTransTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
		inTransTextBox.setEditable(false);
		infoPanel.add(inTransTextBox);
		
		JLabel outTransLabel = new JLabel("OUT-Trans:");
		outTransLabel.setBounds(infPanelX+345, infPanelY, 65, 20);
		infoPanel.add(outTransLabel);
		
		JFormattedTextField outTransTextBox = new JFormattedTextField(outTrans);
		outTransTextBox.setBounds(infPanelX+420, infPanelY, 30, 20);
		outTransTextBox.setEditable(false);
		infoPanel.add(outTransTextBox);
		
		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel labelName = new JLabel("Name:");
		labelName.setBounds(infPanelX, infPanelY, 40, 20);
		infoPanel.add(labelName);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(infPanelX+60, infPanelY);
		nameField.setSize(460, 20);
		nameField.setValue(place.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				
				action.changeName(place, newName);
				action.parentTableUpdate(parentFrame, newName);
			}
		});
		infoPanel.add(nameField);

		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel commmentLabel = new JLabel("Comm.:", JLabel.LEFT);
		commmentLabel.setBounds(infPanelX, infPanelY, 50, 20);
		infoPanel.add(commmentLabel);
		
		JTextArea commentField = new JTextArea(place.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();
            	
				action.changeComment(place, newComment);
            }
        });
		
        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+60, infPanelY, 460, 60);

        infoPanel.add(creationPanel);
        infPanelY += 60;
        
		mainPanel.add(infoPanel);
		
		
		JPanel placesJPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
		placesJPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
		placesJPanel.setBounds(0, infoPanel.getHeight(), mainPanel.getWidth()-10, 250);
		placesJPanel.add(createPlacesChartPanel(place), BorderLayout.CENTER);
		mainPanel.add(placesJPanel);
		
		//SS, chart:
		if(mainSimulatorActive == false) {
			
		} else {
			placesJPanel.setEnabled(false);
			TextTitle title = dynamicsChart.getTitle();   
			//title.setBorder(0, 0, 2, 0);   
			//title.setBackgroundPaint(Color.white); 
			title.setFont(new Font("Dialog", Font.PLAIN, 20));
			title.setExpandToFitSpace(true);
			title.setPaint(Color.red);
			title.setText("Chart unavailable, main simulator is active.");
	           
			//dynamicsChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 18));
			//dynamicsChart.setTitle("Chart unavailable, main simulator is active.");
		}

		add(mainPanel);
	}
	
	JPanel createPlacesChartPanel(Node node) {
		String chartTitle = node.getName()+ " dynamics";
	    String xAxisLabel = "Simulation steps";
	    String yAxisLabel = "Tokens";
	    if(node instanceof Transition)
	    	yAxisLabel = "Firings";
	    
	    boolean showLegend = false;
	    boolean createTooltip = true;
	    boolean createURL = false;
	    
		dynamicsSeriesDataSet = new XYSeriesCollection();
	    dynamicsChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dynamicsSeriesDataSet, 
	    		PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
	    
	    dynamicsChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));
	    //NOT UNTIL PLOT IN PLACE:
	    //CategoryPlot plot = (CategoryPlot) placesChart.getPlot();
        //Font font = new Font("Dialog", Font.PLAIN, 12); 
      	//plot.getDomainAxis().setLabelFont(font);
      	//plot.getRangeAxis().setLabelFont(font);
	
	    ChartPanel placesChartPanel = new ChartPanel(dynamicsChart);
	    return placesChartPanel;
	}
	
	private void initializeTransitionInfo() {
		this.setLocation(20, 20);
		setSize(new Dimension(600, 500));

		mainPanel = new JPanel(null);
		mainPanel.setBounds(0, 0, 600, 500);
		
		int mPanelX = 0;
		int mPanelY = 0;
		
		//panel informacji podstawowych
		JPanel infoPanel = new JPanel(null);
		infoPanel.setBounds(mPanelX, mPanelY, 580, 130);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Transition general information:"));
		
		int infPanelX = 10;
		int infPanelY = 20;
		
		//************************* NEWLINE *************************
		
		JLabel labelID = new JLabel("ID:");
		labelID.setBounds(infPanelX, infPanelY, 20, 20);
		infoPanel.add(labelID);
		
		int id = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().indexOf(transition);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
		idTextBox.setEditable(false);
		infoPanel.add(idTextBox);
		
		JLabel portalLabel = new JLabel("Portal:");
		portalLabel.setBounds(infPanelX+60, infPanelY, 40, 20);
		infoPanel.add(portalLabel);
		
		String port = "no";
		if(transition.isPortal() == true) 
			port = "yes";
		
		JLabel portalLabel2 = new JLabel(port);
		portalLabel2.setBounds(infPanelX+100, infPanelY, 30, 20);
		infoPanel.add(portalLabel2);
		
		JLabel avgFiredLabel = new JLabel("Avg.f.:", JLabel.LEFT);
		avgFiredLabel.setBounds(infPanelX+130, infPanelY, 50, 20);
        infoPanel.add(avgFiredLabel);
        
        JFormattedTextField avgFiredTextBox = new JFormattedTextField(id);
        if(mainSimulatorActive == false) {
        	//TODO: inne modele sieci
        	StateSimulator ss = new StateSimulator();
    		ss.initiateSim(NetType.BASIC, false);
    		ArrayList<Integer> dataVector = ss.simulateNetSingleTransition(1000, transition);
    		int sum = dataVector.get(dataVector.size()-2);
    		int steps = dataVector.get(dataVector.size()-1);
    		double avgFired = sum;
    		avgFired /= steps;
    		avgFired *= 100; // * 100%
    		avgFiredTextBox.setText(Tools.cutValue(avgFired)+"%");
        } else {
        	avgFiredTextBox.setEnabled(false);
        	avgFiredTextBox.setText("n/a");
        }
        avgFiredTextBox.setBounds(infPanelX+180, infPanelY, 50, 20);
        avgFiredTextBox.setEditable(false);
		infoPanel.add(avgFiredTextBox);
		
		int preP = 0;
		int postP = 0;
		for (ElementLocation el : transition.getElementLocations()) {
			preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
			postP += el.getOutArcs().size();
		}
		
		JLabel prePlaceLabel = new JLabel("PRE-Place:");
		prePlaceLabel.setBounds(infPanelX+235, infPanelY, 65, 20);
		infoPanel.add(prePlaceLabel);
		
		JFormattedTextField prePlaceTextBox = new JFormattedTextField(preP);
		prePlaceTextBox.setBounds(infPanelX+300, infPanelY, 30, 20);
		prePlaceTextBox.setEditable(false);
		infoPanel.add(prePlaceTextBox);
		
		JLabel postPlaceLabel = new JLabel("POST-Place:");
		postPlaceLabel.setBounds(infPanelX+335, infPanelY, 75, 20);
		infoPanel.add(postPlaceLabel);
		
		JFormattedTextField postPlaceTextBox = new JFormattedTextField(postP);
		postPlaceTextBox.setBounds(infPanelX+420, infPanelY, 30, 20);
		postPlaceTextBox.setEditable(false);
		infoPanel.add(postPlaceTextBox);
		
		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel labelName = new JLabel("Name:");
		labelName.setBounds(infPanelX, infPanelY, 40, 20);
		infoPanel.add(labelName);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(infPanelX+60, infPanelY);
		nameField.setSize(460, 20);
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				
				action.changeName(transition, newName);
				action.parentTableUpdate(parentFrame, newName);
			}
		});
		infoPanel.add(nameField);
		
		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel commmentLabel = new JLabel("Comm.:", JLabel.LEFT);
		commmentLabel.setBounds(infPanelX, infPanelY, 50, 20);
		infoPanel.add(commmentLabel);
		
		JTextArea commentField = new JTextArea(transition.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();
            	
				action.changeComment(transition, newComment);
            }
        });
		
        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+60, infPanelY, 460, 60);

        infoPanel.add(creationPanel);
        infPanelY += 60;
        
		mainPanel.add(infoPanel);

		add(mainPanel);
		
	}
}
