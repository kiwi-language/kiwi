package tech.metavm.flow;

import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.object.type.*;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Nodes {

    // create raise node
    public static RaiseNode raise(String name, ScopeRT scope, Value message) {
        return new RaiseNode(null, name, null, scope.getLastNode(), scope,
                RaiseParameterKind.MESSAGE, null, message);
    }

    public static SelfNode self(String name, @Nullable String code, ClassType type, ScopeRT scope) {
        return new SelfNode(null, name, code, type, scope.getLastNode(), scope);
    }

    public static NewArrayNode newArray(String name, @Nullable String code, ArrayType type,
                                        @Nullable Value value, @Nullable ParentRef parentRef, ScopeRT scope) {
        return new NewArrayNode(null, name, code, type, value, parentRef, scope.getLastNode(), scope);
    }

    public static NewObjectNode newObject(String name, ClassType type, ScopeRT scope, Method constructor,
                                          List<Argument> arguments, boolean ephemeral, boolean unbound) {
        return new NewObjectNode(null, name, null,
                constructor, arguments, scope.getLastNode(), scope, null, ephemeral, unbound);
    }

    public static ReturnNode ret(String name, ScopeRT scope, @Nullable Value value) {
        return new ReturnNode(null, name, null, scope.getLastNode(), scope, value);
    }

    public static NodeRT forEach(
            String name,
            Supplier<Value> getArray, TriConsumer<ScopeRT, Supplier<Value>,
            Supplier<Value>> action,
            ScopeRT scope) {
        var seq = NncUtils.randomNonNegative();
        var whileOutputType = ClassTypeBuilder.newBuilder("循环输出", null)
                .temporary()
                .build();
        var indexField = FieldBuilder.newBuilder("索引", "index", whileOutputType, StandardTypes.getLongType())
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
                        Expressions.arrayLength(getArray.get().getExpression().copy())
                )
        ));
        var bodyScope = node.getBodyScope();
        var element = new GetElementNode(
                null, "数组元素_" + seq, "Element_" + seq, bodyScope.getLastNode(), bodyScope,
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
        var seq = NncUtils.randomNonNegative();
        var whileOutputType = ClassTypeBuilder.newBuilder("循环输出", null)
                .temporary()
                .build();
        var indexField = FieldBuilder.newBuilder("索引", "index", whileOutputType, StandardTypes.getLongType())
                .build();
        var list = getArray.get();
        var listType = (ClassType) list.getType();
        var size = new MethodCallNode(
                null,
                "列表大小_" + seq,
                null,
                StandardTypes.getLongType(),
                scope.getLastNode(),
                scope, list,
                listType.getMethodByCodeAndParamTypes("size", List.of()),
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
        var getMethod = ((ClassType )list.getType()).getMethodByCodeAndParamTypes("get", List.of(StandardTypes.getLongType()));
        var element = new MethodCallNode(
                null, "获取元素_" + seq, null,
                ((ClassType) list.getType()).getListElementType(),
                bodyScope.getLastNode(), bodyScope,
                getArray.get(), getMethod,
                List.of(Nodes.argument(getMethod, 0, Values.nodeProperty(node, indexField)))
        );
        action.accept(bodyScope, () -> Values.node(element), () -> Values.nodeProperty(node, indexField));
        return node;
    }

    public static MapNode map(String name, ScopeRT scope, Value source, ObjectMapping mapping) {
        return new MapNode(null, name, null, scope.getLastNode(), scope, source, mapping);
    }

    public static UnmapNode unmap(String name, ScopeRT scope, Value view, ObjectMapping mapping) {
        return new UnmapNode(null, name, null, scope.getLastNode(), scope, view, mapping);
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
        var mergeOutput = ClassTypeBuilder.newBuilder("MergeOutput", null).temporary().build();
        var mergeNode = new MergeNode(
                null, name + "_merge", NncUtils.get(code, c -> c + "_merge"),
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
                                                Function function, List<Argument> arguments, CompositeTypeFacade compositeTypeFacade) {
        var outputType = function.getReturnType().isVoid() ? null : Types.tryCapture(function.getReturnType(), function, compositeTypeFacade, null);
        return new FunctionCallNode(null, name, null, outputType, scope.getLastNode(), scope, function, arguments);
    }

    public static MethodCallNode methodCall(String name, ScopeRT scope,
                                            Value self, Method method, List<Argument> arguments, CompositeTypeFacade compositeTypeFacade) {
        var outputType = method.getReturnType().isVoid() ? null : Types.tryCapture(method.getReturnType(), method, compositeTypeFacade, null);
        return new MethodCallNode(null, name, null, outputType, scope.getLastNode(), scope, self, method, arguments);
    }

    public static FunctionNode function(String name, ScopeRT scope, Value function, List<Value> arguments) {
        return new FunctionNode(null, name, null, scope.getLastNode(), scope, function, arguments);
    }

    public static CastNode cast(String name, Type outputType, Value object, ScopeRT scope) {
        return new CastNode(null, name, null, outputType, scope.getLastNode(), scope, object);
    }

    public static InputNode input(Flow flow, CompositeTypeFacade compositeTypeFacade) {
        var inputType = ClassTypeBuilder.newBuilder("输入", null)
                .temporary()
                .build();
        for (var parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType,
                            Types.tryCapture(parameter.getType(), flow, compositeTypeFacade, null))
                    .build();
        }
        return new InputNode(
                null, "输入", "Input", inputType,
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
        return new Argument(null, flow.getParameters().get(index), value);
    }

    public static void setSource(Value view, Value source, ScopeRT scope) {
        var seq = NncUtils.randomNonNegative();
        var setSourceFunc = NativeFunctions.setSource();
        new FunctionCallNode(null, "设置来源_" + seq, "setSource_" + seq, null, scope.getLastNode(), scope,
                setSourceFunc, List.of(
                Nodes.argument(setSourceFunc, 0, view),
                Nodes.argument(setSourceFunc, 1, source)
        ));
    }

}
