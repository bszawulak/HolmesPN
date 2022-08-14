package holmes.analyse.comparison;

import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KnockoutInvariantComparison {
    private GUIManager overlord;
    private IOprotocols communicationProtocol;

    private ArrayList<Integer> nodesList1;
    private ArrayList<ArrayList<Integer>> invariantsList1 = new ArrayList<>();
    private ArrayList<Integer> nodesList2;
    private ArrayList<ArrayList<Integer>> invariantsList2 = new ArrayList<>();

    private ArrayList<ArrayList<Integer>> commonInvariantsList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> onlyFirstInvariantsList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> onlySecondInvariantsList = new ArrayList<>();

    public KnockoutInvariantComparison() {
        overlord = GUIManager.getDefaultGUIManager();
        resetComponents();

        communicationProtocol = new IOprotocols();
    }

    private void resetComponents() {
        nodesList1 = new ArrayList<>();
        nodesList2 = new ArrayList<>();
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
                    if (inv_net_1.get(i).equals(inv_net_2.get(i))) {
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
                for (ArrayList<Integer> integers : listOfDisSimilar) {
                    tmp.add(invariantsList2.indexOf(integers));
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
                    if (inv_net_1.get(i).equals(inv_net_2.get(i)))
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

    public void checkInvariants(){
        String pathToResult = "/home/Szavislav/SARS-Cov-2/wynik.txt";
        String pathToNet_I = "/home/Szavislav/SARS-Cov-2/0-6-8-bez-sarsa.pnt";
        String pathToNet_II = "/home/Szavislav/SARS-Cov-2/sars-cov-2_v-0-6-8_2021-01-06_DF_PF_KG.pnt";
        String pathToInv_I = "/home/Szavislav/SARS-Cov-2/bez.inv";
        String pathToInv_II = "/home/Szavislav/SARS-Cov-2/z.inv";

        try {
            DataInputStream in = new DataInputStream(new FileInputStream(pathToResult));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine;

            Boolean readTransitions = false;

            HashMap<Integer,Integer> supportMap = new HashMap<>();
            int count = 0;
            while ((readLine = buffer.readLine()) != null) {
                String[] line = readLine.split("inv_2:");


                String line_3 = line[0].replaceAll("inv_1:","");
                String val1 = line_3.replaceAll("\\s+", "");
                int lnv_1 = Integer.parseInt(val1);

                String[] line_2 = line[1].split("value:");
                String val = line_2[0].replaceAll("\\s+", "");
                int lnv_2 = Integer.parseInt(val);
                supportMap.put(lnv_1,lnv_2);
            }

            supportMap.size();



            //read I net support
            //transition ID transition Name
            HashMap<Integer, String> mapIdNameI = readNetSupport(pathToNet_I);

            //read II net support
            //transition ID transition Name
            HashMap<Integer, String> mapIdNameII = readNetSupport(pathToNet_II);


            //read I inv
            //Transition ID Firing number
            ArrayList<ArrayList<Integer>> mapIdFiringI = readNetInvariants(pathToInv_I);

            //read II inv
            //Transition ID Firing number
            ArrayList<ArrayList<Integer>> mapIdFiringII = readNetInvariants(pathToInv_II);

            HashMap<Integer, Integer> matchedNodes = new HashMap<>();
            for (Map.Entry<Integer, String> pair : mapIdNameI.entrySet()) {
                for (Map.Entry<Integer, String> secondPair : mapIdNameII.entrySet()) {
                    if (pair.getValue().equals(secondPair.getValue())) {
                        matchedNodes.put(pair.getKey(), secondPair.getKey());
                        System.out.format("Matched :" + pair.getKey() + " - " + secondPair.getKey() + " - " + pair.getValue() + "\n");
                    }
                }
            }

            for(Map.Entry<Integer, Integer> entry : supportMap.entrySet()) {
                Integer key = entry.getKey();
                Integer value = entry.getValue();

                ArrayList<Integer> invariant = mapIdFiringI.get(key);
                boolean invariantsMMatched = true;

                for(int i = 0 ; i < invariant.size() ; i++)
                {
                    if(invariant.get(i).intValue() != mapIdFiringII.get(value).get( matchedNodes.get(i)).intValue() ){
                        //System.out.println(invariant.get(i) + " != " +  mapIdFiringII.get(value).get( matchedNodes.get(i)));
                        //System.out.println("net1 inv : " + Arrays.toString(invariant.toArray()));
                        //System.out.println("net2 inv : " + Arrays.toString(mapIdFiringII.get(value).toArray()));
                        invariantsMMatched = false;
                    }
                }
                if(!invariantsMMatched)
                {
                    System.out.println("NOT MATCHED : " + key  + " - " + value);
                    System.out.println("net1 inv : " + Arrays.toString(invariant.toArray()));
                    System.out.println("net2 inv : " + Arrays.toString(mapIdFiringII.get(value).toArray()));

                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getUnused() {

        String pathToInv_I = "/home/Szavislav/SARS-Cov-2/bez.inv";
        String pathToInv_II = "/home/Szavislav/SARS-Cov-2/z.inv";

        String pathToNet_I = "/home/Szavislav/SARS-Cov-2/0-6-8-bez-sarsa.pnt";
        String pathToNet_II = "/home/Szavislav/SARS-Cov-2/sars-cov-2_v-0-6-8_2021-01-06_DF_PF_KG.pnt";

        String pathToResult = "/home/Szavislav/SARS-Cov-2/wynik.txt";


        try {
            DataInputStream in = new DataInputStream(new FileInputStream(pathToResult));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine;

            Boolean readTransitions = false;

            ArrayList<Integer> listOFFoundInvariants = new ArrayList<>();

            int count = 0;
            while ((readLine = buffer.readLine()) != null) {
                String[] line = readLine.split("inv_2:");
                String[] line_2 = line[1].split("value:");
                String val = line_2[0].replaceAll("\\s+", "");
                int lnv_2 = Integer.parseInt(val);
                listOFFoundInvariants.add(lnv_2);
            }

            ArrayList< ArrayList<Integer>> inv = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
            ArrayList< ArrayList<Integer>> result_Inv = new ArrayList<>();
            for(int i = 0 ; i < inv.size() ; i++)
            {
                if(!listOFFoundInvariants.contains(i))
                {
                    result_Inv.add(inv.get(i));
                    System.out.println("new un usded inv " + i);
                }
            }


            GUIManager.getDefaultGUIManager().getWorkspace().getProject().setT_InvMatrix(result_Inv,false);
            IOprotocols io = new IOprotocols();
            io.writeT_invINA("/home/Szavislav/SARS-Cov-2/covidoveInvarianty.inv",result_Inv,GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions());


            System.out.println("Done " );

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //-----new comparison

    public void compare_new() {
        String pathToInv_I = "/home/Szavislav/SARS-Cov-2/bez.inv";
        String pathToInv_II = "/home/Szavislav/SARS-Cov-2/z.inv";

        String pathToNet_I = "/home/Szavislav/SARS-Cov-2/0-6-8-bez-sarsa.pnt";
        String pathToNet_II = "/home/Szavislav/SARS-Cov-2/sars-cov-2_v-0-6-8_2021-01-06_DF_PF_KG.pnt";

        //read I net support
        //transition ID transition Name
        HashMap<Integer, String> mapIdNameI = readNetSupport(pathToNet_I);

        //read II net support
        //transition ID transition Name
        HashMap<Integer, String> mapIdNameII = readNetSupport(pathToNet_II);

        //read I inv
        //Transition ID Firing number
        ArrayList<ArrayList<Integer>> mapIdFiringI = readNetInvariants(pathToInv_I);

        //read II inv
        //Transition ID Firing number
        ArrayList<ArrayList<Integer>> mapIdFiringII = readNetInvariants(pathToInv_II);

        //compare vectors and supports


        //compare only supports

        ArrayList<String> resultsHeuI = heuristic_I(mapIdNameI, mapIdNameII, mapIdFiringI, mapIdFiringII, false);

        FileWriter writer;
        try {
            writer = new FileWriter("/home/Szavislav/SARS-Cov-2/wynik.txt");
            for (String str : resultsHeuI) {
                writer.write(str + System.lineSeparator());
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();


        }
        for (String line : resultsHeuI) {
            System.out.println(line);
        }
    }

    private ArrayList<String> heuristic_I(HashMap<Integer, String> mapIdNameI, HashMap<Integer, String> mapIdNameII, ArrayList<ArrayList<Integer>> mapIdFiringI, ArrayList<ArrayList<Integer>> mapIdFiringII, boolean type) {
        ArrayList<String> result = new ArrayList<>();
        //HashMap<Integer, String> mapIdNameII = (HashMap<Integer, String>) mapIdNameIIoriginal.clone();

        HashMap<Integer, Integer> matchedNodes = new HashMap<>();
        for (Map.Entry<Integer, String> pair : mapIdNameI.entrySet()) {
            for (Map.Entry<Integer, String> secondPair : mapIdNameII.entrySet()) {
                if (pair.getValue().equals(secondPair.getValue())) {
                    matchedNodes.put(pair.getKey(), secondPair.getKey());
                    System.out.format("Matched :" + pair.getKey() + " - " + secondPair.getKey() + " - " + pair.getValue() + "\n");
                }
            }
        }

        ArrayList<Integer> unmatchedNodesI = new ArrayList<>();
        for (Map.Entry<Integer, String> pair : mapIdNameI.entrySet()) {
            if (!matchedNodes.containsKey(pair.getKey())) {
                unmatchedNodesI.add(pair.getKey());
            }
        }

        ArrayList<Integer> unmatchedNodesII = new ArrayList<>();
        for (Map.Entry<Integer, String> pair : mapIdNameII.entrySet()) {
            if (!matchedNodes.containsValue(pair.getKey())) {
                unmatchedNodesII.add(pair.getKey());
            }
        }

        //Proper comparison section

        ArrayList<Double> listOfMaxVal = new ArrayList<>();
        ArrayList<Integer> listOfMaxIndex = new ArrayList<>();

        for (int i = 0; i < mapIdFiringI.size(); i++) {
            System.out.println("i - " + i + " / " + mapIdFiringI.size());
            double maxVal = -1;
            int maxIndex = -1;

            //add minus value

            for (int j = 0; j < mapIdFiringII.size(); j++) {
                if (!listOfMaxIndex.contains(j)) {
                    double val;
                    if (type) {
                        val = matchSupportsAndVectors(mapIdFiringI.get(i), mapIdFiringII.get(j), matchedNodes, unmatchedNodesI, unmatchedNodesII);
                    } else {
                        val = matchSupports(mapIdFiringI.get(i), mapIdFiringII.get(j), matchedNodes, unmatchedNodesI, unmatchedNodesII);
                    }

                    if (val > maxVal) {
                        maxVal = val;
                        maxIndex = j;
                    }
                }
            }

            listOfMaxVal.add(maxVal);
            listOfMaxIndex.add(maxIndex);

            String line = "inv_1: " + i + " inv_2: " + maxIndex + " value: " + maxVal;
            result.add(line);
        }

        return result;
    }

    private double matchSupports(ArrayList<Integer> inv1, ArrayList<Integer> inv2, HashMap<Integer, Integer> matchedNodes, ArrayList<Integer> unmatchedNodesI, ArrayList<Integer> unmatchedNodesII) {
        double value;
        ArrayList<Integer> values = new ArrayList<>();

        for (int index = 0; index < inv1.size(); index++) {
            int supportMatch;
            if (matchedNodes.containsKey(index)) {
                int secondInvariantIndex = matchedNodes.get(index);

                if (inv1.get(index) > 0 && inv2.get(secondInvariantIndex) > 0) {
                    supportMatch = 1;
                } else if (inv1.get(index) == 0 && inv2.get(secondInvariantIndex) == 0) {
                    supportMatch = 0;
                } else {
                    supportMatch = -1;
                }
            } else {
                /*
                for(int i = 0 ; i < unmatchedNodesI.size() ; i++)
                {

                    int unicalIndex = unmatchedNodesI.get(i);

                    if(inv1.get(unicalIndex)>0){
                        supportMatch = -1;
                    }
                }
                */

                supportMatch = -1;
            }

            values.add(supportMatch);
        }

        //check first net

        long counted = values.stream().filter(x -> x > 0).count();
        long maxCounted = inv1.stream().filter(x -> x > 0).count();
        value = counted * 1.0 / maxCounted;
        return value;
    }

    private int matchSupportsAndVectors(ArrayList<Integer> integers, ArrayList<Integer> integers1, HashMap<Integer, Integer> matchedNodes, ArrayList<Integer> unmatchedNodesI, ArrayList<Integer> unmatchedNodesII) {
        return 0;
    }

    private ArrayList<ArrayList<Integer>> readNetInvariants(String pathToInv_i) {
        ArrayList<ArrayList<Integer>> readedTransitions = new ArrayList<>();
        ArrayList<Integer> nodesList = new ArrayList<>();
        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(pathToInv_i));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            String backup = readLine;

            if (backup.contains("transition invariants basis")) {
                JOptionPane.showMessageDialog(null, "Wrong invariants. Only semipositives are acceptable.",
                        "ERROR:readINV", JOptionPane.ERROR_MESSAGE);
                buffer.close();
                return readedTransitions;
            }

            buffer.readLine();
            while (!readLine.contains("semipositive transition invariants =")) {
                readLine = buffer.readLine();
            }
            buffer.readLine();
            nodesList.clear();
            // Etap I - Liczba tranzycji/miejsc
            while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
                if (readLine.endsWith("~~~~~~~~~~~")) {
                    break;
                }
                String[] formattedLine = readLine.split(" ");
                for (String s : formattedLine) {
                    if (!(s.isEmpty() || s.contains("Nr."))) {
                        try {
                            nodesList.add(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista T-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<>();
            //invariantsList.clear();
            while ((readLine = buffer.readLine()) != null) {
                if (readLine.contains("@") || readLine.isEmpty()) {
                    break;
                }
                String[] formattedLine = readLine.split("\\|");
                formattedLine = formattedLine[1].split(" ");
                for (String s : formattedLine) {
                    if (!s.isEmpty()) {
                        tmpInvariant.add(Integer.parseInt(s));
                    }
                }
                if (tmpInvariant.size() == nodesList.size()) {
                    readedTransitions.add(tmpInvariant);
                    tmpInvariant = new ArrayList<>();
                }
            }
            buffer.close();
            overlord.log("T-invariants from INA file have been read.", "text", true);
            return readedTransitions;
        } catch (Exception e) {
            overlord.log("T-invariants reading operation failed.", "error", true);
            return new ArrayList<>();
        }
    }


    private HashMap<Integer, String> readNetSupport(String pathToNet_i) {
        HashMap<Integer, String> mapIdName = new HashMap<>();
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(pathToNet_i));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine;

            readLine = buffer.readLine();

            String backup = readLine;

            boolean readTransitions = false;

            while ((readLine = buffer.readLine()) != null) {
                if (readTransitions) {
                    if (readLine.contains("@")) {
                        return mapIdName;
                    }
                    //split ": "
                    // [0] cast to int [1] cast to string;
                    String[] splited = readLine.split(": ");
                    String cleaned = splited[0].replaceAll("\\s+", "");
                    System.out.println(readLine + " number spliting " + splited.length);
                    mapIdName.put(Integer.parseInt(cleaned), splited[1]);
                }

                if (readLine.contains("trans nr.             name priority time")) {
                    readTransitions = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapIdName;
    }


    //-----comapison end

    public void compare_inv_lab() {

        readT_invariants1("/home/Szavislav/SARS-Cov-2/bez.inv");
        IOprotocols io = new IOprotocols();
        io.readPNT("/home/Szavislav/SARS-Cov-2/0-6-8-bez-sarsa.pnt");
        PetriNet pn1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
        System.out.println("Wczytywanie I sieci zakońćzone - liczba inwariantów = " + invariantsList1.size());

        ArrayList<Transition> pn1T = pn1.getTransitions();

        readT_invariants2("/home/Szavislav/SARS-Cov-2/z.inv");
        io.readPNT("/home/Szavislav/SARS-Cov-2/sars-cov-2_v-0-6-8_2021-01-06_DF_PF_KG.pnt");
        PetriNet pn2 = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
        ArrayList<SimilarityInvarianStructure> ikl = new ArrayList<>();

        System.out.println("Wczytywanie II sieci zakońćzone - liczba inwariantów = " + invariantsList2.size());
        ArrayList<ArrayList<Integer>> TMPinvariantsList2 = new ArrayList<>(invariantsList2);
        ArrayList<MatchedInvariants> invariantsMatchings = new ArrayList<>();

        ArrayList<Transition> pn2T = pn2.getTransitions();

        for (int k = 0; k < invariantsList1.size(); k++) {
            ArrayList<Integer> inv_net_1 = invariantsList1.get(k);
            if (TMPinvariantsList2.size() == 0) {
                break;
            }

            ArrayList<Integer> commonPartSum = new ArrayList<>();
            ArrayList<ArrayList<Integer>> commonPart = new ArrayList<>();

            for (int j = 0; j < TMPinvariantsList2.size(); j++) {
                ArrayList<Integer> part = new ArrayList<>();
                int value = 0;
                for (int i = 0; i < inv_net_1.size(); i++) {
                    for (int b = 0; b < TMPinvariantsList2.get(j).size(); b++) {
                        System.out.println(i + " - " + j + " - " + b);
                        if (inv_net_1.get(i) != 0 && TMPinvariantsList2.get(j).get(b) != 0 && pn1T.get(i).getName().equals(pn2T.get(b).getName())) {
                            value += inv_net_1.get(i);
                            part.add(inv_net_1.get(i));
                        }
                        //} else {
                        //    value += 0;//Math.min(inv_net_1.get(i), TMPinvariantsList2.get(j).get(i));
                        //    part.add(0);//Math.min(inv_net_1.get(i), TMPinvariantsList2.get(j).get(i)));
                        //}
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
/*
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
  */
    }


    public void compare_baldan_cocco() {

        readT_invariants1("/home/Szavislav/SARS-Cov-2/0-6-8-bez-sarsa-tinv-ElementaryModes.inv");
        System.out.println("Wczytywanie I sieci zakońćzone - liczba inwariantów = " + invariantsList1.size());

        readT_invariants2("/home/Szavislav/SARS-Cov-2/t-inv-0-6-8-ElementaryModes.inv");
        ArrayList<SimilarityInvarianStructure> ikl = new ArrayList<>();

        System.out.println("Wczytywanie II sieci zakońćzone");
        ArrayList<ArrayList<Integer>> TMPinvariantsList2 = new ArrayList<>(invariantsList2);
        ArrayList<MatchedInvariants> invariantsMatchings = new ArrayList<>();

        for (int k = 0; k < invariantsList1.size(); k++) {
            ArrayList<Integer> inv_net_1 = invariantsList1.get(k);
            if (TMPinvariantsList2.size() == 0) {
                break;
            }

            double index_Sorensen = -1;
            double index_Tanimoto = -1;
            double index_Jacard = -1;

            ArrayList<Integer> commonPartSum = new ArrayList<>();
            ArrayList<ArrayList<Integer>> commonPart = new ArrayList<>();
            for (ArrayList<Integer> integers : TMPinvariantsList2) {
                ArrayList<Integer> part = new ArrayList<>();
                int value = 0;
                for (int i = 0; i < inv_net_1.size(); i++) {
                    if (inv_net_1.get(i).equals(integers.get(i))) {
                        value += inv_net_1.get(i);
                        part.add(inv_net_1.get(i));
                    } else {
                        value += Math.min(inv_net_1.get(i), integers.get(i));
                        part.add(Math.min(inv_net_1.get(i), integers.get(i)));
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

        for (MatchedInvariants invariantsMatching : invariantsMatchings) {
            index_I += invariantsMatching.index_I;
            index_II += invariantsMatching.index_II;
            index_III += invariantsMatching.index_III;
            index_Jacard += invariantsMatching.index_Jacard;
            index_Tanimoto += invariantsMatching.index_Tanimoto;
            index_Sorensen += invariantsMatching.index_Sorensen;
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

            for (SimilarityInvarianStructure invariant : invariants) {
                StringBuilder knocked = new StringBuilder();
                for (Integer in : invariant.knockedInvariantIndexes
                ) {
                    knocked.append(in).append(",");
                }
                pw.print(invariant.originalInvariantindex + " :   " + knocked);
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
                for (String s : formattedLine) {
                    if (!(s.isEmpty() || s.contains("Nr."))) {
                        try {
                            nodesList1.add(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista T-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<>();
            invariantsList1.clear();
            while ((readLine = buffer.readLine()) != null) {
                if (readLine.contains("@") || readLine.isEmpty()) {
                    break;
                }
                String[] formattedLine = readLine.split("\\|");
                formattedLine = formattedLine[1].split(" ");
                for (String s : formattedLine) {
                    if (!s.isEmpty()) {
                        tmpInvariant.add(Integer.parseInt(s));
                    }
                }
                if (tmpInvariant.size() == nodesList1.size()) {
                    invariantsList1.add(tmpInvariant);
                    tmpInvariant = new ArrayList<>();
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
                for (String s : formattedLine) {
                    if (!(s.isEmpty() || s.contains("Nr."))) {
                        try {
                            nodesList2.add(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista T-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<>();
            invariantsList2.clear();
            while ((readLine = buffer.readLine()) != null) {
                if (readLine.contains("@") || readLine.isEmpty()) {
                    break;
                }
                String[] formattedLine = readLine.split("\\|");
                formattedLine = formattedLine[1].split(" ");
                for (String s : formattedLine) {
                    if (!s.isEmpty()) {
                        tmpInvariant.add(Integer.parseInt(s));
                    }
                }
                if (tmpInvariant.size() == nodesList2.size()) {
                    invariantsList2.add(tmpInvariant);
                    tmpInvariant = new ArrayList<>();
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

            double fniSUM = firstNetInvariant.stream().mapToInt(Integer::intValue).sum();
            double sniSUM = secondNetInvariant.stream().mapToInt(Integer::intValue).sum();
            double cpSUM = commonPart.stream().mapToInt(Integer::intValue).sum();
            double mpSUM = maxPart.stream().mapToInt(Integer::intValue).sum();

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
