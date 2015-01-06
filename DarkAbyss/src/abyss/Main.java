package abyss;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import abyss.darkgui.GUIManager;

/**
 * Główna klasa programu. Jedna metoda, odpowiedzialna za tworzenie środowiska graficznego Abyss
 * @author students
 *
 */
public class Main {

	public static GUIManager guiManager;

	/**
	 * Tej motdy chyba nie trzeba przedstawiać
	 * @param args String[] - argumenty.
	 */
	public static void main(String[] args) {
		Runnable doCreateAndShowGUI = new Runnable() {
			public void run() {
				try {
					guiManager = new GUIManager(new JFrame("Abyss 1.22"));
					guiManager.getSimulatorBox().createSimulatorProperties();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		};
		SwingUtilities.invokeLater(doCreateAndShowGUI);
	}
}
