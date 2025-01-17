//package org.metavm.object.type;
//
//import org.metavm.api.Entity;
//import org.metavm.object.type.Klass;
//
//import java.util.List;
//
//@Entity(ephemeral = true)
//public class DummyIndex extends Index {
//
//    public static final DummyIndex INSTANCE = new DummyIndex();
//    @SuppressWarnings("unused")
//    private static Klass __klass__;
//
//    private DummyIndex() {
//        super(DummyKlass.INSTANCE, "unnamed", "", false, List.of(), null);
//    }
//
//    @Override
//    void addField(IndexField item) {
//    }
//
//    @Override
//    public ClassType getInstanceType() {
//        return __klass__.getType();
//    }
//
//    @Override
//    public Klass getInstanceKlass() {
//        return __klass__;
//    }
//}
