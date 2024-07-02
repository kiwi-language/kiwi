package org.metavm.object.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.ListNative;
import org.metavm.entity.natives.NativeBase;
import org.metavm.expression.Var;
import org.metavm.flow.Error;
import org.metavm.flow.*;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.object.view.MappingSaver;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.metavm.util.NncUtils.*;

@EntityType
public class Klass extends TypeDef implements GenericDeclaration, ChangeAware, GenericElement, StagedEntity, GlobalKey, LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final Logger logger = LoggerFactory.getLogger(Klass.class);

    public static final IndexDef<Klass> IDX_NAME = IndexDef.create(Klass.class, "name");

    public static final IndexDef<Klass> UNIQUE_CODE = IndexDef.createUnique(Klass.class, "code");

    public static final IndexDef<Klass> TEMPLATE_IDX = IndexDef.create(Klass.class, "template");

    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String code;
    private final ClassKind kind;
    private boolean anonymous;
    private boolean ephemeral;
    @Nullable
    private ClassType superType;
    @ChildEntity
    private final ReadWriteArray<ClassType> interfaces = addChild(new ReadWriteArray<>(ClassType.class), "interfaces");
    private ClassSource source;
    @ChildEntity
    private final ReadWriteArray<Klass> subKlasses = addChild(new ReadWriteArray<>(Klass.class), "subKlasses");
    @Nullable
    private String desc;
    @ChildEntity
    private final ChildArray<Field> fields = addChild(new ChildArray<>(Field.class), "fields");
    @Nullable
    private Field titleField;
    @ChildEntity
    private final ChildArray<Method> methods = addChild(new ChildArray<>(Method.class), "methods");
    @ChildEntity
    private final ChildArray<Method> parameterizedMethods = addChild(new ChildArray<>(Method.class), "parameterizedMethods");

    @ChildEntity
    private final ChildArray<Field> staticFields = addChild(new ChildArray<>(Field.class), "staticFields");
    @ChildEntity
    private final ChildArray<Constraint> constraints = addChild(new ChildArray<>(Constraint.class), "constraints");
    @Nullable
    private Klass template;
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
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");
    @ChildEntity
    private final ChildArray<Error> errors = addChild(new ChildArray<>(Error.class), "errors");

    private boolean error;

    @ChildEntity
    private final ChildArray<ObjectMapping> mappings = addChild(new ChildArray<>(ObjectMapping.class), "mappings");

    @Nullable
    private ObjectMapping defaultMapping;

    @CopyIgnore
    private @Nullable Klass copySource;

    private int nextFieldTag;

    private ClassTypeState state = ClassTypeState.INIT;

    private final long tag;

    private transient final int typeTag;

    @SuppressWarnings("unused")
    private boolean templateFlag = false;

    private boolean struct;

    @SuppressWarnings("FieldMayBeFinal") // for unit test
    private boolean dummyFlag = false;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    private transient int level;

    private transient List<Klass> supers;

    private transient  List<Klass> loadedSubClasses;

    private transient List<Klass> sortedKlasses = new ArrayList<>();

    private transient List<Field> sortedFields = new ArrayList<>();

    private transient Closure closure;

    private transient ClassType type;

    @Nullable
    private transient List<Klass> supersCheckpoint;

    private transient List<Runnable> ancestorChangeListeners = new ArrayList<>();

    private transient ParameterizedElementMap<List<? extends Type>, Klass> parameterizedClasses;

    private transient Class<? extends NativeBase> nativeClass;

    public Klass(
            Long tmpId,
            String name,
            @Nullable String code,
            @Nullable ClassType superType,
            List<ClassType> interfaces,
            @NotNull ClassKind kind,
            ClassSource source,
            @Nullable Klass template,
            boolean anonymous,
            boolean ephemeral,
            boolean struct,
            @Nullable String desc,
            boolean isAbstract,
            boolean isTemplate,
            List<TypeVariable> typeParameters,
            List<Type> typeArguments,
            long tag) {
        this.name = name;
        this.code = code;
        this.kind = kind;
        setTmpId(tmpId);
        setSuperType(superType);
        setInterfaces(interfaces);
        this.isAbstract = isAbstract;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.struct = struct;
        this.template = copySource = template;
        this.source = source;
        this.desc = desc;
        this.tag = tag;
        this.typeTag = tag < 1000000 ? (int) tag : 0;
        setTypeParameters(typeParameters);
        setTypeArguments(typeArguments);
        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        resetSortedClasses();
        NncUtils.requireTrue(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    public void update(KlassDTO klassDTO) {
        this.name = klassDTO.name();
        setDesc(klassDTO.desc());
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void addSubType(@NotNull Klass subType) {
        if (subKlasses.contains(subType))
            throw new InternalException("Subtype '" + subType + "' is already added to this type");
        subKlasses.add(subType);
    }

    void resetSortedClasses() {
        sortedKlasses.clear();
        forEachSuperClass(sortedKlasses::add);
        sortedKlasses.sort(Comparator.comparingLong(Klass::getTag));
        level = sortedKlasses.size() - 1;
    }

    void removeSubType(Klass subType) {
        subKlasses.remove(subType);
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
        if (superType != null)
            return NncUtils.union(superType.resolve().getAllFields(), fields.toList());
        else
            return fields.toList();
    }

    @Override
    public boolean isValidGlobalKey() {
        return isBuiltin() || template != null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        if (template == null) {
//            java.lang.reflect.Type javaType = Objects.requireNonNull(
//                    context.getJavaType(this), () -> "Can not get java type for type '" + this + "'");
            return getCodeNotNull();
        } else {
            return context.getModelName(template, this) + "<"
                    + NncUtils.join(typeArguments, object -> context.getModelName(object, this))
                    + ">";
        }
    }

    public String getCodeNotNull() {
        return Objects.requireNonNull(code);
    }

    public void forEachField(Consumer<Field> action) {
        if (superType != null)
            superType.resolve().forEachField(action);
        for (Field field : fields) {
            action.accept(field);
        }
    }

    public boolean allFieldsMatch(Predicate<Field> predicate) {
        if (superType != null && !superType.resolve().allFieldsMatch(predicate))
            return false;
        return this.fields.stream().allMatch(predicate);
    }

    public long getTag() {
        return tag;
    }

    @NoProxy
    public int getTypeTag() {
        return typeTag;
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING);
        this.titleField = titleField;
    }

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    public boolean isLocalClass() {
        var parent = getParentEntity();
        return parent instanceof Method || parent instanceof NodeRT;
    }

    //<editor-fold desc="hierarchy">

    public List<Klass> getAncestorClasses() {
        List<Klass> result = new ArrayList<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitKlass(Klass klass) {
                if (klass.superType != null)
                    klass.superType.accept(this);
                result.add(klass);
                return super.visitKlass(klass);
            }
        });
        return result;
    }

    public Closure getClosure() {
        ensureListenersInitialized();
        if (closure == null)
            closure = createClosure();
        return closure;
    }

    private Closure createClosure() {
        return new Closure(this);
    }

    public List<Klass> getSubKlasses() {
        return subKlasses.toList();
    }

    public List<Klass> getDescendantTypes() {
        List<Klass> types = new ArrayList<>();
        visitDescendantTypes(types::add);
        return types;
    }

    public void visitDescendantTypes(Consumer<Klass> action) {
        action.accept(this);
        for (Klass subType : subKlasses) {
            subType.visitDescendantTypes(action);
        }
    }

    public List<ObjectMapping> getMappings() {
        return mappings.toList();
    }

    public ObjectMapping getMapping(Predicate<ObjectMapping> predicate) {
        return Objects.requireNonNull(findMapping(predicate),
                () -> "Can not find mapping with predicate in klass " + getTypeDesc());
    }

    public @Nullable ObjectMapping findMapping(Predicate<ObjectMapping> predicate) {
        var found = NncUtils.find(mappings, predicate);
        if (found != null)
            return found;
        if (superType != null && (found = superType.resolve().findMapping(predicate)) != null)
            return found;
        return null;
    }

    public @Nullable ObjectMapping getBuiltinMapping() {
        return NncUtils.find(mappings, ObjectMapping::isBuiltin);
    }

    public Set<Id> getSubTypeIds() {
        Set<Id> typeIds = new HashSet<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitKlass(Klass klass) {
                typeIds.add(klass.getId());
                for (Klass subType : klass.subKlasses)
                    subType.accept(this);
                return super.visitKlass(klass);
            }
        });
        return typeIds;
    }

    public int getRank() {
        if (rank == 0) {
            int r = superType != null ? superType.resolve().getRank() : 0;
            for (var it : interfaces) {
                var itRank = it.resolve().getRank();
                if (itRank > r)
                    r = itRank;
            }
            rank = r + 1;
        }
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
        return getMethodByCodeAndParamTypes(Types.getConstructorCode(this), List.of());
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

    void resetSortedFields() {
        this.sortedFields.clear();
        sortedFields.addAll(this.fields.toList());
        sortedFields.sort(Comparator.comparingLong(Field::getTag));
    }

    public void moveMethod(Method method, int index) {
        moveProperty(methods, method, index);
    }

    private MethodTable getMethodTable() {
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

    public List<Method> getAllMethods() {
        if (superType != null)
            return NncUtils.union(superType.resolve().getAllMethods(), NncUtils.listOf(methods));
        else
            return NncUtils.listOf(getMethods());
    }

    public Method getMethodByInternalName(String internalName) {
        return NncUtils.findRequired(methods, m -> m.getInternalName(null).equals(internalName));
    }

    public ReadonlyArray<Method> getDeclaredMethods() {
        return methods;
    }

    public Method getMethod(long id) {
        return methods.get(Entity::tryGetId, id);
    }

    public Method tryGetMethod(String name, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superType != null)
            return superType.resolve().getMethod(name, parameterTypes);
        return null;
    }

    public Method getMethod(String name, List<Type> parameterTypes) {
        return NncUtils.requireNonNull(
                tryGetMethod(name, parameterTypes),
                () -> new InternalException("Can not find method '" + name + "(" +
                        NncUtils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'")
        );
    }

    public @Nullable Method findMethodByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getCode(), code) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superType != null) {
            var m = superType.resolve().findMethodByCodeAndParamTypes(code, parameterTypes);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.resolve().findMethodByCodeAndParamTypes(code, parameterTypes);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public Method getMethodByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        return Objects.requireNonNull(
                findMethodByCodeAndParamTypes(code, parameterTypes),
                () -> String.format("Can not find method %s(%s) in klass %s",
                        code, NncUtils.join(parameterTypes, Type::getName, ","), getName())
        );
    }

    public Method findMethodBySignatureString(String signatureString) {
        return methods.get(Flow::getSignatureString, signatureString);
    }

    public Method getMethodByCode(String code) {
        return Objects.requireNonNull(findMethodByCode(code), () -> "Can not find method with code '" + code + "' in type '" + name + "'");
    }

    public @Nullable Method findMethodByCode(String code) {
        return findMethod(Method::getCode, code);
    }

    public @Nullable Method findSelfMethodByCode(String code) {
        return methods.get(Flow::getCode, code);
    }

    public @Nullable Method findMethodByVerticalTemplate(Method template) {
        return findMethod(Method::getVerticalTemplate, template);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return NncUtils.find(methods.toList(), predicate);
    }

    public Method getMethod(Predicate<Method> predicate) {
        var found = findMethod(predicate);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose) {
            logger.info("Fail to resolve method with predicate in klass " + getTypeDesc());
            forEachMethod(m -> logger.info(m.getQualifiedName()));
        }
        throw new NullPointerException("Can not find method with predicate in klass " + this);
    }

    public @Nullable Method findMethod(Predicate<Method> predicate) {
        var found = NncUtils.find(methods, predicate);
        if (found != null)
            return found;
        if (superType != null && (found = superType.resolve().findMethod(predicate)) != null)
            return found;
        for (ClassType it : interfaces) {
            if ((found = it.resolve().findMethod(predicate)) != null)
                return found;
        }
        return null;
    }

    public <T> @Nullable Method findMethod(IndexMapper<Method, T> property, T value) {
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superType != null) {
            var m = superType.resolve().findMethod(property, value);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.resolve().findMethod(property, value);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public void removeMethod(Method method) {
        methods.remove(method);
        getMethodTable().rebuild();
    }

    public void addMethod(Method method) {
        if (methods.contains(method))
            throw new InternalException("Method '" + method + "' is already added to the class type");
        methods.addChild(method);
        getMethodTable().rebuild();
    }
    //</editor-fold>

    public Set<TypeVariable> getVariables() {
        return NncUtils.flatMapUnique(typeArguments, Type::getVariables);
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
        if (tryGetFieldByName(field.getName()) != null || tryGetStaticFieldByName(field.getName()) != null)
            throw BusinessException.invalidField(field, "Field name '" + field.getName() + "' is already used in class " + getName());
        if (field.getCode() != null &&
                (findSelfFieldByCode(field.getCode()) != null || findSelfStaticFieldByCode(field.getCode()) != null))
            throw BusinessException.invalidField(field, "Field code " + field.getCode() + " is already used in class " + getName());
        if (field.isStatic())
            staticFields.addChild(field);
        else
            fields.addChild(field);
        resetSortedFields();
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
        sortedFields = new ArrayList<>();
        resetSortedFields();
        sortedKlasses = new ArrayList<>();
        resetSortedClasses();
        loadedSubClasses = new ArrayList<>();
//        if(superType != null) {
//            superType.resolve().addLoadedSubClass(this);
//        }
    }

    protected void addLoadedSubClass(Klass klass) {
        loadedSubClasses.add(klass);
    }

    public int getLevel() {
        return level;
    }

    public List<Index> getFieldIndices(Field field) {
        return NncUtils.filter(
                getConstraints(Index.class),
                index -> index.isFieldIndex(field)
        );
    }

    @Override
    @Nullable
    public Klass getCopySource() {
        return copySource;
    }

    public void setCopySource(@Nullable Object copySource) {
        this.copySource = (Klass) copySource;
    }

    public void addConstraint(Constraint constraint) {
        constraints.addChild(constraint);
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

    public void addMapping(ObjectMapping mapping) {
        this.mappings.addChild(mapping);
        if (defaultMapping == null)
            defaultMapping = mapping;
    }

    public Field findFieldById(Id fieldId) {
        Field found = NncUtils.find(fields, f -> f.idEquals(fieldId));
        if (found != null)
            return found;
        if (superType != null)
            return superType.resolve().findFieldById(fieldId);
        else
            return null;
    }

//    public Field getField(Id fieldId) {
//        return NncUtils.requireNonNull(findFieldById(fieldId),
//                () -> new InternalException(String.format("Field %d not found", fieldId)));
//    }

    public Field getField(Id id) {
        var field = fields.get(Entity::tryGetId, id);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().getField(id);
        throw new NullPointerException("Can not find field for " + id + " in type " + name);
    }

    public Method getMethod(Id id) {
        return Objects.requireNonNull(methods.get(Entity::tryGetId, id));
    }

    public @Nullable Field findSelfField(Predicate<Field> predicate) {
        return NncUtils.find(fields, predicate);
    }

    public @Nullable Field findField(Predicate<Field> predicate) {
        var field = findSelfField(predicate);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().findField(predicate);
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
        if (superType != null)
            return superType.resolve().findStaticField(predicate);
        return null;
    }

    public Field getStaticField(Predicate<Field> predicate) {
        var found = findStaticField(predicate);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose) {
            forEachStaticField(field -> logger.info("field: {}, id: {}", field.getQualifiedName(), field.getStringId()));
        }
        throw new NullPointerException("Fail to find static field satisfying the specified criteria in klass: " + this);
    }

    public void forEachStaticField(Consumer<Field> action) {
        staticFields.forEach(action);
        if (superType != null)
            superType.resolve().forEachStaticField(action);
    }

    public boolean containsField(long fieldId) {
        return fields.get(Entity::tryGetId, fieldId) != null || superType != null && superType.resolve().containsField(fieldId);
    }

    public boolean containsStaticField(Id fieldId) {
        return staticFields.get(Entity::tryGetId, fieldId) != null || superType != null && superType.resolve().containsStaticField(fieldId);
    }

    public Field tryGetFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.resolve().tryGetFieldByName(fieldName);
            if (superField != null)
                return superField;
        }
        return fields.get(Field::getName, fieldName);
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetFieldByName(fieldName));
    }

    public Field tryGetStaticFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.resolve().tryGetStaticFieldByName(fieldName);
            if (superField != null)
                return superField;
        }
        return staticFields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetStaticFieldByName(fieldName));
    }

    public Field getStaticFieldByVar(Var var) {
        if (var.isId())
            return getStaticField(var.getId());
        else
            return tryGetStaticFieldByName(var.getName());
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
        if (superType != null) {
            var p = superType.resolve().findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        for (var it : interfaces) {
            var p = it.resolve().findStaticPropertyByVar(var);
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

    public Field getStaticField(Id id) {
        if (superType != null && superType.resolve().containsStaticField(id))
            return superType.resolve().getStaticField(id);
        Field field = staticFields.get(Entity::tryGetId, id);
        if (field != null)
            return field;
        throw new InternalException("Field '" + id + "' does not exist or is not ready");
    }

    @Nullable
    public Field findFieldByCode(String code) {
        var field = fields.get(Field::getCode, code);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().findFieldByCode(code);
        return null;
    }

    public Property getPropertyByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getPropertyByName(var.getName());
            case ID -> getProperty(var.getId());
        };
    }

    public Property getProperty(Id id) {
        return NncUtils.requireNonNull(getProperty(Property::tryGetId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getPropertyByCode(String code) {
        return getProperty(Property::getCode, code);
    }

    public Property getPropertyByName(String name) {
        return getProperty(Property::getName, name);
    }

    private <T> Property getProperty(IndexMapper<Property, T> property, T value) {
        var field = fields.get(property, value);
        if (field != null)
            return field;
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superType != null)
            return superType.resolve().getProperty(property, value);
        return null;
    }

    public List<Property> getProperties() {
        return NncUtils.concatList(fields.toList(), methods.toList());
    }

    public Field getFieldByCode(String code) {
        return NncUtils.requireNonNull(findFieldByCode(code),
                String.format("Can not find field with code '%s' in type '%s'", code, name));
    }


    @Nullable
    public Field findSelfFieldByCode(String code) {
        return fields.get(Property::getCode, code);
    }

    @Nullable
    public Field findSelfStaticFieldByCode(String code) {
        return staticFields.get(Property::getCode, code);
    }

    @Nullable
    public Method findSelfMethod(String code, List<Type> parameterTypes) {
        return NncUtils.find(
                methods,
                method -> Objects.equals(method.getCode(), code) &&
                        method.getParameterTypes().equals(parameterTypes)
        );
    }

    @Nullable
    public Field findStaticFieldByCode(String code) {
        if (superType != null) {
            Field superField = superType.resolve().findStaticFieldByCode(code);
            if (superField != null)
                return superField;
        }
        return staticFields.get(Field::getCode, code);
    }

    @Nullable
    public Index findIndex(Predicate<Index> predicate) {
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

    public Field getFieldByVar(Var var) {
        if (var.isId())
            return getField(var.getId());
        else
            return tryGetFieldByName(var.getName());
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = EntityUtils.getMetaFieldName(javaField);
        return requireNonNull(tryGetFieldByName(fieldName),
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

    private void getFieldsDownwardInHierarchy0(List<Field> results) {
        listAddAll(results, fields);
        for (Klass subType : subKlasses) {
            subType.getFieldsDownwardInHierarchy0(results);
        }
    }

    public Field getFieldNyName(String fieldName) {
        return NncUtils.requireNonNull(
                tryGetFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic())
            staticFields.remove(field);
        else
            fields.remove(field);
        resetSortedFields();
    }

    public boolean isAssignableFrom(Klass that) {
        if (equals(that)) {
            return true;
        }
        if (template != null) {
            var s = that.findAncestorByTemplate(template);
            if (s != null)
                return NncUtils.biAllMatch(typeArguments, s.typeArguments, Type::contains);
            else
                return false;
        } else {
            if (that.getSuperType() != null && getType().isAssignableFrom(that.getSuperType()))
                return true;
            if (isInterface()) {
                for (var it : that.interfaces) {
                    if (isAssignableFrom(it.resolve())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public @NotNull ClassType getType() {
        if (type == null)
            type = new ClassType(this.getEffectiveTemplate(), isParameterized() ? typeArguments.toList() : List.of());
        return type;
    }

    public void addParameterized(Klass parameterized) {
        NncUtils.requireTrue(parameterized.getTemplate() == this);
        NncUtils.requireNull(parameterizedClasses().put(parameterized.typeArguments.secretlyGetTable(), parameterized),
                () -> "Parameterized klass " + parameterized.getTypeDesc() + " already exists");
    }

    public Klass getExistingParameterized(List<? extends Type> typeArguments) {
        if (NncUtils.map(typeParameters, TypeVariable::getType).equals(typeArguments))
            return this;
        return parameterizedClasses().get(typeArguments);
    }

    private ParameterizedElementMap<List<? extends Type>, Klass> parameterizedClasses() {
        if (parameterizedClasses == null)
            parameterizedClasses = new ParameterizedElementMap<>();
        return parameterizedClasses;
    }

    public Klass getParameterized(List<? extends Type> typeArguments) {
        return getParameterized(typeArguments, ResolutionStage.DEFINITION);
    }

    public Klass getParameterized(List<? extends Type> typeArguments, ResolutionStage stage) {
        if (!isTemplate) {
            if (typeArguments.isEmpty())
                return this;
            else
                throw new InternalException(this + " is not a template class");
        }
        var pClass = getExistingParameterized(typeArguments);
        if (pClass == this)
            return this;
        if (pClass != null && pClass.getStage().isAfterOrAt(stage))
            return pClass;
        var subst = new SubstitutorV2(
                this, typeParameters.toList(), typeArguments, stage);
        pClass = (Klass) subst.copy(this);
        return pClass;
    }

    @Nullable
    public ClassType getSuperType() {
        return superType;
    }

    public @Nullable Klass getSuperClass() {
        return superType != null ? superType.resolve() : null;
    }

    public Klass asSuper(Klass template) {
        NncUtils.requireTrue(template.isTemplate());
        Klass sup = accept(new ElementVisitor<>() {
            @Override
            public Klass visitKlass(Klass klass) {
                if (klass.getEffectiveTemplate() == template)
                    return klass;
                Klass found;
                if (superType != null && (found = superType.accept(this)) != null)
                    return found;
                for (var anInterface : interfaces) {
                    if ((found = anInterface.accept(this)) != null)
                        return found;
                }
                return null;
            }
        });
        return NncUtils.requireNonNull(sup,
                String.format("Can not find a super type of type '%s' with template '%s'",
                        this.name, template.name));
    }

    public List<ClassType> getInterfaces() {
        return Collections.unmodifiableList(interfaces.toList());
    }

    public void forEachSuper(Consumer<Klass> action) {
        if (superType != null)
            action.accept(superType.resolve());
        interfaces.forEach(it -> action.accept(it.resolve()));
    }

    public void forEachSuperClass(Consumer<Klass> action) {
        action.accept(this);
        if (superType != null)
            superType.resolve().forEachSuperClass(action);
    }

    public Klass getAncestorType(Klass targetType) {
        return NncUtils.requireNonNull(findAncestorByTemplate(targetType));
    }

    public Klass findAncestorByTemplate(Klass template) {
        return getClosure().find(t -> t.templateEquals(template));
    }

    public KlassDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return toDTO(serContext);
        }
    }

    public KlassDTO toDTO(SerializeContext serializeContext) {
        typeParameters.forEach(serializeContext::writeTypeDef);
        if (template != null)
            serializeContext.writeTypeDef(template);
        return new KlassDTO(
                serializeContext.getStringId(this),
                name,
                code,
                kind.code(),
                ephemeral,
                anonymous,
                getAttributesMap(),
                NncUtils.get(superType, t -> t.toExpression(serializeContext)),
                NncUtils.map(interfaces, t -> t.toExpression(serializeContext)),
                source.code(),
                NncUtils.map(fields, Field::toDTO),
                NncUtils.map(staticFields, Field::toDTO),
                NncUtils.get(titleField, serializeContext::getStringId),
                NncUtils.map(constraints, Constraint::toDTO),
                NncUtils.map(methods, f -> f.toDTO(serializeContext.shouldWriteCode(this), serializeContext)),
                NncUtils.map(mappings, m -> m.toDTO(serializeContext)),
                NncUtils.get(defaultMapping, serializeContext::getStringId),
                desc,
                getExtra(),
                isEnum() ? NncUtils.map(getEnumConstants(), Instance::toDTO) : List.of(),
                isAbstract,
                isTemplate(),
                NncUtils.map(typeParameters, serializeContext::getStringId),
                NncUtils.map(typeParameters, tv -> tv.toDTO(serializeContext)),
                NncUtils.get(template, serializeContext::getStringId),
                NncUtils.map(typeArguments, t -> t.toExpression(serializeContext)),
                !subKlasses.isEmpty(),
                struct,
                NncUtils.map(errors, Error::toDTO)
        );
    }

    protected Object getExtra() {
        return null;
    }

    public <T extends Constraint> List<T> getConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
        if (superType != null) {
            result = NncUtils.union(
                    superType.resolve().getConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public List<Constraint> getConstraints() {
        return constraints.toList();
    }

    public <T extends Constraint> T getConstraint(Class<T> constraintType, Id id) {
        return find(getConstraints(constraintType), c -> c.getId().equals(id));
    }

    public List<CheckConstraint> getFieldCheckConstraints(Field field) {
        var constraints = getConstraints(CheckConstraint.class);
        return NncUtils.filter(constraints, c -> c.isFieldConstraint(field));
    }

    @SuppressWarnings("unused")
    public Constraint getConstraint(Id id) {
        return NncUtils.find(requireNonNull(constraints), c -> c.idEquals(id));
    }

    public Index getUniqueConstraint(Id id) {
        return getConstraint(Index.class, id);
    }

    @JsonIgnore
    public Klass getConcreteType() {
        return this;
    }

    public List<ClassInstance> getEnumConstants() {
        if (!isEnum())
            throw new InternalException("type " + this + " is not a enum type");
        return NncUtils.filterAndMap(
                staticFields,
                this::isEnumConstantField,
                f -> (ClassInstance) f.getStaticValue()
        );
    }

    public EnumConstantRT getEnumConstant(Id id) {
        if (!isEnum())
            throw new InternalException("type " + this + " is not a enum type");
        for (Field field : staticFields) {
            if (isEnumConstantField(field) && Objects.equals(field.getStaticValue().tryGetId(), id))
                return createEnumConstant((ClassInstance) field.getStaticValue());
        }
        throw new InternalException("Can not find enum constant with id " + id);
    }

    private EnumConstantRT createEnumConstant(ClassInstance instance) {
        return new EnumConstantRT(instance);
    }

    boolean isEnumConstantField(Field field) {
        // TODO be more precise
        return isEnum() && field.isStatic() && isType(field.getType())
                && field.getStaticValue() instanceof ClassInstance;
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

    public Method resolveMethod(@NotNull Method methodRef) {
        return Objects.requireNonNull(
                tryResolveMethod(methodRef),
                () -> String.format("Fail to resolve method %s.%s in type %s",
                        methodRef.getDeclaringType().getTypeDesc(),
                        methodRef.getTypeDesc(), this)
        );
    }

    public @Nullable Method tryResolveMethod(@NotNull Method methodRef) {
        if (methodRef.getDeclaringType() == this)
            return methodRef;
        var hTemplate = methodRef.getHorizontalTemplate();
        if (hTemplate != null) {
            var resolvedTemplate = tryResolveNonParameterizedMethod(hTemplate);
            if (resolvedTemplate == null)
                return null;
            return (Method) resolvedTemplate.getParameterized(methodRef.getTypeArguments());
        } else
            return tryResolveNonParameterizedMethod(methodRef);
    }

    public void forEachMethod(Consumer<Method> action) {
        methods.forEach(action);
        if (superType != null)
            superType.resolve().forEachMethod(action);
        interfaces.forEach(it -> it.resolve().forEachMethod(action));
    }

    public Method resolveMethod(String code, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly) {
        var found = tryResolveMethod(code, argumentTypes, typeArguments, staticOnly);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose) {
            logger.info("method resolution failed");
            forEachMethod(m -> logger.info(m.getSignatureString()));
        }
        throw new NullPointerException(
                String.format("Can not find method %s%s(%s) in type %s",
                        NncUtils.isNotEmpty(typeArguments) ? "<" + NncUtils.join(typeArguments, Type::getName) + ">" : "",
                        code,
                        NncUtils.join(argumentTypes, Type::getName, ","), getName()));
    }

    public @Nullable Method tryResolveMethod(String code, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly) {
        var candidates = new ArrayList<Method>();
        getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates);
        out:
        for (Method m1 : candidates) {
            for (Method m2 : candidates) {
                if (m1 != m2 && m1.isHiddenBy(m2))
                    continue out;
            }
            return m1;
        }
        return null;
    }

    private void getCallCandidates(String code,
                                   List<Type> argumentTypes,
                                   List<Type> typeArguments,
                                   boolean staticOnly,
                                   List<Method> candidates) {
        methods.forEach(m -> {
            if ((m.isStatic() || !staticOnly) && code.equals(m.getCode()) && m.getParameters().size() == argumentTypes.size()) {
                if (NncUtils.isNotEmpty(typeArguments)) {
                    if (m.getTypeParameters().size() == typeArguments.size()) {
                        var pMethod = (Method) m.getParameterized(typeArguments);
                        if (pMethod.matches(code, argumentTypes))
                            candidates.add(pMethod);
                    }
                } else {
                    if (m.matches(code, argumentTypes))
                        candidates.add(m);
                }
            }
        });
        if (superType != null)
            superType.resolve().getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates);
        if (isInterface() || isAbstract || staticOnly) {
            interfaces.forEach(it -> it.resolve().getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates));
        }
    }

    public @NotNull Method resolveNonParameterizedMethod(Method methodRef) {
        return Objects.requireNonNull(tryResolveNonParameterizedMethod(methodRef), () -> "Can not resolve method " + methodRef.getTypeDesc() + " in klass " + this);
    }

    @Nullable
    public Method tryResolveNonParameterizedMethod(Method methodref) {
        NncUtils.requireFalse(methodref.getParameterizedFlows());
        var methodTable = getMethodTable();
        if (methodref.getDeclaringType().isUncertain())
            methodref = methodTable.findByVerticalTemplate(Objects.requireNonNull(methodref.getVerticalTemplate()));
        return methodTable.findByOverridden(methodref);
    }

    public boolean isUncertain() {
        return NncUtils.anyMatch(typeArguments, Type::isUncertain);
    }

    @Nullable
    public Klass getTemplate() {
        return template;
    }

    public Collection<Klass> getParameterized() {
        return parameterizedClasses().values();
    }

    //    @Override
    public void setTemplate(Object template) {
        NncUtils.requireNull(this.template);
        isParameterized = template != null;
        this.template = copySource = (Klass) template;
    }

    public Klass getEffectiveTemplate() {
        return template != null ? template : this;
    }

    public boolean templateEquals(Klass that) {
        return this == that || this.getTemplate() == that;
    }

    public List<TypeVariable> getTypeParameters() {
        return typeParameters.toList();
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
        typeArguments.add(typeParameter.getType());
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
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public ClassKind getKind() {
        return kind;
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

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    public List<Type> getEffectiveTypeArguments() {
        return isParameterized() ? typeArguments.toList() : NncUtils.map(typeParameters, TypeVariable::getType);
    }

//    @Override
//    public ClassType getRawClass() {
//        return this;
//    }

//    public GenericClass getGenericSuperType() {
//        return genericSuperType;
//    }

//    public List<GenericClass> getGenericInterfaces() {
//        return Collections.unmodifiableList(genericInterfaces);
//    }

    public Index getUniqueConstraint(List<Field> fields) {
        return find(getIndices(), c -> c.isUnique() && c.getTypeFields().equals(fields));
    }

    public List<Index> getIndices() {
        return getConstraints(Index.class);
    }

    @Override
    protected String toString0() {
        return "Klass " + name + " (id:" + id + ")";
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        if (isTemplate() && !NncUtils.iterableEquals(NncUtils.map(typeParameters, TypeVariable::getType), typeArguments))
            throw new InternalException("Type arguments must equal to type parameters for a template type. Actual type arguments: " + typeArguments);
        this.typeArguments.reset(typeArguments);
    }

    private void addAncestorChangeListener(Runnable listener) {
        ancestorChangeListeners().add(listener);
    }

    private void removeAncestorChangeListener(Runnable listener) {
        ancestorChangeListeners().remove(listener);
    }

    protected final void onSuperTypesChanged() {
        if (supersCheckpoint != null) {
            for (var oldSuper : supersCheckpoint) {
                oldSuper.removeAncestorChangeListener(this::onAncestorChanged);
            }
        }
        onAncestorChanged();
        resetSuperTypeListeners();
        resetSortedClasses();
    }

    private void resetSuperTypeListeners() {
        supersCheckpoint = new ArrayList<>();
        forEachSuper(s -> {
            s.addAncestorChangeListener(this::onAncestorChanged);
            supersCheckpoint.add(s);
        });
    }

    private void onAncestorChanged() {
        closure = null;
        onAncestorChanged0();
        ancestorChangeListeners().forEach(Runnable::run);
    }

    protected List<Runnable> ancestorChangeListeners() {
        if (ancestorChangeListeners == null)
            ancestorChangeListeners = new ArrayList<>();
        return ancestorChangeListeners;
    }

    private void ensureListenersInitialized() {
        if (supersCheckpoint == null) {
            forEachSuper(Klass::ensureListenersInitialized);
            resetSuperTypeListeners();
        }
    }

    public void setSuperType(@Nullable ClassType superType) {
        if (this.superType != null)
            this.superType.resolve().removeSubType(this);
        if (superType != null) {
            this.superType = superType;
            superType.resolve().addSubType(this);
        } else
            this.superType = null;
        onSuperTypesChanged();
        supers = null;
    }

    public void setInterfaces(List<ClassType> interfaces) {
        for (var anInterface : this.interfaces) {
            anInterface.resolve().removeSubType(this);
        }
        this.interfaces.clear();
        for (var anInterface : interfaces) {
            this.interfaces.add(anInterface);
            anInterface.resolve().addSubType(this);
        }
        onSuperTypesChanged();
        supers = null;
    }

    protected void onAncestorChanged0() {
        getMethodTable().rebuild();
        rank = 0;
    }

    public void clearTypeParameters() {
        this.typeParameters.clear();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> tp.setGenericDeclaration(this));
        this.typeParameters.resetChildren(typeParameters);
        if (isTemplate())
            setTypeArguments(NncUtils.map(typeParameters, TypeVariable::getType));
    }

    public ObjectMapping getMappingInAncestors(Id id) {
        var mapping = findMapping(id);
        if (mapping != null)
            return mapping;
        if (superType != null) {
            if ((mapping = superType.resolve().getMappingInAncestors(id)) != null)
                return mapping;
        }
        for (var it : interfaces) {
            if ((mapping = it.resolve().getMappingInAncestors(id)) != null)
                return mapping;
        }
        throw new InternalException("Can not find mapping in the ancestors of type: " + getName());
    }

    public @Nullable ObjectMapping findMapping(Id id) {
        return mappings.get(Entity::getId, id);
    }

    public void setFields(List<Field> fields) {
        requireTrue(allMatch(fields, f -> f.getDeclaringType() == this));
        this.fields.resetChildren(fields);
        resetSortedFields();
    }

    public void moveField(Field field, int index) {
        moveProperty(fields, field, index);
    }

    private <T extends Entity & Property> void moveProperty(ChildArray<T> properties, T property, int index) {
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
        requireTrue(allMatch(staticFields, f -> f.getDeclaringType() == this));
        this.staticFields.resetChildren(staticFields);
    }

    public void setConstraints(List<Constraint> constraints) {
        requireTrue(allMatch(constraints, c -> c.getDeclaringType() == this));
        this.constraints.resetChildren(constraints);
    }

    public void setMethods(List<Method> methods) {
        requireTrue(allMatch(methods, f -> f.getDeclaringType() == this));
        this.methods.resetChildren(methods);
        rebuildMethodTable();
    }

    public boolean isParameterized() {
        return template != null && template != this;
    }

    @Override
    public String getTypeDesc() {
        if (isParameterized())
            return Objects.requireNonNull(template).getName() + "<" + NncUtils.join(typeArguments, Type::getTypeDesc, ",") + ">";
        else
            return getName();
    }

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        if (superType != null)
            superType.resolve().removeSubType(this);
        for (var anInterface : interfaces) {
            anInterface.resolve().removeSubType(this);
        }
        if (isTemplate())
            return new ArrayList<>(context.selectByKey(Klass.TEMPLATE_IDX, this));
        else
            return List.of();
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

    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        typeArguments.forEach(t -> t.getCapturedTypes(capturedTypes));
    }

    public Klass findInClosure(Id id) {
        return getClosure().find(t -> Objects.equals(t.tryGetId(), id));
    }

    @Override
    public void onChange(ClassInstance instance, IEntityContext context) {
        rebuildMethodTable();
        if (!isInterface()) {
            for (var it : interfaces) {
                for (Method method : it.resolve().getMethods()) {
                    if (tryResolveNonParameterizedMethod(method) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), it.getName(), method.getName());
                    }
                }
            }
        }
        saveMapping(context);
    }

    @Override
    public boolean isChangeAware() {
        return !anonymous;
    }

    public void saveMapping(IEntityContext context) {
        if (!(context instanceof DefContext) && shouldGenerateBuiltinMapping()) {
            MappingSaver.create(context).saveBuiltinMapping(this, true);
        }
    }

    public boolean shouldGenerateBuiltinMapping() {
        return (isClass() || isValue()) && !anonymous;
    }

    public void setDefaultMapping(@Nullable ObjectMapping mapping) {
        this.defaultMapping = mapping;
    }

    @Nullable
    public Field getTitleField() {
        if (titleField != null)
            return titleField;
        if (superType != null)
            return superType.resolve().getTitleField();
        return null;
    }

    public void setMappings(List<ObjectMapping> mappings) {
        this.mappings.resetChildren(mappings);
    }

    public @Nullable ObjectMapping getDefaultMapping() {
        return defaultMapping;
    }

    public List<Field> getStaticFields() {
        return staticFields.toList();
    }

    public void removeMapping(ObjectMapping mapping) {
        mappings.remove(mapping);
    }

    public boolean isViewType(Type type) {
        if (isType(type))
            return true;
        return NncUtils.anyMatch(mappings, m -> m.getTargetType().equals(type));
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if (isParameterized())
            return requireNonNull(template).getCodeNotNull() + "<" + NncUtils.join(typeArguments, type -> type.getInternalName(current)) + ">";
        else
            return getCodeNotNull();
    }

    public boolean isList() {
        var t = getEffectiveTemplate();
//        return t == StandardTypes.getListKlass() || BuiltinKlasses.childList.get() == t || StandardTypes.getReadWriteListKlass() == t || StandardTypes.getValueListKlass() == t;
        return t.getNativeClass() == ListNative.class;
    }

    public boolean isChildList() {
        return getEffectiveTemplate() == StdKlass.childList.get();
    }

    public Type getFirstTypeArgument() {
        return getTypeArguments().get(0);
    }

    public Type getIterableElementType() {
        var iterableType = Objects.requireNonNull(
                findAncestorByTemplate(StdKlass.iterable.get()),
                () -> getTypeDesc() + " is not an Iterable class");
        return iterableType.getTypeArguments().get(0);
    }

    public boolean isSAMInterface() {
        return isInterface() && getMethods().size() == 1;
    }

    public Method getSingleAbstractMethod() {
        if (!isSAMInterface())
            throw new InternalException("Type " + getName() + " is not a SAM interface");
        return getMethods().get(0);
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

    public @Nullable ObjectMapping getSourceMapping() {
        var template = getEffectiveTemplate();
        var mapping = template.findAncestorEntity(ObjectMapping.class);
        if (mapping != null) {
            var sourceKlass = mapping.getSourceKlass().getParameterized(typeArguments.toList());
            return sourceKlass.getMapping(m -> m.getEffectiveTemplate() == mapping);
        } else
            return null;
    }

    public Method findGetterByPropertyName(String propertyName) {
        for (Method method : getAllMethods()) {
            if (method.isGetter() && method.getPropertyName().equals(propertyName))
                return method;
        }
        return null;
    }

    public Method findSetterByPropertyName(String propertyName) {
        for (Method method : getAllMethods()) {
            if (method.isSetter() && method.getPropertyName().equals(propertyName))
                return method;
        }
        return null;
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

}

