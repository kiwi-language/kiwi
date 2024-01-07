package tech.metavm.object.view;

import tech.metavm.common.ErrorCode;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.object.view.rest.dto.FieldsObjectMappingParam;
import tech.metavm.object.view.rest.dto.ObjectMappingParam;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NamingUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@EntityType("字段对象映射")
public class FieldsObjectMapping extends ObjectMapping {

    @ChildEntity("字段列表")
    private final ChildArray<FieldMapping> fieldMappings = addChild(new ChildArray<>(FieldMapping.class), "fieldMappings");
    @ChildEntity("内置目标类型")
    @Nullable
    private ClassType builtinTargetType;
    @EntityField("读取流程")
    @Nullable
    private Method readMethod;
    @EntityField("写入流程")
    @Nullable
    private Method writeMethod;

    // TODO add NotNull annotation to required constructor parameters
    public FieldsObjectMapping(Long tmpId, String name, @Nullable String code, ClassType sourceType, boolean builtin, List<ObjectMapping> overridden) {
        this(tmpId, name, code, sourceType, builtin, null, overridden);
    }

    public FieldsObjectMapping(Long tmpId, String name, @Nullable String code, ClassType sourceType, boolean builtin,
                               @Nullable ClassType targetType, List<ObjectMapping> overridden) {
        super(tmpId, name, code, sourceType, targetType == null ? createTargetType(sourceType, name, code) : targetType, builtin);
        overridden.forEach(this::checkOverridden);
        this.overridden.addAll(overridden);
        sourceType.addMapping(this);
        if (targetType == null)
            this.builtinTargetType = addChild(getTargetType(), "builtinTargetType");
    }

    private static ClassType createTargetType(ClassType sourceType, String name, @Nullable String code) {
        return ClassBuilder.newBuilder(getTargetTypeName(sourceType, name), getTargetTypeCode(sourceType, code))
                .ephemeral(true)
                .anonymous(true)
                .build();
    }

    private void checkOverridden(ObjectMapping overridden) {
        if (!overridden.getTargetType().equals(getTargetType())
                || !overridden.getSourceType().isAssignableFrom(getSourceType())) {
            throw new BusinessException(ErrorCode.INVALID_OVERRIDDEN_MAPPING);
        }
    }

    public static String getTargetTypeName(ClassType sourceType, String mappingName) {
        if (mappingName.endsWith("视图") && mappingName.length() > 2)
            mappingName = mappingName.substring(0, mappingName.length() - 2);
        return NamingUtils.escapeTypeName(sourceType.getName()) + mappingName + "视图";
    }

    public static @Nullable String getTargetTypeCode(ClassType sourceType, @Nullable String mappingCode) {
        if (sourceType.getCode() == null || mappingCode == null)
            return null;
        if (mappingCode.endsWith("View") && mappingCode.length() > 4)
            mappingCode = mappingCode.substring(0, mappingCode.length() - 4);
        return NamingUtils.escapeTypeName(sourceType.getCode())
                + NamingUtils.firstCharToUpperCase(mappingCode) + "View";
    }

    public List<ObjectMapping> getOverridden() {
        return overridden.toList();
    }

    @Nullable
    public FieldsObjectMapping getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(@Nullable Object template) {
        NncUtils.requireNull(this.template);
        this.template = (FieldsObjectMapping) template;
    }

    public void setCode(@Nullable String code) {
        super.setCode(code);
        if (builtinTargetType != null)
            builtinTargetType.setCode(getTargetTypeCode(getSourceType(), code));
    }

    @Override
    public Method getReadMethod() {
        return Objects.requireNonNull(readMethod);
    }

    @Override
    public Method getWriteMethod() {
        return Objects.requireNonNull(writeMethod);
    }

    @Override
    protected Flow generateMappingCode(FunctionTypeProvider functionTypeProvider) {
        generateReadMethodCode();
        return super.generateMappingCode(functionTypeProvider);
    }

    @Override
    protected Flow generateUnmappingCode(FunctionTypeProvider functionTypeProvider) {
        generateWriteMethodCode();
        return super.generateUnmappingCode(functionTypeProvider);
    }

    @Override
    public void generateDeclarations(FunctionTypeProvider functionTypeProvider) {
        super.generateDeclarations(functionTypeProvider);
        generateReadMethodDeclaration(functionTypeProvider);
        generateWriteMethodDeclaration(functionTypeProvider);
    }

    @Override
    public void generateCode(Flow flow, FunctionTypeProvider functionTypeProvider) {
        if(flow == readMethod)
            generateReadMethodCode();
        else if(flow == writeMethod)
            generateWriteMethodCode();
        else
            super.generateCode(flow, functionTypeProvider);
    }

    private void generateReadMethodDeclaration(FunctionTypeProvider functionTypeProvider) {
        readMethod = MethodBuilder.newBuilder(getSourceType(), "获取" + getTargetType().getName(),
                        NamingUtils.getGetterName(getTargetType().getCode()), functionTypeProvider)
                .existing(readMethod)
                .codeSource(this)
                .returnType(getTargetType())
                .isSynthetic(true)
                .overridden(NncUtils.map(overridden, ObjectMapping::getReadMethod))
                .build();
    }

    private void generateWriteMethodDeclaration(FunctionTypeProvider functionTypeProvider) {
        writeMethod = MethodBuilder.newBuilder(getSourceType(), "保存" + getTargetType().getName(),
                        NamingUtils.tryAddPrefix(getTargetType().getCode(), "save"), functionTypeProvider)
                .existing(writeMethod)
                .codeSource(this)
                .returnType(StandardTypes.getVoidType())
                .isSynthetic(true)
                .parameters(writeMethod != null ? writeMethod.getParameter(0) :
                        new Parameter(null, "视图", "View", getTargetType()))
                .overridden(NncUtils.map(overridden, ObjectMapping::getWriteMethod))
                .build();
    }

    public void generateReadMethodCode() {
        var scope = Objects.requireNonNull(readMethod).newEphemeralRootScope();
        var selfNode = new SelfNode(null, "当前对象", "Self", getTargetType(), null, scope);
        List<FieldParam> fieldParams = new ArrayList<>();
        for (FieldMapping fieldMapping : fieldMappings)
            fieldParams.add(fieldMapping.generateReadCode(selfNode));
        var view = new AddObjectNode(null, "视图", "View", false,
                true, getTargetType(), scope.getLastNode(), scope);
        fieldParams.forEach(view::addField);
        new ReturnNode(null, "返回", "Return", scope.getLastNode(), scope, Values.node(view));
    }

    public void generateWriteMethodCode() {
        var scope = Objects.requireNonNull(writeMethod).newEphemeralRootScope();
        var selfNode = new SelfNode(null, "当前对象", "Self", getSourceType(), null, scope);
        var inputNode = Nodes.input(writeMethod);
        var viewNode = new ValueNode(null, "视图", "View", getTargetType(), scope.getLastNode(), scope,
                Values.inputValue(inputNode, 0));
        for (FieldMapping fieldMapping : fieldMappings) {
            if (!fieldMapping.isReadonly())
                fieldMapping.generateWriteCode(selfNode, viewNode);
        }
        new ReturnNode(null, "返回", "Return", scope.getLastNode(), scope, null);
    }

    public void setName(String name) {
        super.setName(name);
        if (builtinTargetType != null)
            builtinTargetType.setName(getTargetTypeName(getSourceType(), name));
    }

    public void setOverridden(List<ObjectMapping> overridden) {
        overridden.forEach(this::checkOverridden);
        getReadMethod().setOverridden(NncUtils.map(overridden, ObjectMapping::getReadMethod));
        getWriteMethod().setOverridden(NncUtils.map(overridden, ObjectMapping::getWriteMethod));
        this.overridden.reset(overridden);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDefaultObjectMapping(this);
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings.toList();
    }

    public FieldMapping getFieldMappingByTargetField(Field targetField) {
        return Objects.requireNonNull(
                fieldMappings.get(FieldMapping::getTargetField, targetField),
                () -> "Can not find field mapping for target field: " + targetField.getName()
        );
    }

    @Override
    protected ObjectMappingParam getParam(SerializeContext serializeContext) {
        return new FieldsObjectMappingParam(
                NncUtils.map(fieldMappings, f -> f.toDTO(serializeContext))
        );
    }

    public FieldMapping findFieldMapping(RefDTO ref) {
        return fieldMappings.get(Entity::getRef, ref);
    }

    void addField(FieldMapping field) {
        this.fieldMappings.addChild(field);
    }

    public void setFieldMappings(List<FieldMapping> fields) {
        this.fieldMappings.resetChildren(fields);
        Set<Field> aliveFields = NncUtils.mapUnique(fields, FieldMapping::getTargetField);
        var deadFields = NncUtils.exclude(getTargetType().getFields(), aliveFields::contains);
        deadFields.forEach(getTargetType()::removeField);
    }

    @Nullable
    public ClassType getBuiltinTargetType() {
        return builtinTargetType;
    }

}
