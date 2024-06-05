package tech.metavm.autograph;

import tech.metavm.object.type.*;
import tech.metavm.util.AuthConfig;
import tech.metavm.util.LoginUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final String DEFAULT_HOME = System.getProperty("user.home") + File.separator + ".metavm";

    public static final String DEFAULT_AUTH_FILE = DEFAULT_HOME + File.separator + "auth";

    private final Compiler compiler;
    private final String sourceRoot;
    private final CompilerContext compilerContext;

    public Main(String home, String sourceRoot, AuthConfig authConfig, TypeClient typeClient, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this.sourceRoot = sourceRoot;
        compilerContext = new CompilerContext(home, typeClient, allocatorStore, columnStore, typeTagStore);
        LoginUtils.loginWithAuthFile(authConfig, typeClient);
        compilerContext.getBootstrap().boot();
        compilerContext.getTreeLoader().load();
        compiler = new Compiler(sourceRoot, compilerContext.getContextFactory(), typeClient);
    }

    public List<String> run() {
        try (var files = Files.walk(Paths.get(sourceRoot))) {
            List<String> sources = new ArrayList<>();
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> sources.add(path.toString()));
            if (!compiler.compile(sources))
                throw new RuntimeException("Compilation failed");
            return compiler.getClassNames();
        } catch (IOException e) {
            throw new RuntimeException("Compilation failed", e);
        }
    }

    private static void ensureAuthorized() {
        var autFile = new File(DEFAULT_AUTH_FILE);
        if (autFile.exists())
            return;
        var scanner = new Scanner(System.in);
        System.out.print("appId: ");
        long appId;
        try {
            appId = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid appId");
            System.exit(1);
            return;
        }
        System.out.print("username: ");
        var name = scanner.nextLine();
        System.out.print("password: ");
        var password = scanner.nextLine();
        try {
            Files.createDirectories(Paths.get(DEFAULT_HOME));
        } catch (IOException e) {
            System.err.println("Fail to create home directory: " + DEFAULT_HOME);
        }
        try (var authWriter = new PrintWriter(new FileOutputStream(DEFAULT_AUTH_FILE))) {
            authWriter.println(appId);
            authWriter.println(name);
            authWriter.println(password);
        } catch (IOException e) {
            System.out.println("Fail to write auth file");
            System.exit(1);
        }
    }

    private static void clear() {
        var homeDir = Path.of(DEFAULT_HOME);
        if (Files.exists(homeDir)) {
            try (var files = Files.walk(homeDir)) {
                //noinspection ResultOfMethodCallIgnored
                files.sorted(Comparator.reverseOrder())
                         .forEach(f -> f.toFile().delete());
            } catch (IOException e) {
                System.err.println("fail to visit home directory");
                System.exit(1);
            }
        }
        System.out.println("clear successful");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ");
            System.out.println("metavm <source root>");
            System.out.println("metavm -clear");
            System.exit(1);
        }
        String sourceRoot = args[0];
        if(sourceRoot.equals("-clear")) {
            clear();
            return;
        }
        ensureAuthorized();
        var main = new Main(DEFAULT_HOME, sourceRoot, AuthConfig.fromFile(DEFAULT_AUTH_FILE), new HttpTypeClient(),
                new DirectoryAllocatorStore("/not_exist"), new FileColumnStore("/not_exist"), new FileTypeTagStore("/not_exist"));
        main.run();
    }

}
