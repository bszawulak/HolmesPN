package holmes.utilities;

public class Triplet<T0, T1, T2> {
    private T0 first;
    private T1 second;
    private T2 third;

    public Triplet(T0 first, T1 second, T2 third) {
        this.first = first;
        this.second = second;
        this.third = third;
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

    public T2 getThird() {
        return third;
    }

    public void setThird(T2 third) {
        this.third = third;
    }
}
