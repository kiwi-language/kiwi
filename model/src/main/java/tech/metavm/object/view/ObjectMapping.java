package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;
import tech.metavm.object.view.rest.dto.ObjectMappingParam;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public abstract class ObjectMapping extends Mapping implements LocalKey, GenericElement {

    @ChildEntity("被复写映射列表")
    protected final ReadWriteArray<ObjectMapping> overridden =
            addChild(new ReadWriteArray<>(ObjectMapping.class), "overridden");
    @EntityField("是否预置")
    private final boolean builtin;
    @EntityField("模板")
    @Nullable
    @CopyIgnore
    protected FieldsObjectMapping template;

    public ObjectMapping(Long tmpId, String name, @Nullable String code, ClassType sourceType, ClassType targetType, boolean builtin) {
        super(tmpId, name, code, sourceType, targetType);
        this.builtin = builtin;
    }

    @Override
    protected Flow generateMappingCode(FunctionTypeProvider functionTypeProvider) {
        var scope = Objects.requireNonNull(mapper).newEphemeralRootScope();
        var input = Nodes.input(mapper);
        var view = new MethodCallNode(
                null, "视图", "view",
                scope.getLastNode(), scope,
                Values.inputValue(input, 0),
                getReadMethod(), List.of()
        );
//        Nodes.setSource(Values.node(view), Values.inputValue(input, 0), scope);
        new ReturnNode(null, "结束", "Return", scope.getLastNode(), scope, Values.node(view));
        return mapper;
    }

    protected Flow generateUnmappingCode(FunctionTypeProvider functionTypeProvider) {
        var scope = Objects.requireNonNull(unmapper).newEphemeralRootScope();
        var input = Nodes.input(unmapper);
        var source = new FunctionCallNode(
                null, "来源", "source", scope.getLastNode(), scope,
                NativeFunctions.getSource(), List.of(Nodes.argument(NativeFunctions.getSource(), 0, Values.inputValue(input, 0)))
        );
        var castedSource = new CastNode(
                null, "Casted来源", "CastedSource", getSourceType(), scope.getLastNode(),
                scope, Values.node(source)
        );
        new MethodCallNode(
                null, "保存视图", "saveView", scope.getLastNode(), scope, Values.node(castedSource),
                getWriteMethod(), List.of(Nodes.argument(getWriteMethod(), 0, Values.inputValue(input, 0)))
        );
        new ReturnNode(null, "返回", "Return", scope.getLastNode(), scope, Values.node(castedSource));
        return unmapper;
    }

    @Override
    public ClassInstance map(DurableInstance instance, InstanceRepository repository, ParameterizedFlowProvider parameterizedFlowProvider) {
        if (instance instanceof ClassInstance)
            return (ClassInstance) super.map(instance, repository, parameterizedFlowProvider);
        else
            throw new InternalException("Invalid source");
    }

    @Override
    public ClassType getSourceType() {
        return (ClassType) super.getSourceType();
    }

    public ClassType getTargetType() {
        return (ClassType) super.getTargetType();
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public abstract Method getReadMethod();

    public abstract Method getWriteMethod();

    public ObjectMappingDTO toDTO(SerializeContext serializeContext) {
        return new ObjectMappingDTO(
                tryGetId(),
                serializeContext.getTmpId(this),
                getName(),
                getCode(),
                serializeContext.getRef(getSourceType()),
                serializeContext.getRef(getTargetType()),
                isDefault(),
                isBuiltin(),
                NncUtils.map(overridden, Entity::getRef),
                getParam(serializeContext)
        );
    }

    protected abstract ObjectMappingParam getParam(SerializeContext serializeContext);

    public boolean isDefault() {
        return getSourceType().getDefaultMapping() == this;
    }

    public void setDefault() {
        getSourceType().setDefaultMapping(this);
    }

    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(getCode());
    }

    public boolean isValidLocalKey() {
        return getCode() != null;
    }
}
