package org.metavm.entity.natives;

import org.metavm.api.lang.EmailUtils;
import org.metavm.api.lang.*;
import org.metavm.common.ErrorCode;
import org.metavm.entity.DefContext;
import org.metavm.entity.StandardTypes;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Types;
import org.metavm.user.Session;
import org.metavm.util.*;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class NativeFunctions {

    private static EmailSender emailSender;
    private static Supplier<FunctionHolder> functionHolderSupplier = DirectFunctionHolder::new;
    private static final List<NativeFunctionDef> defs = new ArrayList<>();

    public static final NativeFunctionDef isSourcePresent = createDef(
            "boolean isSourcePresent(any view)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof DurableInstance durableInstance)
                    return FlowExecResult.of(Instances.booleanInstance(durableInstance.tryGetSource() != null));
                else
                    throw new InternalException("Can not get source of a non-durable instance: " + args.get(0));
            });

    public static final NativeFunctionDef getSource = createDef(
            "any getSource(any view)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof DurableInstance durableInstance)
                    return FlowExecResult.of(durableInstance.getSource());
                else
                    throw new InternalException("Can not get source of a non-durable instance: " + args.get(0));
            });

    public static final NativeFunctionDef setSource = createDef(
            "void setSource(any view, any source)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof DurableInstance durableInstance) {
                    durableInstance.setSourceRef(new SourceRef((DurableInstance) args.get(1), null));
                    return FlowExecResult.of(Instances.nullInstance());
                } else
                    throw new InternalException("Can not set source for a non-durable instance: " + args.get(0));
            });

    public static final NativeFunctionDef functionToInstance = createDef(
            "T functionToInstance<T>(any func)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof FunctionInstance functionInstance) {
                    var samInterface = ((ClassType) func.getTypeArguments().get(0)).resolve();
                    var type = Types.createSAMInterfaceImpl(samInterface, functionInstance);
                    return FlowExecResult.of(new ClassInstance(null, Map.of(), type));
                } else {
                    throw new InternalException("Invalid function instance: " + Instances.getInstancePath(args.get(0)));
                }
            });

    public static final NativeFunctionDef sendEmail = createDef(
            "void sendEmail(string recipient, string subject, string content)",
            false,
            List.of(ReflectionUtils.getMethod(EmailUtils.class, "send", String.class, String.class, String.class)),
            (func, args, callContext) -> {
                emailSender.send(
                        ((StringInstance) args.get(0)).getValue(),
                        ((StringInstance) args.get(1)).getValue(),
                        ((StringInstance) args.get(2)).getValue()
                );
                return FlowExecResult.of(Instances.nullInstance());
            });

    public static final NativeFunctionDef getSessionEntry = createDef(
            "any|null getSessionEntry(string key)",
            false,
            List.of(ReflectionUtils.getMethod(SessionUtils.class, "getEntry", String.class)),
            (func, args, callContext) -> {
                var key = ((StringInstance) args.get(0)).getValue();
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, ContextUtil.getToken());
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                var value = session.getEntry(key);
                return FlowExecResult.of(NncUtils.orElse(value, Instances.nullInstance()));
            });

    public static final NativeFunctionDef setSessionEntry = createDef(
            "void setSessionEntry(string key, any value)",
            false,
            List.of(ReflectionUtils.getMethod(SessionUtils.class, "setEntry", String.class, Object.class)),
            (func, args, callContext) -> {
                var key = ((StringInstance) args.get(0)).getValue();
                var value = args.get(1);
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, ContextUtil.getToken());
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                session.setEntry(key, value);
                return FlowExecResult.of(Instances.nullInstance());
            });

    public static final NativeFunctionDef removeSessionEntry = createDef(
            "boolean removeSessionEntry(string key)",
            false,
            List.of(ReflectionUtils.getMethod(SessionUtils.class, "removeEntry", String.class)),
            (func, args, callContext) -> {
                var key = ((StringInstance) args.get(0)).getValue();
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, ContextUtil.getToken());
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                return FlowExecResult.of(Instances.booleanInstance(session.removeEntry(key)));
            });

    public static final NativeFunctionDef typeCast = createDef(
            "T typeCast<T>(any|null instance)",
            true,
            List.of(),
            (func, args, callContext) -> {
                var type = func.getTypeArguments().get(0);
                var value = args.get(0);
                if (type.isInstance(value))
                    return FlowExecResult.of(value);
                else
                    throw new BusinessException(ErrorCode.TYPE_CAST_ERROR, value.getType(), type);
            });

    public static final NativeFunctionDef print = createDef(
            "void print(any|null content)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "print", Object.class)),
            (func, args, callContext) -> {
                System.out.println(args.get(0).getTitle());
                return FlowExecResult.of(Instances.nullInstance());
            });

    public static final NativeFunctionDef delete = createDef(
            "void delete(any instance)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "delete", Object.class)),
            (func, args, callContext) -> {
                var entityContext = ContextUtil.getEntityContext();
                var instance = args.get(0);
                if (instance instanceof DurableInstance durableInstance) {
                    entityContext.getInstanceContext().remove(durableInstance);
                    return FlowExecResult.of(Instances.nullInstance());
                } else
                    throw new BusinessException(ErrorCode.DELETE_NON_DURABLE_INSTANCE, instance);
            });

    public static final NativeFunctionDef setContext = createDef(
            "void setContext(string key, string value)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "setContext", String.class, Object.class)),
            (func, args, callContext) -> {
                var key = (StringInstance) args.get(0);
                ContextUtil.setUserData(key.getValue(), args.get(1));
                return FlowExecResult.of(Instances.nullInstance());
            });

    public static final NativeFunctionDef getContext = createDef(
            "string|null getContext(string key)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getContext", String.class)),
            (func, args, callContext) -> {
                var key = (StringInstance) args.get(0);
                return FlowExecResult.of(ContextUtil.getUserData(key.getValue()));
            });

    public static final NativeFunctionDef toString = createDef(
            "string toString(any|null instance)",
            true,
            List.of(ReflectionUtils.getMethod(Object.class, "toString"),
                    ReflectionUtils.getMethod(Objects.class, "toString", Object.class),
                    ReflectionUtils.getMethod(Byte.class, "toString", byte.class),
                    ReflectionUtils.getMethod(Short.class, "toString", short.class),
                    ReflectionUtils.getMethod(Integer.class, "toString", int.class),
                    ReflectionUtils.getMethod(Long.class, "toString", long.class),
                    ReflectionUtils.getMethod(Float.class, "toString", float.class),
                    ReflectionUtils.getMethod(Double.class, "toString", double.class),
                    ReflectionUtils.getMethod(Boolean.class, "toString", boolean.class),
                    ReflectionUtils.getMethod(Character.class, "toString", char.class)
            ),
            (func, args, callContext) -> FlowExecResult.of(Instances.stringInstance(args.get(0).getTitle()))
    );

    public static final NativeFunctionDef requireNonNull = createDef(
            "T requireNonNull<T>(T|null value)",
            true,
            List.of(ReflectionUtils.getMethod(Objects.class, "requireNonNull", Object.class)),
            (func, args, ctx) -> {
                if (args.size() != 1) {
                    throw new IllegalArgumentException("requireNonNull requires exactly one argument");
                }
                var value = args.get(0);
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(StandardTypes.getNullPointerExceptionKlass().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    nat.NullPointerException(ctx);
                    return FlowExecResult.ofException(npe);
                }
            });

    public static final NativeFunctionDef requireNonNull1 = createDef(
            "T requireNonNull1<T>(T|null value, string message)",
            true,
            List.of(ReflectionUtils.getMethod(Objects.class, "requireNonNull", Object.class, String.class)),
            (func, args, ctx) -> {
                if (args.size() != 2) {
                    throw new IllegalArgumentException("requireNonNull requires exactly two arguments");
                }
                var value = args.get(0);
                var message = args.get(1);
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(StandardTypes.getNullPointerExceptionKlass().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    nat.NullPointerException(message, ctx);
                    return FlowExecResult.ofException(npe);
                }
            });

    public static final NativeFunctionDef requireNonNull2 = createDef(
            "T requireNonNull2<T>(T|null value, java.util.function.Supplier<string> messageSupplier)",
            false,
            List.of(ReflectionUtils.getMethod(Objects.class, "requireNonNull", Object.class, Supplier.class)),
            (func, args, ctx) -> {
                if (args.size() != 2) {
                    throw new IllegalArgumentException("requireNonNull requires exactly three arguments");
                }
                var value = args.get(0);
                var messageSupplier = (ClassInstance) args.get(1);
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(StandardTypes.getNullPointerExceptionKlass().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    var getMethod = messageSupplier.getKlass().getMethodByCodeAndParamTypes("get", List.of());
                    var getResult = getMethod.execute(messageSupplier, List.of(), ctx);
                    if (getResult.exception() != null)
                        return FlowExecResult.ofException(getResult.exception());
                    var message = getResult.ret();
                    nat.NullPointerException(message, ctx);
                    return FlowExecResult.ofException(npe);
                }
            });

    public static final NativeFunctionDef dateBefore = createDef(
            "boolean dateBefore(time date1, time date2)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "before", Date.class)),
            (func, args, ctx) -> {
                var date1 = (TimeInstance) args.get(0);
                var date2 = (TimeInstance) args.get(1);
                return FlowExecResult.of(date1.before(date2));
            });

    public static final NativeFunctionDef dateAfter = createDef(
            "boolean dateAfter(time date1, time date2)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "after", Date.class)),
            (func, args, ctx) -> {
                var date1 = (TimeInstance) args.get(0);
                var date2 = (TimeInstance) args.get(1);
                return FlowExecResult.of(date1.after(date2));
            });

    public static final NativeFunctionDef concat = createDef(
            "string concat(string str1, string str2)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "concat", String.class)),
            (func, args, ctx) -> {
                var str1 = (StringInstance) args.get(0);
                var str2 = (StringInstance) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(str1.getValue() + str2.getValue()));
            });

    public static final NativeFunctionDef replace = createDef(
            "string replace(string str, string target, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replace", CharSequence.class, CharSequence.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                var target = (StringInstance) args.get(1);
                var replacement = (StringInstance) args.get(2);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().replace(target.getValue(), replacement.getValue())));
            });

    public static final NativeFunctionDef replaceFirst = createDef(
            "string replaceFirst(string str, string regex, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replaceFirst", String.class, String.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                var regex = (StringInstance) args.get(1);
                var replacement = (StringInstance) args.get(2);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().replaceFirst(regex.getValue(), replacement.getValue())));
            });

    public static final NativeFunctionDef randomUUID = createDef(
            "string randomUUID()",
            true,
            List.of(ReflectionUtils.getMethod(UUIDUtils.class, "randomUUID")),
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(UUID.randomUUID().toString()))
    );

    public static final NativeFunctionDef currentTimeMillis = createDef(
            "long currentTimeMillis()",
            true,
            List.of(ReflectionUtils.getMethod(System.class, "currentTimeMillis")),
            (func, args, ctx) -> FlowExecResult.of(Instances.longInstance(System.currentTimeMillis()))
    );

    public static final NativeFunctionDef equals = createDef(
            "boolean equals(any obj1, any obj2)",
            true,
            List.of(ReflectionUtils.getMethod(Objects.class, "equals", Object.class, Object.class),
                    ReflectionUtils.getMethod(Object.class, "equals", Object.class)),
            (func, args, ctx) -> FlowExecResult.of(Instances.booleanInstance(args.get(0).equals(args.get(1))))
    );

    public static final NativeFunctionDef md5 = createDef(
            "string md5(string str)",
            true,
            List.of(ReflectionUtils.getMethod(MD5Utils.class, "md5", String.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.md5(str.getValue())));
            });

    public static final NativeFunctionDef randomPassword = createDef(
            "string randomPassword()",
            true,
            List.of(ReflectionUtils.getMethod(PasswordUtils.class, "randomPassword")),
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(NncUtils.randomPassword()))
    );

    public static final NativeFunctionDef regexMatch = createDef(
            "boolean regexMatch(string pattern, string str)",
            true,
            List.of(ReflectionUtils.getMethod(RegexUtils.class, "match", String.class, String.class)),
            (func, args, ctx) -> {
                var pattern = (StringInstance) args.get(0);
                var str = (StringInstance) args.get(1);
                return FlowExecResult.of(Instances.booleanInstance(Pattern.compile(pattern.getValue()).matcher(str.getValue()).matches()));
            });

    public static final NativeFunctionDef random = createDef(
            "long random(long bound)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "random", long.class)),
            (func, args, ctx) -> {
                var bound = (LongInstance) args.get(0);
                return FlowExecResult.of(Instances.longInstance(NncUtils.random(bound.getValue())));
            }
    );

    public static final NativeFunctionDef timeToLong = createDef(
            "long timeToLong(time value)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "getTime")),
            (func, args, ctx) -> {
                var date = (TimeInstance) args.get(0);
                return FlowExecResult.of(Instances.longInstance(date.getValue()));
            });

    public static final NativeFunctionDef formatNumber = createDef(
            "string formatNumber(string format, long number)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "formatNumber", String.class, long.class)),
            (func, args, ctx) -> {
                var format = (StringInstance) args.get(0);
                var number = (LongInstance) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(new DecimalFormat(format.getValue()).format(number.getValue())));
            });

    public static final NativeFunctionDef format = createDef(
            "string format(string format, any[] values)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "format", String.class, Object[].class)),
            (func, args, ctx) -> {
                var format = (StringInstance) args.get(0);
                var values = (ArrayInstance) args.get(1);
                var argsArray = new Object[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    argsArray[i] = values.get(i).getTitle();
                }
                return FlowExecResult.of(Instances.stringInstance(String.format(format.getValue(), argsArray)));
            });

    public static final NativeFunctionDef getId = createDef(
            "string getId(any obj)",
            true,
            List.of(ReflectionUtils.getMethod(IdUtils.class, "getId", Object.class)),
            (func, args, ctx) -> {
                var obj = args.get(0);
                if (obj instanceof DurableInstance d) {
                    var id = d.getStringId();
                    if (id != null)
                        return FlowExecResult.of(Instances.stringInstance(id));
                }
                var npe = ClassInstance.allocate(StandardTypes.getNullPointerExceptionKlass().getType());
                var nat = new NullPointerExceptionNative(npe);
                nat.NullPointerException(Instances.stringInstance("Object has no ID"), ctx);
                return FlowExecResult.ofException(npe);
            });


    public static void setDirectMode() {
        functionHolderSupplier = DirectFunctionHolder::new;
        defs.forEach(def -> def.setFunctionHolder(new DirectFunctionHolder()));
    }

    public static void setThreadLocalMode() {
        functionHolderSupplier = ThreadLocalFunctionHolder::new;
        defs.forEach(def -> def.setFunctionHolder(new ThreadLocalFunctionHolder()));
    }

    public static void setEmailSender(EmailSender emailSender) {
        NativeFunctions.emailSender = emailSender;
    }

    public static List<Function> defineSystemFunctions() {
        return defs.stream()
                .filter(NativeFunctionDef::isSystem)
                .map(def -> def.define(null))
                .toList();
    }

    public static List<Function> defineUserFunctions(DefContext defContext) {
        return defs.stream()
                .filter(def -> !def.isSystem())
                .map(def -> def.define(defContext))
                .toList();
    }

    public static void initializeFromDefContext(DefContext defContext) {
        for (NativeFunctionDef def : defs) {
            def.set(Objects.requireNonNull(
                    defContext.selectFirstByKey(Function.UNIQUE_IDX_CODE, def.getName()),
                    "Function not found: " + def.getName())
            );
        }
    }

    public static List<NativeFunctionDef> defs() {
        return Collections.unmodifiableList(defs);
    }

    private static class DirectFunctionHolder implements FunctionHolder {

        private Function function;

        @Override
        public Function get() {
            return function;
        }

        @Override
        public void set(Function value) {
            this.function = value;
        }
    }

    private static class ThreadLocalFunctionHolder implements FunctionHolder {

        private final ThreadLocal<Function> TL = new ThreadLocal<>();

        @Override
        public Function get() {
            return TL.get();
        }

        @Override
        public void set(Function value) {
            TL.set(value);
        }
    }

    private static NativeFunctionDef createDef(String signature, boolean system, List<Method> javaMethods, FunctionImpl impl) {
        var def = new NativeFunctionDef(signature, system, javaMethods, functionHolderSupplier.get(), impl);
        defs.add(def);
        return def;
    }

}
