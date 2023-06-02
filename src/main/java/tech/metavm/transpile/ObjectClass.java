package tech.metavm.transpile;

import tech.metavm.transpile.ir.*;

import java.util.List;
import java.util.Map;

public class ObjectClass extends IRClass {

    private static final ObjectClass INSTANCE = new ObjectClass();

    public static ObjectClass getInstance() {
        return INSTANCE;
    }

    private ObjectClass() {
        super(
                "Object",
                IRClassKind.CLASS,
                new IRPackage("java.lang"),
                List.of(Modifier.PUBLIC),
                List.of(),
                null,
                null
        );
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        return true;
    }

    @Override
    public List<IRType> getReferences() {
        return List.of();
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return this;
    }

    @Override
    public String toString() {
        return "IRClass Object";
    }
}
