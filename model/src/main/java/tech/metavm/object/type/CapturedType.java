package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.CapturedTypeKey;
import tech.metavm.object.type.rest.dto.CapturedTypeParam;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@EntityType("捕获类型")
public class CapturedType extends Type implements ITypeVariable, PostRemovalAware, GenericElement {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    @EntityField("范围")
    private CapturedTypeScope scope;

    @EntityField("不确定类型")
    private final UncertainType uncertainType;

    @ChildEntity("捕获流程列表")
    private final ReadWriteArray<Flow> capturedFlows = addChild(new ReadWriteArray<>(Flow.class), "capturedFlows");

    @ChildEntity("捕获复合类型列表")
    private final ReadWriteArray<Type> capturedCompositeTypes = addChild(new ReadWriteArray<>(Type.class), "capturedCompositeTypes");

    @CopyIgnore
    @EntityField("复制来源")
    private @Nullable CapturedType copySource;

    public CapturedType(@NotNull UncertainType uncertainType,
                        @NotNull CapturedTypeScope scope) {
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

    @Override
    public boolean isCaptured() {
        return true;
    }

    @Override
    protected boolean isAssignableFrom0(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        return this == that;
    }

    @Override
    public boolean equals(Type that, @Nullable Map<TypeVariable, ? extends Type> mapping) {
        return this.equals(that);
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
        if (this.scope != DummyCapturedTypeScope.INSTANCE)
            throw new IllegalStateException("Scope is already set");
        this.scope = scope;
        scope.addCapturedType(this);
    }

    public CapturedTypeScope getScope() {
        return scope;
    }

    @Override
    public String getTypeDesc() {
        return scope.getScopeName() + "_" + name;
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
    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        capturedTypes.add(this);
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
}
