package abyss.graphpanel;

public class IdGenerator {
	
	private static int lastId = 0;
	private static int lastPlaceId = 0;
	private static int lastTransitionId = 0;
	
	public static int getNextId()
	{
		return lastId++;
	}
	
	static void setStartId(int id)
	{
		lastId = id;
	}

	public static int getNextPlaceId() 
	{
		return lastPlaceId++;
	}

	static void setPlaceId(int lastPlaceId) 
	{
		IdGenerator.lastPlaceId = lastPlaceId;
	}

	public static int getNextTransitionId() 
	{
		return lastTransitionId++;
	}

	static void setTransitionId(int lastTransitionId) 
	{
		IdGenerator.lastTransitionId = lastTransitionId;
	}
	
}
