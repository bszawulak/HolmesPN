package holmes.petrinet.subnets.dialogs;

/**
 * Klasa generyczna wykorzystywana w combo-boxach składająca się z etykiety (label) i wartości (value).
 */
public class ComboBoxItem<T> {
    private T value;
    private String label;

    public ComboBoxItem(T value, String label) {
        this.value = value;
        this.label = label;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return label;
    }
}