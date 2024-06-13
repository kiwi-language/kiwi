package tech.metavm.object.instance.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.common.ErrorCode;
import tech.metavm.object.instance.ApiService;
import tech.metavm.object.instance.core.Id;
import tech.metavm.user.LoginService;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;

    private final LoginService loginService;

    private final boolean verify;

    public ApiController(ApiService apiService, LoginService loginService, @Value("${metavm.api.verify}") boolean verify) {
        this.apiService = apiService;
        this.loginService = loginService;
        this.verify = verify;
    }

    @RequestMapping("/**")
    public Object handle(HttpServletRequest request, @RequestBody(required = false) Object requestBody) {
        verify(request);
        var method = request.getMethod();
        var path = request.getRequestURI().substring(5);
        switch (method) {
            case "POST" -> {
                var idx = path.indexOf('/');
                if (idx == -1)
                    throw new BusinessException(ErrorCode.INVALID_REQUEST_PATH);
                var methodCode = path.substring(idx + 1);
                var qualifier = path.substring(0, idx).replace('/', '.');
                //noinspection unchecked
                var arguments = (List<Object>) requestBody;
                if (Id.isId(qualifier))
                    return apiService.handleInstanceMethodCall(qualifier, methodCode, arguments);
                else if (methodCode.equals("new"))
                    return apiService.handleNewInstance(qualifier, arguments);
                else
                    return apiService.handleStaticMethodCall(qualifier, methodCode, arguments);
            }
            case "GET" -> {
                return apiService.getInstance(path);
            }
            case "PUT" -> {
                var klassName = path.replace('/', '.');
                //noinspection unchecked
                return apiService.saveInstance(klassName, (Map<String, Object>) requestBody);
            }
            case "DELETE" -> {
                apiService.deleteInstance(path);
                return null;
            }
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST_METHOD);
        }
    }

    private void verify(HttpServletRequest request) {
        var appIdStr = request.getHeader(Headers.APP_ID);
        if (appIdStr == null || !ValueUtils.isIntegerStr(appIdStr))
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        var appId = Long.parseLong(appIdStr);
        if(verify) {
            var secret = request.getHeader(Headers.SECRET);
            if (!loginService.verifySecret(appId, secret))
                throw new BusinessException(ErrorCode.AUTH_FAILED);
        }
        ContextUtil.setAppId(appId);
    }

}
