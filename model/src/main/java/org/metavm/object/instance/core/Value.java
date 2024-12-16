package org.metavm.object.instance.core;

import org.metavm.entity.IEntityContext;
import org.metavm.entity.NoProxy;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.ClosureContext;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.Type;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class Value {

    public Value() {
    }

    @NoProxy
    public abstract Type getType();

    @NoProxy
    public boolean isValue() {
        return false;
    }

    @NoProxy
    public boolean isNull() {
        return false;
    }

    @NoProxy
    public boolean isNotNull() {
        return !isNull();
    }

    @NoProxy
    public boolean isPassword() {
        return getType().isPassword();
    }

    public StringValue toStringInstance() {
        return Instances.stringInstance(getTitle());
    }

    @NoProxy
    public boolean isArray() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    @NoProxy
    public boolean isPrimitive() {
        return false;
    }

    @NoProxy
    public boolean isNotPrimitive() {
        return !isPrimitive();
    }

    public abstract boolean isReference();

    public String toStringValue() {
        throw new UnsupportedOperationException();
    }

    public boolean isEphemeral() {
        return false;
    }

    public boolean shouldSkipWrite() {
        return false;
    }

    public abstract @Nullable Id tryGetId();

    public Id getId() {
        return Objects.requireNonNull(tryGetId());
    }

    public @Nullable String getStringId() {
        return NncUtils.get(tryGetId(), Id::toString);
    }

    public @Nullable String getStringIdForDTO() {
//        var id = tryGetId();
//        if(id instanceof PhysicalId physicalId)
//            return new TypedPhysicalId(physicalId.isArray(), physicalId.getTreeId(), physicalId.getNodeId(), getType().toTypeKey()).toString();
//        else
//            return NncUtils.get(id, Id::toString);
        return getStringId();
    }

    public abstract FieldValue toFieldValueDTO();

    public abstract String getTitle();


    @Override
    @NoProxy
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    @NoProxy
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public abstract void writeInstance(InstanceOutput output) ;

    public abstract void write(MvOutput output);

    public abstract Object toSearchConditionValue();

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    protected InstanceDTO toDTO(InstanceParam param) {
        try (var serContext = SerializeContext.enter()) {
            return new InstanceDTO(
                    getStringIdForDTO(),
                    getType().toExpression(serContext),
                    getType().getName(),
                    getTitle(),
                    param
            );
        }
    }

    protected abstract InstanceParam getParam();

    @NoProxy
    public abstract <R> R accept(ValueVisitor<R> visitor);

    public abstract <R> void acceptReferences(ValueVisitor<R> visitor);

    public abstract <R> void acceptChildren(ValueVisitor<R> visitor);

    public String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }

    protected abstract void writeTree(TreeWriter treeWriter);

    public abstract  boolean isMutable();

    public abstract Object toJson(IEntityContext context);

    public ClassInstance resolveObject() {
        return (ClassInstance) resolveDurable();
    }

    public ArrayInstance resolveArray() {
        return (ArrayInstance) resolveDurable();
    }

    public Instance resolveDurable(){
        return ((Reference) this).resolve();
    }

    public Value toStackValue() {
        return this;
    }

    public ClosureContext getClosureContext() {
        return null;
    }
}
