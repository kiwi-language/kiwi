package org.metavm.compiler;

public interface TypeClient {

    void deploy(long appId, String mvaFile);

    void secretDeploy(long appId, String mvaFile);

    void revert(long appId);

    void login(String loginName, String password);

    boolean ping();

}
