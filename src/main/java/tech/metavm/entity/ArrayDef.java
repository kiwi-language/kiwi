package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceArray;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import java.util.Map;

public class ArrayDef<E> extends ModelDef<Table<E>, InstanceArray> {

    private final java.lang.reflect.Type reflectType;
    private final ModelDef<E, ?> elementDef;
    private Long id;
    private final Type type;

    public ArrayDef(ModelDef<E, ?> elementDef,
                    java.lang.reflect.Type reflectType,
                    Long id) {
        super(new TypeReference<>() {}, InstanceArray.class);
        this.elementDef = elementDef;
        this.reflectType = reflectType;
        this.id = id;
        this.type = elementDef.getType().getArrayType();
        if(id != null && type.getId() == null) {
            type.initId(id);
        }
    }

    @Override
    public void initModel(Table<E> table, InstanceArray instanceArray, ModelInstanceMap modelInstanceMap) {
        table.initialize(
                NncUtils.map(
                        instanceArray.getElements(),
                        element -> {
                            if(element instanceof Instance instance) {
                                return modelInstanceMap.getModel(elementDef.getModelType(), instance);
                            }
                            else {
                                return elementDef.getModelType().cast(element);
                            }
                        }
                )
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Table<E> createModelProxy(Class<? extends Table<E>> proxyClass) {
        return ReflectUtils.invokeConstructor(ReflectUtils.getConstructor(proxyClass));
    }

    @Override
    public void updateModel(Table<E> model, InstanceArray instance, ModelInstanceMap modelInstanceMap) {
        model.clear();
        for (Object element : instance.getElements()) {
            E modelElement;
            if(element instanceof Instance elementInst) {
                modelElement = modelInstanceMap.getModel(elementDef.getModelType(), elementInst);
            }
            else {
                modelElement = elementDef.getModelType().cast(element);
            }
            model.add(modelElement);
        }
    }

    @Override
    public void initInstance(InstanceArray instanceArray, Table<E> table, ModelInstanceMap instanceMap) {
        instanceArray.initialize(NncUtils.map(table, instanceMap::getInstance));
    }

    @Override
    public void updateInstance(Table<E> model, InstanceArray instance, ModelInstanceMap instanceMap) {
        instance.clear();
        for (E elementModel : model) {
            instance.add(instanceMap.getInstance(elementModel));
        }
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of(reflectType, type);
    }

    @Override
    public Type getType() {
        return type;
    }

}
