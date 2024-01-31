package tech.metavm.util;

import tech.metavm.autograph.TypeClient;
import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.LoginRequest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LoginUtils {

    public static void loginWithAuthFile(String filePath, TypeClient typeClient) {
        try (Scanner scanner = new Scanner(new FileInputStream(filePath))) {
            long appId = scanner.nextLong();
            String loginName = scanner.next();
            String password = scanner.next();
            typeClient.login(appId, loginName, password);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void login(long appId, String loginName, String password) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.post("/login", new LoginRequest(2L, loginName, password),
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
