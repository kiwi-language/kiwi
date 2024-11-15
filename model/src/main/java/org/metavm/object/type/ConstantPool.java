package org.metavm.object.type;

import org.metavm.api.ChildEntity;
import org.metavm.entity.*;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.rest.dto.ConstantPoolDTO;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPool extends Entity implements LoadAware {

    @ChildEntity
    private final ReadWriteArray<CpEntry> entries = addChild(new ReadWriteArray<>(CpEntry.class), "entries");
    @CopyIgnore
    private transient Map<Object, CpEntry> value2entry = new HashMap<>();
    @CopyIgnore
    private transient volatile Object[] resolvedValues;

    public void addEntry(CpEntry entry) {
        entries.add(entry);
        value2entry.put(entry.getValue(), entry);
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
            case FieldRef fieldRef -> new FieldCpEntry(i, fieldRef);
            case MethodRef methodRef -> new MethodCpEntry(i, methodRef);
            case FunctionRef funcRef -> new FunctionCpEntry(i, funcRef);
            case LambdaRef lambdaRef -> new LambdaCpEntry(i, lambdaRef);
            case Type type -> new TypeCpEntry(i, type);
            case IndexRef indexRef -> new IndexCpEntry(i, indexRef);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
        addEntry(entry);
        return entry;
    }

    public synchronized void resolve() {
        if(resolvedValues != null)
            return;
        var entries = this.entries;
        var r = new Object[entries.size()];
        int i = 0;
        for (CpEntry entry : entries)
            r[i++] = entry.resolve();
        this.resolvedValues = r;
    }

    public void clear() {
        entries.clear();
        value2entry.clear();
        resolvedValues = null;
    }

    public Object[] getResolvedValues() {
        if(resolvedValues == null)
            resolve();
        return resolvedValues;
    }

    public ConstantPoolDTO toDTO(SerializeContext serializeContext) {
        return new ConstantPoolDTO(NncUtils.map(entries, e -> e.toDTO(serializeContext)));
    }

    public List<CpEntry> getEntries() {
        return entries.toList();
    }

    @Override
    public void onLoadPrepare() {
        value2entry = new HashMap<>();
    }

    @Override
    public void onLoad() {
        value2entry = new HashMap<>();
        for (CpEntry entry : entries)
            value2entry.put(entry.getValue(), entry);
    }
}
