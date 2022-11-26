package tech.metavm.object.instance;

import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import java.util.Map;

public class ValueInstance extends Instance {

    public ValueInstance(Map<Field, Object> data, Type type, Class<?> entityType) {
        super(data, type, entityType);
    }

    @Override
    public void initId(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getId() {
        throw new UnsupportedOperationException();
    }
}
