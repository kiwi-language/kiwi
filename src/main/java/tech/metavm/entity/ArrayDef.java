package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.TypeReference;

import java.util.Map;

public class ArrayDef<E> extends ModelDef<Table<E>, InstanceArray> {

    private final java.lang.reflect.Type reflectType;
    private final ModelDef<E, ?> elementDef;
    private final Type type;

    public ArrayDef(ModelDef<E, ?> elementDef,
                    java.lang.reflect.Type reflectType, Type type) {
        super(new TypeReference<>() {}, InstanceArray.class);
        this.elementDef = elementDef;
        this.reflectType = reflectType;
        this.type = type != null ? type : elementDef.getType().getArrayType();
    }

    @Override
    public Table<E> newModel(InstanceArray instanceArray, ModelMap modelMap) {
        return new Table<>(
                NncUtils.map(
                        instanceArray.getElements(),
                        element -> newElementModel(elementDef, element, modelMap)
                )
        );
    }

    @Override
    public void updateModel(Table<E> model, InstanceArray instance, ModelMap modelMap) {
        model.clear();
        for (IInstance element : instance.getElements()) {
            model.add(newElementModel(elementDef, element, modelMap));
        }
    }

    private <I extends Instance> E newElementModel(ModelDef<E, I> elementDef, IInstance element, ModelMap modelMap) {
        return elementDef.newModel(elementDef.getInstanceType().cast(element), modelMap);
    }

    @Override
    public InstanceArray newInstance(Table<E> table, InstanceMap instanceMap) {
        return new InstanceArray(
                type,
                Table.class,
                NncUtils.map(table, elementModel -> newElementInstance(elementModel, instanceMap)),
                false
        );
    }

    @Override
    public void updateInstance(Table<E> model, InstanceArray instance, InstanceMap instanceMap) {
        instance.clear();
        for (E elementModel : model) {
            instance.add(newElementInstance(elementModel, instanceMap));
        }
    }

    @Override
    public Map<Object, Entity> getEntityMapping() {
        return Map.of(reflectType, type);
    }

    private Instance newElementInstance(Object elementModel, InstanceMap instanceMap) {
        return elementDef.newInstance(elementDef.getEntityType().cast(elementModel), instanceMap);
    }

    @Override
    public Type getType() {
        return type;
    }

}
