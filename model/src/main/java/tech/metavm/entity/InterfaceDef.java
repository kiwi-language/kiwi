package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.type.ClassType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

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
    public void initModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {
        throw new UnsupportedOperationException();
    }

}
