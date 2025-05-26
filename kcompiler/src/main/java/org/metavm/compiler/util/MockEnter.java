package org.metavm.compiler.util;

import org.metavm.compiler.analyze.Enter;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;

import java.util.Objects;

public class MockEnter {
    public static void enterStandard(Project project) {
        var rootPkg = project.getRootPackage();
        createInteger(rootPkg);
        createLong(project);
        createEnum(rootPkg);
        createIterator(rootPkg);
        createIterable(rootPkg);
        createSerializable(rootPkg);
        createCollection(rootPkg);
        createKlass(rootPkg);
        createList("List", ClassTag.INTERFACE, rootPkg);
        createList("ArrayList", ClassTag.CLASS, rootPkg);
        createSet("Set", ClassTag.INTERFACE, rootPkg);
        createSet("TreeSet", ClassTag.CLASS, rootPkg);
        createSet("HashSet", ClassTag.CLASS, rootPkg);
        createMap("Map", ClassTag.INTERFACE, rootPkg);
        createMap("HashMap", ClassTag.CLASS, rootPkg);
        createComparator(project);
        createComparable(project);
        var thr = createThrowable(rootPkg);
        var ex = createException("Exception", thr, project);
        var rtEx = createException("RuntimeException", ex, project);
        createException("IllegalStateException", rtEx, project);
        createException("NullPointerException", rtEx, project);
        createException("IndexOutOfBoundsException", rtEx, project);
        createException("NoSuchElementException", rtEx, rootPkg.subPackage("java").subPackage("util"));
        createAioob(rtEx, project);
        createObjectInputStream(rootPkg);
        createObjectOutputStream(rootPkg);
        createIndex(rootPkg);

        createHttpCookie(project);
        createHttpHeader(project);
        createHttpRequest(project);
        createHttpResponse(project);
        createInterceptor(project);

        createNowFunc(rootPkg);
        createToStringFunc(rootPkg);
        createEqualsFunc(rootPkg);
        createRequireFunc(rootPkg);
        createForEachFunc(rootPkg);
        createMapFunc(rootPkg);
        createPrintFunc(project);
        createConcatFunc(rootPkg);
        createConcatFunc(rootPkg);
        createEnumValueOfFunc(rootPkg);
        createSumInt(rootPkg);
        createSumLong(rootPkg);
        createSumFloat(rootPkg);
        createSumDouble(rootPkg);
    }

    private static void createInteger(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("lang");
        new Clazz(ClassTag.CLASS, Name.from("Integer"), Access.PUBLIC, pkg);
    }

    private static void createLong(Project proj) {
        var pkg = proj.getPackage("java.lang");
        var cls = new Clazz(ClassTag.CLASS, Name.from("Long"), Access.PUBLIC, pkg);
        var compareMeth = new Method("compare", Access.PUBLIC, true, false, false, cls);
        new Param("x", PrimitiveType.LONG, compareMeth);
        new Param("y", PrimitiveType.LONG, compareMeth);
        compareMeth.setRetType(PrimitiveType.INT);
    }

    private static void createIndex(Package rootPkg) {
        var pkg = rootPkg.subPackage("org").subPackage("metavm").subPackage("api");
        var clazz = new Clazz(ClassTag.CLASS, Name.from("Index"), Access.PUBLIC, pkg);
        var keyType = new TypeVar(Name.from("K"), PrimitiveType.ANY, clazz);
        var valueType = new TypeVar(Name.from("V"), PrimitiveType.ANY, clazz);
        var init = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param(Name.from("name"), Types.instance.getStringType(), init);
        new Param(Name.from("unique"), PrimitiveType.BOOL, init);
        new Param(Name.from("keyComputer"), Types.instance.getFuncType(List.of(valueType), keyType), init);

        var init1 = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param(Name.from("unique"), PrimitiveType.BOOL, init1);
        new Param(Name.from("keyComputer"), Types.instance.getFuncType(List.of(valueType), keyType), init1);

        var getFirst = new Method(Name.from("getFirst"), Access.PUBLIC, false, false, false, clazz);
        new Param(Name.from("key"), Types.instance.getNullableType(keyType), getFirst);
        getFirst.setRetType(Types.instance.getNullableType(valueType));

        var query = new Method("query", Access.PUBLIC, false, false, false, clazz);
        new Param("min", Types.instance.getNullableType(keyType), query);
        new Param("max", Types.instance.getNullableType(keyType), query);
        var listType = rootPkg.subPackage("java").subPackage("util").getClass("List")
                .getInst(List.of(valueType));
        query.setRetType(Types.instance.getNullableType(listType));

        var count = new Method("count", Access.PUBLIC, false, false, false, clazz);
        new Param("min", Types.instance.getNullableType(keyType), count);
        new Param("max", Types.instance.getNullableType(keyType), count);
        count.setRetType(PrimitiveType.LONG);
    }

    private static void createCollection(Package rootPkg) {
        var clazz = new Clazz(
                ClassTag.INTERFACE,
                "Collection",
                Access.PUBLIC,
                rootPkg.subPackage("java").subPackage("util")
        );
        var typeVar = new TypeVar("E", PrimitiveType.ANY, clazz);
        var iterableClazz = rootPkg.subPackage("java").subPackage("lang").getClass("Iterable");
        clazz.setInterfaces(List.of(iterableClazz.getInst(List.of(typeVar))));
    }

    private static void createList(String name, ClassTag tag, Package rootPkg) {
        var types = Types.instance;
        var abs = tag == ClassTag.INTERFACE;
        var pkg = rootPkg.subPackage("java").subPackage("util");
        var clazz = new Clazz(
                tag,
                name,
                Access.PUBLIC,
                pkg
        );
        var typeVar = new TypeVar("E", PrimitiveType.ANY, clazz);
        var collectionCls = rootPkg.subPackage("java").subPackage("util").getClass("Collection");
        if (tag == ClassTag.INTERFACE)
            clazz.setInterfaces(List.of(collectionCls.getInst(List.of(typeVar))));
        else {
            var listCls = rootPkg.subPackage("java").subPackage("util").getClass("List");
            clazz.setInterfaces(List.of(listCls.getInst(List.of(typeVar))));
        }
        if (tag == ClassTag.CLASS) {
            new Method(
                    Name.init(),
                    Access.PUBLIC,
                    false,
                    false,
                    true,
                    clazz
            );

            var c2 = new Method(
                    Name.init(),
                    Access.PUBLIC,
                    false,
                    false,
                    true,
                    clazz
            );
            new Param(
                    "c",
                    types.getNullableType(Objects.requireNonNull(collectionCls).getInst(
                            List.of(types.getUncertainType(PrimitiveType.NEVER, typeVar))
                    )),
                    c2
            );
        }
        var getMethod = new Method(
                "get",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        getMethod.setRetType(typeVar);
        new Param("index", PrimitiveType.INT, getMethod);

        var getFirstMethod = new Method(
                "getFirst",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        getFirstMethod.setRetType(types.getNullableType(typeVar));

        var addMethod = new Method(
                "add",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Param("e", types.getNullableType(typeVar), addMethod);
        addMethod.setRetType(PrimitiveType.BOOL);

        var addAllMethod = new Method(
                "addAll",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Param("c",
                types.getNullableType(collectionCls.getInst(
                        null,
                        List.of(
                                types.getUncertainType(PrimitiveType.NEVER, typeVar)
                        )
                )),
                addAllMethod
        );
        addAllMethod.setRetType(PrimitiveType.BOOL);

        new Method(
                "clear",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );

        var sizeMethod = new Method(
                "size",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        sizeMethod.setRetType(PrimitiveType.INT);

        if (clazz.isInterface()) {
            var of = new Method("of", Access.PUBLIC, true, false, false, clazz);
            var typeVar1 = new TypeVar("E", PrimitiveType.ANY, of);
            new Param("e1", types.getNullableType(typeVar1), of);
            of.setRetType(clazz.getInst(List.of(typeVar1)));

            var of2 = new Method("of", Access.PUBLIC, true, false, false, clazz);
            var typeVar2 = new TypeVar("E", PrimitiveType.ANY, of2);
            new Param("e1", types.getNullableType(typeVar2), of2);
            new Param("e2", types.getNullableType(typeVar2), of2);
            of2.setRetType(clazz.getInst(List.of(typeVar2)));

            var of3 = new Method("of", Access.PUBLIC, true, false, false, clazz);
            var typeVar3 = new TypeVar("E", PrimitiveType.ANY, of3);
            new Param("e1", types.getNullableType(typeVar3), of3);
            new Param("e2", types.getNullableType(typeVar3), of3);
            new Param("e3", types.getNullableType(typeVar3), of3);
            of3.setRetType(clazz.getInst(List.of(typeVar3)));
        }
    }

    private static void createComparator(Project project) {
        var pkg = project.getPackage("java.util");
        var cls = new Clazz(ClassTag.INTERFACE, "Comparator", Access.PUBLIC, pkg);
        var typeVar = new TypeVar("T", PrimitiveType.ANY, cls);
        var compareMeth = new Method("compare", Access.PUBLIC, false, false, false, cls);
        new Param("o1", Types.instance.getNullableType(typeVar), compareMeth);
        new Param("o2", Types.instance.getNullableType(typeVar), compareMeth);
        compareMeth.setRetType(PrimitiveType.INT);
    }

    private static void createComparable(Project project) {
        var pkg = project.getPackage("java.lang");
        var cls = new Clazz(ClassTag.INTERFACE, "Comparable", Access.PUBLIC, pkg);
        var typeVar = new TypeVar("T", PrimitiveType.ANY, cls);
        var compareMeth = new Method("compareTo", Access.PUBLIC, false, false, false, cls);
        new Param("o", Types.instance.getNullableType(typeVar), compareMeth);
        compareMeth.setRetType(PrimitiveType.INT);
    }

    private static void createSet(String name, ClassTag tag, Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("util");
        var clazz = new Clazz(tag, name, Access.PUBLIC, pkg);
        var typeVar = new TypeVar("E", PrimitiveType.ANY, clazz);
        var abs = tag == ClassTag.INTERFACE;
        if (abs) {
            clazz.setInterfaces(List.of(pkg.getClass("Collection").getInst(List.of(typeVar))));
        } else {
            clazz.setInterfaces(List.of(pkg.getClass("Set").getInst(List.of(typeVar))));
            new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
            var init = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
            var collType = Types.instance.getNullableType(pkg.getClass("Collection").getInst(
                    List.of(Types.instance.getUpperBoundedType(typeVar))
            ));
            new Param("c", collType, init);
        }
        var addMethod = new Method(
                "add",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Param("e", Types.instance.getNullableType(typeVar), addMethod);
        addMethod.setRetType(PrimitiveType.BOOL);

        var contains = new Method(
                "contains",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Param("o", Types.instance.getNullableAny(), contains);
        contains.setRetType(PrimitiveType.BOOL);
    }

    private static void createMap(String name, ClassTag tag, Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("util");
        var clazz = new Clazz(tag, name, Access.PUBLIC, pkg);
        var keyTypeVar = new TypeVar("K", PrimitiveType.ANY, clazz);
        var valueTypeVar = new TypeVar("V", PrimitiveType.ANY, clazz);
        var abs = tag == ClassTag.INTERFACE;
        if (!abs) {
            clazz.setInterfaces(List.of(pkg.getClass("Map").getInst(List.of(keyTypeVar, valueTypeVar))));
            new Method(
                    Name.init(),
                    Access.PUBLIC,
                    false,
                    false,
                    true,
                    clazz
            );
        }

        var types = Types.instance;
        var getMethod = new Method(
                "get",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Param("key", types.getNullableAny(), getMethod);
        getMethod.setRetType(types.getNullableType(valueTypeVar));

        var putMethod = new Method(
                "put",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Param("key", types.getNullableType(keyTypeVar), putMethod);
        new Param("value", types.getNullableType(valueTypeVar), putMethod);
        putMethod.setRetType(types.getNullableType(valueTypeVar));
    }

    private static void createIterator(Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("util");
        var clazz = new Clazz(
                ClassTag.INTERFACE,
                "Iterator",
                Access.PUBLIC,
                pkg
        );
        var typeVar = new TypeVar("T", PrimitiveType.ANY, clazz);

        var hasNextMethod = new Method("hasNext", Access.PUBLIC, false, true, false, clazz);
        hasNextMethod.setRetType(PrimitiveType.BOOL);

        var nextMethod = new Method("next", Access.PUBLIC, false, true, false, clazz);
        nextMethod.setRetType(typeVar);
    }

    private static Clazz createIterable(Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("lang");
        var clazz = new Clazz(ClassTag.INTERFACE, "Iterable", Access.PUBLIC, pkg);
        var typeVar = new TypeVar("T", PrimitiveType.ANY, clazz);
        var iteratorMethod = new Method("iterator", Access.PUBLIC, false, true, false, clazz);
        var iteratorClazz = rootPackage.subPackage("java").subPackage("util").getClass("Iterator");
        iteratorMethod.setRetType(iteratorClazz.getInst(List.of(typeVar)));
        return clazz;
    }

    private static Clazz createSerializable(Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("io");
        return new Clazz(ClassTag.INTERFACE, "Serializable", Access.PUBLIC, pkg);
    }

    private static Clazz createThrowable(Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("lang");
        var clazz = new Clazz(
                ClassTag.CLASS,
                "Throwable",
                Access.PUBLIC,
                pkg
        );
        new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);

        var c2 = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param("message", Types.instance.getNullableType(Types.instance.getStringType()), c2);
        return clazz;
    }

    private static void createAioob(Clazz superClass, Project proj) {
        var clazz = createException("ArrayIndexOutOfBoundsException", superClass, proj);
        var c = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param("index", PrimitiveType.INT, c);

    }

    private static Clazz createException(String name, Clazz superClass, Project project) {
        var pkg = project.getRootPackage().subPackage("java").subPackage("lang");
        return createException(name, superClass, pkg);
    }

    private static Clazz createException(String name, Clazz superClass, Package pkg) {
        var clazz = new Clazz(
                ClassTag.CLASS,
                name,
                Access.PUBLIC,
                pkg
        );
        clazz.setInterfaces(List.of(superClass));
        new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        var c2 = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param("message", Types.instance.getNullableType(Types.instance.getStringType()), c2);
        return clazz;
    }

    private static void createInterceptor(Project project) {
        var reqCls = project.getClass("org.metavm.api.entity.HttpRequest");
        var respCls = project.getClass("org.metavm.api.entity.HttpResponse");
        var pkg = project.getPackage("org.metavm.api");
        var clazz = new Clazz(ClassTag.INTERFACE, "Interceptor", Access.PUBLIC, pkg);
        var beforeMeth = new Method("before", Access.PUBLIC, false, false, false, clazz);
        new Param("request", reqCls, beforeMeth);
        new Param("response", respCls, beforeMeth);

        var afterMeth = new Method("after", Access.PUBLIC, false, false, false, clazz);
        new Param("request", reqCls, afterMeth);
        new Param("response", respCls, afterMeth);
        new Param("result", Types.instance.getNullableAny(), afterMeth);
        afterMeth.setRetType(Types.instance.getNullableAny());
    }

    private static void createHttpRequest(Project project) {
        var pkg = project.getPackage("org.metavm.api.entity");
        var cls = new Clazz(ClassTag.INTERFACE, "HttpRequest", Access.PUBLIC, pkg);
        var getMethodMeth = new Method("getMethod", Access.PUBLIC, false, false, false, cls);
        getMethodMeth.setRetType(Types.instance.getStringType());

        var getRequestURIMeth = new Method("getRequestURI", Access.PUBLIC, false, false, false, cls);
        getRequestURIMeth.setRetType(Types.instance.getStringType());

        var getCookie = new Method("getCookie", Access.PUBLIC, false, false, false, cls);
        new Param("name", Types.instance.getStringType(), getCookie);
        getCookie.setRetType(Types.instance.getNullableString());

        var getHeader = new Method("getHeader", Access.PUBLIC, false, false, false, cls);
        new Param("name", Types.instance.getStringType(), getHeader);
        getHeader.setRetType(Types.instance.getNullableString());
    }

    private static void createHttpResponse(Project project) {
        var listCls = project.getClass("java.util.List");
        var pkg = project.getPackage("org.metavm.api.entity");
        var cookieCls = project.getClass("org.metavm.api.entity.HttpCookie");
        var headerCls = project.getClass("org.metavm.api.entity.HttpHeader");
        var cls = new Clazz(ClassTag.INTERFACE, "HttpResponse", Access.PUBLIC, pkg);
        var addCookieMeth = new Method("addCookie", Access.PUBLIC, false, false, false, cls);
        new Param("name", Types.instance.getStringType(), addCookieMeth);
        new Param("value", Types.instance.getStringType(), addCookieMeth);

        var addHeaderMeth = new Method("addHeader", Access.PUBLIC, false, false, false, cls);
        new Param("name", Types.instance.getStringType(), addHeaderMeth);
        new Param("value", Types.instance.getStringType(), addHeaderMeth);

        var getHeadersMeth = new Method("getHeaders", Access.PUBLIC, false, false, false, cls);
        getHeadersMeth.setRetType(listCls.getInst(List.of(headerCls)));

        var getCookiesMeth = new Method("getCookies", Access.PUBLIC, false, false, false, cls);
        getCookiesMeth.setRetType(listCls.getInst(List.of(cookieCls)));
    }

    private static void createHttpCookie(Project project) {
        var pkg = project.getPackage("org.metavm.api.entity");
        var cls = new Clazz(ClassTag.INTERFACE, "HttpCookie", Access.PUBLIC, pkg);
        var nameMeth = new Method("name", Access.PUBLIC, false, false, false, cls);
        nameMeth.setRetType(Types.instance.getStringType());

        var valueMeth = new Method("value", Access.PUBLIC, false, false, false, cls);
        valueMeth.setRetType(Types.instance.getStringType());
    }

    private static void createHttpHeader(Project project) {
        var pkg = project.getPackage("org.metavm.api.entity");
        var cls = new Clazz(ClassTag.INTERFACE, "HttpHeader", Access.PUBLIC, pkg);
        var nameMeth = new Method("name", Access.PUBLIC, false, false, false, cls);
        nameMeth.setRetType(Types.instance.getStringType());

        var valueMeth = new Method("value", Access.PUBLIC, false, false, false, cls);
        valueMeth.setRetType(Types.instance.getStringType());
    }

    private static void createEnum(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("lang");
        var clazz = new Clazz(
                ClassTag.CLASS,
                "Enum",
                Access.PUBLIC,
                pkg
        );
        var typeVar = new TypeVar("E", PrimitiveType.ANY, clazz);
        typeVar.setBound(clazz.getInst(List.of(typeVar)));

        var nullableStrType = Types.instance.getNullableString();
        new Field("name", nullableStrType, Access.PRIVATE, false, clazz);
        new Field("ordinal", PrimitiveType.INT, Access.PRIVATE, false, clazz);

        var initMethod = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param(Name.from("name"), nullableStrType, initMethod);
        new Param(Name.from("ordinal"), PrimitiveType.INT, initMethod);

        var nameMethod = new Method("name", Access.PUBLIC, false, false, false, clazz);
        nameMethod.setRetType(nullableStrType);

        var ordinalMethod = new Method("ordinal", Access.PUBLIC, false, false, false, clazz);
        ordinalMethod.setRetType(PrimitiveType.INT);
    }

    private static void createKlass(Package rootPkg) {
        var pkg = rootPkg.subPackage("org").subPackage("metavm").subPackage("object").subPackage("type");
        new Clazz(ClassTag.CLASS, Name.from("Klass"), Access.PUBLIC, pkg);
    }

    private static void createObjectOutputStream(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("io");
        new Clazz(ClassTag.CLASS, Name.from("ObjectOutputStream"), Access.PUBLIC, pkg);
    }

    private static void createObjectInputStream(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("io");
        new Clazz(ClassTag.CLASS, Name.from("ObjectInputStream"), Access.PUBLIC, pkg);
    }

    private static void createNowFunc(Package rootPackage) {
        var nowFunc = new FreeFunc(NameTable.instance.get("now"), rootPackage);
        nowFunc.setRetType(PrimitiveType.TIME);
    }

    private static void createToStringFunc(Package rootPackage) {
        var func = new FreeFunc(NameTable.instance.toString, rootPackage);
        new Param(Name.from("o"), Types.instance.getNullableAny(), func);
        func.setRetType(Types.instance.getStringType());
    }

    private static void createEqualsFunc(Package rootPkg) {
        var equalsFunc = new FreeFunc(Name.from("equals"), rootPkg);
        new Param(Name.from("o1"), Types.instance.getNullableType(PrimitiveType.ANY), equalsFunc);
        new Param(Name.from("o2"), Types.instance.getNullableType(PrimitiveType.ANY), equalsFunc);
        equalsFunc.setRetType(PrimitiveType.BOOL);
    }

    private static void createRequireFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("require"), rootPkg);
        new Param(Name.from("cond"), PrimitiveType.BOOL, func);
        new Param(Name.from("message"), Types.instance.getStringType(), func);
    }

    private static void createForEachFunc(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.forEach, rootPkg);
        var typeVar = new TypeVar(NameTable.instance.E, PrimitiveType.ANY, func);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(typeVar),
                func
        );
        new Param(NameTable.instance.action,
                Types.instance.getFuncType(List.of(typeVar), PrimitiveType.VOID),
                func
        );
    }

    private static void createMapFunc(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.map, rootPkg);
        var typeVar1 = new TypeVar(NameTable.instance.T, PrimitiveType.ANY, func);
        var typeVar2 = new TypeVar(NameTable.instance.R, PrimitiveType.ANY, func);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(typeVar1),
                func
        );
        new Param(NameTable.instance.action,
                Types.instance.getFuncType(List.of(typeVar1), typeVar2),
                func
        );
        func.setRetType(Types.instance.getArrayType(typeVar2));
    }

    private static void createSumInt(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.sumInt, rootPkg);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(PrimitiveType.INT),
                func
        );
        func.setRetType(PrimitiveType.INT);
    }

    private static void createSumLong(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.sumLong, rootPkg);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(PrimitiveType.LONG),
                func
        );
        func.setRetType(PrimitiveType.LONG);
    }

    private static void createSumFloat(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.sumFloat, rootPkg);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(PrimitiveType.FLOAT),
                func
        );
        func.setRetType(PrimitiveType.FLOAT);
    }

    private static void createSumDouble(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.sumDouble, rootPkg);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(PrimitiveType.DOUBLE),
                func
        );
        func.setRetType(PrimitiveType.DOUBLE);
    }

    private static void createPrintFunc(Project proj) {
        var func = new FreeFunc(Name.from("print"), proj.getRootPackage());
        new Param(Name.from("message"), Types.instance.getNullableAny(), func);
    }

    private static void createConcatFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("concat"), rootPkg);
        new Param(Name.from("o1"), Types.instance.getNullableAny(), func);
        new Param(Name.from("o2"), Types.instance.getNullableAny(), func);
        func.setRetType(Types.instance.getStringType());
    }

    private static void createEnumValueOfFunc(Package rootPkg) {
        var enumClass = rootPkg.subPackage("java").subPackage("lang").getClass("Enum");
        var func = new FreeFunc(Name.from("enumValueOf"), rootPkg);
        var typeVar = new TypeVar(Name.from("E"), PrimitiveType.ANY, func);
        typeVar.setBound(enumClass.getInst(List.of(typeVar)));
        new Param(Name.from("values"), Types.instance.getArrayType(typeVar), func);
        new Param(Name.from("name"), Types.instance.getStringType(), func);
        func.setRetType(Types.instance.getNullableType(typeVar));
    }

    public static Project enter(List<File> files) {
        var project = new Project();
        var enter = new Enter(project);
        enter.enter(files);
        return project;
    }
}
