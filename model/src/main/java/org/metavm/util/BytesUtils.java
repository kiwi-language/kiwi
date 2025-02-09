package org.metavm.util;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.AnyType;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.TypeKey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class BytesUtils {

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

    public static byte[] toIndexBytes(Value instance) {
        var bout = new ByteArrayOutputStream();
        var output = new IndexKeyWriter(bout);
        output.writeValue(instance);
        return bout.toByteArray();
    }

    public static Value readIndexValue(byte[] bytes, Function<Id, Instance> resolver) {
        var bin = new ByteArrayInputStream(bytes);
        return new IndexKeyReader(bin, resolver).readValue();
    }

    public static Object readIndexBytes(byte[] bytes) {
        var bin = new ByteArrayInputStream(bytes);
        var input = new IndexKeyReader(bin, id -> new MockInstance(id));
        return convertInstanceToValue(input.readValue());
    }

    private static Object convertInstanceToValue(Value instance) {
        if(instance instanceof PrimitiveValue primitiveValue)
            return primitiveValue.getValue();
        else if(instance instanceof Reference durableInstance)
            return durableInstance.getId();
        else
            throw new InternalException("Can not convert instance: " + instance);
    }

    private static class MockInstance extends MvInstance {

        private final Id id;

        public MockInstance(Id id) {
            super(AnyType.instance);
            this.id = id;
        }

        @Override
        public Id getId() {
            return id;
        }

        @Override
        protected void readBody(InstanceInput input) {

        }

        @Override
        public boolean isArray() {
            return false;
        }

        public boolean isReference() {
            return false;
        }

        public FieldValue toFieldValueDTO() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public void forEachChild(Consumer<? super Instance> action) {

        }

        @Override
        public void forEachMember(Consumer<? super Instance> action) {

        }

        @Override
        public void forEachValue(Consumer<? super Instance> action) {

        }

        @Override
        public void forEachReference(Consumer<Reference> action) {

        }


        @Override
        public void forEachReference(BiConsumer<Reference, Boolean> action) {
            
        }

        @Override
        public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {

        }

        @Override
        public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {

        }

        @Override
        public void writeBody(MvOutput output) {

        }

        @Override
        public InstanceParam getParam() {
            return null;
        }

        @Override
        public Instance copy() {
            return null;
        }

        public <R> R accept(ValueVisitor<R> visitor) {
            return null;
        }

        public <R> void acceptReferences(ValueVisitor<R> visitor) {

        }

        public <R> void acceptChildren(ValueVisitor<R> visitor) {

        }

        public void writeTree(TreeWriter treeWriter) {

        }

        @Override
        public <R> R accept(InstanceVisitor<R> visitor) {
            return visitor.visitInstance(this);
        }

        public boolean isMutable() {
            return false;
        }

        public Object toJson(IInstanceContext context) {
            throw new UnsupportedOperationException();
        }
    }

    public static byte[] hexToBytes(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException("Hex string is null.");
        }
        int len = hexString.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string length.");
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int byteValue = Integer.parseInt(hexString.substring(i, i + 2), 16);
            data[i / 2] = (byte) byteValue;
        }
        return data;
    }

    public static byte[] toBytes(Instance instance) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeLong(instance.getVersion());
        output.writeInstance(instance.getReference());
        return bout.toByteArray();
    }

    public static Object convertToJSON(byte[] data, boolean withVersion) {
        var input = new JsonReader(new ByteArrayInputStream(data));
        Map<String, Object> result = new HashMap<>();
        if (withVersion) {
            result.put("version", input.readLong());
            result.put("treeId", input.readTreeId());
            result.put("nextNodeId", input.readInt());
        }
        result.put("data", input.readJson());
        return result;
    }

    private static class JsonReader extends InstanceInput {

        public JsonReader(InputStream inputStream) {
            super(inputStream);
        }

        public Object readJson() {
            var wireType = read();
            return switch (wireType) {
                case WireTypes.INSTANCE -> readRecord();
                case WireTypes.VALUE_INSTANCE -> readMap();
                case WireTypes.NULL -> null;
                case WireTypes.BOOLEAN -> readBoolean();
                case WireTypes.REFERENCE -> readId().toString();
                case WireTypes.LONG, WireTypes.TIME -> readLong();
                case WireTypes.INT -> readInt();
                case WireTypes.CHAR -> readChar();
                case WireTypes.SHORT -> readShort();
                case WireTypes.BYTE -> read();
                case WireTypes.DOUBLE -> readDouble();
                case WireTypes.FLOAT -> readFloat();
                case WireTypes.PASSWORD, WireTypes.STRING -> readUTF();
                default -> throw new IllegalStateException("Invalid wire type");
            };
        }

        private Object readRecord() {
            Map<String, Object> map = new HashMap<>();
            map.put("nodeId", readLong());
            readBody(TypeKey.read(this), map);
            return map;
        }

        private Object readMap() {
            Map<String, Object> map = new HashMap<>();
            readBody(TypeKey.read(this), map);
            return map;
        }

        private void readBody(TypeKey typeKey, Map<String, Object> map) {
            map.put("type", typeKey.toTypeExpression());
            if (typeKey.isArray()) {
                int len = readInt();
                var elements = new ArrayList<>(len);
                map.put("elements", elements);
                for (int i = 0; i < len; i++)
                    elements.add(readJson());
            } else {
                int numKlasses = readInt();
                var klasses = new ArrayList<>(numKlasses);
                map.put("klasses", klasses);
                for (int i = 0; i < numKlasses; i++) {
                    var klass = new HashMap<>();
                    klasses.add(klass);
                    klass.put("tag", readLong());
                    int numFields = readInt();
                    var fields = new ArrayList<>(numFields);
                    klass.put("fields", fields);
                    for (int j = 0; j < numFields; j++)
                        fields.add(Map.of("tag", readLong(), "value", Utils.orElse(readJson(), "null")));
                }
            }
        }

    }

    public static void saveCacheBytes(String name, byte[] bytes) {
        var path = SAVE_DIR + "/" + name;
        Utils.writeFile(path, bytes);
    }

    public static int compareBytes(byte[] bytes1, byte[] bytes2) {
        int len = Math.min(bytes1.length, bytes2.length);
        for (int i = 0; i < len; i++) {
            int d = compareByte(bytes1[i], bytes2[i]);
            if (d != 0)
                return d;
        }
        return Integer.compare(bytes1.length, bytes2.length);
    }

    public static int compareByte(byte b1, byte b2) {
        if (b1 == b2)
            return 0;
        for (int s = 0x80; s > 0; s >>= 1) {
            int bit1 = b1 & s;
            int bit2 = b2 & s;
            int d = bit1 - bit2;
            if (d < 0)
                return -1;
            if (d > 0)
                return 1;
        }
        throw new InternalException("Should not reach here");
    }

    public static String toBinaryString(byte[] bytes) {
        var buf = new StringBuilder();
        for (byte b : bytes) {
            toBinaryString(b, buf);
        }
        return buf.toString();
    }

    public static void toBinaryString(byte b, StringBuilder buf) {
        for (int i = 7; i >= 0; i--) {
            buf.append((b & 1 << i) != 0 ? '1' : '0');
        }
    }


}
