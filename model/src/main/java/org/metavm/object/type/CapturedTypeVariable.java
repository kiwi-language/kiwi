package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.LoadAware;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(2)
@Entity
public class CapturedTypeVariable extends TypeDef implements LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private CapturedTypeScope scope;

    private int uncertainTypeIndex;
    private Reference typeVariable;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    public CapturedTypeVariable(Long tmpId, @NotNull UncertainType uncertainType,
                                @NotNull Reference typeVariable,
                                @NotNull CapturedTypeScope scope) {
        this(tmpId, scope.getConstantPool().addValue(uncertainType), typeVariable, scope);
    }

    public CapturedTypeVariable(Long tmpId, int uncertainTypeIndex,
                        @NotNull Reference typeVariable,
                        @NotNull CapturedTypeScope scope) {
        setTmpId(tmpId);
        this.scope = scope;
        this.typeVariable = typeVariable;
        this.uncertainTypeIndex = uncertainTypeIndex;
        scope.addCapturedTypeVariable(this);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TypeDef.visitBody(visitor);
        visitor.visitInt();
        visitor.visitValue();
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

    public int getUncertainTypeIndex() {
        return uncertainTypeIndex;
    }

    public void setUncertainTypeIndex(int uncertainTypeIndex) {
        this.uncertainTypeIndex = uncertainTypeIndex;
    }

    public void setScope(CapturedTypeScope scope) {
        this.scope = scope;
    }

    public CapturedTypeScope getScope() {
        return scope;
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

    public ResolutionStage getStage() {
        return stage;
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return (org.metavm.entity.Entity) scope;
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
    public void buildJson(Map<String, Object> map) {
        map.put("upperBound", this.getUpperBound().toJson());
        map.put("lowerBound", this.getLowerBound().toJson());
        map.put("uncertainTypeIndex", this.getUncertainTypeIndex());
        map.put("scope", this.getScope());
        map.put("typeVariable", this.getTypeVariable().getStringId());
        map.put("typeVariableReference", this.getTypeVariableReference().toJson());
        map.put("uncertainType", this.getUncertainType().toJson());
        map.put("type", this.getType().toJson());
        map.put("stage", this.getStage().name());
        map.put("attributes", this.getAttributes().stream().map(org.metavm.entity.Attribute::toJson).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_CapturedTypeVariable;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.scope = (CapturedTypeScope) parent;
        this.uncertainTypeIndex = input.readInt();
        this.typeVariable = (Reference) input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeInt(uncertainTypeIndex);
        output.writeValue(typeVariable);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
