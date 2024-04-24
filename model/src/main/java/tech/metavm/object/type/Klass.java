package tech.metavm.object.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.Var;
import tech.metavm.flow.Error;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.generic.SubstitutorV2;
import tech.metavm.object.type.rest.dto.ClassTypeParam;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.view.MappingSaver;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static tech.metavm.util.NncUtils.*;

@EntityType("类")
public class Klass extends TypeDef implements GenericDeclaration, ChangeAware, GenericElement, StagedEntity, GlobalKey, LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final Logger logger = LoggerFactory.getLogger(Klass.class);

    public static final IndexDef<Klass> IDX_NAME = IndexDef.create(Klass.class, "name");

    public static final IndexDef<Klass> IDX_PARAMETERIZED_TYPE_KEY =
            IndexDef.createUnique(Klass.class, "parameterizedTypeKey");

    public static final IndexDef<Klass> UNIQUE_CODE = IndexDef.createUnique(Klass.class, "code");

    public static final IndexDef<Klass> TEMPLATE_IDX = IndexDef.create(Klass.class, "template");

    public static final IndexDef<Klass> TYPE_ARGUMENTS_IDX = IndexDef.create(Klass.class, "typeArguments");

    @EntityField(value = "name", asTitle = true)
    private String name;
    @Nullable
    private String code;
    private final ClassKind kind;
    private boolean anonymous;
    private boolean ephemeral;
    @EntityField("超类")
    @Nullable
    private ClassType superType;
    @ChildEntity("接口")
    private final ReadWriteArray<ClassType> interfaces = addChild(new ReadWriteArray<>(ClassType.class), "interfaces");
    @EntityField("来源")
    private ClassSource source;
    @ChildEntity("子类列表")
    private final ReadWriteArray<Klass> subTypes = addChild(new ReadWriteArray<>(Klass.class), "subTypes");
    @EntityField("描述")
    @Nullable
    private String desc;
    @ChildEntity("字段列表")
    private final ChildArray<Field> fields = addChild(new ChildArray<>(Field.class), "fields");
    @EntityField("标题属性")
    @Nullable
    private Field titleField;
    @ChildEntity("方法列表")
    private final ChildArray<Method> methods = addChild(new ChildArray<>(Method.class), "methods");
    @ChildEntity("参数化方法列表")
    private final ChildArray<Method> parameterizedMethods = addChild(new ChildArray<>(Method.class), "parameterizedMethods");

    @ChildEntity("静态字段列表")
    private final ChildArray<Field> staticFields = addChild(new ChildArray<>(Field.class), "staticFields");
    @ChildEntity("约束列表")
    private final ChildArray<Constraint> constraints = addChild(new ChildArray<>(Constraint.class), "constraints");
    @Nullable
    @EntityField("模板")
    private Klass template;
    // Don't remove, for search
    @SuppressWarnings("unused")
    @EntityField("是否抽象")
    private boolean isAbstract;
    @EntityField("是否模版")
    private boolean isTemplate;
    // Don't remove, used for search
    @EntityField("是否参数化")
    private boolean isParameterized;
    @ChildEntity("类型参数")
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @ChildEntity("类型实参")
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");

    // TODO (Important!) not scalable, must be optimized before going to production
    @ChildEntity("依赖")
    private final ReadWriteArray<Klass> dependencies = addChild(new ReadWriteArray<>(Klass.class), "dependencies");
    @Nullable
    private String parameterizedTypeKey;
    @ChildEntity("错误列表")
    private final ChildArray<Error> errors = addChild(new ChildArray<>(Error.class), "errors");

    private boolean error;

    @ChildEntity("视图映射列表")
    private final ChildArray<ObjectMapping> mappings = addChild(new ChildArray<>(ObjectMapping.class), "mappings");

    @EntityField("默认视图")
    @Nullable
    private ObjectMapping defaultMapping;

    @EntityField("复制来源")
    @CopyIgnore
    private @Nullable Klass copySource;

    @EntityField("状态")
    private ClassTypeState state = ClassTypeState.INIT;

    @SuppressWarnings("unused")
    private boolean templateFlag = false;

    private boolean struct;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    private transient List<Klass> supers;

    private transient volatile List<Field> sortedFields;

    private transient Closure closure;

    private transient ClassType type;

    @Nullable
    private transient List<Klass> supersCheckpoint;

    private transient List<Runnable> ancestorChangeListeners = new ArrayList<>();

    private transient Map<List<Type>, Klass> parameterizedClasses = new HashMap<>();

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
            List<Type> typeArguments) {
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
        setTypeParameters(typeParameters);
        setTypeArguments(typeArguments);
        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        NncUtils.requireTrue(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    public void update(TypeDTO typeDTO) {
        this.name = typeDTO.name();
        ClassTypeParam param = (ClassTypeParam) typeDTO.param();
        setDesc(param.desc());
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void addSubType(@NotNull Klass subType) {
        if (subTypes.contains(subType))
            throw new InternalException("Subtype '" + subType + "' is already added to this type");
        subTypes.add(subType);
    }

    void removeSubType(Klass subType) {
        subTypes.remove(subType);
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
        return readyFields();
    }

    public List<Field> getFields() {
        return fields.toList();
    }

    public List<Field> getAllFields() {
        if (superType != null)
            return NncUtils.union(superType.resolve().getAllFields(), readyFields());
        else
            return readyFields();
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
            return getCodeRequired();
        } else {
            return context.getModelName(template, this) + "<"
                    + NncUtils.join(typeArguments, object -> context.getModelName(object, this))
                    + ">";
        }
    }

    public String getCodeRequired() {
        return Objects.requireNonNull(code);
    }

    public void forEachField(Consumer<Field> action) {
        if (superType != null)
            superType.resolve().forEachField(action);
        this.fields.stream().filter(Field::isReady).forEach(action);
    }

    public boolean allFieldsMatch(Predicate<Field> predicate) {
        if (superType != null && !superType.resolve().allFieldsMatch(predicate))
            return false;
        return this.fields.stream().filter(Field::isReady).allMatch(predicate);
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING);
        this.titleField = titleField;
    }

    @Override
    public void onBind(IEntityContext context) {
        if (isTemplate())
            context.getGenericContext().add(this);
    }

//    public boolean isView() {
//        return sourceMapping != null;
//    }
//
//    public ObjectMapping getSourceMapping() {
//        return Objects.requireNonNull(sourceMapping, String.format("Type '%s' is not a view", getName()));
//    }

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

    public List<Klass> getSubTypes() {
        return subTypes.toList();
    }

    public List<Klass> getDescendantTypes() {
        List<Klass> types = new ArrayList<>();
        visitDescendantTypes(types::add);
        return types;
    }

    public void visitDescendantTypes(Consumer<Klass> action) {
        action.accept(this);
        for (Klass subType : subTypes) {
            subType.visitDescendantTypes(action);
        }
    }

    public List<ObjectMapping> getMappings() {
        return mappings.toList();
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
                for (Klass subType : klass.subTypes)
                    subType.accept(this);
                return super.visitKlass(klass);
            }
        });
        return typeIds;
    }

    public int getRank() {
        if (rank == 0) {
            int r = superType != null ? superType.resolve().rank : 0;
            for (var it : interfaces) {
                var itKlass = it.resolve();
                if (itKlass.rank > r)
                    r = itKlass.rank;
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

    public List<Field> getSortedFields() {
        if (sortedFields == null) {
            synchronized (this) {
                if (sortedFields == null) {
                    var sf = new ArrayList<Field>();
                    forEachField(f -> {
                        if (f.isIdNotNull())
                            sf.add(f);
                    });
                    sf.sort(Comparator.comparing(Field::getId));
                    sortedFields = sf;
                }
            }
        }
        return sortedFields;
    }

    void resetFieldsMemoryDataStructures() {
        this.sortedFields = null;
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
                () -> String.format("Can not find method %s(%s) in type %s",
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
        return getMethod(Method::getCode, code);
    }

    public @Nullable Method findSelfMethodByCode(String code) {
        return methods.get(Flow::getCode, code);
    }

    public @Nullable Method findMethodByVerticalTemplate(Method template) {
        return getMethod(Method::getVerticalTemplate, template);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return NncUtils.find(methods.toList(), predicate);
    }

    public <T> Method getMethod(IndexMapper<Method, T> property, T value) {
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superType != null) {
            var m = superType.resolve().getMethod(property, value);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.resolve().getMethod(property, value);
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
            throw BusinessException.invalidField(field, "字段名称'" + field.getName() + "'已存在");
        if (field.getCode() != null &&
                (findSelfFieldByCode(field.getCode()) != null || findSelfStaticFieldByCode(field.getCode()) != null))
            throw BusinessException.invalidField(field, "字段编号" + field.getCode() + "已存在");
        if (field.isStatic())
            staticFields.addChild(field);
        else
            fields.addChild(field);
        resetFieldsMemoryDataStructures();
    }

    @Override
    public void onLoad(IEntityContext context) {
        stage = ResolutionStage.INIT;
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
        return false;
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

    private List<Field> readyFields() {
        return fields.filter(Field::isReady, true);
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
                () -> "Can not find property for var " + var + " in type " + name
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
        if (field != null && field.isReady())
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
                "Can not find field for java indexItem " + javaField);
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
        for (Klass subType : subTypes) {
            subType.getFieldsDownwardInHierarchy0(results);
        }
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return NncUtils.requireNonNull(
                tryGetFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic())
            staticFields.remove(field);
        else
            fields.remove(field);
        resetFieldsMemoryDataStructures();
    }

    public boolean isAssignableFrom(Klass that) {
        if (equals(that)) {
            return true;
        }
        if (template != null) {
            var s = that.findAncestor(template);
            if (s != null)
                return NncUtils.biAllMatch(typeArguments, s.typeArguments, (type, that1) -> type.contains(that1));
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

    public ClassType getType() {
        if (type == null)
            type = new ClassType(this.getEffectiveTemplate(), typeArguments);
        return type;
    }

    public void addParameterized(Klass parameterized) {
        NncUtils.requireTrue(parameterized.getTemplate() == this);
        parameterizedClasses.putIfAbsent(parameterized.getTypeArguments(), parameterized);
    }

    public Klass getExistingParameterized(List<Type> typeArguments) {
        if (parameterizedClasses == null)
            parameterizedClasses = new HashMap<>();
        if (NncUtils.map(typeParameters, TypeVariable::getType).equals(typeArguments))
            return this;
        return parameterizedClasses.get(typeArguments);
    }

    public Klass getParameterized(List<Type> typeArguments) {
        if (!isTemplate)
            throw new InternalException(this + " is not a template class");
        var pClass = getExistingParameterized(typeArguments);
        if (pClass != null && pClass.getStage().isAfterOrAt(stage))
            return pClass;
        var subst = new SubstitutorV2(
                this, typeParameters.toList(), typeArguments, stage);
        pClass = (Klass) this.accept(subst);
        parameterizedClasses.put(typeArguments, pClass);
        return pClass;
    }

    @Nullable
    public ClassType getSuperType() {
        return superType;
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
        return Collections.unmodifiableList(interfaces);
    }

    public void forEachSuper(Consumer<Klass> action) {
        if (superType != null)
            action.accept(superType.resolve());
        interfaces.forEach(it -> action.accept(it.resolve()));
    }

    public Klass getAncestorType(Klass targetType) {
        return NncUtils.requireNonNull(findAncestor(targetType));
    }

    public Klass findAncestor(Klass targetType) {
        return getClosure().find(t -> t.templateEquals(targetType));
    }

    public TypeDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return toDTO(serContext);
        }
    }

    public TypeDTO toDTO(SerializeContext serializeContext) {
        return new TypeDTO(
                serializeContext.getId(this),
                name,
                code,
                kind.code(),
                ephemeral,
                anonymous,
                getParam(serializeContext)
        );
    }

    protected ClassTypeParam getParam(SerializeContext serContext) {
        typeParameters.forEach(serContext::writeTypeVariable);
//        typeArguments.forEach(serContext::writeType);
//        if (superType != null)
//            serContext.writeClass(superType);
//        interfaces.forEach(serContext::writeClass);
        if (template != null)
            serContext.writeClass(template);
        var param = new ClassTypeParam(
                NncUtils.get(superType, serContext::getId),
                NncUtils.map(interfaces, serContext::getId),
                source.code(),
                NncUtils.map(fields, Field::toDTO),
                NncUtils.map(staticFields, Field::toDTO),
                NncUtils.get(titleField, serContext::getId),
                NncUtils.map(constraints, Constraint::toDTO),
                NncUtils.map(methods, f -> f.toDTO(serContext.shouldWriteCode(this), serContext)),
                NncUtils.map(mappings, m -> m.toDTO(serContext)),
                NncUtils.get(defaultMapping, serContext::getId),
                desc,
                getExtra(),
                isEnum() ? NncUtils.map(getEnumConstants(), Instance::toDTO) : List.of(),
                isAbstract,
                isTemplate(),
                NncUtils.map(typeParameters, serContext::getId),
                NncUtils.map(typeParameters, tv -> tv.toDTO(serContext)),
                NncUtils.get(template, serContext::getId),
                NncUtils.map(typeArguments, serContext::getId),
                NncUtils.map(dependencies, serContext::getId),
                !subTypes.isEmpty(),
                struct,
                NncUtils.map(errors, Error::toDTO)
        );
        return param;
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
        if (type instanceof ClassType classType) {
            if (isParameterized())
                return classType.getKlass().equals(getTemplate()) && classType.getTypeArguments().equals(typeArguments);
            else
                return classType.getKlass() == this && classType.getTypeArguments().isEmpty();
        } else
            return false;
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

    public Method resolveMethod(@NotNull Method methodRef, @NotNull ParameterizedFlowProvider parameterizedFlowProvider) {
        return Objects.requireNonNull(
                tryResolveMethod(methodRef, parameterizedFlowProvider),
                () -> String.format("Fail to resolve method %s.%s in type %s",
                        methodRef.getDeclaringType().getTypeDesc(),
                        methodRef.getTypeDesc(), this)
        );
    }

    public @Nullable Method tryResolveMethod(@NotNull Method methodRef, @NotNull ParameterizedFlowProvider parameterizedFlowProvider) {
        if (methodRef.getDeclaringType() == this)
            return methodRef;
        var hTemplate = methodRef.getHorizontalTemplate();
        if (hTemplate != null) {
            var resolvedTemplate = tryResolveNonParameterizedMethod(hTemplate);
            if (resolvedTemplate == null)
                return null;
            return parameterizedFlowProvider.getParameterizedFlow(resolvedTemplate, methodRef.getTypeArguments());
        } else
            return tryResolveNonParameterizedMethod(methodRef);
    }

    public Method resolveMethod(String code, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly, ParameterizedFlowProvider parameterizedFlowProvider) {
        return Objects.requireNonNull(tryResolveMethod(code, argumentTypes, typeArguments, staticOnly, parameterizedFlowProvider), () ->
                String.format("Can not find method %s%s(%s) in type %s",
                        NncUtils.isNotEmpty(typeArguments) ? "<" + NncUtils.join(typeArguments, Type::getName) + ">" : "",
                        code,
                        NncUtils.join(argumentTypes, Type::getName, ","), getName()));
    }

    public @Nullable Method tryResolveMethod(String code, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly, ParameterizedFlowProvider parameterizedFlowProvider) {
        var candidates = new ArrayList<Method>();
        getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates, parameterizedFlowProvider);
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
                                   List<Method> candidates,
                                   ParameterizedFlowProvider parameterizedFlowProvider) {
        methods.forEach(m -> {
            if ((m.isStatic() || !staticOnly) && code.equals(m.getCode()) && m.getParameters().size() == argumentTypes.size()) {
                if (NncUtils.isNotEmpty(typeArguments)) {
                    if (m.getTypeParameters().size() == typeArguments.size()) {
                        var pMethod = parameterizedFlowProvider.getParameterizedFlow(m, typeArguments);
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
            superType.resolve().getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates, parameterizedFlowProvider);
        if (isInterface() || isAbstract || staticOnly) {
            interfaces.forEach(it -> it.resolve().getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates, parameterizedFlowProvider));
        }
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
        typeArguments.add(typeParameter.getType().copy());
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
        return "ClassType " + name + " (id:" + id + ")";
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        if (isTemplate() && !NncUtils.iterableEquals(NncUtils.map(typeParameters, TypeVariable::getType), typeArguments))
            throw new InternalException("Type arguments must equal to type parameters for a template type. Actual type arguments: " + typeArguments);
        this.typeArguments.reset(NncUtils.map(typeArguments, Type::copy));
        parameterizedTypeKey = null;
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
        this.superType = superType;
        if (superType != null)
            superType.resolve().addSubType(this);
        onSuperTypesChanged();
        supers = null;
    }

    public void setInterfaces(List<ClassType> interfaces) {
        for (var anInterface : this.interfaces) {
            anInterface.resolve().removeSubType(this);
        }
        this.interfaces.clear();
        this.interfaces.addAll(interfaces);
        for (var anInterface : interfaces) {
            anInterface.resolve().addSubType(this);
        }
        onSuperTypesChanged();
        supers = null;
    }

    protected void onAncestorChanged0() {
        getMethodTable().rebuild();
        rank = 0;
    }

    public void addDependency(Klass dependency) {
        NncUtils.requireFalse(dependencies.contains(dependency),
                "Dependency " + dependency + " already exists in type " + getName());
        dependencies.add(dependency);
    }

    public void setDependencies(List<Klass> dependencies) {
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public List<Klass> getDependencies() {
        return dependencies.toList();
    }

    public Klass getDependency(Klass template) {
        return NncUtils.findRequired(dependencies, dep -> Objects.equals(dep.getTemplate(), template));
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

    @Override
    public boolean afterContextInitIds() {
        if (template != null || isTemplate()) {
            if (parameterizedTypeKey == null) {
                parameterizedTypeKey = Types.getParameterizedKey(getEffectiveTemplate(), typeArguments);
            }
        }
        return true;
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
        return isClass() && !anonymous;
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
        return NncUtils.anyMatch(mappings, m -> m.getTargetType() == type);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if (typeArguments.isEmpty())
            return getCodeRequired();
        else
            return requireNonNull(template).getCodeRequired() + "<" + NncUtils.join(typeArguments, type -> type.getInternalName(current)) + ">";
    }

    public boolean isList() {
        var t = getEffectiveTemplate();
        return t == StandardTypes.getListType() || StandardTypes.getChildListType() == t || StandardTypes.getReadWriteListType() == t;
    }

    public boolean isChildList() {
        return getEffectiveTemplate() == StandardTypes.getChildListType();
    }

    public Type getListElementType() {
        NncUtils.requireTrue(isList());
        return getTypeArguments().get(0);
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

}

