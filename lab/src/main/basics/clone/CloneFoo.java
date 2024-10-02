package clone;

public class CloneFoo implements Cloneable {

    private String value;

    public CloneFoo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CloneFoo clone() {
        try {
            return (CloneFoo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
