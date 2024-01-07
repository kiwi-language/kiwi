package tech.metavm.flow;

import tech.metavm.object.type.FunctionTypeProvider;

public interface CodeSource {

    void generateCode(Flow flow, FunctionTypeProvider functionTypeProvider);

}
