package tech.metavm.expression;

import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.IndexedTypeProvider;

public abstract class BaseParsingContext implements ParsingContext{

    private final InstanceProvider instanceProvider;
    private final IndexedTypeProvider typeProvider;
    private final ArrayTypeProvider arrayTypeProvider;

    protected BaseParsingContext(InstanceProvider instanceProvider,
                                 IndexedTypeProvider typeProvider,
                                 ArrayTypeProvider arrayTypeProvider) {
        this.instanceProvider = instanceProvider;
        this.typeProvider = typeProvider;
        this.arrayTypeProvider = arrayTypeProvider;
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public IndexedTypeProvider getTypeProvider() {
        return typeProvider;
    }

    @Override
    public ArrayTypeProvider getArrayTypeProvider() {
        return arrayTypeProvider;
    }
}
