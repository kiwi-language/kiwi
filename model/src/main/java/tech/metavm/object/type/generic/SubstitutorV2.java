package tech.metavm.object.type.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.GenericElementDTO;
import tech.metavm.object.view.FieldsObjectMapping;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.type.ResolutionStage.*;

public class SubstitutorV2 extends CopyVisitor {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static SubstitutorV2 create(Object root,
                                       List<TypeVariable> typeParameters,
                                       List<? extends Type> typeArguments,
                                       ResolutionStage stage) {
        return new SubstitutorV2(
                root, typeParameters, typeArguments, stage
        );
    }

    private final TypeSubstitutor typeSubstitutor;
    private final ResolutionStage stage;
    private final Map<String, String> copyTmpIds = new HashMap<>();
    private final Map<Object, Object> existingCopies = new HashMap<>();

    public SubstitutorV2(Object root,
                         List<TypeVariable> typeParameters,
                         List<? extends Type> typeArguments,
                         ResolutionStage stage) {
        super(root);
        if(typeParameters.size() != typeArguments.size()) {
            logger.info("#type parameters != #type arguments. root: {}", EntityUtils.getEntityDesc(root));
        }
        if (DebugEnv.debugging) {
            if (root instanceof Klass ct && ct.getTypeDesc().equals("Node") && typeArguments.get(0).getTypeDesc().equals("MyList_T") && stage == DEFINITION)
                System.out.println("Caught");
            debugLogger.info("substituting {}, type parameters: {}, type arguments: {}, stage: {}",
                    EntityUtils.getEntityDesc(root), NncUtils.map(typeParameters, TypeVariable::getTypeDesc),
                    NncUtils.map(typeArguments, Type::getTypeDesc), stage.name());
        }
        this.typeSubstitutor = new TypeSubstitutor(NncUtils.map(typeParameters, TypeVariable::getType), typeArguments);
        this.stage = stage;
//        NncUtils.biForEach(typeParameters, typeArguments, this::addCopy);
        /*var rootDTO = switch (root) {
            case Klass classType ->
                    dtoProvider.getPTypeDTO(classType.getStringId(), NncUtils.map(typeArguments, Entity::getStringId));
            case Flow flow ->
                    dtoProvider.getParameterizedFlowDTO(flow.getStringId(), NncUtils.map(typeArguments, Entity::getStringId));
            default -> throw new IllegalStateException("Unexpected root: " + root);
        };
        if (rootDTO != null)
            rootDTO.forEachDescendant(this::addCopyTmpId); */
        Object existingRoot = switch (root) {
            case Flow flow -> flow.getEffectiveHorizontalTemplate().getExistingParameterized(
                    NncUtils.map(NncUtils.map(flow.getTypeParameters(), TypeVariable::getType), this::substituteType));
            case Klass klass -> klass.getEffectiveTemplate().getExistingParameterized(
                    NncUtils.map(klass.getTypeArguments(), this::substituteType));
            default -> throw new IllegalStateException("Unexpected root: " + root);
        };
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
            });
        }
//        if(root instanceof Flow flow && flow.getName().equals("findRequired")) {
//            debugLogger.info("Substituting {}, with type arguments: {}", EntityUtils.getEntityDesc(root),
//                    NncUtils.join(typeArguments, EntityUtils::getEntityDesc));
//            existingCopies.forEach((s, t) -> {
//                debugLogger.info("source: {}, target: {}", EntityUtils.getEntityDesc(s), EntityUtils.getEntityDesc(t));
//            });
//        }
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
            return NncUtils.requireNonNull(type.findSelfField(f -> f.getCopySource() == field.getEffectiveTemplate()));
    }

    private Method substituteMethod(Method method) {
        var type = (Klass) substituteClass(method.getDeclaringType());
        Method subst;
        if (type == method.getDeclaringType())
            subst = method;
        else
            subst = NncUtils.requireNonNull(type.findSelfMethod(
                    m -> m.getEffectiveVerticalTemplate() == method.getEffectiveVerticalTemplate()));
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
            var typeArgs = NncUtils.map(NncUtils.map(method.getTypeParameters(), TypeVariable::getType), this::substituteType);
            var copy = (Method) getExistingCopy(method);
            if (copy == null) {
                copy = MethodBuilder
                        .newBuilder(currentClass(), method.getName(), method.getCode())
                        .tmpId(getCopyTmpId(method))
                        .horizontalTemplate(method)
                        .isSynthetic(method.isSynthetic())
                        .access(method.getAccess())
                        .isStatic(method.isStatic())
                        .typeArguments(typeArgs)
                        .build();
                if (method.isEphemeralEntity() || NncUtils.anyMatch(typeArgs, Entity::isEphemeralEntity))
                    copy.setEphemeralEntity(true);
                method.addParameterized(copy);
            }
            copy.setStage(stage);
            copy.setAbstract(method.isAbstract());
            copy.setNative(method.isNative());
            copy.setConstructor(method.isConstructor());
            addCopy(method, copy);
            addCopy(method.getRootScope(), copy.getRootScope());
            enterElement(copy);
            for (Method overridden : method.getOverridden())
                NncUtils.biForEach(NncUtils.map(overridden.getTypeParameters(), TypeVariable::getType), copy.getTypeArguments(), typeSubstitutor::addMapping);
            copy.setParameters(NncUtils.map(method.getParameters(), p -> (Parameter) copy(p)));
            copy.setReturnType(substituteType(method.getReturnType()));
            processFlowBody(method, copy);
            exitElement();
            return copy;
        } else
            return super.visitFlow(method);
    }

    @Override
    public Element visitFunction(Function function) {
        if (function == getRoot()) {
            var typeArgs = NncUtils.map(function.getEffectiveTypeArguments(), this::substituteType);
            var copy = (Function) getExistingCopy(function);
            if (copy == null) {
                var name = Types.getParameterizedName(function.getName(), typeArgs);
                var code = Types.getParameterizedCode(function.getCode(), typeArgs);
                copy = FunctionBuilder
                        .newBuilder(name, code)
                        .tmpId(getCopyTmpId(function))
                        .horizontalTemplate(function)
                        .typeArguments(typeArgs)
                        .isSynthetic(function.isSynthetic())
                        .build();
                if (function.isEphemeralEntity() || NncUtils.anyMatch(typeArgs, Entity::isEphemeralEntity))
                    copy.setEphemeralEntity(true);
                function.addParameterized(copy);
            }
            copy.setStage(stage);
            copy.setNative(function.isNative());
            addCopy(function, copy);
            if (function.isRootScopePresent())
                addCopy(function.getRootScope(), copy.getRootScope());
            enterElement(copy);
            copy.setParameters(NncUtils.map(function.getParameters(), p -> (Parameter) copy(p)));
            copy.setReturnType(substituteType(function.getReturnType()));
            processFlowBody(function, copy);
            exitElement();
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
            copy.setCapturedTypeVariables(NncUtils.map(flow.getCapturedTypeVariables(), ct -> (CapturedTypeVariable) copy(ct)));
//            for (var ct : flow.getCapturedTypeVariables()) {
//                var ctCopy = (CapturedType) getCopy(ct);
//                ctCopy.setCapturedCompositeTypes(NncUtils.map(ct.getCapturedCompositeTypes(), this::substituteType));
//                ctCopy.setCapturedFlows(NncUtils.map(ct.getCapturedFlows(), this::substituteFlow));
//            }
            for (NodeRT node : flow.getRootScope().getNodes())
                copy.getRootScope().addNode((NodeRT) copy(node));
            for (Type capturedCompositeType : flow.getCapturedCompositeTypes())
                copy.addCapturedCompositeType((Type) copy(capturedCompositeType));
            for (Flow capturedFlow : flow.getCapturedFlows())
                copy.addCapturedFlow((Flow) copy(capturedFlow));
        }
    }

    @Override
    public Type visitType(Type type) {
        return type.accept(typeSubstitutor);
    }

    @Override
    public Element visitCapturedTypeVariable(CapturedTypeVariable type) {
        var copy = (CapturedTypeVariable) getExistingCopy(type);
        if (copy == null) {
            copy = new CapturedTypeVariable(
                    (UncertainType) substituteType(type.getUncertainType()),
                    (CapturedTypeScope) getCopy(type.getScope())
            );
            copy.setCopySource(type);
        }
        addCopy(type, copy);
        typeSubstitutor.addMapping(type.getType(), copy.getType());
        return copy;
    }

    @Override
    public Element visitKlass(Klass klass) {
        if (klass == getRoot()) {
            var copy = (Klass) getExistingCopy(klass);
            var template = klass.getEffectiveTemplate();
            var typeArguments = NncUtils.map(klass.getTypeArguments(), this::substituteType);
            var name = Types.getParameterizedName(template.getName(), typeArguments);
            if (copy == null) {
                copy = ClassTypeBuilder.newBuilder(name, null)
                        .kind(klass.getKind())
                        .typeArguments(typeArguments)
                        .anonymous(true)
                        .ephemeral(klass.isEphemeral())
                        .template(template)
                        .tmpId(getCopyTmpId(template))
                        .build();
                if (klass.isEphemeralEntity() || NncUtils.anyMatch(typeArguments, Entity::isEphemeralEntity))
                    copy.setEphemeralEntity(true);
                klass.addParameterized(copy);
            } else {
                copy.setName(name);
            }
            addCopy(klass, copy);
            var curStage = copy.setStage(stage);
            if (stage.isAfterOrAt(SIGNATURE) && curStage.isBefore(SIGNATURE)) {
                if (klass.getSuperType() != null)
                    copy.setSuperType((ClassType) substituteType(klass.getSuperType()));
                copy.setInterfaces(NncUtils.map(klass.getInterfaces(), t -> (ClassType) substituteType(t)));
                copy.setDependencies(NncUtils.map(klass.getDependencies(), this::substituteClass));
            }
            enterElement(copy);
            if (stage.isAfterOrAt(DECLARATION) && curStage.isBefore(DEFINITION)) {
                copy.setFields(NncUtils.map(klass.getFields(), field -> (Field) copy(field)));
                copy.setStaticFields(NncUtils.map(klass.getStaticFields(), field -> (Field) copy(field)));
                copy.setMethods(NncUtils.map(klass.getMethods(), method -> (Method) copy(method)));
                if (klass.getTitleField() != null)
                    copy.setTitleField((Field) getValue(klass.getTitleField(), v -> {
                    }));
                copy.setMappings(NncUtils.map(klass.getMappings(), m -> (ObjectMapping) copy(m)));
                if (klass.getDefaultMapping() != null)
                    copy.setDefaultMapping((FieldsObjectMapping) getValue(klass.getDefaultMapping(), v -> {
                    }));
            }
            if (stage.isAfterOrAt(DEFINITION) && curStage.isBefore(DEFINITION)) {
//                copy.setMappings(NncUtils.map(type.getMappings(), m -> (ObjectMapping) copy(m)));
//                copy.setArrayMappings(NncUtils.map(type.getArrayMappings(), m -> (ArrayMapping) copy(m)));
//                if (type.getDefaultMapping() != null)
//                    copy.setDefaultMapping((FieldsObjectMapping) getValue(type.getDefaultMapping(), v -> {
//                    }));
            }
            exitElement();
            if (klass == root && stage.isAfterOrAt(DEFINITION))
                check();
            return copy;
        } else {
            return super.visitKlass(klass);
        }
    }

}
