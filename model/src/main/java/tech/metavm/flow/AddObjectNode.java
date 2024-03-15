package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.AddObjectNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstanceBuilder;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType("新增对象节点")
public class AddObjectNode extends ScopeNode implements NewNode {

    public static AddObjectNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        AddObjectNodeParam param = nodeDTO.getParam();
        ClassType type = context.getClassType(Id.parse(param.getTypeId()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        AddObjectNode node = (AddObjectNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    NncUtils.orElse(param.isInitializeArrayChildren(), false), param.isEphemeral(),
                    type, prev, scope);
        } else {
            node.setOutputType(type);
            node.setEphemeral(param.isEphemeral());
            node.setInitializeArrayChildren(param.isInitializeArrayChildren());
        }
        node.setFields(NncUtils.map(
                param.getFieldParams(),
                fp -> new FieldParam(context.getField(Id.parse(fp.fieldId())), fp.value(), parsingContext)
        ));
        var parentRef = param.getParentRef() != null ?
                ParentRef.create(param.getParentRef(), parsingContext, context, type) : null;
        node.setParentRef(parentRef);
        return node;
    }

    @ChildEntity("父引用")
    @Nullable
    private ParentRef parentRef;

    @EntityField("是否初始化数组子对象")
    private boolean initializeArrayChildren;

    @EntityField("是否临时")
    private boolean ephemeral;

    @ChildEntity("字段列表")
    private final ChildArray<FieldParam> fields = addChild(new ChildArray<>(FieldParam.class), "fields");

    public AddObjectNode(Long tmpId, String name, @Nullable String code, boolean initializeArrayChildren, boolean ephemeral, ClassType type, NodeRT prev,
                         ScopeRT scope) {
        super(tmpId, name, code, type, prev, scope, false);
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
        try (var serContext = SerializeContext.enter()) {
            return new AddObjectNodeParam(
                    serContext.getRef(getType()),
                    initializeArrayChildren,
                    ephemeral, NncUtils.map(fields, FieldParam::toDTO),
                    NncUtils.get(parentRef, ParentRef::toDTO),
                    bodyScope.toDTO(true, serializeContext)
            );
        }
    }

    @Override
    public String check0() {
        var fieldParamMap = NncUtils.toMap(fields, FieldParam::getField, Function.identity());
        ErrorBuilder errorBuffer = new ErrorBuilder();
        for (Field field : getType().getAllFields()) {
            if (field.getType().isNotNull() && !fieldParamMap.containsKey(field)) {
                errorBuffer.addError(String.format("必填字段'%s'未配置", field.getName()));
            }
        }
        return errorBuffer.getMessage();
    }

    public void setField(long fieldId, Value value) {
        fields.get(FieldParam::tryGetId, fieldId).setValue(value);
    }

    public void addField(FieldParam fieldParam) {
        NncUtils.requireTrue(fieldParam.getField().getDeclaringType() == getType()
                && fields.get(FieldParam::getField, fieldParam.getField()) == null
        );
        fields.addChild(fieldParam);
    }

    public boolean isInitializeArrayChildren() {
        return initializeArrayChildren;
    }

    @Override
    protected void setOutputType(@Nullable Type outputType) {
        NncUtils.requireTrue(outputType instanceof ClassType);
        if (outputType != this.getType())
            this.fields.clear();
        super.setOutputType(outputType);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var parentRef = NncUtils.get(this.parentRef, p -> p.evaluate(frame));
        var instance = ClassInstanceBuilder.newBuilder(getType())
                .parentRef(parentRef)
                .ephemeral(ephemeral)
                .build();
        var fieldParamMap = NncUtils.toMap(fields, FieldParam::getField, Function.identity());
        for (Field field : getType().getAllFields()) {
            var fieldParam = fieldParamMap.get(field);
            instance.initField(field,
                    NncUtils.getOrElse(fieldParam, fp -> fp.evaluate(frame), Instances.nullInstance()));
        }
        if (initializeArrayChildren) {
            for (Field field : getType().getAllFields()) {
                if (field.isChild()
                        && field.getType() instanceof ArrayType arrayType
                        && arrayType.getKind() != ArrayKind.READ_ONLY) {
                    new ArrayInstance(arrayType, new InstanceParentRef(instance, field));
                }
            }
        }
        if (!instance.isEphemeral())
            frame.addInstance(instance);
        return bodyScope.isNotEmpty() ?
                NodeExecResult.jump(instance, bodyScope.tryGetFirstNode()) : next(instance);
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
        NncUtils.requireTrue(NncUtils.allMatch(fields, f -> f.getField().getDeclaringType() == getType()));
        this.fields.resetChildren(fields);
    }

    @Override
    public @Nullable ParentRef getParentRef() {
        return parentRef;
    }
}
