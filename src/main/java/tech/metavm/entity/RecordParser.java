package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

public class RecordParser<T extends Record> extends PojoParser<T, RecordDef<T>> {

    public static <T extends Record> RecordDef<T> parse(Class<T> entityType,
                                                        Function<Object, Instance> getInstance,
                                                        DefMap defMap,
                                                        ModelMap modelMap) {
        return new RecordParser<>(entityType, getInstance, defMap, modelMap).parse();
    }

    public RecordParser(Class<T> entityType, Function<Object, Instance> getInstance, DefMap defMap, ModelMap modelMap) {
        super(entityType, getInstance, defMap, modelMap);
    }

    @Override
    protected List<Field> getFields(Class<T> entityType) {
        return NncUtils.map(entityType.getRecordComponents(), ReflectUtils::getField);
    }

    @Override
    protected RecordDef<T> createDef() {
        return new RecordDef<>(
                null,
                entityType,
                defMap.getPojoDef(entityType.getSuperclass()),
                type,
                defMap
        );
    }
}
