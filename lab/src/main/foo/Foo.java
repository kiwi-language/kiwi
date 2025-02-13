import org.metavm.api.Entity;

import javax.annotation.Nullable;

@Entity(searchable = true)
public class Foo {

    private String name;

    private final Bar bar;

    private final Baz[] bazList;

    @Nullable
    private final Qux qux;

    public Foo(String name, BarDto bar, BazDto[] bazList, @Nullable Qux qux) {
        this.name = name;
        this.bar = new Bar(bar.code());
        this.bazList = new Baz[bazList.length];
        for (int i = 0; i < bazList.length; i++) {
            var bazDTO = bazList[i];
            var bars = new Bar[bazDTO.bars().length];
            for (int i1 = 0; i1 < bazDTO.bars().length; i1++) {
                bars[i1] = new Bar(bazDTO.bars()[i1].code());
            }
            this.bazList[i] = new Baz(bars);
        }
        this.qux = qux;
    }

    public class Bar {
        private final String code;

        public Bar(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bar getBar() {
        return bar;
    }

    public Baz[] getBazList() {
        return bazList;
    }

    @Nullable
    public Qux getQux() {
        return qux;
    }
}
