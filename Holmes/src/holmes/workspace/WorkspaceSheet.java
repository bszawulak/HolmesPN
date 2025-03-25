package holmes.workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serial;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import holmes.graphpanel.GraphPanel;

/**
 * Klasa konkretnego obszaru roboczego. Jej obiekty są trzymane w tablicy znajdującej
 * się w klasie Workspace.
 */
public class WorkspaceSheet extends JScrollPane {
	@Serial
	private static final long serialVersionUID = -3216362854094205041L;
	private final int id; // aktualne id sheeta w workspace
	private GraphPanel graphPanel;
	private Workspace workspace; //referencja na rodzica
	private SheetPanel sheetPanel;
	//private JPanel sheetPanel;

	public JScrollPane scrollPane;

	/**
	 * Konstruktor obiektu klasy WorkspaceSheet
	 * @param ID int - identyfikator
	 * @param work Workspace - referencja na obiekt inicjujący i przechowujący
	 */
	public WorkspaceSheet(int ID, Workspace work) {
		workspace = work;

		//setMinimumSize(new Dimension(100, 100));  //....
		//setBackground(Color.white);  //....
		//setBorder(BorderFactory.createLineBorder(Color.lightGray));  //....

		sheetPanel = new SheetPanel(this);
		sheetPanel.setLayout(null);
		setGraphPanel(workspace.getProject().createAndAddGraphPanel(ID));
		getGraphPanel().setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height);
		sheetPanel.add(getGraphPanel());
		sheetPanel.setOpaque(true);

		//getViewport().add(sheetPanel);  //....

		graphPanel.setOriginSize(graphPanel.getSize());
		id = ID;

		//scrollPane = new JScrollPane(sheetPanel);
		//scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(JScrollPane sp) {
		scrollPane = sp;
	}

	/**
	 * Metoda zwraca identyfikator obszaru roboczego.
	 * @return int - identyfikator
	 */
	public int getId() {
		return id;
	}

	/**
	 * Metoda zwraca obiekt JPanel. W zasadzie to SheetPanel która rozszera JPanel. SheetPanel siedzi
	 * jako klasa wewnętrzna w WorkspaceSheet.
	 * @return JPanel - JPanel (WorkspaceSheet.SheetPanel)
	 */
	public JPanel getContainerPanel() {
		return sheetPanel;
	}

	/**
	 * Metoda zwracająca obiekt GraphPanel.
	 * @return GraphPanel - obiekt klasy.
	 */
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiająca nowy obiekt GraphPanel.
	 */
	private void setGraphPanel(GraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}
	
	/**
	 * Metoda odpowiedzialna za przewijanie w poziomie.
	 * @param delta int - długość przewinięcia
	 */
	public void scrollHorizontal(int delta) {
		//JScrollBar bar = this.getHorizontalScrollBar();
		JScrollBar bar = this.scrollPane.getHorizontalScrollBar();
		int value = bar.getValue();
		value += delta;
		bar.setValue(value);
	}
	
	/**
	 * Metoda odpowiedzialna za przewijanie w pionie.
	 * @param delta int - długość przewinięcia
	 */
	public void scrollVertical(int delta) {
		//JScrollBar bar = this.getVerticalScrollBar();
		JScrollBar bar = this.scrollPane.getVerticalScrollBar();
		int value = bar.getValue();
		value += delta;
		bar.setValue(value);
	}

	/**
	 * Klasa narzędziowa wewnątrz WorkspaceSheet. To po prostu panel zawierający jednek dodatkową informację - 
	 * pole sheet klasy WorkspaceSheet mówiące, który obiekt jest jego właścicielem.
	 */
	public static class SheetPanel extends JPanel {
		@Serial
		private static final long serialVersionUID = -1440470091168792811L;
		private WorkspaceSheet sheet;

		/**
		 * Konstruktor obiektu klasy SheetPanel
		 * @param sheet WorkspaceSheet - referencja obiektu nadrzędnego
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
