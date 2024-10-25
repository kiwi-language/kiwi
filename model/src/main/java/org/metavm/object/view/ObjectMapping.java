package org.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.LocalKey;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expressions;
import org.metavm.flow.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.ParameterizedTypeKey;
import org.metavm.object.view.rest.dto.*;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public abstract class ObjectMapping extends Mapping implements LocalKey {

    public static final Logger logger = LoggerFactory.getLogger(ObjectMapping.class);

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
                StdFunction.isSourcePresent.get(),
                List.of(Nodes.argument(StdFunction.isSourcePresent.get(), 0, Values.inputValue(input, 0)))
        );
        var ifNode = Nodes.if_(scope.nextNodeName("if"),
                Values.expression(Expressions.not(Expressions.node(isSourcePresent))), null, scope
        );
        var source = Nodes.functionCall(
                scope.nextNodeName("source"), scope,
                StdFunction.getSource.get(),
                List.of(Nodes.argument(StdFunction.getSource.get(), 0, Values.inputValue(input, 0)))
        );
        var castedSource = Nodes.cast(scope.nextNodeName("castedSource"), getSourceType(), Values.node(source), scope);
        Nodes.methodCall(
                scope.nextNodeName("saveView"), scope, Values.node(castedSource),
                writeMethod, List.of(Nodes.argument(writeMethod, 0, Values.inputValue(input, 0)))
        );
        Nodes.ret(scope.nextNodeName("return"), scope, Values.node(castedSource));
        if (fromViewMethod != null) {
            var fromView = Nodes.methodCall(
                    scope.nextNodeName("fromView"), scope,
                    null, fromViewMethod,
                    List.of(
                            Nodes.argument(fromViewMethod, 0, Values.inputValue(input, 0))
                    )
            );
            ifNode.setTarget(fromView);
            Nodes.ret(scope.nextNodeName("return"), scope, Values.node(fromView));
        } else {
            ifNode.setTarget(Nodes.raise(
                    scope.nextNodeName("fromViewNotSupported"),
                    scope,
                    Values.constant(Expressions.constantString("fromView not supported"))
            ));
        }
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
