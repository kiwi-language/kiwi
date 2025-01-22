package org.metavm.autograph;

import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.CompilerHttpUtils;
import org.metavm.util.Constants;
import org.metavm.util.TypeReference;

public class HttpTypeClient implements TypeClient {

    @Override
    public long getAppId() {
        return CompilerHttpUtils.getAppId();
    }

    @Override
    public void setAppId(long appId) {
        CompilerHttpUtils.setAppId(appId);
    }

    @Override
    public void deploy(String mvaPath) {
        CompilerHttpUtils.upload("/type/deploy", mvaPath, new TypeReference<Void>() {});
    }

    @Override
    public void login(long appId, String loginName, String password) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        CompilerHttpUtils.setAppId(appId);
    }

    @Override
    public boolean ping() {
        return CompilerHttpUtils.get("/instance/ping", new TypeReference<>() {});
    }


}
