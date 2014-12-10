package abyss.workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import abyss.graphpanel.GraphPanel;

/**
 * Klasa konkretnego obszaru roboczego. Jej obiekty s¹ trzymane w tablicy, znajduj¹cej
 * siê w klasie WorkSpace.
 * @author students
 *
 */
public class WorkspaceSheet extends JScrollPane {
	private static final long serialVersionUID = -3420561980683001607L;
	private final int id; // aktualne id sheeta w workspace
	private GraphPanel graphPanel;
	private Workspace workspace;
	private SheetPanel panel;

	/**
	 * Konstruktor obiektu klasy WorkspaceSheet
	 * @param text String - opis
	 * @param ID int - identyfikator
	 * @param work Workspace - referencja na obiekt inicjuj¹cy i przechowuj¹cy
	 */
	public WorkspaceSheet(String text, int ID, Workspace work) {
		// super(new FlowLayout());
		workspace = work;

		// The pane
		setMinimumSize(new Dimension(100, 100));
		setBackground(Color.white);
		setBorder(BorderFactory.createLineBorder(Color.lightGray));
		panel = new SheetPanel(this);
		panel.setLayout(null);
		setGraphPanel(workspace.getProject().createAndAddGraphPanel(ID));
		getGraphPanel().setBounds(0, 0,
				(int) Toolkit.getDefaultToolkit().getScreenSize().width,
				(int) Toolkit.getDefaultToolkit().getScreenSize().height);
		panel.add(getGraphPanel());
		panel.setOpaque(true);
		getViewport().add(panel);

		id = ID;
	}

	/**
	 * Metoda zwraca identyfikator obszaru roboczego.
	 * @return int - identyfikator
	 */
	public int getId() {
		return id;
	}

	/**
	 * Metoda zwraca obiekt JPanel.
	 * @return JPanel - JPanel
	 */
	public JPanel getContainerPanel() {
		return panel;
	}

	/**
	 * Metoda zwracaj¹ca obiekt GraphPanel.
	 * @return GraphPanel - obiekt
	 */
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt GraphPanel.
	 * @return GraphPanel - obiekt
	 */
	private void setGraphPanel(GraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}
	
	/**
	 * Metoda odpowiedzialna za przewijanie w poziomie.
	 * @param delta int - d³ugoœæ przewiniêcia
	 */
	public void scrollHorizontal(int delta) {
		JScrollBar bar = this.getHorizontalScrollBar();
		int value = bar.getValue();
		value += delta;
		bar.setValue(value);
	}
	
	/**
	 * Metoda odpowiedzialna za przewijanie w pionie.
	 * @param delta int - d³ugoœæ przewiniêcia
	 */
	public void scrollVertical(int delta) {
		JScrollBar bar = this.getVerticalScrollBar();
		int value = bar.getValue();
		value += delta;
		bar.setValue(value);
	}

	/**
	 * Klasa narzêdziowa wewn¹trz WorkspaceSheet.
	 * @author students
	 *
	 */
	public class SheetPanel extends JPanel {
		private static final long serialVersionUID = -1440470091168792811L;
		private WorkspaceSheet sheet;

		/**
		 * Konstruktor obiektu klasy SheetPanel
		 * @param sheet WorkspaceSheet - referencja obiektu nadrzêdnego
		 */
		SheetPanel(WorkspaceSheet sheet) {
			this.sheet = sheet;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension dim = new Dimension();
			dim = sheet.getGraphPanel().getSize();
			return dim;
		}
	}
}
