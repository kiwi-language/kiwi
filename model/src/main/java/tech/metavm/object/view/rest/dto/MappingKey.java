package tech.metavm.object.view.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.type.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = DirectMappingKey.class),
                @JsonSubTypes.Type(name = "2", value = ParameterizedMappingKey.class)
        }
)
public interface MappingKey {

    int kind();

    ObjectMapping toMapping(MappingProvider mappingProvider, TypeDefProvider typeDefProvider);

    void write(InstanceOutput output);

    static MappingKey read(InstanceInput input) {
        var kind = input.read();
        return switch (kind) {
            case 1 -> new DirectMappingKey(input.readId());
            case 2 -> new ParameterizedMappingKey(
                    (ParameterizedTypeKey) TypeKey.read(input),
                    input.readId().toString()
            );
            default -> throw new IllegalStateException("Invalid mapping key kind: " + kind);
        };
    }

}
