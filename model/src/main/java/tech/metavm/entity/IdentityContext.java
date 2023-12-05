package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;
import tech.metavm.util.LinkedList;
import tech.metavm.util.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static tech.metavm.entity.EntityUtils.getRealType;
import static tech.metavm.util.NncUtils.allMatch;

public class IdentityContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityContext.class);

    private final Map<Object, ModelIdentity> model2identity = new IdentityHashMap<>();
    private final Map<ModelIdentity, Object> identity2model = new HashMap<>();
    private final Map<Object, List<Reference>> invertedIndex = new IdentityHashMap<>();
    private final Predicate<ClassType> isClassTypeInitialized;
    private final Function<tech.metavm.object.type.Type, Type> getJavaType;

    public IdentityContext(Predicate<ClassType> isClassTypeInitialized,
                           Function<tech.metavm.object.type.Type, Type> getJavaType) {
        this.isClassTypeInitialized = isClassTypeInitialized;
        this.getJavaType = getJavaType;
    }

    public ModelIdentity getModelId(Object model) {
        return model2identity.get(model);
    }

    public void putModelId(Object model, ModelIdentity modelId, Map<Object, ModelIdentity> tempMap) {
        model2identity.put(model, modelId);
        identity2model.put(modelId, model);
        tempMap.put(model, modelId);
    }

    public Object getModel(ModelIdentity identity) {
        return NncUtils.requireNonNull(
                identity2model.get(identity), "Can not find model for identity '" + identity + "'"
        );
    }

    public ModelIdentity getIdentity(Object model) {
        return model2identity.get(model);
    }

    public Map<Object, ModelIdentity> getIdentityMap(Object model) {
        Map<Object, ModelIdentity> modelIdMap = new IdentityHashMap<>();
        getIdentityMap0(model, modelIdMap, new LinkedList<>());
        return modelIdMap;
    }

    private void getIdentityMap0(Object model, Map<Object, ModelIdentity> result, LinkedList<String> path) {
        if (model2identity.containsKey(model) || ValueUtil.isPrimitive(model) || (model instanceof Instance)) {
            return;
        }
        if ((model instanceof ClassType classType)
                && classType.isFromReflection()
                && !isClassTypeInitialized.test(classType)) {
            return;
        }
        switch (model) {
            case GlobalKey globalKey -> putModelId(model,
                    new ModelIdentity(ReflectUtils.getType(model), globalKey.getKey(getJavaType)), result);
//            case tech.metavm.object.meta.Type type when isBuiltinType(type) ->
//                    putModelId(model, ModelIdentity.type(type, this::getJavaType), result);
//            case Field field when field.getDeclaringType().isFromReflection() ->
//                    putModelId(model, ModelIdentity.field(field, this::getJavaType, this::getJavaField), result);
//            case Flow flow when isBuiltinType(flow.getDeclaringType()) ->
//                    putModelId(model, new ModelIdentity(Flow.class, flow.getKey(getJavaType)), result);
//            case Parameter param
//                    when ((param.getCallable() instanceof Flow flow) && isBuiltinType(flow.getDeclaringType())) ->
//                    putModelId(param, new ModelIdentity(Parameter.class,
//                            flow.getKey(getJavaType) + ".parameters." + requireNonNull(param.getCode())), result);
//            case Index index when index.getDeclaringType().isFromReflection() ->
//                    putModelId(model, ModelIdentity.uniqueConstraint(getIndexDefField(index)), result);
//            case Column column -> putModelId(column, ModelIdentity.column(column), result);
//            case DynamicKey dynamicKey -> putModelId(dynamicKey,
//                    new ModelIdentity(ReflectUtils.getType(model), dynamicKey.getKey(getJavaType)), result);
            default -> {
                Reference ref = getIncomingReference(model);
                ModelIdentity sourceId = NncUtils.requireNonNull(
                        getModelId(ref.source()),
                        "Fail to create model id fro model '" + model + "', " +
                                "can not get model id of the source model '" + ref.source() + "'");
                putModelId(
                        model,
                        new ModelIdentity(ReflectUtils.getType(model), sourceId.name() + "." + ref.fieldName()),
                        result
                );
            }
        }
        if (model instanceof ReadonlyArray<?> array) {
            int index = 0;
            for (Object element : array) {
                if (element != null
                        && (array instanceof ChildArray<?> || element instanceof GlobalKey)) {
                    String childKey = getKey(element);
                    if (childKey == null) {
                        if (!(element instanceof GlobalKey)) {
                            LOGGER.warn(
                                    String.format("Child array element doesn't provide a key. Element: %s",
                                            element.getClass().getName())
                            );
                        }
                        childKey = index + "";
                    }
                    path.addLast(childKey);
                    addToInvertedIndex(array, childKey, element);
                    getIdentityMap0(element, result, path);
                    path.removeLast();
                }
                index++;
            }
        } else if (!ValueUtil.isEnumConstant(model)) {
            for (EntityProp prop : DescStore.get(model.getClass()).getNonTransientProps()) {
                Object fieldValue = prop.get(model);
                if (fieldValue != null
                        && (prop.hasAnnotation(ChildEntity.class) || fieldValue instanceof GlobalKey)) {
                    addToInvertedIndex(model, prop.getField().getName(), fieldValue);
                    path.addLast(prop.getName());
                    getIdentityMap0(fieldValue, result, path);
                    path.removeLast();
                }
            }
        }
    }

    private static String getKey(Object entity) {
        var desc = DescStore.get(entity.getClass());
        for (EntityProp prop : desc.getPropsWithAnnotation(EntityField.class)) {
            if (prop.getField().getAnnotation(EntityField.class).asKey()) {
                var value = prop.get(entity);
                if (value instanceof String str)
                    return str;
                else
                    throw new InternalException(
                            String.format("Invalid key for entity %s, expecting a string value but got %s",
                                    entity, value)
                    );
            }
        }
        return null;
    }

    private boolean isBuiltinType(tech.metavm.object.type.Type type) {
        return switch (type) {
            case PrimitiveType ignored1 -> true;
            case ObjectType ignored2 -> true;
            case NothingType ignored3 -> true;
            case ClassType classType when classType.isFromReflection() -> true;
            case ClassType classType when classType.getTemplate() != null -> isBuiltinType(classType.getTemplate())
                    && allMatch(classType.getTypeArguments(), this::isBuiltinType);
            case CompositeType compositeType -> allMatch(compositeType.getComponentTypes(), this::isBuiltinType);
            case TypeVariable typeVariable -> switch (typeVariable.getGenericDeclaration()) {
                case ClassType classType -> isBuiltinType(classType);
                case Flow flow -> isBuiltinType(flow.getDeclaringType());
                default -> throw new IllegalStateException("Unexpected value: " + typeVariable.getGenericDeclaration());
            };
            default -> false;
        };
    }

    private void addToInvertedIndex(Object source, String fieldName, Object target) {
        invertedIndex.computeIfAbsent(target, k -> new ArrayList<>()).add(
                new Reference(source, fieldName, target)
        );
    }

    private Reference getIncomingReference(Object model) {
        List<Reference> refs = invertedIndex.get(model);
        if (NncUtils.isEmpty(refs)) {
            throw new InternalException("Can not create an identifier for model '" + model +
                    "' because there's no reference to the model");
        }
//        if(refs.size() > 1) {
//            throw new InternalException("Can not create an identifier for model '" + model +
//                    "' because there are multiple references to the model: " + refs);
//        }
        return refs.get(0);
    }

    private java.lang.reflect.Field getIndexDefField(Index uniqueConstraint) {
        NncUtils.requireNonNull(uniqueConstraint.getIndexDef(),
                "Can not create model id for unique constraint '" + uniqueConstraint + " because" +
                        " indexDef is not present"
        );
        Class<?> javaClass = (Class<?>) getJavaType(uniqueConstraint.getDeclaringType());
        for (java.lang.reflect.Field indexDefField : EntityUtils.getIndexDefFields(javaClass)) {
            IndexDef<?> indexDef = (IndexDef<?>) ReflectUtils.get(null, indexDefField);
            if (indexDef == uniqueConstraint.getIndexDef()) {
                return indexDefField;
            }
        }
        throw new InternalException("Can not find a indexDef field for UniqueConstraint '" + uniqueConstraint + "'");
    }

    private java.lang.reflect.Field getJavaField(Field field) {
        var javaClass = getRawClass(field.getDeclaringType());
        return ReflectUtils.getDeclaredFieldByName(javaClass, field.getCodeRequired());
    }

    private Class<?> getRawClass(ClassType classType) {
        return NncUtils.requireNonNull(
                ReflectUtils.getRawClass(getJavaType(classType)),
                "Fail to get java type for type '" + classType + "'"
        );
    }

    private Type getJavaType(tech.metavm.object.type.Type type) {
        return getJavaType.apply(type);
    }

    public Map<Object, ModelIdentity> getIdentityMap() {
        return model2identity;
    }

}
