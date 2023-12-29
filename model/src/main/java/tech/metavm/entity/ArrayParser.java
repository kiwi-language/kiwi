package tech.metavm.entity;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static tech.metavm.object.type.ResolutionStage.*;

public class ArrayParser<C extends ReadonlyArray<?>> extends DefParser<C, ArrayInstance, CollectionDef<?, C>> {

    private final Class<C> javaClass;
    private final Type javaType;
    private final DefContext defContext;
    private CollectionDef<?, C> def;

    public ArrayParser(Class<C> javaClass, Type javaType, DefContext defContext) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defContext = defContext;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CollectionDef<?, C> create() {
        Type elementType = getElementType();
        ModelDef elementDef = defContext.getDef(elementType, INIT);
        ArrayType type = defContext.getArrayType(elementDef.getType(), ArrayKind.getByEntityClass(javaClass));
        return def = new CollectionDef<>((Class)javaClass, javaType, type, elementDef, defContext);
    }

    @Override
    public CollectionDef<?, C> get() {
        return def;
    }

    @Override
    public void generateSignature() {
        defContext.ensureStage(def.getType().getElementType(), SIGNATURE);
    }

    @Override
    public void generateDeclaration() {
        defContext.ensureStage(def.getType().getElementType(), DECLARATION);
    }

    @Override
    public void generateDefinition() {
        defContext.ensureStage(def.getType().getElementType(), DEFINITION);
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
