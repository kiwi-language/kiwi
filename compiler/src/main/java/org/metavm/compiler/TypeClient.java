package org.metavm.compiler;

public interface TypeClient {

    String deploy(long appId, String mvaFile);

    String secretDeploy(long appId, String mvaFile);

    String getDeployStatus(long appId, String deployId);

    void revert(long appId);

    void login(String username, String password);

    boolean ping();

}
