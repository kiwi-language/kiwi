package tech.metavm.entity;

import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

public class RecordParser<T extends Record> extends PojoParser<T, RecordDef<T>> {

    public static <T extends Record> RecordDef<T> parse(Class<T> entityType,
                                                        Function<Object, Long> getId,
                                                        DefMap defMap,
                                                        ModelInstanceMap modelInstanceMap) {
        return new RecordParser<>(entityType, getId, defMap, modelInstanceMap).parse();
    }

    public RecordParser(Class<T> entityType, Function<Object, Long> getId, DefMap defMap, ModelInstanceMap modelInstanceMap) {
        super(entityType, getId, defMap, modelInstanceMap);
    }

    @Override
    protected List<Field> getPropertyFields() {
        return NncUtils.map(javaType.getRecordComponents(), ReflectUtils::getField);
    }

    @Override
    protected RecordDef<T> createDef() {
        return new RecordDef<>(
                null,
                javaType,
                defMap.getPojoDef(javaType.getSuperclass()),
                createType(),
                defMap
        );
    }
}
