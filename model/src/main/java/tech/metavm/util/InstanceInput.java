package tech.metavm.util;

import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class InstanceInput implements Closeable {

    public static final Function<Id, DurableInstance> UNSUPPORTED_RESOLVER = id -> {
        throw new UnsupportedOperationException();
    };

    public static final TypeDefProvider UNSUPPORTED_TYPE_DEF_PROVIDER = id -> {
        throw new UnsupportedOperationException();
    };

    public static InstanceInput create(byte[] bytes, IInstanceContext context) {
        var bout = new ByteArrayInputStream(bytes);
        if (context == null)
            return new InstanceInput(bout);
        else
            return new InstanceInput(bout, context::internalGet, context.getTypeDefProvider());
    }

    private final InputStream inputStream;
    private final Function<Id, DurableInstance> getInstance;
    private final TypeDefProvider typeDefProvider;
    private long treeId;
    @Nullable
    private DurableInstance parent;
    @Nullable
    private Field parentField;
    private boolean loadedFromCache;

    public InstanceInput(InputStream inputStream) {
        this(inputStream, UNSUPPORTED_RESOLVER, UNSUPPORTED_TYPE_DEF_PROVIDER);
    }

    public InstanceInput(InputStream inputStream,
                         Function<Id, DurableInstance> getInstance,
                         TypeDefProvider typeDefProvider) {
        this.inputStream = inputStream;
        this.getInstance = getInstance;
        this.typeDefProvider = typeDefProvider;
    }

    public DurableInstance readMessage() {
        var version = readLong();
        readTreeId();
        var nextNodeId = readInt();
        var instance = (DurableInstance) readInstance();
        instance.setVersion(version);
        instance.setNextNodeId(nextNodeId);
        return instance;
    }

    public Instance readInstance() {
        var wireType = read();
        return switch (wireType) {
            case WireTypes.NULL -> new NullInstance(StandardTypes.getNullType());
            case WireTypes.DOUBLE -> new DoubleInstance(readDouble(), StandardTypes.getDoubleType());
            case WireTypes.STRING -> new StringInstance(readString(), StandardTypes.getStringType());
            case WireTypes.LONG -> new LongInstance(readLong(), StandardTypes.getLongType());
            case WireTypes.BOOLEAN -> new BooleanInstance(readBoolean(), StandardTypes.getBooleanType());
            case WireTypes.TIME -> new TimeInstance(readLong(), StandardTypes.getTimeType());
            case WireTypes.PASSWORD -> new PasswordInstance(readString(), StandardTypes.getPasswordType());
            case WireTypes.REFERENCE -> resolveInstance(readId());
            case WireTypes.RECORD -> readRecord();
            default -> throw new IllegalStateException("Invalid wire type: " + wireType);
        };
    }

    private DurableInstance resolveInstance(Id id) {
        return getInstance.apply(id);
    }

    private final StreamVisitor skipper = new StreamVisitor(this);

    private Instance readRecord() {
        var nodeId = readLong();
        var type = Type.readType(this, typeDefProvider);
        var id = PhysicalId.of(treeId, nodeId, type);
        var instance = resolveInstance(id);
        if (instance.isInitialized())
            skipper.visitRecordBody(nodeId, type);
        else {
            instance.setType(type);
            instance.setParentInternal(parent, parentField);
            instance.readFrom(this);
        }
        return instance;
    }

    public void skipInstance() {
        skipper.visit();
    }

    public String readString() {
        int len = readInt();
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public boolean readBoolean() {
        return read() != 0;
    }

    public long readLong() {
        int b = read();
        boolean negative = (b & 1) == 1;
        long v = b >> 1 & 0x3f;
        int shifts = 6;
        for (int i = 0; (b & 0x80) != 0 && i < 10; i++, shifts += 7) {
            b = read();
            v |= (long) (b & 0x7f) << shifts;
        }
        return negative ? -v : v;
    }

    public int readInt() {
        return (int) readLong();
    }

    public TypeTag readTypeTag() {
        return TypeTag.fromCode(read());
    }

    public double readDouble() {
        long l = 0;
        for (int shifts = 0; shifts < 64; shifts += 8)
            l |= (long) read() << shifts;
        return Double.longBitsToDouble(l);
    }

    public int read(byte[] buf) {
        try {
            return inputStream.read(buf);
        } catch (IOException e) {
            throw new InternalException("Failed to read from the underlying input steam", e);
        }
    }

    public int read() {
        try {
            return inputStream.read();
        } catch (IOException e) {
            throw new InternalException("Failed to read from the underlying input steam", e);
        }
    }

    public void setParent(@Nullable DurableInstance parent, @Nullable Field parentField) {
        this.parent = parent;
        this.parentField = parentField;
    }

    public Id readId() {
        return Id.readId(this);
    }

    public long readTreeId() {
        return treeId = readLong();
    }

    public boolean isLoadedFromCache() {
        return loadedFromCache;
    }

    public void setLoadedFromCache(boolean loadedFromCache) {
        this.loadedFromCache = loadedFromCache;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public long getTreeId() {
        return treeId;
    }
}
