package interceptors;

import org.metavm.api.Component;
import org.metavm.api.Interceptor;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;

import javax.annotation.Nullable;
import java.util.Objects;

@Component
public class TelephoneMaskInterceptor implements Interceptor {
    @Override
    public void before(HttpRequest request, HttpResponse response) {
    }

    @Override
    public @Nullable Object after(HttpRequest request, HttpResponse response, @Nullable Object result) {
        if(request.getRequestURI().equals("/api/userService/getUserByName")) {
            var user = (UserDTO) Objects.requireNonNull(result);
            var tel = user.telephone();
            return new UserDTO(user.name(), tel.substring(0, 3) + "******" + tel.substring(9));
        }
        else
            return result;
    }
}
