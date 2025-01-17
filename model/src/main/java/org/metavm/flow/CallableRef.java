package org.metavm.flow;

import org.metavm.entity.Reference;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.MvOutput;

import java.util.function.Consumer;

public interface CallableRef extends Reference {

    TypeMetadata getTypeMetadata();

    Code getCode();

    FlowRef getFlow();

    void write(MvOutput output);

    void forEachReference(Consumer<org.metavm.object.instance.core.Reference> action);
}
