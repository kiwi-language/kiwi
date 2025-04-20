package primitives;

public class CharSequenceFoo {

    public static int length(CharSequence cs) {
        return cs.length();
    }

    public static CharSequence subSequence(CharSequence cs, int start, int end) {
        return cs.subSequence(start, end);
    }

    public static char charAt(CharSequence cs, int index) {
        return cs.charAt(index);
    }

}
