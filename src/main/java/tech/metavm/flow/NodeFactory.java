package tech.metavm.flow;

import tech.metavm.autograph.ExpressionTypeMap;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContext;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import static tech.metavm.util.ReflectUtils.*;

public class NodeFactory {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static NodeRT<?> create(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        NodeKind nodeType = NodeKind.getByCodeRequired(nodeDTO.kind());
        Class<? extends NodeRT<?>> klass = nodeType.getKlass();
        Method createMethod = tryGetStaticMethod(
                klass, "create", NodeDTO.class, NodeRT.class, ScopeRT.class, IEntityContext.class);
        NodeRT<?> prev = nodeDTO.prevRef() != null ? context.getNode(nodeDTO.prevRef()) : scope.getLastNode();
        NodeRT node;
        if (createMethod != null) {
            node = (NodeRT<?>) ReflectUtils.invoke(null, createMethod, nodeDTO, prev, scope, context);
        } else {
            Constructor<?> constructor = getConstructor(klass, NodeDTO.class, ScopeRT.class);
            node = (NodeRT) newInstance(constructor, nodeDTO, scope);
            if(nodeDTO.param() != null) node.setParam(nodeDTO.param(), context);
        }
        var prevExprTypes = prev != null ? prev.getExpressionTypes() : ExpressionTypeMap.EMPTY;
        node.mergeExpressionTypes(prevExprTypes);
        context.bind(node);
        return node;
    }

    public static NodeRT<?> create(NodePO nodePO, InstanceContext context) {
        NodeKind nodeType = NodeKind.getByCodeRequired(nodePO.getType());
        Class<? extends NodeRT<?>> klass = nodeType.getKlass();
        Class<?> paramClass = getParamClass(klass);
        Constructor<?> constructor;
        if(paramClass != Void.class) {
            constructor = getConstructor(klass, NodePO.class, paramClass, ScopeRT.class);
            Object param = NncUtils.readJSONString(nodePO.getParam(), paramClass);
            return (NodeRT<?>) newInstance(constructor, nodePO, param, context);
        }
        else {
            constructor = getConstructor(klass, NodePO.class, ScopeRT.class);
            return (NodeRT<?>) newInstance(constructor, nodePO, context);
        }
    }

    public static Class<?> getParamClass(Class<? extends NodeRT<?>> nodeType) {
        ParameterizedType superType = (ParameterizedType) nodeType.getGenericSuperclass();
        return  (Class<?>) superType.getActualTypeArguments()[0];
    }


}
