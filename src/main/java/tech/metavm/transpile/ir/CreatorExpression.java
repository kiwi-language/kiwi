package tech.metavm.transpile.ir;

import javax.annotation.Nullable;
import java.util.List;

public record CreatorExpression(IRType type,
                                IRConstructor constructor,
                                List<IRType> typeArguments,
                                List<IRExpression> arguments,
                                @Nullable IRExpression owner
) implements IRExpression {

}
