package holmes.utilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

/**
 * Klasa użytkowa, zapewniająca dużo kolorów
 */
public class ColorPalette {
	private int currentColor = 0;
	private ArrayList<Color> colors = new ArrayList<Color>();
	private Random generator = new Random(777);
	
	/**
	 * Konstruktor obiektów klasy ColorPalette.
	 */
	public ColorPalette () {
		initializeColor();
	}
	
	/**
	 * Każde wywołanie tej metody zwraca nowy kolor.
	 * @return Color - kolor
	 */
	public Color getColor() {
		if(currentColor < colors.size()) {
			currentColor++;
			return colors.get(currentColor-1);
		} else {
			return generateRandomColor(null);
		}
	}
	
	/**
	 * Metoda zwraca kolor o podanym numerze.
	 * @param value int - indeks
	 * @return Color - kolor
	 */
	public Color getColor(int value) {
		int size = colors.size();
		if(value >= size-1)
			return Color.BLACK;
		else
			return colors.get(value);
	}
	
	/**
	 * Metoda zwraca numer poprzednio zwróconego koloru.
	 * @return int - nr koloru z tablicy
	 */
	public int getCurrentIndex() {
		return currentColor;
	}
	
	/**
	 * Metoda pomocnicza, wypełnia wektor danych kolorami.
	 */
	private void initializeColor() {
		colors.add(new Color(255, 0, 0)); //1: czerwony
		colors.add(new Color(255, 255, 0)); //2: żółty
		colors.add(new Color(0, 255, 0)); //3: zielony
		colors.add(new Color(0, 0, 255)); //4: niebieski
		colors.add(new Color(102, 0, 204)); //5: fioletowy
		//colors.add(new Color(96, 96, 96)); //6: ciemny szary
		colors.add(new Color(255, 128, 0)); //7: pomarańczowy
		colors.add(new Color(102, 0, 0)); //8: dark red
		colors.add(new Color(51, 51, 0)); //9: brązowy
		colors.add(new Color(255, 102, 178)); //10: różowy
		colors.add(new Color(0, 102, 0)); //11: ciemny zielony
		colors.add(new Color(102, 102, 255)); //12: ...
		colors.add(new Color(153, 204, 255)); //13: ...
		//colors.add(new Color(192, 192, 192)); //14: jasny szary
		colors.add(new Color(102, 255, 255)); //15: ...
		colors.add(new Color(102, 102, 0)); //16: ...
		colors.add(new Color(0, 0, 102)); //17: ...
		colors.add(new Color(204, 153, 255)); //18: ...
		colors.add(new Color(229, 255, 204)); //19: ...
		colors.add(new Color(255, 204, 153)); //20: ...
		
		for(int i=0; i<100; i++) {
			colors.add(generateRandomColor(null));
		}

	}
	
	/**
	 * Metoda generuje losowy kolor.
	 * @param mix Color - opcjonalny kolor bazowy
	 * @return Color - nowy kolor
	 */
	private Color generateRandomColor(Color mix) {
		int red = generator.nextInt(256);
		int green = generator.nextInt(256);
		int blue = generator.nextInt(256);
		if (mix != null) {
			red = (red + mix.getRed()) / 2;
			green = (green + mix.getGreen()) / 2;
			blue = (blue + mix.getBlue()) / 2;
		}

		return new Color(red, green, blue);
	}
}
