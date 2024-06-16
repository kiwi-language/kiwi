package org.metavm.entity;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.Parameter;
import org.metavm.flow.ValueFactory;
import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.NullInstance;
import org.metavm.object.type.Index;
import org.metavm.object.type.*;
import org.metavm.object.view.MappingSaver;
import org.metavm.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static org.metavm.object.type.ResolutionStage.*;

public abstract class PojoParser<T, D extends PojoDef<T>> extends DefParser<T, ClassInstance, D> {

    protected final Class<T> javaClass;
    protected final java.lang.reflect.Type javaType;
    private D def;
    protected final DefContext defContext;
    protected final TypeFactory typeFactory;
    private final JavaSubstitutor substitutor;
    private final ColumnStore columnStore;

    public PojoParser(Class<T> javaClass, java.lang.reflect.Type javaType, DefContext defContext, ColumnStore columnStore) {
        this.javaClass = javaClass;
        this.javaType = javaType;
        this.defContext = defContext;
        substitutor = ReflectionUtils.resolveGenerics(javaType);
        typeFactory = new DefTypeFactory(defContext);
        this.columnStore = columnStore;
    }

    @SuppressWarnings("unchecked")
    private PojoDef<? super T> getSuperDef() {
        var superClass = javaClass.getSuperclass();
        if (superClass != null && superClass != Object.class)
            return (PojoDef<? super T>) defContext.getDef(substituteType(javaClass.getGenericSuperclass()), INIT);
        return null;
    }

    private java.lang.reflect.Type substituteType(java.lang.reflect.Type type) {
        var rawClass = switch (type) {
            case Class<?> klass -> klass;
            case ParameterizedType pType -> (Class<?>) pType.getRawType();
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
        if (RuntimeGeneric.class.isAssignableFrom(rawClass)) {
            return substitutor.substitute(type);
        } else {
            return ReflectionUtils.eraseType(type);
        }
    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defContext.getDef(substituteType(it), INIT)
        );
    }

    private List<Field> getIndexDefFields() {
        return ReflectionUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == IndexDef.class);
    }

    private List<Field> getConstraintDefFields() {
        return ReflectionUtils.getDeclaredStaticFields(javaClass, f -> f.getType() == ConstraintDef.class);
    }

    protected List<Field> getPropertyFields() {
        return ReflectionUtils.getDeclaredPersistentFields(javaClass);
    }

    @Override
    public D create() {
        return def = createDef(getSuperDef());
    }

    @Override
    public D get() {
        return def;
    }

    @Override
    public void generateSignature() {
        var klass = def.getTypeDef();
        if (javaType instanceof Class<?> && RuntimeGeneric.class.isAssignableFrom(javaClass)) {
            for (var javaTypeParam : javaClass.getTypeParameters()) {
                defContext.getDef(javaTypeParam, INIT);
            }
        }
        List<Type> typeArgs = new ArrayList<>();
        if (javaType instanceof java.lang.reflect.ParameterizedType pType) {
            for (var javaTypeArg : pType.getActualTypeArguments()) {
                typeArgs.add(defContext.getType(javaTypeArg));
            }
        }
        klass.setTypeArguments(typeArgs);
        klass.setStage(SIGNATURE);
    }

    @Override
    public void generateDeclaration() {
        var klass = def.getKlass();
        if (klass.getSuperType() != null)
            defContext.ensureStage(klass.getSuperType(), DECLARATION);
        klass.getInterfaces().forEach(it -> defContext.ensureStage(it, DECLARATION));
        getPropertyFields().forEach(f -> {
            if (!defContext.isFieldBlacklisted(f))
                parseField(f, def);
        });
        getIndexDefFields().forEach(f -> parseUniqueConstraint(f, def));
        saveBuiltinMapping(false);
        klass.setStage(DECLARATION);
    }

    @Override
    public void generateDefinition() {
        var klass = def.getKlass();
        klass.forEachField(f ->
                defContext.ensureStage(f.getType().getUnderlyingType(), DECLARATION));
        getConstraintDefFields().forEach(f -> parseCheckConstraint(f, def));
        saveBuiltinMapping(true);
        klass.setStage(DEFINITION);
    }

    private void saveBuiltinMapping(boolean saveContent) {
        var klass = def.getKlass();
        if (klass.shouldGenerateBuiltinMapping())
            MappingSaver.create(defContext).saveBuiltinMapping(klass, saveContent);
    }

    protected abstract D createDef(PojoDef<? super T> superDef);

    private void parseField(Field javaField, PojoDef<?> declaringTypeDef) {
        if (Instance.class.isAssignableFrom(javaField.getType())) {
            Type fieldType;
            Class<?> javaFieldClass = javaField.getType();
            if (javaFieldClass == ArrayInstance.class)
                fieldType = defContext.getType(ReadWriteArray.class);
            else if (javaFieldClass == ClassInstance.class)
                fieldType = defContext.getType(Object.class);
            else
                fieldType = defContext.getType(BiUnion.createNullableType(Object.class));
            org.metavm.object.type.Field field = createField(javaField, declaringTypeDef, fieldType);
            new InstanceFieldDef(
                    javaField,
                    field,
                    declaringTypeDef
            );
        } else if (Class.class == javaField.getType()) {
            org.metavm.object.type.Field field = createField(javaField, declaringTypeDef, defContext.getType(Klass.class));
            new ClassFieldDef(
                    declaringTypeDef,
                    field,
                    javaField,
                    defContext
            );
        } else {
            var javaFieldType = evaluateFieldType(javaField);
            var fieldType = defContext.getType(javaFieldType);
            var targetMapper = fieldType instanceof PrimitiveType ? null : defContext.getMapper(javaFieldType);
            if (ReflectionUtils.isAnnotatedWithNullable(javaField))
                fieldType = StandardTypes.getNullableType(fieldType);
            org.metavm.object.type.Field field = createField(javaField, declaringTypeDef, fieldType);
            new FieldDef(
                    field,
                    typeFactory.isNullable(field.getType()),
                    javaField,
                    declaringTypeDef,
                    targetMapper
            );
        }
    }

    private void parseUniqueConstraint(Field indexDefField, PojoDef<T> declaringTypeDef) {
        IndexDef<?> indexDef = (IndexDef<?>) ReflectionUtils.get(null, indexDefField);
        var uniqueConstraint = new Index(
                declaringTypeDef.getKlass(),
                EntityUtils.getMetaConstraintName(indexDefField), indexDefField.getName(), null, indexDef.isUnique(), NncUtils.map(indexDef.getFieldNames(), this::getFiled)
        );
        new IndexConstraintDef(
                uniqueConstraint,
                indexDefField,
                declaringTypeDef
        );
    }

    private void parseCheckConstraint(Field constraintField, PojoDef<T> declaringTypeDef) {
        ConstraintDef<?> constraintDef = (ConstraintDef<?>) ReflectionUtils.get(null, constraintField);
        org.metavm.flow.Value value = ValueFactory.create(
                ValueDTO.exprValue(constraintDef.expression()),
                TypeParsingContext.create(declaringTypeDef.getKlass(), defContext)
        );
        CheckConstraint checkConstraint = new CheckConstraint(
                declaringTypeDef.getKlass(), EntityUtils.getMetaConstraintName(constraintField), constraintField.getName(), "", value
        );
        new CheckConstraintDef(
                checkConstraint,
                constraintField,
                declaringTypeDef
        );
    }

    private org.metavm.object.type.Field getFiled(String javaFieldName) {
        Field field = ReflectionUtils.getField(javaClass, javaFieldName);
        return def.getFieldDef(field).getField();
    }

    protected org.metavm.object.type.Field createField(Field javaField,
                                                       PojoDef<?> declaringTypeDef,
                                                       Type fieldType) {
        EntityField annotation = javaField.getAnnotation(EntityField.class);
        boolean unique = annotation != null && annotation.unique();
        boolean asTitle = annotation != null && annotation.asTitle();
        boolean isChild = javaField.isAnnotationPresent(ChildEntity.class);
        boolean lazy = isChild && javaField.getAnnotation(ChildEntity.class).lazy();
        var declaringType = declaringTypeDef.getKlass();
        var field = FieldBuilder.newBuilder(
                        EntityUtils.getMetaFieldName(javaField),
                        javaField.getName(), declaringType, fieldType)
                .unique(unique)
                .lazy(lazy)
                .readonly(Modifier.isFinal(javaField.getModifiers()))
                .column(columnStore.getColumn(javaType, javaField, fieldType.getSQLType()))
                .defaultValue(new NullInstance(StandardTypes.getNullType()))
                .isChild(isChild)
                .staticValue(new NullInstance(StandardTypes.getNullType()))
                .access(parseAccess(javaField.getModifiers()))
                .build();
        if (asTitle)
            declaringType.setTitleField(field);
        return field;
    }

    private Access parseAccess(int modifiers) {
        if (isSystemAPI()) {
            if (Modifier.isPublic(modifiers))
                return Access.PUBLIC;
            if (Modifier.isProtected(modifiers))
                return Access.PROTECTED;
            if (Modifier.isPrivate(modifiers))
                return Access.PRIVATE;
            return Access.PACKAGE;
        } else
            return Access.PUBLIC;
    }

    private java.lang.reflect.Type evaluateFieldType(Field javaField) {
        return ReflectionUtils.evaluateFieldType(javaType, javaField.getGenericType());
    }

    protected Klass createType() {
        var templateDef = javaType != javaClass ? defContext.getPojoDef(javaClass, INIT) : null;
        PojoDef<? super T> superDef = getSuperDef();
        List<InterfaceDef<? super T>> interfaceDefs = getInterfaceDefs();
        return KlassBuilder.newBuilder(Types.getTypeName(javaType), Types.getTypeCode(javaType))
                .kind(ClassKind.fromTypeCategory(getTypeCategory()))
                .source(ClassSource.BUILTIN)
                .template(NncUtils.get(templateDef, PojoDef::getKlass))
                .superClass(NncUtils.get(superDef, PojoDef::getType))
                .interfaces(NncUtils.map(interfaceDefs, InterfaceDef::getType))
                .tag(defContext.getTypeTag(javaClass))
                .build();
    }

    private List<java.lang.reflect.Type> getTypeArguments() {
        if (javaType instanceof java.lang.reflect.ParameterizedType pType) {
            return List.of(pType.getActualTypeArguments());
        } else {
            return List.of();
        }
    }

    protected final java.lang.reflect.Type getJavaType() {
        return javaType;
    }

    protected abstract TypeCategory getTypeCategory();

    protected Parameter createParameter(java.lang.reflect.Parameter javaParameter) {
        var type = defContext.getType(javaParameter.getParameterizedType());
        if (ReflectionUtils.isAnnotatedWithNullable(javaParameter))
            type = StandardTypes.getNullableType(type);
        return new Parameter(
                null,
                javaParameter.getName(),
                javaParameter.getName(),
                type
        );
    }

    protected boolean isSystemAPI() {
        var annotation = javaClass.getAnnotation(EntityType.class);
        return annotation != null && annotation.systemAPI();
    }

}
