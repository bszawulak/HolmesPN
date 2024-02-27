package holmes.petrinet.subnets.dialogs;

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