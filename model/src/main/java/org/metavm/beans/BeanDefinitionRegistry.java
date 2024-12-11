package org.metavm.beans;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class BeanDefinitionRegistry extends org.metavm.entity.Entity {
    public static final IndexDef<BeanDefinitionRegistry> IDX_ALL_FLAGS = IndexDef.create(BeanDefinitionRegistry.class, "allFlags");

    @SuppressWarnings("unused")
    private final boolean allFlags = true;

    @ChildEntity
    private final ReadWriteArray<BeanDefinition> interceptorDefinitions = addChild(new ReadWriteArray<>(BeanDefinition.class), "interceptorDefinitions");

    @ChildEntity
    private final ChildArray<BeanDefinition> beanDefinitions = addChild(new ChildArray<>(BeanDefinition.class), "beanDefinitions");

    public static BeanDefinitionRegistry getInstance(IEntityContext context) {
        return Objects.requireNonNull(context.selectFirstByKey(IDX_ALL_FLAGS, true), "BeanDefinitionRegistry not found");
    }

    public static void initialize(IEntityContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, true);
        if(existing != null)
            throw new IllegalStateException("BeanDefinitionRegistry already exists");
        context.bind(new BeanDefinitionRegistry());
    }

    private BeanDefinitionRegistry() {}

    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        if(NncUtils.exists(beanDefinitions, b -> b.getName().equals(beanDefinition.getName())))
            throw new IllegalStateException("BeanDefinition with name " + beanDefinition.getName() + " already exists");
        beanDefinitions.addChild(beanDefinition);
        if(StdKlass.interceptor.type().isAssignableFrom(beanDefinition.getBeanType()))
            interceptorDefinitions.add(beanDefinition);
    }

    public @Nullable BeanDefinition tryGetBeanDefinition(String name) {
        return NncUtils.find(beanDefinitions, b -> b.getName().equals(name));
    }

    public BeanDefinition getBeanDefinition(String name) {
        return NncUtils.findRequired(beanDefinitions, b -> b.getName().equals(name),
                "BeanDefinition with name " + name + " not found");
    }

    public ClassInstance getBean(String name) {
        return getBeanDefinition(name).getBean();
    }

    public @Nullable ClassInstance tryGetBean(String name) {
        var def = tryGetBeanDefinition(name);
        return def != null ? def.getBean() : null;
    }

    public void removeBeanDefinition(String name) {
        var beanDef = NncUtils.findRequired(beanDefinitions, bean -> bean.getName().equals(name),
                "BeanDefinition with name " + name + " not found");
        beanDefinitions.remove(beanDef);
        if(StdKlass.interceptor.type().isAssignableFrom(beanDef.getBeanType()))
            interceptorDefinitions.remove(beanDef);
    }

    public List<BeanDefinition> getBeanDefinitionsByType(ClassType type) {
        return NncUtils.filter(beanDefinitions, b -> type.isAssignableFrom(b.getBeanType()));
    }

    public List<ClassInstance> getBeansOfType(ClassType type) {
        return NncUtils.filterAndMap(beanDefinitions, b -> type.isAssignableFrom(b.getBeanType()), BeanDefinition::getBean);
    }

    public List<Value> getFlowArguments(Flow method) {
        var arguments = new ArrayList<Value>();
        for (Parameter parameter : method.getParameters()) {
            var beanName = parameter.getAttribute(AttributeNames.BEAN_NAME);
            if (beanName != null) {
                var bean = getBean(beanName);
                if (parameter.getType().isInstance(bean.getReference()))
                    arguments.add(bean.getReference());
                else
                    throw new InternalException("Bean " + beanName + " is not of type " + parameter.getType());
                continue;
            }
            if (parameter.getType().getUnderlyingType() instanceof ClassType paramType) {
                if (paramType.isList()) {
                    if(paramType.getFirstTypeArgument() instanceof ClassType beanType)
                        arguments.add(Instances.createList(paramType, NncUtils.map(getBeansOfType(beanType), Instance::getReference)).getReference());
                    else
                        throw new InternalException("Unsupported list element type " + paramType.getFirstTypeArgument() + " in bean factory method " + method.getName());
                } else {
                    var beans = getBeansOfType(paramType);
                    if (beans.isEmpty())
                        throw new InternalException("No beans of type " + paramType + " found");
                    if (beans.size() > 1)
                        throw new InternalException("Multiple beans of type " + paramType + " found");
                    arguments.add(beans.get(0).getReference());
                }
            } else {
                throw new InternalException("Unsupported parameter type " + parameter.getType() + " in bean factory method " + method.getName());
            }
        }
        return arguments;
    }

    public List<ClassInstance> getInterceptors() {
        return NncUtils.map(interceptorDefinitions, BeanDefinition::getBean);
    }

    public List<BeanDefinition> getFlowDependencies(Flow flow) {
        return NncUtils.flatMap(flow.getParameters(), parameter -> {
            var beanName = parameter.getAttribute(AttributeNames.BEAN_NAME);
            if (beanName != null)
                return List.of(getBeanDefinition(beanName));
            var type = parameter.getType().getUnderlyingType();
            if(type instanceof ClassType classType) {
                if (classType.isList()) {
                    if(classType.getFirstTypeArgument() instanceof ClassType elementType)
                        return getBeanDefinitionsByType(elementType);
                    else
                        return List.of();
                }
                return getBeanDefinitionsByType(classType);
            }
            else
                return List.of();
        });

    }

}
