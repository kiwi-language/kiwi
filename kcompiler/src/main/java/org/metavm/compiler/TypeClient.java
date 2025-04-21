package org.metavm.compiler;

public interface TypeClient {

    long getAppId();

    void setAppId(long appId);

    void deploy(String mvaFile);

    void login(long appId, String loginName, String password);

    boolean ping();

}
