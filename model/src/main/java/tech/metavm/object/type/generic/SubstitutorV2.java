package tech.metavm.object.type.generic;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.GenericElementDTO;
import tech.metavm.object.view.FieldsObjectMapping;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.type.ResolutionStage.*;

public class SubstitutorV2 extends CopyVisitor {

    public static SubstitutorV2 create(Object root,
                                       List<TypeVariable> typeParameters,
                                       List<? extends Type> typeArguments,
                                       ResolutionStage stage, IEntityContext entityContext,
                                       DTOProvider dtoProvider) {
        var compositeTypeFacade = new CompositeTypeFacadeImpl(
                new ContextArrayTypeProvider(entityContext),
                entityContext.getFunctionTypeContext(),
                entityContext.getUnionTypeContext(),
                entityContext.getIntersectionTypeContext(),
                entityContext.getUncertainTypeContext(),
                entityContext.getGenericContext()
        );
        return new SubstitutorV2(
                root, typeParameters, typeArguments, stage,
                entityContext,
                compositeTypeFacade,
                entityContext.getGenericContext(),
                entityContext.getGenericContext(),
                dtoProvider
        );
    }

    private final TypeSubstitutor typeSubstitutor;
    private final CompositeTypeFacade compositeTypeFacade;
    private final ParameterizedTypeProvider parameterizedTypeProvider;
    private final ParameterizedFlowProvider parameterizedFlowProvider;
    private final EntityRepository entityRepository;
    private final ResolutionStage stage;
    private final Map<RefDTO, RefDTO> copyTmpIds = new HashMap<>();
    private final Map<Object, Object> existingCopies = new HashMap<>();

    public SubstitutorV2(Object root,
                         List<TypeVariable> typeParameters,
                         List<? extends Type> typeArguments,
                         ResolutionStage stage,
                         EntityRepository entityRepository,
                         CompositeTypeFacade compositeTypeFacade,
                         ParameterizedTypeProvider parameterizedTypeProvider,
                         ParameterizedFlowProvider parameterizedFlowProvider,
                         DTOProvider dtoProvider) {
        super(root);
        this.entityRepository = entityRepository;
        this.compositeTypeFacade = compositeTypeFacade;
        this.parameterizedTypeProvider = parameterizedTypeProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        this.typeSubstitutor = new TypeSubstitutor(typeParameters, typeArguments, compositeTypeFacade, dtoProvider);
        this.stage = stage;
        NncUtils.biForEach(typeParameters, typeArguments, this::addCopy);
        var rootDTO = switch (root) {
            case ClassType classType ->
                    dtoProvider.getPTypeDTO(classType.getRef(), NncUtils.map(typeArguments, Entity::getRef));
            case Flow flow ->
                    dtoProvider.getParameterizedFlowDTO(flow.getRef(), NncUtils.map(typeArguments, Entity::getRef));
            default -> throw new IllegalStateException("Unexpected root: " + root);
        };
        if (rootDTO != null)
            rootDTO.forEachDescendant(this::addCopyTmpId);
        Object existingRoot = switch (root) {
            case Flow flow -> parameterizedFlowProvider.getExistingFlow(flow.getEffectiveHorizontalTemplate(),
                    NncUtils.map(flow.getTypeParameters(), this::substituteType));
            case ClassType type -> parameterizedTypeProvider.getExisting(type.getEffectiveTemplate(),
                    NncUtils.map(type.getTypeArguments(), this::substituteType));
            default -> throw new IllegalStateException("Unexpected root: " + root);
        };
        if (existingRoot != null) {
            existingCopies.put(root, existingRoot);
            EntityUtils.forEachDescendant(existingRoot, d -> {
                if (d instanceof GenericElement genericElement) {
                    var temp = genericElement.getCopySource();
                    if(temp != null) {
                        var parentTemp = ((Entity) temp).getParentEntity();
                        if (existingRoot == d || parentTemp == null || existingCopies.containsKey(parentTemp)) {
                            existingCopies.put(temp, genericElement);
                            var childMap = ((Entity) d).getChildMap();
                            var tempChildMap = ((Entity) temp).getChildMap();
                            childMap.forEach((field, child) -> {
                                var tempChild = tempChildMap.get(field);
                                if (tempChild != null
                                        && EntityUtils.getRealType(tempChild) == EntityUtils.getRealType(child.getClass()))
                                    existingCopies.put(tempChild, child);
                            });
                        }
                    }
                }
            });
        }
    }

    private void addCopyTmpId(GenericElementDTO member) {
        if (member.getTemplateRef() != null && member.getRef() != null)
            this.copyTmpIds.put(member.getTemplateRef(), member.getRef());
    }

    public Type substituteType(Type type) {
        return type.accept(typeSubstitutor);
    }

    private Field substituteField(Field field) {
        var type = (ClassType) substituteType(field.getDeclaringType());
        if (type == field.getDeclaringType())
            return field;
        else
            return NncUtils.requireNonNull(type.findField(f -> f.getCopySource() == field.getEffectiveTemplate()));
    }

    private Method substituteMethod(Method method) {
        var type = (ClassType) substituteType(method.getDeclaringType());
        Method subst;
        if (type == method.getDeclaringType())
            subst = method;
        else
            subst = NncUtils.requireNonNull(type.findMethod(
                    m -> m.getEffectiveVerticalTemplate() == method.getEffectiveVerticalTemplate()));
        var typeArgs = NncUtils.map(method.getTypeArguments(), this::substituteType);
        if (subst.getTypeArguments().equals(typeArgs))
            return subst;
        return parameterizedFlowProvider.getParameterizedFlow(subst.getEffectiveHorizontalTemplate(), typeArgs);
    }

    private Function substituteFunction(Function function) {
        var typeArgs = NncUtils.map(function.getTypeArguments(), this::substituteType);
        if (function.getTypeArguments().equals(typeArgs))
            return function;
        return parameterizedFlowProvider.getParameterizedFlow(function.getEffectiveHorizontalTemplate(), typeArgs);
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
        var sourceType = (ClassType) substituteType(objectMapping.getSourceType());
        if(sourceType == objectMapping.getSourceType())
            return objectMapping;
        else {
            return NncUtils.findRequired(
                    sourceType.getMappings(),
                    m -> m.getSelfOrCopySource() == objectMapping.getSelfOrCopySource()
            );
        }
    }

    @Override
    protected Object substituteReference(Object reference) {
        return switch (reference) {
            case Type type -> substituteType(type);
            case Field field -> substituteField(field);
            case Method method -> substituteMethod(method);
            case Function function -> substituteFunction(function);
            case Parameter parameter -> substituteParameter(parameter);
            case ObjectMapping objectMapping -> substituteObjectMapping(objectMapping);
            case null, default -> super.substituteReference(reference);
        };
    }

    @Override
    protected @Nullable Object getExistingCopy(Object object) {
        return existingCopies.get(object);
    }

    @Override
    protected @Nullable Long getCopyTmpId(Object object) {
        return object instanceof Entity entity ?
                NncUtils.get(copyTmpIds.get(entity.getRef()), RefDTO::tmpId) : null;
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
            var typeArgs = NncUtils.map(method.getTypeParameters(), this::substituteType);
            var copy = (Method) getExistingCopy(method);
            if (copy == null) {
                copy = MethodBuilder
                        .newBuilder(currentClass(), method.getName(), method.getCode(), compositeTypeFacade)
                        .tmpId(getCopyTmpId(method))
                        .horizontalTemplate(method)
                        .isSynthetic(method.isSynthetic())
                        .access(method.getAccess())
                        .isStatic(method.isStatic())
                        .typeArguments(typeArgs)
                        .build();
                entityRepository.bind(copy);
            }
            copy.setStage(stage);
            copy.setAbstract(method.isAbstract());
            copy.setNative(method.isNative());
            copy.setConstructor(method.isConstructor());
            addCopy(method, copy);
            addCopy(method.getRootScope(), copy.getRootScope());
            enterElement(copy);
            copy.update(
                    NncUtils.map(method.getParameters(), p -> (Parameter) copy(p)),
                    substituteType(method.getReturnType()),
                    NncUtils.map(method.getOverridden(), this::substituteMethod),
                    compositeTypeFacade
            );
            if (stage.isAfterOrAt(DEFINITION)) {
                copy.getRootScope().clearNodes();
                for (NodeRT node : method.getRootScope().getNodes())
                    copy.getRootScope().addNode((NodeRT) copy(node));
            }
            exitElement();
            return copy;
        } else
            return super.visitFlow(method);
    }

    @Override
    public Element visitFunction(Function function) {
        if (function == getRoot()) {
            var typeArgs = NncUtils.map(function.getTypeParameters(), this::substituteType);
            var copy = (Function) getExistingCopy(function);
            if (copy == null) {
                copy = FunctionBuilder
                        .newBuilder(function.getName(), function.getCode(), compositeTypeFacade)
                        .tmpId(getCopyTmpId(function))
                        .horizontalTemplate(function)
                        .typeArguments(typeArgs)
                        .isSynthetic(function.isSynthetic())
                        .build();
                entityRepository.tryBind(copy);
            }
            copy.setStage(stage);
            copy.setNative(function.isNative());
            addCopy(function, copy);
            if(function.isRootScopePresent())
                addCopy(function.getRootScope(), copy.getRootScope());
            enterElement(copy);
            copy.update(
                    NncUtils.map(function.getParameters(), p -> (Parameter) copy(p)),
                    substituteType(function.getReturnType()),
                    compositeTypeFacade
            );
            if (stage.isAfterOrAt(DEFINITION) && function.isRootScopePresent()) {
                copy.getRootScope().clearNodes();
                for (NodeRT node : function.getRootScope().getNodes())
                    copy.getRootScope().addNode((NodeRT) copy(node));
            }
            exitElement();
            return copy;
        }
        else
            return super.visitFunction(function);
    }

    @Override
    public Element visitClassType(ClassType type) {
        if (type == getRoot()) {
            var copy = (ClassType) getExistingCopy(type);
            var template = type.getEffectiveTemplate();
            var typeArguments = NncUtils.map(type.getTypeArguments(), this::substituteType);
            var name = Types.getParameterizedName(template.getName(), typeArguments);
            var code = Types.getParameterizedCode(template.getCode(), typeArguments);
            if (copy == null) {
                copy = ClassTypeBuilder.newBuilder(name, code)
                        .typeArguments(typeArguments)
                        .anonymous(true)
                        .ephemeral(type.isEphemeral())
                        .template(template)
                        .tmpId(getCopyTmpId(template))
                        .build();
                parameterizedTypeProvider.add(copy);
            } else {
                copy.setName(name);
                copy.setCode(code);
            }
            addCopy(type, copy);
            var curStage = copy.setStage(stage);
            if (stage.isAfterOrAt(SIGNATURE) && curStage.isBefore(SIGNATURE)) {
                if (type.getSuperClass() != null)
                    copy.setSuperClass((ClassType) substituteType(type.getSuperClass()));
                copy.setInterfaces(NncUtils.map(type.getInterfaces(), t -> (ClassType) substituteType(t)));
                copy.setDependencies(NncUtils.map(type.getDependencies(), t -> (ClassType) substituteType(t)));
            }
            enterElement(copy);
            if (stage.isAfterOrAt(DECLARATION) && curStage.isBefore(DEFINITION)) {
                copy.setFields(NncUtils.map(type.getFields(), field -> (Field) copy(field)));
                copy.setStaticFields(NncUtils.map(type.getStaticFields(), field -> (Field) copy(field)));
                copy.setMethods(NncUtils.map(type.getMethods(), method -> (Method) copy(method)));
                if (type.getTitleField() != null)
                    copy.setTitleField((Field) getValue(type.getTitleField(), v -> {
                    }));
                copy.setMappings(NncUtils.map(type.getMappings(), m -> (ObjectMapping) copy(m)));
                if (type.getDefaultMapping() != null)
                    copy.setDefaultMapping((FieldsObjectMapping) getValue(type.getDefaultMapping(), v -> {
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
            if (type == root && stage.isAfterOrAt(DEFINITION))
                check();
            return copy;
        } else
            return super.visitClassType(type);
    }

}
