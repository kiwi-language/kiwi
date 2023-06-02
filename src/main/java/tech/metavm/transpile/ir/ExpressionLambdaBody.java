package tech.metavm.transpile.ir;

public record ExpressionLambdaBody(
        IRExpression expression,
        CodeBlock virtualBlock
) implements LambdaBody{
}
