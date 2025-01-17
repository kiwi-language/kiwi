package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.api.JsonIgnore;
import org.metavm.entity.LocalKey;
import org.metavm.object.instance.core.Id;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@Entity
public interface Property extends ClassMember, LocalKey {

    @Nullable Id tryGetId();

    default Id getIdNotNull() {
        return requireNonNull(tryGetId(), () -> "Id is not set for " + this);
    }

    String getName();

    void setName(String name);

    Access getAccess();

    void setAccess(Access access);

    Type getType(TypeMetadata typeMetadata);

    default Type getType() {
        return getType(getDeclaringType().getConstantPool());
    }

//    void setType(Type type);

    boolean isStatic();

    void setStatic(boolean _static);

    PropertyRef getRef();

    MetadataState getState();

    void setState(MetadataState state);

    @JsonIgnore
    default boolean isPublic() {
        return getAccess() == Access.PUBLIC;
    }

    @JsonIgnore
    default boolean isPrivate() {
        return getAccess() == Access.PRIVATE;
    }

    @JsonIgnore
    default boolean isPackagePrivate() {
        return getAccess() == Access.PACKAGE;
    }

    @JsonIgnore
    default boolean isProtected() {
        return getAccess() == Access.PROTECTED;
    }

//    default Id getId() {
//        return Objects.requireNonNull(tryGetId());
//    }

    default boolean idEquals(Id id) {
        var selfId = tryGetId();
        return selfId != null && selfId.equals(id);
    }

    String getQualifiedName();
}
