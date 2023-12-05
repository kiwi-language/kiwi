package tech.metavm.autograph.mocks;

public class ColonSwitchFoo {

    public String  test(Object value) {
        String result;
        switch (value) {
            case String str:
                result = str;
                break;
            case Long l:
                result = l + "L";
                break;
            default:
                result = value.toString();
        }
        return result;
    }

}
