package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.CapturedTypeVariableDTO;
import tech.metavm.object.type.rest.dto.TypeDefDTO;

import javax.annotation.Nullable;

public class CapturedTypeVariable extends TypeDef implements GenericElement, LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @EntityField("范围")
    private CapturedTypeScope scope;

    private UncertainType uncertainType;

    @CopyIgnore
    @EntityField("复制来源")
    private @Nullable CapturedTypeVariable copySource;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    public CapturedTypeVariable(@NotNull UncertainType uncertainType,
                        @NotNull CapturedTypeScope scope) {
        this.scope = scope;
        this.uncertainType = uncertainType;
        scope.addCapturedTypeVariable(this);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCapturedTypeVariable(this);
    }

    public Type getUpperBound() {
        return uncertainType.getUpperBound();
    }

    public Type getLowerBound() {
        return uncertainType.getLowerBound();
    }

    public void setUncertainType(UncertainType uncertainType) {
        this.uncertainType = uncertainType;
    }

    public void setScope(CapturedTypeScope scope) {
        if (this.scope != DummyCapturedTypeScope.INSTANCE)
            throw new IllegalStateException("Scope is already set");
        this.scope = scope;
        scope.addCapturedTypeVariable(this);
    }

    public CapturedTypeScope getScope() {
        return scope;
    }

    public String getInternalName(@Nullable Flow current) {
        return scope.getInternalName(current) + ".CaptureOf" + uncertainType.getInternalName(current) +
                scope.getCapturedTypeVariableIndex(this);
    }

    public UncertainType getUncertainType() {
        return uncertainType;
    }

    @Nullable
    @Override
    public CapturedTypeVariable getCopySource() {
        return copySource;
    }

    @Override
    public void setCopySource(Object copySource) {
        this.copySource = (CapturedTypeVariable) copySource;
    }

    @Override
    public @NotNull CapturedType getType() {
        return new CapturedType(this);
    }

    @Override
    public TypeDefDTO toDTO(SerializeContext serContext) {
        return new CapturedTypeVariableDTO(
                serContext.getStringId(this),
                uncertainType.toExpression(serContext, null),
                serContext.getStringId(scope),
                scope.getCapturedTypeVariableIndex(this)
        );
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var curStage = this.stage;
        this.stage = stage;
        return curStage;
    }

    public ResolutionStage getStage() {
        return stage;
    }

    @Override
    public void onLoad(IEntityContext context) {
        stage = ResolutionStage.INIT;
    }
}
