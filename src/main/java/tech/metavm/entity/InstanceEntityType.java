//package tech.metavm.entity;
//
//import tech.metavm.object.instance.Instance;
//import tech.metavm.object.meta.EnumConstantRT;
//import tech.metavm.object.meta.IdConstants;
//import tech.metavm.object.meta.Type;
//import tech.metavm.user.RoleRT;
//import tech.metavm.user.UserRT;
//import tech.metavm.util.NncUtils;
//
//import java.util.function.Predicate;
//
//public enum InstanceEntityType {
//
//    USER(UserRT.class, idEquals(IdConstants.USER.ID)),
//    ROLE(RoleRT.class, idEquals(IdConstants.ROLE.ID)),
//    ENUM_CONSTANT(EnumConstantRT.class, Type::isEnum);
//
//    private static Predicate<Type> idEquals(long id) {
//        return t -> t.getId() == id;
//    }
//
//    private final Class<? extends Instance> entityType;
//    private final Predicate<Type> typePredicate;
//
//    InstanceEntityType(Class<? extends Instance> klass, Predicate<Type> typePredicate) {
//        this.entityType = klass;
//        this.typePredicate = typePredicate;
//    }
//
//    private boolean isMatched(Type type) {
//        return typePredicate.test(type);
//    }
//
//    public static InstanceEntityType getByType(Type type) {
//        return NncUtils.find(values(), t -> t.isMatched(type));
//    }
//
//    public static InstanceEntityType getByEntityType(Class<? extends Instance> entityType) {
//        return NncUtils.findRequired(values(), v -> v.entityType.equals(entityType));
//    }
//
//    public Class<? extends Instance> getEntityType() {
//        return entityType;
//    }
//}
