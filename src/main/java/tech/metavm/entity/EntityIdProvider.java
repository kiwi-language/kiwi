package tech.metavm.entity;

import java.lang.reflect.Field;

public interface EntityIdProvider {

    Long getEnumConstantId(Enum<?> enumConstant);

    Long getUniqueConstraintId(Field field);

    Long getFieldId(Field field);

    Long getTypeId(Class<?> klass);

}
