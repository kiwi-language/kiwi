package switch_;

public class EnumSwitchFoo {

    public static int test(Option op) {
       switch (op) {
           case op1: return 1;
           case op2: return 2;
           case op3: return 3;
           case null: return 0;
           default: return -1;
       }
    }

    public enum Option {
        op1,
        op2,
        op3
    }

}
