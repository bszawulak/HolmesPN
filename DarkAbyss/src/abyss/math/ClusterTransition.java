package abyss.math;

import java.awt.Color;

/**
 * Klasa pojemnik u�ywana do pod�wietlania element�w klastra na sieci w programie.
 * @author MR
 *
 */
public class ClusterTransition {
	/**
	 * Kolor wyst�powania tranzycji w klastrze - prosty.
	 */
	public Color colorTransGrade;
	/**
	 * Kolor wyst�powania tranzycji w klastrze - skala.
	 */
	public Color colorTransScale;
	/**
	 * Kolor sumy odpale� tranzycji w klastrze - prosty.
	 */
	public Color colorFiredGrade;
	/**
	 * Kolor sumy odpale� tranzycji w klastrze - skala.
	 */
	public Color colorFiredScale;
	/**
	 * Liczba wyst�pie� tranzycji w klastrze
	 */
	public int transInCluster;
	/**
	 * Liczba odpale� tranzycji w klastrze
	 */
	public int firedInCluster;
	
	/**
	 * Konstruktor g��wny obiektu klasy ClusterTransition.
	 * @param colorTransGrade Color - kolor wyst�powania tranzycji w klastrze - prosty
	 * @param colorTransScale Color - kolor wyst�powania tranzycji w klastrze - skala
	 * @param colorFiredGrade Color - kolor sumy odpale� tranzycji w klastrze - prosty
	 * @param colorFiredScale Color - kolor sumy odpale� tranzycji w klastrze - skala
	 * @param transInCluster int - liczba wyst�pie� tranzycji w klastrze
	 * @param firedInCluster int - liczba odpale� tranzycji w klastrze
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
