package holmes;

import javax.swing.*;

import holmes.darkgui.GUIManager;

/**
 * Główna klasa programu. Jedna metoda, odpowiedzialna za tworzenie środowiska graficznego Holmes. I całej reszty.
 * Przy okazji jedyna zrozumiała. Garść sucharów poniżej, wszędzie dalej w kodzie jest już tylko gorzej.
 *
 * [2022-06-21] Wrócilem. MR.
 * [2022-07-01] Jak sie nazywa szersza langusta? wangusta!
 * "Czy położyłby się Pan pod kroplówką obsługiwaną przez ten algorytm? -A co by w niej było? -Denaturat." A.D. circa 2001
 * [2024-02-06] rozwój trwa, tym razem z copilotem
 * [2026-02-25] do zapamiętania na przyszłość: Maven jest nam potrzebny do tego, aby dynamicznie móc dodawać 
 * nowe biblioteki których zupełnie nie potrzebujemy ale rzucamy się na nie bo są nowe. Nie wiemy jak działają
 * ale będą wymagać zmiany starego kodu. Ale przynajmniej dzięki temu będziemy mogli zmarnować kolejne miesiące
 * pisząc zupełnie niepotrzebne w tej chwili testy. Które teraz są nam chyba tylko po to potrzebne aby potwierdzić,
 * że jeden token plus drugi token to razem dwa tokeny. Dziękuję, postoję. Moje IQ, rozum i godność człowieka
 * i zasady religijne nie pozwalają brać udziału w tej farsie. [MR]
 */
public class Main {
    public static GUIManager guiManager;

    /**
     * Tej metody chyba nie trzeba przedstawiać.
     * @param args (<b>String[]</b>) argumenty. Dla zasady, bo i tak nie będzie żadnych.
     */
    public static void main(String[] args) {
        Runnable fiatLux = () -> { //tylko po to żeby się NIE skompilowało jeżeli ktoś obniży wersję Javy poniżej sensownego poziomu.
            try {
                guiManager = new GUIManager(new JFrame("Holmes 2.0")); //and pray
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
        SwingUtilities.invokeLater(fiatLux);
    }
}
