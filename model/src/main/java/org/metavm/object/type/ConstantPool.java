package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.api.ValueObject;
import org.metavm.entity.*;
import org.metavm.wire.*;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import java.util.*;
import java.util.function.Consumer;

@Wire
@Slf4j
public class ConstantPool implements LoadAware, TypeMetadata, Element, LocalKey, Struct, ValueObject {

    @Parent
    private final ConstantScope scope;
    private List<Value> entries = new ArrayList<>();
    private transient ConstantPool template;
    @CopyIgnore
    private transient Map<Object, Integer> value2entry = new HashMap<>();
    @CopyIgnore
    private transient Object[] values = new Object[1];

    public transient List<Type> typeArguments;
    @Setter
    @Getter
    private transient ResolutionStage stage = ResolutionStage.INIT;
    @Getter
    private transient int version;

    public ConstantPool(ConstantScope scope) {
        this.scope = scope;
        this.template = this;
    }

    public ConstantPool(ConstantPool template, List<? extends Type> typeArguments) {
        this(template.scope);
        this.typeArguments = new ArrayList<>(typeArguments);
        this.template = template;
    }

    @Generated
    public static ConstantPool read(MvInput input, Object parent) {
        var r = new ConstantPool((ConstantScope) parent);
        r.entries = input.readList(input::readValue);
        r.onRead();
        return r;
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitList(visitor::visitValue);
    }

    public int addValue(Value value) {
        var idx = value2entry.get(value);
        if(idx != null)
            return idx;
        return addEntry(value);
    }

    public int addEntry(Value value) {
        version++;
        int i = entries.size();
        entries.add(value);
        value2entry.put(value, i);
        while (values.length < entries.size())
            values = Arrays.copyOf(values, values.length << 1);
        values[i] = value;
        return i;
    }

    public void clear() {
        version++;
        entries = new ArrayList<>();
        value2entry = new HashMap<>();
        values = new Object[1];
    }

    @JsonIgnore
    public Object[] getValues() {
        return values;
    }

    public List<Value> getEntries() {
        return Collections.unmodifiableList(entries);
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
        for (var entry : entries) {
            value2entry.put(entry, i);
            values[i++] = entry;
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

    public KlassType getKlassType(int index) {
        return (KlassType) values[index];
    }

    @Override
    public String toString() {
        return Arrays.toString(getValues());
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return "constantPool";
    }

    private void onRead() {
        values = new Object[Math.max(1, entries.size())];
        value2entry = new HashMap<>();
        int i = 0;
        for (var entry : entries) {
            value2entry.put(entry, i);
            values[i++] = entry;
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConstantPool(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    @SuppressWarnings("unused")
    public void printEntries() {
        int i = 0;
        for (Value entry : entries) {
            log.trace("Entry {}: {}", i++ ,entry);
        }
    }

    public void forEachReference(Consumer<Reference> action) {
        for (var entries_ : entries)
            if (entries_ instanceof Reference r) action.accept(r);
            else if (entries_ instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    public void buildJson(Map<String, Object> map) {
        map.put("entries", this.getEntries().stream().map(Value::toJson).toList());
        map.put("stage", this.getStage().name());
    }

    @Generated
    public void write(MvOutput output) {
        output.writeList(entries, output::writeValue);
    }

    public Map<String, Object> toJson() {
        var map = new HashMap<String, Object>();
        buildJson(map);
        return map;
    }

    public ConstantPool parameterize(List<Type> typeArgs) {
        if (typeArgs.isEmpty() || Utils.biAllMatch(scope.getAllTypeParameters(), typeArgs, (tv, t) -> tv.getType().equals(t)))
            return this;
        var typeMetadata = new ConstantPool(this, typeArgs);
        substitute(typeMetadata);
        return typeMetadata;
    }

    public void ensureUptodate() {
        if (template != null && template.version > version)
            template.substitute(this);
    }

    private void substitute(ConstantPool parameterized) {
        Utils.require(parameterized.scope == scope, "Cannot substitute constant pool with different scope");
        var subst = new SubstitutorV2(
                this, scope.getAllTypeParameters(), parameterized.typeArguments, parameterized, stage);
        this.accept(subst);
        parameterized.version = version;
    }

}
