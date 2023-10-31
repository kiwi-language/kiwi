package tech.metavm.object.meta.rest.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(name = "1", value = ClassTypeParam.class),
                @JsonSubTypes.Type(name = "2", value = ArrayTypeParam.class),
                @JsonSubTypes.Type(name = "3", value = PrimitiveTypeParam.class),
                @JsonSubTypes.Type(name = "4", value = UnionTypeParam.class),
                @JsonSubTypes.Type(name = "5", value = TypeVariableParam.class),
                @JsonSubTypes.Type(name = "6", value = FunctionTypeParam.class),
                @JsonSubTypes.Type(name = "7", value = UncertainTypeParam.class),
        }
)
public interface TypeParam {

    int getType();

}
