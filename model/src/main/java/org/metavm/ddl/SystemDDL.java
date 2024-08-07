package org.metavm.ddl;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.ReadWriteArray;

import java.util.List;

@EntityType
public class SystemDDL extends Entity {

    @ChildEntity
    private final ReadWriteArray<FieldAddition> fieldAdditions = addChild(new ReadWriteArray<>(FieldAddition.class), "fieldAdditions");

    public SystemDDL(List<FieldAddition> fieldAdditions) {
        this.fieldAdditions.addAll(fieldAdditions);
    }

    public List<FieldAddition> getFieldAdditions() {
        return fieldAdditions.toList();
    }
}
