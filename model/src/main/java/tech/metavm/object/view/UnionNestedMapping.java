package tech.metavm.object.view;

import tech.metavm.entity.ChildArray;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.InstanceOfExpression;
import tech.metavm.flow.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.UnionType;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Supplier;

@EntityType
public class UnionNestedMapping extends NestedMapping {

    private final UnionType sourceType;
    private final UnionType targetType;
    @ChildEntity
    private final ChildArray<NestedMapping> memberNestedMappings =
            addChild(new ChildArray<>(NestedMapping.class), "memberNestedMappings");

    public UnionNestedMapping(UnionType sourceType, UnionType targetType, List<NestedMapping> nestedMappings) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.memberNestedMappings.resetChildren(nestedMappings);
    }

    private static Type getViewType(Type sourceType, UnionType targetUnionType) {
        return NncUtils.findRequired(targetUnionType.getMembers(), sourceType::isViewType);
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        Map<Type, Type> viewType2sourceType = new HashMap<>();
        for (Type member : sourceType.getMembers()) {
            viewType2sourceType.put(getViewType(member, targetType), member);
        }
        Map<Type, NestedMapping> sourceType2codeGenerator = new HashMap<>();
        for (var codeGenerator : memberNestedMappings) {
            var viewType = codeGenerator.getTargetType();
            sourceType2codeGenerator.put(viewType2sourceType.get(viewType), codeGenerator);
        }
        var values = new HashMap<Branch, Value>();
        var valueFieldRef = new Object() {
            Field valueField;
        };
        var source = Nodes.value(scope.nextNodeName("source"), getSource.get(), scope);
        Nodes.branch(
                scope.nextNodeName("checkType"),
                null,
                scope,
                NncUtils.map(
                        sourceType.getMembers(),
                        t -> Values.expression(
                                new InstanceOfExpression(getSource.get().getExpression(), t)
                        )
                ),
                NncUtils.map(
                        sourceType.getMembers(),
                        t -> branch -> {
                            var castSource = Nodes.castNode(scope.nextNodeName("castSource"),
                                    t, branch.getScope(), Values.node(source));
                            values.put(
                                    branch,
                                    sourceType2codeGenerator.get(t).generateMappingCode(() -> Values.node(castSource), branch.getScope()).get()
                            );
                        }
                ),
                branch -> Nodes.raise(scope.nextNodeName("invalidTypeError"), branch.getScope(), Values.constantString("Invalid type")),
                mergeNode -> {
                    valueFieldRef.valueField = FieldBuilder.newBuilder("value", null, mergeNode.getType().resolve(), targetType).build();
                    new MergeNodeField(valueFieldRef.valueField, mergeNode, values);
                }
        );
        return () -> Values.nodeProperty(scope.getLastNode(), valueFieldRef.valueField);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
        var targetType2codeGenerator = new HashMap<Type, NestedMapping>();
        for (var memberCodeGenerator : memberNestedMappings) {
            targetType2codeGenerator.put(memberCodeGenerator.getTargetType(), memberCodeGenerator);
        }
        var values = new HashMap<Branch, Value>();
        var valueFieldRef = new Object() {
            Field valueField;
        };
        var view = Nodes.value(scope.nextNodeName("view"), getView.get(), scope);
        Nodes.branch(
                scope.nextNodeName("checkType"),
                null,
                scope,
                NncUtils.map(
                        targetType.getMembers(),
                        t -> Values.expression(
                                new InstanceOfExpression(getView.get().getExpression(), t)
                        )
                ),
                NncUtils.map(
                        targetType.getMembers(),
                        t -> branch -> {
                            var castView =  Nodes.cast(scope.nextNodeName("castView"), t, Values.node(view), branch.getScope());
                            values.put(
                                    branch,
                                    targetType2codeGenerator.get(t).generateUnmappingCode(() -> Values.node(castView), branch.getScope()).get()
                            );
                        }
                ),
                branch -> Nodes.raise(scope.nextNodeName("invalidTypeError"), branch.getScope(), Values.constantString("invalid type")),
                mergeNode -> {
                    valueFieldRef.valueField = FieldBuilder.newBuilder("value", null, mergeNode.getType().resolve(), sourceType).build();
                    new MergeNodeField(valueFieldRef.valueField, mergeNode, values);
                }
        );
        var mergeNode = scope.getLastNode();
        return () -> Values.nodeProperty(mergeNode, valueFieldRef.valueField);
    }

    @Override
    public Type getTargetType() {
        return targetType;
    }

    @Override
    public String getText() {
        return "{\"kind\": \"Union\", \"sourceType\": \"" + sourceType.getTypeDesc() + "\", \"targetType\": \"" + targetType.getTypeDesc()
                + "\", \"memberMappings\": [" + NncUtils.join(memberNestedMappings, NestedMapping::getText) + "]}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UnionNestedMapping that)) return false;
        return Objects.equals(sourceType, that.sourceType) && Objects.equals(targetType, that.targetType) &&
                Objects.equals(Set.of(memberNestedMappings), Set.of(that.memberNestedMappings));
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType, Set.of(memberNestedMappings));
    }
}
