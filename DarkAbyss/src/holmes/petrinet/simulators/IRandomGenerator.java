package holmes.petrinet.simulators;

/**
 * Interface generatorów liczb pseudolosowych.
 * 
 * @author MR
 */
public interface IRandomGenerator {
	public long nextLong();
	public long nextLong(long max);
	public long nextLong(long min, long max);
	public int nextInt(int bits);
	public double nextDouble();
}
