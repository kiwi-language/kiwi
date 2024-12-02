package org.metavm.object.type;

import org.metavm.api.ChildEntity;
import org.metavm.entity.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPool extends Element implements LoadAware, TypeMetadata {

    @ChildEntity
    private final ReadWriteArray<CpEntry> entries = addChild(new ReadWriteArray<>(CpEntry.class), "entries");
    @CopyIgnore
    private transient Map<Object, CpEntry> value2entry = new HashMap<>();
    @CopyIgnore
    private transient Object[] values = new Object[1];

    public transient ReadWriteArray<Type> typeArguments;
    private transient ResolutionStage stage = ResolutionStage.INIT;

    public ConstantPool() {
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConstantPool(this);
    }

    public ConstantPool(List<? extends Type> typeArguments) {
        this.typeArguments = new ReadWriteArray<>(Type.class);
        this.typeArguments.addAll(typeArguments);
    }

    public void addEntry(CpEntry entry) {
        entries.add(entry);
        var value = entry.getValue();
        value2entry.put(value, entry);
        while (values.length < entries.size())
            values = Arrays.copyOf(values, values.length << 1);
        values[entries.size() - 1] = value;
    }

    public int addValue(Object value) {
        var entry = value2entry.get(value);
        if(entry != null)
            return entry.getIndex();
        return addEntry(value).getIndex();
    }

    private CpEntry addEntry(Object value) {
        int i = entries.size();
        var entry = switch (value) {
            case Value v -> new ValueCpEntry(i, v);
            case Element element -> new ElementCpEntry(i, element);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
        addEntry(entry);
        return entry;
    }

    public void clear() {
        entries.clear();
        value2entry.clear();
        values = new Object[1];
    }

    public Object[] getValues() {
        return values;
    }

    public List<CpEntry> getEntries() {
        return entries.toList();
    }

    @Override
    public void onLoadPrepare() {
        value2entry = new HashMap<>();
        stage = ResolutionStage.INIT;
    }

    @Override
    public void onLoad() {
        values = new Object[Math.max(1, entries.size())];
        int i = 0;
        for (CpEntry entry : entries) {
            var value = entry.getValue();
            value2entry.put(value, entry);
            values[i++] = value;
        }
    }

    public void write(KlassOutput output) {
        output.writeInt(entries.size());
        for (CpEntry entry : entries) {
            entry.write(output);
        }
    }

    public void read(KlassInput input) {
        clear();
        int entryCount = input.readInt();
        for (int i = 0; i < entryCount; i++) {
            var value = input.readElement();
            addValue(value);
        }
    }

    public int size() {
        return entries.size();
    }

    @Override
    public Type getType(int index) {
        return (Type) values[index];
    }

    @Override
    public MethodRef getMethodRef(int index) {
        return (MethodRef) values[index];
    }

    public ClassType getClassType(int index) {
        return (ClassType) values[index];
    }

    public ResolutionStage getStage() {
        return stage;
    }

    public void setStage(ResolutionStage stage) {
        this.stage = stage;
    }

    @Override
    protected String toString0() {
        return Arrays.toString(getValues());
    }

    public ConstantPool copy() {
        var copy = new ConstantPool();
        for (CpEntry entry : entries) {
            copy.addValue(entry.getValue());
        }
        return copy;
    }
}
