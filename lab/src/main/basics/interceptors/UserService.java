package interceptors;

import org.metavm.api.Component;

@Component
public class UserService {

    public UserDTO getUserByName(String name) {
        return new UserDTO(name, "12312312312");
    }

}
