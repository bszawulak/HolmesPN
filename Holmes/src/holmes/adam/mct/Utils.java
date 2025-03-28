package holmes.adam.mct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Utils {
	public static class HybridStringNumberComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			try {
				Integer n1 = Integer.parseInt(o1);
				Integer n2 = Integer.parseInt(o2);
				return n1.compareTo(n2);
			} catch (NumberFormatException nfe) {
				return o1.compareTo(o2);
			}
		}
		
	}
	
	public static SortedSet<MCTSet> buildMCT(Set<TInvariant> tiSet, Map<String, String> renameMap)
	{
		Set<MCTTransition> all = new HashSet<MCTTransition>();
		SortedSet<MCTSet> result = new TreeSet<MCTSet>();
		
		for (TInvariant tinv : tiSet)
		{
			all.addAll(tinv.getTransitionMap().keySet());
		}
		
		TreeMap<MCTTransition, Set<TInvariant>> contained = new TreeMap<MCTTransition, Set<TInvariant>> ();

		for (MCTTransition t : all)
		{
			Set<TInvariant> supportSet = new HashSet<TInvariant>();
			contained.put(t, supportSet);
			for (TInvariant tinv : tiSet)
			{
				if (tinv.getTransitionMap().containsKey(t))
					supportSet.add(tinv);
			}
		}
		
		while (contained.size() > 0)
		{
			MCTSet mct = new MCTSet();
			MCTTransition transition = contained.firstKey();
			mct.getTransitionSet().add(transition);
			
			Set<TInvariant> supportSet = contained.get(transition);
			
			contained.remove(transition);
			
			for (Map.Entry<MCTTransition,Set<TInvariant>>  tnext : contained.entrySet())
			{
				//if (tnext.getKey().equals(transition))
				//	continue;
				
				Set<TInvariant> supportSeti = tnext.getValue();
				if (supportSet.equals(supportSeti))
				{
					mct.getTransitionSet().add(tnext.getKey());
				}
				
			}
			
			for (MCTTransition inMCT : mct.getTransitionSet())
				contained.remove(inMCT);
			
			result.add(mct);
			
		}
		
		if (renameMap != null) {
			MCTSet.renameMctSets(result, renameMap);
		}
		
		result = MCTSet.rebuildMctOrder(result, MCTSet.COMP_BY_SIZE_DESCENDING);
		
		for (MCTSet mct : result) {
			for (MCTTransition t : mct.getTransitionSet()) {
				t.setMCTSet(mct);
			}
		}
		
		return result;
	}
	
	/**
	 * Do dorobienia opis formatu pliku
	 * @param is (<b>InputStream</b>)
	 * @param firstColumnTID (<b>boolean</b>)
	 * @param petriNet (<b>MCTPetriNet</b>)
	 * @return  (<b>SortedSet[TInvariant]</b>)
	 * @throws IOException ex
	 */
	public static SortedSet<TInvariant> readTInvariants(InputStream is, boolean firstColumnTID, MCTPetriNet petriNet) throws IOException
	{
		SortedSet<TInvariant> result = new TreeSet<TInvariant>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		Map<String, MCTTransition> transitions =  new HashMap<String, MCTTransition>();
		int minWidth = firstColumnTID?2:1;
		int rcount = 0;
		
		while ((line = br.readLine()) != null)
		{
			String[] items = line.split("[, \\t;]+");
			if (items.length < minWidth)
				continue;
			rcount++;
			TInvariant tInv = new TInvariant(firstColumnTID?items[0]:String.valueOf(rcount), petriNet);
			for (int i = minWidth - 1; i < items.length; i++)
			{
				
				int colon = items[i].indexOf(':');
				int count = 1;
				String tId = colon < 0 ? items[i] : items[i].substring(0, colon);
				String tCount = colon < 0 ? "1" : items[i].substring(colon + 1);
				try
				{
					count = Integer.parseInt(tCount);
				}catch (Exception nfe)
				{
					//
				}
				MCTTransition t = transitions.get(tId);
				if (t == null) {
					t = new MCTTransition(tId, petriNet);
					transitions.put(tId, t);
				}
				
				tInv.getTransitionMap().put(t, count);
			}
			result.add(tInv);
		}
		is.close();
		return result;
	}
	
	/**
	 * Format pliku:<br/>
	 * <pre>
	 * ;nazwa_tranzycji_1;nazwa_tranzycji_2;..;nazwa_tranzycji_n
	 * nazwa_t_inwariantu_1;liczno��_tranzycji_1;liczno��_tranzycji_2;...;liczno��_tranzycji_n
	 * nazwa_t_inwariantu_2;liczno��_tranzycji_1;liczno��_tranzycji_2;...;liczno��_tranzycji_n
	 * .......................................................................................
	 * nazwa_t_inwariantu_m;liczno��_tranzycji_1;liczno��_tranzycji_2;...;liczno��_tranzycji_n
	 * </pre>
	 * np.<br/>
	 * <br/>
	 * <pre>
	 *    ;t1;t2;t3;t4;t5;t6
	 * i1.; 0; 0; 1; 1; 2; 1
	 * i2.; 1; 1; 1; 0; 2; 1
	 * i3.; 1; 1; 1; 0; 0; 0
	 * i4.; 0; 0; 1; 1; 0; 0
	 * </pre>
	 * @param is (<b>InputStream</b>)
	 * @param petriNet (<b>MCTPetriNet</b>)
	 * @return (<b>SortedSet[TInvariant]</b>)
	 * @throws IOException (<b>ex</b>)
	 */
	public static SortedSet<TInvariant> readFromCSV(InputStream is, MCTPetriNet petriNet) throws IOException
	{
		SortedSet<TInvariant> result = new TreeSet<TInvariant>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		
		line = br.readLine();
		String[] transitionIDs = line.split("[, \\t;]+");
		int vectorLength = transitionIDs.length;
		for (int i = 1; i < transitionIDs.length; i++)
		{
			petriNet.getOrCreateTransition(transitionIDs[i]);
		}
		while ((line = br.readLine()) != null)
		{
			String[] items = line.split("[, \\t;]+");
			if (items.length < vectorLength)
				continue;

			TInvariant tInv = new TInvariant(items[0], petriNet);
			for (int i = 1; i < vectorLength; i++)
			{
				int count = 0;
				String tCount = items[i];
				try
				{
					count = Integer.parseInt(tCount);
				}catch (Exception nfe)
				{
					//
				}
				MCTTransition t = petriNet.getOrCreateTransition(transitionIDs[i]);
				if (count > 0)
					tInv.getTransitionMap().put(t, count);
			}
			result.add(tInv);
		}
		is.close();
		return result;
	}
	
	/**
	 * Reader kt�ry uwzgl�dnia przypadek, gdy wszystkie tranzycje s� w jednej linii
	 * (usuni�to \n przy obr�bce w excelu np. w celu posortowania inwariant�w po klastrach).
	 * Odczytane linie s� przyci�te trim'em
	 */
	static class LineReader
	{
		private String[] lines;
		private int index;
		private final BufferedReader br;
		
		public LineReader(BufferedReader br) {
			super();
			this.br = br;
		}
		
		public String readLine() throws IOException
		{
			String brLine;
			if (lines == null)
			{
				brLine = br.readLine();
				if (brLine == null)
					return null;
				
				brLine = brLine.trim();

				if (brLine.indexOf(',') == brLine.length() - 1)
				{
					return brLine;
				} else {
					lines = brLine.replaceAll(",", ",\n").split("\n[\\t ]*");
					index = 0;
				}
			}
			
			String result = lines[index++];
			
			if (index == lines.length)
			{
				lines = null;
			}
			
			return result;
			
		}
	}

	/**
	 * Format pliku:
	 * <pre>
	 * linia nag�wka
	 * nr_inwariantu | id_tranzycji  : liczno��,
	 *               | id_tranzycji  : liczno��,
	 *               | id_tranzycji  : liczno��
	 * nr_inwariantu | id_tranzycji  : liczno��,
	 *               | id_tranzycji  : liczno��,
	 *               | id_tranzycji  : liczno��
	 * 
	 * np.
	 * 
	 * minimal semipositive transition invariants=
	 * 1	|	22.iron_ions_Fe3_synth		:1,
	 *  	|	23.iron_ions_Fe2_synth		:1
	 * 2	|	36.L_arginine_lowering		:1,
	 *  	|	38.citruline_increasing		:1
	 *  
	 *  lub w wariancie z wszystkimi tranzycjami w jednej linii:
	 *  
	 *  minimal semipositive transition invariants=
	 * 1	|	22.iron_ions_Fe3_synth		:1,	|	23.iron_ions_Fe2_synth		:1
	 * 2	|	36.L_arginine_lowering		:1,	|	38.citruline_increasing		:1
	 *  
	 *  </pre>
	 * @param is (<b>InputStream</b>)
	 * @param petriNet (<b>MCTPetriNet</b>)
	 * @return (<b>SortedSet[TInvariant]</b>)
	 * @throws IOException (<b>ex</b>)
	 */
	public static SortedSet<TInvariant> readFromCharlie(InputStream is, MCTPetriNet petriNet) throws IOException {
		SortedSet<TInvariant> result = new TreeSet<TInvariant>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		//int rcount = 0;
		br.readLine();
		TInvariant tInv = null;
		LineReader lr = new LineReader(br);
		while ((line = lr.readLine()) != null)
		{
			String[] items = line.split("\\|");
			if (items.length != 2)
				continue;
			
			for (int i = 0; i < items.length; i++)
			{
				items[i]=items[i].trim();
			}
			if (items[0].length() > 0) {
				//pojawi� si� nowy t-inwariant wi�c dotychczasowy l�duje w wynikach
				if (validateTInvariant(tInv)) 
				{
					result.add(tInv);
				}
				tInv = new TInvariant(items[0], petriNet);
			}
			if (tInv != null)
			{
				int colon = items[1].indexOf(':');
				int count = 1;
				String tId = (colon < 0 ? items[1] : items[1].substring(0, colon)).trim();
				tId = tId.replaceFirst("", "");
				String tCount = (colon < 0 ? "1" : items[1].substring(colon + 1)).trim().replaceAll(",","");
				try
				{
					count = Integer.parseInt(tCount);
				}catch (Exception nfe)
				{
					//
				}
				//if (count > 1) 
				//	System.out.println();
				MCTTransition t = petriNet.getOrCreateTransition(tId);
				
				tInv.getTransitionMap().put(t, count);
			}
		}
		
		//dodanie ostatniego t-inwariantu do wynik�w
		if (validateTInvariant(tInv)) 
		{
			result.add(tInv);
		}
		is.close();
		return result;
		
	}
	
	private static boolean validateTInvariant(TInvariant tInv) {
		if (tInv == null) {
			return false;
		}
		
		//czy zawiera tylko nieujemne rozwi�zania (Charlie czasem zwraca wyniki razem z ujemnymi)
		for (MCTTransition t : tInv.getTransitionMap().keySet()) {
			if (tInv.getTransitionMap().get(t) < 0) {
				return false;
			}
		}
		return true;
	}

	public static class TInvariantTrasitionDescriptor implements Comparable<TInvariantTrasitionDescriptor>
	{
		public TInvariant tinv;
		public List<MCTTransition> transitions = new ArrayList<MCTTransition>();
		
		@Override
		public int compareTo(TInvariantTrasitionDescriptor o) {
			return tinv.compareTo(o.tinv);
		}
	}
	
	/**
	 * Pobiera zbiór inwariantów, które zawierają/nie zawierają podanych tranzycji
	 * @param tinvs - zbiór t-inwariantów
	 * @param tids - tablica id tranzycji
	 * @param containing - jeśli <code>true</code>, to zwracane są t-inwarianty zawierające przynajmniej jedną tranzycję,
	 * 	 jeśli <code>false</code>, zwracane są t-inwarianty nie zawierające żadnej z tranzycji
	 * @return (<b>SortedSet[TInvariantTrasitionDescriptor]</b>)
	 */
	public static SortedSet<TInvariantTrasitionDescriptor> getInvariantsWith(SortedSet<TInvariant> tinvs, String[] tids, boolean containing)
	{
		SortedSet<TInvariantTrasitionDescriptor> result = new TreeSet<TInvariantTrasitionDescriptor>();
		for (TInvariant tinv : tinvs)
		{
			TInvariantTrasitionDescriptor titd = new TInvariantTrasitionDescriptor();
			titd.tinv = tinv;
			for (String id : tids)
			{
				MCTTransition t = tinv.petriNet.getTransition(id);
				if (tinv.contains(t))
				{
					titd.transitions.add(t);
				}
			}
			if ((titd.transitions.size() > 0 && containing)
					|| 
					(titd.transitions.size() == 0 && !containing))
			{
				result.add(titd);					
			}
			
		}
		return result;
	}

	public static String invariantsToString(SortedSet<TInvariant> tinv)
	{
		StringBuilder sb = new StringBuilder();
		for (TInvariant ti : tinv)
		{
			sb.append(ti.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}
	
	public static String invariantsToCsvVectors(SortedSet<TInvariant> tinv, boolean boolValues, boolean includeId, boolean latexMode) {
		/*
		StringBuffer sb = new StringBuffer();
		for (TInvariant ti : tinv)
		{
			if (includeId)
			{
				sb.append("'");
				sb.append(ti.id);
				sb.append("';");
			}
			
			sb.append(ti.toVector(boolValues));
			sb.append("\r\n");
		}
		return sb.toString();
		*/
		return invariantsWithMCT(tinv, null, includeId, true, boolValues, latexMode, null);
	}
	
	/**
	 * @param tinvs zbi�r t-inwariant�w
	 * @param mctSets zbi�r zbi�r�w MCT
	 * @param includeId drukuj z id
	 * @param includeVectors drukuj jako wektory 
	 * @param boolValues drukuj jako warto�ci boolowskie (supporty)
	 * @param latexMode tryb latex
	 * @param clusters opcjonalna lista klastr�w do drukowania w trybie latex (po klastrze wstawia now� lini� i na pierwszym numer klastra)
	 * @return (<b>String</b>)
	 */
	public static String invariantsWithMCT(SortedSet<TInvariant> tinvs,
			SortedSet<MCTSet> mctSets, boolean includeId, boolean includeVectors, boolean boolValues, boolean latexMode, List<SortedSet<String>> clusters) {
		StringBuilder sb = new StringBuilder();
		String separator = latexMode ? "\t\t&\t\t" : ";";
		
		Map<String, Integer> firstInCluster = new HashMap<String, Integer>();
		Map<String, Integer> lastInCluster = new HashMap<String, Integer>();
		
		if (clusters != null) {
			int ccnt = 1;
			for (SortedSet<String> cluster : clusters) {
				firstInCluster.put(cluster.first(), ccnt);
				lastInCluster.put(cluster.last(), ccnt);
				ccnt++;
			}
		}

		for (TInvariant tinv : tinvs)
		{
			if (latexMode && mctSets != null) {
				if (firstInCluster.containsKey(tinv.id)) {
					sb.append("$c_{").append(firstInCluster.get(tinv.id)).append("}$");
				} else {
					sb.append("\t");
				}
				sb.append("\t&\t\t"); //pierwsza kolumna - klastry
			}
			if (includeId)
			{
				sb.append((latexMode ? "$x_{" : ""));
				sb.append(tinv.id);
				sb.append((latexMode ? "}$" : "."));
				sb.append(separator);
			}
			if (includeVectors)
			{
				sb.append(tinv.toVector(boolValues));
				if (mctSets != null)
					sb.append(separator);
			}
			if (mctSets != null) {
				sb.append(tinv.toMCTString(mctSets, latexMode));
				if (lastInCluster.containsKey(tinv.id)) {
					sb.append("\t\\\\ \\cline{1-4} \r\n\r\n");
				} else {
					sb.append("\t\\\\ \\cline{2-4}");
				}
			}
			sb.append("\r\n");
		}
		return sb.toString();
	}

	/**
	 * Metoda zczytuje nowe mapowanie nazw zbior�w MCT z pliku .properties
	 * @param file - plik z mapowaniem nazw
	 * @return - mapa tranzycja - nazwa (tj. je�li zbi�r MCT zawiera tak� tranzycj�, to nadana mu b�dzie taka nazwa)
	 * @throws FileNotFoundException ex1
	 * @throws IOException ex2
	 */
	public static Map<String, String> readMCTNameMap(String file) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new InputStreamReader(new FileInputStream(file)));
		
		Map<String, String> renameMap = new HashMap<String, String>();
		for (Object key : props.keySet()) {
			renameMap.put(String.valueOf(key), (String) props.get(key));
		}
		return renameMap;
	}

	/**
	 * Metoda zlicza wyst�pienia tranzycji w klastrze
	 * @param clusters - lista klastr�w (posortowanych zbior�w zawieraj�cych list� identyfikator�w t-inwariant�w)
	 * @param tInvariants - zbi�r t-inwariant�w
	 * @return - lista o rozmiarze clusters.size() zawieraj�ca map� zlicze� tranzycji w danym klastrze tj.
	 * 	pierwszy element tej listy odpowiada pierwszemu klastrowi i zawiera map� tranzycja-zliczenie jej wyst�pie�
	 *  w tym klastrze; zliczenie to naturalnie nie mo�e by� wi�ksze ni� rozmiar klastra
	 */
	public static List<SortedMap<MCTTransition, Integer>> computeTransitionCoverance(
			List<SortedSet<String>> clusters, SortedSet<TInvariant> tInvariants) {
		
		List<SortedMap<MCTTransition, Integer>> result = new ArrayList<SortedMap<MCTTransition, Integer>>(clusters.size());
		MCTPetriNet pn = tInvariants.first().petriNet;
		
		Map<String, TInvariant> tInvMap = new HashMap<String, TInvariant>();
		for (TInvariant tinv : tInvariants) {
			tInvMap.put(tinv.id, tinv);
		}
		
		for (SortedSet<String> cluster : clusters) {
			SortedMap<MCTTransition, Integer> transitionCountInCluster = new TreeMap<MCTTransition, Integer>();
			for (MCTTransition t : pn.getTransitions()) {
				transitionCountInCluster.put(t, 0);
			}
			for (String tInvariantId : cluster) {
				TInvariant tInvariant = tInvMap.get(tInvariantId);
				
				if (tInvariant == null) {
					continue;
				}
				
				for (MCTTransition t : tInvariant.getTransitionMap().keySet()) {
					transitionCountInCluster.put(t, transitionCountInCluster.get(t) + 1);
				}
			}
			
			result.add(transitionCountInCluster);
		}
		return result;
	}
	
}
