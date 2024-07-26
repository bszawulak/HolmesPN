package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;

import java.util.*;

public class HolmesSimXTPNActions {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    
    /**
     * Konstruktor klasy HolmesStateSimulatorActions.
     */
    public HolmesSimXTPNActions() {

    }

    /**
     * Metoda ta dostaje pełną nazwę dla wierzchołka z comboBoxa, następnie zwraca prawdziwy
     * ID tego wierzchołka w bazie tychże.
     * @param name String - preformatowana nazwa wierzchołka, zaczynająca się od p/t[ID].[nazwa]
     * @return int - ID tranzycji
     */
    protected int getRealNodeID(String name) {
        name = name.substring(1, name.indexOf("."));
        int result;
        try {
            result = Integer.parseInt(name);
        } catch (Exception e) {
            overlord.log("System malfunction: unable to extract transition ID", "error", true);
            return -1;
        }
        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> crunchifySortMapXTPN(final Map<K, V> mapToSort) {
        List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(mapToSort.size());
        entries.addAll(mapToSort.entrySet());
        entries.sort(new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(final Map.Entry<K, V> entry1, final Map.Entry<K, V> entry2) {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });

        Map<K, V> sortedCrunchifyMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : entries) {
            sortedCrunchifyMap.put(entry.getKey(), entry.getValue());
        }
        return sortedCrunchifyMap;
    }
}
