//package tech.metavm.entity;
//
//import tech.metavm.object.instance.Instance;
//import tech.metavm.object.instance.ArrayInstance;
//import tech.metavm.object.instance.ModelInstanceMap;
//import tech.metavm.object.instance.PrimitiveInstance;
//import tech.metavm.object.meta.Type;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.Table;
//import tech.metavm.util.TypeReference;
//
//import java.util.Map;
//
//public class TableDef<E> extends ModelDef<Table<E>, ArrayInstance> {
//
//    private final ModelDef<E, ?> elementDef;
//    private Long id;
//    private final Type type;
//
//    public TableDef(ModelDef<E, ?> elementDef,
//                    java.lang.reflect.Type reflectType,
//                    Type type) {
//        super(new TypeReference<Table<E>>() {}.getType(),
//                reflectType,
//                ArrayInstance.class);
//        this.elementDef = elementDef;
//        this.type = type;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    @Override
//    public Table<E> createModelProxy(Class<? extends Table<E>> proxyClass) {
//        return Table.createProxy(proxyClass, elementDef.getJavaType());
//    }
//
//    @Override
//    public void initModel(Table<E> table, ArrayInstance arrayInstance, ModelInstanceMap modelInstanceMap) {
//        table.addAll(
//                NncUtils.map(
//                        arrayInstance.getElements(),
//                        element -> getElementModel(element, modelInstanceMap)
//                )
//        );
//    }
//
//    @Override
//    public void updateModel(Table<E> model, ArrayInstance instance, ModelInstanceMap modelInstanceMap) {
//        model.clear();
//        initModel(model, instance, modelInstanceMap);
//    }
//
//    private E getElementModel(Instance elementInstance, ModelInstanceMap modelInstanceMap) {
//        if(elementInstance instanceof PrimitiveInstance primitiveInstance) {
//            return elementDef.getJavaClass().cast(primitiveInstance.getValue());
//        }
//        else {
//            return modelInstanceMap.getModel(elementDef.getJavaClass(), elementInstance);
//        }
//    }
//
//    @Override
//    public void initInstance(ArrayInstance instance, Table<E> table, ModelInstanceMap instanceMap) {
//        instance.addAll(NncUtils.map(table, instanceMap::getInstance));
//    }
//
//    @Override
//    public void updateInstance(ArrayInstance instance, Table<E> model, ModelInstanceMap instanceMap) {
//        instance.clear();
//        initInstance(instance, model, instanceMap);
//    }
//
//    @Override
//    public boolean isProxySupported() {
//        return true;
//    }
//
//    @Override
//    public Map<Object, Identifiable> getEntityMapping() {
//        return Map.of(getJavaType(), type);
//    }
//
//    @Override
//    public Type getType() {
//        return type;
//    }
//
//}
