package holmes.petrinet.simulators;

/**
 * Interface generatorów liczb pseudolosowych.
 */
public interface IRandomGenerator {
	/**
	 * Zwraca liczbę z przedziału <0, Long.MAXVALUE>
	 * @return long - wartość losowa
	 */
	long nextLong();
	/**
	 * Zwraca liczbę z przedziału <0,max-1>
	 * @param max long - maksymalna wartość
	 * @return long - liczba : <0,max-1>
	 */
	long nextLong(long max);
	/**
	 * Zwraca liczbe z przedziału <min, max-1>
	 * @param min long - dolny zakres domknięty
	 * @param max long - gówny zakres otwarty
	 * @return long - wartość losowa
	 */
	long nextLong(long min, long max);
	int nextInt(int max);
	/**
	 * Deleguje generator Java.Random do wygenerowania losowej liczby double
	 * @return double - losowa wartość
	 */
	double nextDouble();

	double nextDouble(double min, double max);
}
