package tech.metavm.entity;

import tech.metavm.object.meta.EnumConstant;
import tech.metavm.object.meta.StdTypeConstants;
import tech.metavm.object.meta.Type;
import tech.metavm.user.RoleRT;
import tech.metavm.user.UserRT;
import tech.metavm.util.NncUtils;

import java.util.function.Predicate;

public enum InstanceEntityType {

    USER(UserRT.class, idEquals(StdTypeConstants.USER.ID)),
    ROLE(RoleRT.class, idEquals(StdTypeConstants.ROLE.ID)),
    ENUM_CONSTANT(EnumConstant.class, Type::isEnum);

    private static Predicate<Type> idEquals(long id) {
        return t -> t.getId() == id;
    }

    private final Class<? extends InstanceEntity> entityType;
    private final Predicate<Type> typePredicate;

    InstanceEntityType(Class<? extends InstanceEntity> klass, Predicate<Type> typePredicate) {
        this.entityType = klass;
        this.typePredicate = typePredicate;
    }

    private boolean isMatched(Type type) {
        return typePredicate.test(type);
    }

    public static InstanceEntityType getByType(Type type) {
        return NncUtils.findRequired(values(), t -> t.isMatched(type));
    }

    public static InstanceEntityType getByEntityType(Class<? extends InstanceEntity> entityType) {
        return NncUtils.findRequired(values(), v -> v.entityType.equals(entityType));
    }

    public Class<? extends InstanceEntity> getEntityType() {
        return entityType;
    }
}
