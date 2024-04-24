package tech.metavm.expression;

import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.IndexedTypeDefProvider;
import tech.metavm.object.type.UnionTypeProvider;

public abstract class BaseParsingContext implements ParsingContext{

    private final InstanceProvider instanceProvider;
    private final IndexedTypeDefProvider klassProvider;
    private final ArrayTypeProvider arrayTypeProvider;
    private final UnionTypeProvider unionTypeProvider;

    protected BaseParsingContext(InstanceProvider instanceProvider,
                                 IndexedTypeDefProvider klassProvider,
                                 ArrayTypeProvider arrayTypeProvider, UnionTypeProvider unionTypeProvider) {
        this.instanceProvider = instanceProvider;
        this.klassProvider = klassProvider;
        this.arrayTypeProvider = arrayTypeProvider;
        this.unionTypeProvider = unionTypeProvider;
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public IndexedTypeDefProvider getTypeDefProvider() {
        return klassProvider;
    }

    @Override
    public ArrayTypeProvider getArrayTypeProvider() {
        return arrayTypeProvider;
    }

    @Override
    public UnionTypeProvider getUnionTypeProvider() {
        return unionTypeProvider;
    }
}
