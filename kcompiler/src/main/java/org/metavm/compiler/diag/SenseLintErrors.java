package org.metavm.compiler.diag;

public class SenseLintErrors {

    public static final Error variableNameEndsWithId = create("sense-lint.variable.name.ends.with.id");
    public static final Error variableNameMatchesClassName = create("sense-lint.variable.name.matches.class.name");
    public static final Error variableNameMatchesPluralClassName = create("sense-lint.variable.name.matches.plural.class.name");

    private static Error create(String code, Object...args) {
        return new Error(code, args);
    }


}
