import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;

import java.util.List;

public class LabBOM {

    @ChildEntity("components")
    private final ChildList<LabComponentMaterial> components = new ChildList<>();

    public List<LabComponentMaterial> getComponents() {
        return components;
    }

    public void addComponent(LabComponentMaterial component) {
        this.components.add(component);
    }

    public void test() {
        for (LabComponentMaterial component : components) {
            var version = component.getVersion();
            if(version != null)
                version.getComponents().forEach(c -> processComponent(c));
            else
                processComponent(component);
        }
    }

    private void processComponent(LabComponentMaterial component) {}

}
