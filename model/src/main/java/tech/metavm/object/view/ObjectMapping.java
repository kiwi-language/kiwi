package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;
import tech.metavm.object.view.rest.dto.ObjectMappingParam;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public abstract class ObjectMapping extends Mapping implements LocalKey {

    @ChildEntity("被复写映射列表")
    protected final ReadWriteArray<ObjectMapping> overridden =
            addChild(new ReadWriteArray<>(ObjectMapping.class), "overridden");
    @EntityField("是否预置")
    private final boolean builtin;

    public ObjectMapping(Long tmpId, String name, @Nullable String code, ClassType sourceType, ClassType targetType, boolean builtin) {
        super(tmpId, name, code, sourceType, targetType);
        this.builtin = builtin;
    }

    @Override
    protected Flow generateMappingCode(FunctionTypeProvider functionTypeProvider) {
        var scope = Objects.requireNonNull(mapper).newEphemeralRootScope();
        var input = Nodes.input(mapper);
        var actualSourceType = (ClassType) mapper.getParameter(0).getType();
        var readMethod = getSourceMethod(actualSourceType, getReadMethod());
        var view = new MethodCallNode(
                null, "视图", "view",
                scope.getLastNode(), scope,
                Values.inputValue(input, 0),
                readMethod, List.of()
        );
//        Nodes.setSource(Values.node(view), Values.inputValue(input, 0), scope);
        new ReturnNode(null, "结束", "Return", scope.getLastNode(), scope, Values.node(view));
        return mapper;
    }

    protected Flow generateUnmappingCode(FunctionTypeProvider functionTypeProvider) {
        Objects.requireNonNull(unmapper);
        var actualSourceType = (ClassType) unmapper.getReturnType();
        var fromViewMethod = findSourceMethod(actualSourceType, findFromViewMethod());
        var writeMethod = getSourceMethod(actualSourceType, getWriteMethod());
        var scope = unmapper.newEphemeralRootScope();
        var input = Nodes.input(unmapper);
        var isSourcePresent = Nodes.functionCall(
                "来源是否存在", scope,
                NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, Values.inputValue(input, 0)))
        );
        Nodes.branch(
                "分支", null, scope,
                Values.node(isSourcePresent),
                trueBranch -> {
                    var bodyScope = trueBranch.getScope();
                    var source = Nodes.functionCall(
                            "来源", bodyScope,
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, Values.inputValue(input, 0)))
                    );
                    var castedSource = Nodes.cast("Casted来源", getSourceType(), Values.node(source), bodyScope);
                    Nodes.methodCall(
                            "保存视图", bodyScope, Values.node(castedSource),
                            writeMethod, List.of(Nodes.argument(writeMethod, 0, Values.inputValue(input, 0)))
                    );
                    Nodes.ret("返回", bodyScope, Values.node(castedSource));
                },
                falseBranch -> {
                    var bodyScope = falseBranch.getScope();
                    if (fromViewMethod != null) {
                        var fromView = Nodes.methodCall(
                                "从视图创建", bodyScope,
                                null, fromViewMethod,
                                List.of(
                                        Nodes.argument(fromViewMethod, 0, Values.inputValue(input, 0))
                                )
                        );
                        Nodes.ret("返回", bodyScope, Values.node(fromView));
                    } else
                        Nodes.raise("不支持从视图创建", bodyScope, Values.constant(Expressions.constantString("该对象不支持从视图创建")));
                },
                mergeNode -> {
                }
        );
        return unmapper;
    }

    private Method getSourceMethod(ClassType actualSourceType, Method method) {
        return Objects.requireNonNull(findSourceMethod(actualSourceType, method));
    }

    @Override
    protected ClassType getClassTypeForDeclaration() {
        return getSourceType();
    }

    private @Nullable Method findSourceMethod(ClassType actualSourceType, Method method) {
        if(method == null)
            return null;
        var sourceType = getSourceType();
        if(sourceType == actualSourceType)
            return method;
        else {
            assert actualSourceType.getEffectiveTemplate() == sourceType.getEffectiveTemplate();
            return actualSourceType.findMethodByVerticalTemplate(method.getEffectiveVerticalTemplate());
        }
    }

    public Type substituteType(Type type) {
        int idx = getSourceType().getTypeArguments().indexOf(type);
        return idx != -1 ? getTargetType().getTypeArguments().get(idx) : type;
    }

    private Method findFromViewMethod() {
        return NncUtils.find(getSourceType().getAllMethods(), this::isFromViewMethod);
    }

    private boolean isFromViewMethod(Method method) {
        return method.isStatic() &&
                Objects.equals(method.getCode(), "fromView") &&
                method.getReturnType().equals(getSourceType()) &&
                method.getParameters().size() == 1 && method.getParameters().get(0).getType().equals(getTargetType());
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

    @Override
    public String getQualifiedName() {
        return getSourceType().getName().replace('.', '_') + "_" + getName();
    }

    @Override
    public @Nullable String getQualifiedCode() {
        if (getCode() != null && getSourceType().getCode() != null)
            return getSourceType().getCode().replace('.', '_') + "_" + getCode();
        else
            return null;
    }
}
