package tech.metavm.transpile.ir;

import java.util.List;

public record SwitchMatchList (
        List<IRExpression> expressions
) implements SwitchCaseCondition {
}
