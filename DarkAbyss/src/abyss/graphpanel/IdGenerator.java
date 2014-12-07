package abyss.graphpanel;

/**
 * Generator identyfikatorów dla wierzcho³ków
 * @author students
 *
 */
public class IdGenerator {
	
	private static int lastId = 0;
	private static int lastPlaceId = 0;
	private static int lastTransitionId = 0;
	
	/**
	 * Metoda zwraca nowy wolny identyfikator.
	 * @return int - wartoœæ ID
	 */
	public static int getNextId()
	{
		return lastId++;
	}
	
	/**
	 * Metoda ustawia pocz¹tkow¹ wartoœæ identyfikatora.
	 * @param id int - nowa wartoœæ startowa
	 */
	public static void setStartId(int id)
	{
		lastId = id;
	}

	/**
	 * Metoda zwraca ostatni¹ wartoœæ identyfikatora dla miejsca.
	 * @return int - identyfikator miejsca
	 */
	public static int getNextPlaceId() 
	{
		return lastPlaceId++;
	}

	/**
	 * Metoda ustawiaj¹ca ostatni¹ wartoœæ identyfikatora dla miejsca.
	 * @param lastPlaceId int - nowy ostatni identyfikator miejsca
	 */
	public static void setPlaceId(int lastPlaceId) 
	{
		IdGenerator.lastPlaceId = lastPlaceId;
	}

	/**
	 * Metoda zwraca ostatni¹ wartoœæ identyfikatora dla tranzycji.
	 * @return int - identyfikator tranzycji
	 */
	public static int getNextTransitionId() 
	{
		return lastTransitionId++;
	}

	/**
	 * Metoda ustawiaj¹ca ostatni¹ wartoœæ identyfikatora dla tranzycji.
	 * @param lastTransitionId int - identyfikator tranzycji
	 */
	public static void setTransitionId(int lastTransitionId) 
	{
		IdGenerator.lastTransitionId = lastTransitionId;
	}
	
}
