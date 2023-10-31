package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.AddObjectParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.ChildArray;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReadonlyArray;

import javax.annotation.Nullable;

@EntityType("新增记录节点")
public class AddObjectNode extends NodeRT<AddObjectParam> {

    public static AddObjectNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        AddObjectParam param = nodeDTO.getParam();
        ClassType type = context.getClassType(param.typeRef());
        AddObjectNode node = new AddObjectNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, scope);
        node.setParam(param, context);
        return node;
    }

    @ChildEntity("主对象")
    @Nullable
    private MasterRef master;

    @ChildEntity("字段列表")
    private final ChildArray<FieldParam> fields = new ChildArray<>(FieldParam.class);

    public AddObjectNode(Long tmpId, String name, ClassType type, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, type, prev, scope);
    }

    @Override
    public ClassType getType() {
        return (ClassType) super.getType();
    }

    public ReadonlyArray<FieldParam> getFields() {
        return fields;
    }

    @Override
    protected AddObjectParam getParam(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            return new AddObjectParam(
                    context.getRef(getType()),
                    NncUtils.map(fields, fp -> fp.toDTO(persisting)),
                    NncUtils.get(master, MasterRef::toDTO)
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

    @Override
    protected void setParam(AddObjectParam param, IEntityContext context) {
        if (param.typeRef() != null) {
            setOutputType(context.getType(param.typeRef()));
        }
        var parsingContext = getParsingContext(context);
        if (param.fieldParams() != null) {
            fields.resetChildren(NncUtils.map(
                    param.fieldParams(),
                    fp -> new FieldParam(context.getField(fp.fieldRef()), fp.value(), parsingContext)
            ));
        }
        if (param.master() != null) {
            master = MasterRef.create(param.master(), parsingContext, getType());
        }
    }


    @Override
    public void execute(MetaFrame frame) {
        var instance = frame.addInstance(
                new ClassInstance(
                        NncUtils.toMap(
                                fields,
                                FieldParam::getField,
                                fp -> fp.evaluate(frame)
                        ),
                        getType()
                )
        );
        if(master != null) {
            master.setChild(instance, frame);
        }
        frame.setResult(instance);
    }

}
