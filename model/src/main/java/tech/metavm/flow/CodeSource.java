package tech.metavm.flow;

import tech.metavm.object.type.CompositeTypeFacade;

public interface CodeSource {

    void generateCode(Flow flow, CompositeTypeFacade compositeTypeFacade);

}
