package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.CompositeTypeFacade;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.type.Klass;
import tech.metavm.object.view.rest.dto.FieldsObjectMappingParam;
import tech.metavm.object.view.rest.dto.ObjectMappingParam;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NamingUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType("字段对象映射")
public class FieldsObjectMapping extends ObjectMapping {

    @ChildEntity("字段列表")
    private final ChildArray<FieldMapping> fieldMappings = addChild(new ChildArray<>(FieldMapping.class), "fieldMappings");
    @ChildEntity("内置目标类型")
    @Nullable
    @CopyIgnore
    private Klass builtinTargetType;
    @EntityField("读取流程")
    @Nullable
    private Method readMethod;
    @EntityField("写入流程")
    @Nullable
    private Method writeMethod;

    public FieldsObjectMapping(Long tmpId, String name, @Nullable String code, Klass sourceType, boolean builtin,
                               @NotNull Klass targetType, List<ObjectMapping> overridden) {
        super(tmpId, name, code, sourceType, targetType, builtin);
        overridden.forEach(this::checkOverridden);
        this.overridden.addAll(overridden);
        sourceType.addMapping(this);
        this.builtinTargetType = addChild(targetType.getEffectiveTemplate(), "builtinTargetType");
    }

    private void checkOverridden(ObjectMapping overridden) {
        if (!overridden.getTargetType().equals(getTargetType())
                || !overridden.getSourceType().isAssignableFrom(getSourceType())) {
            throw new BusinessException(ErrorCode.INVALID_OVERRIDDEN_MAPPING);
        }
    }

    public static String getTargetTypeName(Klass sourceType, String mappingName) {
        if (mappingName.endsWith("视图") && mappingName.length() > 2)
            mappingName = mappingName.substring(0, mappingName.length() - 2);
        return NamingUtils.escapeTypeName(sourceType.getName()) + mappingName + "视图";
    }

    public static @Nullable String getTargetTypeCode(Klass sourceType, @Nullable String mappingCode) {
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

    public void setCode(@Nullable String code) {
        super.setCode(code);
        if (builtinTargetType != null)
            builtinTargetType.setCode(getTargetTypeCode(getSourceKlass(), code));
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
    protected Flow generateMappingCode(CompositeTypeFacade compositeTypeFacade) {
        generateReadMethodCode(compositeTypeFacade);
        return super.generateMappingCode(compositeTypeFacade);
    }

    @Override
    protected Flow generateUnmappingCode(CompositeTypeFacade compositeTypeFacade) {
        generateWriteMethodCode(compositeTypeFacade);
        return super.generateUnmappingCode(compositeTypeFacade);
    }

    @Override
    public void generateDeclarations(CompositeTypeFacade compositeTypeFacade) {
        generateReadMethodDeclaration(compositeTypeFacade);
        generateWriteMethodDeclaration(compositeTypeFacade);
        super.generateDeclarations(compositeTypeFacade);
    }

    @Override
    public void generateCode(Flow flow, CompositeTypeFacade compositeTypeFacade) {
        if (flow == readMethod)
            generateReadMethodCode(compositeTypeFacade);
        else if (flow == writeMethod)
            generateWriteMethodCode(compositeTypeFacade);
        else
            super.generateCode(flow, compositeTypeFacade);
    }

    private void generateReadMethodDeclaration(FunctionTypeProvider functionTypeProvider) {
        readMethod = MethodBuilder.newBuilder(getSourceKlass(),
                        "获取视图$" + getName(),
                        NncUtils.get(getCode(), c -> "getView$" + c)
                )
                .existing(readMethod)
                .codeSource(this)
                .returnType(getTargetType())
                .isSynthetic(true)
                .overridden(NncUtils.map(overridden, ObjectMapping::getReadMethod))
                .build();
    }

    private void generateWriteMethodDeclaration(FunctionTypeProvider functionTypeProvider) {
        writeMethod = MethodBuilder.newBuilder(getSourceKlass(),
                        "保存视图$" + getName(),
                        NncUtils.get(getCode(), c -> "saveView$" + c)
                )
                .existing(writeMethod)
                .codeSource(this)
                .returnType(StandardTypes.getVoidType())
                .isSynthetic(true)
                .parameters(writeMethod != null ? writeMethod.getParameter(0) :
                        new Parameter(null, "视图", "View", getTargetType()))
                .overridden(NncUtils.map(overridden, ObjectMapping::getWriteMethod))
                .build();
    }

    public void generateReadMethodCode(CompositeTypeFacade compositeTypeFacade) {
        var scope = Objects.requireNonNull(readMethod).newEphemeralRootScope();
        var selfNode = new SelfNode(null, "当前对象", "Self", getTargetType(), null, scope);
        List<FieldParam> fieldParams = new ArrayList<>();
        for (FieldMapping fieldMapping : fieldMappings)
            fieldParams.add(fieldMapping.generateReadCode(selfNode, compositeTypeFacade));
        var view = new AddObjectNode(null, "视图", "View", false,
                true, getTargetKlass(), scope.getLastNode(), scope);
        fieldParams.forEach(view::addField);
        new ReturnNode(null, "返回", "Return", scope.getLastNode(), scope, Values.node(view));
    }

    public void generateWriteMethodCode(CompositeTypeFacade compositeTypeFacade) {
        var scope = Objects.requireNonNull(writeMethod).newEphemeralRootScope();
        var selfNode = new SelfNode(null, "当前对象", "Self", getSourceType(), null, scope);
        var inputNode = Nodes.input(writeMethod, compositeTypeFacade);
        var viewNode = new ValueNode(null, "视图", "View", getTargetType(), scope.getLastNode(), scope,
                Values.inputValue(inputNode, 0));
        for (FieldMapping fieldMapping : fieldMappings) {
            if (!fieldMapping.isReadonly())
                fieldMapping.generateWriteCode(selfNode, viewNode, compositeTypeFacade);
        }
        new ReturnNode(null, "返回", "Return", scope.getLastNode(), scope, null);
    }

    public void setName(String name) {
        super.setName(name);
        if (builtinTargetType != null)
            builtinTargetType.setName(getTargetTypeName(getSourceKlass(), name));
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

    public FieldMapping findFieldMapping(Id id) {
        return fieldMappings.get(Entity::getId, id);
    }

    void addField(FieldMapping field) {
        this.fieldMappings.addChild(field);
    }

    public void setFieldMappings(List<FieldMapping> fields) {
        this.fieldMappings.resetChildren(fields);
        getTargetKlass().setFields(NncUtils.map(fields, FieldMapping::getTargetField));
    }

    @Nullable
    public Klass getBuiltinTargetType() {
        return builtinTargetType;
    }

}
