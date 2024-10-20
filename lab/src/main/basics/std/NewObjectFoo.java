package std;

public class NewObjectFoo {

    public static Object newObject() {
        return new Object();
    }

    public static int testNewObjectArray() {
        var array = new Object[0];
        return array.length;
    }

    public static int testNewAnonymous() {
        return new Object() {
            final int value = 1;
        }.value;
    }

}
