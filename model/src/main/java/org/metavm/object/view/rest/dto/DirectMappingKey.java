package org.metavm.object.view.rest.dto;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.InstanceOutput;

public record DirectMappingKey(
        Id mappingId
) implements MappingKey {

    @Override
    public int kind() {
        return 1;
    }

    @Override
    public ObjectMapping toMapping(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return mappingProvider.getObjectMapping(mappingId);
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(1);
        output.writeId(mappingId);
    }
}
