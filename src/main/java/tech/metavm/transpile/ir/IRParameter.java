package tech.metavm.transpile.ir;

import java.util.List;

public record IRParameter (
    boolean immutable,
    List<IRAnnotation> annotations,
    String name,
    IRType type,
    boolean varArgs
) {

    public void defineVariable(CodeBlock block) {
        new LocalVariable(
                block,
                immutable,
                annotations,
                name,
                varArgs ? type.getArrayType() : type
        );
    }

}
