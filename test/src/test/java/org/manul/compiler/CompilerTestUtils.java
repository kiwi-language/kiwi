package org.manul.compiler;

import org.manul.compiler.analyze.Attr;
import org.manul.compiler.analyze.IdentAttr;
import org.manul.compiler.analyze.ImportResolver;
import org.manul.compiler.analyze.TypeResolver;
import org.manul.compiler.diag.DummyLog;
import org.manul.compiler.element.Package;
import org.manul.compiler.element.*;
import org.manul.compiler.syntax.File;
import org.manul.compiler.syntax.Parser;
import org.manul.compiler.type.Types;
import org.manul.compiler.util.List;
import org.manul.compiler.util.MockEnter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompilerTestUtils {

    public static File parse(String path) {
        try {
            return new Parser(new DummyLog(), Files.readString(Path.of(path))).file();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createConcat(Package rootPkg) {
        var func = new FreeFunc(NameTable.instance.concat, rootPkg);
        new Param(Name.from("s1"), Types.instance.getNullableString(), func);
        new Param(Name.from("s2"), Types.instance.getNullableString(), func);
        func.setRetType(Types.instance.getStringType());
    }

    public static Project attr(File file) {
        var project = MockEnter.enter(List.of(file));
        MockEnter.enterStandard(project);
        ImportResolver.resolve(file, project, new DummyLog());
        var typeResolver = new TypeResolver(project, new DummyLog());
        file.accept(typeResolver);
        file.accept(new IdentAttr(project, new DummyLog()));
        file.accept(new Attr(project, new DummyLog()));
        return project;
    }
}
