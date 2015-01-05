package abyss.math;

import java.awt.Color;

/**
 * Klasa pojemnik u¿ywana do podœwietlania elementów klastra na sieci w programie.
 * @author MR
 *
 */
public class ClusterTransition {
	/**
	 * Kolor wystêpowania tranzycji w klastrze - prosty.
	 */
	public Color colorTransGrade;
	/**
	 * Kolor wystêpowania tranzycji w klastrze - skala.
	 */
	public Color colorTransScale;
	/**
	 * Kolor sumy odpaleñ tranzycji w klastrze - prosty.
	 */
	public Color colorFiredGrade;
	/**
	 * Kolor sumy odpaleñ tranzycji w klastrze - skala.
	 */
	public Color colorFiredScale;
	/**
	 * Liczba wyst¹pieñ tranzycji w klastrze
	 */
	public int transInCluster;
	/**
	 * Liczba odpaleñ tranzycji w klastrze
	 */
	public int firedInCluster;
	
	/**
	 * Konstruktor g³ówny obiektu klasy ClusterTransition.
	 * @param colorTransGrade Color - kolor wystêpowania tranzycji w klastrze - prosty
	 * @param colorTransScale Color - kolor wystêpowania tranzycji w klastrze - skala
	 * @param colorFiredGrade Color - kolor sumy odpaleñ tranzycji w klastrze - prosty
	 * @param colorFiredScale Color - kolor sumy odpaleñ tranzycji w klastrze - skala
	 * @param transInCluster int - liczba wyst¹pieñ tranzycji w klastrze
	 * @param firedInCluster int - liczba odpaleñ tranzycji w klastrze
	 */
	public ClusterTransition(Color colorTransGrade, Color colorTransScale, Color colorFiredGrade,
			Color colorFiredScale, int transInCluster, int firedInCluster) {
		this.colorTransGrade = colorTransGrade;
		this.colorTransScale = colorTransScale;
		this.colorFiredGrade = colorFiredGrade;
		this.colorFiredScale = colorFiredScale;
		this.transInCluster = transInCluster;
		this.firedInCluster = firedInCluster;
	}
	
	
}
