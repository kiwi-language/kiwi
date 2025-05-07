package org.metavm.applab;

import org.jetbrains.annotations.Nullable;
import org.metavm.api.Entity;
import org.metavm.api.EntityFlow;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Map;
import java.util.function.Consumer;

@Entity(javaNative = true)
public class Car extends org.metavm.entity.Entity {

    private static Klass __klass__;

//    @EntityFlow
    public Car(Id id) {
        super(id);
    }

    @EntityFlow(javaNative = true)
    public String greet() {
        return "Hello";
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    protected void writeBody(MvOutput output) {

    }

    @Override
    protected void readBody(MvInput input, org.metavm.entity.Entity parent) {

    }

    @Override
    public int getEntityTag() {
        return (int) __klass__.getTag();
    }

    @Override
    protected void buildJson(Map<String, Object> map) {

    }

    @Override
    protected void buildSource(Map<String, Value> source) {

    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {

    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {

    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    public static void visitBody(StreamVisitor visitor) {
    }

}
