package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Types;
import org.metavm.object.view.rest.dto.FieldsObjectMappingParam;
import org.metavm.object.view.rest.dto.ObjectMappingParam;
import org.metavm.util.NamingUtils;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType
public class FieldsObjectMapping extends ObjectMapping {

    @ChildEntity
    private final ChildArray<FieldMapping> fieldMappings = addChild(new ChildArray<>(FieldMapping.class), "fieldMappings");
    @ChildEntity
    @Nullable
    @CopyIgnore
    private Klass builtinTargetType;
    @Nullable
    private Method readMethod;
    @Nullable
    private Method writeMethod;

    public FieldsObjectMapping(Long tmpId, String name, @Nullable String code, Klass sourceType, boolean builtin,
                               @NotNull ClassType targetType) {
        super(tmpId, name, code, sourceType, targetType, builtin);
        sourceType.addMapping(this);
        this.builtinTargetType = addChild(targetType.resolve().getEffectiveTemplate(), "builtinTargetType");
    }

    public static String getTargetTypeName(Klass sourceType, String mappingName) {
        if (mappingName.endsWith("View") && mappingName.length() > 4)
            mappingName = mappingName.substring(0, mappingName.length() - 4);
        return NamingUtils.escapeTypeName(sourceType.getName()) + mappingName + "View";
    }

    public static @Nullable String getTargetTypeCode(Klass sourceType, @Nullable String mappingCode) {
        if (sourceType.getCode() == null || mappingCode == null)
            return null;
        if (mappingCode.endsWith("View") && mappingCode.length() > 4)
            mappingCode = mappingCode.substring(0, mappingCode.length() - 4);
        return NamingUtils.escapeTypeName(sourceType.getCode())
                + NamingUtils.firstCharToUpperCase(mappingCode) + "View";
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
    protected Flow generateMappingCode(boolean generateReadMethod) {
        if(generateReadMethod)
            generateReadMethodCode();
        return super.generateMappingCode(generateReadMethod);
    }

    @Override
    protected Flow generateUnmappingCode(boolean generateWriteMethod) {
        if(generateWriteMethod)
            generateWriteMethodCode();
        return super.generateUnmappingCode(generateWriteMethod);
    }

    @Override
    public void generateDeclarations() {
        generateReadMethodDeclaration();
        generateWriteMethodDeclaration();
        super.generateDeclarations();
    }

    @Override
    public void generateCode(Flow flow) {
        if (flow == readMethod)
            generateReadMethodCode();
        else if (flow == writeMethod)
            generateWriteMethodCode();
        else
            super.generateCode(flow);
    }

    private void generateReadMethodDeclaration() {
        readMethod = MethodBuilder.newBuilder(getSourceKlass(),
                        "getView$" + getName(),
                        NncUtils.get(getCode(), c -> "getView$" + c)
                )
                .existing(readMethod)
                .codeSource(this)
                .returnType(getTargetType())
                .isSynthetic(true)
                .build();
    }

    private void generateWriteMethodDeclaration() {
        writeMethod = MethodBuilder.newBuilder(getSourceKlass(),
                        "saveView$" + getName(),
                        NncUtils.get(getCode(), c -> "saveView$" + c)
                )
                .existing(writeMethod)
                .codeSource(this)
                .returnType(Types.getVoidType())
                .isSynthetic(true)
                .parameters(writeMethod != null ? writeMethod.getParameter(0) :
                        new Parameter(null, "view", "view", getTargetType()))
                .build();
    }

    public void generateReadMethodCode() {
        var scope = Objects.requireNonNull(readMethod).newEphemeralRootScope();
        var selfNode = new SelfNode(null, "self", null, getSourceType(), null, scope);
        List<FieldParam> fieldParams = new ArrayList<>();
        for (FieldMapping fieldMapping : fieldMappings)
            fieldParams.add(fieldMapping.generateReadCode(selfNode));
        var view = new AddObjectNode(null, "view", null, false,
                true, getTargetType(), scope.getLastNode(), scope);
        fieldParams.forEach(view::addField);
        new ReturnNode(null, "return", null, scope.getLastNode(), scope, Values.node(view));
        readMethod.computeMaxes();
    }

    public void generateWriteMethodCode() {
        var scope = Objects.requireNonNull(writeMethod).newEphemeralRootScope();
        var selfNode = new SelfNode(null, "self", null, getSourceType(), null, scope);
        var inputNode = Nodes.input(writeMethod);
        var viewNode = Nodes.inputField(inputNode, 0, scope);
        for (FieldMapping fieldMapping : fieldMappings) {
            if (!fieldMapping.isReadonly())
                fieldMapping.generateWriteCode(selfNode, viewNode);
        }
        new ReturnNode(null, "return", null, scope.getLastNode(), scope, null);
        writeMethod.computeMaxes();
    }

    public void setName(String name) {
        super.setName(name);
        if (builtinTargetType != null)
            builtinTargetType.setName(getTargetTypeName(getSourceKlass(), name));
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

    public String getText() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"sourceType\": \"").append(sourceType.getTypeDesc())
                .append("\", \"targetType\": \"").append(targetType.getTypeDesc()).append('\"')
                .append(", \"fieldMappings\": [");
        boolean first = true;
        for (FieldMapping fieldMapping : fieldMappings) {
            if(first)
                first = false;
            else
                builder.append(", ");
            builder.append(fieldMapping.getText());
        }
        builder.append("]}");
        return builder.toString();
    }

}
