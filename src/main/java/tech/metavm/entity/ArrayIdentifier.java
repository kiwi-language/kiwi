package tech.metavm.entity;

public record ArrayIdentifier(
        String name
) {

    public static ArrayIdentifier typeFields(Class<?> javaType) {
        return new ArrayIdentifier(javaType.getName() + ".fields");
    }

    public static ArrayIdentifier typeConstraints(Class<?> javaType) {
        return new ArrayIdentifier(javaType.getName() + ".constraints");
    }

}
