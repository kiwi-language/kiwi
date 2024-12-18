package switch_;

public class PatternGuardFoo {

    public static int test(Object object) {
        switch (object) {
            case Integer i when i > 0 -> {
                return 1;
            }
            case Integer i when i == 0 -> {
                return 0;
            }
            default -> {
                return -1;
            }
        }
    }

}
