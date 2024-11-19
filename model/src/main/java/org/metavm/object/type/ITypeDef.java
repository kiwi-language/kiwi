package org.metavm.object.type;

import org.metavm.entity.Identifiable;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;

public interface ITypeDef extends Identifiable {

    void read(KlassInput input);

    void write(KlassOutput output);

}
