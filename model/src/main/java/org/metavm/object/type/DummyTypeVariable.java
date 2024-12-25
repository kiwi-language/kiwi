package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.DummyGenericDeclaration;

@Entity(ephemeral = true)
public class DummyTypeVariable extends TypeVariable {

    public static final DummyTypeVariable instance = new DummyTypeVariable();

    private DummyTypeVariable() {
        super(null, "Dummy", DummyGenericDeclaration.INSTANCE);
    }
}
