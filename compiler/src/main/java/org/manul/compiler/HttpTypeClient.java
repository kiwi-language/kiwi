package org.manul.compiler;

import org.manul.compiler.util.HttpUtil;

public class HttpTypeClient implements TypeClient {

    @Override
    public String deploy(long appId, String mvaPath) {
        return HttpUtil.upload("/manul-system/type/deploy/" + appId, mvaPath, String.class);
    }

    @Override
    public String secretDeploy(long appId, String mvaPath) {
        return HttpUtil.deploy(appId, mvaPath);
    }

    @Override
    public String getDeployStatus(long appId, String deployId) {
        return HttpUtil.get("/internal-api/deploy/status/" + appId + "/" + deployId, String.class);
    }

    @Override
    public void revert(long appId) {
        HttpUtil.revert(appId);
    }

    @Override
    public void login(String username, String password) {
        HttpUtil.login(username, password);
    }

    @Override
    public boolean ping() {
        HttpUtil.get("/ping", void.class);
        return true;
    }


}
