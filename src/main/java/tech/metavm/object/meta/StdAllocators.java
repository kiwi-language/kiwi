package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class StdAllocators implements EntityIdProvider {

    private final Map<String, StdAllocator> typeAllocatorMap = new HashMap<>();

    public StdAllocators(Map<String, String> idFileMap
    ) {
        idFileMap.forEach((entityType, file) -> typeAllocatorMap.put(entityType, new StdAllocator(file)));
    }

    public Long getId(Object object) {
        if(object instanceof java.lang.reflect.Field field) {
            return getFieldId(field);
        }
        if(object instanceof Class<?> klass) {
            return getTypeId(klass);
        }
        if(object instanceof Enum<?> enumConstant) {
            return getEnumConstantId(enumConstant);
        }
        throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
    }

    public long getId(Class<? extends Entity> entityType, String entityCode) {
        return typeAllocatorMap.get(entityType.getSimpleName()).getId(entityCode);
    }

    public Long getEnumConstantId(Enum<?> enumConstant) {
        return typeAllocatorMap.get(enumConstant.getClass().getSimpleName()).getId(enumConstant.name());
    }

    @SuppressWarnings("rawtypes")
    public Enum<?> getEnumConstantById(long id) {
        String code = typeAllocatorMap.get("EnumConstant").getCodeById(id);
        int idx = code.lastIndexOf(".");
        try {
            Class<? extends Enum> klass = Class.forName(code.substring(0, idx)).asSubclass(Enum.class);
            String name = code.substring(idx + 1);
            for (Enum enumConstant : klass.getEnumConstants()) {
                if(enumConstant.name().equals(name)) {
                    return enumConstant;
                }
            }
            throw new InternalException("Enum constant " + code + " not found");
        } catch (ClassNotFoundException e) {
            throw new InternalException("Invalid enum constant code: " + code);
        }

    }

    @Override
    public Long getUniqueConstraintId(java.lang.reflect.Field field) {
        return getId(UniqueConstraintRT.class,
                field.getDeclaringClass().getName() + "." + field.getName());
    }

    @Override
    public Long getFieldId(java.lang.reflect.Field field) {
        return getId(Field.class, field.getDeclaringClass().getName() + "." + field.getName());
    }

    public Long getTypeId(Class<?> klass) {
        return getId(Type.class, klass.getName());
    }

    public Long getTypeId(java.lang.reflect.Type type) {
        return getId(Type.class, getTypeCode(type));
    }

    private String getTypeCode(java.lang.reflect.Type type) {
        if(type instanceof Class<?> klass) {
            return klass.getName();
        }
        if(type instanceof ParameterizedType pType) {
            return getTypeCode(pType.getRawType()) + "<" +
                    NncUtils.join(pType.getActualTypeArguments(), this::getTypeCode) + ">";
        }
        throw new InternalException("Can not get code for type: " + type);
    }

    public long getUniqueConstraintId(String code) {
        return getId(UniqueConstraintRT.class, code);
    }

    public long getTypeCategoryId(TypeCategory typeCategory) {
        return getEnumConstantId(typeCategory);
    }

}
