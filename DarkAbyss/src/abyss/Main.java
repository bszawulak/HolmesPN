package abyss;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import abyss.darkgui.GUIManager;

/**
 * G³ówna klasa programu. Jedna metoda, odpowiedzialna za tworzenie œrodowiska graficznego Abyss
 * @author students
 *
 */
public class Main {

	public static GUIManager guiManager;

	/**
	 * Metoda main(). I wszystko jasne?
	 * @param args String[] - argumenty.
	 */
	public static void main(String[] args) {
		Runnable doCreateAndShowGUI = new Runnable() {
			public void run() {
				try {
					guiManager = new GUIManager(new JFrame("Abyss - Zintegrowane œrodowisko do edycji, symulacji i analizy Sieci Petriego"));
					guiManager.getSimulatorBox().createSimulatorProperties();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		};
		SwingUtilities.invokeLater(doCreateAndShowGUI);
	}
}
