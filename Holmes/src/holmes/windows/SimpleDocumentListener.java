package holmes.windows;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Interfejs funkcyjny pozwalający na nasłuchiwanie i reagowanie na zmiany w komponentach tekstowych np. JTextField
 */
@FunctionalInterface
public interface SimpleDocumentListener extends DocumentListener {
    void update(DocumentEvent e);

    @Override
    default void insertUpdate(DocumentEvent e) {
        update(e);
    }
    @Override
    default void removeUpdate(DocumentEvent e) {
        update(e);
    }
    @Override
    default void changedUpdate(DocumentEvent e) {
        update(e);
    }
}
