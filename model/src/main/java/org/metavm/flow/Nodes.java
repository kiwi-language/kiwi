package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.*;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.Instances;
import org.metavm.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class Nodes {

    public static RaiseNode raiseWithMessage(ScopeRT scope) {
        var klass = StdKlass.runtimeException.get();
        var constructor = klass.resolveMethod(klass.getName(), List.of(Types.getNullableStringType()), List.of(), false);
        Nodes.newObject(scope, constructor, true, true);
        return raise(scope);
    }

    public static RaiseNode raise(ScopeRT scope) {
        return new RaiseNode(null, scope.nextNodeName("raise"), scope.getLastNode(), scope
        );
    }

    public static NewArrayNode newArray(ArrayType type, ScopeRT scope) {
        return new NewArrayNode(null, scope.nextNodeName("newArray"), type, scope.getLastNode(), scope);
    }

    public static ArrayLengthNode arrayLength(String name, ScopeRT scope) {
        return new ArrayLengthNode(null, name, scope.getLastNode(), scope);
    }

    public static GetElementNode getElement(ScopeRT scope) {
        return new GetElementNode(null, scope.nextNodeName("getElement"),
                scope.getLastNode(), scope);
    }

    public static NewObjectNode newObject(ScopeRT scope, Method constructor, boolean ephemeral, boolean unbound) {
        return new NewObjectNode(null, scope.nextNodeName("newObject"),
                constructor.getRef(), scope.getLastNode(), scope, ephemeral, unbound);
    }

    public static VoidReturnNode voidRet(ScopeRT scope) {
        return new VoidReturnNode(null, scope.nextNodeName("voidRet"), scope.getLastNode(), scope);
    }

    public static ReturnNode ret(ScopeRT scope) {
        return new ReturnNode(null, scope.nextNodeName("ret"), scope.getLastNode(), scope);
    }

    public static AddObjectNode addObject(ClassType type, boolean ephemeral, ScopeRT scope) {
        return new AddObjectNode(null, scope.nextNodeName("addObject"), ephemeral,
                type, scope.getLastNode(), scope);
    }

    public static void copyArray(Supplier<NodeRT> getSourceArray, Supplier<NodeRT> getTargetArray, ScopeRT scope) {
        forEach(getSourceArray,
                (bodyScope, getElement, getIndex) -> {
                    getTargetArray.get();
                    getElement.get();
                    Nodes.addElement(bodyScope);
                }, scope);
    }

    public static void forEach(Supplier<NodeRT> arraySupplier,
            TriConsumer<ScopeRT, Supplier<NodeRT>, Supplier<NodeRT>> action,
            ScopeRT scope) {
        var i = scope.nextVariableIndex();
        loadConstant(Instances.longInstance(0), scope);
        Nodes.store(i, scope);
        var entry = noop(scope.nextNodeName("noop"), scope);
        Supplier<NodeRT> indexSupplier = () -> Nodes.load(i, Types.getLongType(), scope);
        indexSupplier.get();
        arraySupplier.get();
        arrayLength("len", scope);
        Nodes.ge(scope);
        var ifNode = if_(null, scope);
        Supplier<NodeRT> elementSupplier = () -> {
            arraySupplier.get();
            indexSupplier.get();
            return Nodes.getElement(scope);
        };
        action.accept(scope, elementSupplier, indexSupplier);
        indexSupplier.get();
        loadConstant(Instances.longInstance(1), scope);
        add(scope);
        Nodes.store(i, scope);
        goto_(entry, scope);
        ifNode.setTarget(noop(scope));
    }

    public static void listForEach(
            Supplier<NodeRT> listSupplier,
            ClassType listType,
            TriConsumer<ScopeRT, Supplier<NodeRT>, Supplier<NodeRT>> action,
            ScopeRT scope) {
        var i = scope.nextVariableIndex();
        loadConstant(Instances.longInstance(0), scope);
        store(i, scope);
        var listClass = listType.resolve();
        var sizeMethod = listClass.getMethodByCodeAndParamTypes("size", List.of());
        var entry = noop(scope);
        Supplier<NodeRT> indexSupplier = () -> Nodes.load(i, Types.getLongType(), scope);
        indexSupplier.get();
        listSupplier.get();
        Nodes.methodCall(sizeMethod, scope);
        ge(scope);
        var ifNode = if_(null, scope);
        var getMethod = listClass.getMethodByCodeAndParamTypes("get", List.of(Types.getLongType()));
        Supplier<NodeRT> elementSupplier = () -> {
            listSupplier.get();
            indexSupplier.get();
            return Nodes.methodCall(getMethod, scope);
        };
        action.accept(scope, elementSupplier, indexSupplier);
        indexSupplier.get();
        loadConstant(Instances.longInstance(1), scope);
        Nodes.add(scope);
        Nodes.store(i, scope);
        goto_(entry, scope);
        ifNode.setTarget(noop(scope));
    }

    public static MapNode map(ScopeRT scope, ObjectMapping mapping) {
        return new MapNode(null, scope.nextNodeName("map"), scope.getLastNode(), scope, mapping.getRef());
    }

    public static UnmapNode unmap(ScopeRT scope, ObjectMapping mapping) {
        return new UnmapNode(null, scope.nextNodeName("unmap"), scope.getLastNode(), scope, mapping.getRef());
    }

    public static CastNode castNode(Type type, ScopeRT scope) {
        return new CastNode(null, scope.nextNodeName("cast"), type, scope.getLastNode(), scope);
    }

    public static FunctionCallNode functionCall(ScopeRT scope, Function function) {
        return new FunctionCallNode(null, scope.nextNodeName("functionCall"), scope.getLastNode(), scope, function.getRef());
    }

    public static MethodCallNode methodCall(Method method, ScopeRT scope) {
        return methodCall(scope.nextNodeName("methodCall") ,
                method,
                scope);
    }

    public static MethodCallNode methodCall(String name, Method method, ScopeRT scope) {
        return new MethodCallNode(null, name, scope.getLastNode(), scope, method.getRef());
    }

    public static FunctionNode function(ScopeRT scope, FunctionType functionType) {
        return new FunctionNode(null, scope.nextNodeName("func"), scope.getLastNode(), scope, functionType);
    }

    public static CastNode cast(Type outputType, ScopeRT scope) {
        return new CastNode(null, scope.nextNodeName("cast"), outputType, scope.getLastNode(), scope);
    }

    public static IfNode if_(@Nullable NodeRT target, ScopeRT scope) {
        return if_(scope.nextNodeName("if"), target, scope);
    }

    public static IfNode if_(String name,  @Nullable NodeRT target, ScopeRT scope) {
        return new IfNode(
                null,
                name,
                scope.getLastNode(),
                scope,
                target
        );
    }

    public static IfNotNode ifNot(@Nullable NodeRT target, ScopeRT scope) {
        return ifNot(scope.nextNodeName("ifNot"), target, scope);
    }

    public static IfNotNode ifNot(String name, @Nullable NodeRT target, ScopeRT scope) {
        return new IfNotNode(
                null,
                name,
                scope.getLastNode(),
                scope,
                target
        );
    }

    public static GotoNode goto_(ScopeRT scope) {
        return goto_(scope.nextNodeName("goto"), scope);
    }

    public static GotoNode goto_(String name, ScopeRT scope) {
        return new GotoNode(null, name, scope.getLastNode(), scope);
    }

    public static GotoNode goto_(NodeRT target, ScopeRT scope) {
        return new GotoNode(null, scope.nextNodeName("goto"), scope.getLastNode(), scope, target);
    }

    public static AddElementNode addElement(ScopeRT scope) {
        return new AddElementNode(null, scope.nextNodeName("arrayadd"), scope.getLastNode(), scope);
    }

    public static SetElementNode setElement(ScopeRT scope) {
        return new SetElementNode(null, scope.nextNodeName("arrayset"), scope.getLastNode(), scope);
    }

    public static ClearArrayNode clearArray(ScopeRT scope) {
        return new ClearArrayNode(null, scope.nextNodeName("arrayclear"), scope.getLastNode(), scope);
    }

//    public static void setSource(Value view, Value source, ScopeRT scope) {
//        var setSourceFunc = StdFunction.setSource.get();
//        new FunctionCallNode(null, scope.nextNodeName("setSource"), scope.getLastNode(), scope,
//                setSourceFunc.getRef(), List.of(
//                Nodes.argument(setSourceFunc, 0, view),
//                Nodes.argument(setSourceFunc, 1, source)
//        ));
//    }

    public static SetFieldNode setField(String name, Field field, ScopeRT scope) {
        return new SetFieldNode(
                null,
                name,
                scope.getLastNode(),
                scope,
                field.getRef()
        );
    }

    public static SetFieldNode setField(Field field, ScopeRT scope) {
        return setField(scope.nextNodeName("setField"),  field, scope);
    }

    public static SetStaticNode setStatic(Field field, ScopeRT scope) {
        return new SetStaticNode(
                null,
                scope.nextNodeName("setStatic"),

                scope.getLastNode(),
                scope,
                field.getRef()
        );
    }

    public static NonNullNode nonNull(ScopeRT scope) {
        return nonNull(scope.nextNodeName("nonnull"), scope);
    }

    public static NonNullNode nonNull(String name, ScopeRT scope) {
        return new NonNullNode(null, name, scope.getLastNode(), scope);
    }

    public static NoopNode noop(ScopeRT scope) {
        return noop(scope.nextNodeName("noop"), scope);
    }

    public static NoopNode noop(String name, ScopeRT scope) {
        return new NoopNode(null, name,scope.getLastNode(), scope);
    }

    public static NodeRT add(ScopeRT scope) {
        return new AddNode(
                null,
                scope.nextNodeName("add"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT sub(ScopeRT scope) {
        return new SubNode(
                null,
                scope.nextNodeName("sub"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT mul(ScopeRT scope) {
        return new MultiplyNode(
                null,
                scope.nextNodeName("mul"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT div(ScopeRT scope) {
        return new DivideNode(
                null,
                scope.nextNodeName("div"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT leftShift(ScopeRT scope) {
        return new LeftShiftNode(
                null,
                scope.nextNodeName("leftShift"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT rightShift(ScopeRT scope) {
        return new RightShiftNode(
                null,
                scope.nextNodeName("rightShift"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT unsignedRightShift(ScopeRT scope) {
        return new UnsignedRightShiftNode(
                null,
                scope.nextNodeName("unsignedRightShift"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT bitwiseOr(ScopeRT scope) {
        return new BitwiseOrNode(
                null,
                scope.nextNodeName("bitwiseOr"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT bitwiseAnd(ScopeRT scope) {
        return new BitwiseAndNode(
                null,
                scope.nextNodeName("bitwiseAnd"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT bitwiseXor(ScopeRT scope) {
        return new BitwiseXorNode(
                null,
                scope.nextNodeName("bitwiseXor"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT and(ScopeRT scope) {
        return new AndNode(
                null,
                scope.nextNodeName("and"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT or(ScopeRT scope) {
        return new OrNode(
                null,
                scope.nextNodeName("or"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT bitwiseComplement(ScopeRT scope) {
        return new BitwiseComplementNode(
                null,
                scope.nextNodeName("bitnot"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT not(ScopeRT scope) {
        return new NotNode(
                null,
                scope.nextNodeName("not"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT negate(ScopeRT scope) {
        return new NegateNode(
                null,
                scope.nextNodeName("negate"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT rem(ScopeRT scope) {
        return new RemainderNode(
                null,
                scope.nextNodeName("rem"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT eq(ScopeRT scope) {
        return new EqNode(
                null,
                scope.nextNodeName("eq"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT ne(ScopeRT scope) {
        return new NeNode(
                null,
                scope.nextNodeName("ne"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT ge(ScopeRT scope) {
        return new GeNode(
                null,
                scope.nextNodeName("ge"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT gt(ScopeRT scope) {
        return new GtNode(
                null,
                scope.nextNodeName("gt"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT lt(ScopeRT scope) {
        return new LtNode(
                null,
                scope.nextNodeName("lt"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT le(ScopeRT scope) {
        return new LeNode(
                null,
                scope.nextNodeName("le"),
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT instanceOf(Type targetType, ScopeRT scope) {
        return new InstanceOfNode(
                null,
                scope.nextNodeName("le"),
                scope.getLastNode(),
                scope,
                targetType
        );
    }

    public static NodeRT this_(ScopeRT scope) {
        var type = ((Method) scope.getFlow()).getDeclaringType().getType();
        return Nodes.load(0, type, scope);
    }

    public static NodeRT thisProperty(Property property, ScopeRT scope) {
        this_(scope);
        return getProperty(property, scope);
    }

    public static NodeRT getProperty(Property property, ScopeRT scope) {
        return getProperty(scope.nextNodeName("property"), property, scope);
    }

    public static NodeRT getProperty(String name,Property property, ScopeRT scope) {
        return new GetPropertyNode(
                null,
                name,
                scope.getLastNode(),
                scope,
                property.getRef()
        );
    }

    public static NodeRT getStatic(Property property, ScopeRT scope) {
        return new GetStaticNode(
                null,
                scope.nextNodeName("getStatic"),
                scope.getLastNode(),
                scope,
                property.getRef()
        );
    }

    public static NodeRT store(int index, ScopeRT scope) {
        return new StoreNode(
                null,
                scope.nextNodeName("store"),
                scope.getLastNode(),
                scope,
                index
        );
    }

    public static NodeRT argument(Callable callable, int index) {
        var i = callable instanceof Method method && !method.isStatic() ? index + 1 : index;
        return load(i, callable.getParameters().get(index).getType(), callable.getScope());
    }

    public static NodeRT load(int index, Type type, ScopeRT scope) {
        return new LoadNode(
                null,
                scope.nextNodeName("load"),
                type,
                scope.getLastNode(),
                scope,
                index
        );
    }

    public static NodeRT storeContextSlot(int contextIndex, int slotIndex, ScopeRT scope) {
        return new StoreContextSlotNode(
                null,
                scope.nextNodeName("storeContextSlot"),
                scope.getLastNode(),
                scope,
                contextIndex,
                slotIndex
        );
    }

    public static NodeRT loadContextSlot(int contextIndex, int slotIndex, Type type, ScopeRT scope) {
        return new LoadContextSlotNode(
                null,
                scope.nextNodeName("loadContextSlot"),
                type,
                scope.getLastNode(),
                scope,
                contextIndex,
                slotIndex
        );
    }

    public static NodeRT lambda(Lambda lambda, ScopeRT scope) {
        return new LambdaNode(
                null,
                scope.nextNodeName("lambda"),
                scope.getLastNode(),
                scope,
                lambda,
                null
        );
    }

    public static NodeRT select(Index index, ScopeRT scope) {
        return new IndexSelectNode(
                null,
                scope.nextNodeName("select"),
                scope.getLastNode(),
                scope,
                index
        );
    }

    public static NodeRT selectFirst(Index index, ScopeRT scope) {
        return new IndexSelectFirstNode(
                null,
                scope.nextNodeName("select"),
                scope.getLastNode(),
                scope,
                index
        );
    }

    public static NodeRT loadConstant(org.metavm.object.instance.core.Value value, ScopeRT scope) {
        return new LoadConstantNode(
                null,
                scope.nextNodeName("ldc"),
                scope.getLastNode(),
                scope,
                value
        );
    }

    public static NodeRT dup(ScopeRT scope) {
        return new DupNode(null, scope.nextNodeName("dup"), scope.getLastNode(), scope);
    }

    public static NodeRT dupX1(ScopeRT scope) {
        return new DupX1Node(null, scope.nextNodeName("dup_x1"), scope.getLastNode(), scope);
    }

    public static NodeRT dupX2(ScopeRT scope) {
        return new DupX2Node(null, scope.nextNodeName("dup_x2"), scope.getLastNode(), scope);
    }

    public static NodeRT pop(ScopeRT scope) {
        return new PopNode(null, scope.nextNodeName("pop"), scope.getLastNode(), scope);
    }
}
