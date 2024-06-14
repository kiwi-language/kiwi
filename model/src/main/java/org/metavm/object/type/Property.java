package org.metavm.object.type;

import org.metavm.entity.EntityType;
import org.metavm.entity.LocalKey;
import org.metavm.object.instance.core.Id;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@EntityType
public interface Property extends ClassMember, LocalKey {

    @Nullable Id tryGetId();

    default Id getIdRequired() {
        return requireNonNull(tryGetId());
    }

    String getName();

    void setName(String name);

    Access getAccess();

    void setAccess(Access access);

    @Nullable
    String getCode();

    void setCode(@Nullable String code);

    default String getCodeRequired() {
        return NncUtils.requireNonNull(getCode(), "code is null for property " + getName());
    }

    Type getType();

//    void setType(Type type);

    boolean isStatic();

    void setStatic(boolean _static);

    PropertyRef getRef();

    MetadataState getState();

    default boolean isReady() {
        return getState() == MetadataState.READY;
    }

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
