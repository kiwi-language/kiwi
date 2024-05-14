package tech.metavm.object.view.rest.dto;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.InstanceOutput;

public record DirectMappingKey(
        String mappingId
) implements MappingKey {

    @Override
    public int kind() {
        return 1;
    }

    @Override
    public ObjectMapping toMapping(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        return mappingProvider.getObjectMapping(Id.parse(mappingId));
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(1);
        output.writeId(Id.parse(mappingId));
    }
}
