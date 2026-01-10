package org.manul.expression;

import org.manul.object.instance.core.InstanceProvider;
import org.manul.object.type.IndexedTypeDefProvider;

public abstract class BaseParsingContext implements ParsingContext{

    private final InstanceProvider instanceProvider;
    private final IndexedTypeDefProvider klassProvider;

    protected BaseParsingContext(InstanceProvider instanceProvider,
                                 IndexedTypeDefProvider klassProvider) {
        this.instanceProvider = instanceProvider;
        this.klassProvider = klassProvider;
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public IndexedTypeDefProvider getTypeDefProvider() {
        return klassProvider;
    }

}
