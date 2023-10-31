package tech.metavm.object.meta.generic;

import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.task.FieldData;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static tech.metavm.object.meta.TypeUtil.getParameterizedCode;
import static tech.metavm.object.meta.TypeUtil.getParameterizedName;
import static tech.metavm.util.NncUtils.requireNonNull;
import static tech.metavm.util.NncUtils.zip;

public class GenericContext {

    private final Map<ClassType, Map<List<Type>, ClassType>> map = new HashMap<>();
    private final IEntityContext entityContext;
    private final TypeFactory typeFactory;
    private final List<Consumer<ClassType>> callbacks = new ArrayList<>();

    public GenericContext(@Nullable IEntityContext entityContext, TypeFactory typeFactory) {
        this.entityContext = entityContext;
        this.typeFactory = typeFactory;
    }

    public ClassType getParameterizedType(ClassType template, Type... typeArguments) {
        return getParameterizedType(template, List.of(typeArguments));
    }

    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments) {
        var transformed = (ClassType) createTransformer(template, template.getStage(), typeArguments, typeFactory, null).transformType(template);
        if (entityContext != null && entityContext.isNewEntity(transformed) && entityContext.isBindSupported()) {
            entityContext.bind(transformed);
        }
        return transformed;
    }

    public Field retransformField(Field field, ClassType existing) {
        var transformer = createTransformer(
                field.getDeclaringType(), ResolutionStage.GENERATED, existing.getTypeArguments(), typeFactory, existing
        );
        transformer.enterClass(existing);
        var transformedField = transformer.transformField(field);
        transformer.exitClass();
        return transformedField;
    }

    public Flow retransformFlow(Flow flow, ClassType existing) {
        var transformer = createTransformer(
                flow.getDeclaringType(), ResolutionStage.GENERATED, existing.getTypeArguments(), typeFactory, existing
        );
        transformer.enterClass(existing);
        var transformedFlow = transformer.transformFlow(flow);
        transformer.exitClass();
        return transformedFlow;
    }

    public ClassType retransformClass(ClassType template, ClassType existing) {
        var transformer = createTransformer(
                template, ResolutionStage.GENERATED, existing.getTypeArguments(), typeFactory, existing
        );
        transformer.enterClass(existing);
        transformer.transformClassBody(template);
        transformer.exitClass();
        return existing;
    }

    public FieldData transformFieldData(ClassType template, ClassType templateInstance, FieldData fieldData) {
        var transformer = createTransformer(
                template, ResolutionStage.GENERATED, templateInstance.getTypeArguments(), typeFactory, templateInstance
        );
        transformer.enterClass(templateInstance);
        var transformedFieldData = transformer.transformFieldData(fieldData);
        transformer.exitClass();
        return transformedFieldData;
    }

    public Type substitute(Type type, TypeArgumentMap typeArgumentMap) {
        var transformer = createTransformer(typeArgumentMap, ResolutionStage.GENERATED, typeFactory, null);
        return transformer.transformType(type);
    }

    public ClassType getExisting(ClassType template, List<Type> typeArguments) {
        var existing = map.computeIfAbsent(template, k -> new HashMap<>()).get(typeArguments);
        if (existing != null) {
            return existing;
        }
        if (template.getId() != null && NncUtils.allMatch(typeArguments, typeArg -> typeArg.getId() != null)) {
            var loaded = load(template, typeArguments);
            if (loaded != null) {
                map.get(template).put(typeArguments, loaded);
                return loaded;
            }
        }
        return null;
    }

    public ClassType load(ClassType template, List<Type> typeArguments) {
        if (entityContext == null) {
            return null;
        }
        return entityContext.selectByUniqueKey(
                ClassType.IDX_PARAMETERIZED_TYPE_KEY,
                ClassType.pTypeKey(template, typeArguments)
        );
    }

    private GenericTransformer createTransformer(GenericDeclaration genericDeclarator,
                                                 ResolutionStage stage,
                                                 List<? extends Type> typeArguments,
                                                 TypeFactory typeFactory,
                                                 @Nullable ClassType existing
    ) {
        return createTransformer(new TypeArgumentMap(zip(genericDeclarator.getTypeParameters(), typeArguments)),
                stage,
                typeFactory, existing);
    }

    private GenericTransformer createTransformer(TypeArgumentMap typeArgumentMap, ResolutionStage stage, TypeFactory typeFactory, @Nullable ClassType existing) {
        return new GenericTransformer(
                typeArgumentMap,
                stage,
                typeFactory,
                this,
                entityContext,
                existing,
                this::postProcess
        );
    }

    private void postProcess(ClassType classType) {
        var template = classType.getTemplate();
        if (template == null) {
            return;
        }
        map.computeIfAbsent(template, k -> new HashMap<>()).put(classType.getTypeArguments(), classType);
        if (typeFactory.isPutTypeSupported()) {
            var templateClass = (Class<?>) typeFactory.getJavaType(template);
            if (templateClass != null) {
                var javaType = ParameterizedTypeImpl.create(
                        templateClass,
                        NncUtils.map(classType.getTypeArguments(), typeFactory::getJavaType)
                );
                typeFactory.putType(javaType, classType);
            }
        }
        if (typeFactory.isAddTypeSupported()) {
            typeFactory.addType(classType);
        }
        for (Consumer<ClassType> callback : callbacks) {
            callback.accept(classType);
        }
    }

    public void addCallback(Consumer<ClassType> callback) {
        this.callbacks.add(callback);
    }

    public void generateDeclarations(ClassType declaringType, ClassType template) {
        if (declaringType.getStage().isAfterOrAt(ResolutionStage.DECLARED)) {
            return;
        }
        if (template.getStage().isBeforeOrAt(ResolutionStage.CREATED)) {
            throw new InternalException("Template declarations not generated yet");
        }
        var transformer = createTransformer(requireNonNull(declaringType.getTemplate()),
                ResolutionStage.DECLARED,
                declaringType.getTypeArguments(), typeFactory, null);
        transformer.enterClass(declaringType);
        template.getAllFields().forEach(transformer::transformField);
        template.getFlows().forEach(transformer::transformFlow);
        transformer.exitClass();
        declaringType.setStage(ResolutionStage.DECLARED);
    }

    public void generateDeclarations(ClassType template) {
        var templateInstances = map.get(template);
        if (templateInstances != null) {
            for (ClassType templateInst : templateInstances.values()) {
                generateDeclarations(templateInst, template);
            }
        }
    }

    public void generateCode(ClassType template) {
        var templateInstances = map.get(template);
        if (templateInstances != null) {
            for (ClassType templateInst : templateInstances.values()) {
                generateCode(templateInst, template);
            }
        }
    }

    public void generateCode(ClassType declaringType, ClassType template) {
        if (declaringType.getStage().isAfterOrAt(ResolutionStage.GENERATED)) {
            return;
        }
        if (template.getStage().isBeforeOrAt(ResolutionStage.DECLARED)) {
            throw new InternalException("Template code not generated yet");
        }
        if (declaringType.getStage().isBeforeOrAt(ResolutionStage.CREATED)) {
            generateDeclarations(declaringType, template);
        } else {
            var transformer = createTransformer(requireNonNull(declaringType.getTemplate()),
                    ResolutionStage.GENERATED,
                    declaringType.getTypeArguments(), typeFactory, null);
            transformer.enterClass(declaringType);
            NncUtils.biForEach(declaringType.getFlows(), template.getFlows(), (flow, flowTemplate) -> {
                if (flowTemplate.getRootScope() != null) {
                    transformer.enterFlow(flow);
                    flow.setRootScope(transformer.transformScope(flowTemplate.getRootScope()));
                    transformer.exitFlow();
                }
            });
            transformer.exitClass();
            declaringType.setStage(ResolutionStage.GENERATED);
        }
    }

    public Flow getParameterizedFlow(Flow template, List<Type> typeArguments) {
        NncUtils.requireTrue(template.isTemplate(), "Not a flow template");
        if (template.getTypeParameters().isEmpty()) {
            return template;
        }
        var ti = template.getTemplateInstance(typeArguments);
        if (ti != null) {
            return ti;
        }
        var transformer = createTransformer(template, ResolutionStage.GENERATED, typeArguments, typeFactory, null);
        transformer.enterClass(template.getDeclaringType());
        String name = getParameterizedName(template.getName(), typeArguments);
        String code = getParameterizedCode(template.getCode(), typeArguments);
        var transformed = transformer.transformFlow(template, template, name, code, List.of(), typeArguments);
        transformer.exitClass();
        return transformed;
    }

    private boolean isVariableType(Type type) {
        return type instanceof TypeVariable;
    }


}
