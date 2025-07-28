package org.metavm.object.type;

public record DeployError(String message) implements DeployDiag {

    public static DeployError newFieldMissingMigrationFunc(String fieldName) {
        return new DeployError("Migration function is missing for new field: " + fieldName);
    }

    public static DeployError typeChangedFieldMissingMigrationFunc(String fieldName) {
        return new DeployError("Migration function is missing for type-changed field: " + fieldName);
    }

    public static DeployError superChangedClassMissingMigrationFunc(String className) {
        return new DeployError("Migration function is missing for class with a changed super class: " + className);
    }

}
