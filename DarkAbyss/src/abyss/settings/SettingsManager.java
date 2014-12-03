package abyss.settings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class SettingsManager {
	private ArrayList<Setting> settings;

	public SettingsManager() {
		settings = new ArrayList<Setting>();
	}
	
	public Integer getValue(String ID) {
		for(Setting s : settings) {
			if(s.getID()==ID) return new Integer(s.getValue());
		}
		return null;
	}
	
	public int setValue(String ID,int value) {
		boolean found= false;
		for(Setting s : settings) {
			if(s.getID()==ID) 
				{
				found = true;
				s.setValue(value);
				return 0;
				}
		}
		if (!found) settings.add(new Setting(ID,value));
		return -1;
	}

	public void write() {
		try {
			FileOutputStream fileOut = new FileOutputStream("settings.stg");
			PrintStream ps = new PrintStream(fileOut);
			for (Setting data : settings) {
				ps.println(data.getID() + " "
						+ Integer.toString(data.getValue()));
			}
			fileOut.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unknown error",
					"For some reason, settings could not be saved.",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void read() {
		Path path = Paths.get("settings.stg");
		String currentLine;
		settings.clear();
		try (Scanner scanner = new Scanner(path)) {
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				settings.add(convertLineToSetting(currentLine));
				//log(currentLine);
			}
		} catch (IOException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"Settings not found!",
							"The file \"settings\".stg, which normally contains the settings for this application, has not been found. Unable to load settings.",
							JOptionPane.ERROR_MESSAGE);
		}
	}

	private Setting convertLineToSetting(String line) {
		try {
			int i;
			for (i = 0; i < line.length(); i++)
				if (line.charAt(i) == ' ')
					break;
			String ID = line.substring(0, i);
			// log("ID: " + ID);
			int value = Integer.parseInt(line.substring(i + 1, line.length()));
			// log("Value: " + Integer.toString(value));
			return new Setting(ID, value);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							null,
							"Settings corrupt!",
							"The file \"settings\".stg, which normally contains the settings for this application, is corrupt. Unable to load settings.",
							JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public static void log(Object aMsg) {
		System.out.println(String.valueOf(aMsg));
	}
	
	public void loadSettings() {
		read();
	}
	
	public void saveSettings() {
		write();
	}
}