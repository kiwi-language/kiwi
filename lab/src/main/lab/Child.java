import tech.metavm.entity.EntityField;

import javax.annotation.Nullable;

public class Child {

    @EntityField(value = "name", asTitle = true)
    private final String name;
    private @Nullable Child next;

    public Child(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Child getNext() {
        return next;
    }

    public void setNext(@Nullable Child next) {
        this.next = next;
    }
}
