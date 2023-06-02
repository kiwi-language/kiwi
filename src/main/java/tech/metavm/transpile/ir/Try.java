package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record Try(
        CodeBlock block,
        List<Resource> resources,
        CodeBlock body,
        List<CatchClause> catchClauses,
        @Nullable CodeBlock finallyBlock
)
implements Statement{

    @Override
    public List<Statement> getChildren() {
        List<Statement> children = new ArrayList<>();
        children.add(body);
        children.addAll(NncUtils.map(catchClauses, CatchClause::body));
        if(finallyBlock != null) {
            children.add(finallyBlock);;
        }
        return children;
    }
}
