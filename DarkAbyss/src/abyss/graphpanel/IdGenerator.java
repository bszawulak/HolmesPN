package abyss.graphpanel;

/**
 * Generator identyfikator�w dla wierzcho�k�w
 * @author students
 *
 */
public class IdGenerator {
	
	private static int lastId = 0;
	private static int lastPlaceId = 0;
	private static int lastTransitionId = 0;
	
	/**
	 * Metoda zwraca nowy wolny identyfikator.
	 * @return int - warto�� ID
	 */
	public static int getNextId()
	{
		return lastId++;
	}
	
	/**
	 * Metoda ustawia pocz�tkow� warto�� identyfikatora.
	 * @param id int - nowa warto�� startowa
	 */
	public static void setStartId(int id)
	{
		lastId = id;
	}

	/**
	 * Metoda zwraca ostatni� warto�� identyfikatora dla miejsca.
	 * @return int - identyfikator miejsca
	 */
	public static int getNextPlaceId() 
	{
		return lastPlaceId++;
	}

	/**
	 * Metoda ustawiaj�ca ostatni� warto�� identyfikatora dla miejsca.
	 * @param lastPlaceId int - nowy ostatni identyfikator miejsca
	 */
	public static void setPlaceId(int lastPlaceId) 
	{
		IdGenerator.lastPlaceId = lastPlaceId;
	}

	/**
	 * Metoda zwraca ostatni� warto�� identyfikatora dla tranzycji.
	 * @return int - identyfikator tranzycji
	 */
	public static int getNextTransitionId() 
	{
		return lastTransitionId++;
	}

	/**
	 * Metoda ustawiaj�ca ostatni� warto�� identyfikatora dla tranzycji.
	 * @param lastTransitionId int - identyfikator tranzycji
	 */
	public static void setTransitionId(int lastTransitionId) 
	{
		IdGenerator.lastTransitionId = lastTransitionId;
	}
	
}
