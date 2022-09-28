package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static tech.metavm.util.ReflectUtils.getConstructor;
import static tech.metavm.util.ReflectUtils.newInstance;

public class NodeFactory {

    public static NodeRT<?> getFlowNode(NodeDTO nodeDTO, ScopeRT scope) {
        NodeType nodeType = NodeType.getByCodeRequired(nodeDTO.type());
        Class<? extends NodeRT<?>> klass = nodeType.getKlass();;
        Class<?> paramClass = getParamClass(klass);
        Constructor<?> constructor;
        if(paramClass != Void.class) {
            constructor = getConstructor(klass, NodeDTO.class, paramClass, ScopeRT.class);
            return (NodeRT<?>) newInstance(constructor, nodeDTO, nodeDTO.getParam(), scope);
        }
        else {
            constructor = getConstructor(klass, NodeDTO.class,  ScopeRT.class);
            return (NodeRT<?>) newInstance(constructor, nodeDTO, scope);
        }
    }

    public static NodeRT<?> getFlowNode(NodePO nodePO, ScopeRT scope) {
        NodeType nodeType = NodeType.getByCodeRequired(nodePO.getType());
        Class<? extends NodeRT<?>> klass = nodeType.getKlass();
        Class<?> paramClass = getParamClass(klass);
        Constructor<?> constructor;
        if(paramClass != Void.class) {
            constructor = getConstructor(klass, NodePO.class, paramClass, ScopeRT.class);
            Object param = NncUtils.readJSONString(nodePO.getParam(), paramClass);
            return (NodeRT<?>) newInstance(constructor, nodePO, param, scope);
        }
        else {
            constructor = getConstructor(klass, NodePO.class, ScopeRT.class);
            return (NodeRT<?>) newInstance(constructor, nodePO, scope);
        }
    }

    public static Class<?> getParamClass(Class<? extends NodeRT<?>> nodeType) {
        ParameterizedType superType = (ParameterizedType) nodeType.getGenericSuperclass();
        return  (Class<?>) superType.getActualTypeArguments()[0];
    }


}
