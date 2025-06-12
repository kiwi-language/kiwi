package org.metavm.compiler;

import org.metavm.compiler.analyze.Attr;
import org.metavm.compiler.analyze.IdentAttr;
import org.metavm.compiler.analyze.ImportResolver;
import org.metavm.compiler.analyze.TypeResolver;
import org.metavm.compiler.diag.DummyLog;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.syntax.Parser;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;

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
        ImportResolver.resolve(file, project);
        var typeResolver = new TypeResolver(project);
        file.accept(typeResolver);
        file.accept(new IdentAttr(project, new DummyLog()));
        file.accept(new Attr(project, new DummyLog()));
        return project;
    }
}
