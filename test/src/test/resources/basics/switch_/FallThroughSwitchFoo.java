package switch_;

public class FallThroughSwitchFoo {

    public static int test(int value) {
        //noinspection EnhancedSwitchMigration
        switch (value) {
            case 1, 2:
                return 0;
            case 3:
            case 4:
                return 1;
            default:
                return -1;
        }
    }

}
