package tech.metavm.object.instance.query;

import tech.metavm.entity.InstanceContext;

import java.util.List;

public interface ParsingContext {

    Expression parse(List<Var> varPath);

}
