package org.manul.entity;

public interface LoadAware {

    default void onLoadPrepare() {};

    void onLoad();

}
