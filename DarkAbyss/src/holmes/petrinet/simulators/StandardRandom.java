package holmes.petrinet.simulators;

import java.util.Random;

public class StandardRandom implements IRandomGenerator {
	private Random generator;

	public StandardRandom() {
		generator = new Random();
	}
	public StandardRandom(long seed) {
		generator = new Random(seed);
	}
	
	public long nextLong() {
		return generator.nextLong();
	}
	
	public long nextLong(long max) {
		long v = generator.nextLong();
		long ref = Long.MAX_VALUE;
		
		double tmp = (double)v/(double)ref;
		long result = (long) (tmp * max);
		
		if(result == max) //TODO:?
			result--;
			
		return result;
	}
	
	public long nextLong(long min, long max) {
		long res = nextLong(max - min) + min;
		return res;
	}
	
	public int nextInt(int max) {
		return generator.nextInt(max);
	}
}
