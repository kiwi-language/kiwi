package tech.metavm.entity;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ObjectType;
import tech.metavm.util.InternalException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class InstanceCollectionParser<E extends Instance, C extends ReadWriteArray<E>>
    implements DefParser<C, ArrayInstance, InstanceCollectionDef<E,C>> {

    private final Type javaType;
    private final Class<?> javaClass;
    private final Class<?> elementClass;
    private final ArrayType type;

    public InstanceCollectionParser(Type javaType, Class<?> javaClass, Class<?> elementClass, ArrayType type) {
        this.javaType = javaType;
        this.javaClass = javaClass;
        this.elementClass = elementClass;
        this.type = type;
    }

    @Override
    public InstanceCollectionDef<E, C> create() {
        if(javaType instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            if(Collection.class.isAssignableFrom(rawClass)
                    && rawClass == javaClass
                    && parameterizedType.getActualTypeArguments().length == 1) {
                java.lang.reflect.Type elementType = parameterizedType.getActualTypeArguments()[0];
                if(Instance.class.isAssignableFrom(elementClass)
                        && elementType == elementClass
                        && (type.getElementType() instanceof ObjectType)) {
                    //noinspection unchecked,rawtypes
                    return new InstanceCollectionDef(
                            javaClass, javaType, elementClass, type
                    );
                }
            }
        }
        throw new InternalException("Fail to create InstanceCollectionDef with arguments (" +
                javaClass + ", " + javaType  + ", " + elementClass + ", " + type + ")"
        );
    }

    @Override
    public List<Type> getDependencyTypes() {
        return List.of();
    }

    @Override
    public void initialize() {

    }

}
