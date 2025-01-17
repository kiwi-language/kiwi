import org.metavm.api.ChildEntity;

public class Baz {

    @ChildEntity
    private final Bar[] bars;

    public Baz(Bar[] bars) {
        this.bars = bars;
    }

    public Bar[] getBars() {
        return bars;
    }
}
