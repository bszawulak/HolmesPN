package holmes.workspace;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import holmes.darkgui.SpringUtilities;

/**
 * Klasa okna pomocniczego, zapewniającego trwałość środkowego podokna programu w wypadku np. zamknięcia
 * ostatniej zakładki.
 * @author students
 *
 */
public class WorkspaceFiller extends JPanel {
	private static final long serialVersionUID = 2789774405036709669L;
	private JScrollPane scrollPane;
	private JPanel innerPanel;
	
	/**
	 * Konstruktor obiektu podokna pomocniczego klasy WorkspaceFiller.
	 */
	public WorkspaceFiller() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane();
		innerPanel = new JPanel();
		innerPanel.setLayout(new SpringLayout());
		innerPanel.add(new JLabel("As it turns out, you might have closed, minimized or externalized all of the sheets in your project."));
		innerPanel.add(new JLabel("In such a situation, this panel will appear where your workspace should be."));
		innerPanel.add(new JLabel("It guards your workspace, prepared for your sheets to return home."));
		innerPanel.add(new JLabel("It keeps it ready for future generations of project sheets."));
		innerPanel.add(new JLabel("If you decide it's not needed where it is right now - feel free to close it."));
		innerPanel.add(new JLabel("If, however, the magic behind the interface decides otherwise, it may reappear immediately."));
		innerPanel.add(new JLabel("Don't worry. It's not a bug. It's a feature."));
		scrollPane.getViewport().add(innerPanel);
		add(scrollPane);
		SpringUtilities.makeCompactGrid(innerPanel, 7, 1, 5, 5, 5, 5);
	}
}
