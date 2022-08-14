package holmes.petrinet.simulators;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  Numerical Recipes 3rd Edition: The Art of Scientific Computing
 *  by William H. Press (Author), Saul A. Teukolsky (Author), William T. Vetterling (Author), Brian P. Flannery (Author) 
 * 
 *	http://www.javamex.com/tutorials/random_numbers/numerical_recipes.shtml
 */
public class HighQualityRandom implements IRandomGenerator {
	private Lock l = new ReentrantLock();
	private long u;
	private long v = 4101842887655102017L;
	private long w = 1;
	private Random generator;
	
	public HighQualityRandom() {
		this(System.nanoTime());
		generator = new Random(System.nanoTime());
	}
	
	public HighQualityRandom(long seed) {
		l.lock();
		u = seed ^ v;
		nextLong();
		v = u;
		nextLong();
		w = v;
		nextLong();
		l.unlock();
		
		generator = new Random(System.nanoTime());
	}
	
	public long nextLong() {
		l.lock();
		try {
			u = u * 2862933555777941757L + 7046029254386353087L;
			v ^= v >>> 17;
			v ^= v << 31;
			v ^= v >>> 8;
			w = 4294957665L * (w & 0xffffffff) + (w >>> 32);
			long x = u ^ (u << 21);
			x ^= x >>> 35;
			x ^= x << 4;
			return (x + v) ^ w;
		} finally {
			l.unlock();
		}
	}
	
	public long nextLong(long limit) {
		long v = nextLong();
		long ref = Long.MAX_VALUE;
		
		double tmp = (double)v/(double)ref;
		long result = (long) (tmp * limit);
		
		if(result == limit) //TODO:?
			result--;
			
		return result;
	}
	
	public long nextLong(long min, long max) 
	{
		long res = nextLong(max - min) + min;
		return res;
	}
	
	public int nextInt(int bits) {
		//return (int) (nextLong() >>> (64-bits));
		
		int result = (int) nextLong(bits);
		result = result < 0 ? -result : result;
		return result;
	}

	@Override
	public double nextDouble() {
		return generator.nextDouble();
	}

	public double nextDouble(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max);
	}
}
