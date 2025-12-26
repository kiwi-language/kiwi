package org.metavm.entity.natives;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;
import org.metavm.entity.DefContext;
import org.metavm.entity.StdField;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Flows;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.user.Session;
import org.metavm.util.*;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Slf4j
public enum StdFunction {

    functionToInstance(
            "functionToInstance<T>(function: any) -> T", true,
            (func, args, callContext) -> {
                if (args.getFirst() instanceof FunctionValue functionValue) {
                    var samInterface = ((ClassType) func.getTypeArguments().getFirst());
                    var type = Types.createSAMInterfaceImpl(samInterface, functionValue);
                    return FlowExecResult.of(new MvClassInstance(TmpId.random(), Map.of(), type, false).getReference());
                } else {
                    throw new InternalException("Invalid function instance: " + Instances.getInstancePath(args.getFirst()));
                }
            }),
    getSessionEntry(
            "getSessionEntry(key: string) -> any|null",
            false,
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
            "setSessionEntry(key: string, value: any) -> void",
            false,
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
            "removeSessionEntry(key: string) -> boolean",
            false,
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                var entityContext = ContextUtil.getEntityContext();
                var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, Instances.stringInstance(ContextUtil.getToken()));
                if (session == null || !session.isActive())
                    throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
                return FlowExecResult.of(Instances.intInstance(session.removeEntry(key)));
            }),
    typeCast(
            "typeCast<T>(instance: any|null) -> T",
            true,
            (func, args, callContext) -> {
                var type = func.getTypeArguments().getFirst();
                var value = args.getFirst();
                if (type.isInstance(value))
                    return FlowExecResult.of(value);
                else {
                    var exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                    var msg = value.getValueType().getTypeDesc() + " cannot be cast to " + type.getTypeDesc();
                    ExceptionNative.Exception(exception, Instances.stringInstance(msg));
                    return FlowExecResult.ofException(exception);
                }
            }),
    print(
            "print(content: any|null) -> void",
            true,
            (func, args, callContext) -> {
                System.out.println(args.getFirst().getTitle());
                return FlowExecResult.of(null);
            }),
    setContext(
            "setContext(key: string, value: any) -> void",
            true,
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                ContextUtil.setUserData(key, args.get(1));
                return FlowExecResult.of(null);
            }),
    getContext(
            "getContext(key: string) -> any|null",
            true,
            (func, args, callContext) -> {
                var key = Instances.toJavaString(args.getFirst());
                return FlowExecResult.of(ContextUtil.getUserData(key));
            }),
    toString(
            "toString(instance: any|null) -> string",
            true,
            (func, args, callContext) -> FlowExecResult.of(
                    Instances.stringInstance(Instances.toString(args.getFirst(), callContext)))
    ),
    hashCode(
            "hashCode(instance: any) -> int",
            true,
            (func, args, callContext) -> {
                var value = args.getFirst();
                return FlowExecResult.of(Instances.intInstance(Instances.hashCode(value, callContext)));
            }
    ),
    requireNonNull(
            "requireNonNull<T>(value: T|null) -> T",
            true,
            (func, args, ctx) -> {
                if (args.size() != 1) {
                    throw new IllegalArgumentException("requireNonNull requires exactly one argument");
                }
                var value = args.getFirst();
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(TmpId.random(), StdKlass.exception.get().getType());
                    ExceptionNative.Exception(npe, Instances.stringInstance("Null pointer"));
                    return FlowExecResult.ofException(npe);
                }
            }),
    requireNonNull1(
            "requireNonNull1<T>(value: T|null, message: string) -> T",
            true,
            (func, args, ctx) -> {
                if (args.size() != 2) {
                    throw new IllegalArgumentException("requireNonNull requires exactly two arguments");
                }
                var value = args.getFirst();
                var message = args.get(1);
                if (value.isNotNull())
                    return FlowExecResult.of(value);
                else {
                    var npe = ClassInstance.allocate(TmpId.random(), StdKlass.exception.get().getType());
                    ExceptionNative.Exception(npe, message);
                    return FlowExecResult.ofException(npe);
                }
            }),
    dateBefore(
            "dateBefore(date1: time, date2: time) -> boolean",
            true,
            (func, args, ctx) -> {
                var date1 = (TimeValue) args.getFirst();
                var date2 = (TimeValue) args.get(1);
                return FlowExecResult.of(date1.before(date2));
            }),
    dateAfter(
            "dateAfter(date1: time, date2: time) -> boolean",
            true,
            (func, args, ctx) -> {
                var date1 = (TimeValue) args.getFirst();
                var date2 = (TimeValue) args.get(1);
                return FlowExecResult.of(date1.after(date2));
            }),
    replace(
            "replace(str: string, target: string, replacement: string) -> string",
            true,
            (func, args, ctx) -> {
                var str = requireNonNull(Instances.toJavaString(args.getFirst()));
                var target = requireNonNull(Instances.toJavaString(args.get(1)));
                var replacement = requireNonNull(Instances.toJavaString(args.get(2)));
                return FlowExecResult.of(Instances.stringInstance(str.replace(target, replacement)));
            }),
    uuid(
            "uuid() -> string",
            true,
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(UUID.randomUUID().toString()))
    ),
    currentTimeMillis(
            "currentTimeMillis() -> long",
            true,
            (func, args, ctx) -> FlowExecResult.of(Instances.longInstance(System.currentTimeMillis()))
    ),
    equals(
            "equals(obj1: any|null, obj2: any|null) -> boolean",
            true,
            (func, args, ctx) -> FlowExecResult.of(Instances.intInstance(Instances.equals(args.getFirst(), args.getLast(), ctx)))
    ),
    md5(
            "md5(str: string) -> string",
            true,
            (func, args, ctx) -> {
                var str = requireNonNull(Instances.toJavaString(args.getFirst()));
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.md5(str)));
            }),
    randomPassword(
            "randomPassword() -> string",
            true,
            (func, args, ctx) -> FlowExecResult.of(Instances.stringInstance(Utils.randomPassword()))
    ),
    password(
            "passwd(s: string) -> password",
            true,
            (func, args, ctx) -> {
                var s = requireNonNull(Instances.toJavaString(args.getFirst()));
                return FlowExecResult.of(Instances.passwordInstance(EncodingUtils.md5(s)));
            }
    ),
    regexMatch(
            "regexMatch(pattern: string, str: string) -> boolean",
            true,
            (func, args, ctx) -> {
                var pattern = requireNonNull(Instances.toJavaString(args.getFirst()));
                var str = requireNonNull(Instances.toJavaString(args.get(1)));
                return FlowExecResult.of(Instances.intInstance(Pattern.compile(pattern).matcher(str).matches()));
            }),
    random(
            "random(bound: long) -> long",
            true,
            (func, args, ctx) -> {
                var bound = (LongValue) args.getFirst();
                return FlowExecResult.of(Instances.longInstance(Utils.random(bound.getValue())));
            }
    ),
    timeToLong(
            "timeToLong(value: time) -> long",
            true,
            (func, args, ctx) -> {
                var date = (TimeValue) args.getFirst();
                return FlowExecResult.of(Instances.longInstance(date.getValue()));
            }),
    now(
            "now() -> long",
            true,
            (func, args, ctx) -> FlowExecResult.of(Instances.longInstance(System.currentTimeMillis()))
    ),
    newDate(
            "newDate() -> time",
            true,
            (func, args, ctx) -> FlowExecResult.of(Instances.timeInstance(System.currentTimeMillis()))
    ),
    dateFromTime(
            "timeFromMillis(millis: long) -> time",
            true,
            (func, args, ctx) -> {
                var millis = (LongValue) args.getFirst();
                return FlowExecResult.of(Instances.timeInstance(millis.getValue()));
            }
    ),
    formatNumber(
            "formatNumber(format: string, number: long) -> string",
            true,
            (func, args, ctx) -> {
                var format = requireNonNull(Instances.toJavaString(args.getFirst()));
                var number = (LongValue) args.get(1);
                return FlowExecResult.of(Instances.stringInstance(new DecimalFormat(format).format(number.getValue())));
            }),
    getId(
            "getId(obj: any) -> string",
            true,
            (func, args, ctx) -> {
                var obj = args.getFirst();
                if (obj instanceof EntityReference r)
                    return FlowExecResult.of(Instances.stringInstance(r.getStringId()));
                var npe = ClassInstance.allocate(TmpId.random(), StdKlass.exception.get().getType());
                ExceptionNative.Exception(npe, Instances.stringInstance("Object has no ID"));
                return FlowExecResult.ofException(npe);
            }),
    sendEmail(
            "sendEmail(recipient: string, subject: string, content: string) -> void",
            false,
            (func, args, callContext) -> {
                EmailUtils.emailSender.send(
                        Instances.toJavaString(args.getFirst()),
                        Instances.toJavaString(args.get(1)),
                        Instances.toJavaString(args.get(2))
                );
                return FlowExecResult.of(null);
            }),
    secureRandom(
            "secureRandom(length: int) -> string",
            true,
            (func, args, callContext) -> {
                var len = ((IntValue) args.getFirst()).value;
                return FlowExecResult.of(Instances.stringInstance(EncodingUtils.secureRandom(len)));
            }
    ),
    secureHash(
            "secureHash(value: string, salt: string|null) -> string",
            true,
            (func, args, callContext) -> {
                var v = Instances.toJavaString(args.getFirst());
                var s = args.get(1) instanceof StringReference sr ? sr.getValue() : null;
                var h = EncodingUtils.secureHash(v, s);
                return FlowExecResult.of(Instances.stringInstance(h));
            }
    ),
    getParent(
            "getParent(object: any) -> any|null",
            true,
            (func, args, callContext) -> {
                var obj = args.getFirst().resolveDurable();
                return FlowExecResult.of(Utils.getOrElse(obj.getParent(), Instance::getReference, Instances.nullInstance()));
            }
    ),
    getRoot(
            "getRoot(object: any) -> any",
            true,
            (func, args, callContext) -> {
                var obj = args.getFirst().resolveDurable();
                return FlowExecResult.of(obj.getRoot().getReference());
            }
    ),
    sort(
            "sort<E>(a: E[], c: (E, E)->int) -> void",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var c = args.get(1);
                var comparator = (FunctionValue) c;
                array.sort((e1, e2) -> Instances.toInt(
                        Flows.invoke(comparator, List.of(e1,e2), callContext)
                ));
                return FlowExecResult.of(null);
            }
    ),
    reverse(
            "reverse<E>(a: E[]) -> void",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                array.reverse();
                return FlowExecResult.of(null);
            }
    ),
    copyOfArray(
            "copyOfArray<T>(array: (T|null)[], newLength: int) -> (T|null)[]",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var newLength = ((IntValue) args.get(1)).value;
                return FlowExecResult.of(array.copyOf(newLength).getReference());
            }
    ),
    copyOfLongArray(
            "copyOfLongArray(array: long[], newLength: int) -> long[]",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var newLength = ((IntValue) args.get(1)).value;
                return FlowExecResult.of(array.copyOf(newLength).getReference());
            }
    ),
    copyOfArrayRange(
            "copyOfArrayRange<T>(array: (T|null)[], from: int, to: int) -> (T|null)[]",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var from = ((IntValue) args.get(1)).value;
                var to = ((IntValue) args.get(2)).value;
                return FlowExecResult.of(array.copyOfRange(from, to).getReference());
            }
    ),
    arraycopy(
            "arraycopy(source: any, srcPos: int, dest: any, destPos: int, length: int) -> void",
            false,
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
    checkIndex(
            "checkIndex(index: int, length: int) -> int",
            false,
            (func, args, callContext) -> {
                var index = ((IntValue) args.getFirst()).value;
                var length = ((IntValue) args.get(1)).value;
                if(index >= 0 && index < length)
                    return FlowExecResult.of(Instances.intInstance(index));
                else {
                    var exception = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                    ExceptionNative.Exception(exception, Instances.stringInstance("Index out of bound"));
                    return FlowExecResult.ofException(exception);
                }
            }
    ),
    maxInt(
            "maxInt(v1: int, v2: int) -> int",
            false,
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
            "maxLong(v1: long, v2: long) -> long",
            false,
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
            "clone(o: any) -> any",
            false,
            (func, args, callContext) -> {
                var context = ContextUtil.getEntityContext();
                var clone = args.getFirst().resolveMv().copy(context::allocateRootId);
                context.bind(clone);
                return FlowExecResult.of(clone.getReference());
            }
    ),
    concat(
            "concat(s1: any|null, s2: any|null) -> string",
            false,
            (func, args, callContext) -> {
                var s1 = Instances.toString(args.getFirst(), callContext);
                var s2 = Instances.toString(args.get(1), callContext);
                return FlowExecResult.of(Instances.stringInstance(s1 + s2));
            }
    ),
    checkFromIndexSize(
            "checkFromIndexSize(fromIndex: int, size: int, length: int) -> int",
            false,
            (func, args, callContext) -> {
                var from = ((IntValue) args.getFirst()).getValue();
                var size = ((IntValue) args.get(1)).getValue();
                var len = ((IntValue) args.get(2)).getValue();
                if(from < 0 || size < 0 || from + size > len || len < 0) {
                    var e = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                    ExceptionNative.Exception(e, Instances.stringInstance("Index out of bound"));
                    return FlowExecResult.ofException(e);
                }
                return FlowExecResult.of(Instances.intInstance(from));
            }
    ),
    enumValueOf(
            "enumValueOf<T>(values: T[], name: string) -> T|null",
            false,
            (func, args, callContext) -> {
                var values = args.getFirst().resolveArray();
                var name = (StringReference) args.get(1);
                for (Value value : values) {
                    var inst = value.resolveObject();
                    if (inst.getField(StdField.enumName.get()).equals(name))
                        return FlowExecResult.of(value);
                }
                return FlowExecResult.of(Instances.nullInstance());
            }
    ),
    require(
            "require(condition: boolean, message: string) -> void",
            false,
            (func, args, callContext) -> {
                var cond = (IntValue) args.getFirst();
                if (cond.value != 0)
                    return FlowExecResult.of(Instances.nullInstance());
                else {
                    var e = ClassInstance.allocate(TmpId.random(), StdKlass.exception.type());
                    ExceptionNative.Exception(e, args.get(1));
                    return FlowExecResult.ofException(e);
                }
            }),
    forEach(
            "forEach<E>(a: E[], action: (E) -> void) -> void",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var action = (FunctionValue) args.get(1);
                array.forEach(e -> action.execute(List.of(e), callContext));
                return FlowExecResult.of(Instances.nullInstance());
            }),
    map(
            "map<T, R>(a: T[], mapper: (T) -> R) -> R[]",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var action = (FunctionValue) args.get(1);
                var rType = func.getTypeArguments().get(1);
                var result = new ArrayInstance(new ArrayType(rType, ArrayKind.DEFAULT));
                array.forEach(e -> {
                    var r = requireNonNull(action.execute(List.of(e), callContext).ret());
                    result.addElement(r);
                });
                return FlowExecResult.of(result.getReference());
            }),
    sumInt(
            "sumInt(a: int[]) -> int",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var sum = 0;
                for (Value value : array) {
                    var i = (IntValue) value;
                    sum += i.value;
                }
                return FlowExecResult.of(Instances.intInstance(sum));
            }),
    sumLong(
            "sumLong(a: long[]) -> long",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var sum = 0L;
                for (Value value : array) {
                    var i = (LongValue) value;
                    sum += i.value;
                }
                return FlowExecResult.of(Instances.longInstance(sum));
            }),
    sumFloat(
            "sumFloat(a: float[]) -> float",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var sum = 0f;
                for (Value value : array) {
                    var i = (FloatValue) value;
                    sum += i.value;
                }
                return FlowExecResult.of(Instances.floatInstance(sum));
            }),
    sumDouble(
            "sumDouble(a: double[]) -> double",
            false,
            (func, args, callContext) -> {
                var array = args.getFirst().resolveArray();
                var sum = 0.0;
                for (Value value : array) {
                    var i = (DoubleValue) value;
                    sum += i.value;
                }
                return FlowExecResult.of(Instances.doubleInstance(sum));
            }),
    ;

    @Getter
    private final String name;
    @Getter
    private final String signature;
    @Getter
    private final boolean system;
    private final ValueHolder<Function> functionHolder;
    private final FunctionImpl impl;

    StdFunction(String signature, boolean system, FunctionImpl impl) {
        this.signature = signature;
        this.system = system;
        this.impl = impl;
        this.functionHolder = new HybridValueHolder<>();
        var typeParser = new TypeParserImpl((String name) -> {
            throw new NullPointerException("defContext is null");
        });
        this.name = typeParser.getFunctionName(signature);
    }

    public static void setEmailSender(EmailSender emailSender) {
        EmailUtils.emailSender = emailSender;
    }

    public static List<Function> defineSystemFunctions(DefContext defContext) {
        return Arrays.stream(values())
                .filter(StdFunction::isSystem)
                .map(def -> def.define(defContext))
                .toList();
    }

    public static List<Function> defineUserFunctions(DefContext defContext) {
        return Arrays.stream(values())
                .filter(def -> !def.isSystem())
                .map(def -> def.define(defContext))
                .toList();
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
                (String name1) -> {
                    throw new NullPointerException("Unrecognized type: " + name1);
                }
        ).parseFunction(signature, defContext::getModelId);
    }

    public void set(Function function) {
        function.setNativeCode(impl);
        functionHolder.set(function);
    }

    public Function get() {
        return functionHolder.get();
    }

}