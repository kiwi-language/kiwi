package tech.metavm.flow;

import javax.annotation.Nullable;

public interface NewNode {

    void setParentRef(@Nullable ParentRef parentRef);

    @Nullable ParentRef getParentRef();

}
