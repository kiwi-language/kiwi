package org.metavm.compiler;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metavm.compiler.analyze.Attr;
import org.metavm.compiler.analyze.Enter;
import org.metavm.compiler.analyze.ImportResolver;
import org.metavm.compiler.analyze.TypeResolver;
import org.metavm.compiler.antlr.KiwiLexer;
import org.metavm.compiler.antlr.KiwiParser;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.AstBuilder;
import org.metavm.compiler.syntax.AstBuilderTest;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.util.InternalException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class CompilerTestUtils {
    public static KiwiParser.CompilationUnitContext antlrParse(String path) {
        try (var reader = new BufferedReader(new FileReader(path))) {
            int n = reader.read(AstBuilderTest.buf);
            var source = new String(AstBuilderTest.buf, 0, n);
            var input = CharStreams.fromString(source);
            var parser = new KiwiParser(new CommonTokenStream(new KiwiLexer(input)));
            parser.setErrorHandler(new BailErrorStrategy());
            return parser.compilationUnit();
        } catch (IOException e) {
            throw new InternalException("Can not read source '" + path + "'", e);
        }
    }

    public static File parse(String path) {
        return AstBuilder.build(antlrParse(path));
    }

    public static Project enter(List<File> files) {
        var project = new Project();
        var enter = new Enter(project);
        enter.enter(files);
        return project;
    }

    public static void enterStandard(Project project) {
        var rootPkg = project.getRootPackage();
        createInteger(rootPkg);
        createEnum(rootPkg);
        createIterator(rootPkg);
        createIterable(rootPkg);
        createCollection(rootPkg);
        createKlass(rootPkg);
        createList("List", ClassTag.INTERFACE, rootPkg);
        createList("ArrayList", ClassTag.CLASS, rootPkg);
        createSet("Set", ClassTag.INTERFACE, rootPkg);
        createSet("TreeSet", ClassTag.CLASS, rootPkg);
        createException("RuntimeException", rootPkg);
        createException("IllegalStateException", rootPkg);
        createObjectInputStream(rootPkg);
        createObjectOutputStream(rootPkg);
        createIndex(rootPkg);

        createNowFunc(rootPkg);
        createEqualsFunc(rootPkg);
        createConcatFunc(rootPkg);
        createEnumValueOfFunc(rootPkg);
    }

    private static void createInteger(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("lang");
        new Clazz(ClassTag.CLASS, SymName.from("Integer"), Access.PUBLIC, pkg);
    }

    private static void createIndex(Package rootPkg) {
        var pkg = rootPkg.subPackage("org").subPackage("metavm").subPackage("api");
        var clazz = new Clazz(ClassTag.CLASS, SymName.from("Index"), Access.PUBLIC, pkg);
        var keyType = new TypeVariable(SymName.from("K"), PrimitiveType.ANY, clazz);
        var valueType = new TypeVariable(SymName.from("V"), PrimitiveType.ANY, clazz);
        var init = new Method(SymName.init(), Access.PUBLIC, false, false, true, clazz);
        new Parameter(SymName.from("name"), PrimitiveType.STRING, init);
        new Parameter(SymName.from("unique"), PrimitiveType.BOOLEAN, init);
        new Parameter(SymName.from("keyComputer"), Types.instance.getFunctionType(List.of(valueType), keyType), init);

        var getFirst = new Method(SymName.from("getFirst"), Access.PUBLIC, false, false, false, clazz);
        new Parameter(SymName.from("key"), Types.instance.getNullableType(keyType), getFirst);
        getFirst.setReturnType(Types.instance.getNullableType(valueType));
    }

    private static void createCollection(Package rootPkg) {
        var clazz = new Clazz(
                ClassTag.INTERFACE,
                "Collection",
                Access.PUBLIC,
                rootPkg.subPackage("java").subPackage("util")
        );
        var typeVar = new TypeVariable("E", PrimitiveType.ANY, clazz);
        var iterableClazz = rootPkg.subPackage("java").subPackage("lang").getClass("Iterable");
        clazz.setInterfaces(List.of(iterableClazz.getType(List.of(typeVar))));
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
        var typeVar = new TypeVariable("E", PrimitiveType.ANY, clazz);
        var collectionCls = rootPkg.subPackage("java").subPackage("util").getClass("Collection");
        if (tag == ClassTag.INTERFACE)
            clazz.setInterfaces(List.of(collectionCls.getType(List.of(typeVar))));
        else {
            var listCls = rootPkg.subPackage("java").subPackage("util").getClass("List");
            clazz.setInterfaces(List.of(listCls.getType(List.of(typeVar))));
        }
        if (tag == ClassTag.CLASS) {
            new Method(
                    SymName.init(),
                    Access.PUBLIC,
                    false,
                    false,
                    true,
                    clazz
            );

            var c2 = new Method(
                    SymName.init(),
                    Access.PUBLIC,
                    false,
                    false,
                    true,
                    clazz
            );
            new Parameter(
                    "c",
                    types.getNullableType(Objects.requireNonNull(collectionCls).getType(
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
        getMethod.setReturnType(types.getNullableType(typeVar));
        new Parameter("index", PrimitiveType.INT, getMethod);

        var getFirstMethod = new Method(
                "getFirst",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        getFirstMethod.setReturnType(types.getNullableType(typeVar));

        var addMethod = new Method(
                "add",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Parameter("e", types.getNullableType(typeVar), addMethod);
        addMethod.setReturnType(PrimitiveType.BOOLEAN);

        var addAllMethod = new Method(
                "addAll",
                Access.PUBLIC,
                false,
                abs,
                false,
                clazz
        );
        new Parameter("c",
                types.getNullableType(collectionCls.getType(
                        null,
                        List.of(
                                types.getUncertainType(PrimitiveType.NEVER, typeVar)
                        )
                )),
                addAllMethod
        );
        addAllMethod.setReturnType(PrimitiveType.BOOLEAN);

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
        sizeMethod.setReturnType(PrimitiveType.INT);
    }

    private static void createSet(String name, ClassTag tag, Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("util");
        var clazz = new Clazz(tag, name, Access.PUBLIC, pkg);
        var typeVar = new TypeVariable("E", PrimitiveType.ANY, clazz);
        var abs = tag == ClassTag.INTERFACE;
        if (abs) {
            clazz.setInterfaces(List.of(pkg.getClass("Collection").getType(List.of(typeVar))));
        } else {
            clazz.setInterfaces(List.of(pkg.getClass("Set").getType(List.of(typeVar))));
            new Method(
                    SymName.init(),
                    Access.PUBLIC,
                    false,
                    false,
                    true,
                    clazz
            );
        }
    }

    private static void createIterator(Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("util");
        var clazz = new Clazz(
                ClassTag.INTERFACE,
                "Iterator",
                Access.PUBLIC,
                pkg
        );
        var typeVar = new TypeVariable("T", PrimitiveType.ANY, clazz);

        var hasNextMethod = new Method("hasNext", Access.PUBLIC, false, true, false, clazz);
        hasNextMethod.setReturnType(PrimitiveType.BOOLEAN);

        var nextMethod = new Method("next", Access.PUBLIC, false, true, false, clazz);
        nextMethod.setReturnType(typeVar);
    }

    private static Clazz createIterable(Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("lang");

        var clazz = new Clazz(ClassTag.INTERFACE, "Iterable", Access.PUBLIC, pkg);
        var typeVar = new TypeVariable("T", PrimitiveType.ANY, clazz);

        var iteratorMethod = new Method("iterator", Access.PUBLIC, false, true, false, clazz);
        var iteratorClazz = rootPackage.subPackage("java").subPackage("util").getClass("Iterator");
        iteratorMethod.setReturnType(iteratorClazz.getType(List.of(typeVar)));
        return clazz;
    }

    private static void createException(String name, Package rootPackage) {
        var pkg = rootPackage.subPackage("java").subPackage("lang");
        var clazz = new Clazz(
                ClassTag.CLASS,
                name,
                Access.PUBLIC,
                pkg
        );
        new Method(name, Access.PUBLIC, false, false, true, clazz);

        var c2 = new Method(SymName.init(), Access.PUBLIC, false, false, true, clazz);
        new Parameter("message", Types.instance.getNullableType(PrimitiveType.STRING), c2);
    }

    private static void createEnum(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("lang");
        var clazz = new Clazz(
                ClassTag.CLASS,
                "Enum",
                Access.PUBLIC,
                pkg
        );
        var typeVar = new TypeVariable("E", PrimitiveType.ANY, clazz);
        typeVar.setBound(clazz.getType(List.of(typeVar)));

        var nullableStrType = Types.instance.getNullableType(PrimitiveType.STRING);
        new Field("name", nullableStrType, Access.PRIVATE, false, clazz);
        new Field("ordinal", PrimitiveType.INT, Access.PRIVATE, false, clazz);

        var initMethod = new Method(SymName.init(), Access.PUBLIC, false, false, true, clazz);
        new Parameter(SymName.from("name"), nullableStrType, initMethod);
        new Parameter(SymName.from("ordinal"), PrimitiveType.INT, initMethod);

        var nameMethod = new Method("name", Access.PUBLIC, false, false, false, clazz);
        nameMethod.setReturnType(nullableStrType);

        var ordinalMethod = new Method("ordinal", Access.PUBLIC, false, false, false, clazz);
        ordinalMethod.setReturnType(PrimitiveType.INT);
    }

    private static void createKlass(Package rootPkg) {
        var pkg = rootPkg.subPackage("org").subPackage("metavm").subPackage("object").subPackage("type");
        new Clazz(ClassTag.CLASS, SymName.from("Klass"), Access.PUBLIC, pkg);
    }

    private static void createObjectOutputStream(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("io");
        new Clazz(ClassTag.CLASS, SymName.from("ObjectOutputStream"), Access.PUBLIC, pkg);
    }

    private static void createObjectInputStream(Package rootPkg) {
        var pkg = rootPkg.subPackage("java").subPackage("io");
        new Clazz(ClassTag.CLASS, SymName.from("ObjectInputStream"), Access.PUBLIC, pkg);
    }

    private static void createNowFunc(Package rootPackage) {
        var nowFunc = new FreeFunc(SymNameTable.instance.get("now"));
        nowFunc.setReturnType(PrimitiveType.TIME);
        rootPackage.addFunction(nowFunc);
    }

    private static void createEqualsFunc(Package rootPkg) {
        var equalsFunc = new FreeFunc(SymName.from("equals"));
        new Parameter(SymName.from("o1"), Types.instance.getNullableType(PrimitiveType.ANY), equalsFunc);
        new Parameter(SymName.from("o2"), Types.instance.getNullableType(PrimitiveType.ANY), equalsFunc);
        equalsFunc.setReturnType(PrimitiveType.BOOLEAN);
        rootPkg.addFunction(equalsFunc);
    }

    private static void createConcatFunc(Package rootPkg) {
        var func = new FreeFunc(SymName.from("concat"));
        new Parameter(SymName.from("o1"), Types.instance.getNullableAny(), func);
        new Parameter(SymName.from("o2"), Types.instance.getNullableAny(), func);
        func.setReturnType(PrimitiveType.STRING);
        rootPkg.addFunction(func);
    }

    private static void createEnumValueOfFunc(Package rootPkg) {
        var enumClass = rootPkg.subPackage("java").subPackage("lang").getClass("Enum");
        var func = new FreeFunc(SymName.from("enumValueOf"));
        var typeVar = new TypeVariable(SymName.from("E"), PrimitiveType.ANY, func);
        typeVar.setBound(enumClass.getType(List.of(typeVar)));
        new Parameter(SymName.from("values"), Types.instance.getArrayType(typeVar), func);
        new Parameter(SymName.from("name"), PrimitiveType.STRING, func);
        func.setReturnType(Types.instance.getNullableType(typeVar));
        rootPkg.addFunction(func);
    }

    public static Project attr(File file) {
        var project = enter(List.of(file));
        enterStandard(project);
        ImportResolver.resolve(file, project);
        var typeResolver = new TypeResolver();
        file.accept(typeResolver);
        file.accept(new Attr());
        return project;
    }
}
