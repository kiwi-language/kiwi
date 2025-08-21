package org.metavm.compiler;

import lombok.SneakyThrows;
import org.metavm.compiler.analyze.*;
import org.metavm.compiler.apigen.ApiGenerator;
import org.metavm.compiler.apigen.ApiGeneratorV1;
import org.metavm.compiler.apigen.ApiGeneratorV3;
import org.metavm.compiler.diag.DefaultLog;
import org.metavm.compiler.diag.DiagFactory;
import org.metavm.compiler.diag.DiagSource;
import org.metavm.compiler.element.ClassScope;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.file.FileManager;
import org.metavm.compiler.file.PathSourceFile;
import org.metavm.compiler.generate.ClassFileWriter;
import org.metavm.compiler.generate.Gen;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.CompilerUtils;
import org.metavm.compiler.util.List;
import org.metavm.flow.KlassOutput;
import org.metavm.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class CompilationTask {

    private final List<Path> paths;
    private final Path buildDir;
    private List<File> files = List.nil();
    private final Project project = new Project();
    private final FileManager fileManager = new FileManager();
    private final boolean aiLint;

    private final DefaultLog log = new DefaultLog(
            new PathSourceFile(Path.of(""), fileManager),
            DiagFactory.instance,
            new PrintWriter(System.out),
            new PrintWriter(System.err)
    );


    public CompilationTask(Collection<Path> paths, Path buildDir, boolean aiLint) {
        this.paths = List.from(paths);
        this.buildDir = buildDir;
        this.aiLint = aiLint;
    }

    public List<File> parse() {
        try {
            return files = paths.map(path -> {
                var file = new PathSourceFile(path, fileManager);
                log.setSource(new DiagSource(file, log));
                try {
                    var cb = file.getContent();
                    var parser = new Parser(log, new Lexer(log, cb.array(), cb.length()));
                    return parser.file();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        finally {
            log.flush();
        }
    }

    public Project analyze() {
        try {
            var enter = new Enter(project, log);
            enter.enter(files);
            var meta = new Meta();
            files.forEach(f -> f.accept(meta));
            for (File file : files) {
                log.setSourceFile(file.getSourceFile());
                ImportResolver.resolve(file, project, log);
                file.accept(new TypeResolver(project, log));
            }
            for (File file : files) {
                log.setSourceFile(file.getSourceFile());
                file.accept(new IdentAttr(project, log));
            }
            for (File file : files) {
                log.setSourceFile(file.getSourceFile());
                file.accept(new Attr(project, log));
            }
            for (File file : files) {
                log.setSourceFile(file.getSourceFile());
                file.accept(new Check(project, log));
            }
            if (aiLint)
                SenseLint.run(files, log);
            return project;
        }
        finally {
            log.flush();
        }
    }

    public void generate() {
        try {
            for (File file : files) {
                log.setSourceFile(file.getSourceFile());
                file.accept(new Lower(project, log));
            }
            Utils.clearDirectory(buildDir);
            for (File file : files) {
                var gen = new Gen(project, log);
                file.accept(gen);
                for (ClassDecl classDeclaration : file.getClassDeclarations()) {
                    writeClassFile(classDeclaration.getElement());
                }
            }
            CompilerUtils.createArchive(buildDir);
        }
        finally {
            log.flush();
        }
    }

    public void generateApi(long version) {
        var classes = List.<Clazz>builder();
        files.forEach(f -> {
            for (var classDecl : f.getClassDeclarations()) {
                classes.append(classDecl.getElement());
            }
        });
        var c = classes.build();
        var api = getApiGenerator(version).generate(c);
        var apiBuildDir = buildDir.getParent().resolve("apigen");
        Utils.writeFile(apiBuildDir.resolve("api.ts"), api.getBytes(StandardCharsets.UTF_8));
    }

    private ApiGenerator getApiGenerator(long version) {
        if (version <= 2)
            return new ApiGeneratorV1(version > 1);
        else if (version == 3)
            return new ApiGeneratorV3();
        else
            throw new CompilationException(
                "Unsupported API version: " + version + ". Supported versions are 1, 2, and 3."
            );
    }

    public Project getProject() {
        return project;
    }

    @SneakyThrows
    private void writeClassFile(Clazz klass) {
        var path = getClassFilePath(buildDir, klass);
        var bout = new ByteArrayOutputStream();
        var output = new KlassOutput(bout);
        var writer = new ClassFileWriter(output);
        writer.write(klass);
        Utils.writeFile(path, bout.toByteArray());
    }

    private Path getClassFilePath(Path root, Clazz cls) {
        return getClassScopePath(root, cls.getScope()).resolve(cls.getName() + ".mvclass");
    }

    private Path getClassScopePath(Path root, ClassScope scope) {
        return switch (scope) {
            case Package pkg -> pkg.isRoot() ? root :
                getClassScopePath(root, requireNonNull(pkg.getParent())).resolve(pkg.getName().toString());
            case Clazz parentCls -> getClassFilePath(root, parentCls);
            default -> throw new IllegalStateException("Cannot get path for " + scope);
        };
    }

    public int getErrorCount() {
        return log.getErrorCount();
    }

}
