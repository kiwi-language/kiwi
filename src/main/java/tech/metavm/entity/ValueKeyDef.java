//package tech.metavm.entity;
//
//import tech.metavm.object.instance.InstanceRelation;
//import tech.metavm.object.instance.RelationStore;
//
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiFunction;
//
//public class ValueKeyDef<T extends Value> {
//
//    public static final ValueKeyDef<InstanceRelation> RelationSource
//            = new ValueKeyDef<>(InstanceRelation.class, (refIds, context) -> {
//                RelationStore relationStore = (RelationStore) context.getValueStore(InstanceRelation.class);
//                return relationStore.getBySourceIds(refIds, context);
//    })
//
//    ;
//    private final Class<T> valueType;
//    private final BiFunction<List<Long>, InstanceContext, Map<Long, List<T>>> function;
//
//    private ValueKeyDef(Class<T> valueType, BiFunction<List<Long>, InstanceContext, Map<Long, List<T>>> function) {
//        this.valueType = valueType;
//        this.function = function;
//    }
//
//    @SuppressWarnings("unused")
//    public Class<T> getValueType() {
//        return valueType;
//    }
//
//    public Map<Long, List<T>> query(List<Long> refIds, InstanceContext context) {
//        return function.apply(refIds, context);
//    }
//
//}
