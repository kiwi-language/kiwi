package tech.metavm.util;

import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.LoginRequest;

public class LoginUtils {

    public static void loginWithAuthFile(long appId, String token) {
//        typeClient.login(appId, authConfig.loginName(), authConfig.password());
        CompilerHttpUtils.setAppId(appId);
        CompilerHttpUtils.setToken(appId, token);
    }

    public static void login(long appId, String loginName, String password) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        CompilerHttpUtils.setAppId(appId);


//        if(loginInfo.isSuccessful()) {
//            TokenStore.INSTANCE.setToken(HOST, appId, loginInfo.);
//        }
    }

}
