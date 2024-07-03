import org.metavm.api.ChildEntity;

import java.util.ArrayList;
import java.util.List;

public class Product {
    @ChildEntity
    private final List<Attribute<String>> attributes = new ArrayList<>();

    public void setAttribute(String name, String value) {
        attributes.removeIf(a -> a.name().equals(name));
        attributes.add(new Attribute<>(name, value));
    }

    public List<Attribute<String>> getAttributes() {
        return attributes;
    }
}
