import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;

public class CtLab {

    @ChildEntity("foos")
    private ChildList<CtFoo> foos;

    public CtFoo getFooByName(String name) {
        return CtUtils.findRequired(foos, f -> f.getName().equals(name));
    }

}
