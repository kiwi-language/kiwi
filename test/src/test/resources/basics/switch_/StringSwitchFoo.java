package switch_;

public class StringSwitchFoo {

    public static int test(String s) {
        //noinspection EnhancedSwitchMigration
        switch (s) {
            case "Meta": return 1;
            case "VM": return 2;
            case "MetaVM": return 3;
            default: return -1;
        }
    }

}
