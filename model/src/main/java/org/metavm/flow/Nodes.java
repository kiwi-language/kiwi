package org.metavm.flow;

import org.metavm.entity.natives.NativeFunctions;
import org.metavm.expression.Expressions;
import org.metavm.object.type.*;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.NncUtils;
import org.metavm.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
        return new NewArrayNode(null, name, code, type, value, parentRef, scope.getLastNode(), scope);
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
        var whileOutputType = KlassBuilder.newBuilder("WhileOutput", null)
                .temporary()
                .build();
        var indexField = FieldBuilder.newBuilder("index", "index", whileOutputType, Types.getLongType())
                .build();
        var node = new WhileNode(
                null, name, null, whileOutputType, scope.getLastNode(), scope,
                Values.constant(Expressions.trueExpression())
        );
        node.setField(indexField, Values.constantLong(0L),
                Values.expression(
                        Expressions.add(
                                Expressions.nodeProperty(node, indexField),
                                Expressions.constantLong(1L)
                        )
                )
        );
        node.setCondition(Values.expression(
                Expressions.lt(
                        Expressions.nodeProperty(node, indexField),
                        Expressions.arrayLength(getArray.get().getExpression())
                )
        ));
        var bodyScope = node.getBodyScope();
        var element = new GetElementNode(
                null, scope.nextNodeName("Element"), null, bodyScope.getLastNode(), bodyScope,
                getArray.get(), Values.nodeProperty(node, indexField)
        );
        action.accept(bodyScope, () -> Values.node(element), () -> Values.nodeProperty(node, indexField));
        return node;
    }

    public static NodeRT listForEach(
            String name,
            Supplier<Value> getArray, TriConsumer<ScopeRT, Supplier<Value>,
            Supplier<Value>> action,
            ScopeRT scope) {
        var whileOutputType = KlassBuilder.newBuilder("WhileOutput", null)
                .temporary()
                .build();
        var indexField = FieldBuilder.newBuilder("index", "index", whileOutputType, Types.getLongType())
                .build();
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
        var node = new WhileNode(
                null, name, null, whileOutputType, scope.getLastNode(), scope,
                Values.constant(Expressions.trueExpression())
        );
        node.setField(indexField, Values.constantLong(0L),
                Values.expression(
                        Expressions.add(
                                Expressions.nodeProperty(node, indexField),
                                Expressions.constantLong(1L)
                        )
                )
        );
        node.setCondition(Values.expression(
                Expressions.lt(
                        Expressions.nodeProperty(node, indexField),
                        Expressions.node(size)
                )
        ));
        var bodyScope = node.getBodyScope();
        var getMethod = listClass.getMethodByCodeAndParamTypes("get", List.of(Types.getLongType()));
        var element = new MethodCallNode(
                null, scope.nextNodeName("getElement"), null,
                bodyScope.getLastNode(), bodyScope,
                getArray.get(), getMethod.getRef(),
                List.of(Nodes.argument(getMethod, 0, Values.nodeProperty(node, indexField)))
        );
        action.accept(bodyScope, () -> Values.node(element), () -> Values.nodeProperty(node, indexField));
        return node;
    }

    public static MapNode map(String name, ScopeRT scope, Value source, ObjectMapping mapping) {
        return new MapNode(null, name, null, scope.getLastNode(), scope, source, mapping.getRef());
    }

    public static UnmapNode unmap(String name, ScopeRT scope, Value view, ObjectMapping mapping) {
        return new UnmapNode(null, name, null, scope.getLastNode(), scope, view, mapping.getRef());
    }

    public static BranchNode branch(String name, @Nullable String code, ScopeRT scope,
                                    Value condition, Consumer<Branch> thenGenerator, Consumer<Branch> elseGenerator,
                                    Consumer<MergeNode> processMerge) {
        return branch(name, code, scope, List.of(condition), List.of(thenGenerator), elseGenerator, processMerge);
    }

    public static BranchNode branch(String name, @Nullable String code, ScopeRT scope,
                                    List<Value> conditions, List<Consumer<Branch>> branchGenerators,
                                    Consumer<Branch> defaultBranchGenerator,
                                    Consumer<MergeNode> processMerge) {
        var node = new BranchNode(null, name, code, false, scope.getLastNode(), scope);
        NncUtils.biForEach(conditions, branchGenerators, (cond, generator) -> {
            var thenBranch = node.addBranch(cond);
            generator.accept(thenBranch);
        });
        var elseBranch = node.addDefaultBranch();
        defaultBranchGenerator.accept(elseBranch);
        var mergeOutput = KlassBuilder.newBuilder("MergeOutput", null).temporary().build();
        var mergeNode = new MergeNode(
                null, scope.nextNodeName("merge"), null,
                node, mergeOutput, scope
        );
        processMerge.accept(mergeNode);
        return node;
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
        var setSourceFunc = NativeFunctions.setSource.get();
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

}
