package org.metavm.object.view.rest.dto;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.rest.dto.ParameterizedTypeKey;
import org.metavm.object.view.MappingProvider;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.InstanceOutput;

public record ParameterizedMappingKey(
        ParameterizedTypeKey typeKey,
        String rawMappingId
) implements MappingKey {
    @Override
    public int kind() {
        return 2;
    }

    @Override
    public ObjectMapping toMapping(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        var klass = typeKey.toType(typeDefProvider).resolve();
        var mappingId = Id.parse(rawMappingId);
        return klass.getMapping(m -> m.getEffectiveTemplate().idEquals(mappingId));
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(2);
        typeKey.write(output);
        output.writeId(Id.parse(rawMappingId));
    }
}
