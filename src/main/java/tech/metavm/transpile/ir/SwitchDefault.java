package tech.metavm.transpile.ir;

import java.util.List;

public record SwitchDefault(
        List<Statement> statements
) {
}
