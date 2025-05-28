package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@NativeEntity(58)
@Entity
@Slf4j
public class StaticFieldTable extends org.metavm.entity.Entity implements LoadAware, GlobalKey {

    public static final Logger logger = LoggerFactory.getLogger(StaticFieldTable.class);

    public static final IndexDef<StaticFieldTable> IDX_KLASS = IndexDef.createUnique(StaticFieldTable.class,
            1, staticFieldTable -> List.of(staticFieldTable.klassReference));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static StaticFieldTable getInstance(ClassType type, EntityRepository repository) {
        var klass = type.getKlass();
        var sft = repository.selectFirstByKey(IDX_KLASS, klass.getReference());
        if(sft == null) {
            if (DebugEnv.traceStaticFieldTableCreation)
                log.trace("Creating static field table for klass {}", klass.getQualifiedName());
            sft = new StaticFieldTable(repository.allocateRootId(), klass);
            repository.bind(sft);
        }
        return sft;
    }

    private Reference klassReference;

    private List<StaticFieldTableEntry> entries = new ArrayList<>();

    private transient Map<Field, StaticFieldTableEntry> map = new HashMap<>();

    public StaticFieldTable(@NotNull Id id, Klass klass) {
        super(id);
        this.klassReference = klass.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitList(() -> StaticFieldTableEntry.visit(visitor));
    }

    @Override
    public void onLoad() {
        map = new HashMap<>();
        for (StaticFieldTableEntry entry : entries) {
            map.put(entry.getField(), entry);
        }
    }

    public Value getByName(String name) {
        return get(getKlass().getStaticFieldByName(name));
    }

    public Value get(Field field) {
        assert field.getDeclaringType() == getKlass();
        var entry = map.get(field);
        return entry != null ? entry.getValue() : Instances.nullInstance();
    }

    public DoubleValue getDouble(Field field) {
        return (DoubleValue) get(field);
    }

    public LongValue getLong(Field field) {
        return (LongValue) get(field);
    }

    public void set(Field field, Value value) {
        assert field.getDeclaringType() == getKlass() : "Field " + field.getQualifiedName() + " is not defined in class " + getKlass() ;
        var entry = map.get(field);
        if(entry != null)
            entry.setValue(value);
        else {
            entry = new StaticFieldTableEntry(this, field, value);
            entries.add(entry);
            map.put(field, entry);
        }
    }

    public boolean isEnumConstant(Reference reference) {
        assert getKlass().isEnum();
        for (var ec : getKlass().getEnumConstants()) {
            if(reference.equals(get(ec)))
                return true;
        }
        return false;
    }

    public List<ClassInstance> getEnumConstants() {
        return Utils.map(getKlass().getEnumConstants(), ec -> get(ec).resolveObject());
    }

    public void remove(Field field) {
        entries.removeIf(e -> e.getField() == field);
    }

    public EnumConstantRT getEnumConstant(Id id) {
        assert getKlass().isEnum();
        for (var ec : getKlass().getEnumConstants()) {
            var ref = (EntityReference) get(ec);
            if(id.equals(ref.getId()))
                return createEnumConstant(ref.resolveObject());
        }
        throw new InternalException("Can not find enum constant with id " + id);
    }

    public ClassInstance getEnumConstantByName(String name) {
        var ec = Utils.findRequired(getKlass().getEnumConstants(), e -> e.getName().equals(name));
        return get(ec).resolveObject();
    }

    private EnumConstantRT createEnumConstant(ClassInstance instance) {
        return new EnumConstantRT(instance);
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getKlass().getQualifiedName();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public String getTitle() {
        return "";
    }

    public Klass getKlass() {
        return (Klass) klassReference.get();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(klassReference);
        for (var entries_ : entries) entries_.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("enumConstants", this.getEnumConstants());
        map.put("klass", this.getKlass().getStringId());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_StaticFieldTable;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.klassReference = (Reference) input.readValue();
        this.entries = input.readList(() -> StaticFieldTableEntry.read(input, this));
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeValue(klassReference);
        output.writeList(entries, arg0 -> arg0.write(output));
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }

    public void purgeRemovedFields() {
        var klass = getKlass();
        var fieldSet = new HashSet<>(klass.getStaticFields());
        var it = entries.iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (!fieldSet.contains(e.getField())) {
                it.remove();
                if (DebugEnv.traceDeployment)
                    log.trace("Purge removed field {} from SFT", e.getField().getQualifiedName());
            }
        }
    }

}
