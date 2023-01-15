package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.util.Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class CollectionParser<C extends Table<?>>
        implements DefParser<C, ArrayInstance, CollectionDef<?, C>> {

    private final Class<C> javaClass;
    private final Type javaType;
    private final DefMap defMap;

    public CollectionParser(Class<C> javaClass, Type javaType, DefMap defMap) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defMap = defMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CollectionDef<?, C> create() {
        Type elementType = getElementType();
        ModelDef elementDef = defMap.getDef(elementType);
        ArrayType type = elementDef.getType().getArrayType();
        CollectionDef<?, C> def;
        return new CollectionDef<>((Class)javaClass, javaType, type, elementDef);
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
