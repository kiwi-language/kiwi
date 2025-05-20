package org.metavm.compiler;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.metavm.compiler.analyze.*;
import org.metavm.compiler.antlr.KiwiLexer;
import org.metavm.compiler.antlr.KiwiParser;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.generate.ClassFileWriter;
import org.metavm.compiler.generate.Gen;
import org.metavm.compiler.syntax.AstBuilder;
import org.metavm.compiler.syntax.ClassDecl;
import org.metavm.compiler.syntax.File;
import org.metavm.compiler.util.CompilerUtils;
import org.metavm.compiler.util.List;
import org.metavm.flow.KlassOutput;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class CompilationTask {

    private final char[] buf = new char[1024 * 1024];

    private final List<String> paths;
    private final String buildDir;
    private List<File> files = List.nil();
    private final Project project = new Project();

    public CompilationTask(Collection<String> paths, String buildDir) {
        this.paths = List.from(paths);
        this.buildDir = buildDir;
    }

    public List<File> parse() {
        return files = paths.map(path -> AstBuilder.build(antlrParse(path)));
    }

    public Project analyze() {
        var enter = new Enter(project);
        enter.enter(files);
        var meta = new Meta();
        files.forEach(f -> f.accept(meta));
        for (File file : files) {
            ImportResolver.resolve(file, project);
            file.accept(new TypeResolver(project));
        }
        for (File file : files) {
            file.accept(new IdentAttr(project));
        }
        for (File file : files) {
            file.accept(new Attr(project));
        }
        for (File file : files) {
            file.accept(new Lower(project));
        }
        return project;
    }

    public void generate() {
        Utils.clearDirectory(buildDir);
        for (File file : files) {
            var gen = new Gen(project);
            file.accept(gen);
            for (ClassDecl classDeclaration : file.getClassDeclarations()) {
                writeClassFile(classDeclaration.getElement());
            }
        }
        CompilerUtils.createArchive(buildDir);
    }

    public Project getProject() {
        return project;
    }

    private void writeClassFile(Clazz klass) {
        var path = buildDir + '/' + requireNonNull(klass.getQualName()).toString().replace('.', '/') + ".mvclass";
        var bout = new ByteArrayOutputStream();
        var output = new KlassOutput(bout);
        var writer = new ClassFileWriter(output);
        writer.write(klass);
        Utils.writeFile(path, bout.toByteArray());
    }

    private KiwiParser.CompilationUnitContext antlrParse(String path) {
        try (var reader = new BufferedReader(new FileReader(path))) {
            int n = reader.read(buf);
            var source = new String(buf, 0, n);
            var input = CharStreams.fromString(source);
            var parser = new KiwiParser(new CommonTokenStream(new KiwiLexer(input)));
            parser.setErrorHandler(new BailErrorStrategy());
            return parser.compilationUnit();
        } catch (IOException e) {
            throw new InternalException("Can not read source '" + path + "'", e);
        }

    }

}
