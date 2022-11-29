package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.ValueUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

public abstract class PojoParser<T, D extends PojoDef<T>> {

    protected final Class<T> entityType;
    protected final Instance instance;
    protected final Type type;
    private D def;
    protected final Function<Object, Instance> getInstance;
    protected final DefMap defMap;
    protected final ModelMap modelMap;

    public PojoParser(Class<T> entityType,
                      Function<Object, Instance> getInstance,
                      DefMap defMap,
                      ModelMap modelMap
    ) {
        this.entityType = entityType;
        this.getInstance = getInstance;
        this.instance = getInstance.apply(entityType);
        type = NncUtils.get(instance, modelMap::getType);
        this.defMap = defMap;
        this.modelMap = modelMap;
    }

    protected D parse() {
        def = createDef();
        defMap.putDef(entityType, def);
        getFields(entityType).forEach(this::parseField);
        return def;
    }

    protected List<Field> getFields(Class<T> entityType) {
        return ReflectUtils.getDeclaredPersistentFields(entityType);
    }

    protected abstract D createDef();

    private void parseField(Field reflectField) {
        Instance fieldInst = getInstance.apply(reflectField);
        new FieldDef(
                null,
                NncUtils.get(fieldInst, modelMap::getField),
                reflectField,
                def,
                getFieldTargetDef(reflectField)
        );
    }

    private ModelDef<?, ?> getFieldTargetDef(Field reflectField) {
        TypeCategory typeCategory = ValueUtil.getTypeCategory(reflectField.getGenericType());
        if(typeCategory.isPrimitive()) {
            return null;
        }
        else {
            return defMap.getDef(reflectField.getType());
        }
    }

}
