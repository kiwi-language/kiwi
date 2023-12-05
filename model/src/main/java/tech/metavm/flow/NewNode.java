package tech.metavm.flow;

import javax.annotation.Nullable;

public interface NewNode {

    void setParent(@Nullable ParentRef parentRef);

    @Nullable ParentRef getParentRef();

}
