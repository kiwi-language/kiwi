package tech.metavm.entity;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.TypeFactory;

import java.lang.reflect.Type;

public class EntityParser<T extends Entity> extends PojoParser<T, EntityDef<T>> {

//    public static <T extends Entity> EntityDef<T> parse(Class<T> entityType,
//                                                        Type genericType,
//                                                        DefMap defMap) {
//        return new EntityParser<>(entityType, genericType, defMap).parse();
//    }

    public EntityParser(Class<T> entityType, Type genericType, DefMap defMap) {
        super(entityType, genericType, defMap);
    }

    @Override
    protected EntityDef<T> createDef(PojoDef<? super T> superDef) {
        return new EntityDef<>(
                javaClass,
                getJavaType(),
                superDef,
                createType(),
                defMap
        );
    }

    @Override
    protected ClassType createType(TypeFactory typeFactory, String name, String code, ClassType superType) {
        return typeFactory.createClass(name, code, superType);
    }

}
