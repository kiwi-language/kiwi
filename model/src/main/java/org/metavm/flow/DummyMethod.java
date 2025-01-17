//package org.metavm.flow;
//
//import org.metavm.api.Entity;
//import org.metavm.entity.Attribute;
//import org.metavm.entity.natives.CallContext;
//import org.metavm.object.instance.core.InstanceVisitor;
//import org.metavm.object.instance.core.Value;
//import org.metavm.object.type.*;
//import org.metavm.object.type.Klass;
//import org.metavm.util.MvInput;
//import org.metavm.util.MvOutput;
//
//import javax.annotation.Nullable;
//import java.util.List;
//
//@Entity(ephemeral = true)
//public class DummyMethod extends Method {
//
//    public static final DummyMethod INSTANCE = new DummyMethod();
//    @SuppressWarnings("unused")
//    private static Klass __klass__;
//
//    private DummyMethod() {
//        super(
//                null,
//                DummyKlass.INSTANCE,
//                "<unnamed>",
//                false,
//                true,
//                false,
//                false,
//                List.of(),
//                Types.getVoidType(),
//                List.of(),
//                false,
//                Access.PUBLIC,
//                null,
//                false,
//                MetadataState.READY
//        );
//    }
//
//    @Override
//    public <R> R accept(InstanceVisitor<R> visitor) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String getInternalName(@Nullable Flow current) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public FlowExecResult execute(@Nullable Value self, List<? extends Value> arguments, FlowRef flowRef, CallContext callContext) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public MethodRef getRef() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable) {
//    }
//
//    @Override
//    public void addTypeParameter(TypeVariable typeParameter) {
//    }
//
//    @Override
//    public void addLambda(Lambda lambda) {
//    }
//
//    @Nullable
//    @Override
//    public FunctionType getStaticType() {
//        return new FunctionType(List.of(DummyKlass.INSTANCE.getType()), Types.getVoidType());
//    }
//
//    @Override
//    void addNode(Node node) {
//    }
//
//    @Override
//    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void writeBody(MvOutput output) {
//        throw new UnsupportedOperationException();
//    }
//
//    public List<Attribute> getAttributeArray() {
//        return attributes;
//    }
//
//    @Override
//    public ConstantPool getConstantPool() {
//        return new ConstantPool(this) {
//
//            @Override
//            public Klass getInstanceKlass() {
//                return __klass__;
//            }
//
//            @Override
//            public ClassType getInstanceType() {
//                return __klass__.getType();
//            }
//
//            @SuppressWarnings("unused")
//            private static Klass __klass__;
//
//            @Override
//            public int addValue(Object value) {
//                return 0;
//            }
//        };
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
