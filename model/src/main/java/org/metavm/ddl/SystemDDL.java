package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Wire(9)
@Entity
public class SystemDDL extends org.metavm.entity.Entity {

    private final List<FieldAddition> fieldAdditions = new ArrayList<>();
    private final List<Reference> runMethods = new ArrayList<>();

    public SystemDDL(Id id, List<FieldAddition> fieldAdditions, List<Method> runMethods) {
        super(id);
        this.fieldAdditions.addAll(fieldAdditions);
        runMethods.forEach(m -> this.runMethods.add(m.getReference()));
    }

    public List<FieldAddition> getFieldAdditions() {
        return Collections.unmodifiableList(fieldAdditions);
    }

    public List<Method> getRunMethods() {
        return Utils.map(runMethods, r -> (Method) r.get());
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var fieldAdditions_ : fieldAdditions) fieldAdditions_.forEachReference(action);
        for (var runMethods_ : runMethods) action.accept(runMethods_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
