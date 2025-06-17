package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.classfile.ClassFileListener;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.FieldChange;
import org.metavm.ddl.FieldChangeKind;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRepository;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
public class SaveTypeBatch implements TypeDefProvider, ClassFileListener {

    public static SaveTypeBatch create(IInstanceContext context) {
        return new SaveTypeBatch(context);
    }

    private final IInstanceContext context;
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
    private final Set<Field> newEnumConstants = new HashSet<>();
    private final Set<Field> modifiedEnumConstants = new HashSet<>();
    private final Set<Klass> klasses = new HashSet<>();
    private final Set<Klass> newKlasses = new HashSet<>();
    private final Set<Index> newIndexes = new HashSet<>();
    private final Set<Klass> searchEnabledClasses = new HashSet<>();
    private final Set<Field> newStaticFields = new HashSet<>();
    private final Set<Field> removedEnumConstants = new HashSet<>();
    private final boolean tracing = DebugEnv.traceDeployment;


    private SaveTypeBatch(IInstanceContext context) {
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

    public void addNewEnumConstant(Field newEnumConstant) {
        newEnumConstants.add(newEnumConstant);
    }

    public void addModifiedEnumConstant(Field changedEnumConstant) {
        modifiedEnumConstants.add(changedEnumConstant);
    }

    public void addRunMethod(Method method) {
        this.runMethods.add(method);
    }

    public void addSearchEnabledKlass(Klass klass) {
        searchEnabledClasses.add(klass);
    }

    public Set<Field> getNewEnumConstants() {
        return newEnumConstants;
    }

    public Set<Field> getModifiedEnumConstants() {
        return modifiedEnumConstants;
    }

    public EntityRepository getContext() {
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

    public Commit buildCommit() {
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
                PhysicalId.of(context.allocateTreeId(), 0),
                context.getAppId(),
                Utils.map(newFields, Entity::getStringId),
                Utils.map(typeChangedFields, Entity::getStringId),
                Utils.map(toChildFields, Entity::getStringId),
                Utils.map(toNonChildFields, Entity::getStringId),
                Utils.map(removedChildFields, Entity::getStringId),
                Utils.map(changingSuperKlasses, Entity::getStringId),
                Utils.map(entityToValueKlasses, Entity::getStringId),
                Utils.map(valueToEntityKlasses, Entity::getStringId),
                Utils.map(toEnumKlasses, Entity::getStringId),
                Utils.map(fromEnumKlasses, Entity::getStringId),
                Utils.map(runMethods, Entity::getStringId),
                Utils.filterAndMap(newIndexes, i -> i.getDeclaringType().isPersisted(), Entity::getStringId),
                Utils.map(searchEnabledClasses, Entity::getStringId),
                Utils.map(modifiedEnumConstants, Entity::getStringId),
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

    public void applyDDLToEnumConstants() {
        var tracing = DebugEnv.traceDeployment;
        var enumConstants = new ArrayList<ClassInstance>();
        for (Klass klass : klasses) {
            if (klass.isEnum()) {
                for (var ec : klass.getEnumConstants()) {
                    ec.updateEnumConstant(context);
                    enumConstants.add(ec.getStatic(context).resolveObject());
                }
            }
        }
        for (ClassInstance enumConstant : enumConstants) {
            Instances.migrate(
                    (MvClassInstance) enumConstant,
                    newFields,
                    typeChangedFields,
                    changingSuperKlasses,
                    entityToValueKlasses,
                    valueToEntityKlasses,
                    toEnumKlasses,
                    removedChildFields,
                    runMethods,
                    newIndexes,
                    searchEnabledClasses,
                    null,
                    context
            );
        }
    }

    public void addKlass(Klass klass) {
        klasses.add(klass);
        if(klass.isNew())
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

    public void addRemovedEnumConstant(Field enumConstant) {
        if (DebugEnv.traceDeployment)
            log.trace("Adding removed enum constant {}", enumConstant.getQualifiedName(), new Exception());
        removedEnumConstants.add(enumConstant);
    }

    // Class file listener implementation

    private record FieldInfo(Type type, MetadataState state, String name, int ordinal) {

        static FieldInfo fromField(Field field) {
            return new FieldInfo(field.getType(), field.getState(), field.getName(), field.getOrdinal());
        }

    }

    private static class KlassInfo {

        static KlassInfo fromKlass(Klass klass, @Nullable KlassInfo parent) {
            return new KlassInfo(
                    klass.getKind(),
                    klass.isSearchable(),
                    klass.getSuperType(),
                    klass.getFields(),
                    klass.getEnumConstants(),
                    klass.getNextFieldTag(),
                    klass.getNextFieldSourceCodeTag(),
                    parent);
        }

        @Nullable KlassInfo parent;
        private final ClassKind kind;
        private final List<Field> fields;
        private final List<Field> enumConstants;
        private final ClassType superType;
        private final boolean searchable;
        private int nextFieldTag;
        private int nextFieldSourceTag;

        KlassInfo(ClassKind kind, boolean searchable, ClassType superType,  List<Field> fields,
                  List<Field> enumConstants, int nextFieldTag, int nextFieldSourceTag,
                  @Nullable KlassInfo parent) {
            this.kind = kind;
            this.searchable = searchable;
            this.superType = superType;
            this.fields = new ArrayList<>(fields);
            this.enumConstants = new ArrayList<>(enumConstants);
            this.nextFieldTag = nextFieldTag;
            this.nextFieldSourceTag = nextFieldSourceTag;
            this.parent = parent;
        }

        public static KlassInfo empty(KlassInfo klassInfo) {
            return new KlassInfo(
                    ClassKind.CLASS,
                    false,
                    null,
                    List.of(),
                    List.of(),
                    0,
                    1000000,
                    klassInfo
            );
        }

        int nextFieldTag() {
            return nextFieldTag++;
        }

        int nextFieldSourceTag() {
            return nextFieldSourceTag++;
        }
    }

    private FieldInfo fieldInfo;
    private KlassInfo klassInfo;

    @Override
    public void onFieldCreate(Field field) {
        if (tracing) log.trace("New field created: {}", field.getQualifiedName());
        context.bind(field);
        field.initTag(Objects.requireNonNull(klassInfo).nextFieldTag());
        if(field.getSourceTag() == null)
            field.setSourceTag(klassInfo.nextFieldSourceTag());
        if(field.isStatic())
            addNewStaticField(field);
        else if (field.getDeclaringType().isPersisted())
            addNewField(field);
        if (field.isEnumConstant())
            addNewEnumConstant(field);
    }

    @Override
    public void beforeFieldUpdate(Field field) {
        fieldInfo = FieldInfo.fromField(field);
    }

    @Override
    public void onFieldUpdate(Field field) {
        var tracing = this.tracing;
        var fieldInfo = Objects.requireNonNull(this.fieldInfo);
        if(tracing) log.trace("Field {} updated", field.getQualifiedName());
        if(!field.isStatic() && !field.getType().isAssignableFrom(fieldInfo.type)) {
            addTypeChangedField(field);
            field.changeTag(Objects.requireNonNull(klassInfo).nextFieldTag());
        }
        if(fieldInfo.state != field.getState()) {
            if(fieldInfo.state == MetadataState.REMOVED) {
                if (field.isStatic())
                    addNewStaticField(field);
                else {
                    addNewField(field);
                    if (tracing) log.trace("Resurrecting removed field {}", field.getQualifiedName());
                }
            }
        }
        if (field.isEnumConstant() && (!fieldInfo.name.equals(field.getName()) || fieldInfo.ordinal != field.getOrdinal()))
            addModifiedEnumConstant(field);
        getContext().updateMemoryIndex(field);
    }

    @Override
    public void onMethodRead(Method method) {
        if(method.getParameters().isEmpty() && !method.isStatic() && method.getName().equals(Constants.RUN_METHOD_NAME))
            addRunMethod(method);
//        if (method.hasBody())
//            method.getCode().rebuildNodes();
    }

    @Override
    public void onIndexRead(Index index) {
        if (index.isNew())
            addNewIndex(index);
    }

    @Override
    public void beforeKlassCreate() {
        klassInfo = KlassInfo.empty(this.klassInfo);
    }

    @Override
    public void onKlassCreate(Klass klass) {
        if (tracing) log.trace("Klass '{}' ({}) created", klass.getName(), klass.getId());
        var klassInfo = Objects.requireNonNull(this.klassInfo);
        klass.setNextFieldTag(klassInfo.nextFieldTag);
        klass.setNextFieldSourceCodeTag(klassInfo.nextFieldSourceTag);
        klasses.add(klass);
        context.updateMemoryIndex(klass);
        if(klass.getTag() == TypeTags.DEFAULT) {
            klass.setTag(KlassTagAssigner.getInstance(context).next());
            if (tracing) log.trace("Assigned tag {} to klass {}", klass.getTag(), klass.getName());
        } if(klass.getSourceTag() == null)
            klass.setSourceTag(KlassSourceCodeTagAssigner.getInstance(context).next());
    }

    @Override
    public void beforeKlassUpdate(Klass klass) {
        klassInfo = KlassInfo.fromKlass(klass, klassInfo);
        if (tracing) log.trace("Klass {} to be updated", klass.getName());
    }

    @Override
    public void onKlassUpdate(Klass klass) {
        var tracing = this.tracing;
        klasses.add(klass);
        var klassInfo = Objects.requireNonNull(this.klassInfo);
        klass.setNextFieldTag(klassInfo.nextFieldTag);
        klass.setNextFieldSourceCodeTag(klassInfo.nextFieldSourceTag);
        if (tracing) log.trace("Klass '{}' updated", klass.getName());
        var prevKind = klassInfo.kind;
        var newKind = klass.getKind();
        if(prevKind != newKind) {
            if(newKind == ClassKind.VALUE)
                addEntityToValueKlass(klass);
            else if (prevKind == ClassKind.VALUE)
                addValueToEntityKlass(klass);
            if(newKind == ClassKind.ENUM) {
                if (tracing) log.trace("Class {} changed to enum", klass.getQualifiedName());
                addToEnumKlass(klass);
            } else if(prevKind == ClassKind.ENUM) {
                if (tracing) log.trace("Enum {} changed to ordinary class", klass.getQualifiedName());
                addFromEnumKlass(klass);
            }
        }
        if(!klass.isEnum() && klass.getSuperType() != null && !Objects.equals(klassInfo.superType, klass.getSuperType())) {
            if (tracing) log.trace("Super type of {} changed from {} to {}", klass.getName(), klassInfo.superType, klass.getSuperType());
            addChangingSuperKlass(klass);
        } if (klass.isSearchable() && !klassInfo.searchable)
            addSearchEnabledKlass(klass);
        if (klass.isEnum()) {
            var enumConstantSet = new HashSet<>(klass.getEnumConstants());
            for (var prevEnumConstant : klassInfo.enumConstants) {
                if (!enumConstantSet.contains(prevEnumConstant))
                    addRemovedEnumConstant(prevEnumConstant);
            }
        }
        context.updateMemoryIndex(klass);
    }

    @Override
    public void onKlassRemove(Klass klass) {
        var sft = StaticFieldTable.getInstance(klass.getType(), context);
        context.remove(sft);
    }
}
