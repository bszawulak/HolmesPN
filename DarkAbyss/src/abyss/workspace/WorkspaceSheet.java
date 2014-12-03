package abyss.workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import abyss.graphpanel.GraphPanel;

public class WorkspaceSheet extends JScrollPane {
	private static final long serialVersionUID = -3420561980683001607L;

	private final int id; // aktualne id sheeta w workspace

	private GraphPanel graphPanel;
	private Workspace workspace;
	private SheetPanel panel;

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

	public int getId() {
		return id;
	}

	public JPanel getContainerPanel() {
		return panel;
	}

	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	private void setGraphPanel(GraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}
	
	public void scrollHorizontal(int delta) {
		JScrollBar bar = this.getHorizontalScrollBar();
		int value = bar.getValue();
		value += delta;
		bar.setValue(value);
	}
	
	public void scrollVertical(int delta) {
		JScrollBar bar = this.getVerticalScrollBar();
		int value = bar.getValue();
		value += delta;
		bar.setValue(value);
	}

	public class SheetPanel extends JPanel {
		private static final long serialVersionUID = -1440470091168792811L;
		private WorkspaceSheet sheet;

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
