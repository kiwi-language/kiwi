package index;

import org.metavm.api.Component;
import org.metavm.api.Index;

import javax.annotation.Nullable;

@Component
public class FooService {

    private static final Index<String, Foo> descIndex = new Index<>(false,
            f -> f.getName() + "-" + f.getSeq() + "-" + f.getBar().getCode());

    public @Nullable Foo findByDesc(String desc) {
        return descIndex.getFirst(desc);
    }

}
