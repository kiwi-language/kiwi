package org.metavm.entity.natives;

import org.metavm.entity.DefContext;
import org.metavm.flow.Function;
import org.metavm.object.type.TypeParserImpl;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NativeFunctionDef {

    private final String name;
    private final String signature;
    private final boolean system;
    private ValueHolder<Function> functionHolder;
    private final FunctionImpl impl;
    private final List<Method> javaMethods;

    public NativeFunctionDef(String signature, boolean system, List<Method> javaMethods, ValueHolder<Function> functionHolder, FunctionImpl impl) {
        this.signature = signature;
        this.system = system;
        this.javaMethods = new ArrayList<>(javaMethods);
        this.impl = impl;
        this.functionHolder = functionHolder;
        var typeParser = new TypeParserImpl((String name) -> {
            throw new NullPointerException("defContext is null");
        });
        this.name = typeParser.getFunctionName(signature);
    }

    public String getName() {
        return name;
    }

    public boolean isSystem() {
        return system;
    }

    public Function define(DefContext defContext) {
        var function = parseFunction(defContext);
        function.setNative(true);
        function.setNativeCode(impl);
        set(function);
        return function;
    }

    private Function parseFunction(DefContext defContext) {
        return new TypeParserImpl(
                (String name) -> {
                    if(defContext != null)
                        return defContext.getKlass(ReflectionUtils.classForName(name));
                    else
                        throw new NullPointerException("defContext is null");
                }
        ).parseFunction(signature);
    }

    public List<Method> getJavaMethods() {
        return javaMethods;
    }

    public String getSignature() {
        return signature;
    }

    public void set(Function function) {
        functionHolder.set(function);
    }

    public Function get() {
        return functionHolder.get();
    }

    public void setFunctionHolder(ValueHolder functionHolder) {
        this.functionHolder = functionHolder;
    }

    public FunctionImpl getImpl() {
        return impl;
    }
}
