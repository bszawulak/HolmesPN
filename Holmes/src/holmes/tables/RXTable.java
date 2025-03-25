package holmes.tables;

import java.awt.*;
import java.awt.event.*;
import java.io.Serial;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

/**
 * The RXTable provides some extensions to the default JTable
 * @author - well, the author, whoever he is. Not me (MR), just borrowed the code from
 * <a href="https://tips4java.wordpress.com/2008/10/20/table-select-all-editor/">...</a>
 */
public class RXTable extends JTable {
	@Serial
	private static final long serialVersionUID = 7638147862995205299L;
	private boolean isSelectAllForMouseEvent = false;
	private boolean isSelectAllForActionEvent = false;
	private boolean isSelectAllForKeyEvent = false;

    /**
     * Constructs a default <code>RXTable</code> that is initialized with a default
     * data model, a default column model, and a default selection model.
     */
    public RXTable() {
        this(null, null, null);
    }

    /**
     * Constructs a <code>RXTable</code> that is initialized with <code>dm</code> as the data model, a default column model,
     * and a default selection model.
     * @param dm the data model for the table
     */
    public RXTable(TableModel dm) {
        this(dm, null, null);
    }

    /**
     * Constructs a <code>RXTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code> as the
     * column model, and <code>sm</code> as the selection model.
     * If any of the parameters are <code>null</code> this method
     * will initialize the table with the corresponding default model.
     * The <code>autoCreateColumnsFromModel</code> flag is set to false
     * if <code>cm</code> is non-null, otherwise it is set to true
     * and the column model is populated with suitable
     * <code>TableColumns</code> for the columns in <code>dm</code>.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @param sm        the row selection model for the table
     */
    public RXTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
    }

    /**
     * Constructs a <code>RXTable</code> with <code>numRows</code>
     * and <code>numColumns</code> of empty cells using
     * <code>DefaultTableModel</code>.  The columns will have
     * names of the form "A", "B", "C", etc.
     *
     * @param numRows           the number of rows the table holds
     * @param numColumns        the number of columns the table holds
     */
    public RXTable(int numRows, int numColumns) {
        this(new DefaultTableModel(numRows, numColumns));
    }

	/*
	 *  Override to provide Select All editing functionality
	 */
	public boolean editCellAt(int row, int column, EventObject e) {
		boolean result = super.editCellAt(row, column, e);
		if (isSelectAllForMouseEvent || isSelectAllForActionEvent || isSelectAllForKeyEvent) {
			selectAll(e);
		}
		return result;
	}

	/*
	 * Select the text when editing on a text related cell is started
	 */
	private void selectAll(EventObject e)
	{
		final Component editor = getEditorComponent();

		if (editor == null || ! (editor instanceof JTextComponent))
			return;

		if (e == null) {
			((JTextComponent)editor).selectAll();
			return;
		}

		//  Typing in the cell was used to activate the editor

		if (e instanceof KeyEvent && isSelectAllForKeyEvent) {
			((JTextComponent)editor).selectAll();
			return;
		}

		//  F2 was used to activate the editor

		if (e instanceof ActionEvent && isSelectAllForActionEvent) {
			((JTextComponent)editor).selectAll();
			return;
		}

		//  A mouse click was used to activate the editor.
		//  Generally this is a double click and the second mouse click is
		//  passed to the editor which would remove the text selection unless
		//  we use the invokeLater()

		if (e instanceof MouseEvent && isSelectAllForMouseEvent) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((JTextComponent)editor).selectAll();
				}
			});
		}
	}

	/*
	 *  Sets the Select All property for for all event types
	 */
	public void setSelectAllForEdit(boolean isSelectAllForEdit) {
		setSelectAllForMouseEvent( isSelectAllForEdit );
		setSelectAllForActionEvent( isSelectAllForEdit );
		setSelectAllForKeyEvent( isSelectAllForEdit );
	}

	/*
	 *  Set the Select All property when editing is invoked by the mouse
	 */
	public void setSelectAllForMouseEvent(boolean isSelectAllForMouseEvent) {
		this.isSelectAllForMouseEvent = isSelectAllForMouseEvent;
	}

	/*
	 *  Set the Select All property when editing is invoked by the "F2" key
	 */
	public void setSelectAllForActionEvent(boolean isSelectAllForActionEvent) {
		this.isSelectAllForActionEvent = isSelectAllForActionEvent;
	}

	/*
	 *  Set the Select All property when editing is invoked by
	 *  typing directly into the cell
	 */
	public void setSelectAllForKeyEvent(boolean isSelectAllForKeyEvent) {
		this.isSelectAllForKeyEvent = isSelectAllForKeyEvent;
	}
}  // End of Class RXTable
