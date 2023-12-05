package tech.metavm.entity;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ArrayParser<C extends ReadonlyArray<?>>
        implements DefParser<C, ArrayInstance, CollectionDef<?, C>> {

    private final Class<C> javaClass;
    private final Type javaType;
    private final DefContext defContext;

    public ArrayParser(Class<C> javaClass, Type javaType, DefContext defContext) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defContext = defContext;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CollectionDef<?, C> create() {
        Type elementType = getElementType();
        ModelDef elementDef = defContext.getDef(elementType);
        ArrayType type = defContext.getArrayType(elementDef.getType(), ArrayKind.getByEntityClass(javaClass));
        return new CollectionDef<>((Class)javaClass, javaType, type, elementDef, defContext);
    }

    @Override
    public List<Type> getDependencyTypes() {
        return List.of(getElementType());
    }

    @Override
    public void initialize() {

    }

    private Type getElementType() {
        if(javaType instanceof ParameterizedType pType) {
            return pType.getActualTypeArguments()[0];
        }
        else {
            return Object.class;
        }
    }
}
