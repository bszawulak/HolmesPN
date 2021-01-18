package holmes.analyse.comparison;

import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class KnockoutInvariantComparison {
    private GUIManager overlord;
    private IOprotocols communicationProtocol;

    private ArrayList<Integer> nodesList1;
    private ArrayList<ArrayList<Integer>> invariantsList1 = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Integer> nodesList2;
    private ArrayList<ArrayList<Integer>> invariantsList2 = new ArrayList<ArrayList<Integer>>();

    private ArrayList<ArrayList<Integer>> commonInvariantsList = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> onlyFirstInvariantsList = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> onlySecondInvariantsList = new ArrayList<ArrayList<Integer>>();

    public KnockoutInvariantComparison() {
        overlord = GUIManager.getDefaultGUIManager();
        resetComponents();

        communicationProtocol = new IOprotocols();
    }

    private void resetComponents() {
        nodesList1 = new ArrayList<Integer>();
        nodesList2 = new ArrayList<Integer>();
        commonInvariantsList = new ArrayList<>();
        onlyFirstInvariantsList = new ArrayList<>();
        onlySecondInvariantsList = new ArrayList<>();
    }

    public void compare() {

        readT_invariants1("/home/Szavislav/Covid/inv/exportowane.inv");
        ArrayList<ArrayList<Integer>> tmp1 = new ArrayList<>(invariantsList1);
        //readT_invariants1("/home/Szavislav/Covid/inv/49004_tinv.inv");
        //invariantsList1.removeAll(tmp1);
        System.out.println("Wczytywanie I sieci zakońćzone");
        readT_invariants2("/home/Szavislav/Covid/inv/49004_tinv.inv");
        ArrayList<ArrayList<Integer>> tmp2 = new ArrayList<>(invariantsList2);
        //readT_invariants2("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/InvOnly-B.inv");
        //ArrayList<ArrayList<Integer>> tmp22 = new ArrayList<>(invariantsList2);
        //readT_invariants2("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/tinv-knocnet.inv");
        //invariantsList2.removeAll(tmp2);
        //invariantsList2.removeAll(tmp22);


/*
        readT_invariants1("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/InvCommon.inv");
        ArrayList<ArrayList<Integer>> tmp1 = new ArrayList<>(invariantsList1);
        readT_invariants1("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/tinv-orignet.inv");
        invariantsList1.removeAll(tmp1);
        System.out.println("Wczytywanie I sieci zakońćzone");
        readT_invariants1("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/InvCommon.inv");
        ArrayList<ArrayList<Integer>> tmp2 = new ArrayList<>(invariantsList2);
        readT_invariants2("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/InvOnly-B.inv");
        ArrayList<ArrayList<Integer>> tmp22 = new ArrayList<>(invariantsList2);
        readT_invariants2("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/tinv-knocnet.inv");
        invariantsList2.removeAll(tmp2);
        invariantsList2.removeAll(tmp22);
*/
        ArrayList<SimilarityInvarianStructure> ikl = new ArrayList<>();

        System.out.println("Wczytywanie II sieci zakońćzone");
        int count = 0;
        for (ArrayList<Integer> inv_net_1 : invariantsList1) {
            System.out.println(count + " \\ " + invariantsList1.size());
            count++;
            boolean found = false;
            int maxDegreeSim = Integer.MAX_VALUE;
            ArrayList<ArrayList<Integer>> listOfDisSimilar = new ArrayList<>();

            for (ArrayList<Integer> inv_net_2 : invariantsList2) {
                int simVal = 0;
                int simDegree = 0;
                for (int i = 0; i < inv_net_1.size(); i++) {
                    if (inv_net_1.get(i) == inv_net_2.get(i)) {
                        simVal++;
                    }
                    simDegree += Math.abs(inv_net_1.get(i) - inv_net_2.get(i));
                }

                if (simVal == inv_net_1.size()) {
                    //commonInvariantsList.add(inv_net_1);
                    found = true;
                }

                if (simDegree < maxDegreeSim) {
                    maxDegreeSim = simDegree;
                    listOfDisSimilar.clear();
                    listOfDisSimilar.add(inv_net_2);
                } else if (simDegree == maxDegreeSim) {
                    listOfDisSimilar.add(inv_net_2);
                }
            }

            if (found)
                commonInvariantsList.add(inv_net_1);
            else {
                onlyFirstInvariantsList.add(inv_net_1);
                ArrayList<Integer> tmp = new ArrayList<>();
                for (int j = 0; j < listOfDisSimilar.size(); j++) {
                    tmp.add(invariantsList2.indexOf(listOfDisSimilar.get(j)));
                }
                ikl.add(new SimilarityInvarianStructure(invariantsList1.indexOf(inv_net_1), tmp));
            }
        }

        count = 0;
        for (ArrayList<Integer> inv_net_2 : invariantsList2) {
            System.out.println(count + " \\ " + invariantsList1.size());
            count++;
            boolean found = false;

            for (ArrayList<Integer> inv_net_1 : invariantsList1) {

                int simVal = 0;
                for (int i = 0; i < inv_net_1.size(); i++) {
                    if (inv_net_1.get(i) == inv_net_2.get(i))
                        simVal++;
                }

                if (simVal == inv_net_1.size()) {
                    //commonInvariantsList.add(inv_net_1);
                    found = true;
                }
            }

            if (!found)
                onlySecondInvariantsList.add(inv_net_2);
        }
        if (commonInvariantsList.size() > 0) {
            communicationProtocol.writeT_invINA("/home/Szavislav/Covid/inv/comp1", commonInvariantsList, overlord.getWorkspace().getProject().getTransitions());
            System.out.println("Zapisywanie wspólnych invariantów zakońćzone");
        }
        if (commonInvariantsList.size() > 0) {
            communicationProtocol.writeT_invINA("/home/Szavislav/Covid/inv/comp2", onlyFirstInvariantsList, overlord.getWorkspace().getProject().getTransitions());
            System.out.println("Zapisywanie invariantów występujących jedynie w orginalnej sieci zakońćzone");
        }
        if (commonInvariantsList.size() > 0) {
            communicationProtocol.writeT_invINA("/home/Szavislav/Covid/inv/comp3", onlySecondInvariantsList, overlord.getWorkspace().getProject().getTransitions());
            System.out.println("Zapisywanie invariantów występujących jedynie w knockoutowej sieci zakońćzone");
        }
        if (ikl.size() > 0) {
            writeSimTinv("/home/Szavislav/Covid/inv/comp4", ikl);
            System.out.println("Zapisywanie matchowania 1 to 2 invariantów zakońćzone");
        }


    }

    public void compare_baldan_cocco() {
        //readT_invariants1("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/InvCommon.inv");
        readT_invariants1("/home/Szavislav/Covid/inv/exportowane.inv");
        //ArrayList<ArrayList<Integer>> tmp1 = new ArrayList<>(invariantsList1);
        //readT_invariants1("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/tinv-orignet.inv");
        //invariantsList1.removeAll(tmp1);

        System.out.println("Wczytywanie I sieci zakońćzone - liczba inwariantów = " + invariantsList1.size());


        readT_invariants2("/home/Szavislav/Covid/inv/49004_tinv.inv");
        //readT_invariants2("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/sur.inv");
        //ArrayList<ArrayList<Integer>> tmp7 = new ArrayList<>(invariantsList2);
        //readT_invariants2("/home/Szavislav/Dropbox/Doktorat/Moje Notatki/Covid19 net analysis/tinv-knocnet-only.inv");
        //invariantsList2.addAll(tmp7);
        ArrayList<SimilarityInvarianStructure> ikl = new ArrayList<>();

        System.out.println("Wczytywanie II sieci zakońćzone");
        ArrayList<ArrayList<Integer>> TMPinvariantsList2 = new ArrayList<>(invariantsList2);
        ArrayList<MatchedInvariants> invariantsMatchings = new ArrayList<>();

        for (int k = 0; k < invariantsList1.size(); k++) {
            ArrayList<Integer> inv_net_1 = invariantsList1.get(k);
            if (TMPinvariantsList2.size() <= 0) {
                break;
            }

            double index_Sorensen = -1;
            double index_Tanimoto = -1;
            double index_Jacard = -1;

            ArrayList<Integer> commonPartSum = new ArrayList<>();
            ArrayList<ArrayList<Integer>> commonPart = new ArrayList<>();
            for (int j = 0; j < TMPinvariantsList2.size(); j++) {
                ArrayList<Integer> part = new ArrayList<Integer>();
                int value = 0;
                for (int i = 0; i < inv_net_1.size(); i++) {
                    if (inv_net_1.get(i) == TMPinvariantsList2.get(j).get(i)) {
                        value += inv_net_1.get(i);
                        part.add(inv_net_1.get(i));
                    } else {
                        value += Math.min(inv_net_1.get(i), TMPinvariantsList2.get(j).get(i));
                        part.add(Math.min(inv_net_1.get(i), TMPinvariantsList2.get(j).get(i)));
                    }
                }
                commonPartSum.add(value);
                commonPart.add(part);
            }

            int maxCommon = commonPartSum.stream().mapToInt(x -> x).max().orElse(0);
            int indexOfBeastMatch = commonPartSum.indexOf(maxCommon);
            int index = invariantsList2.indexOf(TMPinvariantsList2.get(indexOfBeastMatch));

            MatchedInvariants mi = new MatchedInvariants(inv_net_1, invariantsList2.get(index), commonPart.get(indexOfBeastMatch));

            System.out.println("Dopasowanie: oryginalny inwariant : " + k + " ; knockoutowany inwariant : " + index + " ; sorensen index : " + mi.index_Sorensen);
            invariantsMatchings.add(mi);
            TMPinvariantsList2.remove(indexOfBeastMatch);
        }

        double index_I = 0;
        double index_II = 0;
        double index_III = 0;

        double index_Sorensen = 0;
        double index_Tanimoto = 0;
        double index_Jacard = 0;

        for (int i = 0; i < invariantsMatchings.size(); i++) {
            index_I += invariantsMatchings.get(i).index_I;
            index_II += invariantsMatchings.get(i).index_II;
            index_III += invariantsMatchings.get(i).index_III;
            index_Jacard += invariantsMatchings.get(i).index_Jacard;
            index_Tanimoto += invariantsMatchings.get(i).index_Tanimoto;
            index_Sorensen += invariantsMatchings.get(i).index_Sorensen;
        }

        System.out.println("Sumy");
        System.out.println("Index I " + index_I);
        System.out.println("Index II " + index_II);
        System.out.println("Index III " + index_III);
        System.out.println("Jackard Index " + index_Jacard);
        System.out.println("Tanimoto Index " + index_Tanimoto);
        System.out.println("Sorensen Index " + index_Sorensen);

        System.out.println("Suma / proper invariant count");
        System.out.println("Index I " + index_I / invariantsList1.size());
        System.out.println("Index II " + index_II / invariantsList1.size());
        System.out.println("Index III " + index_III / invariantsList1.size());
        System.out.println("Jackard Index " + index_Jacard / invariantsList1.size());
        System.out.println("Tanimoto Index " + index_Tanimoto / invariantsList1.size());
        System.out.println("Sorensen Index " + index_Sorensen / invariantsList1.size());

        System.out.println("Suma / matched invariant count");
        System.out.println("Index I " + index_I / invariantsMatchings.size());
        System.out.println("Index II " + index_II / invariantsMatchings.size());
        System.out.println("Index III " + index_III / invariantsMatchings.size());
        System.out.println("Jackard Index " + index_Jacard / invariantsMatchings.size());
        System.out.println("Tanimoto Index " + index_Tanimoto / invariantsMatchings.size());
        System.out.println("Sorensen Index " + index_Sorensen / invariantsMatchings.size());
    }

    public void writeSimTinv(String path, ArrayList<SimilarityInvarianStructure> invariants) {
        try {
            String extension = "";
            if (!path.contains(".comp"))
                extension = ".comp";
            PrintWriter pw = new PrintWriter(path + extension);
            pw.print("\r\n");
            pw.print("\r\n");
            pw.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            pw.print("\r\n");

            pw.print("Original invariant: Konckout invariants");

            for (int i = 0; i < invariants.size(); i++) {
                String knocked = "";
                for (Integer in : invariants.get(i).knockedInvariantIndexes
                ) {
                    knocked += in + ",";
                }
                pw.print(invariants.get(i).originalInvariantindex + " :   " + knocked);
                pw.print("\r\n");
            }
            pw.print("\r\n");
            pw.print("@");
            pw.close();
            overlord.log("T-invariants in INA file format saved to " + path, "text", true);
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writeT_invINA", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }
    }

    private String convertIntToStr(boolean large, int tr) {
        if (large) {
            if (tr < 10)
                return "    " + tr;
            if (tr < 100)
                return "   " + tr;
            if (tr < 1000)
                return "  " + tr;
            else
                return " " + tr;
        } else { //smaller
            if (tr < 10)
                return "   " + tr;
            if (tr < 100)
                return "  " + tr;
            else
                return " " + tr;
        }
    }

    public boolean readT_invariants1(String path) {
        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            String backup = readLine;

            if (readLine.contains("transition sub/sur/invariants for net")) {
                //to znaczy, że wczytujemy plik INA, po prostu
            }

            if (backup.contains("transition invariants basis")) {
                JOptionPane.showMessageDialog(null, "Wrong invariants. Only semipositives are acceptable.",
                        "ERROR:readINV", JOptionPane.ERROR_MESSAGE);
                buffer.close();
                return false;
            }

            buffer.readLine();
            while (!readLine.contains("semipositive transition invariants =")) {
                readLine = buffer.readLine();
            }
            buffer.readLine();
            nodesList1.clear();
            // Etap I - Liczba tranzycji/miejsc
            while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
                if (readLine.endsWith("~~~~~~~~~~~")) {
                    break;
                }
                String[] formattedLine = readLine.split(" ");
                for (int j = 0; j < formattedLine.length; j++) {
                    if (!(formattedLine[j].isEmpty() || formattedLine[j].contains("Nr."))) {
                        try {
                            nodesList1.add(Integer.parseInt(formattedLine[j]));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista T-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            invariantsList1.clear();
            while ((readLine = buffer.readLine()) != null) {
                if (readLine.contains("@") || readLine.isEmpty()) {
                    break;
                }
                String[] formattedLine = readLine.split("\\|");
                formattedLine = formattedLine[1].split(" ");
                for (int i = 0; i < formattedLine.length; i++) {
                    if (!formattedLine[i].isEmpty()) {
                        tmpInvariant.add(Integer.parseInt(formattedLine[i]));
                    }
                }
                if (tmpInvariant.size() == nodesList1.size()) {
                    invariantsList1.add(tmpInvariant);
                    tmpInvariant = new ArrayList<Integer>();
                }
            }
            buffer.close();
            overlord.log("T-invariants from INA file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("T-invariants reading operation failed.", "error", true);
            return false;
        }
    }

    public boolean readT_invariants2(String path) {
        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            String backup = readLine;

            if (readLine.contains("transition sub/sur/invariants for net")) {
                //to znaczy, że wczytujemy plik INA, po prostu
            }
            if (backup.contains("transition invariants basis")) {
                JOptionPane.showMessageDialog(null, "Wrong invariants. Only semipositives are acceptable.",
                        "ERROR:readINV", JOptionPane.ERROR_MESSAGE);
                buffer.close();
                return false;
            }

            buffer.readLine();
            while (!readLine.contains("semipositive transition invariants =")) {
                readLine = buffer.readLine();
            }
            buffer.readLine();
            nodesList2.clear();
            // Etap I - Liczba tranzycji/miejsc
            while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
                if (readLine.endsWith("~~~~~~~~~~~")) {
                    break;
                }
                String[] formattedLine = readLine.split(" ");
                for (int j = 0; j < formattedLine.length; j++) {
                    if (!(formattedLine[j].isEmpty() || formattedLine[j].contains("Nr."))) {
                        try {
                            nodesList2.add(Integer.parseInt(formattedLine[j]));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista T-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            invariantsList2.clear();
            while ((readLine = buffer.readLine()) != null) {
                if (readLine.contains("@") || readLine.isEmpty()) {
                    break;
                }
                String[] formattedLine = readLine.split("\\|");
                formattedLine = formattedLine[1].split(" ");
                for (int i = 0; i < formattedLine.length; i++) {
                    if (!formattedLine[i].isEmpty()) {
                        tmpInvariant.add(Integer.parseInt(formattedLine[i]));
                    }
                }
                if (tmpInvariant.size() == nodesList2.size()) {
                    invariantsList2.add(tmpInvariant);
                    tmpInvariant = new ArrayList<Integer>();
                }
            }
            buffer.close();
            overlord.log("T-invariants from INA file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("T-invariants reading operation failed.", "error", true);
            return false;
        }
    }

    public class MatchedInvariants {
        public ArrayList<Integer> firstNetInvariant;
        public ArrayList<Integer> secondNetInvariant;
        public ArrayList<Integer> commonPart;
        public ArrayList<Integer> maxPart;

        public double index_I;
        public double index_II;
        public double index_III;

        public double index_Sorensen;
        public double index_Tanimoto;
        public double index_Jacard;

        public MatchedInvariants(ArrayList<Integer> fni, ArrayList<Integer> sni, ArrayList<Integer> cp) {
            firstNetInvariant = fni;
            secondNetInvariant = sni;
            commonPart = cp;
            maxPart = new ArrayList<>();

            double AB = 0;
            double A2 = 0;
            double B2 = 0;
            for (int i = 0; i < firstNetInvariant.size() || i < secondNetInvariant.size(); i++) {
                maxPart.add(Math.max(firstNetInvariant.get(i), secondNetInvariant.get(i)));
                AB += firstNetInvariant.get(i) * secondNetInvariant.get(i);
                A2 += firstNetInvariant.get(i) * firstNetInvariant.get(i);
                B2 += secondNetInvariant.get(i) * secondNetInvariant.get(i);
            }

            double fniSUM = (double) firstNetInvariant.stream().collect(Collectors.summingInt(Integer::intValue));
            double sniSUM = (double) secondNetInvariant.stream().collect(Collectors.summingInt(Integer::intValue));
            double cpSUM = (double) commonPart.stream().collect(Collectors.summingInt(Integer::intValue));
            double mpSUM = (double) maxPart.stream().collect(Collectors.summingInt(Integer::intValue));

            index_I = fniSUM + sniSUM - 2 * cpSUM;
            index_II = (cpSUM * cpSUM) / (2 * AB);
            index_III = (1 - index_II) / (fniSUM + sniSUM);
            index_Jacard = cpSUM / mpSUM;
            index_Tanimoto = AB / (A2 + B2 - AB);
            index_Sorensen = (2 * cpSUM) / (sniSUM + fniSUM);
        }
    }

    public class SimilarityInvarianStructure {
        public ArrayList<Integer> originalInvariant = new ArrayList<>();
        public ArrayList<ArrayList<Integer>> knockedInvariants = new ArrayList<>();
        public int originalInvariantindex = -1;
        public ArrayList<Integer> knockedInvariantIndexes = new ArrayList<>();

        public SimilarityInvarianStructure(ArrayList<Integer> oi, ArrayList<ArrayList<Integer>> ki) {
            originalInvariant = oi;
            knockedInvariants = ki;
        }

        public SimilarityInvarianStructure(int oi, ArrayList<Integer> ki) {
            originalInvariantindex = oi;
            knockedInvariantIndexes = ki;
        }
    }

}
