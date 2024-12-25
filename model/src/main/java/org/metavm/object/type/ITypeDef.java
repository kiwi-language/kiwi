package org.metavm.object.type;

import org.metavm.entity.Identifiable;
import org.metavm.flow.KlassInput;
import org.metavm.util.MvOutput;

public interface ITypeDef extends Identifiable {

    void read(KlassInput input);

    void write(MvOutput output);

}
