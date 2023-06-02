package tech.metavm.transpile.ir;

import java.util.List;

public record ExpressionForInit(
        List<IRExpression> expressions
) implements ForInit{
}
