package tech.metavm.object.instance;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import java.util.Collections;
import java.util.List;

public class Directory extends Entity {

    private final Type type;
    private final List<Field> fields;

    public Directory(Long id, EntityContext context, Type type, List<Field> fields) {
        super(id, context);
        this.type = type;
        this.fields = Collections.unmodifiableList(fields);
    }

    public Type getType() {
        return type;
    }

    public List<Field> getFields() {
        return fields;
    }
}
