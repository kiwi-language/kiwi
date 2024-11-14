package org.metavm.object.type.generic;

import org.metavm.entity.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.GenericElementDTO;
import org.metavm.object.view.FieldsObjectMapping;
import org.metavm.object.view.ObjectMapping;
import org.metavm.object.view.ObjectMappingRef;
import org.metavm.util.DebugEnv;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static org.metavm.object.type.ResolutionStage.*;

public class SubstitutorV2 extends CopyVisitor {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static SubstitutorV2 create(Object root,
                                       List<TypeVariable> typeParameters,
                                       List<? extends Type> typeArguments,
                                       ResolutionStage stage) {
        return new SubstitutorV2(
                root, typeParameters, typeArguments, null, stage
        );
    }

    private final TypeSubstitutor typeSubstitutor;
    private final ResolutionStage stage;
    private final Map<String, String> copyTmpIds = new HashMap<>();
    private final Map<Object, Object> existingCopies = new IdentityHashMap<>();

    public SubstitutorV2(Object root,
                         List<TypeVariable> typeParameters,
                         List<? extends Type> typeArguments,
                         Object existingRoot,
                         ResolutionStage stage) {
        super(root, true);
        if (typeParameters.size() != typeArguments.size()) {
            logger.info("#type parameters != #type arguments. root: {}", EntityUtils.getEntityDesc(root));
        }
        if (DebugEnv.debugging) {
            debugLogger.info("substituting {}, type parameters: {}, type arguments: {}, stage: {}",
                    EntityUtils.getEntityDesc(root), NncUtils.map(typeParameters, TypeVariable::getTypeDesc),
                    NncUtils.map(typeArguments, Type::getTypeDesc), stage.name());
        }
        this.typeSubstitutor = new TypeSubstitutor(NncUtils.map(typeParameters, TypeVariable::getType), typeArguments);
        this.stage = stage;
//        Object existingRoot = switch (root) {
//            case Flow flow -> flow.getEffectiveHorizontalTemplate().getExistingParameterized(
//                    NncUtils.map(NncUtils.map(flow.getTypeParameters(), TypeVariable::getType), this::substituteType));
//            case Klass klass -> klass.getEffectiveTemplate().getExistingParameterized(
//                    NncUtils.map(klass.getTypeArguments(), this::substituteType));
//            default -> throw new IllegalStateException("Unexpected root: " + root);
//        };
        if (existingRoot != null) {
            addExistingCopy(root, existingRoot);
            EntityUtils.forEachDescendant(existingRoot, d -> {
                var temp = d == existingRoot ? root : (
                        d instanceof GenericElement genericElement ? genericElement.getCopySource() : null
                );
                if (temp != null) {
                    var parentTemp = ((Entity) temp).getParentEntity();
                    if (existingRoot == d || parentTemp == null || existingCopies.containsKey(parentTemp)) {
                        addExistingCopy(temp, d);
                        var childMap = ((Entity) d).getChildMap();
                        var tempChildMap = ((Entity) temp).getChildMap();
                        childMap.forEach((field, child) -> {
                            var tempChild = tempChildMap.get(field);
                            if (tempChild != null
                                    && EntityUtils.getRealType(tempChild) == EntityUtils.getRealType(child.getClass()))
                                addExistingCopy(tempChild, child);
                        });
                    }
                }
            }, true);
        }
    }


    private void addCopyTmpId(GenericElementDTO member) {
        if (member.getTemplateId() != null && member.getId() != null)
            this.copyTmpIds.put(member.getTemplateId(), member.getId());
    }

    public Type substituteType(Type type) {
        return type.accept(typeSubstitutor);
    }

    public Klass substituteClass(Klass klass) {
        return klass.getEffectiveTemplate().getParameterized(NncUtils.map(klass.getEffectiveTypeArguments(), this::substituteType));
    }

    private Field substituteField(Field field) {
        var type = (Klass) substituteClass(field.getDeclaringType());
        if (type == field.getDeclaringType())
            return field;
        else
            return Objects.requireNonNull(type.findSelfField(f -> f.getEffectiveTemplate() == field.getEffectiveTemplate()),
                    () -> "Cannot find field with template " + field.getEffectiveTemplate().getQualifiedName() + " in klass " + type.getQualifiedName() +
                    ", root: " + root + ", type arguments: " + typeSubstitutor.getVariableMap());
    }

    private Method substituteMethod(Method method) {
        var type = (Klass) substituteClass(method.getDeclaringType());
        Method subst;
        if (type == method.getDeclaringType())
            subst = method;
        else {
            var found = type.findSelfMethod(
                    m -> m.getEffectiveVerticalTemplate() == method.getUltimateTemplate());
            if(found == null) {
                logger.debug("Substituted method: {}", method.getQualifiedSignature());
                type.forEachMethod(m -> logger.debug("Method: {}", m.getQualifiedSignature()));
            }
            subst = Objects.requireNonNull(found,
                    () -> "Cannot find method with vertical template " + method.getUltimateTemplate().getQualifiedSignature()
                            + " in klass " + type.getTypeDesc()
                            + ", root: " + root + ", type arguments: " + typeSubstitutor.getVariableMap()
            );
        }
        var typeArgs = NncUtils.map(method.getTypeArguments(), this::substituteType);
        if (subst.getTypeArguments().equals(typeArgs))
            return subst;
        return subst.getEffectiveHorizontalTemplate().getParameterized(typeArgs);
    }

    private Function substituteFunction(Function function) {
        var typeArgs = NncUtils.map(function.getTypeArguments(), this::substituteType);
        if (function.getTypeArguments().equals(typeArgs))
            return function;
        return function.getEffectiveHorizontalTemplate().getParameterized(typeArgs);
    }

    private Parameter substituteParameter(Parameter parameter) {
        var callable = (Callable) substituteReference(parameter.getCallable());
        if (callable == parameter.getCallable())
            return parameter;
        else
            return NncUtils.findRequired(callable.getParameters(),
                    p -> p.getSelfOrCopySource() == parameter.getSelfOrCopySource());
    }

    private ObjectMapping substituteObjectMapping(ObjectMapping objectMapping) {
        var sourceClass = (Klass) substituteClass(objectMapping.getSourceKlass());
        if (sourceClass == objectMapping.getSourceKlass())
            return objectMapping;
        else {
            return NncUtils.findRequired(
                    sourceClass.getMappings(),
                    m -> m.getSelfOrCopySource() == objectMapping.getSelfOrCopySource()
            );
        }
    }

    @Override
    protected Object substituteReference(Object reference) {
        return switch (reference) {
            case Type type -> substituteType(type);
            case Field field -> substituteField(field);
            case Flow flow -> substituteFlow(flow);
            case Parameter parameter -> substituteParameter(parameter);
            case ObjectMapping objectMapping -> substituteObjectMapping(objectMapping);
            case null, default -> super.substituteReference(reference);
        };
    }

    protected Flow substituteFlow(Flow flow) {
        return switch (flow) {
            case Method method -> substituteMethod(method);
            case Function function -> substituteFunction(function);
            default -> throw new IllegalStateException("Unexpected flow: " + flow);
        };
    }

//    @Override
//    public Element visitCompositeType(CompositeType type) {
//        var copy = (CompositeType) super.visitCompositeType(type);
//        if(!entityRepository.containsEntity(copy))
//            entityRepository.bind(copy);
//        return copy;
//    }

    @Override
    protected @Nullable Object getExistingCopy(Object object) {
        return existingCopies.get(object);
    }

    @Override
    protected @Nullable Long getCopyTmpId(Object object) {
        if (object instanceof Entity entity) {
            var id = NncUtils.get(copyTmpIds.get(entity.getStringId()), Id::parse);
            if (id instanceof TmpId tmpId)
                return tmpId.getTmpId();
        }
        return null;
    }

    @Override
    protected Object allocateCopy(Object entity) {
        if (entity instanceof GenericElement genericElement) {
            var genericElementCopy = ((GenericElement) super.allocateCopy(entity));
            genericElementCopy.setCopySource(genericElement);
            return genericElementCopy;
        } else
            return super.allocateCopy(entity);
    }

    @Override
    public Element visitMethod(Method method) {
        if (method == getRoot()) {
            var copy = (Method) Objects.requireNonNull(getExistingCopy(method));
            copy.setStage(stage);
            copy.setAbstract(method.isAbstract());
            copy.setNative(method.isNative());
            if(method.isNative())
                copy.setJavaMethod(method.getJavaMethod());
            copy.setConstructor(method.isConstructor());
            addCopy(method, copy);
            if(method.isRootScopePresent())
                addCopy(method.getCode(), copy.getCode());
            enterElement(copy);
            copy.setParameters(NncUtils.map(method.getParameters(), p -> (Parameter) copy0(p)));
            copy.setReturnType(substituteType(method.getReturnType()));
            copy.setLambdas(NncUtils.map(method.getLambdas(), l -> (Lambda) copy0(l)));
            processFlowBody(method, copy);
            exitElement();
            check();
            return copy;
        } else
            return super.visitFlow(method);
    }

    @Override
    public Element visitFunction(Function function) {
        if (function == getRoot()) {
            var typeArgs = NncUtils.map(function.getEffectiveTypeArguments(), this::substituteType);
            var copy = (Function) Objects.requireNonNull(getExistingCopy(function));
            copy.setStage(stage);
            copy.setNative(function.isNative());
            if(copy.isNative())
                copy.setNativeCode(function.getNativeCode());
            addCopy(function, copy);
            if (function.isRootScopePresent())
                addCopy(function.getCode(), copy.getCode());
            enterElement(copy);
            copy.setParameters(NncUtils.map(function.getParameters(), p -> (Parameter) copy0(p)));
            copy.setReturnType(substituteType(function.getReturnType()));
            copy.setLambdas(NncUtils.map(function.getLambdas(), l -> (Lambda) copy0(l)));
            processFlowBody(function, copy);
            exitElement();
            check();
            return copy;
        } else
            return super.visitFunction(function);
    }


    private void addExistingCopy(Object original, Object copy) {
        existingCopies.put(original, copy);
    }

    private void processFlowBody(Flow flow, Flow copy) {
        if (stage.isAfterOrAt(DEFINITION) && flow.isRootScopePresent()) {
            copy.clearContent();
            copy.setCapturedTypeVariables(NncUtils.map(flow.getCapturedTypeVariables(), ct -> (CapturedTypeVariable) copy0(ct)));
            for (Type capturedCompositeType : flow.getCapturedCompositeTypes())
                copy.addCapturedCompositeType((Type) copy0(capturedCompositeType));
            for (Flow capturedFlow : flow.getCapturedFlows())
                copy.addCapturedFlow((Flow) copy0(capturedFlow));
            for (CpEntry cpEntry : flow.getConstantPool().getEntries())
                copy.getConstantPool().addEntry((CpEntry) copy0(cpEntry));
            copy.getCode().setCode(flow.getCode().getCode());
            copy.getCode().setMaxLocals(flow.getCode().getMaxLocals());
            copy.getCode().setMaxStack(flow.getCode().getMaxStack());
        }
    }

    @Override
    public Element visitMethodRef(MethodRef methodRef) {
        var isRootRef = root instanceof Method m && m.getRef().equals(methodRef);
        return new MethodRef(
                (ClassType) methodRef.getDeclaringType().accept(this),
                methodRef.getRawFlow(),
                isRootRef || methodRef.isParameterized() ?
                        NncUtils.map(methodRef.getTypeArguments(), t -> (Type) t.accept(this)) : List.of()
        );
    }

    @Override
    public Element visitFieldRef(FieldRef fieldRef) {
        var rawField = fieldRef.getRawField();
        if (rawField.getDeclaringType() == root)
            return new FieldRef((ClassType) fieldRef.getDeclaringType().accept(this), rawField);
        else
            return super.visitFieldRef(fieldRef);
    }

    @Override
    public Element visitObjectMappingRef(ObjectMappingRef objectMappingRef) {
        var rawMapping = objectMappingRef.getRawMapping();
        if (rawMapping.getSourceKlass() == root)
            return new ObjectMappingRef((ClassType) objectMappingRef.getDeclaringType().accept(this), rawMapping);
        else
            return super.visitObjectMappingRef(objectMappingRef);
    }

    @Override
    public Element visitParameterRef(ParameterRef parameterRef) {
        var rawParam = parameterRef.getRawParameter();
        if (rawParam.getCallable() == root || rawParam.getCallable() instanceof Method m && m.getDeclaringType() == root)
            return new ParameterRef((CallableRef) parameterRef.getCallableRef().accept(this), rawParam);
        else
            return super.visitParameterRef(parameterRef);
    }

    @Override
    public Element visitClassType(ClassType type) {
        var klass = type.getKlass();
        if (klass == root)
            return new ClassType(type.getKlass(), NncUtils.map(type.getTypeArguments(), t -> (Type) t.accept(this)));
        else {
            var copy = (ClassType) super.visitClassType(type);
            copy = copy.trySimplify();
            return copy;
        }
    }

    @Override
    public Element visitVariableType(VariableType type) {
        var mapped = typeSubstitutor.getVariableMap().get(type);
        if (mapped != null)
            return mapped;
        return new VariableType(
                (GenericDeclarationRef) ((ValueElement)type.getGenericDeclarationRef()).accept(this),
                type.getRawVariable()
        );
    }

    @Override
    public Element visitPrimitiveType(PrimitiveType type) {
        return type;
    }

    @Override
    public Element visitAnyType(AnyType type) {
        return type;
    }

    @Override
    public Element visitNeverType(NeverType type) {
        return type;
    }

    @Override
    public Type visitType(Type type) {
        var mapped = typeSubstitutor.getVariableMap().get(type);
        if (mapped != null)
            return mapped;
        return (Type) super.visitType(type);
    }

    @Override
    public Element visitKlass(Klass klass) {
        if (klass == getRoot()) {
            var copy = (Klass) Objects.requireNonNull(getExistingCopy(klass));
            var template = klass.getEffectiveTemplate();
            var typeArguments = NncUtils.map(klass.getTypeArguments(), this::substituteType);
            var name = Types.getParameterizedName(template.getName(), typeArguments);
            copy.setName(name);
            addCopy(klass, copy);
            var curStage = copy.setStage(stage);
            if (stage.isAfterOrAt(SIGNATURE) && curStage.isBefore(SIGNATURE)) {
                if (klass.getSuperType() != null)
                    copy.setSuperType((ClassType) substituteType(klass.getSuperType()));
                copy.setInterfaces(NncUtils.map(klass.getInterfaces(), t -> (ClassType) substituteType(t)));
            }
            enterElement(copy);
            if (stage.isAfterOrAt(DECLARATION) && curStage.isBefore(DEFINITION)) {
                copy.setFields(NncUtils.map(klass.getFields(), field -> (Field) copy0(field)));
                copy.setStaticFields(NncUtils.map(klass.getStaticFields(), field -> (Field) copy0(field)));
                copy.setMethods(NncUtils.map(klass.getMethods(), method -> (Method) copy0(method)));
                if (klass.getTitleField() != null)
                    copy.setTitleField((Field) getValue(klass.getTitleField(), v -> {
                    }));
                copy.setMappings(NncUtils.map(klass.getMappings(), m -> (ObjectMapping) copy0(m)));
                if (klass.getDefaultMapping() != null)
                    copy.setDefaultMapping((FieldsObjectMapping) getValue(klass.getDefaultMapping(), v -> {
                    }));
            }
            if (stage.isAfterOrAt(DEFINITION) && curStage.isBefore(DEFINITION)) {
//                copy.setMappings(NncUtils.map(type.getMappings(), m -> (ObjectMapping) copy0(m)));
//                copy.setArrayMappings(NncUtils.map(type.getArrayMappings(), m -> (ArrayMapping) copy0(m)));
//                if (type.getDefaultMapping() != null)
//                    copy.setDefaultMapping((FieldsObjectMapping) getValue(type.getDefaultMapping(), v -> {
//                    }));
            }
            exitElement();
            check();
            copy.rebuildMethodTable();
//            if(stage.isAfterOrAt(DEFINITION))
//                copy.resolveConstantPool();
            return copy;
        } else {
            return super.visitKlass(klass);
        }
    }

}
