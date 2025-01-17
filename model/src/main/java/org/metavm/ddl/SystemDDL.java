package org.metavm.ddl;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(9)
@Entity
public class SystemDDL extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private List<FieldAddition> fieldAdditions = new ArrayList<>();
    private List<Reference> runMethods = new ArrayList<>();

    public SystemDDL(List<FieldAddition> fieldAdditions, List<Method> runMethods) {
        this.fieldAdditions.addAll(fieldAdditions);
        runMethods.forEach(m -> this.runMethods.add(m.getReference()));
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitList(() -> FieldAddition.visit(visitor));
        visitor.visitList(visitor::visitValue);
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
        fieldAdditions.forEach(arg -> arg.forEachReference(action));
        runMethods.forEach(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("fieldAdditions", this.getFieldAdditions().stream().map(FieldAddition::toJson).toList());
        map.put("runMethods", this.getRunMethods().stream().map(org.metavm.entity.Entity::getStringId).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_SystemDDL;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.fieldAdditions = input.readList(() -> FieldAddition.read(input));
        this.runMethods = input.readList(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeList(fieldAdditions, arg0 -> arg0.write(output));
        output.writeList(runMethods, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
