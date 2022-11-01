package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityContext;

import java.util.List;

public interface ParsingContext {

    Expression parse(List<Var> varPath);

    EntityContext getEntityContext();

}
