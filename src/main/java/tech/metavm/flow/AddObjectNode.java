package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.AddObjectParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.meta.ArrayKind;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("新增记录节点")
public class AddObjectNode extends ScopeNode<AddObjectParam> {

    public static AddObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        AddObjectParam param = nodeDTO.getParam();
        ClassType type = context.getClassType(param.getTypeRef());
        AddObjectNode node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(),
                NncUtils.orElse(param.isInitializeArrayChildren(), false),
                type, prev, scope);
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("主对象")
    @Nullable
    private ParentRef parent;

    @EntityField("初始化子数组")
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
            parent = ParentRef.create(param.getParentRef(), parsingContext, getType());
        }
        if(param.isInitializeArrayChildren() != null) {
            initializeArrayChildren = param.isInitializeArrayChildren();
        }
    }

    @Override
    public void execute(MetaFrame frame) {
        var instance = new ClassInstance(getType(), NncUtils.get(parent, p -> p.evaluate(frame)));
        for (FieldParam field : fields) {
            instance.initField(field.getField(), field.evaluate(frame));
        }
        if(initializeArrayChildren) {
            for (Field field : getType().getAllFields()) {
                if(field.isChildField()
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

}
