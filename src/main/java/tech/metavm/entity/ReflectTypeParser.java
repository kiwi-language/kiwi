//package tech.metavm.entity;
//
//import tech.metavm.object.instance.core.Instance;
//import tech.metavm.object.meta.ClassType;
//import tech.metavm.object.meta.TypeCategory;
//import tech.metavm.util.*;
//
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.lang.reflect.TypeVariable;
//import java.lang.reflect.WildcardType;
//import java.util.HashMap;
//import java.util.IdentityHashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//public class ReflectTypeParser implements TypeParser {
//
//    private final Map<Type, ModelDef<?,?>> javaType2Def = new HashMap<>();
//    private final IdentitySet<ClassType> initializedClassTypes = new IdentitySet<>();
//    private final IdentitySet<ModelDef<?,?>> processedDefSet = new IdentitySet<>();
//    private final Map<tech.metavm.object.meta.Type, ModelDef<?, ?>> type2Def = new IdentityHashMap<>();
//    private final AnyTypeDef<Object> objectDef;
//    private final ValueDef<Enum<?>> enumDef;
//    private final DefContext defContext;
//    private final IdentityContext identityContext = new IdentityContext(this::isClassTypeInitialized, this::getJavaType);
//    private final Function<Object, Long> getId;
//
//    public ReflectTypeParser(DefContext defContext, Function<Object, Long> getId) {
//        StandardDefBuilder stdBuilder = new StandardDefBuilder();
//        stdBuilder.initRootTypes(defContext);
//        this.defContext = defContext;
//        this.getId = getId;
//        objectDef = stdBuilder.getObjectDef();
//        enumDef = stdBuilder.getEnumDef();
//    }
//
//    @Override
//    public ClassType parse(String className) {
//        return (ClassType) parseType(ReflectUtils.classForName(className)).getType();
//    }
//
//    public ModelDef<?,?> getDef(Type javaType) {
//        checkJavaType(javaType);
//        javaType = ReflectUtils.getBoxedType(javaType);
////        TypeCategory typeCategory = ValueUtil.getTypeCategory(javaType);
////         TODO check and remove
////        if(!typeCategory.isArray()) {
////            javaType = ReflectUtils.getRawClass(javaType);
////        }
//        javaType = ReflectUtils.eraseType(javaType);
//        if(javaType instanceof Class<?> klass) {
//            javaType = EntityUtils.getRealType(klass);
//        }
//        ModelDef<?,?> existing = javaType2Def.get(javaType);
//        if(existing != null) {
//            return existing;
//        }
//        ModelDef<?,?> def = parseType(javaType);
//        if(!processedDefSet.contains(def)) {
//            addDef(def);
//        }
//        return def;
//    }
//
//    private ModelDef<?,?> parseType(Type genericType) {
//        DefParser<?,?,?> parser = getParser(genericType);
//        for (Type dependencyType : parser.getDependencyTypes()) {
//            getDef(dependencyType);
//        }
//        ModelDef<?,?> def;
//        if((def = javaType2Def.get(genericType)) != null) {
//            return def;
//        }
//        def = parser.create();
//        preAddDef(def);
//        parser.initialize();
//        afterDefInitialized(def);
//        return def;
//    }
//
//    public void addDef(ModelDef<?, ?> def) {
//        preAddDef(def);
//        afterDefInitialized(def);
//    }
//
//    private void afterDefInitialized(ModelDef<?,?> def) {
//        if(processedDefSet.contains(def)) {
//            return;
//        }
//        processedDefSet.add(def);
//        if(def.getType() instanceof ClassType classType) {
//            initializedClassTypes.add(classType);
//        }
//        Map<Object, ModelIdentity> identityMap = identityContext.getIdentityMap(def.getType());
//        identityMap.forEach((model, modelId) -> {
//            if((model instanceof IdInitializing idInitializing) && idInitializing.getId() == null) {
//                Long id = getId.apply(modelId);
//                if(id != null) {
//                    idInitializing.initId(id);
//                }
//            }
//            if(!defContext.containsModel(model)) {
//                defContext.addPendingModel(model);
//            }
//        });
//
//        def.getInstanceMapping().forEach((javaConstruct, instance) -> {
//            if(!instance.isValue() && instance.getId() == null) {
//                Long id = getId.apply(javaConstruct);
//                if(id != null) {
//                    instance.initId(id);
//                }
//            }
//            defContext.addMapping(javaConstruct, instance);
//        });
//        defContext.addInstanceMapping(def.getInstanceMapping());
//    }
//
//    public void preAddDef(ModelDef<?,?> def) {
//        ModelDef<?,?> existing = javaType2Def.get(def.getJavaType());
//        if(existing != null && existing != def) {
//            throw new InternalException("Def for java type " + def.getJavaType() + " already exists");
//        }
//        javaType2Def.put(def.getJavaType(), def);
//        if(!(def instanceof InstanceDef) && !(def instanceof InstanceCollectionDef<?,?>)) {
//            existing = type2Def.get(def.getType());
//            if(existing != null && existing != def) {
//                throw new InternalException("Def for type " + def.getType() + " already exists");
//            }
//            type2Def.put(def.getType(), def);
//        }
//    }
//
//    private void checkJavaType(Type javaType) {
//        if(javaType instanceof WildcardType && javaType instanceof TypeVariable<?>) {
//            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
//                    "Because it's either a wildcard type or a type variable");
//        }
//    }
//
//    public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
//        return NncUtils.requireNonNull(type2Def.get(type), () -> new InternalException("Can not find def for type " + type));
//    }
//
//    public Type getJavaType(tech.metavm.object.meta.Type type) {
//        return getDef(type).getJavaType();
//    }
//
//    public boolean isClassTypeInitialized(ClassType classType) {
//        return initializedClassTypes.contains(classType);
//    }
//
//    private DefParser<?,?,?> getParser(Type genericType) {
//        genericType = ReflectUtils.eraseType(genericType);
//        Class<?> rawClass = ReflectUtils.getRawClass(genericType);
//        TypeCategory typeCategory = ValueUtil.getTypeCategory(genericType);
//        if(Table.class.isAssignableFrom(rawClass)) {
//            Class<? extends Table<?>> collectionClass = rawClass.asSubclass(
//                    new TypeReference<Table<?>>(){}.getType()
//            );
//            if (genericType instanceof ParameterizedType pType) {
//                Type elementJavaType = pType.getActualTypeArguments()[0];
//                if((elementJavaType instanceof Class<?> elementJavaClass) &&
//                        Instance.class.isAssignableFrom(elementJavaClass)) {
//                    return new InstanceCollectionParser<>(
//                            genericType,
//                            collectionClass,
//                            elementJavaClass,
//                            objectDef.getType().getArrayType()
//                    );
//                }
//            }
//            return new CollectionParser<>(
//                    collectionClass,
//                    genericType,
//                    defContext
//            );
//        }
//        else if(Instance.class.isAssignableFrom(rawClass)) {
//            throw  new InternalException("Instance def should be predefined by StandardDefBuilder");
//        }
//        else {
//            if (typeCategory.isEnum()) {
//                Class<? extends Enum<?>> enumType = rawClass.asSubclass(new TypeReference<Enum<?>>() {
//                }.getType());
//                return new EnumParser<>(
//                        enumType,
//                        enumDef,
//                        defContext
//                );
//            }
//            if (typeCategory.isEntity()) {
//                return new EntityParser<>(
//                        rawClass.asSubclass(Entity.class),
//                        genericType,
//                        defContext
//                );
//            }
//            if (typeCategory.isValue()) {
//                if(Record.class.isAssignableFrom(rawClass)) {
//                    return new RecordParser<>(
//                            rawClass.asSubclass(Record.class), genericType, defContext
//                    );
//                }
//                else {
//                    return new ValueParser<>(
//                            rawClass,
//                            genericType,
//                            defContext
//                    );
//                }
//            }
//        }
//        throw new InternalException("Can not get def parser for type: " + genericType);
//    }
//
//}
