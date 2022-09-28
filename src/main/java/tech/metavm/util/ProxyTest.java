package tech.metavm.util;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;

public class ProxyTest extends Entity {

    private final String name;

    public ProxyTest(Long id, EntityContext context, String name) {
        super(id, context);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
