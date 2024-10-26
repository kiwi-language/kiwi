package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.AddObjectNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType
public class AddObjectNode extends NodeRT implements NewNode {

    public static AddObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        AddObjectNodeParam param = nodeDTO.getParam();
        var klass = ((ClassType) TypeParser.parseType(param.getType(), context)).resolve();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        AddObjectNode node = (AddObjectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    NncUtils.orElse(param.isInitializeArrayChildren(), false), param.isEphemeral(),
                    klass.getType(), prev, scope);
        } else {
            node.setKlass(klass);
            node.setEphemeral(param.isEphemeral());
            node.setInitializeArrayChildren(param.isInitializeArrayChildren());
        }
        node.setFields(NncUtils.map(
                param.getFieldParams(),
                fp -> new FieldParam(FieldRef.create(fp.fieldRef(), context), fp.value(), parsingContext)
        ));
        var parentRef = param.getParentRef() != null ?
                ParentRef.create(param.getParentRef(), parsingContext, context, klass.getType()) : null;
        node.setParentRef(parentRef);
        return node;
    }

    @Nullable
    private ParentRef parentRef;

    private boolean initializeArrayChildren;

    private boolean ephemeral;

    @ChildEntity
    private final ChildArray<FieldParam> fields = addChild(new ChildArray<>(FieldParam.class), "fields");

    public AddObjectNode(Long tmpId, String name, @Nullable String code, boolean initializeArrayChildren, boolean ephemeral, ClassType type, NodeRT prev,
                         ScopeRT scope) {
        super(tmpId, name, code, type, prev, scope);
        this.initializeArrayChildren = initializeArrayChildren;
        this.ephemeral = ephemeral;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) NncUtils.requireNonNull(super.getType());
    }

    public ReadonlyArray<FieldParam> getFields() {
        return fields;
    }

    @Override
    protected AddObjectNodeParam getParam(SerializeContext serializeContext) {
        return new AddObjectNodeParam(
                getType().toExpression(serializeContext),
                initializeArrayChildren,
                ephemeral, NncUtils.map(fields, FieldParam::toDTO),
                NncUtils.get(parentRef, ParentRef::toDTO)
        );
    }

    @Override
    public String check0() {
        var fieldParamMap = NncUtils.toMap(fields, f -> f.getField().getEffectiveTemplate(), Function.identity());
        var klass = getType().getKlass();
        var allFields = NncUtils.map(klass.getAllFields(), Field::getEffectiveTemplate);
        ErrorBuilder errorBuffer = new ErrorBuilder();
        for (var field : allFields) {
            if (field.getType().isNotNull() && !fieldParamMap.containsKey(field)) {
                errorBuffer.addError(String.format("Not null field '%s' is not set", field.getName()));
            }
        }
        return errorBuffer.getMessage();
    }

    public void setField(long fieldId, Value value) {
        fields.get(FieldParam::tryGetId, fieldId).setValue(value);
    }

    public void addField(FieldParam fieldParam) {
        NncUtils.requireTrue(fieldParam.getField().getDeclaringType() == getKlass()
                && fields.get(FieldParam::getField, fieldParam.getField()) == null
        );
        fields.addChild(fieldParam);
    }

    public boolean isInitializeArrayChildren() {
        return initializeArrayChildren;
    }

    @Override
    protected void setOutputType(@Nullable Type outputType) {
        throw new UnsupportedOperationException();
    }

    public Klass getKlass() {
        return getType().resolve();
    }

    public void setKlass(Klass klass) {
        if (klass != null) {
            this.fields.clear();
            super.setOutputType(klass.getType());
        }
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var parentRef = NncUtils.get(this.parentRef, p -> p.evaluate(frame));
        var instance = ClassInstanceBuilder.newBuilder(getType())
                .parentRef(parentRef)
                .ephemeral(ephemeral)
                .build();
        var fieldParamMap = NncUtils.toMap(fields, FieldParam::getField, Function.identity());
        for (Field field : getKlass().getAllFields()) {
            var fieldParam = fieldParamMap.get(field);
            instance.initField(field,
                    NncUtils.getOrElse(fieldParam, fp -> fp.evaluate(frame), Instances.nullInstance()));
        }
        if (initializeArrayChildren) {
            for (Field field : getKlass().getAllFields()) {
                if (field.isChild()
                        && field.getType() instanceof ArrayType arrayType
                        && arrayType.getKind() != ArrayKind.READ_ONLY) {
                    new ArrayInstance(arrayType, new InstanceParentRef(instance.getReference(), field));
                }
            }
        }
        if (!instance.isEphemeral())
            frame.addInstance(instance);
        return next(instance.getReference());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getName()
                + " (" + NncUtils.join(fields, FieldParam::getText, ", ") + ")");
    }

    public void setInitializeArrayChildren(boolean initializeArrayChildren) {
        this.initializeArrayChildren = initializeArrayChildren;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    @Override
    @NotNull
    public NodeRT getSuccessor() {
        return Objects.requireNonNull(super.getSuccessor());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddObjectNode(this);
    }

    @Override
    public void setParentRef(@Nullable ParentRef parentRef) {
        this.parentRef = NncUtils.get(parentRef, p -> addChild(p, "parentRef"));
    }

    public void setFields(List<FieldParam> fields) {
        NncUtils.requireTrue(NncUtils.allMatch(fields, f -> f.getField().getDeclaringType() == getKlass()));
        this.fields.resetChildren(fields);
    }

    @Override
    public @Nullable ParentRef getParentRef() {
        return parentRef;
    }
}
