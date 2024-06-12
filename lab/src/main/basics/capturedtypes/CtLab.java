package capturedtypes;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;

import java.util.List;

public class CtLab {

    @ChildEntity("foos")
    private final ChildList<CtFoo> foos;

    public CtLab(List<CtFoo> foos) {
        this.foos = new ChildList<>(foos);
    }

    public CtFoo getFooByName(String name) {
        return CtUtils.findRequired(foos, f -> f.getName().equals(name));
    }

}
