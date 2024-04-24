package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.type.*;
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
    private final Klass sourceKlass;
    private final Klass targetKlass;

    public ObjectMapping(Long tmpId, String name, @Nullable String code, Klass sourceKlass, Klass targetKlass, boolean builtin) {
        super(tmpId, name, code, sourceKlass.getType(), targetKlass.getType());
        this.builtin = builtin;
        this.sourceKlass = sourceKlass;
        this.targetKlass = targetKlass;
    }

    @Override
    protected Flow generateMappingCode(CompositeTypeFacade compositeTypeFacade) {
        var scope = Objects.requireNonNull(mapper).newEphemeralRootScope();
        var input = Nodes.input(mapper, compositeTypeFacade);
        var actualSourceType = (ClassType) mapper.getParameter(0).getType();
        var readMethod = getSourceMethod(actualSourceType.resolve(), getReadMethod());
        var view = new MethodCallNode(
                null, "视图", "view",
                targetType,
                scope.getLastNode(), scope,
                Values.inputValue(input, 0),
                readMethod, List.of()
        );
//        Nodes.setSource(Values.node(view), Values.inputValue(input, 0), scope);
        new ReturnNode(null, "结束", "Return", scope.getLastNode(), scope, Values.node(view));
        return mapper;
    }

    protected Flow generateUnmappingCode(CompositeTypeFacade compositeTypeFacade) {
        Objects.requireNonNull(unmapper);
        var actualSourceKlass = ((ClassType) unmapper.getReturnType()).resolve();
        var fromViewMethod = findSourceMethod(actualSourceKlass, findFromViewMethod());
        var writeMethod = getSourceMethod(actualSourceKlass, getWriteMethod());
        var scope = unmapper.newEphemeralRootScope();
        var input = Nodes.input(unmapper, compositeTypeFacade);
        var isSourcePresent = Nodes.functionCall(
                "来源是否存在", scope,
                NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, Values.inputValue(input, 0))),
                compositeTypeFacade
        );
        Nodes.branch(
                "分支", null, scope,
                Values.node(isSourcePresent),
                trueBranch -> {
                    var bodyScope = trueBranch.getScope();
                    var source = Nodes.functionCall(
                            "来源", bodyScope,
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, Values.inputValue(input, 0))),
                            compositeTypeFacade
                    );
                    var castedSource = Nodes.cast("Casted来源", getSourceType(), Values.node(source), bodyScope);
                    Nodes.methodCall(
                            "保存视图", bodyScope, Values.node(castedSource),
                            writeMethod, List.of(Nodes.argument(writeMethod, 0, Values.inputValue(input, 0))),
                            compositeTypeFacade
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
                                ),
                                compositeTypeFacade
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

    private Method getSourceMethod(Klass actualSourceType, Method method) {
        return Objects.requireNonNull(findSourceMethod(actualSourceType, method));
    }

    @Override
    protected Klass getClassTypeForDeclaration() {
        return sourceKlass;
    }

    private @Nullable Method findSourceMethod(Klass actualSourceKlass, Method method) {
        if(method == null)
            return null;
        var sourceType = getSourceType();
        if(actualSourceKlass.isType(sourceType))
            return method;
        else {
            assert actualSourceKlass.getEffectiveTemplate().isType(sourceType.getEffectiveTemplate());
            return actualSourceKlass.findMethodByVerticalTemplate(method.getEffectiveVerticalTemplate());
        }
    }

    public Type substituteType(Type type) {
        int idx = getSourceType().getTypeArguments().indexOf(type);
        return idx != -1 ? getTargetType().getTypeArguments().get(idx) : type;
    }

    private Method findFromViewMethod() {
        return NncUtils.find(sourceKlass.getAllMethods(), this::isFromViewMethod);
    }

    private boolean isFromViewMethod(Method method) {
        return method.isStatic() &&
                Objects.equals(method.getCode(), "fromView") &&
                method.getReturnType().equals(getSourceType()) &&
                method.getParameters().size() == 1 && method.getParameters().get(0).getType().equals(getTargetType());
    }

    @Override
    public ClassInstance map(DurableInstance instance, CallContext callContext) {
        if (instance instanceof ClassInstance)
            return (ClassInstance) super.map(instance, callContext);
        else
            throw new InternalException("Invalid source");
    }

    @Override
    public ClassType getSourceType() {
        return (ClassType) super.getSourceType();
    }

    public Klass getSourceKlass() {
        return sourceKlass;
    }

    public Klass getTargetKlass() {
        return targetKlass;
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
                serializeContext.getId(this),
                getName(),
                getCode(),
                getSourceType().toTypeExpression(serializeContext),
                getTargetType().toTypeExpression(serializeContext),
                isDefault(),
                isBuiltin(),
                NncUtils.map(overridden, Entity::getStringId),
                getParam(serializeContext)
        );
    }

    protected abstract ObjectMappingParam getParam(SerializeContext serializeContext);

    public boolean isDefault() {
        return sourceKlass.getDefaultMapping() == this;
    }

    public void setDefault() {
        sourceKlass.setDefaultMapping(this);
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
