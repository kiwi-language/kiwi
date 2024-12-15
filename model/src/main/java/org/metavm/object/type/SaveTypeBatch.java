package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.FieldChange;
import org.metavm.ddl.FieldChangeKind;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.WAL;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import java.util.*;

@Slf4j
public class SaveTypeBatch implements TypeDefProvider {

    public static SaveTypeBatch create(IEntityContext context) {
        return new SaveTypeBatch(context);
    }

    private final IEntityContext context;
    // Order matters! Don't use HashMap
    private final Set<Field> newFields = new HashSet<>();
    private final Set<Field> typeChangedFields = new HashSet<>();
    private final Set<Field> toChildFields = new HashSet<>();
    private final Set<Field> toNonChildFields = new HashSet<>();
    private final Set<Field> removedChildFields = new HashSet<>();
    private final Set<Klass> changingSuperKlasses = new HashSet<>();
    private final Set<Klass> entityToValueKlasses = new HashSet<>();
    private final Set<Klass> valueToEntityKlasses = new HashSet<>();
    private final Set<Klass> toEnumKlasses = new HashSet<>();
    private final Set<Klass> fromEnumKlasses = new HashSet<>();
    private final Set<Method> runMethods = new HashSet<>();
    private final Set<EnumConstantDef> newEnumConstantDefs = new HashSet<>();
    private final Set<EnumConstantDef> changedEnumConstantDefs = new HashSet<>();
    private final Set<Klass> klasses = new HashSet<>();
    private final Set<Klass> newKlasses = new HashSet<>();
    private final Set<Index> newIndexes = new HashSet<>();
    private final Set<Klass> searchEnabledClasses = new HashSet<>();
    private final Set<Field> newStaticFields = new HashSet<>();

    private SaveTypeBatch(IEntityContext context) {
        this.context = context;
    }

    public void addNewField(Field field) {
        newFields.add(field);
    }

    public void addNewStaticField(Field field) {
        assert field.isStatic();
        newStaticFields.add(field);
    }

    public void addNewIndex(Index index) {
        newIndexes.add(index);
    }

    public void addTypeChangedField(Field field) {
        typeChangedFields.add(field);
    }

    public void addToChildField(Field field) {
        toChildFields.add(field);
    }

    public void addToNonChildField(Field field) {
        toNonChildFields.add(field);
    }

    public void addRemovedChildField(Field field) {
        removedChildFields.add(field);
    }

    public void addChangingSuperKlass(Klass klass) {
        changingSuperKlasses.add(klass);
    }

    public void addEntityToValueKlass(Klass klass) {
        entityToValueKlasses.add(klass);
    }

    public void addValueToEntityKlass(Klass klass) {
        valueToEntityKlasses.add(klass);
    }

    public void addToEnumKlass(Klass klass) {
        toEnumKlasses.add(klass);
    }

    public void addFromEnumKlass(Klass klass) {
        fromEnumKlasses.add(klass);
    }

    public void addNewEnumConstantDef(EnumConstantDef newEnumConstantDef) {
        newEnumConstantDefs.add(newEnumConstantDef);
    }

    public void addChangedEnumConstantDef(EnumConstantDef changedEnumConstantDef) {
        changedEnumConstantDefs.add(changedEnumConstantDef);
    }

    public void addRunMethod(Method method) {
        this.runMethods.add(method);
    }

    public void addSearchEnabledKlass(Klass klass) {
        searchEnabledClasses.add(klass);
    }

    public Set<EnumConstantDef> getNewEnumConstantDefs() {
        return newEnumConstantDefs;
    }

    public IEntityContext getContext() {
        return context;
    }

    public TypeDef getTypeDef(String id) {
        return (TypeDef) getTypeDef(Id.parse(id));
    }

    @Override
    public ITypeDef getTypeDef(Id id) {
        return context.getTypeDef(id);
    }

    public Klass getKlass(String id) {
        return (Klass) getTypeDef(id);
    }

    public TypeVariable getTypeVariable(String id) {
        return (TypeVariable) getTypeDef(id);
    }

    public CapturedTypeVariable getCapturedTypeVariable(String id) {
        return (CapturedTypeVariable) getTypeDef(id);
    }

    public Commit buildCommit(WAL wal) {
        checkForDDL();
        var fieldChanges = new ArrayList<FieldChange>();
        for (Field f : newFields) {
            fieldChanges.add(new FieldChange(
                    f.getDeclaringType().getStringId(),
                    f.getStringId(),
                    f.getTag(),
                    f.getTag(),
                    FieldChangeKind.CREATION));
        }
        for (Field f : typeChangedFields) {
            fieldChanges.add(new FieldChange(
                    f.getDeclaringType().getStringId(),
                    f.getStringId(),
                    f.getOriginalTag(),
                    f.getTag(),
                    FieldChangeKind.TYPE_CHANGE));
        }
        for (Klass k : changingSuperKlasses) {
            var s = Objects.requireNonNull(k.getSuperKlass());
            s.forEachField(f -> fieldChanges.add(new FieldChange(
                    k.getStringId(),
                    f.getStringId(),
                    f.getTag(),
                    f.getTag(),
                    FieldChangeKind.SUPER_CLASS_ADDED
            )));
        }
        return new Commit(
                wal,
                NncUtils.map(newFields, Entity::getStringId),
                NncUtils.map(typeChangedFields, Entity::getStringId),
                NncUtils.map(toChildFields, Entity::getStringId),
                NncUtils.map(toNonChildFields, Entity::getStringId),
                NncUtils.map(removedChildFields, Entity::getStringId),
                NncUtils.map(changingSuperKlasses, Entity::getStringId),
                NncUtils.map(entityToValueKlasses, Entity::getStringId),
                NncUtils.map(valueToEntityKlasses, Entity::getStringId),
                NncUtils.map(toEnumKlasses, Entity::getStringId),
                NncUtils.map(fromEnumKlasses, Entity::getStringId),
                NncUtils.map(runMethods, Entity::getStringId),
                NncUtils.filterAndMap(newIndexes, i -> !context.isNewEntity(i.getDeclaringType()), Entity::getStringId),
                NncUtils.map(searchEnabledClasses, Entity::getStringId),
                fieldChanges
        );
    }

    private void checkForDDL() {
        for (Field field : newFields) {
            if (Instances.findFieldInitializer(field, fromEnumKlasses.contains(field.getDeclaringType())) == null
                    && field.getInitializer() == null && Instances.getDefaultValue(field, context) == null)
                throw new BusinessException(ErrorCode.MISSING_FIELD_INITIALIZER, field.getQualifiedName());
        }
        for (Field field : typeChangedFields) {
            if (Instances.findTypeConverter(field) == null)
                throw new BusinessException(ErrorCode.MISSING_TYPE_CONVERTER, field.getQualifiedName());
        }
        for (var klass : changingSuperKlasses) {
            if (Instances.findSuperInitializer(klass) == null) {
                var superClass = Objects.requireNonNull(klass.getSuperType()).getKlass();
                for (Field field : superClass.getAllFields()) {
                    if(Instances.getDefaultValue(field, context) == null)
                        throw new BusinessException(ErrorCode.MISSING_SUPER_INITIALIZER, klass.getName());
                }
            }
        }
    }

    public void addKlass(Klass klass) {
        klasses.add(klass);
        if(context.isNewEntity(klass))
            newKlasses.add(klass);
    }

    public Set<Klass> getKlasses() {
        return Collections.unmodifiableSet(klasses);
    }

    public Set<Klass> getNewKlasses() {
        return Collections.unmodifiableSet(newKlasses);
    }

    public Set<Index> getNewIndexes() {
        return Collections.unmodifiableSet(newIndexes);
    }

    public Set<Field> getNewStaticFields() {
        return Collections.unmodifiableSet(newStaticFields);
    }

}
