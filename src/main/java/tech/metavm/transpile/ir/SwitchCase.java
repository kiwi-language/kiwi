package tech.metavm.transpile.ir;

import java.util.List;

public record SwitchCase(
        SwitchLabel label,
        List<Statement> statements
) {


}
