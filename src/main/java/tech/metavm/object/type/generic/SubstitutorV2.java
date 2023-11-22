package tech.metavm.object.type.generic;

import tech.metavm.entity.CopyVisitor;
import tech.metavm.entity.Element;
import tech.metavm.entity.Entity;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeVariableKey;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import java.util.List;

import static tech.metavm.object.type.ResolutionStage.*;

public class SubstitutorV2 extends CopyVisitor {

    private final GenericContext context;
    private final TypeSubstitutor typeSubstitutor;
    private final SaveTypeBatch batch;
    private final LinkedList<Object> elements = new LinkedList<>();
    private final ResolutionStage stage;
    private final TypeFactory typeFactory;

    public SubstitutorV2(Object root,
                         GenericContext context,
                         List<TypeVariable> typeParameters,
                         List<? extends Type> typeArguments,
                         ResolutionStage stage,
                         SaveTypeBatch batch,
                         TypeFactory typeFactory) {
        super(root);
        this.context = context;
        typeSubstitutor = new TypeSubstitutor(typeParameters, typeArguments, context.getEntityContext(), batch);
        this.stage = stage;
        this.batch = batch;
        this.typeFactory = typeFactory;
    }

    public Type substituteType(Type type) {
        return type.accept(typeSubstitutor);
    }

    public Property substituteProperty(Property property) {
        return switch (property) {
            case Field field -> substituteField(field);
            case Flow flow -> substituteFlow(flow);
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
    }

    private Field substituteField(Field field) {
        var type = (ClassType) substituteType(field.getDeclaringType());
        if (type == field.getDeclaringType())
            return field;
        else
            return NncUtils.requireNonNull(type.findField(f -> f.getTemplate() == field.getEffectiveTemplate()));
    }

    private Flow substituteFlow(Flow flow) {
        var type = (ClassType) substituteType(flow.getDeclaringType());
        if (type == flow.getDeclaringType())
            return flow;
        else
            return NncUtils.requireNonNull(type.findFlow(f -> f.getTemplate() == flow.getEffectiveTemplate()));
    }

    @Override
    protected Object substituteReference(Object reference) {
        if (reference instanceof Type type)
            return substituteType(type);
        if (reference instanceof Property property)
            return substituteProperty(property);
        return super.substituteReference(reference);
    }

    @Override
    public Element visitScope(ScopeRT scope) {
        return super.visitScope(scope);
    }

    @Override
    public Element visitClassType(ClassType type) {
        var copy = type.isTemplate() ? generateParameterizedType(type) : copyOrdinaryClass(type);
        var curStage = copy.getStage();
        copy.setStage(stage);
        addCopy(type, copy);
        if (stage.isAfterOrAt(SIGNATURE) && curStage.isBefore(SIGNATURE)) {
            if (type.getSuperClass() != null)
                copy.setSuperClass((ClassType) substituteType(type.getSuperClass()));
            copy.setInterfaces(NncUtils.map(type.getInterfaces(), t -> (ClassType) substituteType(t)));
            copy.setDependencies(NncUtils.map(type.getDependencies(), t -> (ClassType) substituteType(t)));
        }
        if (stage.isAfterOrAt(DECLARATION) && curStage.isBefore(DEFINITION)) {
            enterElement(copy);
            copy.setFields(NncUtils.map(type.getFields(), field -> (Field) copy(field)));
            copy.setFlows(NncUtils.map(type.getFlows(), flow -> (Flow) copy(flow)));
            exitElement();
        }
        if (type == root) {
            check();
        }
        return copy;
    }

    private ClassType copyOrdinaryClass(ClassType type) {
        NncUtils.requireTrue(type.isAnonymous());
        var name = Types.renameAnonymousType(type.getName());
        var code = NncUtils.get(type.getCode(), Types::renameAnonymousType);
        return ClassBuilder.newBuilder(name, code)
                .anonymous(true)
                .build();
    }

    private ClassType generateParameterizedType(ClassType type) {
        var template = type.getEffectiveTemplate();
        var typeArguments = NncUtils.map(type.getTypeArguments(), this::substituteType);
        var name = Types.getParameterizedName(template.getName(), typeArguments);
        var code = Types.getParameterizedCode(template.getCode(), typeArguments);
        var existing = context.getExisting(template, typeArguments);
        var pKey = new ParameterizedTypeKey(template.getRef(), NncUtils.map(typeArguments, Entity::getRef));
        var typeDTO = batch.getTypeDTO(pKey);
        var copy = ClassBuilder.newBuilder(name, code)
                .existing(existing)
                .typeArguments(typeArguments)
                .template(template)
                .anonymous(true)
                .tmpId(NncUtils.get(typeDTO, TypeDTO::tmpId))
                .build();
        context.addType(copy);
        return copy;
    }

    public void enterElement(Element element) {
        elements.push(element);
    }

    public void exitElement() {
        elements.pop();
    }

    private <T> T currentElement(Class<T> klass) {
        for (Object element : elements) {
            if (klass.isInstance(element))
                return klass.cast(element);
        }
        throw new InternalException("No enclosing element of type '" + klass.getName() + "' is found");
    }

    private ClassType currentClass() {
        return currentElement(ClassType.class);
    }

    @Override
    public Element visitTypeVariable(TypeVariable type) {
        // Type variables can be created before it's parent object
        var subst = substituteType(type);
        if (subst != type)
            return subst;
        var genericDecl = currentElement(GenericDeclaration.class);
        var genericDeclTemp = NncUtils.requireNonNull(genericDecl.getTemplate());

        var copy = NncUtils.find(genericDecl.getTypeParameters(), v -> v.getTemplate() == type);
        if (copy == null) {
            var typeKey = new TypeVariableKey(genericDecl.getRef(), genericDeclTemp.getTypeParameterIndex(type));
            var typeDTO = batch.getTypeDTO(typeKey);
            if (typeDTO != null)
                copy = batch.getTypeVariable(typeDTO.getRef());
            else {
                copy = NncUtils.find(genericDecl.getTypeParameters(), t -> t.getTemplate() == type);
                if (copy == null) {
                    copy = new TypeVariable(
                            null,
                            type.getName(),
                            type.getCode(),
                            genericDecl
                    );
                }
            }
        }
        typeSubstitutor.addMapping(type, copy);
        copy.setName(type.getName());
        copy.setCode(type.getCode());
        copy.setBounds(NncUtils.map(type.getBounds(), this::substituteType));
        copy.setTemplate(type);
        addCopy(type, copy);
        return copy;
    }

    private Flow generateFlowInst(ClassType declaringType, Flow flow) {
        var typeDTO = batch.getTypeDTO(declaringType.getRef());
        FlowDTO templateFlowDTO = NncUtils.get(typeDTO, t -> t.getClassParam().findFlowBySignature(
                flow.getSignature()
        ));
        List<Type> typeArgs = NncUtils.map(flow.getTypeParameters(), this::substituteType);
        FlowDTO flowDTO = null;
        if (templateFlowDTO != null) {
            flowDTO = NncUtils.find(templateFlowDTO.templateInstances(),
                    f -> f.typeArgumentRefs().equals(NncUtils.map(typeArgs, Entity::getRef)));
        }
        var copy = flow.getTemplateInstance(typeArgs);
        if (copy == null) {
            copy = FlowBuilder
                    .newBuilder(declaringType, flow.getName(), flow.getCode(), context.getEntityContext().getFunctionTypeContext())
                    .tmpId(NncUtils.get(flowDTO, FlowDTO::tmpId))
                    .template(flow)
                    .nullType(typeFactory.getNullType())
                    .voidType(typeFactory.getVoidType())
                    .typeArguments(typeArgs)
                    .build();
            context.getEntityContext().tryBind(copy);
        }
        return copy;
    }

    private Flow generateFlow(ClassType declaringType, Flow flow) {
        var copy = declaringType.findFlow(f -> f.getTemplate() == flow);
        var paramTypes = NncUtils.map(flow.getParameterTypes(), this::substituteType);
        var typeDTO = batch.getTypeDTO(declaringType.getRef());
        var flowDTO = NncUtils.get(typeDTO, t -> t.getClassParam().findFlowBySignature(
                Flow.getSignature(flow.getName(), paramTypes)
        ));
        if (copy == null) {
            copy = FlowBuilder
                    .newBuilder(declaringType, flow.getName(), flow.getCode(), context.getEntityContext().getFunctionTypeContext())
                    .tmpId(NncUtils.get(flowDTO, FlowDTO::tmpId))
                    .template(flow)
                    .nullType(typeFactory.getNullType())
                    .voidType(typeFactory.getVoidType())
                    .build();
            context.getEntityContext().tryBind(copy);
        }
        enterElement(copy);
        copy.setTypeParameters(NncUtils.map(flow.getTypeParameters(), t -> (TypeVariable) copy(t)));
        exitElement();
        return copy;
    }

    @Override
    public Element visitFlow(Flow flow) {
        var declaringType = currentClass();
        var copy = flow.getDeclaringType() != declaringType ?
                generateFlow(declaringType, flow) : generateFlowInst(declaringType, flow);
        addCopy(flow, copy);
        addCopy(flow.getRootScope(), copy.getRootScope());
        enterElement(copy);
        copy.setAbstract(flow.isAbstract());
        copy.setNative(flow.isNative());
        copy.setConstructor(flow.isConstructor());
        copy.update(
                NncUtils.map(flow.getOverridden(), this::substituteFlow),
                NncUtils.map(flow.getParameters(), p -> (Parameter) copy(p)),
                substituteType(flow.getReturnType()),
                (FunctionType) substituteType(flow.getType()),
                (FunctionType) substituteType(flow.getStaticType())
        );
        if (stage.isAfterOrAt(DEFINITION)) {
            copy.getRootScope().clearNodes();
            for (NodeRT<?> node : flow.getRootScope().getNodes())
                copy.getRootScope().addNode((NodeRT<?>) copy(node));
        }
        exitElement();
        return copy;
    }

    @Override
    public Element visitParameter(Parameter parameter) {
        var callable = currentElement(Callable.class);
        ParameterDTO paramDTO = null;
        Parameter copy = null;
        if (callable instanceof Flow flow) {
            var flowDTO = batch.getFlowDTO(flow);
            if (flowDTO != null) {
                paramDTO = flowDTO.findParameterByName(parameter.getName());
                copy = NncUtils.find(flow.getParameters(), p -> p.getTemplate() == parameter);
            }
        }
        addCopy(parameter, copy);
        var condCopy = NncUtils.get(parameter.getCondition(), cond -> (Value) copy(cond));
        if (copy == null) {
            copy = new Parameter(
                    NncUtils.get(paramDTO, ParameterDTO::tmpId),
                    parameter.getName(),
                    parameter.getCode(),
                    substituteType(parameter.getType()),
                    condCopy,
                    parameter,
                    callable
            );
        } else {
            copy.setName(parameter.getName());
            copy.setCode(parameter.getCode());
            copy.setType(substituteType(parameter.getType()));
            copy.setCondition(condCopy);
        }
        return copy;
    }

    @Override
    public Element visitField(Field field) {
        Field existing = currentClass().findField(f -> f.getTemplate() == field);
        var typeDTO = batch.getTypeDTO(currentClass().getRef());
        var fieldDTO = typeDTO != null ? typeDTO.getClassParam().findFieldByName(field.getName()) : null;
        var copy = FieldBuilder
                .newBuilder(field.getName(), field.getCode(), currentClass(), substituteType(field.getType()))
                .tmpId(NncUtils.get(fieldDTO, FieldDTO::tmpId))
                .existing(existing)
                .isChild(field.isChild())
                .unique(field.isUnique())
                .nullType(typeFactory.getNullType())
                .asTitle(field.isAsTitle())
                .column(field.getColumn())
                .template(field)
                .access(field.getAccess())
                .isStatic(field.isStatic())
                .build();
        addCopy(field, copy);
        if (existing == null)
            context.getEntityContext().tryBind(copy);
        return copy;
    }

}
