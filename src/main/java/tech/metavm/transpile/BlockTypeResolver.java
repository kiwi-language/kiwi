package tech.metavm.transpile;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BlockTypeResolver<T extends Statement> {

    public static <E extends Statement> IRType resolve(Class<E> statementClass,
                              Function<E, IRExpression> extractExpression,
                              Iterable<Statement> statementIterable) {
        return new BlockTypeResolver<E>(statementClass, extractExpression, statementIterable).resolvedType();
    }

    private final Function<T, IRExpression> extractExpression;
    private final Class<T> statementClass;
    private final Iterable<Statement> statementIterable;
    private final List<IRType> types = new ArrayList<>();
    private final IRType resolvedType;

    private  BlockTypeResolver(Class<T> statementClass,
                             Function<T, IRExpression> extractExpression,
                             Iterable<Statement> statementIterable) {
        this.statementClass = statementClass;
        this.extractExpression = extractExpression;
        this.statementIterable = statementIterable;
        resolvedType = resolve();
    }

    private IRType resolve() {
        statementIterable.forEach(this::processStatement);
        if(types.isEmpty()) {
            return IRTypeUtil.voidType();
        }
        else {
            return IRTypeUtil.getCompatibleType(types);
        }
    }

    public IRType resolvedType() {
        return resolvedType;
    }

    private void processStatement(Statement statement) {
        if(statementClass.isInstance(statement)) {
            NncUtils.invokeIfNotNull(
                    extractExpression.apply(statementClass.cast(statement)),
                    this::addExpression
            );
        }
        statement.getChildren().forEach(this::processStatement);
    }

    private void addExpression(IRExpression expression) {
        types.add(expression.type());
    }

}
