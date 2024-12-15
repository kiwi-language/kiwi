package org.metavm.object.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.NativeBase;
import org.metavm.expression.Var;
import org.metavm.flow.Error;
import org.metavm.flow.*;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.metavm.util.NncUtils.*;

@Entity(searchable = true)
public class Klass extends TypeDef implements GenericDeclaration, ChangeAware, StagedEntity, GlobalKey, LoadAware, LocalKey {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final Logger logger = LoggerFactory.getLogger(Klass.class);

    public static final IndexDef<Klass> IDX_NAME = IndexDef.create(Klass.class, "name");

    public static final IndexDef<Klass> UNIQUE_QUALIFIED_NAME = IndexDef.createUnique(Klass.class, "qualifiedName");

    public static final IndexDef<Klass> UNIQUE_SOURCE_TAG = IndexDef.createUnique(Klass.class, "sourceTag");

    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String qualifiedName;
    private ClassKind kind;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean searchable;
    @Nullable
    private Integer superTypeIndex;
    @ChildEntity
    private final ReadWriteArray<Integer> interfaceIndexes = addChild(new ReadWriteArray<>(Integer.class), "interfaceIndexes");
    @ChildEntity
    private final ReadWriteArray<Klass> interfaces = addChild(new ReadWriteArray<>(Klass.class), "interfaces");
    private @Nullable Klass superKlass;
    private ClassSource source;
    @Nullable
    private String desc;
    @ChildEntity
    private final ChildArray<Field> fields = addChild(new ChildArray<>(Field.class), "fields");
    @Nullable
    private Field titleField;
    @ChildEntity
    private final ChildArray<Method> methods = addChild(new ChildArray<>(Method.class), "methods");
    private @Nullable Klass declaringKlass;
    @ChildEntity
    private final ChildArray<Klass> klasses = addChild(new ChildArray<>(Klass.class), "klasses");

    @ChildEntity
    private final ChildArray<Field> staticFields = addChild(new ChildArray<>(Field.class), "staticFields");
    @ChildEntity
    private final ChildArray<Constraint> constraints = addChild(new ChildArray<>(Constraint.class), "constraints");
    @ChildEntity
    private final ChildArray<EnumConstantDef> enumConstantDefs = addChild(new ChildArray<>(EnumConstantDef.class), "enumConstantDefs");
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isAbstract;
    private boolean isTemplate;
    // Don't remove, used for search
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean isParameterized;
    @ChildEntity
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @ChildEntity
    private final ChildArray<Error> errors = addChild(new ChildArray<>(Error.class), "errors");
    private boolean error;
    @Nullable
    private Flow enclosingFlow;

    // For unit test. Do not remove
    @ChildEntity(since = 1)
    @Nullable
    private KlassFlags flags;

    private int nextFieldTag;
    private int nextFieldSourceCodeTag = 1000000;

    private ClassTypeState state = ClassTypeState.INIT;

    private @Nullable Integer sourceTag;

    private long tag;

    private int since;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private boolean templateFlag = false;

    private boolean struct;

    @SuppressWarnings({"FieldMayBeFinal", "unused"}) // for unit test
    private boolean dummyFlag = false;

    private boolean methodTableBuildDisabled;

    @ChildEntity
    private final ConstantPool constantPool = addChild(new ConstantPool(), "constantPool");

    private transient ResolutionStage stage = ResolutionStage.INIT;

    @CopyIgnore
    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    private transient int level;

    private transient Map<Long,Integer> tag2level = new HashMap<>();

    private transient List<Klass> extensions = new ArrayList<>();

    private transient List<Klass> implementations = new ArrayList<>();

    private transient List<Klass> sortedKlasses = new ArrayList<>();

    private transient List<Field> sortedFields = new ArrayList<>();

    private transient int numFields;

    @CopyIgnore
    private transient volatile Closure closure;

    private transient ClassType type;

    private transient Class<? extends NativeBase> nativeClass;

    private transient boolean frozen;

    private transient Klass arrayKlass;

    private transient Klass componentKlass;

    private transient ClassType oldSuperType;

    public Klass(
            Long tmpId,
            String name,
            @Nullable String qualifiedName,
            @Nullable ClassType superType,
            List<ClassType> interfaces,
            @NotNull ClassKind kind,
            ClassSource source,
            boolean anonymous,
            boolean ephemeral,
            boolean struct,
            boolean searchable,
            @Nullable String desc,
            boolean isAbstract,
            boolean isTemplate,
            @Nullable Flow enclosingFlow,
            @Nullable Klass declaringKlass,
            List<TypeVariable> typeParameters,
            long tag,
            @Nullable Integer sourceTag,
            int since) {
        setTmpId(tmpId);
        this.name = name;
        this.qualifiedName = qualifiedName;
        this.kind = kind;
        this.isAbstract = isAbstract;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.struct = struct;
        this.searchable = searchable;
        this.source = source;
        this.desc = desc;
        this.tag = tag;
        this.sourceTag = sourceTag;
        this.since = since;
        this.enclosingFlow = enclosingFlow;
        this.declaringKlass = declaringKlass;
        this.numFields = superType != null ? superType.getKlass().getNumFields() : 0;
        setSuperType(superType, true);
        setInterfaces(interfaces, true);
        resetRank();
        if (enclosingFlow != null)
            enclosingFlow.addLocalKlass(this);
        if (declaringKlass != null)
            declaringKlass.addInnerKlass(this);
        closure = new Closure(this);
        resetRank();
        if (superType != null)
            superType.getKlass().addExtension(this);
        interfaces.forEach(it -> it.getKlass().addImplementation(this));
        setTypeParameters(typeParameters);
//        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        resetSortedClasses();
        NncUtils.requireTrue(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void resetSortedClasses() {
        sortedKlasses.clear();
        forEachSuperClass(sortedKlasses::add);
        level = sortedKlasses.size() - 1;
        sortedKlasses.sort(Comparator.comparingInt(Klass::getLevel));
        tag2level.clear();
        for (Klass k : sortedKlasses) {
            tag2level.put(k.getTag(), k.getLevel());
        }
    }

    public Map<Long, Integer> getTag2level() {
        return Collections.unmodifiableMap(tag2level);
    }

    private void removeImplementation(Klass klass) {
        implementations.remove(klass);
    }

    private void removeExtension(Klass klass) {
        extensions.remove(klass);
    }

    protected void setTemplateFlag(boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    public List<Field> getReadyFields() {
        return fields.toList();
    }

    public List<Field> getFields() {
        return fields.toList();
    }

    public List<Field> getAllFields() {
        if (superKlass != null)
            return NncUtils.union(superKlass.getAllFields(), fields.toList());
        else
            return fields.toList();
    }

    @Override
    public boolean isValidGlobalKey() {
        return isBuiltin();
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getQualifiedName();
    }

    public void forEachField(Consumer<Field> action) {
        if (superKlass != null)
            superKlass.forEachField(action);
        for (Field field : fields) {
            if(!field.isMetadataRemoved())
                action.accept(field);
        }
    }

    public long getTag() {
        return tag;
    }

    @Nullable
    public Integer getSourceTag() {
        return sourceTag;
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().getUnderlyingType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING);
        this.titleField = titleField;
    }

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    //<editor-fold desc="hierarchy">

    public List<Klass> getAncestorClasses() {
        List<Klass> result = new ArrayList<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitKlass(Klass klass) {
                if (klass.superKlass != null)
                    klass.superKlass.accept(this);
                result.add(klass);
                return super.visitKlass(klass);
            }
        });
        return result;
    }

    public Closure getClosure() {
        if(closure == null) {
            synchronized (this) {
                if(closure == null)
                    closure = new Closure(this);
            }
        }
        return closure;
    }

    public List<Klass> getSubKlasses() {
        return NncUtils.merge(extensions, implementations);
    }

    public List<Klass> getDescendantTypes() {
        List<Klass> types = new ArrayList<>();
        visitDescendantTypes(types::add);
        return types;
    }

    public void visitDescendantTypes(Consumer<Klass> action) {
        action.accept(this);
        for (Klass subType : extensions) {
            subType.visitDescendantTypes(action);
        }
        for (Klass subType : implementations) {
            subType.visitDescendantTypes(action);
        }
    }

    private void resetRank() {
        int r = superKlass != null ? superKlass.getRank() : 0;
        for (var it : interfaces) {
            var itRank = it.getRank();
            if (itRank > r)
                r = itRank;
        }
        rank = r + 1;
    }

    public int getRank() {
        return rank;
    }

    public List<Error> getErrors() {
        return errors.toList();
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void clearElementErrors(Element element) {
        errors.removeIf(e -> e.getElement() == element);
    }

    public void addError(Element element, ErrorLevel level, String message) {
        errors.addChild(new Error(element, level, message));
    }

    //</editor-fold>

    //<editor-fold desc="method">

    public Method getDefaultConstructor() {
        return getMethodByNameAndParamTypes(Types.getConstructorName(this), List.of());
    }

    public void sortFields(Comparator<Field> comparator) {
        this.fields.sort(comparator);
    }

    public ClassTypeState getState() {
        return state;
    }

    public boolean isDeployed() {
        return state.ordinal() >= ClassTypeState.DEPLOYED.ordinal();
    }

    public void setState(ClassTypeState state) {
        this.state = state;
    }

    public List<Klass> getSortedKlasses() {
        return sortedKlasses;
    }

    public List<Field> getSortedFields() {
        return sortedFields;
    }

    void resetFieldOffsets() {
        forEachExtension(Klass::resetSelfFieldOffset);
    }

    public int getNumFields() {
        return numFields;
    }

    private void resetSelfFieldOffset() {
        var offset = superKlass != null ? superKlass.getNumFields() : 0;
        for (Field f : getSortedFields()) {
            f.setOffset(offset++);
        }
        numFields = offset;
    }

    void resetFieldTransients() {
        resetSortedFields();
        resetFieldOffsets();
    }

    private void resetSortedFields() {
        this.sortedFields.clear();
        sortedFields.addAll(this.fields.toList());
        sortedFields.sort(Comparator.comparingInt(Field::getTag));
        assert fields.size() <= 1 || NncUtils.allMatch(fields, EntityUtils::isModelInitialized);
    }

    public void moveMethod(Method method, int index) {
        moveProperty(methods, method, index);
    }

    MethodTable getMethodTable() {
        if (methodTable == null) {
            synchronized (this) {
                if (methodTable == null)
                    methodTable = new MethodTable(this);
            }
        }
        return methodTable;
    }

    public List<Method> getMethods() {
        return methods.toList();
    }

    public @Nullable Method getWriteObjectMethod() {
        return getMethodTable().getWriteObjectMethod();
    }

    public @Nullable Method getReadObjectMethod() {
        return getMethodTable().getReadObjectMethod();
    }

    public Method getMethodByInternalName(String internalName) {
        return NncUtils.findRequired(methods, m -> m.getInternalName(null).equals(internalName),
                () -> "Failed to find method with internal name '" + internalName + "' in class " + getTypeDesc());
    }

    public ReadonlyArray<Method> getDeclaredMethods() {
        return methods;
    }

    public Method getMethod(long id) {
        return methods.get(org.metavm.entity.Entity::tryGetId, id);
    }

    public Method tryGetMethod(String name, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superKlass != null)
            return superKlass.getMethod(name, parameterTypes);
        return null;
    }

    public Method getMethod(String name, List<Type> parameterTypes) {
        return NncUtils.requireNonNull(
                tryGetMethod(name, parameterTypes),
                () -> "Can not find method '" + name + "(" +
                        NncUtils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'"
        );
    }

    public @Nullable Method findMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superKlass != null) {
            var m = superKlass.findMethodByNameAndParamTypes(name, parameterTypes);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.findMethodByNameAndParamTypes(name, parameterTypes);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public Method getMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var found = findMethodByNameAndParamTypes(name, parameterTypes);
        if(found == null) {
            throw new NullPointerException(String.format("Can not find method %s(%s) in klass %s",
                    name, NncUtils.join(parameterTypes, Type::getTypeDesc, ","), getTypeDesc()));
        }
        return found;
    }

    public Method getMethodByName(String name) {
        return Objects.requireNonNull(findMethodByName(name), () -> "Can not find method with name '" + name + "' in class '" + this.name + "'");
    }

    public @Nullable Method findMethodByName(String name) {
        return findMethod(Method::getName, name);
    }

    public @Nullable Method findSelfMethodByName(String name) {
        return methods.get(Flow::getName, name);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return NncUtils.find(methods.toList(), predicate);
    }

    public Method getSelfMethod(Predicate<Method> predicate) {
        return Objects.requireNonNull(findSelfMethod(predicate));
    }

    public Method getMethod(Predicate<Method> predicate) {
        return getMethod(predicate, () -> "Can not find method with predicate in klass " + this);
    }

    public Method getMethod(Predicate<Method> predicate, Supplier<String> messageSupplier) {
        var found = findMethod(predicate);
        if (found != null)
            return found;
//        if (DebugEnv.resolveVerbose) {
//        logger.debug("Fail to resolve method with predicate in klass " + getTypeDesc());
//        forEachMethod(m -> logger.info(m.getSignatureString()));
//        }
        throw new NullPointerException(messageSupplier.get());
    }

    public @Nullable Method findMethod(Predicate<Method> predicate) {
        var found = NncUtils.find(methods, predicate);
        if (found != null)
            return found;
        if (superKlass != null && (found = superKlass.findMethod(predicate)) != null)
            return found;
        for (var it : interfaces) {
            if ((found = it.findMethod(predicate)) != null)
                return found;
        }
        return null;
    }

    public <T> @Nullable Method findMethod(IndexMapper<Method, T> property, T value) {
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superKlass != null) {
            var m = superKlass.findMethod(property, value);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.findMethod(property, value);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public void removeMethod(Method method) {
        methods.remove(method);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            getMethodTable().rebuild();
    }

    public void addMethod(Method method) {
        if (methods.contains(method))
            throw new InternalException("Method '" + method + "' is already added to the class type");
        methods.addChild(method);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            getMethodTable().rebuild();
        method.setDeclaringType(this);
    }
    //</editor-fold>

    public void addInnerKlass(Klass klass) {
        klasses.addChild(klass);
        klass.declaringKlass = this;
    }

    public List<Klass> getKlasses() {
        return klasses.toList();
    }

    public void setKlasses(List<Klass> klasses) {
        this.klasses.resetChildren(klasses);
        klasses.forEach(k -> k.declaringKlass = this);
    }

    public ReadonlyArray<Field> getDeclaredFields() {
        return fields;
    }

    public ReadonlyArray<Constraint> getDeclaredConstraints() {
        return constraints;
    }

    public void addField(Field field) {
        if (fields.contains(field))
            throw new RuntimeException("Field " + field.tryGetId() + " is already added");
        if (findSelfField(f -> f.getName().equals(field.getName())) != null
                || findSelfStaticField(f -> f.getName().equals(field.getName())) != null)
            throw BusinessException.invalidField(field, "Field name '" + field.getName() + "' is already used in class " + getName());
        if (field.isStatic())
            staticFields.addChild(field);
        else
            fields.addChild(field);
        if(!Constants.maintenanceDisabled)
            resetFieldTransients();
        field.setDeclaringType(this);
    }

    @Override
    public void onLoadPrepare() {
        extensions = new ArrayList<>();
        implementations = new ArrayList<>();
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
        if(!Constants.maintenanceDisabled) {
            closure = new Closure(this);
            sortedFields = new ArrayList<>();
            resetSortedFields();
            sortedKlasses = new ArrayList<>();
            tag2level = new HashMap<>();
            resetSortedClasses();
            if (superKlass != null)
                superKlass.addExtension(this);
            interfaces.forEach(it -> it.addImplementation(this));
            resetSelfFieldOffset();
            resetRank();
        }
    }

    protected void addExtension(Klass klass) {
        if (!frozen)
            extensions.add(klass);
    }

    protected void addImplementation(Klass klass) {
        if (!frozen)
            implementations.add(klass);
    }

    private void forEachExtension(Consumer<Klass> action) {
        action.accept(this);
        extensions.forEach(k -> k.forEachExtension(action));
    }

    private void forEachSubclass(Consumer<Klass> action) {
        action.accept(this);
        extensions.forEach(k -> k.forEachSubclass(action));
        implementations.forEach(k -> k.forEachSubclass(action));
    }

    public void freeze() {
        if (frozen)
            throw new IllegalStateException("Already frozen");
        this.frozen = true;
    }

    public int getLevel() {
        return level;
    }

    public List<Index> getFieldIndices(Field field) {
        return NncUtils.filter(
                getAllConstraints(Index.class),
                index -> index.isFieldIndex(field)
        );
    }

    public void addConstraint(Constraint constraint) {
        constraints.addChild(constraint);
        constraint.setDeclaringType(this);
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    @JsonIgnore
    public boolean isEnum() {
        return kind == ClassKind.ENUM;
    }

    public boolean isInterface() {
        return kind == ClassKind.INTERFACE;
    }

    public boolean isEffectiveAbstract() {
        return isInterface() || isAbstract;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @JsonIgnore
    public boolean isClass() {
        return kind == ClassKind.CLASS;
    }

    @JsonIgnore
    public boolean isValue() {
        return this.kind == ClassKind.VALUE;
    }

    @JsonIgnore
    public boolean isReference() {
        return isEnum() || isClass() || isInterface();
    }

    public Field getField(Id id) {
        var field = fields.get(org.metavm.entity.Entity::tryGetId, id);
        if (field != null)
            return field;
        if (superKlass != null)
            return superKlass.getField(id);
        throw new NullPointerException("Can not find field for " + id + " in type " + name);
    }

    public Method getMethod(Id id) {
        return Objects.requireNonNull(methods.get(org.metavm.entity.Entity::tryGetId, id));
    }

    public @Nullable Field findSelfField(Predicate<Field> predicate) {
        return NncUtils.find(fields, predicate);
    }

    public Klass getInnerKlass(Predicate<Klass> filter) {
        return Objects.requireNonNull(findInnerKlass(filter));
    }

    public @Nullable Klass findInnerKlass(Predicate<Klass> filter) {
        return NncUtils.find(klasses, filter);
    }

    public Field getSelfField(Predicate<Field> predicate) {
        return Objects.requireNonNull(findSelfField(predicate));
    }

    public @Nullable Field findSelfStaticField(Predicate<Field> predicate) {
        return NncUtils.find(staticFields, predicate);
    }

    public @Nullable Field findField(Predicate<Field> predicate) {
        var field = findSelfField(predicate);
        if (field != null)
            return field;
        if (superKlass != null)
            return superKlass.findField(predicate);
        return null;
    }

    public Field getField(Predicate<Field> predicate) {
        var found = findField(predicate);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose)
            forEachField(f -> logger.info(f.getQualifiedName()));
        throw new NullPointerException("Fail to find field satisfying the specified criteria in klass: " + this);
    }

    public @Nullable Field findStaticField(Predicate<Field> predicate) {
        var field = NncUtils.find(staticFields, predicate);
        if (field != null)
            return field;
        if (superKlass != null)
            return superKlass.findStaticField(predicate);
        return null;
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.requireNonNull(findFieldByName(fieldName));
    }

    public Field getStaticFieldByName(String fieldName) {
        return NncUtils.requireNonNull(findStaticFieldByName(fieldName),
                () -> "Static field " + fieldName + " not found in class " + getQualifiedName());
    }

    public Field getSelfStaticField(Predicate<Field> filter) {
        return Objects.requireNonNull(findSelfStaticField(filter));
    }

    /**
     * Get static field, instance method or static method by var.
     */
    public Property getStaticPropertyByVar(Var var) {
        return Objects.requireNonNull(
                findStaticPropertyByVar(var),
                () -> "Can not find property for " + var + " in type " + name
        );
    }

    /**
     * Find static field, instance method or static method by var.
     */
    public Property findStaticPropertyByVar(Var var) {
        if (var.isId()) {
            var p = findSelfStaticProperty(m -> m.idEquals(var.getId()));
            if (p != null)
                return p;
        } else {
            var p = findSelfStaticProperty(m -> m.getName().equals(var.getName()));
            if (p != null)
                return p;
        }
        if (superKlass != null) {
            var p = superKlass.findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        for (var it : interfaces) {
            var p = it.findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        return null;
    }

    private Property findSelfStaticProperty(Predicate<Property> predicate) {
        var field = NncUtils.find(staticFields, predicate);
        if (field != null)
            return field;
        return NncUtils.find(methods, predicate);
    }

    public void setSource(ClassSource source) {
        this.source = source;
    }

    @Nullable
    public Field findFieldByName(String name) {
        var field = fields.get(Field::getName, name);
        if (field != null)
            return field;
        if (superKlass != null)
            return superKlass.findFieldByName(name);
        return null;
    }

    public Property getPropertyByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getPropertyByName(var.getName());
            case ID -> findProperty(p -> p.idEquals(var.getId()));
        };
    }

    public Property getProperty(Id id) {
        return NncUtils.requireNonNull(getProperty(Property::tryGetId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getPropertyByName(String name) {
        return getProperty(Property::getName, name);
    }

    public Property findProperty(Predicate<Property> filter) {
        var field = NncUtils.find(fields, filter);
        if(field != null)
            return field;
        var method = NncUtils.find(methods, m -> !m.isStatic() && filter.test(m));
        if(method != null)
            return method;
        if(superKlass != null)
            return superKlass.findProperty(filter);
        return null;
    }

    private <T> Property getProperty(IndexMapper<Property, T> property, T value) {
        var field = fields.get(property, value);
        if (field != null)
            return field;
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superKlass != null)
            return superKlass.getProperty(property, value);
        return null;
    }

    public List<Property> getProperties() {
        return NncUtils.concatList(fields.toList(), methods.toList());
    }

    public Field getSelfFieldByName(String name) {
        return Objects.requireNonNull(findSelfFieldByName(name),
                () -> "Cannot find field \"" + name + "\" in klass " + getTypeDesc());
    }

    @Nullable
    public Field findSelfFieldByName(String name) {
        return NncUtils.find(fields, f -> f.getName().equals(name));
    }

    public Field getSelfStaticFieldByName(String name) {
        return Objects.requireNonNull(findSelfStaticFieldByName(name));
    }

    @Nullable
    public Field findSelfStaticFieldByName(String name) {
        return NncUtils.find(staticFields, f -> f.getName().equals(name));
    }

    @Nullable
    public Field findStaticFieldByName(String name) {
        if (superKlass != null) {
            Field superField = superKlass.findStaticFieldByName(name);
            if (superField != null)
                return superField;
        }
        return NncUtils.find(staticFields, f -> f.getName().equals(name));
    }

    @Nullable
    public Index findSelfIndex(Predicate<Index> predicate) {
        for (Constraint constraint : constraints) {
            if (constraint instanceof Index index && predicate.test(index))
                return index;
        }
        return null;
    }

    public ClassSource getSource() {
        return source;
    }

    public boolean isBuiltin() {
        return source == ClassSource.BUILTIN;
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = EntityUtils.getMetaFieldName(javaField);
        return requireNonNull(findFieldByName(fieldName),
                "Can not find field for java field " + javaField);
    }

    public boolean checkColumnAvailable(Column column) {
        return NncUtils.find(fields, f -> f.getColumn() == column) == null;
    }

    public boolean check() {
        return accept(new FlowChecker());
    }

    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if (fieldType.isBinaryNullable())
            fieldType = fieldType.getUnderlyingType();
        if (fieldType.getSQLType() == null)
            return null;
        return allocateColumn(fieldType, field);
    }

    public Column allocateColumn(Type fieldType, Field field) {
        Set<Column> usedColumns = filterAndMapUnique(
                getFieldsInHierarchy(),
                f -> !f.equals(field),
                Field::getColumn
        );
        return Column.allocate(usedColumns, fieldType.getSQLType());
    }


    private ReadonlyArray<Field> getFieldsInHierarchy() {
        return fields;
    }

    public Field getFieldNyName(String fieldName) {
        return NncUtils.requireNonNull(
                findFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic())
            staticFields.remove(field);
        else
            fields.remove(field);
        if(!Constants.maintenanceDisabled)
            resetFieldTransients();
    }

    public boolean isAssignableFrom(Klass that) {
        if (this == that) {
            return true;
        }
        if (that.superKlass != null && isAssignableFrom(that.superKlass))
            return true;
        if (isInterface()) {
            for (var it : that.interfaces) {
                if (isAssignableFrom(it)) {
                    return true;
                }
            }
        }
        return false;
    }

    public @NotNull ClassType getType() {
        if (type == null) {
            GenericDeclarationRef owner;
            if(declaringKlass != null)
                owner = declaringKlass.getType();
            else if(enclosingFlow != null)
                owner = enclosingFlow.getRef();
            else
                owner = null;
            type = new ClassType(owner, this, List.of());
        }
        return type;
    }

    @Nullable
    public ClassType getSuperType() {
        return superTypeIndex != null ? constantPool.getClassType(superTypeIndex) : null;
    }

    @Nullable
    public Integer getSuperTypeIndex() {
        return superTypeIndex;
    }

    @Nullable
    public Klass getSuperKlass() {
        return superKlass;
    }

    public List<ClassType> getInterfaces() {
        return Collections.unmodifiableList(NncUtils.map(interfaceIndexes, constantPool::getClassType));
    }

    public List<Integer> getInterfaceIndexes() {
        return interfaceIndexes.toList();
    }

    public void forEachSuper(Consumer<Klass> action) {
        if (superKlass != null)
            action.accept(superKlass);
        interfaces.forEach(action);
    }

    public void forEachSuperType(Consumer<ClassType> action, TypeMetadata typeMetadata) {
        if(superTypeIndex != null)
            action.accept((ClassType) typeMetadata.getType(superTypeIndex));
        interfaceIndexes.forEach(i -> action.accept((ClassType) typeMetadata.getType(i)));
    }

    public void forEachSuperClass(Consumer<Klass> action) {
        action.accept(this);
        if (superKlass != null)
            superKlass.forEachSuperClass(action);
    }

    protected Object getExtra() {
        return null;
    }

    public <T extends Constraint> List<T> getAllConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
        if (superKlass != null) {
            result = NncUtils.union(
                    superKlass.getAllConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public List<Constraint> getConstraints() {
        return constraints.toList();
    }

    public <T extends Constraint> T getConstraint(Class<T> constraintType, Id id) {
        return find(getAllConstraints(constraintType), c -> c.getId().equals(id));
    }

    public List<CheckConstraint> getFieldCheckConstraints(Field field) {
        var constraints = getAllConstraints(CheckConstraint.class);
        return NncUtils.filter(constraints, c -> c.isFieldConstraint(field));
    }

    @SuppressWarnings("unused")
    public Constraint getConstraint(Id id) {
        return NncUtils.find(requireNonNull(constraints), c -> c.idEquals(id));
    }

    public Index getUniqueConstraint(Id id) {
        return getConstraint(Index.class, id);
    }

    public boolean isType(Type type) {
        return getType().equals(type);
    }

    // BFS
    public void foreachAncestor(Consumer<Klass> action) {
        var queue = new LinkedList<Klass>();
        queue.offer(this);
        while (!queue.isEmpty()) {
            var k = queue.poll();
            action.accept(k);
            k.forEachSuper(queue::offer);
        }
    }

    public void forEachMethod(Consumer<Method> action) {
        methods.forEach(action);
        if (superKlass != null)
            superKlass.forEachMethod(action);
        interfaces.forEach(it -> it.forEachMethod(action));
    }

    @Nullable
    public MethodRef findOverride(Method method, TypeMetadata typeMetadata) {
        return getMethodTable().findOverride(method, typeMetadata);
    }

    public void updateParameterized() {
        ParameterizedStore.forEach(constantPool, (typeArgs, k) -> {
            var p = (ConstantPool) k;
            p.setStage(ResolutionStage.INIT);
            var subst = new SubstitutorV2(
                    this, typeParameters.toList(), typeArgs, k, stage);
            constantPool.accept(subst);
        });
    }

    public List<TypeVariable> getTypeParameters() {
        return typeParameters.toList();
    }

    public TypeVariable getTypeParameterByName(String name) {
        return NncUtils.findRequired(typeParameters, tp -> tp.getName().equals(name),
                () -> "Cannot find type parameter with name '" + name + "' in class '" + this + "'");
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getClassFilePath() {
        return Objects.requireNonNull(qualifiedName).replace('.', '/') + ".mvclass";
    }

    public void setQualifiedName(@Nullable String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public ClassKind getKind() {
        return kind;
    }

    public void setKind(ClassKind kind) {
        this.kind = kind;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public List<Type> getDefaultTypeArguments() {
        return NncUtils.map(typeParameters, TypeVariable::getType);
    }

    public Index findUniqueConstraint(List<Field> fields) {
        return find(getAllIndices(), c -> c.isUnique() && c.getTypeFields().equals(fields));
    }

    public Index findSelfUniqueConstraint(List<Field> fields) {
        return (Index) find(constraints,
                c -> c instanceof Index i && i.isUnique() && i.getTypeFields().equals(fields));
    }

    public List<Index> getIndices() {
        return NncUtils.filterByType(constraints, Index.class);
    }

    public List<Index> getAllIndices() {
        return getAllConstraints(Index.class);
    }

    @Override
    protected String toString0() {
        return getTypeDesc();
    }

    protected final void onSuperTypesChanged() {
        onAncestorChanged();
        resetSortedClasses();
        resetFieldOffsets();
    }

    public void resetHierarchy() {
        closure = new Closure(this);
        getMethodTable().rebuild();
        resetRank();
        resetSortedClasses();
        resetSortedFields();
        resetSelfFieldOffset();
    }

    private void onAncestorChanged() {
        forEachSubclass(Klass::onAncestorChangedSelf);
    }

    public void setSuperType(@Nullable ClassType superType) {
        setSuperType(superType, Constants.maintenanceDisabled);
    }

    public void setSuperType(@Nullable ClassType superType, boolean skipMaintenance) {
        if(skipMaintenance) {
            if (superType != null) {
                this.superTypeIndex = constantPool.addValue(superType);
                superKlass = superType.getKlass();
            }
            else {
                superTypeIndex = null;
                superKlass = null;
            }
        }
        else {
            if (this.superKlass != null) {
                this.superKlass.removeExtension(this);
            }
            if (superType != null) {
                this.superTypeIndex = constantPool.addValue(superType);
                this.superKlass = superType.getKlass();
                superKlass.addExtension(this);
            } else {
                this.superKlass = null;
                this.superTypeIndex = null;
            }
            onSuperTypesChanged();
        }
    }

    public void setInterfaces(List<ClassType> interfaces) {
        setInterfaces(interfaces, Constants.maintenanceDisabled);
    }

    public void setInterfaces(List<ClassType> interfaces, boolean skipMaintenance) {
        if(skipMaintenance) {
            this.interfaces.reset(NncUtils.map(interfaces, ClassType::getKlass));
            this.interfaceIndexes.reset(NncUtils.map(interfaces, constantPool::addValue));
        }
        else {
            for (var anInterface : this.interfaces) {
                anInterface.removeImplementation(this);
            }
            this.interfaces.clear();
            this.interfaceIndexes.clear();
            for (var anInterface : interfaces) {
                this.interfaces.add(anInterface.getKlass());
                this.interfaceIndexes.add(constantPool.addValue(anInterface));
                anInterface.getKlass().addImplementation(this);
            }
            onSuperTypesChanged();
        }
    }

    protected void onAncestorChangedSelf() {
        closure = new Closure(this);
        if(!methodTableBuildDisabled)
            getMethodTable().rebuild();
        resetRank();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> tp.setGenericDeclaration(this));
        this.typeParameters.resetChildren(typeParameters);
    }

    public void setFields(List<Field> fields) {
        fields.forEach(f -> f.setDeclaringType(this));
        this.fields.resetChildren(fields);
        if(!Constants.maintenanceDisabled)
            resetFieldTransients();
    }

    @Nullable
    public Flow getEnclosingFlow() {
        return enclosingFlow;
    }

    public boolean isLocal() {
        return enclosingFlow != null;
    }

    public void setEnclosingFlow(@Nullable Flow enclosingFlow) {
        this.enclosingFlow = enclosingFlow;
    }

    public void moveField(Field field, int index) {
        moveProperty(fields, field, index);
    }

    private <T extends org.metavm.entity.Entity & Property> void moveProperty(ChildArray<T> properties, T property, int index) {
        if (index < 0 || index >= properties.size())
            throw new BusinessException(ErrorCode.INDEX_OUT_OF_BOUND);
        if (!properties.remove(property))
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND, property.getName());
        if (index >= properties.size())
            properties.addChild(property);
        else
            properties.addChild(index, property);
    }

    public void setStaticFields(List<Field> staticFields) {
        staticFields.forEach(f -> f.setDeclaringType(this));
        this.staticFields.resetChildren(staticFields);
    }

    public void setConstraints(List<Constraint> constraints) {
        constraints.forEach(c -> c.setDeclaringType(this));
        this.constraints.resetChildren(constraints);
    }

    public void setMethods(List<Method> methods) {
        methods.forEach(m -> m.setDeclaringType(this));
        this.methods.resetChildren(methods);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            rebuildMethodTable();
    }

    @Override
    public String getTypeDesc() {
        if(declaringKlass != null)
            return declaringKlass.getTypeDesc() + "." + name;
        else if(enclosingFlow != null)
            return enclosingFlow.getTypeDesc() + "." + name;
        else
            return Objects.requireNonNullElse(qualifiedName, name);
    }

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        if (superKlass != null)
            superKlass.removeExtension(this);
        for (var anInterface : interfaces) {
            anInterface.removeImplementation(this);
        }
        var cascade = new ArrayList<>();
        var sft = context.selectFirstByKey(StaticFieldTable.IDX_KLASS, this);
        if (sft != null)
            cascade.add(sft);
        return cascade;
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var origStage = this.stage;
        this.stage = stage;
        return origStage;
    }

    public ResolutionStage getStage() {
        if (stage == null)
            stage = ResolutionStage.DEFINITION;
        return stage;
    }

    public void rebuildMethodTable() {
        getMethodTable().rebuild();
    }

    public void removeErrors(Element element) {
        errors.removeIf(e -> e.getElement() == element);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitKlass(this);
    }

    @Override
    public void onChange(ClassInstance instance, IEntityContext context) {
        if(!methodTableBuildDisabled)
            rebuildMethodTable();
        if (!isInterface()) {
            for (var it : interfaces) {
                for(var method : it.getMethods()) {
                    if (!method.isStatic() && findOverride(method, constantPool) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), method.getName(), it.getName());
                    }
                }
            }
        }
    }

    @Override
    public boolean isChangeAware() {
        return !anonymous;
    }

    public @Nullable Field getSelfTitleField() {
        return titleField;
    }

    @Nullable
    public Field getTitleField() {
        if (titleField != null)
            return titleField;
        if (superKlass != null)
            return superKlass.getTitleField();
        return null;
    }

    public List<Field> getStaticFields() {
        return staticFields.toList();
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return getQualifiedName();
    }

    @Override
    public GenericDeclarationRef getRef() {
        return getType();
    }

    public boolean isList() {
        return StdKlass.list.get().isAssignableFrom(this);
    }

    public boolean isChildList() {
        return this == StdKlass.childList.get();
    }

    public boolean isSAMInterface() {
        return isInterface() && NncUtils.count(methods, m -> m.isAbstract() && !m.isStatic()) == 1;
    }

    public Method getSingleAbstractMethod() {
        if (!isSAMInterface())
            throw new InternalException("Type " + getName() + " is not a SAM interface");
        return getMethod(m -> m.isAbstract() && !m.isStatic());
    }

    public boolean isStruct() {
        return struct;
    }

    public void setStruct(boolean struct) {
        this.struct = struct;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public void sortMethods(Comparator<Method> comparator) {
        methods.sort(comparator);
    }

    public int nextFieldTag() {
        return nextFieldTag++;
    }

    public Class<? extends NativeBase> getNativeClass() {
        return nativeClass;
    }

    public void setNativeClass(Class<? extends NativeBase> nativeClass) {
        this.nativeClass = nativeClass;
    }

    public List<EnumConstantDef> getEnumConstantDefs() {
        return enumConstantDefs.toList();
    }

    public @Nullable EnumConstantDef findEnumConstantDef(Predicate<EnumConstantDef> predicate) {
        return NncUtils.find(enumConstantDefs, predicate);
    }

    public void addEnumConstantDef(EnumConstantDef enumConstantDef) {
        this.enumConstantDefs.addChild(enumConstantDef);
        enumConstantDef.setKlass(this);
    }

    public void setEnumConstantDefs(List<EnumConstantDef> enumConstantDefs) {
        this.enumConstantDefs.resetChildren(enumConstantDefs);
        enumConstantDefs.forEach(ecd -> ecd.setKlass(this));
    }

    public void clearEnumConstantDefs() {
        enumConstantDefs.forEach(ecd -> removeField(ecd.getField()));
        enumConstantDefs.clear();
    }

    public int nextFieldSourceCodeTag() {
        return nextFieldSourceCodeTag++;
    }

    public int getSince() {
        return since;
    }

    @Nullable
    public KlassFlags getFlags() {
        return flags;
    }

    public boolean isFlag1() {
        return flags != null && flags.isFlag1();
    }

    public boolean isBeanClass() {
        return getAttribute(AttributeNames.BEAN_KIND) != null;
    }

    public @Nullable Method getHashCodeMethod() {
        return methodTable.getHashCodeMethod();
    }

    public @Nullable Method getToStringMethod() {
        return methodTable.getToStringMethod();
    }

    public @Nullable Method getEqualsMethod() {
        return methodTable.getEqualsMethod();
    }

    public Klass getArrayKlass() {
       if(arrayKlass == null) {
           arrayKlass = KlassBuilder.newBuilder(name + "[]", NncUtils.get(qualifiedName, c -> c + "[]")).build();
           arrayKlass.setEphemeralEntity(true);
           arrayKlass.componentKlass = this;
       }
       return arrayKlass;
    }

    public Klass getComponentKlass() {
        return componentKlass;
    }

    public boolean isOverrideOf(Method override, Method overridden) {
        return methodTable.findOverride(overridden, constantPool).getRawFlow() == override;
    }

    public ClassType getOldSuperType() {
        return oldSuperType;
    }

    public void setOldSuperType(ClassType oldSuperType) {
        this.oldSuperType = oldSuperType;
    }

    public void emitCode() {
        accept(new MaxesComputer());
        methods.forEach(Flow::emitCode);
        klasses.forEach(Klass::emitCode);
    }

    public void setTag(long tag) {
        this.tag = tag;
    }

    public void setSourceTag(@Nullable Integer sourceTag) {
        this.sourceTag = sourceTag;
    }

    public static final int FLAG_ABSTRACT = 1;
    public static final int FLAG_STRUCT = 2;
    public static final int FLAG_SEARCHABLE = 4;
    public static final int FLAG_EPHEMERAL = 8;
    public static final int FLAG_ANONYMOUS = 16;
    public static final int FLAG_TEMPLATE = 32;

    public int getClassFlags() {
        int flags = 0;
        if(isAbstract)
            flags |= FLAG_ABSTRACT;
        if(struct)
            flags |= FLAG_STRUCT;
        if(searchable)
            flags |= FLAG_SEARCHABLE;
        if(ephemeral)
            flags |= FLAG_EPHEMERAL;
        if(anonymous)
            flags |= FLAG_ANONYMOUS;
        if(templateFlag)
            flags |= FLAG_TEMPLATE;
        return flags;
    }

    private void setClassFlags(int flags) {
        isAbstract = (flags & FLAG_ABSTRACT) != 0;
        struct = (flags & FLAG_STRUCT) != 0;
        searchable = (flags & FLAG_SEARCHABLE) != 0;
        ephemeral = (flags & FLAG_EPHEMERAL) != 0;
        anonymous = (flags & FLAG_ANONYMOUS) != 0;
        templateFlag = (flags & FLAG_TEMPLATE) != 0;
    }

    public void write(KlassOutput output) {
        output.writeEntityId(this);
        constantPool.write(output);
        output.write(kind.code());
        output.writeUTF(name);
        output.writeUTF(qualifiedName != null ? qualifiedName : "");
        output.writeInt(getClassFlags());
        output.write(source.code());
        output.writeLong(tag);
        output.writeInt(sourceTag != null ? sourceTag : -1);
        output.writeInt(since);
        output.writeInt(typeParameters.size());
        typeParameters.forEach(tp -> tp.write(output));
        output.writeShort(Objects.requireNonNullElse(superTypeIndex, -1));
        output.writeInt(interfaceIndexes.size());
        interfaceIndexes.forEach(idx -> output.writeShort(idx.intValue()));
        output.writeInt(fields.size());
        fields.forEach(f -> f.write(output));
        output.writeInt(staticFields.size());
        staticFields.forEach(f -> f.write(output));
        output.writeInt(methods.size());
        methods.forEach(m -> m.write(output));
        output.writeInt(constraints.size());
        constraints.forEach(c -> c.write(output));
        output.writeInt(klasses.size());
        klasses.forEach(c -> c.write(output));
        output.writeInt(enumConstantDefs.size());
        enumConstantDefs.forEach(ed -> ed.write(output));
        writeAttributes(output);
    }

    public void read(KlassInput input) {
        constantPool.read(input);
        kind = ClassKind.fromCode(input.read());
        name = input.readUTF();
        qualifiedName = input.readUTF();
        if(qualifiedName.isEmpty())
            qualifiedName = null;
        setClassFlags(input.readInt());
        source = ClassSource.fromCode(input.read());
        tag = input.readLong();
        sourceTag = input.readInt();
        if(sourceTag == -1)
            sourceTag = null;
        since = input.readInt();
        var typeParameterCount = input.readInt();
        var typeParameters = new ArrayList<TypeVariable>();
        for (int i = 0; i < typeParameterCount; i++) {
            typeParameters.add(input.readTypeVariable());
        }
        setTypeParameters(typeParameters);
        this.superTypeIndex = input.readShort();
        if(superTypeIndex == 65535) {
            superTypeIndex = null;
            superKlass = null;
        } else
            superKlass = constantPool.getClassType(superTypeIndex).getKlass();
        int interfaceCount = input.readInt();
        interfaceIndexes.clear();
        interfaces.clear();
        for (int i = 0; i < interfaceCount; i++) {
            var idx = input.readShort();
            interfaceIndexes.add(idx);
            interfaces.add(constantPool.getClassType(idx).getKlass());
        }
        var fieldCount = input.readInt();
        fields.clear();
        for (int i = 0; i < fieldCount; i++) {
            var field = input.readField();
            fields.addChild(field);
        }
        var staticFieldCount = input.readInt();
        staticFields.clear();
        for (int i = 0; i < staticFieldCount; i++) {
            staticFields.addChild(input.readField());
        }
        var methodCount = input.readInt();
        methods.clear();
        for (int i = 0; i < methodCount; i++) {
            methods.addChild(input.readMethod());
        }
        int constraintCount = input.readInt();
        constraints.clear();
        for (int i = 0; i < constraintCount; i++) {
            constraints.addChild(input.readIndex());
        }
        int innerKlassCount = input.readInt();
        var klasses = new ArrayList<Klass>();
        for (int i = 0; i < innerKlassCount; i++) {
            klasses.add(input.readKlass());
        }
        setKlasses(klasses);
        int enumConstantCount = input.readInt();
        enumConstantDefs.clear();
        for (int i = 0; i < enumConstantCount; i++) {
            enumConstantDefs.addChild(input.readEnumConstantDef());
        }
        readAttributes(input);
    }

    public boolean isInner() {
        return declaringKlass != null;
    }

    @Nullable
    public Klass getDeclaringKlass() {
        return declaringKlass;
    }

    public void setDeclaringKlass(@Nullable Klass declaringKlass) {
        this.declaringKlass = declaringKlass;
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int addConstant(Object value) {
        return constantPool.addValue(value);
    }

    public ConstantPool getExistingTypeMetadata(List<? extends Type> typeArguments) {
        if (NncUtils.map(getAllTypeParameters(), TypeVariable::getType).equals(typeArguments))
            return constantPool;
        return (ConstantPool) ParameterizedStore.get(this, typeArguments);
    }

    private ConstantPool createTypeMetadata(List<? extends Type> typeArguments) {
        return new ConstantPool(typeArguments);
    }

    public void addTypeMetadata(ConstantPool parameterized) {
        var existing = ParameterizedStore.get(this, parameterized.typeArguments.secretlyGetTable());
        if(existing != null)
            throw new IllegalStateException("Parameterized klass " + parameterized + " already exists. "
                    + "existing: " + System.identityHashCode(existing) + ", new: "+ System.identityHashCode(parameterized)
            );
        NncUtils.requireNull(ParameterizedStore.put(this, parameterized.typeArguments.secretlyGetTable(), parameterized),
                () -> "Parameterized klass " + parameterized + " already exists");
    }

    public boolean isConstantPoolParameterized() {
        return isTemplate || (declaringKlass != null && declaringKlass.isConstantPoolParameterized())
                || (enclosingFlow != null && enclosingFlow.isConstantPoolParameterized());
    }

    public TypeMetadata getTypeMetadata(List<Type> typeArguments) {
        if (!isConstantPoolParameterized()) {
            if (typeArguments.isEmpty())
                return constantPool;
            else
                throw new InternalException(this + " is not a template class");
        }
//        typeArguments.forEach(Type::getTypeDesc);
        var typeMetadata = getExistingTypeMetadata(typeArguments);
        if (typeMetadata == constantPool)
            return constantPool;
        if(typeMetadata == null) {
            typeMetadata = createTypeMetadata(typeArguments);
            addTypeMetadata(typeMetadata);
        }
        else if (typeMetadata.getStage().isAfterOrAt(stage))
            return typeMetadata;
        var subst = new SubstitutorV2(
                constantPool, getAllTypeParameters(), typeArguments, typeMetadata, stage);
        typeMetadata = (ConstantPool) constantPool.accept(subst);
        return typeMetadata;
    }

    public List<TypeVariable> getAllTypeParameters() {
        var typeParams = new ArrayList<TypeVariable>();
        foreachGenericDeclaration(d -> typeParams.addAll(d.getTypeParameters()));
        return typeParams;
    }

    public void foreachGenericDeclaration(Consumer<GenericDeclaration> action) {
        if(declaringKlass != null)
            declaringKlass.foreachGenericDeclaration(action);
        else if(enclosingFlow != null)
            enclosingFlow.foreachGenericDeclaration(action);
        action.accept(this);
    }

    public @Nullable GenericDeclaration getOwner() {
        if (declaringKlass != null)
            return declaringKlass;
        if (enclosingFlow != null)
            return enclosingFlow;
        return null;
    }

    public void disableMethodTableBuild() {
        methodTableBuildDisabled = true;
    }

}

