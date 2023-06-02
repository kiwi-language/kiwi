package tech.metavm.transpile.ir;

import java.util.List;

public record IRLambda(
        IRType type,
        List<IRParameter> parameters,
        LambdaBody body
) implements IRExpression {

}
