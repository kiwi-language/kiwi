package switch_;

public class SparseSwitchFoo {

    public static int test(int v) {
        //noinspection EnhancedSwitchMigration
        switch (v) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 3;
            case 4: return 4;
            case 5: return 5;
            case Integer.MAX_VALUE: return Integer.MAX_VALUE;
            default: return -1;
        }
    }

}
