package holmes.analyse.firingalgo.petrinetstructure;

public class Arc {
    public holmes.petrinet.elements.Arc arcRef;

    private Node in;
    private Node out;

    public Arc(Node in, Node out) {
        this(in, out, null);
    }

    public Arc(Node in, Node out, holmes.petrinet.elements.Arc arcRef) {
        setIn(in);
        setOut(out);
        this.arcRef = arcRef;
    }

    public Node getIn() {
        return in;
    }

    public void setIn(Node in) {
        if (this.in == in) {
            return;
        }

        if (this.in != null) {
            this.in.removeOutputArc(this);
        }

        this.in = in;

        if (in != null) {
            in.addOutputArc(this);
        }
    }

    public Node getOut() {
        return out;
    }

    public void setOut(Node out) {
        if (this.out == out) {
            return;
        }

        if (this.out != null) {
            this.out.removeInputArc(this);
        }

        this.out = out;

        if (out != null) {
            out.addInputArc(this);
        }
    }
}

