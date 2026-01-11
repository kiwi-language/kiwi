package org.manul.flow;

import org.manul.api.Entity;
import org.manul.entity.Reference;
import org.manul.object.type.TypeMetadata;
import org.manul.util.MvOutput;

import java.util.function.Consumer;

@Entity
public interface CallableRef extends Reference {

    TypeMetadata getTypeMetadata();

    Code getCode();

    FlowRef getFlow();

    void write(MvOutput output);

    void forEachReference(Consumer<org.manul.object.instance.core.Reference> action);
}
