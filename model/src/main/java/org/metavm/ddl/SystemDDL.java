package org.metavm.ddl;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.ReadWriteArray;
import org.metavm.flow.Method;

import java.util.List;

@Entity
public class SystemDDL extends org.metavm.entity.Entity {

    @ChildEntity
    private final ReadWriteArray<FieldAddition> fieldAdditions = addChild(new ReadWriteArray<>(FieldAddition.class), "fieldAdditions");
    @ChildEntity
    private final ReadWriteArray<Method> runMethods = addChild(new ReadWriteArray<>(Method.class), "runMethods");

    public SystemDDL(List<FieldAddition> fieldAdditions, List<Method> runMethods) {
        this.fieldAdditions.addAll(fieldAdditions);
        this.runMethods.addAll(runMethods);
    }

    public List<FieldAddition> getFieldAdditions() {
        return fieldAdditions.toList();
    }

    public ReadWriteArray<Method> getRunMethods() {
        return runMethods;
    }
}
