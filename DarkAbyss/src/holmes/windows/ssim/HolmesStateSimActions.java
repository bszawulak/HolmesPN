package holmes.windows.ssim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import holmes.darkgui.GUIManager;

/**
 * Metoda pomocnicze klasy HolmesStateSimulator.
 * 
 * @author MR
 *
 */
public class HolmesStateSimActions {
	
	/**
	 * Konstruktor klasy HolmesStateSimulatorActions.
	 */
	public HolmesStateSimActions() {
		
	}
	
	/**
	 * Metoda ta dostaje pełną nazwę dla wierzchołka z comboBoxa, następnie zwraca prawdziwy
	 * ID tego wierzchołka w bazie tychże.
	 * @param string String - preformatowana nazwa wierzchołka, zaczynająca się od p/t[ID].[nazwa]
	 * @return int - ID tranzycji
	 */
	protected int getRealNodeID(String name) {
		name = name.substring(1, name.indexOf("."));
		int result = -1;
		try {
			result = Integer.parseInt(name);
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("System malfunction: unable to extract transition ID", "error", true);
			return -1;
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> crunchifySortMap(final Map<K, V> mapToSort) {
		List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(mapToSort.size());
		entries.addAll(mapToSort.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
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
