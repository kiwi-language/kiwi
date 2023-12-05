package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.type.ClassType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class InterfaceDef<T> extends PojoDef<T>  {

    private List<InterfaceDef<? super T>> superDefs;

    public InterfaceDef(Class<T> javaClass,
                     Type javaType,
                     @Nullable PojoDef<? super T> superDef,
                     ClassType type,
                     DefContext defContext
    ) {
        super(javaClass, javaType, superDef, type, defContext);
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    public void initModel(T model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(ClassInstance instance, T model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ModelInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of();
    }
}
