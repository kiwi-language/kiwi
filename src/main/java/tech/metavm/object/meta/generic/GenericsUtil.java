//package tech.metavm.object.meta.generic;
//
//import tech.metavm.entity.GenericDeclaration;
//import tech.metavm.entity.ModelDefRegistry;
//import tech.metavm.flow.Flow;
//import tech.metavm.object.meta.*;
//import tech.metavm.util.IteratorImpl;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.ParameterizedTypeImpl;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import static tech.metavm.util.NncUtils.requireNonNull;
//import static tech.metavm.util.NncUtils.zip;
//
//public class GenericsUtil {
//
//    public static final TypeFactory DEFAULT_TYPE_FACTORY = new DefaultTypeFactory(ModelDefRegistry::getType);
//
//    public static ClassType getParameterizedType(ClassType template, Type... typeArguments) {
//        return getParameterizedType(template, DEFAULT_TYPE_FACTORY, typeArguments);
//    }
//
//    public static ClassType getParameterizedType(ClassType template, TypeFactory typeFactory, Type... typeArguments) {
//        return getParameterizedType(template, typeFactory, List.of(typeArguments));
//    }
//
//    public static ClassType getParameterizedType(ClassType template, List<Type> typeArguments) {
//        return getParameterizedType(template, DEFAULT_TYPE_FACTORY, typeArguments);
//    }
//
//    public static ClassType getParameterizedType(ClassType template, TypeFactory typeFactory, List<Type> typeArguments) {
//        return (ClassType) createTransformer(template, typeArguments, typeFactory).transformType(template);
//    }
//
//    private static GenericTransformer createTransformer(GenericDeclaration genericDeclarator,
//                                                        List<Type> typeArguments,
//                                                        TypeFactory typeFactory) {
//        return createTransformer(new MetaSubstitutor(zip(genericDeclarator.getTypeParameters(), typeArguments)), typeFactory);
//    }
//
//    private static GenericTransformer createTransformer(MetaSubstitutor substitutor, TypeFactory typeFactory) {
//        return new GenericTransformer(
//                substitutor,
//                true,
//                typeFactory,
//                transformed -> postProcess(transformed, typeFactory)
//        );
//    }
//
//    private static void postProcess(ClassType classType, TypeFactory typeFactory) {
//        var template = classType.getTemplate();
//        if (template == null) {
//            return;
//        }
//        if (template == typeFactory.getType(Map.class)) {
//            classType.addDependency(
//                    getParameterizedType(
//                            typeFactory.getClassType(Set.class),
//                            typeFactory,
//                            classType.getTypeArguments().get(0)
//                    )
//            );
//        }
//        if (template == typeFactory.getType(List.class) || template == typeFactory.getType(Set.class)) {
//            classType.addDependency(
//                    getParameterizedType(
//                            typeFactory.getClassType(IteratorImpl.class),
//                            typeFactory,
//                            classType.getTypeArguments().get(0)
//                    )
//            );
//        }
//        if (typeFactory.isPutTypeSupported()) {
//            var templateClass = (Class<?>) typeFactory.getJavaType(template);
//            if (templateClass != null) {
//                var javaType = ParameterizedTypeImpl.create(
//                        templateClass,
//                        NncUtils.map(classType.getTypeArguments(), typeFactory::getJavaType)
//                );
//                typeFactory.putType(javaType, classType);
//            }
//        }
//        if (typeFactory.isAddTypeSupported()) {
//            typeFactory.addType(classType);
//        }
//    }
//
//    public static Flow getParameterizedFlow(Flow flow, List<Type> typeArguments) {
//        return getParameterizedFlow(flow, typeArguments, DEFAULT_TYPE_FACTORY);
//    }
//
//    public static Flow continueFlowTransformation(ClassType declaringType, Flow flow, TypeFactory typeFactory) {
//        var transformer = createTransformer(requireNonNull(declaringType.getTemplate()),
//                declaringType.getTypeArguments(), typeFactory);
//        transformer.enterClass(declaringType);
//        var transformed = transformer.transformFlow(flow);
//        transformer.exitClass();
//        return transformed;
//    }
//
//    public static Flow getParameterizedFlow(Flow template, List<Type> typeArguments, TypeFactory typeFactory) {
//        NncUtils.requireNull(template.getTemplate(), "Not a flow templateName");
//        if (template.getTypeParameters().isEmpty()) {
//            return template;
//        }
//        var ti = template.getTemplateInstance(typeArguments);
//        if (ti != null) {
//            return ti;
//        }
//        var transformer = createTransformer(template, typeArguments, typeFactory);
//        transformer.enterClass(template.getDeclaringType());
//        var transformed = transformer.transformFlow(template, template, typeArguments);
//        transformer.exitClass();
//        return transformed;
//    }
//
//    public static void transform(ClassType classType) {
//        createTransformer(MetaSubstitutor.EMPTY, DEFAULT_TYPE_FACTORY)
//                .transformClassType(classType);
//    }
//
//    private static boolean containsTypeVariables(List<Type> typeArguments) {
//        return NncUtils.anyMatch(typeArguments, GenericsUtil::isVariableType);
//    }
//
//    private static boolean isVariableType(Type type) {
//        return type instanceof TypeVariable;
//    }
//
//}
