package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.DefContext;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Function;
import org.metavm.flow.FunctionBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class StandardStaticMethods {

    private static final List<Class<?>> klasses = List.of(
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, Boolean.class, Character.class,
            String.class
    );

    private static final List<FunctionDef> functionDefs = new ArrayList<>();

    static {
        for (Class<?> k : klasses) {
            for (Method method : k.getDeclaredMethods()) {
                var mod = method.getModifiers();
                if(Modifier.isPublic(mod) && !isBlacklisted(method)) {
                    defineFunction(method);
                }
            }
        }
    }

    private static boolean isBlacklisted(Method method) {
        for (var parameter : method.getParameters()) {
            if(Types.tryFromJavaType(parameter.getType()) == null)
                return true;
        }
        return Types.tryFromJavaType(method.getReturnType()) == null;
    }

    public static List<FunctionDef> getDefs() {
        return Collections.unmodifiableList(functionDefs);
    }

    private static void defineFunction(Method method) {
        functionDefs.add(new FunctionDef(method));
    }

    public static List<Function> defineFunctions() {
        return NncUtils.map(functionDefs, FunctionDef::define);
    }

    public static void initialize(DefContext defContext, boolean local) {
        for (FunctionDef def : functionDefs) {
            var func = Objects.requireNonNull(
                    defContext.selectFirstByKey(Function.UNIQUE_NAME, def.getName()),
                    "Function not found: " + def.getName());
            if(local)
                def.setLocal(func);
            else
                def.set(func);

        }
    }

    public static class FunctionDef implements ValueHolderOwner<Function> {
        private final Method method;
        private final String name;
        private final FunctionImpl impl;
        private ValueHolder<Function> functionHolder = new HybridValueHolder<>();

        private FunctionDef(Method method) {
            this.method = method;
            name = getFunctionName(method);
            var paramTypes = method.getParameterTypes();
            if(Modifier.isStatic(method.getModifiers())) {
                impl = (function, arguments, callContext) -> {
                    var javaArgs = new Object[arguments.size()];
                    var i = 0;
                    for (Value argument : arguments) {
                        javaArgs[i] = Instances.toJavaValue(argument, paramTypes[i++]);
                    }
                    var r = ReflectionUtils.invoke(null, method, javaArgs);
                    return FlowExecResult.of(Instances.fromJavaValue(r, true, Instances::nullInstance));
                };
            }
            else {
                impl = (function, arguments, callContext) -> {
                    var it = arguments.iterator();
                    var self = Instances.toJavaValue(it.next(), method.getDeclaringClass());
                    var i = 0;
                    var javaArgs = new Object[paramTypes.length];
                    while(it.hasNext())
                        javaArgs[i] = Instances.toJavaValue(it.next(), paramTypes[i++]);
                    var r = ReflectionUtils.invoke(self, method, javaArgs);
                    return FlowExecResult.of(Instances.fromJavaValue(r, true, Instances::nullInstance));
                };
            }
        }

        public void set(Function function) {
            functionHolder.set(function);
        }

        public void setLocal(Function function) {
            functionHolder.setLocal(function);
        }

        public Function get() {
            return functionHolder.get();
        }

        public Function define() {
//            log.debug("Defining function for method {}", ReflectionUtils.getMethodQualifiedName(method));
            var params = new ArrayList<NameAndType>();
            if(!Modifier.isStatic(method.getModifiers()))
                params.add(new NameAndType("$this", Types.fromJavaType(method.getDeclaringClass())));
            for (var p : method.getParameters()) {
                params.add(new NameAndType(p.getName(), Types.fromJavaType(p.getParameterizedType())));
            }
            var func = FunctionBuilder.newBuilder(name)
                    .typeParameters(
                            NncUtils.map(
                                    method.getTypeParameters(),
                                    tv -> new TypeVariable(null, tv.getName(), DummyGenericDeclaration.INSTANCE)
                            )
                    )
                    .parameters(params)
                    .returnType(Types.fromJavaType(method.getGenericReturnType()))
                    .isNative(true)
                    .build();
            func.setNativeCode(impl);
            return func;
        }

        public String getName() {
            return name;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public void setValueHolder(ValueHolder<Function> valueHolder) {
            this.functionHolder = valueHolder;
        }

        @Override
        public ValueHolder<Function> getValueHolder() {
            return functionHolder;
        }
    }

    public static String getFunctionName(Method method) {
        return method.getDeclaringClass().getSimpleName()+ "_" + method.getName() + "_" +
                NncUtils.join(List.of(method.getParameterTypes()), Class::getSimpleName, "_");
    }

}
