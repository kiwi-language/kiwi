package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.type.*;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.NncUtils;
import org.metavm.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class Nodes {

    // create raise node
    public static RaiseNode raise(String name, ScopeRT scope, Value message) {
        return new RaiseNode(null, name, null, scope.getLastNode(), scope,
                RaiseParameterKind.MESSAGE, null, message);
    }

    public static RaiseNode raise2(ScopeRT scope, Value exception) {
        return new RaiseNode(null, scope.nextNodeName("raise"), null, scope.getLastNode(), scope,
                RaiseParameterKind.THROWABLE, exception, null);
    }

    public static NewArrayNode newArray(String name, @Nullable String code, ArrayType type,
                                        @Nullable Value value, @Nullable ParentRef parentRef, ScopeRT scope) {
        return new NewArrayNode(null, name, code, type, value, null, parentRef, scope.getLastNode(), scope);
    }

    public static ArrayLengthNode arrayLength(String name, Value array, ScopeRT scope) {
        return new ArrayLengthNode(null, name, null, scope.getLastNode(), scope, array);
    }

    public static GetElementNode getElement(Value array, Value index, ScopeRT scope) {
        return new GetElementNode(null, scope.nextNodeName("getElement"), null,
                scope.getLastNode(), scope, array, index);
    }

    public static NewObjectNode newObject(String name, ScopeRT scope, Method constructor,
                                          List<Argument> arguments, boolean ephemeral, boolean unbound) {
        return new NewObjectNode(null, name, null,
                constructor.getRef(), arguments, scope.getLastNode(), scope, null, ephemeral, unbound);
    }

    public static ReturnNode ret(String name, ScopeRT scope, @Nullable Value value) {
        return new ReturnNode(null, name, null, scope.getLastNode(), scope, value);
    }

    public static NodeRT forEach(
            String name,
            Supplier<Value> getArray, TriConsumer<ScopeRT, Supplier<Value>,
            Supplier<Value>> action,
            ScopeRT scope) {
        var entry = noop(scope.nextNodeName("noop"), scope);
        var join = join(scope.nextNodeName(name), scope);
        var indexField = FieldBuilder.newBuilder("index", "index", join.getKlass(), Types.getLongType())
                .build();
        var len = Values.node(arrayLength("len", getArray.get(), scope));
        var index = Values.node(nodeProperty(join, indexField, scope));
        var ifNode = if_(scope.nextNodeName("if"),
                Values.node(Nodes.ge(index, len, scope)), null, scope);
        var element = new GetElementNode(
                null, scope.nextNodeName("element"), null, scope.getLastNode(), scope,
                getArray.get(), index
        );
        action.accept(scope, () -> Values.node(element), () -> index);
        var updatedIndex = Nodes.add(
                Values.node(nodeProperty(join, indexField, scope)),
                Values.constantLong(1L),
                scope
        );
        var g = goto_(scope.nextNodeName("goto"), scope);
        g.setTarget(join);
        new JoinNodeField(
                indexField, join,
                Map.of(entry, Values.constantLong(0L), g, Values.node(updatedIndex))
        );
        var exit = noop(scope.nextNodeName("noop"), scope);
        ifNode.setTarget(exit);
        return exit;
    }

    public static NodeRT listForEach(
            String name,
            Supplier<Value> getArray, TriConsumer<ScopeRT, Supplier<Value>,
            Supplier<Value>> action,
            ScopeRT scope) {
        var list = getArray.get();
        var listClass = ((ClassType) list.getType()).resolve();
        var methodRef = listClass.getMethodByCodeAndParamTypes("size", List.of()).getRef();
        var size = new MethodCallNode(
                null,
                scope.nextNodeName("listSize"),
                null,
                scope.getLastNode(),
                scope,
                list,
                methodRef,
                List.of()
        );
        var entry = noop(scope.nextNodeName("entry"), scope);
        var join = join(name, scope);
        var indexField = FieldBuilder.newBuilder("index", "index", join.getKlass(), Types.getLongType())
                .build();
        var index = Values.node(nodeProperty(join, indexField, scope));
        var ifNode = if_(scope.nextNodeName("if"),
                Values.node(ge(index, Values.node(size), scope)),
                null,
                scope
        );
        var getMethod = listClass.getMethodByCodeAndParamTypes("get", List.of(Types.getLongType()));
        var element = new MethodCallNode(
                null, scope.nextNodeName("getElement"), null,
                scope.getLastNode(), scope,
                getArray.get(), getMethod.getRef(),
                List.of(Nodes.argument(getMethod, 0, index))
        );
        action.accept(scope, () -> Values.node(element), () -> index);
        var updatedIndex = Nodes.add(
                Values.node(nodeProperty(join, indexField, scope)),
                Values.constantLong(1L),
                scope
        );
        var g = goto_(scope.nextNodeName("goto"), scope);
        g.setTarget(join);
        new JoinNodeField(indexField, join, Map.of(
                entry, Values.constantLong(0L), g, Values.node(updatedIndex)
        ));
        var exit = noop(scope.nextNodeName("noop"), scope);
        ifNode.setTarget(exit);
        return exit;
    }

    public static MapNode map(String name, ScopeRT scope, Value source, ObjectMapping mapping) {
        return new MapNode(null, name, null, scope.getLastNode(), scope, source, mapping.getRef());
    }

    public static UnmapNode unmap(String name, ScopeRT scope, Value view, ObjectMapping mapping) {
        return new UnmapNode(null, name, null, scope.getLastNode(), scope, view, mapping.getRef());
    }

    public static CastNode castNode(String name, Type type, ScopeRT scope, Value value) {
        return new CastNode(null, name, null, type, scope.getLastNode(), scope, value);
    }

    public static ValueNode value(String name, Value value, ScopeRT scope) {
        return new ValueNode(null, name, null, value.getType(), scope.getLastNode(), scope, value);
    }

    public static FunctionCallNode functionCall(String name, ScopeRT scope,
                                                Function function, List<Argument> arguments) {
        return new FunctionCallNode(null, name, null, scope.getLastNode(), scope, function.getRef(), arguments);
    }

    public static MethodCallNode methodCall(Value self, Method method, List<Value> arguments, ScopeRT scope) {
        return methodCall(scope.nextNodeName("methodCall") ,
                self,
                method,
                NncUtils.biMap(method.getParameters(), arguments, (p, a) -> new Argument(null, p.getRef(), a)),
                scope);
    }

    public static MethodCallNode methodCall(String name, Value self, Method method, List<Argument> arguments, ScopeRT scope) {
        return new MethodCallNode(null, name, null, scope.getLastNode(), scope, self, method.getRef(), arguments);
    }

    public static FunctionNode function(String name, ScopeRT scope, Value function, List<Value> arguments) {
        return new FunctionNode(null, name, null, scope.getLastNode(), scope, function, arguments);
    }

    public static CastNode cast(String name, Type outputType, Value object, ScopeRT scope) {
        return new CastNode(null, name, null, outputType, scope.getLastNode(), scope, object);
    }

    public static IfNode if_(String name, Value condition, @Nullable NodeRT target, ScopeRT scope) {
        return new IfNode(
                null,
                name,
                null,
                scope.getLastNode(),
                scope,
                condition,
                target
        );
    }

    public static IfNotNode ifNot(Value condition, @Nullable NodeRT target, ScopeRT scope) {
        return ifNot(scope.nextNodeName("ifNot"), condition, target, scope);
    }

    public static IfNotNode ifNot(String name, Value condition, @Nullable NodeRT target, ScopeRT scope) {
        return new IfNotNode(
                null,
                name,
                null,
                scope.getLastNode(),
                scope,
                condition,
                target
        );
    }

    public static JoinNode join( ScopeRT scope) {
        return join(scope.nextNodeName("join"), scope);
    }

    public static JoinNode join(String name, ScopeRT scope) {
        var klass = KlassBuilder.newBuilder("MergeOutput", null).temporary().build();
        return new JoinNode(
                null,
                name,
                null,
                klass,
                scope.getLastNode(),
                scope
        );
    }

    public static GotoNode goto_(ScopeRT scope) {
        return goto_(scope.nextNodeName("goto"), scope);
    }

    public static GotoNode goto_(String name, ScopeRT scope) {
        return new GotoNode(null, name, null, scope.getLastNode(), scope);
    }

    public static AddElementNode addElement(String name, @Nullable String code, Value array, Value element, ScopeRT scope) {
        return new AddElementNode(null, name, code, scope.getLastNode(), scope, array, element);
    }

    public static ClearArrayNode clearArray(String name, @Nullable String code, Value array, ScopeRT scope) {
        return new ClearArrayNode(null, name, code, scope.getLastNode(), scope, array);
    }

    public static Argument argument(Flow flow, int index, Value value) {
        return new Argument(null, flow.getParameters().get(index).getRef(), value);
    }

    public static void setSource(Value view, Value source, ScopeRT scope) {
        var setSourceFunc = StdFunction.setSource.get();
        new FunctionCallNode(null, scope.nextNodeName("setSource"), null, scope.getLastNode(), scope,
                setSourceFunc.getRef(), List.of(
                Nodes.argument(setSourceFunc, 0, view),
                Nodes.argument(setSourceFunc, 1, source)
        ));
    }

    public static UpdateObjectNode updateField(String name, Value self, Field field, Value value, ScopeRT scope) {
        return new UpdateObjectNode(
                null,
                name,
                null,
                scope.getLastNode(),
                scope,
                self,
                List.of(
                        new UpdateField(field.getRef(), UpdateOp.SET, value)
                )
        );
    }

    public static UpdateObjectNode update(String name, Value self, Map<Field, Value> updates, ScopeRT scope) {
        var fields = new ArrayList<UpdateField>();
        updates.forEach((field, value) -> fields.add(new UpdateField(field.getRef(), UpdateOp.SET, value)));
        return new UpdateObjectNode(
                null,
                name,
                null,
                scope.getLastNode(),
                scope,
                self,
                fields
        );
    }

    public static UpdateObjectNode update(Value self, Field field, Value value, ScopeRT scope) {
        return new UpdateObjectNode(
                null,
                scope.nextNodeName("update"),
                null,
                scope.getLastNode(),
                scope,
                self,
                List.of(new UpdateField(field.getRef(), UpdateOp.SET, value))
        );
    }

    public static UpdateStaticNode updateStatic(Field field, Value value, ScopeRT scope) {
        return new UpdateStaticNode(
                null,
                scope.nextNodeName("updateStatic"),
                null,
                scope.getLastNode(),
                scope,
                field.getDeclaringType(),
                List.of(
                        new UpdateField(field.getRef(), UpdateOp.SET, value)
                )
        );
    }

    public static NonNullNode nonNull(Value value, ScopeRT scope) {
        return nonNull(scope.nextNodeName("nonnull"), value, scope);
    }

    public static NonNullNode nonNull(String name, Value value, ScopeRT scope) {
        return new NonNullNode(
                null, name, null, Types.getNonNullType(value.getType()), scope.getLastNode(), scope, value
        );
    }

    public static NoopNode noop(ScopeRT scope) {
        return noop(scope.nextNodeName("noop"), scope);
    }

    public static NoopNode noop(String name, ScopeRT scope) {
        return new NoopNode(null, name,null, scope.getLastNode(), scope);
    }

    public static NodeRT add(Value first, Value second, ScopeRT scope) {
        return new AddNode(
                null,
                scope.nextNodeName("add"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT sub(Value first, Value second, ScopeRT scope) {
        return new SubNode(
                null,
                scope.nextNodeName("sub"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT mul(Value first, Value second, ScopeRT scope) {
        return new MultiplyNode(
                null,
                scope.nextNodeName("mul"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT div(Value first, Value second, ScopeRT scope) {
        return new DivideNode(
                null,
                scope.nextNodeName("div"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT leftShift(Value first, Value second, ScopeRT scope) {
        return new LeftShiftNode(
                null,
                scope.nextNodeName("leftShift"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT rightShift(Value first, Value second, ScopeRT scope) {
        return new RightShiftNode(
                null,
                scope.nextNodeName("rightShift"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT unsignedRightShift(Value first, Value second, ScopeRT scope) {
        return new UnsignedRightShiftNode(
                null,
                scope.nextNodeName("unsignedRightShift"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT bitwiseOr(Value first, Value second, ScopeRT scope) {
        return new BitwiseOrNode(
                null,
                scope.nextNodeName("bitwiseOr"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT bitwiseAnd(Value first, Value second, ScopeRT scope) {
        return new BitwiseAndNode(
                null,
                scope.nextNodeName("bitwiseAnd"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT bitwiseXor(Value first, Value second, ScopeRT scope) {
        return new BitwiseXorNode(
                null,
                scope.nextNodeName("bitwiseXor"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT and(Value first, Value second, ScopeRT scope) {
        return new AndNode(
                null,
                scope.nextNodeName("and"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT or(Value first, Value second, ScopeRT scope) {
        return new OrNode(
                null,
                scope.nextNodeName("or"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT bitwiseComplement(Value operand, ScopeRT scope) {
        return new BitwiseComplementNode(
                null,
                scope.nextNodeName("bitwiseComplement"),
                null,
                scope.getLastNode(),
                scope,
                operand
        );
    }

    public static NodeRT not(Value operand, ScopeRT scope) {
        return new NotNode(
                null,
                scope.nextNodeName("not"),
                null,
                scope.getLastNode(),
                scope,
                operand
        );
    }

    public static NodeRT negate(Value operand, ScopeRT scope) {
        return new NegateNode(
                null,
                scope.nextNodeName("negate"),
                null,
                scope.getLastNode(),
                scope,
                operand
        );
    }

    public static NodeRT rem(Value first, Value second, ScopeRT scope) {
        return new RemainderNode(
                null,
                scope.nextNodeName("rem"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT eq(Value first, Value second, ScopeRT scope) {
        return new EqNode(
                null,
                scope.nextNodeName("eq"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT ne(Value first, Value second, ScopeRT scope) {
        return new NeNode(
                null,
                scope.nextNodeName("ne"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT ge(Value first, Value second, ScopeRT scope) {
        return new GeNode(
                null,
                scope.nextNodeName("ge"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT gt(Value first, Value second, ScopeRT scope) {
        return new GtNode(
                null,
                scope.nextNodeName("gt"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT lt(Value first, Value second, ScopeRT scope) {
        return new LtNode(
                null,
                scope.nextNodeName("lt"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT le(Value first, Value second, ScopeRT scope) {
        return new LeNode(
                null,
                scope.nextNodeName("le"),
                null,
                scope.getLastNode(),
                scope,
                first,
                second
        );
    }

    public static NodeRT instanceOf(Value operand, Type targetType, ScopeRT scope) {
        return new InstanceOfNode(
                null,
                scope.nextNodeName("le"),
                null,
                scope.getLastNode(),
                scope,
                operand,
                targetType
        );
    }

    public static NodeRT this_(ScopeRT scope) {
        var type = ((Method) scope.getFlow()).getDeclaringType().getType();
        return Nodes.load(0, type, scope);
    }

    public static NodeRT thisProperty(Property property, ScopeRT scope) {
        return getProperty(
                Values.node(this_(scope)),
                property,
                scope
        );
    }

    public static NodeRT nodeProperty(NodeRT node, Property property, ScopeRT scope) {
        return getProperty(Values.node(node), property, scope);
    }

    public static NodeRT nodeProperty(String name, NodeRT node, Property property, ScopeRT scope) {
        return getProperty(name, Values.node(node), property, scope);
    }

    public static NodeRT getProperty(Value instance, Property property, ScopeRT scope) {
        return getProperty(scope.nextNodeName("property"), instance, property, scope);
    }

    public static NodeRT getProperty(String name,Value instance, Property property, ScopeRT scope) {
        return new GetPropertyNode(
                null,
                name,
                null,
                scope.getLastNode(),
                scope,
                instance,
                property.getRef()
        );
    }

    public static NodeRT getStatic(Property property, ScopeRT scope) {
        return new GetStaticNode(
                null,
                scope.nextNodeName("getStatic"),
                null,
                scope.getLastNode(),
                scope,
                property.getRef()
        );
    }

    public static NodeRT store(int index, Value value, ScopeRT scope) {
        return new StoreNode(
                null,
                scope.nextNodeName("store"),
                null,
                scope.getLastNode(),
                scope,
                index,
                value
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
                null,
                type,
                scope.getLastNode(),
                scope,
                index
        );
    }

    public static NodeRT storeContextSlot(int contextIndex, int slotIndex, Value value, ScopeRT scope) {
        return new StoreContextSlotNode(
                null,
                scope.nextNodeName("storeContextSlot"),
                null,
                scope.getLastNode(),
                scope,
                contextIndex,
                slotIndex,
                value
        );
    }

    public static NodeRT loadContextSlot(int contextIndex, int slotIndex, Type type, ScopeRT scope) {
        return new LoadContextSlotNode(
                null,
                scope.nextNodeName("loadContextSlot"),
                null,
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
                null,
                scope.getLastNode(),
                scope,
                lambda,
                null
        );
    }

    public static NodeRT select(Index index, IndexQueryKey key, ScopeRT scope) {
        return new IndexSelectNode(
                null,
                scope.nextNodeName("select"),
                null,
                index.getDeclaringType().getType(),
                scope.getLastNode(),
                scope,
                index,
                key
        );
    }

    public static NodeRT selectFirst(Index index, IndexQueryKey key, ScopeRT scope) {
        return new IndexSelectFirstNode(
                null,
                scope.nextNodeName("select"),
                null,
                scope.getLastNode(),
                scope,
                index,
                key
        );
    }

}
