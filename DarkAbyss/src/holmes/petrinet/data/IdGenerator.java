package holmes.petrinet.data;

/**
 * Generator identyfikatorów dla elementów sieci.
 * 
 * @author students
 * @author MR
 */
public class IdGenerator {
	private static int lastId = 0;
	private static int lastPlaceId = 0;
	private static int lastTransitionId = 0;
	
	/**
	 * Resetuje wartości id do zer.
	 */
	public static void resetIDgenerator() {
		lastId = 0;
		lastPlaceId = 0;
		lastTransitionId = 0;
	}
	
	public static String getCurrentValues() {
		String result = "";
		result += lastId;
		result += ";";
		result += lastPlaceId;
		result += ";";
		result += lastTransitionId;
		return result;
	}
	
	/**
	 * Metoda zwraca nowy wolny identyfikator.
	 * @return int - wartość ID
	 */
	public static int getNextId()
	{
		return lastId++;
	}
	
	/**
	 * Metoda ustawia początkową wartość identyfikatora.
	 * @param id int - nowa wartość startowa
	 */
	public static void setStartId(int id)
	{
		lastId = id;
	}

	/**
	 * Metoda zwraca ostatnią wartość identyfikatora dla miejsca.
	 * @return int - identyfikator miejsca
	 */
	public static int getNextPlaceId() 
	{
		return lastPlaceId++;
	}

	/**
	 * Metoda ustawiająca ostatnią wartość identyfikatora dla miejsca.
	 * @param lastPlaceId int - nowy ostatni identyfikator miejsca
	 */
	public static void setPlaceId(int lastPlaceId) 
	{
		IdGenerator.lastPlaceId = lastPlaceId;
	}

	/**
	 * Metoda zwraca ostatnią wartość identyfikatora dla tranzycji.
	 * @return int - identyfikator tranzycji
	 */
	public static int getNextTransitionId() 
	{
		return lastTransitionId++;
	}

	/**
	 * Metoda ustawiająca ostatnią wartość identyfikatora dla tranzycji.
	 * @param lastTransitionId int - identyfikator tranzycji
	 */
	public static void setTransitionId(int lastTransitionId) 
	{
		IdGenerator.lastTransitionId = lastTransitionId;
	}
}
