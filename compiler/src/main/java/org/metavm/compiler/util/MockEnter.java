package org.metavm.compiler.util;

import org.metavm.compiler.analyze.Enter;
import org.metavm.compiler.diag.DummyLog;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;

public class MockEnter {
    public static void enterStandard(Project project) {
        var rootPkg = project.getRootPackage();
        createEnum(rootPkg);
        createException(project);
        project.setIndexClass(createIndex(rootPkg));

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
        createSortFunc(rootPkg);
        createReverseFunc(rootPkg);
        createMapFunc(rootPkg);
        createPrintFunc(project);
        createConcatFunc(rootPkg);
        createConcatFunc(rootPkg);
        createEnumValueOfFunc(rootPkg);
        createSumInt(rootPkg);
        createSumLong(rootPkg);
        createSumFloat(rootPkg);
        createSumDouble(rootPkg);
        createSecureHashFunc(rootPkg);
        createSecureRandomFunc(rootPkg);
        createUuidFunc(rootPkg);
        createGetContextFunc(rootPkg);
        createSetContextFunc(rootPkg);
        createRandomFunc(rootPkg);
        createRegexMatchFunc(rootPkg);
        createFormatNumberFunc(rootPkg);
    }

    private static Clazz createIndex(Package rootPkg) {
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
        new Param(Name.from("key"), keyType, getFirst);
        getFirst.setRetType(Types.instance.getNullableType(valueType));

        var getLast = new Method(Name.from("getLast"), Access.PUBLIC, false, false, false, clazz);
        new Param(Name.from("key"), keyType, getLast);
        getLast.setRetType(Types.instance.getNullableType(valueType));

        var getAll = new Method(Name.from("getAll"), Access.PUBLIC, false, false, false, clazz);
        new Param(Name.from("key"), keyType, getAll);
        getAll.setRetType(Types.instance.getArrayType(valueType));

        var query = new Method("query", Access.PUBLIC, false, false, false, clazz);
        new Param("min", keyType, query);
        new Param("max", keyType, query);
        query.setRetType(Types.instance.getArrayType(valueType));

        var count = new Method("count", Access.PUBLIC, false, false, false, clazz);
        new Param("min", keyType, count);
        new Param("max", keyType, count);
        count.setRetType(PrimitiveType.LONG);
        return clazz;
    }

    private static Clazz createException(Project project) {
        return createException(project.getRootPackage().subPackage("java").subPackage("lang"));
    }

    private static Clazz createException(Package pkg) {
        var clazz = new Clazz(
                ClassTag.CLASS,
                "Exception",
                Access.PUBLIC,
                pkg
        );
        new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        var c2 = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param("message", Types.instance.getStringType(), c2);
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

        var setCurrentUser = new Method("setCurrentUser", Access.PUBLIC, false, false, false, cls);
        new Param("currentUser", Types.instance.getNullableAny(), setCurrentUser);

        var getCurrentUser = new Method("getCurrentUser", Access.PUBLIC, false, false, false, cls);
        getCurrentUser.setRetType(Types.instance.getNullableAny());
    }

    private static void createHttpResponse(Project project) {
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
        getHeadersMeth.setRetType(Types.instance.getArrayType(headerCls));

        var getCookiesMeth = new Method("getCookies", Access.PUBLIC, false, false, false, cls);
        getCookiesMeth.setRetType(Types.instance.getArrayType(cookieCls));
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
        var clazz = new Clazz(
                ClassTag.CLASS,
                "Enum",
                Access.PUBLIC,
                rootPkg.subPackage("java").subPackage("lang")
        );
        var typeVar = new TypeVar("E", PrimitiveType.ANY, clazz);
        typeVar.setBound(clazz.getInst(List.of(typeVar)));

        var strType = Types.instance.getStringType();
        new Field("name", strType, Access.PRIVATE, false, false, clazz);
        new Field("ordinal", PrimitiveType.INT, Access.PRIVATE, false, false, clazz);

        var initMethod = new Method(Name.init(), Access.PUBLIC, false, false, true, clazz);
        new Param(Name.from("name"), strType, initMethod);
        new Param(Name.from("ordinal"), PrimitiveType.INT, initMethod);

        var nameMethod = new Method("name", Access.PUBLIC, false, false, false, clazz);
        nameMethod.setRetType(strType);

        var ordinalMethod = new Method("ordinal", Access.PUBLIC, false, false, false, clazz);
        ordinalMethod.setRetType(PrimitiveType.INT);
    }

    private static void createNowFunc(Package rootPackage) {
        var nowFunc = new FreeFunc(NameTable.instance.get("now"), rootPackage);
        nowFunc.setRetType(PrimitiveType.LONG);
    }

    private static void createToStringFunc(Package rootPackage) {
        var func = new FreeFunc(NameTable.instance.toString, rootPackage);
        new Param(Name.from("o"), Types.instance.getNullableAny(), func);
        func.setRetType(Types.instance.getStringType());
    }

    private static void createEqualsFunc(Package rootPkg) {
        var equalsFunc = new FreeFunc(Name.from("equals"), rootPkg);
        new Param(Name.from("o1"), Types.instance.getNullableAny(), equalsFunc);
        new Param(Name.from("o2"), Types.instance.getNullableAny(), equalsFunc);
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

    private static void createSortFunc(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.sort, rootPkg);
        var typeVar = new TypeVar(NameTable.instance.E, PrimitiveType.ANY, func);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(typeVar),
                func
        );
        new Param(NameTable.instance.comparator,
                Types.instance.getFuncType(List.of(typeVar, typeVar), PrimitiveType.VOID),
                func
        );
    }

    private static void createReverseFunc(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.reverse, rootPkg);
        var typeVar = new TypeVar(NameTable.instance.E, PrimitiveType.ANY, func);
        new Param(
                NameTable.instance.a,
                Types.instance.getArrayType(typeVar),
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

    private static void createSecureHashFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("secureHash"), rootPkg);
        new Param(Name.from("value"), Types.instance.getStringType(), func);
        new Param(Name.from("salt"), Types.instance.getNullableType(Types.instance.getStringType()), func);
        func.setRetType(Types.instance.getStringType());
    }

    private static void createSecureRandomFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("secureRandom"), rootPkg);
        new Param(Name.from("length"), PrimitiveType.INT, func);
        func.setRetType(Types.instance.getStringType());
    }

    private static void createUuidFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("uuid"), rootPkg);
        func.setRetType(Types.instance.getStringType());
    }

    private static void createGetContextFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("getContext"), rootPkg);
        new Param(Name.from("key"), Types.instance.getStringType(), func);
        func.setRetType(Types.instance.getNullableAny());
    }

    private static void createSetContextFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("setContext"), rootPkg);
        new Param(Name.from("key"), Types.instance.getStringType(), func);
        new Param(Name.from("value"), PrimitiveType.ANY, func);
    }

    private static void createRandomFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("random"), rootPkg);
        new Param(Name.from("bound"), PrimitiveType.LONG, func);
        func.setRetType(PrimitiveType.LONG);
    }

    private static void createRegexMatchFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("regexMatch"), rootPkg);
        new Param(Name.from("pattern"), Types.instance.getStringType(), func);
        new Param(Name.from("str"), Types.instance.getStringType(), func);
        func.setRetType(PrimitiveType.BOOL);
    }

    private static void createFormatNumberFunc(Package rootPkg) {
        var func = new FreeFunc(Name.from("formatNumber"), rootPkg);
        new Param(Name.from("pattern"), Types.instance.getStringType(), func);
        new Param(Name.from("number"), PrimitiveType.LONG, func);
        func.setRetType(Types.instance.getStringType());
    }

    public static Project enter(List<File> files) {
        var project = new Project();
        var enter = new Enter(project, new DummyLog());
        enter.enter(files);
        return project;
    }
}
