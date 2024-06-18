package interceptors;

import org.metavm.api.ValueType;

@ValueType
public record UserDTO(
        String name,
        String telephone
) {

}
