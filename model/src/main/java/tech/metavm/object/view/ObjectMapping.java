package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.view.rest.dto.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public abstract class ObjectMapping extends Mapping implements LocalKey {

    public static final Logger logger = LoggerFactory.getLogger(ObjectMapping.class);

    @ChildEntity
    protected final ReadWriteArray<ObjectMapping> overridden =
            addChild(new ReadWriteArray<>(ObjectMapping.class), "overridden");
    private final boolean builtin;
    private final Klass sourceKlass;

    public ObjectMapping(Long tmpId, String name, @Nullable String code, Klass sourceKlass, ClassType targetType, boolean builtin) {
        super(tmpId, name, code, sourceKlass.getType(), targetType);
        this.builtin = builtin;
        this.sourceKlass = sourceKlass;
    }

    @Override
    protected Flow generateMappingCode(boolean generateReadMethod) {
        var scope = Objects.requireNonNull(mapper).newEphemeralRootScope();
        var input = Nodes.input(mapper);
        var actualSourceType = (ClassType) mapper.getParameter(0).getType();
        var readMethod = getSourceMethod(actualSourceType.resolve(), getReadMethod());
        var view = new MethodCallNode(
                null, scope.nextNodeName("view"), null,
                scope.getLastNode(), scope,
                Values.inputValue(input, 0),
                readMethod.getRef(), List.of()
        );
        new ReturnNode(null, scope.nextNodeName("return"), null, scope.getLastNode(), scope, Values.node(view));
        return mapper;
    }

    protected Flow generateUnmappingCode(boolean generateWriteMethod) {
        Objects.requireNonNull(unmapper);
        var actualSourceKlass = ((ClassType) unmapper.getReturnType()).resolve();
        var fromViewMethod = findSourceMethod(actualSourceKlass, findFromViewMethod());
        var writeMethod = getSourceMethod(actualSourceKlass, getWriteMethod());
        var scope = unmapper.newEphemeralRootScope();
        var input = Nodes.input(unmapper);
        var isSourcePresent = Nodes.functionCall(
                scope.nextNodeName("isSourcePresent"), scope,
                NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, Values.inputValue(input, 0)))
        );
        Nodes.branch(
                scope.nextNodeName("branch"), null, scope,
                Values.node(isSourcePresent),
                trueBranch -> {
                    var bodyScope = trueBranch.getScope();
                    var source = Nodes.functionCall(
                            scope.nextNodeName("source"), bodyScope,
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, Values.inputValue(input, 0)))
                    );
                    var castedSource = Nodes.cast(scope.nextNodeName("castedSource"), getSourceType(), Values.node(source), bodyScope);
                    Nodes.methodCall(
                            scope.nextNodeName("saveView"), bodyScope, Values.node(castedSource),
                            writeMethod, List.of(Nodes.argument(writeMethod, 0, Values.inputValue(input, 0)))
                    );
                    Nodes.ret(scope.nextNodeName("return"), bodyScope, Values.node(castedSource));
                },
                falseBranch -> {
                    var bodyScope = falseBranch.getScope();
                    if (fromViewMethod != null) {
                        var fromView = Nodes.methodCall(
                                scope.nextNodeName("fromView"), bodyScope,
                                null, fromViewMethod,
                                List.of(
                                        Nodes.argument(fromViewMethod, 0, Values.inputValue(input, 0))
                                )
                        );
                        Nodes.ret(scope.nextNodeName("return"), bodyScope, Values.node(fromView));
                    } else
                        Nodes.raise(scope.nextNodeName("fromViewNotSupported"), bodyScope, Values.constant(Expressions.constantString("fromView not supported")));
                },
                mergeNode -> {
                }
        );
        return unmapper;
    }

    private Method getSourceMethod(Klass actualSourceType, Method method) {
        var found = findSourceMethod(actualSourceType, method);
        if (found != null) {
            return found;
        }
        throw new NullPointerException("Can not find source method of " + method.getQualifiedName()
                + " in klass " + actualSourceType.getTypeDesc()
                + ", mapping.sourceType: " + getSourceType().resolve().getTypeDesc());
    }

    @Override
    protected Klass getClassTypeForDeclaration() {
        return sourceKlass;
    }

    private @Nullable Method findSourceMethod(Klass actualSourceKlass, Method method) {
        if (method == null) {
            return null;
        }
        var sourceType = getSourceType();
        if (actualSourceKlass.isType(sourceType)) {
            return method;
        }
        else {
            assert actualSourceKlass.getEffectiveTemplate().isType(sourceType.getEffectiveTemplate()) :
                    sourceType.getEffectiveTemplate().toExpression() + " is not a type of " + actualSourceKlass.getEffectiveTemplate().getTypeDesc();
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
        return getTargetType().resolve();
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
                serializeContext.getStringId(this),
                getName(),
                getCode(),
                getSourceType().toExpression(serializeContext, null),
                getTargetType().toExpression(serializeContext, null),
                isDefault(),
                isBuiltin(),
                NncUtils.map(overridden, Entity::getStringId),
                getParam(serializeContext)
        );
    }

    protected abstract ObjectMappingParam getParam(SerializeContext serializeContext);

    public ObjectMapping getEffectiveTemplate() {
        return sourceKlass.isParameterized() ? (ObjectMapping) Objects.requireNonNull(copySource) : this;
    }

    public ObjectMappingRef getRef() {
        return new ObjectMappingRef(sourceKlass.getType(), getEffectiveTemplate());
    }

    public boolean isDefault() {
        return sourceKlass.getDefaultMapping() == this;
    }

    public void setDefault() {
        sourceKlass.setDefaultMapping(this);
    }

    public MappingKey toKey() {
        try (var serContext = SerializeContext.enter()) {
            if(sourceKlass.isParameterized())
                return new ParameterizedMappingKey((ParameterizedTypeKey) sourceType.toTypeKey(), serContext.getStringId(getEffectiveTemplate()));
            else
                return new DirectMappingKey(serContext.getId(this));
        }
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
