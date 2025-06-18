package org.metavm.compiler;

import org.metavm.compiler.util.CompilerHttpUtils;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.Constants;
import org.metavm.util.TypeReference;

public class HttpTypeClient implements TypeClient {

    @Override
    public void deploy(long appId, String mvaPath) {
        CompilerHttpUtils.upload("/type/deploy/" + appId, mvaPath, new TypeReference<Void>() {});
    }

    @Override
    public void login(String loginName, String password) {
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
    }

    @Override
    public boolean ping() {
        return CompilerHttpUtils.get("/instance/ping", new TypeReference<>() {});
    }


}
