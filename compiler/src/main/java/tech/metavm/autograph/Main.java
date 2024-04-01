package tech.metavm.autograph;

import tech.metavm.object.type.AllocatorStore;
import tech.metavm.object.type.DirectoryAllocatorStore;
import tech.metavm.util.AuthConfig;
import tech.metavm.util.LoginUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String DEFAULT_HOME = System.getProperty("user.home") + File.separator + ".metavm";

    public static final String DEFAULT_AUTH_FILE = DEFAULT_HOME + File.separator + "auth";

    private final Compiler compiler;
    private final String sourceRoot;
    private final CompilerContext compilerContext;

    public Main(String home, String sourceRoot, AuthConfig authConfig, TypeClient typeClient, AllocatorStore allocatorStore) {
        this.sourceRoot = sourceRoot;
        compilerContext = new CompilerContext(home, typeClient, allocatorStore);
        LoginUtils.loginWithAuthFile(authConfig, typeClient);
        compilerContext.getBootstrap().boot();
        compilerContext.getTreeLoader().load();
        compiler = new Compiler(sourceRoot, compilerContext.getContextFactory(), typeClient);
    }

    public List<String> run() {
        try(var files = Files.walk(Paths.get(sourceRoot))) {
            List<String> sources = new ArrayList<>();
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> sources.add(path.toString()));
            if(!compiler.compile(sources))
                throw new RuntimeException("Compilation failed");
            return compiler.getClassNames();
        } catch (IOException e) {
            throw new RuntimeException("Compilation failed", e);
        }
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Invalid arguments. Usage: metavm <source root>");
            System.exit(1);
        }
        String sourceRoot = args[0];
        var main = new Main(DEFAULT_HOME, sourceRoot, AuthConfig.fromFile(DEFAULT_AUTH_FILE), new HttpTypeClient(),
                new DirectoryAllocatorStore("/not_exist"));
        main.run();
    }

}
