package org.metavm.meta.processor;

public interface IdStores {
    String getId(String type, String name);

    int getTypeTag(String className);

}
