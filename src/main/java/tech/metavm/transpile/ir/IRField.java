package tech.metavm.transpile.ir;

import java.util.List;

public record IRField(List<Modifier> modifiers,
                      List<IRAnnotation> annotations,
                      String name, IRType type, IRClass declaringClass) implements IValueSymbol {

    public IRField {
        declaringClass.addField(this);
    }

    public boolean isFinal() {
        return modifiers.contains(Modifier.FINAL);
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

}
