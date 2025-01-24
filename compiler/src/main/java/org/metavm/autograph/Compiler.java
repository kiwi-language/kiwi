package org.metavm.autograph;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.metavm.api.ChildList;
import org.metavm.autograph.env.IrCoreApplicationEnvironment;
import org.metavm.autograph.env.IrCoreProjectEnvironment;
import org.metavm.autograph.env.LightVirtualFileBase;
import org.metavm.classfile.ClassFileWriter;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.KlassOutput;
import org.metavm.object.type.Klass;
import org.metavm.object.type.ResolutionStage;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtils.executeCommand;

public class Compiler {

    public static final Logger logger = LoggerFactory.getLogger(Compiler.class);

    public static final String REQUEST_DIR = "/Users/leen/workspace/object/compiler/src/test/resources/requests";

    private static final LightVirtualFileBase.MyVirtualFileSystem fileSystem = LightVirtualFileBase.ourFileSystem;
    public static final List<CompileStage> prepareStages = List.of(
            new CompileStage(
                    file -> true,
                    file -> {
                        file.accept(new EnhancedSwitchTransformer());
                        file.accept(new OrdinalRecorder());
                        file.accept(new EnumSwitchTransformer());
                        file.accept(new NewIndexTransformer());
                        file.accept(new RecordToClass());
                        file.accept(new EnumTransformer());
                        file.accept(new BodyNormalizer());
                        file.accept(new RawTypeTransformer());
                        file.accept(new ArrayInitializerTransformer());
                    }
            ),
            new CompileStage(
                    file -> true,
                    file -> {
                        file.accept(new DefaultConstructorCreator());
                        file.accept(new SyntheticClassNameTracker());
                        file.accept(new AnonymousClassTransformer());
                    }
            ),
            new CompileStage(
                    file -> true,
                    file -> {
                        file.accept(new SuperCallInserter());
                        file.accept(new ObjectSuperCallRemover());
                        file.accept(new FieldInitializerSetter());
                        file.accept(new FieldInitializerMover());
                        file.accept(new FieldInitializingTransformer());
                    }
            ),
            new CompileStage(
                    file -> true,
                    file -> file.accept(new InnerClassQualifier())
            ),
            new CompileStage(
                    file -> true,
                    file -> file.accept(new NewObjectTransformer())
            ),
            new CompileStage(
                    file -> true,
                    file -> {
                        resolveQnAndActivity(file);
                        file.accept(new VarargsTransformer());
                        resolveQnAndActivity(file);
                        file.accept(new NullSwitchCaseAppender());
                        file.accept(new DefaultSwitchCaseAppender());
                        resolveQnAndActivity(file);
                        file.accept(new StringConcatTransformer());
                        file.accept(new UnboxingTransformer());
                        file.accept(new ExplicitTypeWideningTransformer());
                    }
            )
    );

    private final String baseMod;
    private final String sourceRoot;
    private final String targetDir;

    private final IrCoreApplicationEnvironment appEnv;
    private final IrCoreProjectEnvironment projectEnv;
    private final Project project;
    private final TypeClient typeClient;
    private final List<String> classNames = new ArrayList<>();

    static {
        Utils.ensureDirectoryExists(REQUEST_DIR);
    }

    public Compiler(String sourceRoot, String targetDir, TypeClient typeClient) {
        this.targetDir = targetDir;
        this.typeClient = typeClient;
        var javaHome = System.getProperty("java.home");
        this.baseMod = javaHome + "/jmods/java.base.jmod";
        this.sourceRoot = sourceRoot;
        appEnv = new IrCoreApplicationEnvironment(() -> {
        });
        projectEnv = new IrCoreProjectEnvironment(() -> {
        }, appEnv);
        var javaBaseDir = appEnv.getJarFileSystem().findFileByPath(this.baseMod + "!/classes");
        projectEnv.addSourcesToClasspath(requireNonNull(javaBaseDir));
        projectEnv.addSourcesToClasspath(requireNonNull(fileSystem.findFileByPath(this.sourceRoot)));

//        var apiSource = appEnv.getJarFileSystem().findFileByPath("/Users/leen/workspace/object/api/target/api-1.0-SNAPSHOT.jar!/");
//        var apiSource = requireNonNull(fileSystem.findFileByPath("/Users/leen/workspace/object/api/target/classes/"));
//        projectEnv.addSourcesToClasspath(requireNonNull(apiSource));
//        projectEnv.addSourcesToClasspath(requireNonNull(fileSystem.findFileByPath("/Users/leen/workspace/object/api/src/main/java")));
        try {
            var apiSource = Paths.get(ChildList.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            if (apiSource.getName().endsWith(".jar")) {
                projectEnv.addSourcesToClasspath(
                        Objects.requireNonNull(appEnv.getJarFileSystem().findFileByPath(apiSource.getAbsolutePath() + "!/"))
                );
            } else {
                var compilerSource = Paths.get(Compiler.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
                if (compilerSource.isDirectory()) {
                    var apiSourceDir = compilerSource.getAbsolutePath().replace("/compiler/target/classes", "/api/src/main/java");
                    projectEnv.addSourcesToClasspath(requireNonNull(fileSystem.findFileByPath(apiSourceDir)));
                } else {
                    throw new InternalException("Can not locate API source");
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        project = projectEnv.getProject();
        TranspileUtils.init(project.getService(PsiElementFactory.class), project);
    }

    public boolean compile(List<String> sources) {
        var profiler = ContextUtil.getProfiler();
        Constants.disableMaintenance();
        long start = System.currentTimeMillis();
        var typeResolver = new TypeResolverImpl();
        var files = Utils.map(sources, this::getPsiJavaFile);
        for (int i = 0; i < prepareStages.size(); i++) {
            executeStage(prepareStages.get(i), i, files);
        }
        var psiClasses = Utils.flatMap(files, TranspileUtils::getAllClasses);
        psiClasses.forEach(k -> classNames.add(k.getQualifiedName()));
        for (var stage : stages) {
//                logger.debug("Stage {}", stage.stage.name());
            try (var ignored1 = profiler.enter("stage: " + stage)) {
                var sortedKlasses = stage.sort(psiClasses, typeResolver);
                var psiClassTypes = Utils.map(
                        sortedKlasses,
                        TranspileUtils.getElementFactory()::createType
                );
                psiClassTypes.forEach(t -> {
                     typeResolver.resolve(t, stage.stage);
                    if (stage.stage == ResolutionStage.DECLARATION) {
                        var klass = Objects.requireNonNull(Objects.requireNonNull(t.resolve()).getUserData(Keys.MV_CLASS),
                                () -> "Cannot find MvClass for " + TranspileUtils.getQualifiedName(Objects.requireNonNull(t.resolve())));
                        if (klass.isTemplate())
                            klass.updateParameterized();
                    }
                });
            }
        }
        var klasses = typeResolver.getGeneratedKlasses();
        long elapsed = System.currentTimeMillis() - start;
        logger.info("Compilation done in {} ms. {} types generated", elapsed, klasses.size());
        deploy(klasses, typeResolver);
        logger.info("Deploy done");
        Constants.enableMaintenance();
        logger.info(profiler.finish(false, true).output());
        return true;
    }

    private static final List<Stage> stages = List.of(
            new Stage(ResolutionStage.INIT, Compiler::initDependencies),
            new Stage(ResolutionStage.SIGNATURE, Compiler::noDependencies),
            new Stage(ResolutionStage.DECLARATION, Compiler::declarationDependencies),
            new Stage(ResolutionStage.DEFINITION, Compiler::noDependencies)
    );

    private record Stage(ResolutionStage stage, Function<PsiClass, Set<PsiClass>> getDependencies) {
        List<PsiClass> sort(Collection<PsiClass> classes, TypeResolver typeResolver) {
            return Sorter.sort(classes, getDependencies, typeResolver);
        }
    }

    private static Set<PsiClass> noDependencies(PsiClass klass) {
        return Set.of();
    }

    public static Set<PsiClass> initDependencies(PsiClass klass) {
        var parent = TranspileUtils.getProperParent(klass, Set.of(PsiMethod.class, PsiClassInitializer.class, PsiClass.class));
        if(parent instanceof PsiClass k)
            return Set.of(k);
        else
            return Set.of();
    }

    private static Set<PsiClass> declarationDependencies(PsiClass klass) {
        var deps =  new HashSet<>(Arrays.asList(klass.getSupers()));
        var parent = TranspileUtils.getProperParent(klass, Set.of(PsiMethod.class, PsiClassInitializer.class, PsiClass.class));
        if(parent instanceof PsiMethod method)
            deps.add(method.getContainingClass());
        else if (parent instanceof PsiClassInitializer classInit)
            deps.add(classInit.getContainingClass());
        else if(parent instanceof PsiClass declaringKlass)
            deps.add(declaringKlass);
        return deps;
    }

    private static class Sorter {

        public static List<PsiClass> sort(Collection<PsiClass> classes,
                                            Function<PsiClass, Set<PsiClass>> getDependencies,
                                          TypeResolver typeResolver) {
            var sorter = new Sorter(classes, getDependencies, typeResolver);
            return sorter.result;
        }

        private final IdentitySet<PsiClass> visited = new IdentitySet<>();
        private final IdentitySet<PsiClass> visiting = new IdentitySet<>();
        private final List<PsiClass> result = new ArrayList<>();
        private final Function<PsiClass, Set<PsiClass>> getDependencies;
        private final TypeResolver typeResolver;

        public Sorter(Collection<PsiClass> classes, Function<PsiClass, Set<PsiClass>> getDependencies, TypeResolver typeResolver) {
            this.getDependencies = getDependencies;
            this.typeResolver = typeResolver;
            classes.forEach(this::visit);
        }

        private void visit(PsiClass klass) {
            if (visiting.contains(klass)) {
                throw new InternalException("Circular reference");
            }
            if (visited.contains(klass)) {
                return;
            }
            visiting.add(klass);
            getDependencies(klass).forEach(this::visitDependency);
            result.add(klass);
            visiting.remove(klass);
            visited.add(klass);
        }

        private void visitDependency(PsiClass klass) {
            if(!TranspileUtils.isObjectClass(klass) && !typeResolver.isBuiltinClass(klass))
                visit(klass);
        }

        private Set<PsiClass> getDependencies(PsiClass klass) {
            return getDependencies.apply(klass);
        }

    }

    private void executeStage(CompileStage stage, int i, List<PsiJavaFile> files) {
        try (var ignored = ContextUtil.getProfiler().enter("executeStage" + i)) {
            files.forEach(file -> {
                if (stage.filter().test(file))
                    executeCommand(() -> stage.action().accept(file));
            });
        }
    }

    private void deploy(Collection<Klass> klasses, TypeResolver typeResolver) {
        try (var serContext = SerializeContext.enter();
             var ignored = ContextUtil.getProfiler().enter("deploy")) {
            Utils.clearDirectory(targetDir);
            serContext.includingCode(true)
                    .includeNodeOutputType(false)
                    .includingValueType(false);
            for (var klass : klasses) {
                typeResolver.ensureCodeGenerated(klass);
            }
            for (var klass : klasses) {
                if(!klass.isInner() && !klass.isLocal())
                    writeClassFile(klass);
            }
            logger.info("Compile successful");
            createArchive();
            Constants.enableMaintenance();
            typeClient.deploy(targetDir + "/target.mva");
        }
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    private void writeClassFile(Klass klass) {
        var path = targetDir + '/' + Objects.requireNonNull(klass.getQualifiedName()).replace('.', '/') + ".mvclass";
        var bout = new ByteArrayOutputStream();
        var output = new KlassOutput(bout);
        var writer = new ClassFileWriter(output);
        writer.write(klass);
        Utils.writeFile(path, bout.toByteArray());
    }

    private void createArchive() {
        var targetDir = Paths.get(this.targetDir);
        var zipFilePath = targetDir + "/target.mva";
        try(var zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));
            var files = Files.walk(targetDir)) {
                    files.filter(f -> f.toString().endsWith(".mvclass"))
                        .forEach(f -> {
                            var zipEntry = new ZipEntry(targetDir.relativize(f).toString());
                            try {
                                zipOut.putNextEntry(zipEntry);
                                Files.copy(f, zipOut);
                                zipOut.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PsiJavaFile getPsiJavaFile(String path) {
        var file = Objects.requireNonNull(fileSystem.findFileByPath(path));
        return (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
    }

    private static void resolveQnAndActivity(PsiJavaFile file) {
        file.accept(new QnResolver());
        file.accept(new ActivityAnalyzer());
        if(DebugEnv.debugging)
            file.accept(new ActivityPrinter());
    }
}
