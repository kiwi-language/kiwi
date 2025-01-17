import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;

import javax.annotation.Nullable;

@Entity(searchable = true)
public class Foo {

    private String name;

    @ChildEntity
    private final Bar bar;
    @ChildEntity
    private final Baz[] bazList;

    @Nullable
    private final Qux qux;

    public Foo(String name, Bar bar, Baz[] bazList, @Nullable Qux qux) {
        this.name = name;
        this.bar = bar;
        this.bazList = bazList;
        this.qux = qux;
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
