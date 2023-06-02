package tech.metavm.transpile.ir;

import javax.annotation.Nullable;

public record LocalVariableDeclarator(
        LocalVariable variable,
        @Nullable IRExpression value
) {


}
