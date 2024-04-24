package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.CapturedTypeKey;
import tech.metavm.object.type.rest.dto.CapturedTypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

// TODO make CapturedType the sub type of or the same type as VariableType
@EntityType("捕获类型")
public class CapturedType extends Type {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final CapturedTypeVariable variable;

    public CapturedType(CapturedTypeVariable variable) {
        super("CaptureOf" + variable.getUncertainType().getName(), null,
                true, true, TypeCategory.CAPTURED);
        this.variable = variable;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCapturedType(this);
    }

    @Override
    public CapturedTypeKey toTypeKey() {
        return new CapturedTypeKey(variable.getStringId());
    }

    @Override
    public Type getUpperBound() {
        return variable.getUpperBound();
    }

    @Override
    public Type getLowerBound() {
        return variable.getLowerBound();
    }

    @Override
    public boolean isCaptured() {
        return true;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return this == that;
    }

    @Override
    protected CapturedTypeParam getParam(SerializeContext serializeContext) {
        return new CapturedTypeParam(serializeContext.getId(variable));
    }

    @Override
    public String getTypeDesc() {
        return variable.getScope().getScopeName() + "_" + name;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        var scope = variable.getScope();
        return scope.getInternalName(current) + ".CaptureOf" + variable.getUncertainType().getInternalName(current) +
                scope.getCapturedTypeVariableIndex(variable);
    }

    @Override
    public Type copy() {
        return new CapturedType(variable);
    }

    @Override
    public String toTypeExpression(SerializeContext serializeContext) {
        return "#" + serializeContext.getId(variable);
    }

    @Override
    public void write0(InstanceOutput output) {
        output.writeId(variable.getId());
    }

    public static CapturedType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new CapturedType((CapturedTypeVariable) typeDefProvider.getTypeDef(input.readId()));
    }

    public UncertainType getUncertainType() {
        return variable.getUncertainType();
    }

    @Override
    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        capturedTypes.add(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CapturedType that && variable == that.variable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), variable);
    }
}
