package org.metavm.compiler;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metavm.compiler.analyze.*;
import org.metavm.compiler.antlr.KiwiLexer;
import org.metavm.compiler.antlr.KiwiParser;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.AstBuilder;
import org.metavm.compiler.syntax.AstBuilderTest;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;
import org.metavm.util.InternalException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

    private static void createConcat(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.concat);
        new Param(Name.from("s1"), Types.instance.getNullableString(), func);
        new Param(Name.from("s2"), Types.instance.getNullableString(), func);
        func.setRetType(Types.instance.getStringType());
        rootPkg.addFunction(func);
    }

    public static Project attr(File file) {
        var project = MockEnter.enter(List.of(file));
        MockEnter.enterStandard(project);
        ImportResolver.resolve(file, project);
        var typeResolver = new TypeResolver();
        file.accept(typeResolver);
        file.accept(new IdentAttr());
        file.accept(new Attr(project));
        return project;
    }
}
