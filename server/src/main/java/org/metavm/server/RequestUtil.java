package org.metavm.server;

import javax.annotation.Nullable;

/** @noinspection unused*/
public class RequestUtil {

      public static byte parseByte(@Nullable String value) {
        if (value == null)
            return 0;
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid byte parameter: " + value);
        }
    }

    public static Byte byteValueOf(String value) {
        if (value == null)
            return null;
        try {
            return Byte.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid byte parameter: " + value);
        }
    }

      public static short parseShort(@Nullable String value) {
        if (value == null)
            return 0;
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid short parameter: " + value);
        }
    }

    public static Short shortValueOf(String value) {
        if (value == null)
            return null;
        try {
            return Short.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid short parameter: " + value);
        }
    }

      public static int parseInt(@Nullable String value) {
        if (value == null)
            return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid integer parameter: " + value);
        }
    }

    public static Integer integerValueOf(String value) {
        if (value == null)
            return null;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid integer parameter: " + value);
        }
    }

      public static long parseLong(@Nullable String value) {
        if (value == null)
            return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid long parameter: " + value);
        }
    }

    public static Long longValueOf(String value) {
        if (value == null)
            return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid long parameter: " + value);
        }
    }

      public static float parseFloat(@Nullable String value) {
        if (value == null)
            return 0.0f;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid float parameter: " + value);
        }
    }

    public static Float floatValueOf(String value) {
        if (value == null)
            return null;
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid float parameter: " + value);
        }
    }

      public static double parseDouble(@Nullable String value) {
        if (value == null)
            return 0.0d;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid double parameter: " + value);
        }
    }

    public static Double doubleValueOf(String value) {
        if (value == null)
            return null;
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid double parameter: " + value);
        }
    }

      public static boolean parseBoolean(@Nullable String value) {
        if (value == null)
            return false;

        // Strict parsing to match the exception behavior of number types
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;

        throw new BadRequestException("Invalid boolean parameter: " + value);
    }

    public static Boolean booleanValueOf(String value) {
        if (value == null)
            return null;

        if ("true".equalsIgnoreCase(value)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(value)) return Boolean.FALSE;

        throw new BadRequestException("Invalid boolean parameter: " + value);
    }

      public static char parseChar(@Nullable String value) {
        if (value == null)
            return '\u0000';

        if (value.length() != 1) {
            throw new BadRequestException("Invalid char parameter (must be length 1): " + value);
        }
        return value.charAt(0);
    }

    public static Character characterValueOf(String value) {
        if (value == null)
            return null;

        if (value.length() != 1) {
            throw new BadRequestException("Invalid char parameter (must be length 1): " + value);
        }
        return value.charAt(0);
    }
}