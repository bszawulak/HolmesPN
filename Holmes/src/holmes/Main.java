package holmes;

import javax.swing.*;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.comparison.Benchmarker;
import holmes.analyse.comparison.GatherData;
import holmes.analyse.comparison.GraphletComparator;
import holmes.analyse.comparison.experiment.NetGenerator;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.server.BranchesServerCalc;
import holmes.windows.HolmesComparisonModule;
import holmes.windows.HolmesGraphletsPrototype;
import holmes.windows.HolmesReductionPrototype;

import java.io.FileOutputStream;
import java.util.ArrayList;

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


                    //NetGenerator ng = new NetGenerator(new String[0]);

                    //HolmesGraphletsPrototype HGP = new HolmesGraphletsPrototype();
                    //HGP.collectGDDAFromFiles();
                    //HGP.serverSecondComparisonExperiment();
                    //HGP.serverThirdComparisonExperiment();
                    //HGP.calcVectorForDRGF();
                    //HGP.compareGDDAforDistortions();
                    //HGP.collectDRGF();

                    //BranchesServerCalc bsc = new BranchesServerCalc();
                    //bsc.compareAverageBRDF();

                    //NetGenerator ng = //new NetGenerator(20,21,20,21,0,100,149,250);
                    //new NetGenerator(60,61,60,61,0,100,139,300);
                    //new NetGenerator(80,81,80,81,0,100,179,350);



                    //bsc.getData();
                    //bsc.CompareForBranches();

                    //NetGenerator ng = new NetGenerator((float) 0.0);
                    //NetGenerator ng = new NetGenerator(10,41,0,41,0,100,true);
                    //NetGenerator ng = new NetGenerator(" ");

                    //GraphletComparator gcom = new GraphletComparator(600);
                    //normal mode


                    //new NetGenerator(0,61,0,61,0,100,true);


                    //GraphletsCalculator gc = new GraphletsCalculator();
                    //gc.generateGraphlets();


                    Benchmarker br = new Benchmarker();

                    //guiManager = new GUIManager(new JFrame("Holmes 2.0")); //and pray


                    //IOprotocols io = new IOprotocols();

                    //for(int graphletCounter=2 ; graphletCounter <= 9 ; graphletCounter++)
                    //    io.importSubnetFromFile("/home/bszawulak/IdeaProjects/ipne/GRAFLETY/G3/G-"+graphletCounter+".xml", 0, 0);

                    //for(int graphletCounter=10 ; graphletCounter <= 446 ; graphletCounter++)
                    //   io.importSubnetFromFile("/home/bszawulak/IdeaProjects/ipne/GRAFLETY/G4/G-"+graphletCounter+".xml", 0, 0);

                   // for(int graphletCounter=447 ; graphletCounter <= 928 ; graphletCounter++)
                   //     io.importSubnetFromFile("/home/bszawulak/IdeaProjects/ipne/GRAFLETY/G5/G-"+graphletCounter+".xml", 0, 0);
                    //GatherData gd = new GatherData();

                    //HolmesReductionPrototype HRP = new HolmesReductionPrototype();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        SwingUtilities.invokeLater(fiatLux);
    }
}
