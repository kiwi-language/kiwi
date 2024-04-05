package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.CapturedTypeKey;
import tech.metavm.object.type.rest.dto.CapturedTypeParam;

import java.util.Set;

@EntityType("捕获类型")
public class CapturedType extends Type implements ITypeVariable, AfterRemovalAware {

    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("Debug");

    @EntityField("范围")
    private CapturedTypeScope scope;

    @EntityField("不确定类型")
    private final UncertainType uncertainType;

    public CapturedType(UncertainType uncertainType, CapturedTypeScope scope) {
        super("CaptureOf" + uncertainType.getName(), null,
                true, true, TypeCategory.CAPTURED);
        this.scope = scope;
        this.uncertainType = uncertainType;
        scope.addCapturedType(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCapturedType(this);
    }

    @Override
    public CapturedTypeKey getTypeKey() {
        return new CapturedTypeKey(
                scope.getStringId(),
                uncertainType.getStringId(),
                scope.getCapturedTypeIndex(this)
        );
    }

    @Override
    public Type getUpperBound() {
        return uncertainType.getUpperBound();
    }

    @Override
    public Type getLowerBound() {
        return uncertainType.getLowerBound();
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
        return new CapturedTypeParam(
                serializeContext.getId(scope),
                serializeContext.getId(uncertainType),
                scope.getCapturedTypeIndex(this)
        );
    }

    public void setScope(CapturedTypeScope scope) {
        if(this.scope != DummyCapturedTypeScope.INSTANCE)
            throw new IllegalStateException("Scope is already set");
        this.scope = scope;
        scope.addCapturedType(this);
    }

    public CapturedTypeScope getScope() {
        return scope;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return scope.getInternalName(current) + ".CaptureOf" + uncertainType.getInternalName(current) +
                scope.getCapturedTypeIndex(this);
    }

    public UncertainType getUncertainType() {
        return uncertainType;
    }

    @Override
    protected void getCapturedTypes(Set<CapturedType> capturedTypes) {
        capturedTypes.add(this);
    }

    @Override
    public void afterRemoval(IEntityContext context) {
        DEBUG_LOGGER.info("CapturedType.afterRemoval called");
    }
}
