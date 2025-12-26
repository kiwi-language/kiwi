package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.JsonIgnore;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.Var;
import org.metavm.flow.Error;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;
import org.metavm.wire.*;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.metavm.util.Utils.*;

@Wire(26)
@org.metavm.api.Entity
@Slf4j
public class Klass extends TypeDef implements GenericDeclaration, StagedEntity, GlobalKey, LoadAware, LocalKey, Message, ConstantScope, KlassDeclaration {

    public static final IndexDef<Klass> IDX_ALL_FLAG = IndexDef.create(Klass.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlag)));
    public static final IndexDef<Klass> IDX_NAME = IndexDef.create(Klass.class,
            1, klass -> List.of(Instances.stringInstance(klass.name)));

    public static final IndexDef<Klass> UNIQUE_QUALIFIED_NAME = IndexDef.createUnique(Klass.class,
            1, klass -> List.of(klass.qualifiedName != null ? Instances.stringInstance(klass.qualifiedName) : Instances.nullInstance())
            );

    public static final IndexDef<Klass> UNIQUE_SOURCE_TAG = IndexDef.createUnique(Klass.class,
            1, klass -> List.of(klass.sourceTag != null ? Instances.intInstance(klass.sourceTag) : Instances.nullInstance())
            );

    private @Nullable Integer sourceTag;
    @Setter
    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String qualifiedName;
    @Setter
    @Getter
    private ClassKind kind;
    @Setter
    @Getter
    private boolean anonymous;
    @Setter
    private boolean ephemeral;
    @Getter
    @Setter
    private boolean searchable;

    @Nullable
    private ClassType superType;
    private List<ClassType> interfaces = new ArrayList<>();

    @Nullable
    private Integer superTypeIndex;
    private List<Integer> interfaceIndexes = new ArrayList<>();
//    private transient List<Klass> interfaces = new ArrayList<>();
//    private transient @Nullable Klass superKlass;
    @Getter
    @Setter
    private ClassSource source;
    @Nullable
    private String desc;

    @Setter
    @Getter
    private int nextFieldTag;

    @Setter
    @Getter
    private int nextFieldSourceCodeTag = 1000000;

    private final List<Field> fields = new ArrayList<>();
    @Nullable
    private Reference titleField;
    private final List<Method> methods = new ArrayList<>();
    @Parent
    private @Nullable KlassDeclaration scope;
    private final List<Klass> klasses = new ArrayList<>();

    private final List<Field> staticFields = new ArrayList<>();
    private final List<Index> indices = new ArrayList<>();
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isAbstract;
    private boolean isTemplate;
    // Don't remove, used for search
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean isParameterized;
    private final List<TypeVariable> typeParameters = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();
    @Setter
    @Getter
    private boolean error;

    // For unit test. Do not remove
    @SuppressWarnings("unused")
    @Nullable
    private KlassFlags flags;

    @Setter
    @Getter
    private ClassTypeState state = ClassTypeState.INIT;
    @Setter
    @Getter
    private long tag;

    @Setter
    @Getter
    private int since;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private boolean templateFlag = false;

    @Setter
    @Getter
    private boolean struct;

    @SuppressWarnings({"FieldMayBeFinal", "unused"}) // for unit test
    private boolean dummyFlag = false;

    private boolean methodTableBuildDisabled;

    @Getter
    private final ConstantPool constantPool = new ConstantPool(this);

    private final boolean allFlag = true;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    @CopyIgnore
    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    @Getter
    private transient int level;

    private transient Map<Long,Integer> tag2level = new HashMap<>();

    private transient List<Klass> extensions = new ArrayList<>();

    private transient List<Klass> implementations = new ArrayList<>();

    private transient List<Klass> sortedKlasses = new ArrayList<>();

    private transient List<Field> sortedFields = new ArrayList<>();

    private transient int fieldCount;

    @CopyIgnore
    private transient volatile Closure closure;

    @Setter
    private transient ClassType type;

    private transient boolean frozen;

    private transient Klass arrayKlass;

    private transient Klass componentKlass;

    public Klass(
            @NotNull Id id,
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
            @Nullable KlassDeclaration scope,
            List<TypeVariable> typeParameters,
            long tag,
            @Nullable Integer sourceTag,
            int since,
            boolean methodTableBuildDisabled) {
        super(id);
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
        this.scope = scope;
        this.fieldCount = superType != null ? superType.getKlass().getFieldCount() : 0;
        this.methodTableBuildDisabled = methodTableBuildDisabled;
        setSuperType(superType, true);
        setInterfaces(interfaces, true);
        resetRank();
        if (scope != null)
            scope.addKlass(this);
        closure = new Closure(this);
        resetRank();
        if (superType != null)
            superType.getKlass().addExtension(this);
        interfaces.forEach(it -> it.getKlass().addImplementation(this));
        setTypeParameters(typeParameters);
//        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        resetSortedClasses();
        Utils.require(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void resetSortedClasses() {
        sortedKlasses = new ArrayList<>();
        forEachSuperClass(sortedKlasses::add);
        level = sortedKlasses.size() - 1;
        sortedKlasses.sort(Comparator.comparingInt(Klass::getLevel));
        tag2level = new HashMap<>();
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

    @JsonIgnore
    public List<Field> getReadyFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @JsonIgnore
    public List<Field> getAllFields() {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return Utils.union(superKlass.getAllFields(), fields);
        else
            return Collections.unmodifiableList(fields);
    }

    @JsonIgnore
    @Override
    public boolean isValidGlobalKey() {
        return isBuiltin();
    }

    @JsonIgnore
    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getQualifiedName();
    }

    public void forEachField(Consumer<Field> action) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachField(action);
        for (Field field : fields) {
            if(!field.isMetadataRemoved())
                action.accept(field);
        }
    }

    @Nullable
    public Integer getSourceTag() {
        return sourceTag;
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().getUnderlyingType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING,
                    titleField.getQualifiedName(), titleField.getType().getTypeDesc());
        this.titleField = Utils.safeCall(titleField, Instance::getReference);
    }

    @JsonIgnore
    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    //<editor-fold desc="hierarchy">

    @JsonIgnore
    public List<Klass> getAncestorClasses() {
        List<Klass> result = new ArrayList<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitKlass(Klass klass) {
                var s = klass.getSuperKlass();
                if (s != null)
                    s.accept(this);
                result.add(klass);
                return super.visitKlass(klass);
            }
        });
        return result;
    }

    @org.metavm.api.JsonIgnore
    public Closure getClosure() {
        if(closure == null) {
            synchronized (this) {
                if(closure == null)
                    closure = new Closure(this);
            }
        }
        return closure;
    }

    @JsonIgnore
    public List<Klass> getSubKlasses() {
        return Utils.merge(extensions, implementations);
    }

    @JsonIgnore
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
        var superKlass = getSuperKlass();
        int r = superKlass != null ? superKlass.getRank() : 0;
        for (var it : getInterfaceKlasses()) {
            var itRank = it.getRank();
            if (itRank > r)
                r = itRank;
        }
        rank = r + 1;
    }

    @JsonIgnore
    public int getRank() {
        return rank;
    }

    public List<Error> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void clearElementErrors(Element element) {
        errors.removeIf(e -> e.getElement() == element);
    }

    public void addError(Element element, ErrorLevel level, String message) {
        errors.add(new Error(element, level, message));
    }

    //</editor-fold>

    //<editor-fold desc="method">

    @JsonIgnore
    public Method getDefaultConstructor() {
        return getMethodByNameAndParamTypes(Types.getConstructorName(this), List.of());
    }

    public void sortFields(Comparator<Field> comparator) {
        this.fields.sort(comparator);
        this.staticFields.sort(comparator);
    }

    @JsonIgnore
    public List<Klass> getSortedKlasses() {
        return sortedKlasses;
    }

    @JsonIgnore
    public List<Field> getSortedFields() {
        return Objects.requireNonNull(sortedFields, () -> "Klass '" + getTypeDesc() + "' has not been initialized");
    }

    void resetFieldOffsets() {
        forEachExtension(Klass::resetSelfFieldOffset);
    }

    @JsonIgnore
    public int getFieldCount() {
        return fieldCount;
    }

    private void resetSelfFieldOffset() {
        var superKlass = getSuperKlass();
        var offset = superKlass != null ? superKlass.getFieldCount() : 0;
        for (Field f : getSortedFields()) {
            f.setOffset(offset++);
        }
        fieldCount = offset;
    }

    void resetFieldTransients() {
        resetSortedFields();
        resetFieldOffsets();
    }

    private void resetSortedFields() {
        sortedFields = new ArrayList<>();
        sortedFields.addAll(this.fields);
        sortedFields.sort(Comparator.comparingInt(Field::getTag));
    }

    public MethodTable getMethodTable() {
        if (methodTable == null) {
            synchronized (this) {
                if (methodTable == null)
                    methodTable = new MethodTable(this);
            }
        }
        return methodTable;
    }

    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @JsonIgnore
    public @Nullable Method getWriteObjectMethod() {
        return getMethodTable().getWriteObjectMethod();
    }

    @JsonIgnore
    public @Nullable Method getReadObjectMethod() {
        return getMethodTable().getReadObjectMethod();
    }

    public Method getMethodByInternalName(String internalName) {
        return Utils.findRequired(methods, m -> m.getInternalName(null).equals(internalName),
                () -> "Failed to find method with internal name '" + internalName + "' in class " + getTypeDesc());
    }

    @JsonIgnore
    public List<Method> getDeclaredMethods() {
        return methods;
    }

    public Method getMethod(long id) {
        return Utils.findRequired(methods, m -> Objects.equals(m.tryGetTreeId(), id));
    }

    public Method tryGetMethod(String name, List<Type> parameterTypes) {
        var method = Utils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getMethod(name, parameterTypes);
        return null;
    }

    public Method getMethod(String name, List<Type> parameterTypes) {
        return Objects.requireNonNull(
                tryGetMethod(name, parameterTypes),
                () -> "Can not find method '" + name + "(" +
                        Utils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'"
        );
    }

    public @Nullable Method findMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var method = Utils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            var m = superKlass.findMethodByNameAndParamTypes(name, parameterTypes);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : getInterfaceKlasses()) {
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
                    name, Utils.join(parameterTypes, Type::getTypeDesc, ","), getTypeDesc()));
        }
        return found;
    }

    public Method getMethodByName(String name) {
        return Objects.requireNonNull(findMethodByName(name), () -> "Can not find method with name '" + name + "' in class '" + this.name + "'");
    }

    public @Nullable Method findMethodByName(String name) {
        return findMethod(Method::getName, name);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return Utils.find(methods, predicate);
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
        throw new NullPointerException(messageSupplier.get());
    }

    public @Nullable Method findMethod(Predicate<Method> predicate) {
        var found = Utils.find(methods, predicate);
        if (found != null)
            return found;
        var superKlass = getSuperKlass();
        if (superKlass != null && (found = superKlass.findMethod(predicate)) != null)
            return found;
        for (var it : getInterfaceKlasses()) {
            if ((found = it.findMethod(predicate)) != null)
                return found;
        }
        return null;
    }

    public <T> @Nullable Method findMethod(IndexMapper<Method, T> property, T value) {
        var method = Utils.find(methods, m -> Objects.equals(property.apply(m), value));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            var m = superKlass.findMethod(property, value);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : getInterfaceKlasses()) {
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
        methods.add(method);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            getMethodTable().rebuild();
        method.setDeclaringType(this);
    }
    //</editor-fold>

    public void addInnerKlass(Klass klass) {
        klasses.add(klass);
        klass.scope = this;
    }

    public void removeInnerKlass(Klass klass) {
        if (!klasses.remove(klass))
            throw new RuntimeException("Klass " + klass.getName() + " is not found in " + this.getName());
    }

    public List<Klass> getKlasses() {
        return Collections.unmodifiableList(klasses);
    }

    @Override
    public void addKlass(Klass klass) {
        addInnerKlass(klass);
    }

    @Override
    public void removeKlass(Klass klass) {
        removeInnerKlass(klass);
    }

    public void setKlasses(List<Klass> klasses) {
        klasses.forEach(k -> k.scope = this);
        this.klasses.clear();
        this.klasses.addAll(klasses);
    }

    @JsonIgnore
    public List<Field> getDeclaredFields() {
        return fields;
    }

    @JsonIgnore
    public List<Index> getDeclaredConstraints() {
        return indices;
    }

    public void addField(Field field) {
        if (fields.contains(field))
            throw new RuntimeException("Field " + field.tryGetId() + " is already added");
        if (findSelfInstanceField(f -> f.getName().equals(field.getName())) != null
                || findSelfStaticField(f -> f.getName().equals(field.getName())) != null)
            throw new BusinessException(ErrorCode.INVALID_FIELD, field.getName(), "Duplicate field name");
        if (field.isStatic())
            staticFields.add(field);
        else
            fields.add(field);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
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
//        if(!Constants.maintenanceDisabled) {
//            closure = new Closure(this);
//            sortedFields = new ArrayList<>();
//            resetSortedFields();
//            sortedKlasses = new ArrayList<>();
//            tag2level = new HashMap<>();
//            resetSortedClasses();
//            if (superKlass != null)
//                superKlass.addExtension(this);
//            interfaces.forEach(it -> it.addImplementation(this));
//            resetSelfFieldOffset();
//            resetRank();
//        }
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

    public void addConstraint(Index constraint) {
        indices.add(constraint);
        constraint.setDeclaringType(this);
    }

    public void removeConstraint(Index constraint) {
        indices.remove(constraint);
    }

    @JsonIgnore
    public boolean isEnum() {
        return kind == ClassKind.ENUM;
    }

    @JsonIgnore
    public boolean isInterface() {
        return kind == ClassKind.INTERFACE;
    }

    @JsonIgnore
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
    public boolean isValueKlass() {
        return this.kind == ClassKind.VALUE;
    }

    public Field getField(Id id) {
        var field = Utils.find(fields, f -> f.idEquals(id));
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getField(id);
        throw new NullPointerException("Can not find field for " + id + " in type " + name);
    }

    public Method getMethod(Id id) {
        return Utils.findRequired(methods, m -> m.idEquals(id));
    }

    public @Nullable Field findSelfField(Predicate<Field> predicate) {
        var field = findSelfInstanceField(predicate);
        return field != null ? field : findSelfStaticField(predicate);
    }

    public @Nullable Field findSelfInstanceField(Predicate<Field> predicate) {
        return Utils.find(fields, predicate);
    }

    public Klass getInnerKlass(Predicate<Klass> filter) {
        return Objects.requireNonNull(findInnerKlass(filter));
    }

    public @Nullable Klass findInnerKlass(Predicate<Klass> filter) {
        return Utils.find(klasses, filter);
    }

    public Field getSelfField(Predicate<Field> predicate) {
        return Objects.requireNonNull(findSelfInstanceField(predicate));
    }

    public @Nullable Field findSelfStaticField(Predicate<Field> predicate) {
        return Utils.find(staticFields, predicate);
    }

    public @Nullable Field findField(Predicate<Field> predicate) {
        var field = findSelfInstanceField(predicate);
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.findField(predicate);
        return null;
    }

    public Field getField(Predicate<Field> predicate) {
        var found = findField(predicate);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose)
            forEachField(f -> log.info(f.getQualifiedName()));
        throw new NullPointerException("Fail to find field satisfying the specified criteria in klass: " + this);
    }

    public @Nullable Field findStaticField(Predicate<Field> predicate) {
        var field = Utils.find(staticFields, predicate);
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.findStaticField(predicate);
        return null;
    }

    public Field getFieldByName(String fieldName) {
        return Objects.requireNonNull(findFieldByName(fieldName), () -> "Cannot find field '" + fieldName + "' in class '" + getName() + "'");
    }

    public Field getStaticFieldByName(String fieldName) {
        return Objects.requireNonNull(findStaticFieldByName(fieldName),
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
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            var p = superKlass.findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        for (var it : getInterfaceKlasses()) {
            var p = it.findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        return null;
    }

    private Property findSelfStaticProperty(Predicate<Property> predicate) {
        var field = Utils.find(staticFields, predicate);
        if (field != null)
            return field;
        return Utils.find(methods, predicate);
    }

    @Nullable
    public Field findFieldByName(String name) {
        var field = Utils.find(fields, f -> f.getName().equals(name));
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
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
        return Objects.requireNonNull(getProperty(Property::tryGetId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getPropertyByName(String name) {
        return getProperty(Property::getName, name);
    }

    public Property findProperty(Predicate<Property> filter) {
        var field = Utils.find(fields, filter);
        if(field != null)
            return field;
        var method = Utils.find(methods, m -> !m.isStatic() && filter.test(m));
        if(method != null)
            return method;
        var superKlass = getSuperKlass();
        if(superKlass != null)
            return superKlass.findProperty(filter);
        return null;
    }

    private <T> Property getProperty(IndexMapper<Property, T> property, T value) {
        var field = Utils.find(fields, f -> Objects.equals(property.apply(f), value));
        if (field != null)
            return field;
        var method = Utils.find(methods, m -> Objects.equals(property.apply(m), value));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getProperty(property, value);
        return null;
    }

    @JsonIgnore
    public List<Property> getProperties() {
        return Utils.concatList(fields, methods);
    }

    public @Nullable Field findSelfFieldByName(String name) {
        var field = findSelfInstanceFieldByName(name);
        return field != null ? field : findSelfStaticFieldByName(name);
    }

    public Field getSelfFieldByName(String name) {
        return Objects.requireNonNull(findSelfFieldByName(name),
                () -> "Cannot find field '" + name + "' in klass '" + this.name + "'");
    }

    public Field getSelfInstanceFieldByName(String name) {
        return Objects.requireNonNull(findSelfInstanceFieldByName(name),
                () -> "Cannot find field \"" + name + "\" in klass " + getTypeDesc());
    }

    @Nullable
    public Field findSelfInstanceFieldByName(String name) {
        return Utils.find(fields, f -> f.getName().equals(name));
    }

    public Field getSelfStaticFieldByName(String name) {
        return Objects.requireNonNull(findSelfStaticFieldByName(name),
                () -> "Cannot find static field '" + name + "' in klass " + qualifiedName);
    }

    @Nullable
    public Field findSelfStaticFieldByName(String name) {
        return Utils.find(staticFields, f -> f.getName().equals(name));
    }

    @Nullable
    public Field findStaticFieldByName(String name) {
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            Field superField = superKlass.findStaticFieldByName(name);
            if (superField != null)
                return superField;
        }
        return Utils.find(staticFields, f -> f.getName().equals(name));
    }

    @Nullable
    public Index findSelfIndex(Predicate<Index> predicate) {
        for (Constraint constraint : indices) {
            if (constraint instanceof Index index && predicate.test(index))
                return index;
        }
        return null;
    }

    public boolean isBuiltin() {
        return source == ClassSource.BUILTIN;
    }

    public boolean checkColumnAvailable(Column column) {
        return column == Column.NIL || Utils.find(fields, f -> f.getColumn() == column) == null;
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


    private List<Field> getFieldsInHierarchy() {
        return fields;
    }

    public Field getFieldNyName(String fieldName) {
        return Objects.requireNonNull(
                findFieldByName(fieldName), "Cannot find field '" + fieldName + "' in klass " + qualifiedName
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
        var thatSuperKlass = that.getSuperKlass();
        if (thatSuperKlass != null && isAssignableFrom(thatSuperKlass))
            return true;
        if (isInterface()) {
            for (var it : that.getInterfaceKlasses()) {
                if (isAssignableFrom(it)) {
                    return true;
                }
            }
        }
        return false;
    }

    public @NotNull ClassType getType() {
        if (type == null) {
            var owner = switch (scope) {
                case Klass k -> k.getType();
                case Flow flow -> flow.getRef();
                case null -> null;
                default -> throw new IllegalStateException("Unrecognized klass declaration: " + scope.getClass().getName());
            };
            type = new KlassType(owner, this, List.of());
        }
        return type;
    }

    @Override
    public String getTitle() {
        return name;
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
    @JsonIgnore
    public Klass getSuperKlass() {
        var superType = getSuperType();
        return superType != null ? superType.getKlass() : null;
    }

    @JsonIgnore
    public List<Klass> getInterfaceKlasses() {
        return Utils.map(getInterfaces(), ClassType::getKlass);
    }

    public List<ClassType> getInterfaces() {
        return Utils.map(interfaceIndexes, constantPool::getClassType);
    }

    public List<Integer> getInterfaceIndexes() {
        return Collections.unmodifiableList(interfaceIndexes);
    }

    public void forEachSuper(Consumer<Klass> action) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            action.accept(superKlass);
        Objects.requireNonNull(getInterfaceKlasses(), () -> "Klass " + qualifiedName + " is yet initialized")
                .forEach(action);
    }

    public void forEachSuperType(Consumer<ClassType> action, TypeMetadata typeMetadata) {
        if(superTypeIndex != null)
            action.accept((ClassType) typeMetadata.getType(superTypeIndex));
        interfaceIndexes.forEach(i -> action.accept((ClassType) typeMetadata.getType(i)));
    }

    public void forEachSuperClass(Consumer<Klass> action) {
        action.accept(this);
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachSuperClass(action);
    }

    public void forEachSuperClassTopDown(Consumer<Klass> action) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachSuperClassTopDown(action);
        action.accept(this);
    }

    public <T extends Constraint> List<T> getAllConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                indices,
                constraintType::isInstance,
                constraintType::cast
        );
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            result = Utils.union(
                    superKlass.getAllConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public <T extends Constraint> T getConstraint(Class<T> constraintType, Id id) {
        return find(getAllConstraints(constraintType), c -> c.getId().equals(id));
    }

    @SuppressWarnings("unused")
    public Constraint getConstraint(Id id) {
        return Utils.find(Objects.requireNonNull(indices), c -> c.idEquals(id));
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
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachMethod(action);
        getInterfaceKlasses().forEach(it -> it.forEachMethod(action));
    }

    @Nullable
    public MethodRef findOverride(Method method, TypeMetadata typeMetadata) {
        return getMethodTable().findOverride(method, typeMetadata);
    }

    public List<TypeVariable> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    public TypeVariable getTypeParameterByName(String name) {
        return Utils.findRequired(typeParameters, tp -> tp.getName().equals(name),
                () -> "Cannot find type parameter with name '" + name + "' in class '" + this + "'");
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.add(typeParameter);
    }

    @Override
    public String getName() {
        return name;
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

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return (org.metavm.entity.Entity) scope;
    }

    public boolean isEphemeralKlass() {
        return ephemeral;
    }

    @JsonIgnore
    public List<Type> getDefaultTypeArguments() {
        return Utils.map(typeParameters, TypeVariable::getType);
    }

    public List<Index> getIndices() {
        return Collections.unmodifiableList(indices);
    }

    @JsonIgnore
    public List<Index> getAllIndices() {
        return getAllConstraints(Index.class);
    }

    @Override
    public String toString() {
        return "Klass " + getTypeDesc();
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

    @JsonIgnore
    public boolean isInitialized() {
        return sortedFields != null;
    }

    private void onAncestorChanged() {
        forEachSubclass(Klass::onAncestorChangedSelf);
    }

    public void setSuperType(@Nullable ClassType superType) {
        setSuperType(superType, Constants.maintenanceDisabled);
    }

    public void setSuperTypeIndex(@Nullable Integer superTypeIndex) {
        this.superTypeIndex = superTypeIndex;
    }

    public void setSuperType(@Nullable ClassType superType, boolean skipMaintenance) {
        if(skipMaintenance) {
            if (superType != null) {
                this.superTypeIndex = constantPool.addValue(superType);
                this.superType = superType;
            }
            else {
                superTypeIndex = null;
                this.superType = null;
            }
        }
        else {
            var superKlass = getSuperKlass();
            if (superKlass != null) {
                superKlass.removeExtension(this);
            }
            if (superType != null) {
                this.superTypeIndex = constantPool.addValue(superType);
                superKlass = superType.getKlass();
                this.superType = superType;
                superKlass.addExtension(this);
            } else {
                this.superType = null;
                this.superTypeIndex = null;
            }
            onSuperTypesChanged();
        }
    }

    public void setInterfaces(List<ClassType> interfaces) {
        setInterfaces(interfaces, Constants.maintenanceDisabled);
    }

    public void setInterfaceIndexes(List<Integer> interfaceIndexes) {
        this.interfaceIndexes = new ArrayList<>(interfaceIndexes);
        this.interfaces = Utils.map(interfaceIndexes, constantPool::getClassType);
    }

    public void setInterfaces(List<ClassType> interfaces, boolean skipMaintenance) {
        if(skipMaintenance) {
            Objects.requireNonNull(this.getInterfaceKlasses(), () -> "Klass " + qualifiedName + " is not yet initialized").clear();
            this.interfaces.clear();
            this.interfaces.addAll(interfaces);
            this.interfaceIndexes.clear();
            this.interfaceIndexes.addAll(Utils.map(interfaces, constantPool::addValue));
        }
        else {
            for (var anInterface : this.getInterfaceKlasses()) {
                anInterface.removeImplementation(this);
            }
            this.interfaces.clear();
            this.interfaceIndexes.clear();
            for (var anInterface : interfaces) {
                this.interfaces.add(anInterface);
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
        this.typeParameters.clear();
        this.typeParameters.addAll(typeParameters);
    }

    public void setFields(List<Field> fields) {
        fields.forEach(f -> f.setDeclaringType(this));
        this.fields.clear();
        this.fields.addAll(fields);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            resetFieldTransients();
    }

    public boolean isLocal() {
        return scope instanceof Flow;
    }

    public void setStaticFields(List<Field> staticFields) {
        staticFields.forEach(f -> f.setDeclaringType(this));
        this.staticFields.clear();
        this.staticFields.addAll(staticFields);
    }

    public void setIndices(List<Index> indices) {
        indices.forEach(c -> c.setDeclaringType(this));
        this.indices.clear();
        this.indices.addAll(indices);
    }

    public void setMethods(List<Method> methods) {
        methods.forEach(m -> m.setDeclaringType(this));
        this.methods.clear();
        this.methods.addAll(methods);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            rebuildMethodTable();
    }

    @Override
    public String getTypeDesc() {
        if(scope != null)
            return scope.getTypeDesc() + "." + name;
        else
            return Objects.requireNonNullElse(qualifiedName, name);
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

    @JsonIgnore
    public @Nullable Field getSelfTitleField() {
        return Utils.safeCall(titleField, r -> (Field) r.get());
    }

    @Nullable
    public Field getTitleField() {
        if (titleField != null)
            return getSelfTitleField();
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getTitleField();
        return null;
    }

    public List<Field> getStaticFields() {
        return Collections.unmodifiableList(staticFields);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return getQualifiedName();
    }

    @Override
    public GenericDeclarationRef getRef() {
        return getType();
    }

    @JsonIgnore
    public boolean isSAMInterface() {
        return isInterface() && Utils.count(methods, m -> m.isAbstract() && !m.isStatic()) == 1;
    }

    @JsonIgnore
    public Method getSingleAbstractMethod() {
        if (!isSAMInterface())
            throw new InternalException("Type " + getName() + " is not a SAM interface");
        return getMethod(m -> m.isAbstract() && !m.isStatic());
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

    // This method is used to correct corrupted data
    public void ensureValidFieldTags() {
        var maxFieldTag = 0;
        var maxSourceFieldTag = 0;
        for (Field field : fields) {
            maxFieldTag = Math.max(maxFieldTag, field.getTag() + 1);
            if (field.getSourceTag() != null)
                maxSourceFieldTag = Math.max(maxSourceFieldTag, field.getSourceTag() + 1);
        }
        nextFieldTag = Math.max(nextFieldTag, maxFieldTag);
        nextFieldSourceCodeTag = Math.max(nextFieldSourceCodeTag, maxSourceFieldTag);
    }

    public List<Field> getEnumConstants() {
        return Utils.filter(staticFields, Field::isEnumConstant);
    }

    public Field getEnumConstantByName(String name) {
        return Utils.findRequired(staticFields, f -> f.isEnumConstant() && f.getName().equals(name),
                () -> "Cannot find enum enum constant with name '" + name + "' in class '" + getQualifiedName() + "'");
    }

    public void clearEnumConstantDefs() {
        staticFields.forEach(f -> {
            f.setEnumConstant(false);
            f.setOrdinal(-1);
        });
    }

    public int nextFieldSourceCodeTag() {
        return nextFieldSourceCodeTag++;
    }

    @Nullable
    public KlassFlags getFlags() {
        return flags;
    }

    public int getKlassFlags() {
        // TODO to implement
        return 0;
    }

    public boolean isFlag1() {
        return flags != null && flags.isFlag1();
    }

    @JsonIgnore
    public boolean isBeanClass() {
        return getAttribute(AttributeNames.BEAN_KIND) != null;
    }

    @JsonIgnore
    public @Nullable Method getHashCodeMethod() {
        return methodTable.getHashCodeMethod();
    }

    @JsonIgnore
    public @Nullable Method getToStringMethod() {
        return methodTable.getToStringMethod();
    }

    @JsonIgnore
    public @Nullable Method getEqualsMethod() {
        return methodTable.getEqualsMethod();
    }

    @JsonIgnore
    public Klass getArrayKlass() {
       if(arrayKlass == null) {
           arrayKlass = KlassBuilder.newBuilder(new NullId(), name + "[]", Utils.safeCall(qualifiedName, c -> c + "[]")).build();
           arrayKlass.setEphemeral();
           arrayKlass.componentKlass = this;
       }
       return arrayKlass;
    }

    @JsonIgnore
    public Klass getComponentKlass() {
        return componentKlass;
    }

    public boolean isOverrideOf(Method override, Method overridden) {
        return methodTable.findOverride(overridden, constantPool).getRawFlow() == override;
    }

    public void emitCode() {
        accept(new MaxesComputer());
        methods.forEach(Flow::emitCode);
        klasses.forEach(Klass::emitCode);
    }

    public void setSourceTag(@Nullable Integer sourceTag) {
        this.sourceTag = sourceTag;
    }

    public int getClassFlags() {
        int flags = 0;
        if(isAbstract)
            flags |= org.metavm.entity.KlassFlags.FLAG_ABSTRACT;
        if(struct)
            flags |= org.metavm.entity.KlassFlags.FLAG_STRUCT;
        if(searchable)
            flags |= org.metavm.entity.KlassFlags.FLAG_SEARCHABLE;
        if(ephemeral)
            flags |= org.metavm.entity.KlassFlags.FLAG_EPHEMERAL;
        if(anonymous)
            flags |= org.metavm.entity.KlassFlags.FLAG_ANONYMOUS;
        if(templateFlag)
            flags |= org.metavm.entity.KlassFlags.FLAG_TEMPLATE;
        return flags;
    }

    public void setClassFlags(int flags) {
        isAbstract = (flags & org.metavm.entity.KlassFlags.FLAG_ABSTRACT) != 0;
        struct = (flags & org.metavm.entity.KlassFlags.FLAG_STRUCT) != 0;
        searchable = (flags & org.metavm.entity.KlassFlags.FLAG_SEARCHABLE) != 0;
        ephemeral = (flags & org.metavm.entity.KlassFlags.FLAG_EPHEMERAL) != 0;
        anonymous = (flags & org.metavm.entity.KlassFlags.FLAG_ANONYMOUS) != 0;
        templateFlag = (flags & org.metavm.entity.KlassFlags.FLAG_TEMPLATE) != 0;
    }

    public boolean isInner() {
        return scope != null;
    }

    @Nullable
    public KlassDeclaration getScope() {
        return scope;
    }

    public void setScope(@Nullable KlassDeclaration scope) {
        this.scope = scope;
    }

    @JsonIgnore
    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    public int addConstant(Value value) {
        return constantPool.addValue(value);
    }

    public boolean isConstantPoolParameterized() {
        return isTemplate || (scope != null && scope.isConstantPoolParameterized());
    }

    @JsonIgnore
    public List<TypeVariable> getAllTypeParameters() {
        var typeParams = new ArrayList<TypeVariable>();
        foreachGenericDeclaration(d -> typeParams.addAll(d.getTypeParameters()));
        return typeParams;
    }

    public void foreachGenericDeclaration(Consumer<GenericDeclaration> action) {
        if(scope != null)
            scope.foreachGenericDeclaration(action);
        action.accept(this);
    }

    public void disableMethodTableBuild() {
        methodTableBuildDisabled = true;
    }

    /** @noinspection unused*/
    private void onRead() {
        stage = ResolutionStage.INIT;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitKlass(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        fields.forEach(arg -> arg.accept(visitor));
        methods.forEach(arg -> arg.accept(visitor));
        klasses.forEach(arg -> arg.accept(visitor));
        staticFields.forEach(arg -> arg.accept(visitor));
        indices.forEach(arg -> arg.accept(visitor));
        typeParameters.forEach(arg -> arg.accept(visitor));
        constantPool.accept(visitor);
    }

    public String getText() {
        var writer = new CodeWriter();
        writeCode(writer);
        return writer.toString();
    }

    public void writeCode(CodeWriter writer) {
        writer.write(kind.name().toLowerCase() + " " + name);
        var supType = getSuperType();
        if (supType != null)
            writer.write(" extends " + supType.getTypeDesc());
        if (!interfaces.isEmpty())
            writer.write(" implements " + Utils.join(interfaces, Type::getTypeDesc));
        writer.writeln(" {");
        writer.indent();
        for (Field field : fields) {
            field.writeCode(writer);
        }
        for (Field staticField : staticFields) {
            staticField.writeCode(writer);
        }
        for (Index index : indices) {
            index.writeCode(writer);
        }
        for (Method method : methods) {
            method.writeCode(writer);
        }
        for (Klass klass : klasses) {
            klass.writeCode(writer);
        }
        writer.unindent();
        writer.writeln("}");
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (superType != null) superType.forEachReference(action);
        for (var interfaces_ : interfaces) interfaces_.forEachReference(action);
        for (var fields_ : fields) action.accept(fields_.getReference());
        if (titleField != null) action.accept(titleField);
        for (var methods_ : methods) action.accept(methods_.getReference());
        for (var klasses_ : klasses) action.accept(klasses_.getReference());
        for (var staticFields_ : staticFields) action.accept(staticFields_.getReference());
        for (var indices_ : indices) action.accept(indices_.getReference());
        for (var typeParameters_ : typeParameters) action.accept(typeParameters_.getReference());
        for (var errors_ : errors) errors_.forEachReference(action);
        if (flags != null) action.accept(flags.getReference());
        constantPool.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
        for (var fields_ : fields) action.accept(fields_);
        for (var methods_ : methods) action.accept(methods_);
        for (var klasses_ : klasses) action.accept(klasses_);
        for (var staticFields_ : staticFields) action.accept(staticFields_);
        for (var indices_ : indices) action.accept(indices_);
        for (var typeParameters_ : typeParameters) action.accept(typeParameters_);
        if (flags != null) action.accept(flags);
    }

}

