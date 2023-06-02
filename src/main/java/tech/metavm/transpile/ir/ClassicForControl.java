package tech.metavm.transpile.ir;

import javax.annotation.Nullable;
import java.util.List;

public record ClassicForControl(
        @Nullable ForInit init,
        @Nullable IRExpression condition,
        @Nullable List<IRExpression> update
) implements ForControl {
}
