package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.CapturedTypeVariableDTO;
import tech.metavm.object.type.rest.dto.TypeDefDTO;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CapturedTypeVariable extends TypeDef implements GenericElement, PostRemovalAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @EntityField("范围")
    private CapturedTypeScope scope;

    @ChildEntity("不确定类型")
    private final UncertainType uncertainType;

    @ChildEntity("捕获流程列表")
    private final ReadWriteArray<Flow> capturedFlows = addChild(new ReadWriteArray<>(Flow.class), "capturedFlows");

    @ChildEntity("捕获复合类型列表")
    private final ReadWriteArray<Type> capturedCompositeTypes = addChild(new ReadWriteArray<>(Type.class), "capturedCompositeTypes");

    @CopyIgnore
    @EntityField("复制来源")
    private @Nullable CapturedType copySource;

    public CapturedTypeVariable(@NotNull UncertainType uncertainType,
                        @NotNull CapturedTypeScope scope) {
        this.scope = scope;
        this.uncertainType = uncertainType.copy();
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

    public void setCapturedFlows(List<Flow> capturedFlows) {
        this.capturedFlows.reset(capturedFlows);
    }

    public void setCapturedCompositeTypes(List<Type> capturedCompositeTypes) {
        this.capturedCompositeTypes.reset(capturedCompositeTypes);
    }

    public void addCapturedFlow(Flow flow) {
        capturedFlows.add(flow);
    }

    public void addCapturedCompositeType(Type type) {
        capturedCompositeTypes.add(type);
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

    @Override
    public void postRemove(IEntityContext context) {
        List<Entity> removals = new ArrayList<>(capturedFlows);
        removals.addAll(capturedCompositeTypes);
        if (DebugEnv.debugging) {
            debugLogger.info("{}.afterRemoval called", EntityUtils.getEntityPath(this));
            for (Entity removal : removals) {
                boolean alreadyRemoved = context.isRemoved(removal);
                debugLogger.info("Removing entity {}, already removed: {}",
                        EntityUtils.getEntityDesc(removal) + "/" + removal.getStringId(), alreadyRemoved);
                if (alreadyRemoved && removal instanceof Type removedType) {
                    var capturedTypes = new IdentitySet<CapturedType>();
                    for (Type componentType : Types.getComponentTypes(removedType)) {
                        capturedTypes.addAll(componentType.getCapturedTypes());
                    }
                    debugLogger.info("Component captured types: {}", NncUtils.join(capturedTypes, EntityUtils::getEntityPath));
                }
            }
        }
        context.batchRemove(removals);
    }

    @Nullable
    @Override
    public CapturedType getCopySource() {
        return copySource;
    }

    @Override
    public void setCopySource(Object copySource) {
        this.copySource = (CapturedType) copySource;
    }

    public Collection<Type> getCapturedCompositeTypes() {
        return capturedCompositeTypes.toList();
    }

    public Collection<Flow> getCapturedFlows() {
        return capturedFlows.toList();
    }

    @Override
    public CapturedType getType() {
        return null;
    }

    @Override
    public TypeDefDTO toDTO(SerializeContext serContext) {
        return new CapturedTypeVariableDTO(
                serContext.getId(this),
                uncertainType.toTypeExpression(serContext),
                serContext.getId(scope),
                scope.getCapturedTypeVariableIndex(this)
        );
    }
}
