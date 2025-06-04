package holmes.utilities;

public class Pair<T0, T1> {
    private T0 first;
    private T1 second;

    public Pair(T0 first, T1 second) {
        this.first = first;
        this.second = second;
    }

    public T0 getFirst() {
        return first;
    }

    public void setFirst(T0 value) {
        first = value;
    }

    public T1 getSecond() {
        return second;
    }

    public void setSecond(T1 value) {
        second = value;
    }
}
