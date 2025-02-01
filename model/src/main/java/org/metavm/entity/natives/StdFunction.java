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

    functionToInstance(
            "T functionToInstance<T>(any function)", true, List.of(),
            (func, args, callContext) -> {
                if (args.getFirst() instanceof FunctionValue functionValue) {
                    var samInterface = ((ClassType) func.getTypeArguments().getFirst());
                    var type = Types.createSAMInterfaceImpl(samInterface, functionValue);
                    return FlowExecResult.of(new MvClassInstance(null, Map.of(), type).getReference());
                } else {
                    throw new InternalException("Invalid function instance: " + Instances.getInstancePath(args.getFirst()));
                }
            }),
    getSessionEntry(
            "any|null getSessionEntry(string key)",
            false,
            List.of(ReflectionUtils.getMethod(SessionUtils.class, "getEntry", String.class)),
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, Instances.stringInstance(ContextUtil.getToken()));
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                var value = session.getEntry(key);
                return FlowExecResult.of(Utils.orElse(value, Instances.nullInstance()));
            }),
    setSessionEntry(
            "void setSessionEntry(string key, any value)",
            false,
            List.of(ReflectionUtils.getMethod(SessionUtils.class, "setEntry", String.class, Object.class)),
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                var value = args.get(1);
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, Instances.stringInstance(ContextUtil.getToken()));
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                session.setEntry(key, value);
                return FlowExecResult.of(null);
            }),
    removeSessionEntry(
            "boolean removeSessionEntry(string key)",
            false,
            List.of(ReflectionUtils.getMethod(SessionUtils.class, "removeEntry", String.class)),
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, Instances.stringInstance(ContextUtil.getToken()));
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                return FlowExecResult.of(Instances.intInstance(session.removeEntry(key)));
            }),
    typeCast(
            "T typeCast<T>(any|null instance)",
            true,
            List.of(),
            (func, args, callContext) -> {
                var type = func.getTypeArguments().getFirst();
                var value = args.getFirst();
                if (type.isInstance(value))
                    return FlowExecResult.of(value);
                else {
                    var exception = ClassInstance.allocate(StdKlass.classCastException.type());
                    var nat = new ClassCastExceptionNative(exception);
                    var msg = value.getValueType().getTypeDesc() + " cannot be cast to " + type.getTypeDesc();
                    nat.ClassCastException(Instances.stringInstance(msg), callContext);
                    return FlowExecResult.ofException(exception);
                }
            }),
    print(
            "void print(any|null content)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "print", Object.class)),
            (func, args, callContext) -> {
                System.out.println(args.getFirst().getTitle());
                return FlowExecResult.of(null);
            }),
    delete(
            "void delete(any instance)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "delete", Object.class)),
            (func, args, callContext) -> {
                var entityContext = ContextUtil.getEntityContext();
                var instance = args.getFirst();
                if (instance instanceof Reference ref) {
                    entityContext.remove(ref.get());
                    return FlowExecResult.of(null);
                } else
                    throw new BusinessException(ErrorCode.DELETE_NON_DURABLE_INSTANCE, instance);
            }),
    setContext(
            "void setContext(string key, any value)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "setContext", String.class, Object.class)),
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                ContextUtil.setUserData(key, args.get(1));
                return FlowExecResult.of(null);
            }),
    getContext(
            "any|null getContext(string key)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getContext", String.class)),
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                return FlowExecResult.of(ContextUtil.getUserData(key));
            }),
    toString(
            "string toString(any|null instance)",
            true,
            List.of(ReflectionUtils.getMethod(Object.class, "toString"),
                    ReflectionUtils.getMethod(Objects.class, "toString", Object.class)
            ),
            (func, args, callContext) -> FlowExecResult.of(
                    Instances.stringInstance(Instances.toString(args.getFirst(), callContext)))
    ),
    hashCode(
            "int hashCode(any instance)",
            true,
            List.of(ReflectionUtils.getMethod(Object.class, "hashCode")
            ),
            (func, args, callContext) -> {
                var value = args.getFirst();
                return FlowExecResult.of(Instances.intInstance(Instances.hashCode(value, callContext)));
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
                var value = args.getFirst();
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
                var value = args.getFirst();
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
                var value = args.getFirst();
                var messageSupplier = args.get(1).resolveObject();
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(StdKlass.nullPointerException.get().getType());
                    var nat = new NullPointerExceptionNative(npe);
                    var getMethod = messageSupplier.getInstanceType().getMethodByNameAndParamTypes("get", List.of());
                    var getResult = getMethod.execute(messageSupplier.getReference(), List.of(), ctx);
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
                var date1 = (TimeValue) args.getFirst();
                var date2 = (TimeValue) args.get(1);
                return FlowExecResult.of(date1.before(date2));
            }),
    dateAfter(
            "boolean dateAfter(time date1, time date2)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "after", Date.class)),
            (func, args, ctx) -> {
                var date1 = (TimeValue) args.getFirst();
                var date2 = (TimeValue) args.get(1);
                return FlowExecResult.of(date1.after(date2));
            }),
    replace(
            "string replace(string str, string target, string replacement)",
            true,
            List.of(ReflectionUtils.getMethod(String.class, "replace", CharSequence.class, CharSequence.class)),
            (func, args, ctx) -> {
                var str = Instances.toJavaString(args.getFirst());
                var target = Instances.toJavaString(args.get(1));
                var replacement = Instances.toJavaString(args.get(2));
                return FlowExecResult.of(Instances.stringInstance(str.replace(target, replacement)));
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
            (func, args, ctx) -> FlowExecResult.of(Instances.intInstance(Instances.equals(args.getFirst(), args.getLast(), ctx)))
    ),
    md5(
            "string md5(string str)",
            true,
            List.of(ReflectionUtils.getMethod(MD5Utils.class, "md5", String.class)),
            (func, args, ctx) -> {
                var str = Instances.toJavaString(args.getFirst());
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.md5(str)));
            }),
    randomPassword(
            "string randomPassword()",
            true,
            List.of(ReflectionUtils.getMethod(PasswordUtils.class, "randomPassword")),
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(Utils.randomPassword()))
    ),
    password(
            "password passwd(string s)",
            true,
            List.of(),
            (func, args, ctx) -> {
                var s = Instances.toJavaString(args.getFirst());
                return FlowExecResult.of(Instances.passwordInstance(EncodingUtils.md5(s)));
            }
    ),
    regexMatch(
            "boolean regexMatch(string pattern, string str)",
            true,
            List.of(ReflectionUtils.getMethod(RegexUtils.class, "match", String.class, String.class)),
            (func, args, ctx) -> {
                var pattern = Instances.toJavaString(args.getFirst());
                var str = Instances.toJavaString(args.get(1));
                return FlowExecResult.of(Instances.intInstance(Pattern.compile(pattern).matcher(str).matches()));
            }),
    random(
            "long random(long bound)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "random", long.class)),
            (func, args, ctx) -> {
                var bound = (LongValue) args.getFirst();
                return FlowExecResult.of(Instances.longInstance(Utils.random(bound.getValue())));
            }
    ),
    timeToLong(
            "long timeToLong(time value)",
            true,
            List.of(ReflectionUtils.getMethod(Date.class, "getTime")),
            (func, args, ctx) -> {
                var date = (TimeValue) args.getFirst();
                return FlowExecResult.of(Instances.longInstance(date.getValue()));
            }),
    now(
            "time now()",
            true,
            List.of(),
            (func, args, ctx) -> FlowExecResult.of(Instances.timeInstance(System.currentTimeMillis()))
    ),
    time(
            "time timeFromMillis(long millis)",
            true,
            List.of(),
            (func, args, ctx) -> {
                var millis = (LongValue) args.getFirst();
                return FlowExecResult.of(Instances.timeInstance(millis.getValue()));
            }
    ),
    formatNumber(
            "string formatNumber(string format, long number)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "formatNumber", String.class, long.class)),
            (func, args, ctx) -> {
                var format = Instances.toJavaString(args.getFirst());
                var number = (LongValue) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(new DecimalFormat(format).format(number.getValue())));
            }),
    getId(
            "string getId(any obj)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getId", Object.class)),
            (func, args, ctx) -> {
                var obj = args.getFirst();
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
                        Instances.toJavaString(args.getFirst()),
                        Instances.toJavaString(args.get(1)),
                        Instances.toJavaString(args.get(2))
                );
                return FlowExecResult.of(null);
            }),
    secureRandom(
            "string secureRandom(int length)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "secureRandom", int.class)),
            (func, args, callContext) -> {
                var len = ((IntValue) args.getFirst()).value;
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.secureRandom(len)));
            }
    ),
    secureHash(
            "string secureHash(string value, string|null salt)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "secureHash", String.class, String.class)),
            (func, args, callContext) -> {
                var v = Instances.toJavaString(args.getFirst());
                var s = args.get(1) instanceof StringReference sr ? sr.getValue() : null;
                var h = EncodingUtils.secureHash(v, s);
                return FlowExecResult.of(Instances.stringInstance(h));
            }
    ),
    getParent(
            "any|null getParent(any object)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getParent", Object.class)),
            (func, args, callContext) -> {
                var obj = args.getFirst().resolveDurable();
                return FlowExecResult.of(Utils.getOrElse(obj.getParent(), Instance::getReference, Instances.nullInstance()));
            }
    ),
    getRoot(
            "any getRoot(any object)",
            true,
            List.of(ReflectionUtils.getMethod(Lang.class, "getRoot", Object.class)),
            (func, args, callContext) -> {
                var obj = args.getFirst().resolveDurable();
                return FlowExecResult.of(obj.getRoot().getReference());
            }
    ),
    sortList(
            "void sortList(java.util.List<[never, any]> list)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Collections.class, "sort", List.class)
            ),
            (func, args, callContext) -> {
                var list = args.getFirst().resolveObject();
                var nat = new ListNative(list);
                nat.sort(callContext);
                return FlowExecResult.of(null);
            }
    ),
    sortArray0(
            "void sortArray0([never, any|null][] array, java.util.Comparator<[never,any]>|null comparator)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "sort", Object[].class, Comparator.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var c = args.get(1);
                if(c.isNull())
                    array.sort((e1,e2) -> Instances.compare(e1, e2, callContext));
                else {
                    var comparator = c.resolveObject();
                    var cmpMethod = comparator.getInstanceType().getMethod(StdMethod.comparatorCompare.get());
                    array.sort((e1, e2) -> Instances.toInt(
                            Flows.invokeVirtual(cmpMethod, comparator, List.of(e1,e2), callContext
                            )));
                }
                return FlowExecResult.of(null);
            }
    ),
    sortArray(
            "void sortArray([never, any|null][] array, int from, int to, java.util.Comparator<[never,any]>|null comparator)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "sort", Object[].class, int.class, int.class, Comparator.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var from = ((IntValue) args.get(1)).value;
                var to = ((IntValue) args.get(2)).value;
                var c = args.get(3);
                if(c.isNull())
                    array.sort(from, to, (e1,e2) -> Instances.compare(e1, e2, callContext));
                else {
                    var comparator = c.resolveObject();
                    var cmpMethod = comparator.getInstanceType().getMethod(StdMethod.comparatorCompare.get());
                    array.sort(from, to, (e1, e2) -> Instances.toInt(
                            Flows.invokeVirtual(cmpMethod, comparator, List.of(e1,e2), callContext
                    )));
                }
                return FlowExecResult.of(null);
            }
    ),
    copyOfArray(
            "(T|null)[] copyOfArray<T>((T|null)[] array, int newLength)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOf", Object[].class, int.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var newLength = ((IntValue) args.get(1)).value;
                return FlowExecResult.of(array.copyOf(newLength).getReference());
            }
    ),
    copyOfLongArray(
            "long[] copyOfLongArray(long[] array, int newLength)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOf", long[].class, int.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var newLength = ((IntValue) args.get(1)).value;
                return FlowExecResult.of(array.copyOf(newLength).getReference());
            }
    ),
    copyOfArray2(
            "(T|null)[] copyOfArray2<T, U>((U|null)[] array, int newLength, org.metavm.object.type.Klass newType)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOf", Object[].class, int.class, Class.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var newLength = ((IntValue) args.get(1)).value;
                var newType = new ArrayType(Types.getNullableType(func.getTypeArguments().getFirst()), ArrayKind.READ_WRITE);
                return FlowExecResult.of(array.copyOf(newLength, newType).getReference());
            }
    ),
    copyOfArrayRange(
            "(T|null)[] copyOfArrayRange<T>((T|null)[] array, int from, int to)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOfRange", Object[].class, int.class, int.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var from = ((IntValue) args.get(1)).value;
                var to = ((IntValue) args.get(2)).value;
                return FlowExecResult.of(array.copyOfRange(from, to).getReference());
            }
    ),
    copyOfArrayRange2(
            "(T|null)[] copyOfArrayRange2<T, U>((U|null)[] array, int from, int to, org.metavm.object.type.Klass newType)",
            false,
            List.of(
                    ReflectionUtils.getMethod(Arrays.class, "copyOfRange", Object[].class, int.class, int.class, Class.class)
            ),
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var from = ((IntValue) args.get(1)).value;
                var to = ((IntValue) args.get(2)).value;
                var newType = new ArrayType(Types.getNullableType(func.getTypeArguments().getFirst()), ArrayKind.READ_WRITE);
                return FlowExecResult.of(array.copyOfRange(from, to, newType).getReference());
            }
    ),
    arraycopy(
            "void arraycopy(any source, int srcPos, any dest, int destPos, int length)",
            false,
            List.of(
                    ReflectionUtils.getMethod(System.class, "arraycopy", Object.class, int.class, Object.class, int.class, int.class)
            ),
            (func, args, callContext) -> {
                var src = args.getFirst().resolveArray();
                var srcPos = ((IntValue) args.get(1)).value;
                var dest = args.get(2).resolveArray();
                var destPos = ((IntValue) args.get(3)).value;
                var length = ((IntValue) args.get(4)).value;
                for(int i = srcPos, j = destPos, k = 0; k < length; i++, j++, k++)
                    dest.setElement(j, src.getElement(i));
                return FlowExecResult.of(null);
            }
    ),
    reverse(
            "void reverse(java.util.List<[never, any]> list)",
            false,
            List.of(ReflectionUtils.getMethod(Collections.class, "reverse", List.class)),
            (func, args, callContext) -> {
                var list = args.getFirst().resolveObject();
                var nat = new ListNative(list);
                nat.reverse();
                return FlowExecResult.of(null);
            }
    ),
    getClass(
            "org.metavm.object.type.Klass getClass(any o)",
            false,
            List.of(ReflectionUtils.getMethod(Object.class, "getClass")),
            (func, args, callContext) -> {
                var o = args.getFirst().resolveDurable();
                return FlowExecResult.of(Instances.getGeneralClass(o).getReference());
            }
    ),
    newArray(
            "any newArray(org.metavm.object.type.Klass klass, int length)",
            false,
            List.of(ReflectionUtils.getMethod(Array.class, "newInstance", Class.class, int.class)),
            (func, args, callContext) -> {
                var k = (Klass) args.getFirst().resolveObject();
                var len = ((IntValue) args.get(1)).value;
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
                var k = (Klass) args.getFirst().resolveDurable();
                var c = Objects.requireNonNull(k.getComponentKlass());
                return FlowExecResult.of(c.getReference());
            }
    ),
    checkIndex(
            "int checkIndex(int index, int length)",
            false,
            List.of(ReflectionUtils.getMethod(Objects.class, "checkIndex", int.class, int.class)),
            (func, args, callContext) -> {
                var index = ((IntValue) args.getFirst()).value;
                var length = ((IntValue) args.get(1)).value;
                if(index >= 0 && index < length)
                    return FlowExecResult.of(Instances.intInstance(index));
                else {
                    var exception = ClassInstance.allocate(StdKlass.indexOutOfBoundsException.type());
                    var nat = new IndexOutOfBoundsExceptionNative(exception);
                    nat.IndexOutOfBoundsException(callContext);
                    return FlowExecResult.ofException(exception);
                }
            }
    ),
    maxInt(
            "int maxInt(int v1, int v2)",
            false,
            List.of(ReflectionUtils.getMethod(Math.class, "max", int.class, int.class)),
            (func, args, callContext) -> {
                var v1 = ((IntValue) args.getFirst());
                var v2 = ((IntValue) args.get(1));
                if(v1.value >= v2.value)
                    return FlowExecResult.of(v1);
                else
                    return FlowExecResult.of(v2);
            }
    ),
    maxLong(
            "long maxLong(long v1, long v2)",
            false,
            List.of(ReflectionUtils.getMethod(Math.class, "max", long.class, long.class)),
            (func, args, callContext) -> {
                var v1 = ((LongValue) args.getFirst());
                var v2 = ((LongValue) args.get(1));
                if(v1.value >= v2.value)
                    return FlowExecResult.of(v1);
                else
                    return FlowExecResult.of(v2);
            }
    ),
    clone(
            "any clone(any o)",
            false,
            List.of(ReflectionUtils.getDeclaredMethod(Object.class, "clone", List.of())),
            (func, args, callContext) -> {
                var clone = args.getFirst().resolveMv().copy();
                ContextUtil.getEntityContext().bind(clone);
                return FlowExecResult.of(clone.getReference());
            }
    ),
    concat(
            "string concat(any|null s1, any|null s2)",
            false,
            List.of(ReflectionUtils.getMethod(Lang.class, "concat", Object.class, Object.class)),
            (func, args, callContext) -> {
                var s1 = Instances.toString(args.getFirst(), callContext);
                var s2 = Instances.toString(args.get(1), callContext);
                return FlowExecResult.of(Instances.stringInstance(s1 + s2));
            }
    ),
    checkFromIndexSize(
            "int checkFromIndexSize(int fromIndex, int size, int length)",
            false,
            List.of(ReflectionUtils.getMethod(Objects.class, "checkFromIndexSize", int.class, int.class, int.class)),
            (func, args, callContext) -> {
                var from = ((IntValue) args.getFirst()).getValue();
                var size = ((IntValue) args.get(1)).getValue();
                var len = ((IntValue) args.get(2)).getValue();
                if(from < 0 || size < 0 || from + size > len || len < 0) {
                    var e = ClassInstance.allocate(StdKlass.indexOutOfBoundsException.type());
                    var nat = new IndexOutOfBoundsExceptionNative(e);
                    nat.IndexOutOfBoundsException(callContext);
                    return FlowExecResult.ofException(e);
                }
                return FlowExecResult.of(Instances.intInstance(from));
            }
    ),
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
                    defContext.selectFirstByKey(Function.UNIQUE_NAME, Instances.stringInstance(def.getName())),
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
        function.setNativeCode(impl);
        functionHolder.set(function);
    }

    public void setLocal(Function function) {
        function.setNativeCode(impl);
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
