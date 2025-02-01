package org.metavm.object.instance.core;

import org.metavm.entity.NoProxy;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.ClosureContext;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Objects;

public interface Value {

    @NoProxy
    Type getValueType();

    @NoProxy
    default boolean isValue() {
        return false;
    }

    @NoProxy
    default boolean isNull() {
        return false;
    }

    @NoProxy
    default boolean isNotNull() {
        return !isNull();
    }

    @NoProxy
    default boolean isPassword() {
        return getValueType().isPassword();
    }

    default Value toStringInstance() {
        return Instances.stringInstance(getTitle());
    }

    @NoProxy
    default boolean isArray() {
        return false;
    }

    default boolean isObject() {
        return false;
    }

    @NoProxy
    default boolean isPrimitive() {
        return false;
    }

    default String toStringValue() {
        throw new UnsupportedOperationException();
    }

    default boolean isEphemeral() {
        return false;
    }

    default boolean shouldSkipWrite() {
        return false;
    }

    @Nullable Id tryGetId();

    default Id getId() {
        return Objects.requireNonNull(tryGetId());
    }

    default @Nullable String getStringId() {
        return Utils.safeCall(tryGetId(), Id::toString);
    }

    default @Nullable String getStringIdForDTO() {
//        var id = tryGetId();
//        if(id instanceof PhysicalId physicalId)
//            return new TypedPhysicalId(physicalId.isArray(), physicalId.getTreeId(), physicalId.getNodeId(), getType().toTypeKey()).toString();
//        else
//            return NncUtils.get(id, Id::toString);
        return getStringId();
    }

    FieldValue toFieldValueDTO();

    String getTitle();


    void writeInstance(MvOutput output) ;

    void write(MvOutput output);

    Object toSearchConditionValue();

    default InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    default InstanceDTO toDTO(InstanceParam param) {
        try (var serContext = SerializeContext.enter()) {
            return new InstanceDTO(
                    getStringIdForDTO(),
                    getValueType().toExpression(serContext),
                    getValueType().getName(),
                    getTitle(),
                    param
            );
        }
    }

    InstanceParam getParam();

    @NoProxy
    <R> R accept(ValueVisitor<R> visitor);

    default String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }

    void writeTree(TreeWriter treeWriter);

    Object toJson();

    default ClassInstance resolveObject() {
        return (ClassInstance) resolveDurable();
    }

    default ArrayInstance resolveArray() {
        return (ArrayInstance) resolveDurable();
    }

    default Instance resolveDurable(){
        return ((Reference) this).get();
    }

    default MvInstance resolveMv() {
        return (MvInstance) ((Reference) this).get();
    }

    default MvClassInstance resolveMvObject() {
        return (MvClassInstance) ((Reference) this).get();
    }

    default Value toStackValue() {
        return this;
    }

    default ClosureContext getClosureContext() {
        return null;
    }

    default String stringValue() {
        throw new UnsupportedOperationException();
    }

}
