package tech.metavm.autograph.mocks;

import java.io.Serializable;
import java.util.List;

public class AstWildcardFoo {

    public List<? extends Serializable> get() {
        return List.of();
    }

}
