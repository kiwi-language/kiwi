package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.LoadAware;
import org.metavm.flow.Flow;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

@EntityType
public class CapturedTypeVariable extends TypeDef implements LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private CapturedTypeScope scope;

    private UncertainType uncertainType;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    public CapturedTypeVariable(Long tmpId, @NotNull UncertainType uncertainType,
                        @NotNull CapturedTypeScope scope) {
        setTmpId(tmpId);
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
        this.scope = scope;
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

    @Override
    public @NotNull CapturedType getType() {
        return new CapturedType(this);
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
    public void onLoad() {
        stage = ResolutionStage.INIT;
    }

    public void write(KlassOutput output) {
        output.writeEntityId(this);
        uncertainType.write(output);
        writeAttributes(output);
    }

    public void read(KlassInput input) {
        uncertainType = (UncertainType) input.readType();
        readAttributes(input);
    }

}
