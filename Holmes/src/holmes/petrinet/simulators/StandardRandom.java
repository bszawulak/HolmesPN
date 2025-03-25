package holmes.petrinet.simulators;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Klasa opakowująca standardowy generator liczb pseudo-losowych w Javie w interface IRandomGenerator.
 */
public class StandardRandom implements IRandomGenerator  {
	private Random generator;

	public StandardRandom() {
		generator = new Random();
	}
	public StandardRandom(long seed) {
		generator = new Random(seed);
	}
	
	@Override
	public long nextLong() {
		return generator.nextLong();
	}
	
	@Override
	public long nextLong(long max) {
		long bits, val;
		do {
			bits = (generator.nextLong() << 1) >>> 1;
			val = bits % max;
		} while (bits-val+(max-1) < 0L);
		
		return val;
		/*
		long v = generator.nextLong();
		long ref = Long.MAX_VALUE;
		
		double tmp = (double)v/(double)ref;
		long result = (long) (tmp * max);
		
		if(result == max)
			result--;
			
		return result;
		*/
	}

	/**
	 * Metoda zwraca liczbę losową typu int z podanego zakresu.
	 * @param min int - dolna granica
	 * @param max int - górna granica
	 * @return int - liczba z zakresu [min, max]
	 */
	@SuppressWarnings("unused")
	private int getRandomInt(int min, int max) {
		if(min == 0 && max == 0)
			return 0;
		if(min == max)
			return min;

		return generator.nextInt((max - min) + 1) + min; //OK, zakres np. 3 do 6 daje: 3,4,5,6 (graniczne obie też!)
	}
	
	@Override
	public long nextLong(long min, long max) {
		return nextLong(max - min) + min;
	}
	
	@Override
	public int nextInt(int max) {
		return generator.nextInt(max);
	}
	
	@Override
	public double nextDouble() {
		return generator.nextDouble();
	}

	public double nextDouble(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max);
	}
}
