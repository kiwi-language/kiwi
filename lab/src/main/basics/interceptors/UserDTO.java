package interceptors;

import org.metavm.api.Value;

@Value
public record UserDTO(
        String name,
        String telephone
) {

}
