package tech.metavm.transpile.ir;

import java.util.List;

public record LocalVariableDeclaration(
        List<IRAnnotation> annotations,
        boolean immutable,
        IRType type,
        List<LocalVariableDeclarator> declarators
) implements Statement {

}
