package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.*;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class Nodes {

    public static RaiseNode raiseWithMessage(Code code) {
        var klass = StdKlass.runtimeException.get();
        var constructor = klass.resolveMethod(klass.getName(), List.of(Types.getNullableStringType()), List.of(), false);
        Nodes.newObject(code, constructor, true, true);
        return raise(code);
    }

    public static RaiseNode raise(Code code) {
        return new RaiseNode(null, code.nextNodeName("raise"), code.getLastNode(), code
        );
    }

    public static NewArrayNode newArray(ArrayType type, Code code) {
        return new NewArrayNode(null, code.nextNodeName("newArray"), type, code.getLastNode(), code);
    }

    public static ArrayLengthNode arrayLength(String name, Code code) {
        return new ArrayLengthNode(null, name, code.getLastNode(), code);
    }

    public static GetElementNode getElement(Code code) {
        return new GetElementNode(null, code.nextNodeName("getElement"),
                code.getLastNode(), code);
    }

    public static NewObjectNode newObject(Code code, Method constructor, boolean ephemeral, boolean unbound) {
        return new NewObjectNode(null, code.nextNodeName("newObject"),
                constructor.getRef(), code.getLastNode(), code, ephemeral, unbound);
    }

    public static VoidReturnNode voidRet(Code code) {
        return new VoidReturnNode(null, code.nextNodeName("voidRet"), code.getLastNode(), code);
    }

    public static ReturnNode ret(Code code) {
        return new ReturnNode(null, code.nextNodeName("ret"), code.getLastNode(), code);
    }

    public static AddObjectNode addObject(ClassType type, boolean ephemeral, Code code) {
        return new AddObjectNode(null, code.nextNodeName("addObject"), ephemeral,
                type, code.getLastNode(), code);
    }

    public static void copyArray(Supplier<NodeRT> getSourceArray, Supplier<NodeRT> getTargetArray, Code code) {
        forEach(getSourceArray,
                (getElement, getIndex) -> {
                    getTargetArray.get();
                    getElement.get();
                    Nodes.addElement(code);
                }, code);
    }

    public static void forEach(Supplier<NodeRT> arraySupplier,
            BiConsumer<Supplier<NodeRT>, Supplier<NodeRT>> action,
            Code code) {
        var i = code.nextVariableIndex();
        loadConstant(Instances.longInstance(0), code);
        Nodes.store(i, code);
        var entry = noop(code.nextNodeName("noop"), code);
        Supplier<NodeRT> indexSupplier = () -> Nodes.load(i, Types.getLongType(), code);
        indexSupplier.get();
        arraySupplier.get();
        arrayLength("len", code);
        Nodes.ge(code);
        var ifNode = if_(null, code);
        Supplier<NodeRT> elementSupplier = () -> {
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

    public static void listForEach(
            Supplier<NodeRT> listSupplier,
            ClassType listType,
            BiConsumer<Supplier<NodeRT>, Supplier<NodeRT>> action,
            Code code) {
        var i = code.nextVariableIndex();
        loadConstant(Instances.longInstance(0), code);
        store(i, code);
        var listClass = listType.resolve();
        var sizeMethod = listClass.getMethodByNameAndParamTypes("size", List.of());
        var entry = noop(code);
        Supplier<NodeRT> indexSupplier = () -> Nodes.load(i, Types.getLongType(), code);
        indexSupplier.get();
        listSupplier.get();
        Nodes.methodCall(sizeMethod, code);
        ge(code);
        var ifNode = if_(null, code);
        var getMethod = listClass.getMethodByNameAndParamTypes("get", List.of(Types.getLongType()));
        Supplier<NodeRT> elementSupplier = () -> {
            listSupplier.get();
            indexSupplier.get();
            return Nodes.methodCall(getMethod, code);
        };
        action.accept(elementSupplier, indexSupplier);
        indexSupplier.get();
        loadConstant(Instances.longInstance(1), code);
        Nodes.add(code);
        Nodes.store(i, code);
        goto_(entry, code);
        ifNode.setTarget(noop(code));
    }

    public static MapNode map(Code code, ObjectMapping mapping) {
        return new MapNode(null, code.nextNodeName("map"), code.getLastNode(), code, mapping.getRef());
    }

    public static UnmapNode unmap(Code code, ObjectMapping mapping) {
        return new UnmapNode(null, code.nextNodeName("unmap"), code.getLastNode(), code, mapping.getRef());
    }

    public static CastNode castNode(Type type, Code code) {
        return new CastNode(null, code.nextNodeName("cast"), type, code.getLastNode(), code);
    }

    public static FunctionCallNode functionCall(Code code, Function function) {
        return new FunctionCallNode(null, code.nextNodeName("functionCall"), code.getLastNode(), code, function.getRef());
    }

    public static MethodCallNode methodCall(Method method, Code code) {
        return methodCall(code.nextNodeName("methodCall") ,
                method,
                code);
    }

    public static MethodCallNode methodCall(String name, Method method, Code code) {
        return new MethodCallNode(null, name, code.getLastNode(), code, method.getRef());
    }

    public static FunctionNode function(Code code, FunctionType functionType) {
        return new FunctionNode(null, code.nextNodeName("func"), code.getLastNode(), code, functionType);
    }

    public static CastNode cast(Type outputType, Code code) {
        return new CastNode(null, code.nextNodeName("cast"), outputType, code.getLastNode(), code);
    }

    public static IfNode if_(@Nullable NodeRT target, Code code) {
        return if_(code.nextNodeName("if"), target, code);
    }

    public static IfNode if_(String name,  @Nullable NodeRT target, Code code) {
        return new IfNode(
                null,
                name,
                code.getLastNode(),
                code,
                target
        );
    }

    public static IfNotNode ifNot(@Nullable NodeRT target, Code code) {
        return ifNot(code.nextNodeName("ifNot"), target, code);
    }

    public static IfNotNode ifNot(String name, @Nullable NodeRT target, Code code) {
        return new IfNotNode(
                null,
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
        return new GotoNode(null, name, code.getLastNode(), code);
    }

    public static GotoNode goto_(NodeRT target, Code code) {
        return new GotoNode(null, code.nextNodeName("goto"), code.getLastNode(), code, target);
    }

    public static AddElementNode addElement(Code code) {
        return new AddElementNode(null, code.nextNodeName("arrayadd"), code.getLastNode(), code);
    }

    public static SetElementNode setElement(Code code) {
        return new SetElementNode(null, code.nextNodeName("arrayset"), code.getLastNode(), code);
    }

    public static ClearArrayNode clearArray(Code code) {
        return new ClearArrayNode(null, code.nextNodeName("arrayclear"), code.getLastNode(), code);
    }

    public static SetFieldNode setField(String name, Field field, Code code) {
        return new SetFieldNode(
                null,
                name,
                code.getLastNode(),
                code,
                field.getRef()
        );
    }

    public static SetFieldNode setField(Field field, Code code) {
        return setField(code.nextNodeName("setField"),  field, code);
    }

    public static SetStaticNode setStatic(Field field, Code code) {
        return new SetStaticNode(
                null,
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
        return new NonNullNode(null, name, code.getLastNode(), code);
    }

    public static NoopNode noop(Code code) {
        return noop(code.nextNodeName("noop"), code);
    }

    public static NoopNode noop(String name, Code code) {
        return new NoopNode(null, name, code.getLastNode(), code);
    }

    public static NodeRT add(Code code) {
        return new AddNode(
                null,
                code.nextNodeName("add"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT sub(Code code) {
        return new SubNode(
                null,
                code.nextNodeName("sub"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT mul(Code code) {
        return new MulNode(
                null,
                code.nextNodeName("mul"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT div(Code code) {
        return new DivNode(
                null,
                code.nextNodeName("div"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT leftShift(Code code) {
        return new LeftShiftNode(
                null,
                code.nextNodeName("leftShift"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT rightShift(Code code) {
        return new RightShiftNode(
                null,
                code.nextNodeName("rightShift"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT unsignedRightShift(Code code) {
        return new UnsignedRightShiftNode(
                null,
                code.nextNodeName("unsignedRightShift"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT bitOr(Code code) {
        return new BitOrNode(
                null,
                code.nextNodeName("bitor"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT bitAnd(Code code) {
        return new BitAndNode(
                null,
                code.nextNodeName("bitand"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT bitXor(Code code) {
        return new BitXorNode(
                null,
                code.nextNodeName("bitxor"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT and(Code code) {
        return new AndNode(
                null,
                code.nextNodeName("and"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT or(Code code) {
        return new OrNode(
                null,
                code.nextNodeName("or"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT bitNot(Code code) {
        return new BitNotNode(
                null,
                code.nextNodeName("bitnot"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT not(Code code) {
        return new NotNode(
                null,
                code.nextNodeName("not"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT negate(Code code) {
        return new NegateNode(
                null,
                code.nextNodeName("negate"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT rem(Code code) {
        return new RemainderNode(
                null,
                code.nextNodeName("rem"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT eq(Code code) {
        return new EqNode(
                null,
                code.nextNodeName("eq"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT ne(Code code) {
        return new NeNode(
                null,
                code.nextNodeName("ne"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT ge(Code code) {
        return new GeNode(
                null,
                code.nextNodeName("ge"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT gt(Code code) {
        return new GtNode(
                null,
                code.nextNodeName("gt"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT lt(Code code) {
        return new LtNode(
                null,
                code.nextNodeName("lt"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT le(Code code) {
        return new LeNode(
                null,
                code.nextNodeName("le"),
                code.getLastNode(),
                code
        );
    }

    public static NodeRT instanceOf(Type targetType, Code code) {
        return new InstanceOfNode(
                null,
                code.nextNodeName("le"),
                code.getLastNode(),
                code,
                targetType
        );
    }

    public static NodeRT this_(Code code) {
        var type = ((Method) code.getFlow()).getDeclaringType().getType();
        return Nodes.load(0, type, code);
    }

    public static NodeRT thisProperty(Property property, Code code) {
        this_(code);
        return getProperty(property, code);
    }

    public static NodeRT getProperty(Property property, Code code) {
        return getProperty(code.nextNodeName("property"), property, code);
    }

    public static NodeRT getProperty(String name,Property property, Code code) {
        return new GetPropertyNode(
                null,
                name,
                code.getLastNode(),
                code,
                property.getRef()
        );
    }

    public static NodeRT getStatic(Property property, Code code) {
        return new GetStaticNode(
                null,
                code.nextNodeName("getStatic"),
                code.getLastNode(),
                code,
                property.getRef()
        );
    }

    public static NodeRT store(int index, Code code) {
        return new StoreNode(
                null,
                code.nextNodeName("store"),
                code.getLastNode(),
                code,
                index
        );
    }

    public static NodeRT argument(Callable callable, int index) {
        var i = callable instanceof Method method && !method.isStatic() ? index + 1 : index;
        return load(i, callable.getParameters().get(index).getType(), callable.getCode());
    }

    public static NodeRT load(int index, Type type, Code code) {
        return new LoadNode(
                null,
                code.nextNodeName("load"),
                type,
                code.getLastNode(),
                code,
                index
        );
    }

    public static NodeRT storeContextSlot(int contextIndex, int slotIndex, Code code) {
        return new StoreContextSlotNode(
                null,
                code.nextNodeName("storeContextSlot"),
                code.getLastNode(),
                code,
                contextIndex,
                slotIndex
        );
    }

    public static NodeRT loadContextSlot(int contextIndex, int slotIndex, Type type, Code code) {
        return new LoadContextSlotNode(
                null,
                code.nextNodeName("loadContextSlot"),
                type,
                code.getLastNode(),
                code,
                contextIndex,
                slotIndex
        );
    }

    public static NodeRT lambda(Lambda lambda, Code code) {
        return new LambdaNode(
                null,
                code.nextNodeName("lambda"),
                code.getLastNode(),
                code,
                lambda,
                null
        );
    }

    public static NodeRT select(Index index, Code code) {
        return new IndexSelectNode(
                null,
                code.nextNodeName("select"),
                code.getLastNode(),
                code,
                index
        );
    }

    public static NodeRT selectFirst(Index index, Code code) {
        return new IndexSelectFirstNode(
                null,
                code.nextNodeName("select"),
                code.getLastNode(),
                code,
                index
        );
    }

    public static NodeRT loadConstant(org.metavm.object.instance.core.Value value, Code code) {
        return new LoadConstantNode(
                null,
                code.nextNodeName("ldc"),
                code.getLastNode(),
                code,
                value
        );
    }

    public static NodeRT dup(Code code) {
        return new DupNode(null, code.nextNodeName("dup"), code.getLastNode(), code);
    }

    public static NodeRT dupX1(Code code) {
        return new DupX1Node(null, code.nextNodeName("dup_x1"), code.getLastNode(), code);
    }

    public static NodeRT dupX2(Code code) {
        return new DupX2Node(null, code.nextNodeName("dup_x2"), code.getLastNode(), code);
    }

    public static NodeRT pop(Code code) {
        return new PopNode(null, code.nextNodeName("pop"), code.getLastNode(), code);
    }
}
