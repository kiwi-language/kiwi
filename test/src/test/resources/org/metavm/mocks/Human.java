package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(84)
@Entity
public class Human extends Animal {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private String occupation;

    public Human(Id id, long age, long intelligence, String occupation) {
        super(id, age, intelligence);
        this.occupation = occupation;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Animal.visitBody(visitor);
        visitor.visitUTF();
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("occupation", this.getOccupation());
        map.put("intelligence", this.getIntelligence());
        map.put("age", this.getAge());
        map.put("extraInfo", this.getExtraInfo().toJson());
        map.put("offsprings", this.getOffsprings().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("ancestors", this.getAncestors().stream().map(org.metavm.entity.Entity::getStringId).toList());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Human;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.occupation = input.readUTF();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeUTF(occupation);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
