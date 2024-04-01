package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.BuildKeyContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.CapturedTypeKey;
import tech.metavm.object.type.rest.dto.CapturedTypeParam;

@EntityType("捕获类型")
public class CapturedType extends Type implements ITypeVariable {

    @EntityField("范围")
    private final TypeCapturingScope scope;

    @EntityField("不确定类型")
    private final UncertainType uncertainType;

    public CapturedType(UncertainType uncertainType, TypeCapturingScope scope) {
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
    protected boolean isAssignableFrom0(Type that) {
        return this == that;
    }

    @Override
    protected CapturedTypeParam getParam() {
        return new CapturedTypeParam(
                scope.getStringId(),
                uncertainType.getStringId(),
                scope.getCapturedTypeIndex(this)
        );
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

}
