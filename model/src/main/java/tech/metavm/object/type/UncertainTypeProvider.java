package tech.metavm.object.type;

import javax.annotation.Nullable;

public interface UncertainTypeProvider {

    UncertainType getUncertainType(Type lowerBound, Type upperBound, @Nullable Long tmpId);

}
