package abyss;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import abyss.darkgui.GUIManager;

/**
 * Główna klasa programu. Jedna metoda, odpowiedzialna za tworzenie środowiska graficznego Abyss. I całej reszty.
 * @author students
 *
 * "Czy położyłby się Pan pod kroplówką obsługiwaną przez ten algorytm? -A co by w niej było? -Denaturat." A.D. circa 2001
 * 
 */
public class Main {

	public static GUIManager guiManager;

	/**
	 * Tej metody chyba nie trzeba przedstawiać.
	 * @param args String[] - argumenty. Dla zasady, bo i tak nie będzie żadnych
	 */
	public static void main(String[] args) {
		Runnable fiatLux = new Runnable() {
			public void run() {
				try {
					guiManager = new GUIManager(new JFrame("Abyss 1.25")); //and pray
					guiManager.getSimulatorBox().createSimulatorProperties();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		};
		SwingUtilities.invokeLater(fiatLux);
	}
}
