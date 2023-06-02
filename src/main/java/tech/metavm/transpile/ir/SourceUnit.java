package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.List;

public record SourceUnit(List<IRClass> classes) {

    public IRClass publicClass() {
        return NncUtils.find(classes, IRClass::isPublic);
    }

    public IRClass getClass(String name) {
        return NncUtils.findRequired(classes, c -> name.equals(c.getName()));
    }

}
