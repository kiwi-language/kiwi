package tech.metavm.transpile.ir;

import java.util.List;

public record LocalVariable (
        CodeBlock block,
        boolean immutable,
        List<IRAnnotation> annotations,
        String name,
        IRType type
) implements IValueSymbol {

    public LocalVariable {
        block.addVariable(name, this);
    }

}
