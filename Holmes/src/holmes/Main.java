package holmes;

import javax.swing.*;

import holmes.darkgui.GUIManager;

/**
 * Główna klasa programu. Jedna metoda, odpowiedzialna za tworzenie środowiska graficznego Holmes. I całej reszty.
 * Przy okazji jedyna zrozumiała.
 *
 * [2022-06-21] Wrócilem. MR.
 * [2022-07-01] Jak sie nazywa szersza langusta? wangusta!
 * <p>
 * "Czy położyłby się Pan pod kroplówką obsługiwaną przez ten algorytm? -A co by w niej było? -Denaturat." A.D. circa 2001
 *
 */
public class Main {
    public static GUIManager guiManager;

    /**
     * Tej metody chyba nie trzeba przedstawiać.
     * @param args (<b>String[]</b>) argumenty. Dla zasady, bo i tak nie będzie żadnych.
     */
    public static void main(String[] args) {
        Runnable fiatLux = () -> {
            try {
                guiManager = new GUIManager(new JFrame("Holmes 2.0")); //and pray
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
        SwingUtilities.invokeLater(fiatLux);
    }
}
