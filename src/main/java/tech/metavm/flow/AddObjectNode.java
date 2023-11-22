package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.flow.rest.AddObjectParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.function.Function;

@EntityType("新增记录节点")
public class AddObjectNode extends ScopeNode<AddObjectParam> implements NewNode {

    public static AddObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        AddObjectParam param = nodeDTO.getParam();
        ClassType type = context.getClassType(param.getTypeRef());
        AddObjectNode node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(),
                NncUtils.orElse(param.isInitializeArrayChildren(), false),
                type, prev, scope);
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("父对象")
    @Nullable
    private ParentRef parent;

    @EntityField("初始化子对象数组")
    private boolean initializeArrayChildren;

    @ChildEntity("字段列表")
    private final ChildArray<FieldParam> fields = addChild(new ChildArray<>(FieldParam.class), "fields");

    public AddObjectNode(Long tmpId, String name, boolean initializeArrayChildren, ClassType type, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, type, prev, scope, false);
        this.initializeArrayChildren = initializeArrayChildren;
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
    protected AddObjectParam getParam(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            return new AddObjectParam(
                    context.getRef(getType()),
                    initializeArrayChildren,
                    NncUtils.map(fields, fp -> fp.toDTO(persisting)),
                    NncUtils.get(parent, ParentRef::toDTO),
                    bodyScope.toDTO(true)
            );
        }
    }

    @Override
    public String check0() {
        var fieldParamMap = NncUtils.toMap(fields, FieldParam::getField, Function.identity());
        ErrorBuilder errorBuffer = new ErrorBuilder();
        for (Field field : getType().getAllFields()) {
            if(field.getType().isNotNull() && !fieldParamMap.containsKey(field)) {
                errorBuffer.addError(String.format("必填字段'%s'未配置", field.getName()));
            }
        }
        return errorBuffer.getMessage();
    }

    public void setField(long fieldId, Value value) {
        fields.get(FieldParam::getId, fieldId).setValue(value);
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
    protected void setParam(AddObjectParam param, IEntityContext context) {
        if (param.getTypeRef() != null) {
            setOutputType(context.getType(param.getTypeRef()));
        }
        var parsingContext = getParsingContext(context);
        if (param.getFieldParams() != null) {
            fields.resetChildren(NncUtils.map(
                    param.getFieldParams(),
                    fp -> new FieldParam(context.getField(fp.fieldRef()), fp.value(), parsingContext)
            ));
        }
        if (param.getParentRef() != null) {
            setParent(ParentRef.create(param.getParentRef(), parsingContext, getType()));
        }
        if(param.isInitializeArrayChildren() != null) {
            initializeArrayChildren = param.isInitializeArrayChildren();
        }
    }

    @Override
    public void execute(MetaFrame frame) {
        var instance = new ClassInstance(getType(), NncUtils.get(parent, p -> p.evaluate(frame)));
        var fieldParamMap = NncUtils.toMap(fields, FieldParam::getField, Function.identity());
        for (Field field : getType().getAllFields()) {
            var fieldParam = fieldParamMap.get(field);
            instance.initField(field,
                    NncUtils.getOrElse(fieldParam, fp -> fp.evaluate(frame), InstanceUtils.nullInstance()));
        }
        if(initializeArrayChildren) {
            for (Field field : getType().getAllFields()) {
                if(field.isChild()
                        && field.getType() instanceof ArrayType arrayType
                        && arrayType.getKind() != ArrayKind.READ_ONLY) {
                    new ArrayInstance(arrayType, new InstanceParentRef(instance, field));
                }
            }
        }
        frame.addInstance(instance);
        frame.setResult(instance);
        if(bodyScope.isNotEmpty()) {
            frame.jumpTo(bodyScope.getFirstNode());
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddObjectNode(this);
    }

    @Override
    public void setParent(@Nullable ParentRef parentRef) {
        this.parent = NncUtils.get(parentRef, p -> addChild(p, "parent"));
    }

    @Override
    public @Nullable ParentRef getParentRef() {
        return parent;
    }
}
