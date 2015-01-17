package abyss.clusters;

import java.awt.Color;

/**
 * Klasa pojemnik używana do podświetlania elementów klastra na sieci w programie.
 * @author MR
 *
 */
public class ClusterTransition {
	/**
	 * Kolor występowania tranzycji w klastrze - prosty.
	 */
	public Color colorTransGrade;
	/**
	 * Kolor występowania tranzycji w klastrze - skala.
	 */
	public Color colorTransScale;
	/**
	 * Kolor sumy odpaleń tranzycji w klastrze - prosty.
	 */
	public Color colorFiredGrade;
	/**
	 * Kolor sumy odpaleń tranzycji w klastrze - skala.
	 */
	public Color colorFiredScale;
	/**
	 * Liczba wystąpień tranzycji w klastrze
	 */
	public int transInCluster;
	/**
	 * Liczba odpaleń tranzycji w klastrze
	 */
	public double firedInCluster;
	
	/**
	 * Konstruktor główny obiektu klasy ClusterTransition.
	 * @param colorTransGrade Color - kolor występowania tranzycji w klastrze - prosty
	 * @param colorTransScale Color - kolor występowania tranzycji w klastrze - skala
	 * @param colorFiredGrade Color - kolor sumy odpaleń tranzycji w klastrze - prosty
	 * @param colorFiredScale Color - kolor sumy odpaleń tranzycji w klastrze - skala
	 * @param transInCluster int - liczba wystąpień tranzycji w klastrze
	 * @param firedInCluster int - liczba odpaleń tranzycji w klastrze
	 */
	public ClusterTransition(Color colorTransGrade, Color colorTransScale, Color colorFiredGrade,
			Color colorFiredScale, int transInCluster, double firedInCluster) {
		this.colorTransGrade = colorTransGrade;
		this.colorTransScale = colorTransScale;
		this.colorFiredGrade = colorFiredGrade;
		this.colorFiredScale = colorFiredScale;
		this.transInCluster = transInCluster;
		this.firedInCluster = firedInCluster;
	}
	
	
}
