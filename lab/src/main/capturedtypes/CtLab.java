import java.util.List;

public class CtLab {

    public static CtFoo getFooByName(List<CtFoo> foos, String name) {
        return CtUtils.findRequired(foos, f -> f.getName().equals(name));
    }

}
