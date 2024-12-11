package index;

import org.metavm.api.Component;

import javax.annotation.Nullable;

@Component
public class FooService {

    public @Nullable Foo findByDesc(String desc) {
        return Foo.descIndex.getFirst(desc);
    }

}
