package tech.metavm.autograph;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.autograph.env.IrCoreApplicationEnvironment;
import tech.metavm.autograph.env.IrCoreProjectEnvironment;
import tech.metavm.autograph.env.LightVirtualFileBase;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.ValueFormatter;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.system.RegionConstants;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Compiler {

    public static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    public static final String REQUEST_DIR = "/Users/leen/workspace/object/compiler/src/test/resources/requests";

    private static final LightVirtualFileBase.MyVirtualFileSystem fileSystem = LightVirtualFileBase.ourFileSystem;
    private final String baseMod;
    private final String sourceRoot;

    private final IrCoreApplicationEnvironment appEnv;
    private final IrCoreProjectEnvironment projectEnv;
    private final Project project;
    private final CompilerInstanceContextFactory contextFactory;

    static {
        NncUtils.ensureDirectoryExists(REQUEST_DIR);
    }

    public Compiler(String sourceRoot, CompilerInstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
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
        TranspileUtil.setElementFactory(project.getService(PsiElementFactory.class));
    }

    public void compile(List<String> sources) {
        try (var context = newContext()) {
            var typeResolver = new TypeResolverImpl(context);
            var files = NncUtils.map(sources, this::getPsiJavaFile);
            var psiClasses = NncUtils.flatMap(files, file -> List.of(file.getClasses()));
            var psiClassTypes = NncUtils.map(
                    psiClasses, TranspileUtil.getElementFactory()::createType
            );
            psiClassTypes.forEach(typeResolver::resolve);
            var generatedTypes = typeResolver.getGeneratedTypes();
            var generatedPFlows = typeResolver.getGeneratedParameterizedFlows();
            LOGGER.info("Compilation done. {} types generated", generatedTypes.size());
            deploy(generatedTypes, generatedPFlows, typeResolver);
            LOGGER.info("Deploy done");
        }
    }

    private void deploy(Collection<Type> generatedTypes,
                        Collection<Flow> generatedPFlows,
                        TypeResolver typeResolver) {
        try (SerializeContext serContext = SerializeContext.enter()) {
            serContext.includingCode(true)
                    .includeNodeOutputType(false)
                    .includingValueType(false)
                    .writeParameterizedTypeAsPTypeDTO(true);
            for (Type metaType : generatedTypes) {
                if (metaType instanceof ClassType classType) {
                    typeResolver.ensureCodeGenerated(classType);
                    serContext.addWritingCodeType(classType);
                }
            }
            for (Type metaType : generatedTypes) {
                if (metaType instanceof ClassType classType)
                    typeResolver.ensureCodeGenerated(classType);
                serContext.writeType(metaType);
            }
            var typeDTOs =
                    serContext.getTypes((t -> (t.isIdNull() || !RegionConstants.isSystemId(t.tryGetId()))));
            var pFlowDTOs = NncUtils.map(generatedPFlows, f -> f.toPFlowDTO(serContext));
            LOGGER.info("Compile successful");
            var request = new BatchSaveRequest(typeDTOs, List.of(), pFlowDTOs);
            saveRequest(request);
            HttpUtils.post("/type/batch", request, new TypeReference<List<Long>>() {
            });
        }
    }

    private void saveRequest(BatchSaveRequest request) {
        var path = REQUEST_DIR + File.separator
                + "request." + ValueFormatter.formatTime(System.currentTimeMillis()) + ".json";
        NncUtils.writeJsonToFile(path, request);
    }

    public PsiJavaFile getPsiJavaFile(String path) {
        var file = NncUtils.requireNonNull(fileSystem.findFileByPath(path));
        return (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
    }

    private IEntityContext newContext() {
        return contextFactory.newEntityContext(HttpUtils.getAppId());
    }

}
