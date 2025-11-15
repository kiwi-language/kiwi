package org.metavm.compiler;

import org.metavm.compiler.util.CompilerHttpUtils;

public class HttpTypeClient implements TypeClient {

    @Override
    public String deploy(long appId, String mvaPath) {
        return CompilerHttpUtils.upload("/type/deploy/" + appId, mvaPath, String.class);
    }

    @Override
    public String secretDeploy(long appId, String mvaPath) {
        return CompilerHttpUtils.deploy(appId, mvaPath);
    }

    @Override
    public String getDeployStatus(long appId, String deployId) {
        return CompilerHttpUtils.get("/internal-api/deploy/status/" + appId + "/" + deployId, String.class);
    }

    @Override
    public void revert(long appId) {
        CompilerHttpUtils.revert(appId);
    }

    @Override
    public void login(String username, String password) {
        CompilerHttpUtils.login(username, password);
    }

    @Override
    public boolean ping() {
        CompilerHttpUtils.get("/ping", void.class);
        return true;
    }


}
