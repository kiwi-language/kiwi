package tech.metavm.flow;

import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Nodes {

    public static SelfNode self(String name, @Nullable String code, ClassType type, ScopeRT scope) {
        return new SelfNode(null, name, code, type, scope.getLastNode(), scope);
    }

    public static NewArrayNode newArray(String name, @Nullable String code, ArrayType type,
                                        @Nullable Value value, @Nullable ParentRef parentRef, ScopeRT scope) {
        return new NewArrayNode(null, name, code, type, value, parentRef, scope.getLastNode(), scope);
    }

    public static ReturnNode ret(String name, @Nullable String code, ScopeRT scope, @Nullable Value value) {
        return new ReturnNode(null, name, code, scope.getLastNode(), scope, value);
    }

    public static NodeRT forEach(
            Supplier<Value> getArray, TriConsumer<ScopeRT, Supplier<Value>,
            Supplier<Value>> action,
            ScopeRT scope) {
        var seq = NncUtils.randomNonNegative();
        var name = "Foreach_" + seq;
        var code = "Foreach_" + seq;
        var whileOutputType = ClassTypeBuilder.newBuilder("循环输出", "LoopOutput")
                .temporary()
                .build();
        var indexField = FieldBuilder.newBuilder("索引", "index", whileOutputType, StandardTypes.getLongType())
                .build();
        var node = new WhileNode(
                null, name, code, whileOutputType, scope.getLastNode(), scope,
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

    public static BranchNode branch(String name, @Nullable String code, ScopeRT scope,
                                    Value condition, Consumer<Branch> thenGenerator, Consumer<Branch> elseGenerator,
                                    Consumer<MergeNode> processMerge) {
        var node = new BranchNode(null, name, code, false, scope.getLastNode(), scope);
        var thenBranch = node.addBranch(condition);
        thenGenerator.accept(thenBranch);
        var elseBranch = node.addDefaultBranch();
        elseGenerator.accept(elseBranch);
        var mergeOutput = ClassTypeBuilder.newBuilder("mergeOutput", "mergeOutput").temporary().build();
        var mergeNode = new MergeNode(
                null, name + "_merge", NncUtils.get(code, c -> c + "_merge"),
                node, mergeOutput, scope
        );
        processMerge.accept(mergeNode);
        return node;
    }

    public static InputNode input(Flow flow) {
        var inputType = ClassTypeBuilder.newBuilder("输入", "Input")
                .temporary()
                .build();
        for (var parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputType, parameter.getType())
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
        new FunctionCallNode(null, "设置来源_" + seq, "setSource_" + seq, scope.getLastNode(), scope,
                setSourceFunc, List.of(
                Nodes.argument(setSourceFunc, 0, view),
                Nodes.argument(setSourceFunc, 1, source)
        ));
    }

}
