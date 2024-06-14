package org.metavm.expression;

import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.type.IndexedTypeDefProvider;

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
