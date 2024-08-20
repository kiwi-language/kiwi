package org.metavm.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class PropertiesUtils {

    public static void store(Properties properties, OutputStream out)
            throws IOException
    {
        store0(properties, new BufferedWriter(new OutputStreamWriter(out, ISO_8859_1)), true);
    }

    private static void store0(Properties properties, BufferedWriter bw, boolean escUnicode) throws IOException {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (properties) {
            @SuppressWarnings("unchecked")
            Collection<Map.Entry<String, String>> entries = (Set<Map.Entry<String, String>>) (Set) properties.entrySet();
            entries = new ArrayList<>(entries);
            ((List<Map.Entry<String, String>>) entries).sort(Map.Entry.comparingByKey());
            for (Map.Entry<String, String> e : entries) {
                String key = e.getKey();
                String val = e.getValue();
                key = saveConvert(key, true, escUnicode);
                /* No need to escape embedded and trailing spaces for value, hence
                 * pass false to flag.
                 */
                val = saveConvert(val, false, escUnicode);
                bw.write(key + "=" + val);
                bw.newLine();
            }
        }
        bw.flush();
    }

    private static String saveConvert(String theString,
                                      boolean escapeSpace,
                                      boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);
        HexFormat hex = HexFormat.of().withUpperCase();
        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                    break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                    break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                    break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append("\\u");
                        outBuffer.append(hex.toHexDigits(aChar));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

}
