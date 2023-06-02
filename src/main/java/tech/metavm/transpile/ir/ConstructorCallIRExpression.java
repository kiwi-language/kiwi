package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;

import java.util.Collections;
import java.util.List;

public record ConstructorCallIRExpression(IRType callingType,
                                          List<IRType> typeArguments,
                                          IRConstructor constructor,
                                          List<IRExpression> arguments) implements IRExpression {

    @Override
    public List<IRType> typeArguments() {
        return Collections.unmodifiableList(typeArguments);
    }

    @Override
    public List<IRExpression> arguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public IRType type() {
        return IRTypeUtil.fromClass(void.class);
    }

}
