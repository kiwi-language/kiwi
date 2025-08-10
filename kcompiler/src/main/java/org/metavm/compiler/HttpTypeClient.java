package org.metavm.compiler;

import org.metavm.compiler.util.CompilerHttpUtils;
import org.metavm.ddl.CommitState;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.Constants;
import org.metavm.util.TypeReference;

public class HttpTypeClient implements TypeClient {

    @Override
    public String deploy(long appId, String mvaPath) {
        return CompilerHttpUtils.upload("/type/deploy/" + appId, mvaPath, new TypeReference<>() {});
    }

    @Override
    public String secretDeploy(long appId, String mvaPath) {
        return CompilerHttpUtils.deploy(appId, mvaPath);
    }

    @Override
    public String getDeployStatus(long appId, String deployId) {
        return CompilerHttpUtils.get2("/internal-api/deploy/status/" + appId + "/" + deployId, new TypeReference<>() {});
    }

    @Override
    public void revert(long appId) {
        CompilerHttpUtils.revert(appId);
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
