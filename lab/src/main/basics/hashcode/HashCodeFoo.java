package hashcode;

public class HashCodeFoo {

    private String name;

    public HashCodeFoo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashCodeFoo that && name.equals(that.name);
    }
}
