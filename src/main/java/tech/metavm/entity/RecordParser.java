package tech.metavm.entity;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.TypeFactory;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public class RecordParser<T extends Record> extends PojoParser<T, RecordDef<T>> {

//    public static <T extends Record> RecordDef<T> parse(Class<T> entityType,
//                                                        Type genericType,
//                                                        DefMap defMap) {
//        return new RecordParser<>(entityType, genericType, defMap).parse();
//    }

    public RecordParser(Class<T> entityType, Type genericType, DefMap defMap) {
        super(entityType, genericType, defMap);
    }

    @Override
    protected List<Field> getPropertyFields() {
        return NncUtils.map(javaType.getRecordComponents(), ReflectUtils::getField);
    }

    @Override
    protected RecordDef<T> createDef(PojoDef<? super T> parentDef) {
        return new RecordDef<>(
                javaType,
                getGenericType(),
                parentDef,
                createType(),
                defMap
        );
    }

    @Override
    protected ClassType createType(TypeFactory typeFactory, String name, String code, ClassType superType) {
        return typeFactory.createClass(name, code, superType);
    }

}
