package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.LocalKey;
import org.metavm.object.instance.core.Id;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType
public interface Property extends ClassMember, LocalKey {

    @Nullable Id tryGetId();

    default Id getIdNotNull() {
        return requireNonNull(tryGetId(), () -> "Id is not set for " + this);
    }

    String getName();

    void setName(String name);

    Access getAccess();

    void setAccess(Access access);

    @Nullable
    String getCode();

    void setCode(@Nullable String code);

    default String getCodeNotNull() {
        return NncUtils.requireNonNull(getCode(), "Code is not set for " + this);
    }

    Type getType();

//    void setType(Type type);

    boolean isStatic();

    void setStatic(boolean _static);

    PropertyRef getRef();

    MetadataState getState();

    void setState(MetadataState state);

    default boolean isPublic() {
        return getAccess() == Access.PUBLIC;
    }

    default boolean isPrivate() {
        return getAccess() == Access.PRIVATE;
    }

    default boolean isPackagePrivate() {
        return getAccess() == Access.PACKAGE;
    }

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

}
