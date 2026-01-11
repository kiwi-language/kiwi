package org.manul.object.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.entity.ElementVisitor;
import org.manul.entity.LoadAware;
import org.manul.flow.Flow;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.wire.Parent;
import org.manul.wire.Wire;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(2)
@Entity
public class CapturedTypeVariable extends TypeDef implements LoadAware {

    @Getter
    private final String name;
    @Getter
    @Setter
    @Parent
    private CapturedTypeScope scope;

    @Setter
    @Getter
    private int uncertainTypeIndex;
    @Setter
    private Reference typeVariable;

    @Getter
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public CapturedTypeVariable(@NotNull Id id, String name, @NotNull UncertainType uncertainType,
                                @NotNull Reference typeVariable,
                                @NotNull CapturedTypeScope scope) {
        this(id, name, scope.getConstantPool().addValue(uncertainType), typeVariable, scope);
    }

    public CapturedTypeVariable(@NotNull Id id, String name, int uncertainTypeIndex,
                                @NotNull Reference typeVariable,
                                @NotNull CapturedTypeScope scope) {
        super(id);
        this.name = name;
        this.scope = scope;
        this.typeVariable = typeVariable;
        this.uncertainTypeIndex = uncertainTypeIndex;
        scope.addCapturedTypeVariable(this);
    }

    public Type getUpperBound() {
        return getUncertainType().getUpperBound();
    }

    public Type getLowerBound() {
        return getUncertainType().getLowerBound();
    }

    public void setUncertainType(UncertainType uncertainType) {
        this.uncertainTypeIndex = scope.getConstantPool().addValue(uncertainType);
    }

    public TypeVariable getTypeVariable() {
        return (TypeVariable) typeVariable.resolveDurable();
    }

    public Reference getTypeVariableReference() {
        return typeVariable;
    }

    public String getInternalName(@Nullable Flow current) {
        return scope.getInternalName(current) + ".CaptureOf" + getUncertainType().getInternalName(current) +
                scope.getCapturedTypeVariableIndex(this);
    }

    public UncertainType getUncertainType() {
        return (UncertainType) scope.getConstantPool().getType(uncertainTypeIndex);
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

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
    }

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
        return (org.manul.entity.Entity) scope;
    }

    @Override
    public String getTitle() {
        return "capture of " + getUncertainType().getTypeDesc();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCapturedTypeVariable(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(typeVariable);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
