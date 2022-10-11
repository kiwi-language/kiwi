package tech.metavm.object.instance.query;

import java.util.List;

public interface ParsingContext {

    Expression parse(List<Var> varPath);

}
