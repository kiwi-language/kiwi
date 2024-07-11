package org.metavm.entity.natives;

import org.metavm.api.lang.EmailUtils;
import org.metavm.api.lang.*;
import org.metavm.common.ErrorCode;
import org.metavm.entity.DefContext;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.TypeParserImpl;
import org.metavm.object.type.Types;
import org.metavm.user.Session;
import org.metavm.util.*;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public enum StdFunction implements ValueHolderOwner<Function> {

    isSourcePresent(
            "boolean isSourcePresent(any view)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof InstanceReference durableInstance)
                    return FlowExecResult.of(Instances.booleanInstance(durableInstance.resolve().tryGetSource() != null));
                else
                    throw new InternalException("Can not get source of a non-durable instance: " + args.get(0));
            }),
    getSource(
            "any getSource(any view)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof InstanceReference durableInstance)
                    return FlowExecResult.of(durableInstance.resolve().getSource());
                else
                    throw new InternalException("Can not get source of a non-durable instance: " + args.get(0));
            }),
    setSource(
            "void setSource(any view, any source)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof InstanceReference ref) {
                    ref.resolve().setSourceRef(new SourceRef((InstanceReference) args.get(1), null));
                    return FlowExecResult.of(Instances.nullInstance());
                } else
                    throw new InternalException("Can not set source for a non-durable instance: " + args.get(0));
            }),
    functionToInstance(
            "T functionToInstance<T>(any func)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof FunctionInstance functionInstance) {
                    var samInterface = ((ClassType) func.getTypeArguments().get(0)).resolve();
                    var type = Types.createSAMInterfaceImpl(samInterface, functionInstance);
                    return FlowExecResult.of(new ClassInstance(null, Map.of(), type).getReference());
                } else {
                    throw new InternalException("Invalid function instance: " + Instances.getInstancePath(args.get(0)));
                }
            }),
    getSessionEntry(
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
            }),
    setSessionEntry(
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
            }),
    removeSessionEntry(
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
            }),
    typeCast(
            "T typeCast<T>(any|null instance)",
            true,
            List.of(),
            (func, args, callContext) -> {
                var type = func.getTypeArguments().get(0);
                var value = args.get(0);
                if (type.isInstance(value))
                    return FlowExecResult.of(value);
                else if(type.isConvertibleFrom(value.getType()))
                    return FlowExecResult.of(type.convert(value));
                else
                    throw new BusinessException(ErrorCode.TYPE_CAST_ERROR, value.getType(), type);
            }),
    print(
            "void print(any|null content)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "print", Object.class)),
            (func, args, callContext) -> {
                System.out.println(args.get(0).getTitle());
                return FlowExecResult.of(Instances.nullInstance());
            }),
    delete(
            "void delete(any instance)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "delete", Object.class)),
            (func, args, callContext) -> {
                var entityContext = ContextUtil.getEntityContext();
                var instance = args.get(0);
                if (instance instanceof InstanceReference ref) {
                    entityContext.getInstanceContext().remove(ref.resolve());
                    return FlowExecResult.of(Instances.nullInstance());
                } else
                    throw new BusinessException(ErrorCode.DELETE_NON_DURABLE_INSTANCE, instance);
            }),
    setContext(
            "void setContext(string key, any value)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "setContext", String.class, Object.class)),
            (func, args, callContext) -> {
                var key = (StringInstance) args.get(0);
                ContextUtil.setUserData(key.getValue(), args.get(1));
                return FlowExecResult.of(Instances.nullInstance());
            }),
    getContext(
            "any|null getContext(string key)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getContext", String.class)),
            (func, args, callContext) -> {
                var key = (StringInstance) args.get(0);
                return FlowExecResult.of(ContextUtil.getUserData(key.getValue()));
            }),
    toString(
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
    ),
    requireNonNull(
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
                    var npe = ClassInstance.allocate(StdKlass.nullPointerException.get().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    nat.NullPointerException(ctx);
                    return FlowExecResult.ofException(npe);
                }
            }),
    requireNonNull1(
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
                    var npe = ClassInstance.allocate(StdKlass.nullPointerException.get().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    nat.NullPointerException(message, ctx);
                    return FlowExecResult.ofException(npe);
                }
            }),
    requireNonNull2(
            "T requireNonNull2<T>(T|null value, java.util.function.Supplier<string> messageSupplier)",
            false,
            List.of(ReflectionUtils.getMethod(Objects.class, "requireNonNull", Object.class, Supplier.class)),
            (func, args, ctx) -> {
                if (args.size() != 2) {
                    throw new IllegalArgumentException("requireNonNull requires exactly three arguments");
                }
                var value = args.get(0);
                var messageSupplier = args.get(1).resolveObject();
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(StdKlass.nullPointerException.get().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    var getMethod = messageSupplier.getKlass().getMethodByCodeAndParamTypes("get", List.of());
                    var getResult = getMethod.execute(messageSupplier, List.of(), ctx);
                    if (getResult.exception() != null)
                        return FlowExecResult.ofException(getResult.exception());
                    var message = getResult.ret();
                    nat.NullPointerException(message, ctx);
                    return FlowExecResult.ofException(npe);
                }
            }),
    dateBefore(
            "boolean dateBefore(time date1, time date2)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "before", Date.class)),
            (func, args, ctx) -> {
                var date1 = (TimeInstance) args.get(0);
                var date2 = (TimeInstance) args.get(1);
                return FlowExecResult.of(date1.before(date2));
            }),
    dateAfter(
            "boolean dateAfter(time date1, time date2)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "after", Date.class)),
            (func, args, ctx) -> {
                var date1 = (TimeInstance) args.get(0);
                var date2 = (TimeInstance) args.get(1);
                return FlowExecResult.of(date1.after(date2));
            }),
    concat(
            "string concat(string str1, string str2)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "concat", String.class)),
            (func, args, ctx) -> {
                var str1 = (StringInstance) args.get(0);
                var str2 = (StringInstance) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(str1.getValue() + str2.getValue()));
            }),
    replace(
            "string replace(string str, string target, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replace", CharSequence.class, CharSequence.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                var target = (StringInstance) args.get(1);
                var replacement = (StringInstance) args.get(2);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().replace(target.getValue(), replacement.getValue())));
            }),
    replaceFirst(
            "string replaceFirst(string str, string regex, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replaceFirst", String.class, String.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                var regex = (StringInstance) args.get(1);
                var replacement = (StringInstance) args.get(2);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().replaceFirst(regex.getValue(), replacement.getValue())));
            }),
    randomUUID(
            "string randomUUID()",
            true,
            List.of(ReflectionUtils.getMethod(UUIDUtils.class, "randomUUID")),
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(UUID.randomUUID().toString()))
    ),
    currentTimeMillis(
            "long currentTimeMillis()",
            true,
            List.of(ReflectionUtils.getMethod(System.class, "currentTimeMillis")),
            (func, args, ctx) -> FlowExecResult.of(Instances.longInstance(System.currentTimeMillis()))
    ),
    equals(
            "boolean equals(any obj1, any obj2)",
            true,
            List.of(ReflectionUtils.getMethod(Objects.class, "equals", Object.class, Object.class),
                    ReflectionUtils.getMethod(Object.class, "equals", Object.class)),
            (func, args, ctx) -> FlowExecResult.of(Instances.booleanInstance(args.get(0).equals(args.get(1))))
    ),
    md5(
            "string md5(string str)",
            true,
            List.of(ReflectionUtils.getMethod(MD5Utils.class, "md5", String.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.md5(str.getValue())));
            }),
    randomPassword(
            "string randomPassword()",
            true,
            List.of(ReflectionUtils.getMethod(PasswordUtils.class, "randomPassword")),
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(NncUtils.randomPassword()))
    ),
    regexMatch(
            "boolean regexMatch(string pattern, string str)",
            true,
            List.of(ReflectionUtils.getMethod(RegexUtils.class, "match", String.class, String.class)),
            (func, args, ctx) -> {
                var pattern = (StringInstance) args.get(0);
                var str = (StringInstance) args.get(1);
                return FlowExecResult.of(Instances.booleanInstance(Pattern.compile(pattern.getValue()).matcher(str.getValue()).matches()));
            }),
    random(
            "long random(long bound)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "random", long.class)),
            (func, args, ctx) -> {
                var bound = (LongInstance) args.get(0);
                return FlowExecResult.of(Instances.longInstance(NncUtils.random(bound.getValue())));
            }
    ),
    timeToLong(
            "long timeToLong(time value)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "getTime")),
            (func, args, ctx) -> {
                var date = (TimeInstance) args.get(0);
                return FlowExecResult.of(Instances.longInstance(date.getValue()));
            }),
    formatNumber(
            "string formatNumber(string format, long number)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "formatNumber", String.class, long.class)),
            (func, args, ctx) -> {
                var format = (StringInstance) args.get(0);
                var number = (LongInstance) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(new DecimalFormat(format.getValue()).format(number.getValue())));
            }),
    format(
            "string format(string format, any[] values)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "format", String.class, Object[].class)),
            (func, args, ctx) -> {
                var format = (StringInstance) args.get(0);
                var values = args.get(1).resolveArray();
                var argsArray = new Object[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    argsArray[i] = values.get(i).getTitle();
                }
                return FlowExecResult.of(Instances.stringInstance(String.format(format.getValue(), argsArray)));
            }),
    substring(
            "string substring(string s, long beginIndex)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "substring", int.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                var beginIndex = (LongInstance) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().substring(beginIndex.getValue().intValue())));
            }
    ),
    substring1(
            "string substring1(string s, long beginIndex, long endIndex)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "substring", int.class, int.class)),
            (func, args, ctx) -> {
                var str = (StringInstance) args.get(0);
                var beginIndex = ((LongInstance) args.get(1)).getValue().intValue();
                var endIndex = ((LongInstance) args.get(2)).getValue().intValue();
                return FlowExecResult.of(Instances.stringInstance(str.getValue().substring(beginIndex, endIndex)));
            }
    ),
    getId(
            "string getId(any obj)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getId", Object.class)),
            (func, args, ctx) -> {
                var obj = args.get(0);
                if (obj instanceof InstanceReference d) {
                    var id = d.getStringId();
                    if (id != null)
                        return FlowExecResult.of(Instances.stringInstance(id));
                }
                var npe = ClassInstance.allocate(StdKlass.nullPointerException.get().getType());
                var nat = new NullPointerExceptionNative(npe);
                nat.NullPointerException(Instances.stringInstance("Object has no ID"), ctx);
                return FlowExecResult.ofException(npe);
            }),
    sendEmail(
            "void sendEmail(string recipient, string subject, string content)",
            false,
            List.of(ReflectionUtils.getMethod(EmailUtils.class, "send", String.class, String.class, String.class)),
            (func, args, callContext) -> {
                Constants.emailSender.send(
                        ((StringInstance) args.get(0)).getValue(),
                        ((StringInstance) args.get(1)).getValue(),
                        ((StringInstance) args.get(2)).getValue()
                );
                return FlowExecResult.of(Instances.nullInstance());
            }),
    secureRandom(
            "string secureRandom(long length)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "secureRandom", int.class)),
            (func, args, callContext) -> {
                var len = ((LongInstance) args.get(0)).getValue().intValue();
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.secureRandom(len)));
            }
    ),
    secureHash(
            "string secureHash(string value, string|null salt)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "secureHash", String.class, String.class)),
            (func, args, callContext) -> {
                var v = ((StringInstance) args.get(0)).getValue();
                var s = args.get(1) instanceof StringInstance str ? str.getValue() : null;
                var h = EncodingUtils.secureHash(v, s);
                return FlowExecResult.of(Instances.stringInstance(h));
            }
    );

    private final String name;
    private final String signature;
    private final boolean system;
    private ValueHolder<Function> functionHolder;
    private final FunctionImpl impl;
    private final List<Method> javaMethods;

    StdFunction(String signature, boolean system, List<Method> javaMethods, FunctionImpl impl) {
        this.signature = signature;
        this.system = system;
        this.javaMethods = new ArrayList<>(javaMethods);
        this.impl = impl;
        this.functionHolder = new DirectValueHolder<>();
        var typeParser = new TypeParserImpl((String name) -> {
            throw new NullPointerException("defContext is null");
        });
        this.name = typeParser.getFunctionName(signature);
    }

    public static void setEmailSender(EmailSender emailSender) {
        Constants.emailSender = emailSender;
    }

    public static List<Function> defineSystemFunctions() {
        return Arrays.stream(values())
                .filter(StdFunction::isSystem)
                .map(def -> def.define(null))
                .toList();
    }

    public static List<Function> defineUserFunctions(DefContext defContext) {
        return Arrays.stream(values())
                .filter(def -> !def.isSystem())
                .map(def -> def.define(defContext))
                .toList();
    }

    public static void initializeFromDefContext(DefContext defContext) {
        for (StdFunction def : values()) {
            def.set(Objects.requireNonNull(
                    defContext.selectFirstByKey(Function.UNIQUE_IDX_CODE, def.getName()),
                    "Function not found: " + def.getName())
            );
        }
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
                    if (defContext != null)
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

    public void setValueHolder(ValueHolder<Function> functionHolder) {
        this.functionHolder = functionHolder;
    }

    public FunctionImpl getImpl() {
        return impl;
    }
}
