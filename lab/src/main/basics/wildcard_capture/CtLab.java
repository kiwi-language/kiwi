package wildcard_capture;

import java.util.ArrayList;
import java.util.List;

public class CtLab {

    private final List<CtFoo> foos = new ArrayList<>();

    public CtLab(List<CtFoo> foos) {
        this.foos.addAll(foos);
    }

    public CtFoo getFooByName(String name) {
        return CtUtils.findRequired(foos, f -> f.getName().equals(name));
    }

}
