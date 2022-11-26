package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DefContext implements DefMap {
    private final Function<Object, IInstance> getInstance;
    private final Map<Type, ModelDef<?,?>> defMap = new HashMap<>();
    private final Map<Long, ModelDef<?, ?>> typeId2Def = new HashMap<>();
    private final Map<tech.metavm.object.meta.Type, ModelDef<?, ?>> type2Def = new HashMap<>();
    private final ValueDef<Object> objectDef;
    private final ValueDef<Enum<?>> enumDef;
    private final ModelMap modelMap;

    public DefContext(Function<Object, IInstance> getInstance, ModelMap modelMap) {
        this.getInstance = getInstance;
        StandardDefBuilder stdBuilder = new StandardDefBuilder();
        stdBuilder.initRootTypes(getInstance, defMap::put, modelMap);
        objectDef = stdBuilder.getObjectDef();
        enumDef = stdBuilder.getEnumDef();
        this.modelMap = modelMap;
    }

    @Override
    public ModelDef<?,?> getDef(Type entityType) {
        ModelDef<?,?> existing = defMap.get(entityType);
        if(existing != null) {
            return existing;
        }
        ModelDef<?,?> def = parseType(entityType);
        addDef(def);
        return def;
    }

    public EntityDef<?> getEntityDef(long typeId) {
        return (EntityDef<?>) getDef(typeId);
    }

    public EnumDef<?> getEnumDef(long typeId) {
        return (EnumDef<?>) getDef(typeId);
    }

    public EnumDef<?> getEnumDef(Class<? extends Enum<?>> enumType) {
        return (EnumDef<?>) getDef(enumType);
    }

    public ModelDef<?,?> getDef(long typeId) {
        return typeId2Def.get(typeId);
    }

    public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
        return type2Def.get(type);
    }

    private ModelDef<?,?> parseType(Type entityType) {
        TypeCategory typeCategory = ValueUtil.getTypeCategory(entityType);
        if(typeCategory.isArray()) {
            ParameterizedType pType = (ParameterizedType) entityType;
            return new ArrayDef<>(
                    getDef(pType.getActualTypeArguments()[0]),
                    objectDef,
                    getType(pType)
            );
        }
        else {
            Class<?> rawClass = ReflectUtils.getRawTypeRecursively(entityType);
            if (typeCategory.isEnum()) {
                Class<? extends Enum<?>> enumType = rawClass.asSubclass(new TypeReference<Enum<?>>() {
                }.getType());
                return EnumParser.parse(
                        enumType,
                        enumDef,
                        getInstance,
                        modelMap
                );
            }
            if (typeCategory.isEntity()) {
                return EntityParser.parse(
                        rawClass.asSubclass(Entity.class),
                        getInstance,
                        this,
                        modelMap
                );
            }
            if (typeCategory.isValue()) {
                if(Record.class.isAssignableFrom(rawClass)) {
                    return RecordParser.parse(
                            rawClass.asSubclass(Record.class), getInstance, this, modelMap
                    );
                }
                else {
                    return ValueParser.parse(
                            rawClass,
                            getInstance,
                            this,
                            modelMap
                    );
                }
            }
        }
        throw new InternalException("Can not parse definition for type: " + entityType);
    }

    private tech.metavm.object.meta.Type getType(Object javaConstruct) {
        IInstance instance = getInstance.apply(javaConstruct);
        return NncUtils.get(instance, modelMap::getType);
    }

    public void addDef(ModelDef<?, ?> def) {
        defMap.put(def.getGenericType(), def);
        type2Def.put(def.getType(), def);
    }

    @Override
    public void putDef(Type type, ModelDef<?,?> def) {
        defMap.put(type, def);
    }

}
