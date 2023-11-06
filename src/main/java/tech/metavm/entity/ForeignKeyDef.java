//package tech.metavm.entity;
//
//import tech.metavm.flow.NodeRT;
//import tech.metavm.flow.NodeStore;
//import tech.metavm.flow.ScopeRT;
//import tech.metavm.flow.ScopeStore;
//import tech.metavm.object.instance.core.Instance;
//import tech.metavm.object.instance.InstanceStore;
//import tech.metavm.object.meta.*;
//import tech.metavm.util.TypeReference;
//
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiFunction;
//
//public class ForeignKeyDef<T extends Entity> {
//
//    public static final ForeignKeyDef<Field> FIELD_DECLARING_TYPE =
//            new ForeignKeyDef<>(Field.class, (refIds, context) -> {
//                FieldStore fieldStore = (FieldStore) context.getEntityStore(Field.class);
//                return fieldStore.loadByDeclaringTypeIds(refIds, context);
//            });
//
//    public static final ForeignKeyDef<ConstraintRT<?>> CONSTRAINT_DECLARING_TYPE =
//            new ForeignKeyDef<>(
//                    new TypeReference<>() {},
//                    (refIds, context) -> {
//                        ConstraintStore nodeStore = (ConstraintStore) context.getEntityStore(ConstraintRT.class);
//                        return nodeStore.getByDeclaringTypeIds(refIds, context);
//                    }
//            );
//
//    public static final ForeignKeyDef<EnumConstantRT> ENUM_CONSTANT_TYPE =
//            new ForeignKeyDef<>(EnumConstantRT.class, (refIds, context) -> {
//                InstanceStore instanceStore = (InstanceStore) context.getEntityStore(Instance.class);
//                return instanceStore.getByTypeIdMap(refIds, context);
//            });
//
//    public static final ForeignKeyDef<ScopeRT> SCOPE_FLOW =
//            new ForeignKeyDef<>(ScopeRT.class, (refIds, context) -> {
//                ScopeStore scopeStore = (ScopeStore) context.getEntityStore(ScopeRT.class);
//                return scopeStore.getByFlowIds(refIds, context);
//            });
//
//    public static final ForeignKeyDef<NodeRT<?>> NODE_FLOW =
//            new ForeignKeyDef<>(
//                    new TypeReference<>() {},
//                    (refIds, context) -> {
//                        NodeStore nodeStore = (NodeStore) context.getEntityStore(NodeRT.class);
//                        return nodeStore.getByFlowIds(refIds, context);
//                    }
//            );
//
//    private final Class<T> entityType;
//    private final BiFunction<List<Long>, InstanceContext, Map<Long, List<Long>>> function;
//
//    private ForeignKeyDef(
//            Class<T> entityType,
//            BiFunction<List<Long>, InstanceContext, Map<Long, List<Long>>> function
//    ) {
//        this.entityType = entityType;
//        this.function = function;
//    }
//
//    private ForeignKeyDef(
//            TypeReference<T> typeReference,
//            BiFunction<List<Long>, InstanceContext, Map<Long, List<Long>>> function
//    ) {
//        this.entityType = typeReference.getType();
//        this.function = function;
//    }
//
//    public Class<T> getEntityType() {
//        return entityType;
//    }
//
//    public Map<Long, List<Long>> query(List<Long> refIds, InstanceContext context) {
//        return function.apply(refIds, context);
//    }
//
//
//}
