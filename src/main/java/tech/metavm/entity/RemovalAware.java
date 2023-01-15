package tech.metavm.entity;

import java.util.List;

public interface RemovalAware {

    List<Object> onRemove();

}
