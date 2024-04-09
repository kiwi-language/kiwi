import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;

public class Foo {

    @ChildEntity("children")
    public final ChildList<Child> children = new ChildList<>();

    public Foo() {
        children.add(new Child("Child001"));
    }

    public void test() {
        var child = new Child("Child002");
        child.setNext(children.get(0));
        children.add(child);
        children.clear();
    }

}
