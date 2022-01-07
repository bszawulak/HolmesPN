package holmes;

import javax.swing.*;

import holmes.analyse.comparison.GraphletComparator;
import holmes.analyse.comparison.experiment.NetGenerator;
import holmes.darkgui.GUIManager;
import holmes.server.BranchesServerCalc;
import holmes.windows.HolmesGraphletsPrototype;
import holmes.windows.HolmesReductionPrototype;

/**
 * Główna klasa programu. Jedna metoda, odpowiedzialna za tworzenie środowiska graficznego Holmes. I całej reszty.
 * Przy okazji jedyna zrozumiała.
 *
 * @author students
 * <p>
 * "Czy położyłby się Pan pod kroplówką obsługiwaną przez ten algorytm? -A co by w niej było? -Denaturat." A.D. circa 2001
 */
public class Main {
    public static GUIManager guiManager;

    /**
     * Tej metody chyba nie trzeba przedstawiać.
     *
     * @param args String[] - argumenty. Dla zasady, bo i tak nie będzie żadnych
     */
    public static void main(String[] args) {
        Runnable fiatLux = new Runnable() {
            public void run() {
                try {
                    //server module

                    //new NetGenerator(40,41,40,41,0,100,600);
                    //Integer.parseInt(args[0])
                    //new NetGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]));

                    //HolmesGraphletsPrototype HGP = new HolmesGraphletsPrototype();
                    //HGP.collectGDDAFromFiles();
                    //HGP.serverSecondComparisonExperiment();
                    //HGP.serverThirdComparisonExperiment();
                    //HGP.calcVectorForDRGF();
                    //HGP.compareGDDAforDistortions();
                    //HGP.collectDRGF();

                    //BranchesServerCalc bsc = new BranchesServerCalc();
                    //bsc.getData();

                    //bsc.Compare();

                    //NetGenerator ng = new NetGenerator((float) 0.0);
                    //NetGenerator ng = new NetGenerator(10,41,0,41,0,100,true);
                    //NetGenerator ng = new NetGenerator(" ");

                    //GraphletComparator gcom = new GraphletComparator(600);
                    //normal mode


                    //new NetGenerator(0,61,0,61,0,100,true);

                    guiManager = new GUIManager(new JFrame("Holmes 2.0")); //and pray

                    //HolmesReductionPrototype HRP = new HolmesReductionPrototype();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        SwingUtilities.invokeLater(fiatLux);
    }
}
