import java.util.ArrayList;
import java.util.List;

public class AstLab {

    public <T> List<T> copyList(List<T> list) {
        return new ArrayList<>(list);
    }

}
