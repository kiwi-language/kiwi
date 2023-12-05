package tech.metavm.entity;

import tech.metavm.util.ReflectUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public record EntityParentRef(
        Entity parent,
        @Nullable Field field
) {

    public static EntityParentRef fromArray(ChildArray<?> parent) {
        return new EntityParentRef(parent, null);
    }

    public static EntityParentRef fromObject(Entity entity, Field field) {
        ReflectUtils.ensureFieldDeclared(entity.getClass(), field);
        return new EntityParentRef(entity, field);
    }

}
