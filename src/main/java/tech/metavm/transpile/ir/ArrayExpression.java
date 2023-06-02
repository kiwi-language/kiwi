package tech.metavm.transpile.ir;

import java.util.List;

public record ArrayExpression(
        IRArrayType type,
        List<IRExpression> elements
) implements IRExpression {

}

