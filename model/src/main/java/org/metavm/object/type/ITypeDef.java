package org.metavm.object.type;

import org.metavm.entity.Entity;
import org.metavm.entity.Identifiable;
import org.metavm.flow.KlassInput;
import org.metavm.object.instance.core.Instance;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;

public interface ITypeDef extends Identifiable, Instance {

    void readBody(MvInput input, Entity parent);

    void writeBody(MvOutput output);

}
