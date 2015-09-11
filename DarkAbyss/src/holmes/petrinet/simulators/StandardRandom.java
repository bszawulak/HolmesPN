package holmes.petrinet.simulators;

import java.util.Random;

/**
 * Klasa opakowująca standardowy generator liczb pseudo-losowych w Javie w interface IRandomGenerator.
 * 
 * @author MR
 *
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
		long v = generator.nextLong();
		long ref = Long.MAX_VALUE;
		
		double tmp = (double)v/(double)ref;
		long result = (long) (tmp * max);
		
		if(result == max) //TODO:?
			result--;
			
		return result;
	}
	
	@Override
	public long nextLong(long min, long max) {
		long res = nextLong(max - min) + min;
		return res;
	}
	
	@Override
	public int nextInt(int max) {
		return generator.nextInt(max);
	}
	
	@Override
	public double nextDouble() {
		return generator.nextDouble();
	}
}
