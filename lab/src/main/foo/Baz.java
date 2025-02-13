public class Baz {

    private final Foo.Bar[] bars;

    public Baz(Foo.Bar[] bars) {
        this.bars = bars;
    }

    public Foo.Bar[] getBars() {
        return bars;
    }
}
