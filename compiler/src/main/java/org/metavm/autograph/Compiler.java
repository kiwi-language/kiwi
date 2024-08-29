package org.metavm.autograph;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.metavm.api.ChildList;
import org.metavm.autograph.env.IrCoreApplicationEnvironment;
import org.metavm.autograph.env.IrCoreProjectEnvironment;
import org.metavm.autograph.env.LightVirtualFileBase;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.object.type.Klass;
import org.metavm.object.type.ResolutionStage;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.ValueFormatter;
import org.metavm.object.type.rest.dto.BatchSaveRequest;
import org.metavm.object.type.rest.dto.TypeDefDTO;
import org.metavm.system.RegionConstants;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Compiler {

    public static final Logger logger = LoggerFactory.getLogger(Compiler.class);

    public static final String REQUEST_DIR = "/Users/leen/workspace/object/compiler/src/test/resources/requests";

    private static final LightVirtualFileBase.MyVirtualFileSystem fileSystem = LightVirtualFileBase.ourFileSystem;
    private final String baseMod;
    private final String sourceRoot;

    private final IrCoreApplicationEnvironment appEnv;
    private final IrCoreProjectEnvironment projectEnv;
    private final Project project;
    private final CompilerInstanceContextFactory contextFactory;
    private final TypeClient typeClient;
    private final List<String> classNames = new ArrayList<>();

    static {
        NncUtils.ensureDirectoryExists(REQUEST_DIR);
    }

    public Compiler(String sourceRoot, CompilerInstanceContextFactory contextFactory, TypeClient typeClient) {
        this.contextFactory = contextFactory;
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
        try (var context = newContext(); var entry = profiler.enter("compile")) {
            long start = System.currentTimeMillis();
            var typeResolver = new TypeResolverImpl(context);
            var files = NncUtils.map(sources, this::getPsiJavaFile);
            var psiClasses = NncUtils.flatMap(files, file -> List.of(file.getClasses()));
            psiClasses.forEach(k -> classNames.add(k.getQualifiedName()));
            var psiClassTypes = NncUtils.map(
                    psiClasses, TranspileUtils.getElementFactory()::createType
            );
            for (ResolutionStage stage : ResolutionStage.values()) {
                try (var ignored = profiler.enter("stage: " + stage)) {
                    psiClassTypes.forEach(t -> typeResolver.resolve(t, stage));
                    if (stage == ResolutionStage.INIT) {
                        psiClassTypes.forEach(t -> {
                            var klass = Objects.requireNonNull(Objects.requireNonNull(t.resolve()).getUserData(Keys.MV_CLASS));
                            // The builtin mapping will be regenerated on the server side based on the new metadata.
                            // It must be cleared here because it may contain references to obsolete elements.
                            klass.clearBuiltinMapping();
                        });
                    }
                    if (stage == ResolutionStage.DECLARATION) {
                        psiClassTypes.forEach(t -> {
                            var klass = Objects.requireNonNull(Objects.requireNonNull(t.resolve()).getUserData(Keys.MV_CLASS));
                            if (klass.isTemplate())
                                klass.updateParameterized();
                        });
                    }
                }
            }
            var generatedTypes = typeResolver.getGeneratedTypeDefs();
            long elapsed = System.currentTimeMillis() - start;
            logger.info("Compilation done in {} ms. {} types generated", elapsed, generatedTypes.size());
            deploy(generatedTypes, typeResolver);
            logger.info("Deploy done");
            return true;
        } finally {
//            logger.info(profiler.finish(false, true).output());
        }

    }

    private void deploy(Collection<TypeDef> generatedTypeDefs, TypeResolver typeResolver) {
        try (var serContext = SerializeContext.enter();
             var ignored = ContextUtil.getProfiler().enter("deploy")) {
            serContext.includingCode(true)
                    .includeNodeOutputType(false)
                    .includingValueType(false);
            for (var typeDef : generatedTypeDefs) {
                if (typeDef instanceof Klass klass) {
                    typeResolver.ensureCodeGenerated(klass);
                    serContext.addWritingCodeType(klass);
                }
            }
            for (var typeDef : generatedTypeDefs) {
                if (typeDef instanceof Klass klass)
                    typeResolver.ensureCodeGenerated(klass);
                serContext.writeTypeDef(typeDef);
            }
            var typeDefDTOs = new ArrayList<TypeDefDTO>();
            serContext.forEachType(
                    (t -> (t.isIdNull() || !RegionConstants.isSystemId(t.getId().getTreeId()))),
                    t -> {
                        if (t instanceof Klass k && k.isParameterized())
                            return;
                        typeDefDTOs.add(t.toDTO(serContext));
                    }
            );
//            var pFlowDTOs = NncUtils.map(generatedPFlows, f -> f.toPFlowDTO(serContext));
            logger.info("Compile successful");
            var request = new BatchSaveRequest(typeDefDTOs, List.of(), true);
            if (DebugEnv.saveCompileResult)
                saveRequest(request);
            typeClient.batchSave(request);
        }
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    private void saveRequest(BatchSaveRequest request) {
        var path = REQUEST_DIR + File.separator
                + "request." + ValueFormatter.formatTime(System.currentTimeMillis()) + ".json";
        NncUtils.writeJsonToFileWithIndent(path, request);
    }

    public PsiJavaFile getPsiJavaFile(String path) {
        var file = NncUtils.requireNonNull(fileSystem.findFileByPath(path));
        return (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
    }

    private IEntityContext newContext() {
        return contextFactory.newEntityContext(typeClient.getAppId());
    }

}
