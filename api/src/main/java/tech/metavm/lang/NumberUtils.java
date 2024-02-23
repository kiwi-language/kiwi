package tech.metavm.lang;

import java.text.DecimalFormat;
import java.util.Random;

public class NumberUtils {

    public static String format(String format, long value) {
        return new DecimalFormat(format).format(value);
    }

    public static long random(long bound) {
        return new Random().nextLong(bound);
    }

}
