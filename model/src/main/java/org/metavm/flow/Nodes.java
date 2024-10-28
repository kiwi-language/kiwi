package org.metavm.flow;

import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.object.type.*;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.NncUtils;
import org.metavm.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Nodes {

    // create raise node
    public static RaiseNode raise(String name, ScopeRT scope, Value message) {
        return new RaiseNode(null, name, null, scope.getLastNode(), scope,
                RaiseParameterKind.MESSAGE, null, message);
    }

    public static SelfNode self(String name, Klass type, ScopeRT scope) {
        return new SelfNode(null, name, null, type.getType(), scope.getLastNode(), scope);
    }

    public static NewArrayNode newArray(String name, @Nullable String code, ArrayType type,
                                        @Nullable Value value, @Nullable ParentRef parentRef, ScopeRT scope) {
        return new NewArrayNode(null, name, code, type, value, null, parentRef, scope.getLastNode(), scope);
    }

    public static ArrayLengthNode arrayLength(String name, Value array, ScopeRT scope) {
        return new ArrayLengthNode(null, name, null, scope.getLastNode(), scope, array);
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
        var len = arrayLength("len", getArray.get(), scope);
        var index = Expressions.node(nodeProperty(join, indexField, scope));
        var ifNode = if_(scope.nextNodeName("if"),
                Values.node(Nodes.ge(index, Expressions.node(len), scope)), null, scope);
        var element = new GetElementNode(
                null, scope.nextNodeName("element"), null, scope.getLastNode(), scope,
                getArray.get(), Values.expression(index)
        );
        action.accept(scope, () -> Values.node(element), () -> Values.expression(index));
        var updatedIndex = Nodes.add(
                Expressions.node(nodeProperty(join, indexField, scope)),
                Expressions.constantLong(1L),
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
        var index = Expressions.node(nodeProperty(join, indexField, scope));

        var ifNode = if_(scope.nextNodeName("if"),
                Values.node(ge(index, Expressions.node(size), scope)),
                null,
                scope
        );
        var getMethod = listClass.getMethodByCodeAndParamTypes("get", List.of(Types.getLongType()));
        var element = new MethodCallNode(
                null, scope.nextNodeName("getElement"), null,
                scope.getLastNode(), scope,
                getArray.get(), getMethod.getRef(),
                List.of(Nodes.argument(getMethod, 0, Values.expression(index)))
        );
        action.accept(scope, () -> Values.node(element), () -> Values.expression(index));
        var updatedIndex = Nodes.add(
                Expressions.node(nodeProperty(join, indexField, scope)),
                Expressions.constantLong(1L),
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

    public static MethodCallNode methodCall(String name, ScopeRT scope,
                                            Value self, Method method, List<Argument> arguments) {
        return new MethodCallNode(null, name, null, scope.getLastNode(), scope, self, method.getRef(), arguments);
    }

    public static FunctionNode function(String name, ScopeRT scope, Value function, List<Value> arguments) {
        return new FunctionNode(null, name, null, scope.getLastNode(), scope, function, arguments);
    }

    public static CastNode cast(String name, Type outputType, Value object, ScopeRT scope) {
        return new CastNode(null, name, null, outputType, scope.getLastNode(), scope, object);
    }

    public static InputNode input(Flow flow) {
        return input(flow, "input", null);
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

    public static GotoNode goto_(String name, ScopeRT scope) {
        return new GotoNode(null, name, null, scope.getLastNode(), scope);
    }

    public static InputNode input(Flow flow, String name, String code) {
        var inputType = KlassBuilder.newBuilder("Input", null)
                .temporary()
                .tmpId(NncUtils.randomNonNegative())
                .build();
        for (var parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType,
                            Types.tryCapture(parameter.getType(), flow, null))
                    .build();
        }
        return new InputNode(
                null, name, code, inputType,
                flow.getRootScope().getLastNode(), flow.getRootScope()
        );
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

    public static NonNullNode nonNull(String name, Value value, ScopeRT scope) {
        return new NonNullNode(
                null, name, null, Types.getNonNullType(value.getType()), scope.getLastNode(), scope, value
        );
    }

    public static NoopNode noop(String name, ScopeRT scope) {
        return new NoopNode(null, name,null, scope.getLastNode(), scope);
    }

    public static LambdaExitNode lambdaExit(ScopeRT scope) {
        return new LambdaExitNode(
                null,
                scope.nextNodeName("lambdaExit"),
                null,
                scope.getLastNode(),
                scope
        );
    }

    public static NodeRT eq(Expression first, Expression second, ScopeRT scope) {
        return new EqNode(
                null,
                scope.nextNodeName("eq"),
                null,
                scope.getLastNode(),
                scope,
                Values.expression(first),
                Values.expression(second)
        );
    }

    public static NodeRT ne(Expression first, Expression second, ScopeRT scope) {
        return new NeNode(
                null,
                scope.nextNodeName("ne"),
                null,
                scope.getLastNode(),
                scope,
                Values.expression(first),
                Values.expression(second)
        );
    }

    public static NodeRT add(Expression first, Expression second, ScopeRT scope) {
        return new AddNode(
                null,
                scope.nextNodeName("add"),
                null,
                scope.getLastNode(),
                scope,
                Values.expression(first),
                Values.expression(second)
        );
    }

    public static NodeRT nodeProperty(NodeRT node, Property property, ScopeRT scope) {
        return getProperty(Expressions.node(node), property, scope);
    }

    public static NodeRT nodeProperty(String name, NodeRT node, Property property, ScopeRT scope) {
        return getProperty(name, Expressions.node(node), property, scope);
    }

    public static NodeRT getProperty(Expression instance, Property property, ScopeRT scope) {
        return getProperty(scope.nextNodeName("property"), instance, property, scope);
    }

    public static NodeRT inputField(InputNode node, int parameterIndex, ScopeRT scope) {
         return nodeProperty(node, node.getType().resolve().getFields().get(parameterIndex), scope);
    }

    public static NodeRT getProperty(String name,Expression instance, Property property, ScopeRT scope) {
        return new GetPropertyNode(
                null,
                name,
                null,
                scope.getLastNode(),
                scope,
                Values.expression(instance),
                property.getRef()
        );
    }
    private static NodeRT ge(Expression first, Expression second, ScopeRT scope) {
        return new GeNode(
                null,
                scope.nextNodeName("ge"),
                null,
                scope.getLastNode(),
                scope,
                Values.expression(first),
                Values.expression(second)
        );
    }

}
