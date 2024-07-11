package org.metavm.entity;

public interface LoadAware {

    default void onLoadPrepare() {};

    void onLoad();

}
