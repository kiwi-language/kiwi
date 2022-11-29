package tech.metavm.entity;

import tech.metavm.object.meta.Type;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface EntityIdProvider {

    long getTypeId(long id);

//    Long getEnumConstantId(Enum<?> enumConstant);
//
//    Long getUniqueConstraintId(Field field);
//
//    Long getFieldId(Field field);
//
//    Long getTypeId(Class<?> klass);

    Map<Type, List<Long>> allocate(long tenantId, Map<Type, Integer> typeId2count);

}
