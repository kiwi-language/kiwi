package tech.metavm.entity;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.AnyType;
import tech.metavm.util.InternalException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class InstanceCollectionParser<E extends Instance, C extends ReadWriteArray<E>> extends DefParser<C, ArrayInstance, InstanceCollectionDef<E,C>> {

    private final Type javaType;
    private final Class<?> javaClass;
    private final Class<?> elementClass;
    private final ArrayType type;
    private InstanceCollectionDef<E, C> def;

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
                Type elementType = parameterizedType.getActualTypeArguments()[0];
                if(Instance.class.isAssignableFrom(elementClass)
                        && elementType == elementClass
                        && (type.getElementType() instanceof AnyType)) {
                    //noinspection unchecked,rawtypes
                    return def =  new InstanceCollectionDef(
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
    public InstanceCollectionDef<E, C> get() {
        return def;
    }

    @Override
    public void generateSignature() {

    }

    @Override
    public void generateDeclaration() {

    }

    @Override
    public void generateDefinition() {

    }

}
