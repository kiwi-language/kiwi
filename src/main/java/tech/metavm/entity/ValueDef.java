package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;

public class ValueDef<T> extends PojoDef<T> {

    public ValueDef(/*long id,*/
                    String name,
                    TypeReference<T> typeReference,
                    @Nullable PojoDef<? super T> parentDef,
                    Type type) {
        this(/*id, */name, typeReference.getType(), parentDef, type);
    }

    public ValueDef(/*long id,*/
                    String name,
                    Class<T> entityType,
                    @Nullable PojoDef<? super T> parentDef,
                    Type type) {
        super(/*id,*/ name, entityType, parentDef, type);
    }

}
