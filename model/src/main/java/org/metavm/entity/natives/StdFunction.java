package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.lang.EmailUtils;
import org.metavm.api.lang.*;
import org.metavm.common.ErrorCode;
import org.metavm.entity.DefContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.user.Session;
import org.metavm.util.*;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Slf4j
public enum StdFunction implements ValueHolderOwner<Function> {

    isSourcePresent(
            "boolean isSourcePresent(any view)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof Reference durableInstance)
                    return FlowExecResult.of(Instances.booleanInstance(durableInstance.resolve().tryGetSource() != null));
                else
                    throw new InternalException("Can not get source of a non-durable instance: " + args.get(0));
            }),
    getSource(
            "any getSource(any view)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof Reference durableInstance)
                    return FlowExecResult.of(durableInstance.resolve().getSource());
                else
                    throw new InternalException("Can not get source of a non-durable instance: " + args.get(0));
            }),
    setSource(
            "void setSource(any view, any source)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof Reference ref) {
                    ref.resolve().setSourceRef(new SourceRef((Reference) args.get(1), null));
                    return FlowExecResult.of(Instances.nullInstance());
                } else
                    throw new InternalException("Can not set source for a non-durable instance: " + args.get(0));
            }),
    functionToInstance(
            "T functionToInstance<T>(any function)", true, List.of(),
            (func, args, callContext) -> {
                if (args.get(0) instanceof FunctionValue functionValue) {
                    var samInterface = ((ClassType) func.getTypeArguments().get(0)).resolve();
                    var type = Types.createSAMInterfaceImpl(samInterface, functionValue);
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
                var key = ((StringValue) args.get(0)).getValue();
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
                var key = ((StringValue) args.get(0)).getValue();
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
                var key = ((StringValue) args.get(0)).getValue();
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
                if (instance instanceof Reference ref) {
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
                var key = (StringValue) args.get(0);
                ContextUtil.setUserData(key.getValue(), args.get(1));
                return FlowExecResult.of(Instances.nullInstance());
            }),
    getContext(
            "any|null getContext(string key)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getContext", String.class)),
            (func, args, callContext) -> {
                var key = (StringValue) args.get(0);
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
    hashCode(
            "long hashCode(any instance)",
            true,
            List.of(ReflectionUtils.getMethod(Object.class, "hashCode"),
                    ReflectionUtils.getMethod(Byte.class, "hashCode"),
                    ReflectionUtils.getMethod(Short.class, "hashCode"),
                    ReflectionUtils.getMethod(Integer.class, "hashCode"),
                    ReflectionUtils.getMethod(Long.class, "hashCode"),
                    ReflectionUtils.getMethod(Float.class, "hashCode"),
                    ReflectionUtils.getMethod(Double.class, "hashCode"),
                    ReflectionUtils.getMethod(Boolean.class, "hashCode"),
                    ReflectionUtils.getMethod(String.class, "hashCode"),
                    ReflectionUtils.getMethod(Character.class, "hashCode")
            ),
            (func, args, callContext) -> {
                var value = args.get(0);
                if(value instanceof PrimitiveValue primitiveValue)
                    return FlowExecResult.of(Instances.longInstance(primitiveValue.getValue().hashCode()));
                else {
                    var exception = ClassInstance.allocate(StdKlass.illegalArgumentException.get().getType());
                    var nat = new IllegalArgumentExceptionNative(exception);
                    nat.IllegalArgumentException(Instances.stringInstance("Non-primitive value is not yet supported: " + value), callContext);
                    return FlowExecResult.ofException(exception);
                }
            }
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
                var date1 = (TimeValue) args.get(0);
                var date2 = (TimeValue) args.get(1);
                return FlowExecResult.of(date1.before(date2));
            }),
    dateAfter(
            "boolean dateAfter(time date1, time date2)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "after", Date.class)),
            (func, args, ctx) -> {
                var date1 = (TimeValue) args.get(0);
                var date2 = (TimeValue) args.get(1);
                return FlowExecResult.of(date1.after(date2));
            }),
    concat(
            "string concat(string str1, string str2)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "concat", String.class)),
            (func, args, ctx) -> {
                var str1 = (StringValue) args.get(0);
                var str2 = (StringValue) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(str1.getValue() + str2.getValue()));
            }),
    replace(
            "string replace(string str, string target, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replace", CharSequence.class, CharSequence.class)),
            (func, args, ctx) -> {
                var str = (StringValue) args.get(0);
                var target = (StringValue) args.get(1);
                var replacement = (StringValue) args.get(2);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().replace(target.getValue(), replacement.getValue())));
            }),
    replaceFirst(
            "string replaceFirst(string str, string regex, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replaceFirst", String.class, String.class)),
            (func, args, ctx) -> {
                var str = (StringValue) args.get(0);
                var regex = (StringValue) args.get(1);
                var replacement = (StringValue) args.get(2);
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
                var str = (StringValue) args.get(0);
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
                var pattern = (StringValue) args.get(0);
                var str = (StringValue) args.get(1);
                return FlowExecResult.of(Instances.booleanInstance(Pattern.compile(pattern.getValue()).matcher(str.getValue()).matches()));
            }),
    random(
            "long random(long bound)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "random", long.class)),
            (func, args, ctx) -> {
                var bound = (LongValue) args.get(0);
                return FlowExecResult.of(Instances.longInstance(NncUtils.random(bound.getValue())));
            }
    ),
    timeToLong(
            "long timeToLong(time value)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "getTime")),
            (func, args, ctx) -> {
                var date = (TimeValue) args.get(0);
                return FlowExecResult.of(Instances.longInstance(date.getValue()));
            }),
    formatNumber(
            "string formatNumber(string format, long number)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "formatNumber", String.class, long.class)),
            (func, args, ctx) -> {
                var format = (StringValue) args.get(0);
                var number = (LongValue) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(new DecimalFormat(format.getValue()).format(number.getValue())));
            }),
    format(
            "string format(string format, (any|null)[] values)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "format", String.class, Object[].class)),
            (func, args, ctx) -> {
                var format = (StringValue) args.get(0);
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
                var str = (StringValue) args.get(0);
                var beginIndex = (LongValue) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(str.getValue().substring(beginIndex.getValue().intValue())));
            }
    ),
    substring1(
            "string substring1(string s, long beginIndex, long endIndex)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "substring", int.class, int.class)),
            (func, args, ctx) -> {
                var str = (StringValue) args.get(0);
                var beginIndex = ((LongValue) args.get(1)).getValue().intValue();
                var endIndex = ((LongValue) args.get(2)).getValue().intValue();
                return FlowExecResult.of(Instances.stringInstance(str.getValue().substring(beginIndex, endIndex)));
            }
    ),
    getId(
            "string getId(any obj)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getId", Object.class)),
            (func, args, ctx) -> {
                var obj = args.get(0);
                if (obj instanceof Reference d) {
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
                        ((StringValue) args.get(0)).getValue(),
                        ((StringValue) args.get(1)).getValue(),
                        ((StringValue) args.get(2)).getValue()
                );
                return FlowExecResult.of(Instances.nullInstance());
            }),
    secureRandom(
            "string secureRandom(long length)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "secureRandom", int.class)),
            (func, args, callContext) -> {
                var len = ((LongValue) args.get(0)).getValue().intValue();
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.secureRandom(len)));
            }
    ),
    secureHash(
            "string secureHash(string value, string|null salt)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "secureHash", String.class, String.class)),
            (func, args, callContext) -> {
                var v = ((StringValue) args.get(0)).getValue();
                var s = args.get(1) instanceof StringValue str ? str.getValue() : null;
                var h = EncodingUtils.secureHash(v, s);
                return FlowExecResult.of(Instances.stringInstance(h));
            }
    ),
    getParent(
            "any|null getParent(any object)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getParent", Object.class)),
            (func, args, callContext) -> {
                var obj = args.get(0).resolveDurable();
                return FlowExecResult.of(NncUtils.getOrElse(obj.getParent(), Instance::getReference, Instances.nullInstance()));
            }
    ),
    getRoot(
            "any getRoot(any object)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getRoot", Object.class)),
            (func, args, callContext) -> {
                var obj = args.get(0).resolveDurable();
                return FlowExecResult.of(obj.getRoot().getReference());
            }
    ),
    compareLong(
            "long compareLong(long l1, long l2) ",
            true,
            List.of(
                    ReflectionUtils.getMethod(Byte.class, "compareTo", Byte.class),
                    ReflectionUtils.getMethod(Short.class, "compareTo", Short.class),
                    ReflectionUtils.getMethod(Integer.class, "compareTo", Integer.class),
                    ReflectionUtils.getMethod(Long.class, "compareTo", Long.class),
                    ReflectionUtils.getMethod(Byte.class, "compare", byte.class, byte.class),
                    ReflectionUtils.getMethod(Short.class, "compare", short.class, short.class),
                    ReflectionUtils.getMethod(Integer.class, "compare", int.class, int.class),
                    ReflectionUtils.getMethod(Long.class, "compare", long.class, long.class)
            ),
            (func, args, callContext) -> {
                var v1 = (LongValue) args.get(0);
                var v2 = (LongValue) args.get(1);
                return FlowExecResult.of(Instances.longInstance(Long.compare(v1.getValue(), v2.getValue())));
            }
    ),
    compareString(
            "long compareString(string s1, string s2) ",
            true,
            List.of(
                    ReflectionUtils.getMethod(String.class, "compareTo", String.class)
            ),
            (func, args, callContext) -> {
                var s1 = (StringValue) args.get(0);
                var s2 = (StringValue) args.get(1);
                return FlowExecResult.of(Instances.longInstance(s1.getValue().compareTo(s2.getValue())));
            }
    ),
    isEmptyString(
            "boolean isEmptyString(string s)",
            true,
            List.of(
                    ReflectionUtils.getMethod(String.class, "isEmpty")
            ),
            (func, args, callContext) -> {
                var s = (StringValue) args.get(0);
                return FlowExecResult.of(Instances.booleanInstance(s.getValue().isEmpty()));
            }
    ),
    sortList(
            "void sortList(java.util.List<[never, any]> list)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Collections.class, "sort", List.class)
            ),
            (func, args, callContext) -> {
                var list = args.get(0).resolveObject();
                var nat = new ListNative(list);
                nat.sort(callContext);
                return FlowExecResult.of(Instances.nullInstance());
            }
    ),
    sortArray(
            "void sortArray([never, any|null][] array, long from, long to, java.util.Comparator<[never,any]>|null comparator)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "sort", Object[].class, int.class, int.class, Comparator.class)
            ),
            (func, args, callContext) -> {
                var array = args.get(0).resolveArray();
                var from = ((LongValue) args.get(1)).getValue().intValue();
                var to = ((LongValue) args.get(2)).getValue().intValue();
                var c = args.get(3);
                if(c.isNull())
                    array.sort(from, to, (e1,e2) -> Instances.compare(e1, e2, callContext));
                else {
                    var comparator = c.resolveObject();
                    var cmpMethod = requireNonNull(comparator.getKlass().findMethodByVerticalTemplate(StdMethod.comparatorCompare.get()));
                    array.sort(from, to, (e1, e2) -> Instances.toInt(
                            Flows.invokeVirtual(cmpMethod, comparator, List.of(e1,e2), callContext
                    )));
                }
                return FlowExecResult.of(Instances.nullInstance());
            }
    ),
    copyOfArray(
            "(T|null)[] copyOfArray<T>((T|null)[] array, long newLength)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOf", Object[].class, int.class)
            ),
            (func, args, callContext) -> {
                var array = args.get(0).resolveArray();
                var newLength = ((LongValue) args.get(1)).getValue().intValue();
                return FlowExecResult.of(array.copyOf(0, newLength).getReference());
            }
    ),
    copyOfArray2(
            "(T|null)[] copyOfArray2<T, U>((U|null)[] array, long newLength, org.metavm.object.type.Klass newType)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOf", Object[].class, int.class, Class.class)
            ),
            (func, args, callContext) -> {
                var array = args.get(0).resolveArray();
                var newLength = ((LongValue) args.get(1)).getValue().intValue();
                var newType = new ArrayType(Types.getNullableType(func.getTypeArguments().get(0)), ArrayKind.READ_WRITE);
                return FlowExecResult.of(array.copyOf(0, newLength, newType).getReference());
            }
    ),
    copyOfArrayRange(
            "(T|null)[] copyOfArrayRange<T>((T|null)[] array, long from, long to)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOfRange", Object[].class, int.class, int.class)
            ),
            (func, args, callContext) -> {
                var array = args.get(0).resolveArray();
                var from = ((LongValue) args.get(1)).getValue().intValue();
                var to = ((LongValue) args.get(2)).getValue().intValue();
                return FlowExecResult.of(array.copyOf(from, to).getReference());
            }
    ),
    copyOfArrayRange2(
            "(T|null)[] copyOfArrayRange2<T, U>((U|null)[] array, long from, long to, org.metavm.object.type.Klass newType)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOfRange", Object[].class, int.class, int.class, Class.class)
            ),
            (func, args, callContext) -> {
                var array = args.get(0).resolveArray();
                var from = ((LongValue) args.get(1)).getValue().intValue();
                var to = ((LongValue) args.get(2)).getValue().intValue();
                var newType = new ArrayType(Types.getNullableType(func.getTypeArguments().get(0)), ArrayKind.READ_WRITE);
                return FlowExecResult.of(array.copyOf(from, to, newType).getReference());
            }
    ),
    arraycopy(
            "void arraycopy(any source, long srcPos, any dest, long destPos, long length)",
            false,
            List.of(
                    ReflectionUtils.getMethod(System.class, "arraycopy", Object.class, int.class, Object.class, int.class, int.class)
            ),
            (func, args, callContext) -> {
                var src = args.get(0).resolveArray();
                var srcPos = ((LongValue) args.get(1)).getValue().intValue();
                var dest = args.get(2).resolveArray();
                var destPos = ((LongValue) args.get(3)).getValue().intValue();
                var length = ((LongValue) args.get(4)).getValue().intValue();
                for(int i = srcPos, j = destPos, k = 0; k < length; i++, j++, k++)
                    dest.setElement(j, src.getElement(i));
                return FlowExecResult.of(Instances.nullInstance());
            }
    ),
    reverse(
            "void reverse(java.util.List<[never, any]> list)",
            false,
            List.of(ReflectionUtils.getMethod(Collections.class, "reverse", List.class)),
            (func, args, callContext) -> {
                var list = args.get(0).resolveObject();
                var nat = new ListNative(list);
                nat.reverse();
                return FlowExecResult.of(Instances.nullInstance());
            }
    ),
    getClass(
            "org.metavm.object.type.Klass getClass(any o)",
            false,
            List.of(ReflectionUtils.getMethod(Object.class, "getClass")),
            (func, args, callContext) -> {
                var o = args.get(0).resolveDurable();
                var entityContext = ContextUtil.getEntityContext();
                return FlowExecResult.of(entityContext.getInstance(
                        Instances.getGeneralClass(o).getEffectiveTemplate()).getReference()
                );
            }
    ),
    stringLength(
            "long stringLength(string s)",
            false,
            List.of(ReflectionUtils.getMethod(String.class, "length")),
            (func, args, callContext) -> {
                var s = (StringValue) args.get(0);
                return FlowExecResult.of(Instances.longInstance(s.getValue().length()));
            }
    ),
    newArray(
            "any newArray(org.metavm.object.type.Klass klass, long length)",
            false,
            List.of(ReflectionUtils.getMethod(Array.class, "newInstance", Class.class, int.class)),
            (func, args, callContext) -> {
                var k = ContextUtil.getEntityContext().getEntity(Klass.class, args.get(0).resolveObject());
                var len = ((LongValue) args.get(1)).getValue().intValue();
                var type = new ArrayType(Types.getNullableType(Types.getGeneralType(k)), ArrayKind.READ_WRITE);
                var array = new ArrayInstance(type);
                Instances.initArray(array, new int[] {len}, 0);
                return FlowExecResult.of(array.getReference());
            }
    ),
    getComponentClass(
            "org.metavm.object.type.Klass getComponentClass(org.metavm.object.type.Klass klass)",
            false,
            List.of(ReflectionUtils.getMethod(Class.class, "getComponentType")),
            (func, args, callContext) -> {
                var k = ContextUtil.getEntityContext().getEntity(Klass.class, args.get(0).resolveObject());
                var c = ContextUtil.getEntityContext().getInstance(Objects.requireNonNull(k.getComponentKlass()));
                return FlowExecResult.of(c.getReference());
            }
    )
    ;

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
        this.functionHolder = new HybridValueHolder<>();
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

    public static void initializeFromDefContext(DefContext defContext, boolean local) {
        for (StdFunction def : values()) {
            var func = requireNonNull(
                    defContext.selectFirstByKey(Function.UNIQUE_IDX_CODE, def.getName()),
                    "Function not found: " + def.getName());
            if(local)
                def.setLocal(func);
            else
                def.set(func);
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

    public void setLocal(Function function) {
        functionHolder.setLocal(function);
    }

    public Function get() {
        return functionHolder.get();
    }

    public void setValueHolder(ValueHolder<Function> functionHolder) {
        this.functionHolder = functionHolder;
    }

    @Override
    public ValueHolder<Function> getValueHolder() {
        return functionHolder;
    }

    public FunctionImpl getImpl() {
        return impl;
    }
}
