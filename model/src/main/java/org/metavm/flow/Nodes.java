package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class Nodes {

    public static RaiseNode raiseWithMessage(Code code) {
        var klass = StdKlass.runtimeException.type();
        var constructor = klass.resolveMethod(klass.getName(), List.of(Types.getNullableStringType()), List.of(), false);
        Nodes.newObject(code, constructor, true, true);
        return raise(code);
    }

    public static RaiseNode raise(Code code) {
        return new RaiseNode(code.nextNodeName("raise"), code.getLastNode(), code
        );
    }

    public static NewArrayNode newArray(ArrayType type, Code code) {
        return new NewArrayNode(code.nextNodeName("newArray"), type, code.getLastNode(), code);
    }

    public static ArrayLengthNode arrayLength(String name, Code code) {
        return new ArrayLengthNode(name, code.getLastNode(), code);
    }

    public static GetElementNode getElement(Code code) {
        return new GetElementNode(code.nextNodeName("getElement"),
                code.getLastNode(), code);
    }

    public static NewObjectNode newObject(Code code, MethodRef constructor, boolean ephemeral, boolean unbound) {
        return new NewObjectNode(code.nextNodeName("newObject"),
                constructor, code.getLastNode(), code, ephemeral, unbound);
    }

    public static VoidReturnNode voidRet(Code code) {
        return new VoidReturnNode(code.nextNodeName("voidRet"), code.getLastNode(), code);
    }

    public static ReturnNode ret(Code code) {
        return new ReturnNode(code.nextNodeName("ret"), code.getLastNode(), code);
    }

    public static AddObjectNode addObject(ClassType type, boolean ephemeral, Code code) {
        return new AddObjectNode(code.nextNodeName("addObject"), ephemeral,
                type, code.getLastNode(), code);
    }

    public static void copyArray(Supplier<Node> getSourceArray, Supplier<Node> getTargetArray, Code code) {
        forEach(getSourceArray,
                (getElement, getIndex) -> {
                    getTargetArray.get();
                    getElement.get();
                    Nodes.addElement(code);
                }, code);
    }

    public static void forEach(Supplier<Node> arraySupplier,
                               BiConsumer<Supplier<Node>, Supplier<Node>> action,
                               Code code) {
        var i = code.nextVariableIndex();
        loadConstant(Instances.longInstance(0), code);
        Nodes.store(i, code);
        var entry = noop(code.nextNodeName("noop"), code);
        Supplier<Node> indexSupplier = () -> Nodes.load(i, Types.getLongType(), code);
        indexSupplier.get();
        arraySupplier.get();
        arrayLength("len", code);
        Nodes.ge(code);
        var ifNode = if_(null, code);
        Supplier<Node> elementSupplier = () -> {
            arraySupplier.get();
            indexSupplier.get();
            return Nodes.getElement(code);
        };
        action.accept(elementSupplier, indexSupplier);
        indexSupplier.get();
        loadConstant(Instances.longInstance(1), code);
        add(code);
        Nodes.store(i, code);
        goto_(entry, code);
        ifNode.setTarget(noop(code));
    }

    public static FunctionCallNode functionCall(Code code, Function function) {
        return new FunctionCallNode(code.nextNodeName("functionCall"), code.getLastNode(), code, function.getRef());
    }

    public static MethodCallNode methodCall(MethodRef method, Code code) {
        return methodCall(code.nextNodeName("methodCall"),
                method,
                code);
    }

    public static MethodCallNode methodCall(String name, MethodRef method, Code code) {
        return new MethodCallNode(name, code.getLastNode(), code, method);
    }

    public static FunctionNode function(Code code, FunctionType functionType) {
        return new FunctionNode(code.nextNodeName("func"), code.getLastNode(), code, functionType);
    }

    public static CastNode cast(Type outputType, Code code) {
        return new CastNode(code.nextNodeName("cast"), outputType, code.getLastNode(), code);
    }

    public static IfNode if_(@Nullable Node target, Code code) {
        return new IfNode(
                code.nextNodeName("if"),
                code.getLastNode(),
                code,
                target
        );
    }

    public static IfNotNode ifNot(@Nullable Node target, Code code) {
        return ifNot(code.nextNodeName("ifNot"), target, code);
    }

    public static IfNotNode ifNot(String name, @Nullable Node target, Code code) {
        return new IfNotNode(
                name,
                code.getLastNode(),
                code,
                target
        );
    }

    public static GotoNode goto_(Code code) {
        return goto_(code.nextNodeName("goto"), code);
    }

    public static GotoNode goto_(String name, Code code) {
        return new GotoNode(name, code.getLastNode(), code);
    }

    public static GotoNode goto_(Node target, Code code) {
        return new GotoNode(code.nextNodeName("goto"), code.getLastNode(), code, target);
    }

    public static AddElementNode addElement(Code code) {
        return new AddElementNode(code.nextNodeName("arrayadd"), code.getLastNode(), code);
    }

    public static SetElementNode setElement(Code code) {
        return new SetElementNode(code.nextNodeName("arrayset"), code.getLastNode(), code);
    }

    public static ClearArrayNode clearArray(Code code) {
        return new ClearArrayNode(code.nextNodeName("arrayclear"), code.getLastNode(), code);
    }

    public static SetFieldNode setField(FieldRef fieldRef, Code code) {
        return new SetFieldNode(
                code.nextNodeName("setField"),
                code.getLastNode(),
                code,
                fieldRef
        );

    }

    public static SetStaticNode setStatic(Field field, Code code) {
        return new SetStaticNode(
                code.nextNodeName("setStatic"),

                code.getLastNode(),
                code,
                field.getRef()
        );
    }

    public static NonNullNode nonNull(Code code) {
        return nonNull(code.nextNodeName("nonnull"), code);
    }

    public static NonNullNode nonNull(String name, Code code) {
        return new NonNullNode(name, code.getLastNode(), code);
    }

    public static NoopNode noop(Code code) {
        return noop(code.nextNodeName("noop"), code);
    }

    public static NoopNode noop(String name, Code code) {
        return new NoopNode(name, code.getLastNode(), code);
    }

    public static Node add(Code code) {
        return new AddNode(
                code.nextNodeName("add"),
                code.getLastNode(),
                code
        );
    }

    public static Node sub(Code code) {
        return new SubNode(
                code.nextNodeName("sub"),
                code.getLastNode(),
                code
        );
    }

    public static Node mul(Code code) {
        return new MulNode(
                code.nextNodeName("mul"),
                code.getLastNode(),
                code
        );
    }

    public static Node div(Code code) {
        return new DivNode(
                code.nextNodeName("div"),
                code.getLastNode(),
                code
        );
    }

    public static Node leftShift(Code code) {
        return new LeftShiftNode(
                code.nextNodeName("leftShift"),
                code.getLastNode(),
                code
        );
    }

    public static Node rightShift(Code code) {
        return new RightShiftNode(
                code.nextNodeName("rightShift"),
                code.getLastNode(),
                code
        );
    }

    public static Node unsignedRightShift(Code code) {
        return new UnsignedRightShiftNode(
                code.nextNodeName("unsignedRightShift"),
                code.getLastNode(),
                code
        );
    }

    public static Node bitOr(Code code) {
        return new BitOrNode(
                code.nextNodeName("bitor"),
                code.getLastNode(),
                code
        );
    }

    public static Node bitAnd(Code code) {
        return new BitAndNode(
                code.nextNodeName("bitand"),
                code.getLastNode(),
                code
        );
    }

    public static Node bitXor(Code code) {
        return new BitXorNode(
                code.nextNodeName("bitxor"),
                code.getLastNode(),
                code
        );
    }

    public static Node and(Code code) {
        return new AndNode(
                code.nextNodeName("and"),
                code.getLastNode(),
                code
        );
    }

    public static Node or(Code code) {
        return new OrNode(
                code.nextNodeName("or"),
                code.getLastNode(),
                code
        );
    }

    public static Node bitNot(Code code) {
        return new BitNotNode(
                code.nextNodeName("bitnot"),
                code.getLastNode(),
                code
        );
    }

    public static Node not(Code code) {
        return new NotNode(
                code.nextNodeName("not"),
                code.getLastNode(),
                code
        );
    }

    public static Node negate(Code code) {
        return new NegateNode(
                code.nextNodeName("negate"),
                code.getLastNode(),
                code
        );
    }

    public static Node rem(Code code) {
        return new RemainderNode(
                code.nextNodeName("rem"),
                code.getLastNode(),
                code
        );
    }

    public static Node eq(Code code) {
        return new EqNode(
                code.nextNodeName("eq"),
                code.getLastNode(),
                code
        );
    }

    public static Node ne(Code code) {
        return new NeNode(
                code.nextNodeName("ne"),
                code.getLastNode(),
                code
        );
    }

    public static Node ge(Code code) {
        return new GeNode(
                code.nextNodeName("ge"),
                code.getLastNode(),
                code
        );
    }

    public static Node gt(Code code) {
        return new GtNode(
                code.nextNodeName("gt"),
                code.getLastNode(),
                code
        );
    }

    public static Node lt(Code code) {
        return new LtNode(
                code.nextNodeName("lt"),
                code.getLastNode(),
                code
        );
    }

    public static Node le(Code code) {
        return new LeNode(
                code.nextNodeName("le"),
                code.getLastNode(),
                code
        );
    }

    public static Node instanceOf(Type targetType, Code code) {
        return new InstanceOfNode(
                code.nextNodeName("le"),
                code.getLastNode(),
                code,
                targetType
        );
    }

    public static Node this_(Code code) {
        var type = ((Method) code.getFlow()).getDeclaringType().getType();
        return Nodes.load(0, type, code);
    }

    public static Node thisProperty(PropertyRef propertyRef, Code code) {
        this_(code);
        return getProperty(propertyRef, code);
    }

    public static Node getProperty(PropertyRef propertyRef, Code code) {
        return getProperty(code.nextNodeName("property"), propertyRef, code);
    }

    public static Node getProperty(String name, PropertyRef propertyRef, Code code) {
        return new GetPropertyNode(
                name,
                code.getLastNode(),
                code,
                propertyRef
        );
    }

    public static Node getStatic(Property property, Code code) {
        return new GetStaticNode(
                code.nextNodeName("getStatic"),
                code.getLastNode(),
                code,
                property.getRef()
        );
    }

    public static Node store(int index, Code code) {
        return new StoreNode(
                code.nextNodeName("store"),
                code.getLastNode(),
                code,
                index
        );
    }

    public static Node argument(Callable callable, int index) {
        var i = callable instanceof Method method && !method.isStatic() ? index + 1 : index;
        return load(i, callable.getParameters().get(index).getType(), callable.getCode());
    }

    public static Node load(int index, Type type, Code code) {
        return new LoadNode(
                code.nextNodeName("load"),
                type,
                code.getLastNode(),
                code,
                index
        );
    }

    public static Node storeContextSlot(int contextIndex, int slotIndex, Code code) {
        return new StoreContextSlotNode(
                code.nextNodeName("storeContextSlot"),
                code.getLastNode(),
                code,
                contextIndex,
                slotIndex
        );
    }

    public static Node loadContextSlot(int contextIndex, int slotIndex, Type type, Code code) {
        return new LoadContextSlotNode(
                code.nextNodeName("loadContextSlot"),
                type,
                code.getLastNode(),
                code,
                contextIndex,
                slotIndex
        );
    }

    public static Node lambda(Lambda lambda, Code code) {
        return new LambdaNode(
                code.nextNodeName("lambda"),
                code.getLastNode(),
                code,
                lambda,
                null
        );
    }

    public static Node select(Index index, Code code) {
        return new IndexSelectNode(
                code.nextNodeName("select"),
                code.getLastNode(),
                code,
                index.getRef()
        );
    }

    public static Node selectFirst(Index index, Code code) {
        return new IndexSelectFirstNode(
                code.nextNodeName("select"),
                code.getLastNode(),
                code,
                index.getRef()
        );
    }

    public static Node loadConstant(org.metavm.object.instance.core.Value value, Code code) {
        return new LoadConstantNode(
                code.nextNodeName("ldc"),
                code.getLastNode(),
                code,
                value
        );
    }

    public static Node dup(Code code) {
        return new DupNode(code.nextNodeName("dup"), code.getLastNode(), code);
    }

    public static Node dupX1(Code code) {
        return new DupX1Node(code.nextNodeName("dup_x1"), code.getLastNode(), code);
    }

    public static Node dupX2(Code code) {
        return new DupX2Node(code.nextNodeName("dup_x2"), code.getLastNode(), code);
    }

    public static Node pop(Code code) {
        return new PopNode(code.nextNodeName("pop"), code.getLastNode(), code);
    }
}
