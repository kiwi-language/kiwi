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
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.ValueFormatter;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.io.File;
import java.util.Collection;
import java.util.List;

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
            LOGGER.info("Compilation done. {} types generated", generatedTypes.size());
            deploy(generatedTypes, typeResolver);
            LOGGER.info("Deploy done");
        }
    }

    private void deploy(Collection<Type> generatedTypes, TypeResolver typeResolver) {
        try (SerializeContext context = SerializeContext.enter()) {
            context.setIncludingCode(true);
            context.setIncludingNodeOutputType(false);
            context.setIncludingValueType(false);
            for (Type metaType : generatedTypes) {
                if (metaType instanceof ClassType classType) {
                    typeResolver.ensureCodeGenerated(classType);
                    context.addWritingCodeType(classType);
                }
            }
            for (Type metaType : generatedTypes) {
                if (metaType instanceof ClassType classType)
                    typeResolver.ensureCodeGenerated(classType);
                context.writeType(metaType);
            }
            context.writeDependencies();
            var typeDTOs = context.getNonSystemTypes();
            System.out.println("Compile succeeded");
            var request = new BatchSaveRequest(typeDTOs);
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
