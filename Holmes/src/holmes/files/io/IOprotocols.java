package holmes.files.io;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.windows.decompositions.HolmesBranchVerticesPrototype;

/**
 * Klasa odpowiedzialna za protokoły komunikacyjne z programem INA, Charlie, itd. Precyzyjnie,
 * posiada ona metody zapisu i odczytu plików sieci, inwariantów i innych zbiorów analitycznych
 * do/z różnych formatów plików.
 *
 * @author students - pierwsze wersje metod w czterech oddzielnych klasach
 * @author MR - integracja w jedną klasę, writeINV - przeróbka, aby w ogóle działało
 */
public class IOprotocols {
    private GUIManager overlord;
    private ArrayList<ArrayList<Integer>> invariantsList; // = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Integer> nodesList; // = new ArrayList<Integer>();
    private ArrayList<Node> nodeArray; // = new ArrayList<Node>();
    private ArrayList<Arc> arcArray; // = new ArrayList<Arc>();
    private ArrayList<ElementLocation> elemArray; // = new ArrayList<ElementLocation>();

    private int MatSiz; // = 99999;
    @SuppressWarnings("unused")
    private String netName; // = "";
    private int globalPlaceNumber; // = 0;
    private int stage; // = 1;
    private int placeCount;//  = 0;
    private ArrayList<String[]> placeArcListPost; // = new ArrayList<String[]>();
    private ArrayList<ArrayList<Integer>> placeArcListPostWeight; // = new ArrayList<ArrayList<Integer>>();
    private ArrayList<String[]> placeArcListPre; // = new ArrayList<String[]>();
    private ArrayList<ArrayList<Integer>> placeArcListPreWeight; // = new ArrayList<ArrayList<Integer>>();

    /**
     * Konstruktor obiektu klasy IOprotocols.
     */
    public IOprotocols() {
        overlord = GUIManager.getDefaultGUIManager();
        resetComponents();
    }

    /**
     * Zwraca tablice inwariantow z wczytanego pliku INA
     *
     * @return invariantsList - lista inwariantów
     */
    public ArrayList<ArrayList<Integer>> getInvariantsList() {
        return invariantsList;
    }

    /**
     * Metoda resetująca pola klasy.
     */
    private void resetComponents() {
        invariantsList = new ArrayList<ArrayList<Integer>>();
        nodesList = new ArrayList<Integer>();
        nodeArray = new ArrayList<Node>();
        arcArray = new ArrayList<Arc>();
        elemArray = new ArrayList<ElementLocation>();

        MatSiz = 99999;
        netName = "";
        globalPlaceNumber = 0;
        stage = 1;
        placeCount = 0;
        placeArcListPost = new ArrayList<String[]>();
        placeArcListPostWeight = new ArrayList<ArrayList<Integer>>();
        placeArcListPre = new ArrayList<String[]>();
        placeArcListPreWeight = new ArrayList<ArrayList<Integer>>();
    }

    /**
     * Wczytywanie pliki t-inwariantów INA, wcześniej: INAinvariants.read
     * Dodano poprawki oraz drugą ściękę odczytu - jako plik inwariantów Charliego.
     *
     * @param path String - scieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    public boolean readT_invariants(String path) {

        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            String backup = readLine;

            if (readLine.contains("transition sub/sur/invariants for net")) {
                //to znaczy, że wczytujemy plik INA, po prostu
            } else if (readLine.contains("List of all elementary modes")) {
                buffer.close();
                return readMonaLisaT_inv(path);
            } else if (readLine.contains("minimal semipositive transition")) {
                buffer.close();
                return readCharlieT_inv(path);
            } else {
                Object[] options = {"Read as INA file", "Read as MonaLisa file", "Read as Charlie file", "Terminate reading",};
                int decision = JOptionPane.showOptionDialog(null,
                        "Unknown or corrupted t-invariants file format.\nPlease choose format for this t-invariants file.",
                        "Error reading file header", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (decision == 1) {
                    buffer.close();
                    return readMonaLisaT_inv(path);
                } else if (decision == 2) { //Charlie
                    buffer.close();
                    return readCharlieT_inv(path);
                } else if (decision == 3) {
                    buffer.close();
                    return false;
                }
                //jeśli nie 1, 2 lub 3 to znaczy, że 0, czyli na sieć czytamy dalej jako INA inv.
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
            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            invariantsList.clear();
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
                    invariantsList.add(tmpInvariant);
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

    /**
     * Wczytywanie pliki t-inwariantów INA, wcześniej: INAinvariants.read
     * Dodano poprawki oraz drugą ściękę odczytu - jako plik inwariantów Charliego.
     *
     * @param path String - scieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    public ArrayList<ArrayList<Integer>> readT_invariantsOut(String path) {

        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            //String backup = readLine;

            buffer.readLine();
            while (!readLine.contains("semipositive transition invariants =")) {
                readLine = buffer.readLine();
            }
            buffer.readLine();
            //nodesList.clear();

            ArrayList<Integer> nodesListTmp = new ArrayList<>();
            // Etap I - Liczba tranzycji/miejsc
            while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
                if (readLine.endsWith("~~~~~~~~~~~")) {
                    break;
                }
                String[] formattedLine = readLine.split(" ");
                for (String s : formattedLine) {
                    if (!(s.isEmpty() || s.contains("Nr."))) {
                        try {
                            nodesListTmp.add(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista T-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            //invariantsList.clear();
            ArrayList<ArrayList<Integer>> invariantsListOut = new ArrayList<>();
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
                if (tmpInvariant.size() == nodesListTmp.size()) {
                    invariantsListOut.add(tmpInvariant);
                    tmpInvariant = new ArrayList<Integer>();
                }
            }
            buffer.close();
            overlord.log("T-invariants from INA file have been read.", "text", true);
            return invariantsListOut;
        } catch (Exception e) {
            overlord.log("T-invariants reading operation failed.", "error", true);
            return new ArrayList<>();
        }
    }


    /**
     * Wczytywanie pliki p-inwariantów INA, wcześniej: INAinvariants.read
     * Dodano poprawki oraz drugą ściękę odczytu - jako plik inwariantów Charliego.
     *
     * @param path String - scieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    public ArrayList<ArrayList<Integer>> readP_invariantsOut(String path) {
        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            //String backup = readLine;

            buffer.readLine();
            while (!readLine.contains("semipositive place invariants =")) {
                readLine = buffer.readLine();
            }
            buffer.readLine();
            //nodesList.clear();

            ArrayList<Integer> nodesListTmp = new ArrayList<>();
            // Etap I - Liczba tranzycji/miejsc
            while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
                if (readLine.endsWith("~~~~~~~~~~~")) {
                    break;
                }
                String[] formattedLine = readLine.split(" ");
                for (String s : formattedLine) {
                    if (!(s.isEmpty() || s.contains("Nr."))) {
                        try {
                            nodesListTmp.add(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            overlord.log("Reading file failed in header section.", "text", true);
                        }
                    }
                }
            }
            // Etap II - lista P-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            //invariantsList.clear();
            ArrayList<ArrayList<Integer>> invariantsListOut = new ArrayList<>();
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
                if (tmpInvariant.size() == nodesListTmp.size()) {
                    invariantsListOut.add(tmpInvariant);
                    tmpInvariant = new ArrayList<Integer>();
                }
            }
            buffer.close();
            overlord.log("P-invariants from INA file have been read.", "text", true);
            return invariantsListOut;
        } catch (Exception e) {
            overlord.log("P-invariants reading operation failed.", "error", true);
            return new ArrayList<>();
        }
    }

    /**
     * Metoda odpowiedzialna za wczytywanie p-inwariantów z pliku wygenerowanego programem INAwin32.exe
     *
     * @param path String - ścieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła.
     */
    public boolean readP_invariants(String path) {
        try {
            resetComponents();
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            String backup = readLine;

            if (readLine.contains("place sub/sur/invariants for net")) {
                //to znaczy, że wczytujemy plik INA
            } else if (readLine.contains("List of all place invariants")) {
                buffer.close();
                return readMonaLisaP_inv(path);
            } else if (readLine.contains("minimal semipositive place")) {
                buffer.close();
                return readCharlieP_inv(path);
            } else {
                Object[] options = {"Read as INA file", "Read as MonaLisa file", "Read as Charlie file", "Terminate reading",};
                int decision = JOptionPane.showOptionDialog(null,
                        "Unknown or corrupted p-invariants file format.\nPlease choose format for this p-invariants file.",
                        "Error reading file header", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (decision == 1) {
                    buffer.close();
                    return readMonaLisaP_inv(path);
                } else if (decision == 2) { //Charlie
                    buffer.close();
                    return readCharlieP_inv(path);
                } else if (decision == 3) {
                    buffer.close();
                    return false;
                }
                //jeśli nie 1, 2 lub 3 to znaczy, że 0, czyli na sieć czytamy dalej jako INA inv.
            }

            if (backup.contains("transition invariants basis")) {
                JOptionPane.showMessageDialog(null, "Wrong invariants. Only semipositives are acceptable.",
                        "ERROR:readINV", JOptionPane.ERROR_MESSAGE);
                buffer.close();
                return false;
            }

            buffer.readLine();
            while (!readLine.contains("semipositive place invariants =")) {
                readLine = buffer.readLine();
            }
            buffer.readLine();
            nodesList.clear();
            // Etap I - Liczba tranzycji/miejsc
            while (!(readLine = buffer.readLine()).endsWith("~~~~~~~~~~~")) {
                if (readLine.endsWith("~~~~~~~~~~~"))
                    break;

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
            // Etap II - lista P-inwariantow
            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            invariantsList.clear();
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
                    invariantsList.add(tmpInvariant);
                    tmpInvariant = new ArrayList<Integer>();
                }
            }
            buffer.close();
            overlord.log("P-invariants from INA file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("P-invariants reading operation failed.", "error", true);
            return false;
        }
    }

    /**
     * Metoda wczytująca plik t-inwariantów wygenerowany programem Charlie.
     *
     * @param path String - ścieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    private boolean readCharlieT_inv(String path) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();

            if (!readLine.contains("minimal semipositive transition")) {
                Object[] options = {"Force read as Charlie file", "Terminate reading",};
                int n = JOptionPane.showOptionDialog(null,
                        "Unknown or corrupted t-invariants file format!\nRead anyway as Charlie invariants?",
                        "Error reading file header", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);

                if (n == 1) {
                    buffer.close();
                    return false;
                }

            }
            nodesList.clear();

            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            boolean firstPass = true;

            ArrayList<Transition> namesCheck = overlord.getWorkspace().getProject().getTransitions();

            int transSetSize = namesCheck.size();
            for (int t = 0; t < transSetSize; t++) //init
                tmpInvariant.add(0);

            readLine = buffer.readLine();
            while (readLine != null && readLine.length() > 0) {
                String lineStart = readLine.substring(0, readLine.indexOf("|"));
                lineStart = lineStart.replace(" ", "");
                lineStart = lineStart.replace("\t", "");

                if (lineStart.length() > 0 && !firstPass) { //początek inwariantu
                    invariantsList.add(tmpInvariant);

                    tmpInvariant = new ArrayList<Integer>();
                    for (int t = 0; t < transSetSize; t++) // init
                        tmpInvariant.add(0);

                }
                firstPass = false;

                readLine = readLine.substring(readLine.indexOf("|") + 1);
                readLine = readLine.replace(" ", "");
                readLine = readLine.replace("\t", "");

                String tmp = readLine.substring(0, readLine.indexOf("."));
                int transNumber = Integer.parseInt(tmp); //numer tranzycji w zbiorze

                readLine = readLine.substring(readLine.indexOf(".") + 1);
                String transName = readLine.substring(0, readLine.indexOf(":"));

                String orgName = namesCheck.get(transNumber).getName();
                if (!transName.equals(orgName)) {
                    overlord.log("Transition name and location do not match!"
                            + " Read transition: " + transName + " (loc:" + transNumber + "), while in net: " + orgName, "text", true);
                }

                readLine = readLine.substring(readLine.indexOf(":") + 1);
                readLine = readLine.replace(",", "");
                int transValue = Integer.parseInt(readLine);

                if (transNumber >= transSetSize) {
                    overlord.log("Charlie t-invariants file has reference to non existing transitions in the current net."
                            + " Operation cancelled.", "text", true);
                    buffer.close();
                    return false;
                }
                tmpInvariant.set(transNumber, transValue);

                readLine = buffer.readLine();
            }

            //dodaj ostatni inwariant do listy
            invariantsList.add(tmpInvariant);

            buffer.close();
            overlord.log("T-invariants from Charlie file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("Charlie t-invariants reading operation failed.", "text", true);
            return false;
        }
    }

    /**
     * Metoda wczytująca plik p-inwariantów wygenerowany programem Charlie.
     *
     * @param path String - ścieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    private boolean readCharlieP_inv(String path) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();

            if (!readLine.contains("minimal semipositive place invariants")) {
                Object[] options = {"Force read as Charlie file", "Terminate reading",};
                int n = JOptionPane.showOptionDialog(null,
                        "Unknown or corrupted p-invariants file format!\nRead anyway as Charlie invariants?",
                        "Error reading file header", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);

                if (n == 1) {
                    buffer.close();
                    return false;
                }

            }
            nodesList.clear();

            ArrayList<Integer> tmpInvariant = new ArrayList<Integer>();
            boolean firstPass = true;

            //ArrayList<Transition> namesCheck = overlord.getWorkspace().getProject().getTransitions();
            ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();

            int placesSetSize = places.size();
            for (int p = 0; p < placesSetSize; p++) //init
                tmpInvariant.add(0);

            readLine = buffer.readLine();
            while (readLine != null && readLine.length() > 0) {
                String lineStart = readLine.substring(0, readLine.indexOf("|"));
                lineStart = lineStart.replace(" ", "");
                lineStart = lineStart.replace("\t", "");

                if (lineStart.length() > 0 && !firstPass) { //początek inwariantu
                    invariantsList.add(tmpInvariant);

                    tmpInvariant = new ArrayList<Integer>();
                    for (int p = 0; p < placesSetSize; p++) // init
                        tmpInvariant.add(0);

                }
                firstPass = false;

                readLine = readLine.substring(readLine.indexOf("|") + 1);
                readLine = readLine.replace(" ", "");
                readLine = readLine.replace("\t", "");

                String tmp = readLine.substring(0, readLine.indexOf("."));
                int placeNumber = Integer.parseInt(tmp); //numer miejsca w zbiorze

                readLine = readLine.substring(readLine.indexOf(".") + 1);
                String placeName = readLine.substring(0, readLine.indexOf(":"));

                String orgName = places.get(placeNumber).getName();
                if (!placeName.equals(orgName)) {
                    overlord.log("Place name and location do not match!"
                            + " Read place: " + placeName + " (loc:" + placeNumber + "), while in net: " + orgName, "text", true);
                }

                readLine = readLine.substring(readLine.indexOf(":") + 1);
                readLine = readLine.replace(",", "");
                int transValue = Integer.parseInt(readLine);

                if (placeNumber >= placesSetSize) {
                    overlord.log("Charlie p-invariants file has reference to non existing places in the current net."
                            + " Operation cancelled.", "text", true);
                    buffer.close();
                    return false;
                }
                tmpInvariant.set(placeNumber, transValue);

                readLine = buffer.readLine();
            }
            invariantsList.add(tmpInvariant);

            buffer.close();
            overlord.log("P-invariants from Charlie file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("Charlie p-invariants reading operation failed.", "text", true);
            return false;
        }
    }

    /**
     * Metoda wczytująca plik t-inwariantów wygenerowany programem MonaLisa.
     *
     * @param path String - ścieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    private boolean readMonaLisaT_inv(String path) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String line = buffer.readLine();

            if (!line.contains("# List of all elementary modes")) {
                Object[] options = {"Force read as Mona Lisa file", "Terminate reading",};
                int n = JOptionPane.showOptionDialog(null,
                        "Unknown or corrupted invariants file format! Read anyway as Mona Lisa invariants?",
                        "Error reading file header", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);

                if (n == 1) {
                    buffer.close();
                    return false;
                }

            }
            nodesList.clear();

            ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
            int transSetSize = transitions.size();

            ArrayList<Integer> tmpInvariant;

            line = buffer.readLine();
            while (line != null && line.contains("Elementary")) {
                tmpInvariant = new ArrayList<Integer>();
                for (int t = 0; t < transSetSize; t++) //init
                    tmpInvariant.add(0);

                String lineNumber = line.substring(0, line.indexOf("."));

                try {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.trim();
                    String[] tablica = line.split(" ");
                    for (String el : tablica) {
                        if (el.contains("*")) {
                            String valueS = el.substring(0, el.indexOf("*"));
                            int value = Integer.parseInt(valueS);

                            el = el.substring(el.indexOf("*") + 1);
                            int trans = Integer.parseInt(el);
                            trans--; //MonaLisa liczy od 1, nie od 0
                            tmpInvariant.set(trans, value);
                        } else {
                            int trans = Integer.parseInt(el);
                            trans--; //MonaLisa liczy od 1, nie od 0
                            tmpInvariant.set(trans, 1);
                        }
                    }
                    invariantsList.add(tmpInvariant);
                    line = buffer.readLine();
                } catch (Exception e) {
                    overlord.log("Error reading t-invariant #" + lineNumber, "error", true);
                    line = buffer.readLine();
                }
            }
            buffer.close();
            overlord.log("T-invariants from MonaLisa file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("MonaLisa t-invariants reading operation failed.", "text", true);
            return false;
        }
    }

    /**
     * Metoda wczytująca plik p-inwariantów wygenerowany programem MonaLisa.
     *
     * @param path String - ścieżka do pliku
     * @return boolean - true, jeśli operacja się powiodła
     */
    private boolean readMonaLisaP_inv(String path) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String line = buffer.readLine();

            if (!line.contains("# List of all place invariants")) {
                Object[] options = {"Force read as Mona Lisa file", "Terminate reading",};
                int n = JOptionPane.showOptionDialog(null,
                        "Unknown or corrupted invariants file format!\nRead anyway as Mona Lisa p-invariants?",
                        "Error reading file header", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);

                if (n == 1) {
                    buffer.close();
                    return false;
                }

            }
            nodesList.clear();

            ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
            int placesSetSize = places.size();
            ArrayList<Integer> tmpInvariant;

            line = buffer.readLine();
            while (line != null && line.contains("Place")) {
                tmpInvariant = new ArrayList<Integer>();
                for (int t = 0; t < placesSetSize; t++) //init
                    tmpInvariant.add(0);

                String lineNumber = line.substring(0, line.indexOf("."));

                try {
                    line = line.substring(line.indexOf(":") + 1);
                    line = line.trim();
                    String[] tablica = line.split(" ");
                    for (String el : tablica) {
                        if (el.contains("*")) {
                            String valueS = el.substring(0, el.indexOf("*"));
                            int value = Integer.parseInt(valueS);

                            el = el.substring(el.indexOf("*") + 1);
                            int place = Integer.parseInt(el);
                            place--; //MonaLisa liczy od 1, nie od 0
                            tmpInvariant.set(place, value);
                        } else {
                            int trans = Integer.parseInt(el);
                            trans--; //MonaLisa liczy od 1, nie od 0
                            tmpInvariant.set(trans, 1);
                        }
                    }
                    invariantsList.add(tmpInvariant);
                    line = buffer.readLine();
                } catch (Exception e) {
                    overlord.log("Error reading p-invariant #" + lineNumber, "error", true);
                    line = buffer.readLine();
                }
            }
            buffer.close();
            overlord.log("P-invariants from MonaLisa file have been read.", "text", true);
            return true;
        } catch (Exception e) {
            overlord.log("MonaLisa p-invariants reading operation failed.", "text", true);
            return false;
        }
    }

    /**
     * Zapis t-inwariantow do pliku w formacie INA.
     *
     * @param path        String - scieżka do pliku
     * @param invariants  ArrayList[ArrayList[Integer]] - lista t-inwariantów
     * @param transitions ArrayList[Transition] - lista tranzycji
     */
    public void writeT_invINA(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Transition> transitions) {
        try {
            String extension = "";
            if (!path.contains(".inv"))
                extension = ".inv";
            PrintWriter pw = new PrintWriter(path + extension);

            pw.print("transition sub/sur/invariants for net 0.t\r\n");
            pw.print("\r\n");
            pw.print("semipositive transition invariants =\r\n");
            pw.print("\r\n");
            pw.print("Nr.      ");

            int delimiter = 13;
            if (transitions.size() < 100)
                delimiter = 17;
            int multipl = 1;
            int transNo = invariants.get(0).size();

            for (int i = 0; i < transitions.size(); i++) {
                if (transNo >= 100)
                    pw.print(convertIntToStr(true, i));
                else
                    pw.print(convertIntToStr(false, i));

                if (i == (multipl * delimiter) - 1) {
                    pw.print("\r\n");
                    pw.print("        ");
                    multipl++;
                }
            }
            pw.print("\r\n");
            pw.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            pw.print("\r\n");

            for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach

                if (transNo >= 100) {
                    pw.print(convertIntToStr(true, i) + " |   ");
                } else
                    pw.print(convertIntToStr(false, i) + " |   ");

                multipl = 1;
                for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycja inwariantu
                    int tr = invariants.get(i).get(t); // nr tranzycji
                    if (transNo >= 100)
                        pw.print(convertIntToStr(true, tr)); //tutaj wstawiamy wartość dla tranz. w inw.
                    else
                        pw.print(convertIntToStr(false, tr)); //tutaj wstawiamy wartość dla tranz. w inw.

                    if (t == (multipl * delimiter) - 1) { //rozdzielnik wierszy
                        pw.print("\r\n");
                        if (transNo >= 100)
                            pw.print("      |   ");
                        else
                            pw.print("     |   ");
                        multipl++;
                    }
                }
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

    /**
     * Zapis p-inwariantow do pliku w formacie INA.
     *
     * @param path       String - scieżka do pliku
     * @param invariants ArrayList[ArrayList[Integer]] - lista p-inwariantów
     * @param places     ArrayList[Place] - lista miejsc
     */
    public void writeP_invINA(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Place> places) {
        try {
            String extension = "";
            if (!path.contains(".inv"))
                extension = ".inv";
            PrintWriter pw = new PrintWriter(path + extension);

            pw.print("place sub/sur/invariants for net 0.net.pnt        :\r\n");
            pw.print("\r\n");
            pw.print("semipositive place invariants =\r\n");
            pw.print("\r\n");
            pw.print("Nr.      ");

            int delimiter = 13;
            if (places.size() < 100)
                delimiter = 17;
            int multipl = 1;
            int placesNo = invariants.get(0).size();

            for (int i = 0; i < places.size(); i++) {
                if (placesNo >= 100)
                    pw.print(convertIntToStr(true, i));
                else
                    pw.print(convertIntToStr(false, i));

                if (i == (multipl * delimiter) - 1) {
                    pw.print("\r\n");
                    pw.print("        ");
                    multipl++;
                }
            }
            pw.print("\r\n");
            pw.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            pw.print("\r\n");

            for (int i = 0; i < invariants.size(); i++) { //po wszystkich p-inwariantach
                if (placesNo >= 100) {
                    pw.print(convertIntToStr(true, i) + " |   ");
                } else
                    pw.print(convertIntToStr(false, i) + " |   ");

                multipl = 1;
                for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich miejscach inwariantu
                    int tr = invariants.get(i).get(t); // nr tranzycji
                    if (placesNo >= 100)
                        pw.print(convertIntToStr(true, tr)); //tutaj wstawiamy wartość dla miejsca. w inw.
                    else
                        pw.print(convertIntToStr(false, tr)); //tutaj wstawiamy wartość dla miejsca. w inw.

                    if (t == (multipl * delimiter) - 1) { //rozdzielnik wierszy
                        pw.print("\r\n");
                        if (placesNo >= 100)
                            pw.print("      |   ");
                        else
                            pw.print("     |   ");
                        multipl++;
                    }
                }
                pw.print("\r\n");
            }
            pw.print("\r\n");
            pw.print("@");
            pw.close();
            overlord.log("P-invariants in INA file format saved to " + path, "text", true);
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writeP_invINA", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }
    }

    /**
     * Metoda pomocnicza zwracająca liczbę w formie String z odpowiednią liczbą spacji.
     * Metoda ta jest niezbędna do zapisu pliku inwariantów w formacie programu INA.
     *
     * @param large boolean - true, jeśli dla dużej sieci
     * @param tr    int - liczba do konwersji
     * @return String - liczba po konwersji
     */
    private String convertIntToStr(boolean large, int tr) {
        //String result = "";
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
        //return result = " "+tr;
    }

    /**
     * Metoda zwraca nazwę pliku.
     *
     * @param sciezka - scieżka do pliku
     * @return String - nazwa pliku
     */
    public String getFileName(String sciezka) {
        String[] tablica = sciezka.split("\\\\");
        return tablica[tablica.length - 1];
    }

    /**
     * Metoda zwraca listę wezłów sieci po tym jak readPNT przeczyta plik INY
     *
     * @return nodeArray[Node] - tablica węzłów sieci
     */
    public ArrayList<Node> getNodeArray() {
        return nodeArray;
    }

    /**
     * Zwraca liste krawedzi sieci po tym jak readPNT przeczyta plik INY
     *
     * @return arcArray[Arc] - lista łuków sieci
     */
    public ArrayList<Arc> getArcArray() {
        return arcArray;
    }

    /**
     * Czyta plik sieci petriego w formacie PNT (INA) na serverze
     *
     * @param sciezka String - scieżka do pliku
     */
    public PetriNet serverReadPNT(String sciezka, int SID) {
        try {
            resetComponents();
            //int SID = overlord.getWorkspace().getProject().returnCleanSheetID();
            DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String wczytanaLinia = buffer.readLine();
            String[] tabWczytanaLinia = wczytanaLinia.split(":");
            netName = tabWczytanaLinia[1];
            int[][] trans = new int[MatSiz][2];
            int ID = 0;
            String[] wID = new String[MatSiz];
            int[] wMark = new int[MatSiz];
            ArrayList<Integer> wagiWej = new ArrayList<Integer>();
            ArrayList<Integer> wagiWyj = new ArrayList<Integer>();

            while ((wczytanaLinia = buffer.readLine()) != null) {
                // Etap I

                // Wczytywanie informacji o Arcach i tokenach
                if (wczytanaLinia.equals("@")) {
                    stage++;
                }
                switch (stage) {

                    case 1:
                        ArrayList<String> tmpStringWej = new ArrayList<String>();
                        ArrayList<String> tmpStringWyj = new ArrayList<String>();

                        wczytanaLinia = wczytanaLinia.replace(",", " , ");
                        wczytanaLinia = wczytanaLinia.replace(":", " : ");
                        String[] WczytanyString = wczytanaLinia.split(" ");
                        int poz = 0;
                        int poZap = 0;
                        for (String s : WczytanyString) {
                            if (!s.isEmpty()) {
                                if (!Character.isWhitespace(s.charAt(0))) {
                                    if (s.contains(",")) {
                                        poZap = poz;
                                        poz = 5;
                                    }
                                    if (s.contains(":")) {
                                        poZap = poz;
                                        poz = 4;
                                    }

                                    switch (poz) {
                                        // numer miejsca
                                        case 0:
                                            wID[ID] = s;
                                            poz++;
                                            break;
                                        // ilosc tokenow
                                        case 1:
                                            wMark[ID] = Integer.parseInt(s);
                                            ID++;
                                            poz++;
                                            break;
                                        // wchodzace
                                        case 2:
                                            tmpStringWej.add(s);
                                            wagiWej.add(1);
                                            break;
                                        // wychodzace
                                        case 3:
                                            tmpStringWyj.add(s);
                                            wagiWyj.add(1);
                                            break;
                                        case 4:
                                            if (!s.contains(":")) {
                                                if (poZap == 2) {
                                                    wagiWej.remove(wagiWej.size() - 1);
                                                    wagiWej.add(Integer.parseInt(s));
                                                } else {
                                                    wagiWyj.remove(wagiWyj.size() - 1);
                                                    wagiWyj.add(Integer.parseInt(s));
                                                }
                                                poz = poZap;
                                            }
                                            break;
                                        case 5:
                                            poz = poZap;
                                            poz++;
                                            break;
                                    }
                                }
                            }
                        }
                        String[] a = new String[tmpStringWej.size()];
                        placeArcListPre.add(tmpStringWej.toArray(a));
                        placeArcListPreWeight.add(wagiWej);
                        a = new String[tmpStringWyj.size()];
                        placeArcListPost.add(tmpStringWyj.toArray(a));
                        placeArcListPostWeight.add(wagiWyj);

                        break;
                    case 2:
                        // Etap II
                        // Wczytywanie danych o miejscach

                        if ((wczytanaLinia.contains("capacity") && wczytanaLinia.contains("time")
                                && wczytanaLinia.contains("name")) || wczytanaLinia.equals("@")) {
                        } else {
                            tabWczytanaLinia = wczytanaLinia.split(": ");
                            //String[] tmp4 = tabWczytanaLinia[0].split(" ");
                            int placeNumber = globalPlaceNumber;
                            globalPlaceNumber++;
                            tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
                            String placeName = tabWczytanaLinia[0];
                            Place tmpPlace = new Place(placeNumber, new ArrayList<ElementLocation>(), placeName, "", wMark[placeNumber]);
                            ArrayList<ElementLocation> namesLoc = new ArrayList<ElementLocation>();
                            namesLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(namesLoc, GUIManager.locationMoveType.NAME);
                            //XTPN:
                            ArrayList<ElementLocation> alphaLoc = new ArrayList<ElementLocation>();
                            alphaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);

                            ArrayList<ElementLocation> betaLoc = new ArrayList<ElementLocation>();
                            betaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);

                            ArrayList<ElementLocation> gammaLoc = new ArrayList<ElementLocation>();
                            gammaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);

                            ArrayList<ElementLocation> tauLoc = new ArrayList<ElementLocation>();
                            tauLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);

                            nodeArray.add(tmpPlace);
                        }
                        break;
                    case 3:
                        // Etap III
                        // Wczytywanie danych o tranzycjach
                        if ((wczytanaLinia.contains("priority") && wczytanaLinia.contains("time")
                                && wczytanaLinia.contains("name")) || wczytanaLinia.equals("@")) {
                            placeCount = globalPlaceNumber;
                        } else {
                            tabWczytanaLinia = wczytanaLinia.split(": ");
                            String[] tmp5 = tabWczytanaLinia[0].split(" ");
                            for (String s : tmp5) {
                                if (!s.isEmpty()) {
                                    globalPlaceNumber++;
                                    trans[Integer.parseInt(s)][0] = Integer.parseInt(s);
                                    trans[Integer.parseInt(s)][1] = globalPlaceNumber;
                                }
                            }

                            int transNumber = globalPlaceNumber;
                            tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
                            String transName = tabWczytanaLinia[0];
                            Transition tmpTrans = new Transition(transNumber, new ArrayList<ElementLocation>(), transName, "");

                            ArrayList<ElementLocation> namesLoc = new ArrayList<ElementLocation>();
                            namesLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(namesLoc, GUIManager.locationMoveType.NAME);
                            //XTPN
                            ArrayList<ElementLocation> alphaLoc = new ArrayList<ElementLocation>();
                            alphaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);

                            ArrayList<ElementLocation> betaLoc = new ArrayList<ElementLocation>();
                            betaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);

                            ArrayList<ElementLocation> gammaLoc = new ArrayList<ElementLocation>();
                            gammaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);

                            ArrayList<ElementLocation> tauLoc = new ArrayList<ElementLocation>();
                            tauLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);

                            nodeArray.add(tmpTrans);
                            //mark++;
                        }
                        break;
                    case 4:
                        // Tworzenie Arców, szerokosci okna
                        // tworzenie dla kazdego noda element location
                        for (int j = 0; j < nodeArray.size(); j++) {
                            if (nodeArray.get(j).getType() == PetriNetElementType.PLACE) {
                                elemArray.add(new ElementLocation(SID, new Point(80, 30 + j * 60), nodeArray.get(j)));
                                ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
                                tempElementLocationArry.add(elemArray.get(j));
                                nodeArray.get(j).setElementLocations(tempElementLocationArry);
                            }

                            if (nodeArray.get(j).getType() == PetriNetElementType.TRANSITION) {
                                elemArray.add(new ElementLocation(SID, new Point(280, 30 + (j - placeCount) * 60), nodeArray.get(j)));
                                ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
                                tempElementLocationArry.add(elemArray.get(j));
                                nodeArray.get(j).setElementLocations(tempElementLocationArry);
                            }
                        }

                        int pozycja_a = 0;
                        // Arki
                        for (int k = 0; k < placeArcListPre.size(); k++) {
                            for (int j = 0; j < placeArcListPre.get(k).length; j++) {
                                int t1 = trans[Integer.parseInt(placeArcListPre.get(k)[j])][1];
                                arcArray.add(new Arc(nodeArray.get(t1 - 1).getLastLocation(),
                                        nodeArray.get(k).getLastLocation(), "",
                                        placeArcListPreWeight.get(0).get(pozycja_a), TypeOfArc.NORMAL));
                                pozycja_a++;
                            }
                        }
                        pozycja_a = 0;
                        for (int k = 0; k < placeArcListPost.size(); k++) {
                            for (int j = 0; j < placeArcListPost.get(k).length; j++) {
                                int t2 = trans[Integer.parseInt(placeArcListPost.get(k)[j])][1];
                                arcArray.add(new Arc(nodeArray.get(k).getLastLocation(),
                                        nodeArray.get(t2 - 1).getLastLocation(), "",
                                        placeArcListPostWeight.get(0).get(pozycja_a), TypeOfArc.NORMAL));
                                pozycja_a++;

                            }
                        }

						/*
						int wid = Toolkit.getDefaultToolkit().getScreenSize().width - 20;
						int hei = Toolkit.getDefaultToolkit().getScreenSize().height - 20;
						int SIN = overlord.IDtoIndex(SID);
						int tmpX = 0;
						int tmpY = 0;
						boolean xFound = false;
						boolean yFound = false;
						GraphPanel graphPanel = overlord
								.getWorkspace().getSheets().get(SIN).getGraphPanel();
						for (int l = 0; l < elemArray.size(); l++) {
							if (elemArray.get(l).getPosition().x > wid) {
								tmpX = l;
								xFound = true;
								wid = elemArray.get(l).getPosition().x;
							}
							if (elemArray.get(l).getPosition().y > hei) {
								tmpY = l;
								yFound = true;
								hei = elemArray.get(l).getPosition().y;
							}
						}
						if (xFound && !yFound) {
							graphPanel.setSize(new Dimension(elemArray.get(tmpX)
									.getPosition().x + 90,graphPanel.getSize().height));
						}
						if (!xFound && yFound) {
							graphPanel.setSize(new Dimension(
									graphPanel.getSize().width, elemArray.get(tmpY).getPosition().y + 90));
						}
						if (xFound && yFound) {
							graphPanel.setSize(new Dimension(elemArray.get(tmpX)
									.getPosition().x + 90, elemArray.get(tmpY).getPosition().y + 90));
						}
						graphPanel.setOriginSize(graphPanel.getSize());
						*/
                        break;
                }
            }

            in.close();
            //overlord.log("Petri net from INA .pnt file successfully read.", "text", true);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            //JOptionPane.showMessageDialog(null,e.getMessage(),"ERROR: readPNT",JOptionPane.ERROR_MESSAGE);
            //overlord.log("Error: " + e.getMessage(), "error", true);
        }
        return new PetriNet(nodeArray, arcArray);
    }

    /**
     * Czyta plik sieci petriego w formacie PNT (INA)
     *
     * @param sciezka String - scieżka do pliku
     */
    public void readPNT(String sciezka) {
        try {
            resetComponents();
            int SID = overlord.getWorkspace().getProject().returnCleanSheetID();
            DataInputStream in = new DataInputStream(new FileInputStream(sciezka));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();
            String[] tabWczytanaLinia = readLine.split(":");
            netName = tabWczytanaLinia[1];
            int[][] trans = new int[MatSiz][2];
            int ID = 0;
            String[] wID = new String[MatSiz];
            int[] wMark = new int[MatSiz];
            ArrayList<Integer> wagiWej = new ArrayList<Integer>();
            ArrayList<Integer> wagiWyj = new ArrayList<Integer>();

            while ((readLine = buffer.readLine()) != null) {
                // Etap I

                // Wczytywanie informacji o Arcach i tokenach
                if (readLine.equals("@")) {
                    stage++;
                }
                switch (stage) {
                    case 1:
                        ArrayList<String> tmpStringWej = new ArrayList<String>();
                        ArrayList<String> tmpStringWyj = new ArrayList<String>();
                        readLine = readLine.replace(",", " , ");
                        readLine = readLine.replace(":", " : ");
                        String[] WczytanyString = readLine.split(" ");
                        int poz = 0;
                        int poZap = 0;
                        for (String s : WczytanyString) {
                            if (!s.isEmpty()) {
                                if (!Character.isWhitespace(s.charAt(0))) {
                                    if (s.contains(",")) {
                                        poZap = poz;
                                        poz = 5;
                                    }
                                    if (s.contains(":")) {
                                        poZap = poz;
                                        poz = 4;
                                    }

                                    switch (poz) {
                                        // numer miejsca
                                        case 0:
                                            wID[ID] = s;
                                            poz++;
                                            break;
                                        // ilosc tokenow
                                        case 1:
                                            wMark[ID] = Integer.parseInt(s);
                                            ID++;
                                            poz++;
                                            break;
                                        // wchodzace
                                        case 2:
                                            tmpStringWej.add(s);
                                            wagiWej.add(1);
                                            break;
                                        // wychodzace
                                        case 3:
                                            tmpStringWyj.add(s);
                                            wagiWyj.add(1);
                                            break;
                                        case 4:
                                            if (!s.contains(":")) {
                                                if (poZap == 2) {
                                                    wagiWej.remove(wagiWej.size() - 1);
                                                    wagiWej.add(Integer.parseInt(s));
                                                } else {
                                                    wagiWyj.remove(wagiWyj.size() - 1);
                                                    wagiWyj.add(Integer.parseInt(s));
                                                }
                                                poz = poZap;
                                            }
                                            break;
                                        case 5:
                                            poz = poZap;
                                            poz++;
                                            break;
                                    }
                                }
                            }
                        }
                        String[] a = new String[tmpStringWej.size()];
                        placeArcListPre.add(tmpStringWej.toArray(a));
                        placeArcListPreWeight.add(wagiWej);
                        a = new String[tmpStringWyj.size()];
                        placeArcListPost.add(tmpStringWyj.toArray(a));
                        placeArcListPostWeight.add(wagiWyj);

                        break;
                    case 2:
                        // Etap II
                        // Wczytywanie danych o miejscach

                        if ((readLine.contains("capacity") && readLine.contains("time") && readLine.contains("name")) || readLine.equals("@")) {

                        } else {
                            tabWczytanaLinia = readLine.split(": ");
                            //String[] tmp4 = tabWczytanaLinia[0].split(" ");
                            int placeNumber = globalPlaceNumber;
                            globalPlaceNumber++;
                            tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
                            String placeName = tabWczytanaLinia[0];
                            Place tmpPlace = new Place(placeNumber, new ArrayList<ElementLocation>(), placeName, "", wMark[placeNumber]);

                            ArrayList<ElementLocation> namesLoc = new ArrayList<ElementLocation>();
                            namesLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(namesLoc, GUIManager.locationMoveType.NAME);

                            //XTPN
                            ArrayList<ElementLocation> alphaLoc = new ArrayList<ElementLocation>();
                            alphaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);

                            ArrayList<ElementLocation> betaLoc = new ArrayList<ElementLocation>();
                            betaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);

                            ArrayList<ElementLocation> gammaLoc = new ArrayList<ElementLocation>();
                            gammaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);

                            ArrayList<ElementLocation> tauLoc = new ArrayList<ElementLocation>();
                            tauLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpPlace.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);

                            nodeArray.add(tmpPlace);
                        }
                        break;
                    case 3:
                        // Etap III
                        // Wczytywanie danych o tranzycjach
                        if ((readLine.contains("priority") && readLine.contains("time")
                                && readLine.contains("name")) || readLine.equals("@")) {
                            placeCount = globalPlaceNumber;
                        } else {
                            tabWczytanaLinia = readLine.split(": ");
                            String[] tmp5 = tabWczytanaLinia[0].split(" ");
                            for (String s : tmp5) {
                                if (!s.isEmpty()) {
                                    globalPlaceNumber++;
                                    trans[Integer.parseInt(s)][0] = Integer.parseInt(s);
                                    trans[Integer.parseInt(s)][1] = globalPlaceNumber;
                                }
                            }

                            int transNumber = globalPlaceNumber;
                            tabWczytanaLinia = tabWczytanaLinia[1].split(" ");
                            String transName = tabWczytanaLinia[0];
                            Transition tmpTrans = new Transition(transNumber, new ArrayList<ElementLocation>(), transName, "");

                            ArrayList<ElementLocation> namesLoc = new ArrayList<ElementLocation>();
                            namesLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(namesLoc, GUIManager.locationMoveType.NAME);

                            //XTPN
                            ArrayList<ElementLocation> alphaLoc = new ArrayList<ElementLocation>();
                            alphaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);

                            ArrayList<ElementLocation> betaLoc = new ArrayList<ElementLocation>();
                            betaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);

                            ArrayList<ElementLocation> gammaLoc = new ArrayList<ElementLocation>();
                            gammaLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);

                            ArrayList<ElementLocation> tauLoc = new ArrayList<ElementLocation>();
                            tauLoc.add(new ElementLocation(0, new Point(0, 0), null));
                            tmpTrans.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);
                            //mark++;

                            nodeArray.add(tmpTrans);
                        }
                        break;
                    case 4:
                        // Tworzenie Arców, szerokosci okna
                        // tworzenie dla kazdego noda element location
                        for (int j = 0; j < nodeArray.size(); j++) {
                            if (nodeArray.get(j).getType() == PetriNetElementType.PLACE) {
                                elemArray.add(new ElementLocation(SID, new Point(80, 30 + j * 60), nodeArray.get(j)));
                                ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
                                tempElementLocationArry.add(elemArray.get(j));
                                nodeArray.get(j).setElementLocations(tempElementLocationArry);
                            }

                            if (nodeArray.get(j).getType() == PetriNetElementType.TRANSITION) {
                                elemArray.add(new ElementLocation(SID, new Point(280, 30 + (j - placeCount) * 60), nodeArray.get(j)));
                                ArrayList<ElementLocation> tempElementLocationArry = new ArrayList<ElementLocation>();
                                tempElementLocationArry.add(elemArray.get(j));
                                nodeArray.get(j).setElementLocations(tempElementLocationArry);
                            }
                        }

                        int pozycja_a = 0;
                        // Arki
                        for (int k = 0; k < placeArcListPre.size(); k++) {
                            for (int j = 0; j < placeArcListPre.get(k).length; j++) {
                                int t1 = trans[Integer.parseInt(placeArcListPre.get(k)[j])][1];
                                arcArray.add(new Arc(nodeArray.get(t1 - 1).getLastLocation(),
                                        nodeArray.get(k).getLastLocation(), "",
                                        placeArcListPreWeight.get(0).get(pozycja_a), TypeOfArc.NORMAL));
                                pozycja_a++;
                            }
                        }
                        pozycja_a = 0;
                        for (int k = 0; k < placeArcListPost.size(); k++) {
                            for (int j = 0; j < placeArcListPost.get(k).length; j++) {
                                int t2 = trans[Integer.parseInt(placeArcListPost.get(k)[j])][1];
                                arcArray.add(new Arc(nodeArray.get(k).getLastLocation(),
                                        nodeArray.get(t2 - 1).getLastLocation(), "",
                                        placeArcListPostWeight.get(0).get(pozycja_a), TypeOfArc.NORMAL));
                                pozycja_a++;

                            }
                        }

                        int wid = Toolkit.getDefaultToolkit().getScreenSize().width - 20;
                        int hei = Toolkit.getDefaultToolkit().getScreenSize().height - 20;
                        int SIN = overlord.IDtoIndex(SID);
                        int tmpX = 0;
                        int tmpY = 0;
                        boolean xFound = false;
                        boolean yFound = false;
                        GraphPanel graphPanel = overlord
                                .getWorkspace().getSheets().get(SIN).getGraphPanel();
                        for (int l = 0; l < elemArray.size(); l++) {
                            if (elemArray.get(l).getPosition().x > wid) {
                                tmpX = l;
                                xFound = true;
                                wid = elemArray.get(l).getPosition().x;
                            }
                            if (elemArray.get(l).getPosition().y > hei) {
                                tmpY = l;
                                yFound = true;
                                hei = elemArray.get(l).getPosition().y;
                            }
                        }
                        if (xFound && !yFound) {
                            graphPanel.setSize(new Dimension(elemArray.get(tmpX)
                                    .getPosition().x + 90, graphPanel.getSize().height));
                        }
                        if (!xFound && yFound) {
                            graphPanel.setSize(new Dimension(
                                    graphPanel.getSize().width, elemArray.get(tmpY).getPosition().y + 90));
                        }
                        if (xFound && yFound) {
                            graphPanel.setSize(new Dimension(elemArray.get(tmpX)
                                    .getPosition().x + 90, elemArray.get(tmpY).getPosition().y + 90));
                        }
                        graphPanel.setOriginSize(graphPanel.getSize());
                        break;
                }
            }

            in.close();
            overlord.log("Petri net from INA .pnt file successfully read.", "text", true);
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: readPNT", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }

    }

    /**
     * Metoda służąca do zapisywaniu pliku sieci Petriego w formacie PNT (INA).
     *
     * @param path           - ścieżka zapisu pliku
     * @param placeList      ArrayList[Place] - lista miejsc sieci
     * @param transitionList ArrayList[Transition] - lista tranzycji sieci
     * @param arcList        ArrayList[Arc] - lista łuków sieci
     * @return boolean - status operacji: true jeśli nie było problemów
     */
    public boolean writePNT(String path, ArrayList<Place> placeList, ArrayList<Transition> transitionList, ArrayList<Arc> arcList) {
        StringBuilder fileBuffer = new StringBuilder("P   M   PRE,POST  NETZ 0:");
        try {
            PrintWriter writerObject = new PrintWriter(path);
            fileBuffer.append(getFileName(path));
            fileBuffer.append("\r\n");
            //int[] tabPlace = new int[placeList.size()];

            for (int i = 0; i < placeList.size(); i++) {
                if (i < 9) {
                    fileBuffer.append(" ");
                }
                if (i < 99) {
                    fileBuffer.append(" ");
                }
                fileBuffer.append(i);
                fileBuffer.append(" ");
                fileBuffer.append(placeList.get(i).getTokensNumber());
                fileBuffer.append("    ");

                // łuki
                if (placeList.get(i).getInArcs().isEmpty()
                        && placeList.get(i).getOutArcs().isEmpty()) {
                    fileBuffer.append(" ");
                }
                if (placeList.get(i).getInArcs().size() > 0 && placeList.get(i).getOutArcs().isEmpty()) {
                    for (int j = 0; j < placeList.get(i).getInArcs().size(); j++) {
                        if (transitionList.contains((Transition)placeList.get(i).getInArcs().get(j).getStartNode())) {
                            fileBuffer.append(" ");
                            fileBuffer.append(transitionList.indexOf((Transition)placeList.get(i).getInArcs().get(j).getStartNode()));
                            if (placeList.get(i).getInArcs().get(j).getWeight() > 1) {
                                fileBuffer.append(": ").append(placeList.get(i).getInArcs().get(j).getWeight());
                            }
                        }
                    }
                }
                if (placeList.get(i).getInArcs().size() > 0 && placeList.get(i).getOutArcs().size() > 0) {
                    for (int j = 0; j < placeList.get(i).getInArcs().size(); j++) {
                        if (transitionList.contains((Transition)placeList.get(i).getInArcs().get(j).getStartNode())) {
                            fileBuffer.append(" ");
                            fileBuffer.append(transitionList.indexOf((Transition)placeList.get(i).getInArcs().get(j).getStartNode()));
                            if (placeList.get(i).getInArcs().get(j).getWeight() > 1) {
                                fileBuffer.append(": ").append(placeList.get(i).getInArcs().get(j).getWeight());
                            }
                        }
                    }
                    fileBuffer.append(",");
                    for (int j = 0; j < placeList.get(i).getOutArcs().size(); j++) {
                        if (transitionList.contains((Transition)placeList.get(i).getOutArcs().get(j).getEndNode())) {
                            fileBuffer.append(" ");
                            fileBuffer.append(transitionList.indexOf((Transition)placeList.get(i).getOutArcs().get(j).getEndNode()));
                            if (placeList.get(i).getOutArcs().get(j).getWeight() > 1) {
                                fileBuffer.append(": ").append(placeList.get(i).getOutArcs().get(j).getWeight());
                            }
                        }
                    }
                }
                if (placeList.get(i).getInArcs().isEmpty() && placeList.get(i).getOutArcs().size() > 0) {
                    fileBuffer.append(",");
                    for (int j = 0; j < placeList.get(i).getOutArcs().size(); j++) {
                        if(transitionList.contains((Transition)placeList.get(i).getOutArcs().get(j).getEndNode())) {
                            fileBuffer.append(" ");
                            fileBuffer.append(transitionList.indexOf((Transition)placeList.get(i).getOutArcs().get(j).getEndNode()));
                            if (placeList.get(i).getOutArcs().get(j).getWeight() > 1) {
                                fileBuffer.append(": ").append(placeList.get(i).getOutArcs().get(j).getWeight());
                            }
                        }
                    }
                }
                fileBuffer.append("\r\n");
            }
            fileBuffer.append("@\r\n");
            fileBuffer.append("place nr.             name capacity time\r\n");

            for (int i = 0; i < placeList.size(); i++) {
                fileBuffer.append("     ");
                if (i < 9) {
                    fileBuffer.append(" ");
                }
                if (i < 99) {
                    fileBuffer.append(" ");
                }
                fileBuffer.append(i);
                fileBuffer.append(": ");
                fileBuffer.append(placeList.get(i).getName()).append("                  ");
                fileBuffer.append("65535    0");
                fileBuffer.append("\r\n");
            }

            fileBuffer.append("@\r\n");
            fileBuffer.append("trans nr.             name priority time\r\n");
            for (int i = 0; i < transitionList.size(); i++) {
                fileBuffer.append("     ");
                if (i <= 9) {
                    fileBuffer.append(" ");
                }
                if (i <= 99) {
                    fileBuffer.append(" ");
                }
                fileBuffer.append(i);
                fileBuffer.append(": ");
				/*
				if (transitionList.get(i).getName().length() > 16) {
					tmpNazwy = transitionList.get(i).getName();
					
					tmpNazwy = tmpNazwy.substring(0, 16);
					
				} else {
					tmpNazwy = transitionList.get(i).getName();
					wielkoscNazwy = tmpNazwy.length();
					for (int k = wielkoscNazwy; k < 16; k++) {
						przerwa += " ";
					}
				}*/

                fileBuffer.append(transitionList.get(i).getName()).append("                      ");
                fileBuffer.append("0    0");
                fileBuffer.append("\r\n");
            }

            fileBuffer.append("@");
            writerObject.println(fileBuffer);
            writerObject.close();
            //overlord.log("Petri net exported as .pnt INA format. File: "+path, "text", true);
            //overlord.markNetSaved();
            return true;
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writePNT", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
            return false;
        }
    }

    /**
     * Metoda zapisująca t-inwarianty do pliku w formacie programu Charlie.
     *
     * @param path        String - ścieżka do pliku
     * @param invariants  ArrayList[ArrayList[Integer]] - macierz t-inwariantów
     * @param transitions ArrayList[Transition] - wektor tranzycji
     */
    public void writeT_invCharlie(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Transition> transitions) {
        try {
            String extension = "";
            if (!path.contains(".inv"))
                extension = ".inv";
            PrintWriter pw = new PrintWriter(path + extension);
            pw.print("minimal semipositive transition invariants=");

            for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
                //pw.print(i+1);
                boolean nrPlaced = false;
                for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
                    int value = invariants.get(i).get(t);
                    if (value == 0) {
                        continue;
                    }

                    if (!nrPlaced) {
                        pw.print("\r\n" + (i + 1) + "\t|\t");
                        nrPlaced = true;
                    } else {
                        pw.print(",\r\n");
                        pw.print("\t|\t");
                    }
                    String name = transitions.get(t).getName();
                    pw.print(t + "." + name + "\t\t:" + value);
                }
            }
            pw.close();
            overlord.log("Invariants in Charlie file format saved to " + path, "text", true);
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writeP_invCharlie", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }
    }

    /**
     * Metoda zapisująca p-inwarianty do pliku w formacie programu Charlie.
     *
     * @param path       String - ścieżka do pliku
     * @param invariants ArrayList[ArrayList[Integer]] - macierz p-inwariantów
     * @param places     ArrayList[Place] - wektor miejsc
     */
    public void writeP_invCharlie(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Place> places) {
        try {
            String extension = "";
            if (!path.contains(".inv"))
                extension = ".inv";
            PrintWriter pw = new PrintWriter(path + extension);
            pw.print("minimal semipositive place invariants=");

            for (int i = 0; i < invariants.size(); i++) { //po wszystkich p-inwariantach
                boolean nrPlaced = false;
                for (int p = 0; p < invariants.get(i).size(); p++) { //po wszystkich miejscach
                    int value = invariants.get(i).get(p);
                    if (value == 0) {
                        continue;
                    }

                    if (!nrPlaced) {
                        pw.print("\r\n" + (i + 1) + "\t|\t");
                        nrPlaced = true;
                    } else {
                        pw.print(",\r\n");
                        pw.print("\t|\t");
                    }
                    String name = places.get(p).getName();
                    pw.print(p + "." + name + "\t\t:" + value);
                }
            }
            pw.print("\r\n");
            pw.close();
            overlord.log("P-invariants in Charlie file format saved to " + path, "text", true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writeP_invCharlie", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }
    }

    /**
     * Metoda zapisująca t-inwarianty w formacie CSV (Comma Separated Value).
     *
     * @param path        String - ścieżka do pliku zapisu
     * @param invariants  ArrayList[ArrayList[Integer]] - macierz t-inwariantów
     * @param transitions ArrayList[Transition] - wektor tranzycji
     */
    public void writeT_invCSV(String path, ArrayList<ArrayList<Integer>> invariants, ArrayList<Transition> transitions) {
        try {
            String extension = "";
            if (!path.contains(".csv"))
                extension = ".csv";
            PrintWriter pw = new PrintWriter(path + extension);
            //pw.print(";");
            for (int i = 0; i < transitions.size(); i++) {
                pw.print(";" + i + "." + transitions.get(i).getName());
            }
            pw.print("\r\n");

            //TODO:

            boolean crazyMode = false;
            if (overlord.getSettingsManager().getValue("analysisBinaryCSVInvariants").equals("1")) {
                crazyMode = true;
            }

            if (crazyMode) {
                for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
                    pw.print(i + 1);
                    for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
                        int value = invariants.get(i).get(t);
                        if (value > 0)
                            value = 1;
                        pw.print(";" + value);
                    }
                    pw.print("\r\n");
                }
            } else {
                for (int i = 0; i < invariants.size(); i++) { //po wszystkich inwariantach
                    pw.print(i + 1);
                    for (int t = 0; t < invariants.get(i).size(); t++) { //po wszystkich tranzycjach
                        int value = invariants.get(i).get(t);
                        pw.print(";" + value);
                    }
                    pw.print("\r\n");
                }
            }
            pw.close();
            overlord.log("Invariants saved as CSV file " + path, "text", true);
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writeT_invCSV", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }
    }

    /**
     * Metoda zapisująca p-inwarianty w formacie CSV (Comma Separated Value).
     *
     * @param path         String - ścieżka do pliku zapisu
     * @param p_invariants ArrayList[ArrayList[Integer]] - macierz t-inwariantów
     * @param places       ArrayList[Place] - wektor miejsc
     */
    public void writeP_invCSV(String path, ArrayList<ArrayList<Integer>> p_invariants, ArrayList<Place> places) {
        try {
            String extension = "";
            if (!path.contains(".csv"))
                extension = ".csv";
            PrintWriter pw = new PrintWriter(path + extension);
            for (int i = 0; i < places.size(); i++) {
                pw.print(";" + i + "." + places.get(i).getName());
            }
            pw.print("\r\n");

            for (int i = 0; i < p_invariants.size(); i++) { //po wszystkich p-inwariantach
                pw.print(i + 1);
                for (int p = 0; p < p_invariants.get(i).size(); p++) { //po wszystkich miejscach
                    int value = p_invariants.get(i).get(p);
                    pw.print(";" + value);
                }
                pw.print("\r\n");
            }

            pw.close();
            overlord.log("P-invariants saved as CSV file " + path, "text", true);
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR: writeP_invCSV", JOptionPane.ERROR_MESSAGE);
            overlord.log("Error: " + e.getMessage(), "error", true);
        }
    }

    public void exportSubnet(ArrayList<ElementLocation> listOfElements) {
        ArrayList<Node> listOfParentNodes = new ArrayList<>();

        int min_x = Integer.MAX_VALUE;
        int min_y = Integer.MAX_VALUE;

        for (ElementLocation el : listOfElements) {
            if (!listOfParentNodes.contains(el.getParentNode())) {
                listOfParentNodes.add(el.getParentNode());
            }

            if (el.getPosition().y < min_y) {
                min_y = el.getPosition().y;
            }
            if (el.getPosition().x < min_x) {
                min_x = el.getPosition().x;
            }
        }

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.Export, null, null, listOfParentNodes, null, null);

        try {
            FileDialog fDialog = new FileDialog(new JFrame(), "Save", FileDialog.SAVE);
            fDialog.setVisible(true);
            String path = fDialog.getDirectory() + fDialog.getFile();

            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(sn);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }

    }

    public void exportBranchVertices(ArrayList<HolmesBranchVerticesPrototype.BranchStructure> bsl) {
        try {
            FileDialog fDialog = new FileDialog(new JFrame(), "Save", FileDialog.SAVE);
            fDialog.setVisible(true);
            String path = fDialog.getDirectory() + fDialog.getFile();

            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            //HolmesBranchVerticesPrototype.BranchStructureList b = new HolmesBranchVerticesPrototype.BranchStructureList(bsl);
            oos.writeObject(bsl);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
    }

    public ArrayList<HolmesBranchVerticesPrototype.BranchStructure> importBranchVertices(String absolutePath) {
        ArrayList<HolmesBranchVerticesPrototype.BranchStructure> sn = new ArrayList<>();
        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(absolutePath);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            HolmesBranchVerticesPrototype.BranchStructureList b;
            //b = (HolmesBranchVerticesPrototype.BranchStructureList) in.readObject();

            sn = (ArrayList<HolmesBranchVerticesPrototype.BranchStructure>) in.readObject();// b.bsl;

            //[MR] poniższe było unused:
            //ArrayList<StatePlacesVector>  spv = GUIManager.getDefaultGUIManager().getWorkspace().getProject().accessStatesManager().accessStateMatrix();

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sn;
    }

    public void exportSubnet(ArrayList<ElementLocation> listOfElements, ArrayList<Arc> listOfProperArcs) {
        ArrayList<Node> listOfParentNodes = new ArrayList<>();

        int min_x = Integer.MAX_VALUE;
        int min_y = Integer.MAX_VALUE;

        for (ElementLocation el : listOfElements) {
            if (!listOfParentNodes.contains(el.getParentNode())) {
                listOfParentNodes.add(el.getParentNode());
            }

            if (el.getPosition().y < min_y) {
                min_y = el.getPosition().y;
            }
            if (el.getPosition().x < min_x) {
                min_x = el.getPosition().x;
            }
        }


        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.Export, null, null, listOfParentNodes, null, null);
        sn.setSubArcs(listOfProperArcs);
        try {
            FileDialog fDialog = new FileDialog(new JFrame(), "Save", FileDialog.SAVE);
            fDialog.setVisible(true);
            String path = fDialog.getDirectory() + fDialog.getFile();

            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(sn);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
    }

    public void importSubnetFromFile(String absolutePath, int x, int y) {
        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(absolutePath);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            SubnetCalculator.SubNet sn = (SubnetCalculator.SubNet) in.readObject();

            for (Place p : sn.getSubPlaces()) {
                p.setTokensNumber(0);
            }

            for (Node n : sn.getSubNode()) {
                for (ElementLocation el : n.getElementLocations()) {
                    el.getPosition().x += x;
                    el.getPosition().y += y;
                    el.setSelected(false);
                }
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().add(n);

                if (n.getType() == PetriNetElementType.PLACE) {
                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().accessStatesManager().addPlace((Place)n);
                }
                for (Place p : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces()) {
                    p.setTokensNumber(0);
                }
            }
            for (Arc n : sn.getSubArcs()) {
                n.setSelected(false);
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().add(n);
            }

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
