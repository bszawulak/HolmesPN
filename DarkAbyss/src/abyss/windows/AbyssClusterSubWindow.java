package abyss.windows;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import abyss.clusters.Clustering;

/**
 * Klasa tworz¹ca okno informacyjne wzglêdem tabeli klastrów. Zawiera ono informacje o
 * konkretnych wybranym klastrowaniu.
 * @author MR
 *
 */
public class AbyssClusterSubWindow extends JFrame {
	private static final long serialVersionUID = -5663572683374020754L;
	private JFrame parentFrame;
	private Clustering data;
	private String nL = "\n";
	
	/**
	 * Konstruktor domyœlny obiektu klasy AbyssClusterSubWindow.
	 */
	public AbyssClusterSubWindow() {
		
	}
	
	/**
	 * G³ówny konstruktor parametrowy okna klasy AbyssClusterSubWindow.
	 * @param parent AbyssClusters - obiekt okna wywo³uj¹cego
	 * @param data Clustering - dane do wyœwietlenia
	 */
	public AbyssClusterSubWindow(AbyssClusters parent, Clustering dataPackage) {
		this();
		this.data = dataPackage;
		this.parentFrame = parent;
		parentFrame.setEnabled(false);
		
	    JTextArea area = new JTextArea();
	    area.setLineWrap(true);
	    area.setWrapStyleWord(true);
	    area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

	    add(new JScrollPane(area));
	    setSize(new Dimension(450, 300));

	    addWindowListener(new java.awt.event.WindowAdapter() {
	        @Override
	        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        	parentFrame.setEnabled(true);
	        }
	    });
	    
	    setLocationRelativeTo(null);
	    setVisible(true);
	    
	    if(data != null) {
	    	area.append("Algorithm name: "+data.algorithmName+nL);
	    	area.append("Metric name: "+data.metricName+nL);
	    	area.append(nL);
	    	area.append("Invariants number: "+data.invNumber+nL);
	    	area.append("Clusters number: "+data.clusterNumber+nL);
	    	area.append("Zero-clusters: "+data.zeroClusters+nL);
	    	area.append("MSS evaluation: "+data.evalMSS+nL);
	    	area.append("C-H evaluation: "+data.evalCH+nL);
	    	area.append(nL);
	    	for(int i=0; i<data.clusterNumber; i++) {
	    		area.append("Cluster "+i+" size: "+data.clusterSize[i]+"  MSS: "+data.clusterMSS[i]+nL);
	    	}
	    	area.append(nL);
	    	for(int i=0; i<6; i++) {
	    		area.append(data.vectorMSS[i]+ " | ");
	    	}
	    }
	}
}
