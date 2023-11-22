package tech.metavm.util;

import tech.metavm.management.RegionManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ByteUtils {

    public static final String SAVE_DIR = "/Users/leen/workspace/object/src/test/resources/bytes";

    public static long readFirstLong(byte[] bytes) {
        int b = bytes[0];
        boolean negative = (b & 1) == 1;
        long v = b >> 1 & 0x3f;
        int shifts = 6;
        for (int i = 1; (b & 0x80) != 0 && i < 10; i++, shifts += 7) {
            b = bytes[i];
            v |= (long) (b & 0x7f) << shifts;
        }
        return negative ? -v : v;
    }

    public static Object convertToJSON(byte[] data, boolean withVersion) {
        var input = new JsonReader(new ByteArrayInputStream(data));
        Map<String, Object> result = new HashMap<>();
        if (withVersion) {
            result.put("version", input.readLong());
        }
        result.put("data", input.readValue());
        return result;
    }

    private static class JsonReader extends InstanceInput {

        public JsonReader(InputStream inputStream) {
            super(inputStream);
        }

        public Object readValue() {
            var wireType = read();
            return switch (wireType) {
                case WireTypes.RECORD -> readRecord();
                case WireTypes.NULL -> null;
                case WireTypes.BOOLEAN -> readBoolean();
                case WireTypes.REFERENCE, WireTypes.LONG, WireTypes.TIME -> readLong();
                case WireTypes.DOUBLE -> readDouble();
                case WireTypes.PASSWORD, WireTypes.STRING -> readString();
                default -> throw new IllegalStateException("Invalid wire type");
            };
        }

        private Object readRecord() {
            long id = readLong();
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            if (RegionManager.isArrayId(id)) {
                int len = readInt();
                var elements = new ArrayList<>(len);
                map.put("elements", elements);
                for (int i = 0; i < len; i++)
                    elements.add(readValue());
            } else {
                int numFields = readInt();
                var fields = new ArrayList<>(numFields);
                map.put("fields", fields);
                for (int i = 0; i < numFields; i++)
                    fields.add(Map.of("id", readLong(), "value", readValue()));
            }
            return map;
        }

    }

    public static void saveCacheBytes(String name, byte[] bytes) {
        var path = SAVE_DIR + "/" + name;
        NncUtils.writeFile(path, bytes);
    }

}
