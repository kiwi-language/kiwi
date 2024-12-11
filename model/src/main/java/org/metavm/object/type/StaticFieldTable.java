package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Slf4j
public class StaticFieldTable extends org.metavm.entity.Entity implements LoadAware, GlobalKey {

    public static final Logger logger = LoggerFactory.getLogger(StaticFieldTable.class);

    public static final IndexDef<StaticFieldTable> IDX_KLASS = IndexDef.createUnique(StaticFieldTable.class, "klass");

    public static StaticFieldTable getInstance(ClassType type, IEntityContext context) {
        var klass = type.getKlass();
        var sft = context.selectFirstByKey(IDX_KLASS, klass);
        if(sft == null) {
            sft = new StaticFieldTable(klass);
            context.bind(sft);
        }
        return sft;
    }

    private final Klass klass;

    @ChildEntity
    private final ChildArray<StaticFieldTableEntry> entries = addChild(new ChildArray<>(StaticFieldTableEntry.class), "entries");

    private transient Map<Field, StaticFieldTableEntry> map = new HashMap<>();

    public StaticFieldTable(Klass klass) {
        this.klass = klass;
    }

    @Override
    public void onLoad() {
        map = new HashMap<>();
        for (StaticFieldTableEntry entry : entries) {
            map.put(entry.getField(), entry);
        }
    }

    public Klass getKlass() {
        return klass;
    }

    public Value getByName(String name) {
        return get(klass.getStaticFieldByName(name));
    }

    public Value get(Field field) {
        assert field.getDeclaringType() == klass;
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
        assert field.getDeclaringType() == klass;
        var entry = map.get(field);
        if(entry != null)
            entry.setValue(value);
        else {
            entry = new StaticFieldTableEntry(field, value);
            entries.addChild(entry);
            map.put(field, entry);
        }
    }

    public boolean isEnumConstant(Reference reference) {
        assert klass.isEnum();
        for (EnumConstantDef ecd : klass.getEnumConstantDefs()) {
            if(reference.equals(get(ecd.getField())))
                return true;
        }
        return false;
    }

    public List<ClassInstance> getEnumConstants() {
        return NncUtils.map(klass.getEnumConstantDefs(), ecd -> get(ecd.getField()).resolveObject());
    }

    public void remove(Field field) {
        entries.removeIf(e -> e.getField() == field);
    }

    public EnumConstantRT getEnumConstant(Id id) {
        assert klass.isEnum();
        for (EnumConstantDef ecd : klass.getEnumConstantDefs()) {
            var ref = (Reference) get(ecd.getField());
            if(id.equals(ref.tryGetId()))
                return createEnumConstant(ref.resolveObject());
        }
        throw new InternalException("Can not find enum constant with id " + id);
    }

    public ClassInstance getEnumConstantByName(String name) {
        var ecd = NncUtils.findRequired(klass.getEnumConstantDefs(), e -> e.getName().equals(name));
        return get(ecd.getField()).resolveObject();
    }

    private EnumConstantRT createEnumConstant(ClassInstance instance) {
        return new EnumConstantRT(instance);
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return klass.getQualifiedName();
    }

}
