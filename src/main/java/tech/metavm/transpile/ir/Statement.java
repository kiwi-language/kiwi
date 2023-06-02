package tech.metavm.transpile.ir;

import java.util.List;

public interface Statement {

    default List<Statement> getChildren() {
        return List.of();
    }

}
