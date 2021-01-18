package holmes;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import holmes.analyse.comparison.GraphletComparator;
import holmes.analyse.comparison.experiment.NetGenerator;
import holmes.darkgui.GUIManager;

/**
 * Główna klasa programu. Jedna metoda, odpowiedzialna za tworzenie środowiska graficznego Holmes. I całej reszty.
 * Przy okazji jedyna zrozumiała.
 * 
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
					guiManager = new GUIManager(new JFrame("Holmes 2.0")); //and pray
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		};
		SwingUtilities.invokeLater(fiatLux);
	}
}
