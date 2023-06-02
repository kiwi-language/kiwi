package tech.metavm.transpile.ir;

import java.util.List;

public record GuardedPattern(
        LocalVariable variable,
        List<IRExpression> expressions
) implements SwitchCaseCondition {


}
