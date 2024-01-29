package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.view.rest.dto.FieldMappingDTO;
import tech.metavm.object.view.rest.dto.FieldMappingParam;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType("字段映射")
public abstract class FieldMapping extends Element {

    @EntityField("目标字段")
    private final Field targetField;

    @EntityField("所属映射")
    protected final FieldsObjectMapping containingMapping;

    @EntityField("嵌套映射")
    @Nullable
    protected ObjectMapping nestedMapping;

    public FieldMapping(Long tmpId, Field targetField, FieldsObjectMapping containingMapping, @Nullable ObjectMapping nestedMapping) {
        super(tmpId);
        this.containingMapping = containingMapping;
        this.targetField = targetField;
        this.nestedMapping = nestedMapping;
        containingMapping.addField(this);
    }

    public Field getTargetField() {
        return targetField;
    }

    public void setTargetFieldType(Type type) {
        targetField.setType(type);
    }

    public boolean isReadonly() {
        return targetField.isReadonly();
    }

    public String getName() {
        return targetField.getName();
    }

    public @Nullable String getCode() {
        return targetField.getCode();
    }

    public void setName(String name) {
        targetField.setName(name);
    }

    public void setCode(String code) {
        targetField.setCode(code);
    }

    public abstract FieldMapping getCopySource();

    public boolean isChild() {
        return targetField.isChild();
    }

    public FieldMappingDTO toDTO(SerializeContext serializeContext) {
        return new FieldMappingDTO(
                tryGetId(),
                serializeContext.getTmpId(this),
                getName(),
                getCode(),
                serializeContext.getRef(getType()),
                isChild(),
                isReadonly(),
                NncUtils.get(getSourceField(), serializeContext::getRef),
                serializeContext.getRef(targetField),
                NncUtils.get(nestedMapping, serializeContext::getRef),
                getParam(serializeContext)
        );
    }

    public abstract @Nullable Field getSourceField();

    public abstract FieldMappingParam getParam(SerializeContext serializeContext);

    public FieldsObjectMapping getContainingMapping() {
        return containingMapping;
    }

    public Type getType() {
        return targetField.getType();
    }

    public void setReadonly(boolean readonly) {
        targetField.setReadonly(readonly);
    }

    public FieldParam generateReadCode(SelfNode selfNode) {
        var valueSupplier = generateReadCode0(selfNode);
        var value = valueSupplier.get();
        if (value.getType() != targetField.getType()) {
//            var nestedFieldView = new FunctionCallNode(
//                    null,
//                    targetField.getName() + "嵌套视图",
//                    NncUtils.get(targetField.getCode(), c -> c + "NestedView"),
//                    selfNode.getScope().getLastNode(),
//                    selfNode.getScope(),
//                    nestedMapping.getMapper(),
//                    List.of(Nodes.argument(nestedMapping.getMapper(), 0, value))
//            );
//            var nestedFieldView = new MapNode(
//                    null,
//                    targetField.getName() + "嵌套视图",
//                    NncUtils.get(targetField.getCode(), c -> c + "NestedView"),
//                    selfNode.getScope().getLastNode(),
//                    selfNode.getScope(),
//                    value,
//                    nestedMapping
//            );
            var getNestedValue = generateNestedMappingCode(value.getType(), targetField.getType(), valueSupplier, selfNode.getScope());
            return new FieldParam(targetField, getNestedValue.get());
        } else
            return new FieldParam(targetField, value);
    }


    private Supplier<Value> generateNestedMappingCode(Type sourceType, Type targetType,
                                                      Supplier<Value> getValue,
                                                      ScopeRT scope) {
        if (sourceType == targetType)
            return getValue;
        if (sourceType instanceof ArrayType arraySourceType && targetType instanceof ArrayType arrayTargetType)
            return generateArrayNestedMappingCode(arraySourceType, arrayTargetType, getValue, scope);
        else if (sourceType instanceof ClassType && targetType instanceof ClassType)
            return generateClassNestedMappingCode(getValue, scope);
        throw new InternalException("Invalid mapping from '" + sourceType.getName() + "' to '" + targetType.getName() + "'");
    }

    private Supplier<Value> generateClassNestedMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        var mapNode = Nodes.map("嵌套映射", scope, getSource.get(),
                Objects.requireNonNull(nestedMapping, "nested mapping required"));
        return () -> Values.node(mapNode);
    }

    private Supplier<Value> generateArrayNestedMappingCode(ArrayType sourceType, ArrayType targetType,
                                                           Supplier<Value> getSource, ScopeRT scope) {
        var sourceElementType = sourceType.getElementType();
        var targetElementType = targetType.getElementType();
        var targetArray = Nodes.newArray(
                targetType.getName() + "数组",
                null,
                targetType,
                null,
                null,
                scope
        );
        var setSourceFunc = NativeFunctions.setSource();
        Nodes.functionCall(
                "设置来源" + targetType.getName(),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetArray)),
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                )
        );
        Nodes.forEach(
                "遍历" + sourceType.getName(),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = generateNestedMappingCode(sourceElementType, targetElementType, getElement, bodyScope);
                    Nodes.addElement(
                            "添加" + sourceElementType.getName(),
                            null,
                            Values.node(targetArray),
                            getTargetElement.get(),
                            bodyScope
                    );
                },
                scope
        );
        return () -> Values.node(targetArray);
    }

    protected abstract Supplier<Value> generateReadCode0(SelfNode selfNode);

    public void generateWriteCode(SelfNode selfNode, ValueNode viewNode) {
        if (nestedMapping != null) {
            var scope = selfNode.getScope();
            var nestedFieldSource = generateNestedUnmappingCode(getTargetFieldType(), targetField.getType(),
                    () -> Values.nodeProperty(viewNode, targetField), scope);
            generateWriteCode0(selfNode, nestedFieldSource);
        } else
            generateWriteCode0(selfNode, () -> Values.nodeProperty(viewNode, targetField));
    }

    private Supplier<Value> generateNestedUnmappingCode(Type sourceType, Type targetType, Supplier<Value> getView, ScopeRT scope) {
        if (sourceType instanceof ArrayType arraySourceType && targetType instanceof ArrayType arrayTargetType)
            return generateArrayNestedUnmappingCode(arraySourceType, arrayTargetType, getView, scope);
        else if (sourceType instanceof ClassType classSourceType && targetType instanceof ClassType classTargetType)
            return generateClassNestedUnmappingCode(classSourceType, classTargetType, getView, scope);
        throw new InternalException("Can not unmap from '" + targetType.getName() + "' to '" + sourceType.getName() + "'");
    }

    private Supplier<Value> generateClassNestedUnmappingCode(ClassType sourceType, ClassType targetType, Supplier<Value> getView, ScopeRT scope) {
        if (sourceType == targetType)
            return getView;
        var source = Nodes.unmap("反映射" + sourceType.getName(), scope, getView.get(), Objects.requireNonNull(nestedMapping));
        return () -> Values.node(source);
    }

    private Supplier<Value> generateArrayNestedUnmappingCode(ArrayType sourceType, ArrayType targetType, Supplier<Value> getView, ScopeRT scope) {
        var isSourcePresent = Nodes.functionCall("来源是否存在", scope, NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, getView.get())));
        Map<Branch, Value> branch2sourceNode = new HashMap<>();
        var sourceFieldRef = new Object() {
            Field sourceField;
        };
        Nodes.branch(
                "分支",
                null,
                scope,
                Values.expression(Expressions.eq(Expressions.node(isSourcePresent), Expressions.trueExpression())),
                trueBranch -> {
                    var source = Nodes.functionCall(sourceType.getName() + "来源", trueBranch.getScope(),
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, getView.get())));
                    branch2sourceNode.put(trueBranch, Values.node(source));
                },
                falseBranch -> {
                    var source = Nodes.newArray(sourceType.getName() + "新建来源", null,
                            sourceType, null, null, falseBranch.getScope());
                    branch2sourceNode.put(falseBranch, Values.node(source));
                },
                mergeNode -> {
                    sourceFieldRef.sourceField = FieldBuilder.newBuilder("来源", null, mergeNode.getType(), sourceType)
                            .build();
                    new MergeNodeField(sourceFieldRef.sourceField, mergeNode, branch2sourceNode);
                }
        );
        var mergeNode = scope.getLastNode();
        var sourceField = sourceFieldRef.sourceField;
        Nodes.clearArray("清空数组" + sourceType.getName(), null, Values.nodeProperty(mergeNode, sourceField),
                scope);
        Nodes.forEach(
                "遍历" + targetType.getName(), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = generateNestedUnmappingCode(
                            sourceType.getElementType(), targetType.getElementType(),
                            getElement, bodyScope
                    );
                    Nodes.addElement("添加元素" + sourceType.getName(), null,
                            Values.nodeProperty(mergeNode, sourceField), getSourceElement.get(), bodyScope);
                },
                scope
        );
        return () -> Values.nodeProperty(mergeNode, sourceField);
    }

    protected abstract void generateWriteCode0(SelfNode selfNode, Supplier<Value> fieldValueSupplier);

    protected abstract Type getTargetFieldType();

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        return List.of(targetField);
    }

    public void setNestedMapping(@Nullable ObjectMapping nestedMapping, CompositeTypeFacade compositeTypeFacade) {
        this.nestedMapping = nestedMapping;
        resetTargetFieldType(compositeTypeFacade);
    }

    protected void resetTargetFieldType(CompositeTypeFacade compositeTypeFacade) {
        targetField.setType(getTargetFieldType(getTargetFieldType(), nestedMapping, compositeTypeFacade));
    }

    public static Type getTargetFieldType(Type targetFieldType, @Nullable ObjectMapping nestedMapping, CompositeTypeFacade compositeTypeFacade) {
        if(nestedMapping == null)
            return targetFieldType;
        if(targetFieldType instanceof ArrayType arrayType) {
            var elementType = arrayType.getInnermostElementType();
            var typeSubst = new TypeSubstitutor(List.of(elementType), List.of(nestedMapping.getTargetType()),
                    compositeTypeFacade, new MockDTOProvider());
            return targetFieldType.accept(typeSubst);
        }
        else {
            return targetFieldType;
        }
    }

    @Nullable
    public Mapping getNestedMapping() {
        return nestedMapping;
    }

    public boolean isValidLocalKey() {
        return getCode() != null;
    }

    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(getCode());
    }
}
