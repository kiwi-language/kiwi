package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IdentityContext;
import tech.metavm.entity.LocalKey;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("属性")
public interface Property extends ClassMember, LocalKey {

    Long getId();

    String getName();

    void setName(String name);

    Access getAccess();

    void setAccess(Access access);

    @Nullable
    String getCode();

    void setCode(@Nullable String code);

    default String getCodeRequired() {
        return NncUtils.requireNonNull(getCode(), "code is set for type " + getName());
    }

    Type getType();

    void setType(Type type);

    boolean isStatic();

    void setStatic(boolean _static);

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

    default long getIdRequired() {
        return Objects.requireNonNull(getId());
    }
}
