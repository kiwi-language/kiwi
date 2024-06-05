package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.type.*;
import tech.metavm.util.AuthConfig;
import tech.metavm.util.CompilerHttpUtils;
import tech.metavm.util.LoginUtils;
import tech.metavm.util.NncUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String GLOBAL_HOME = System.getProperty("user.home") + File.separator + ".metavm";

    public static final String LOCAL_HOME = ".metavm";

    public static final String AUTH_FILE = GLOBAL_HOME + File.separator + "auth";

    public static final String HOST_FILE = GLOBAL_HOME + File.separator + "host";

    public static final String APP_FILE = LOCAL_HOME + File.separator + "app";

    public static final String DEFAULT_HOST = "https://metavm.tech/api";

    private final Compiler compiler;
    private final String sourceRoot;

    public Main(String home, String sourceRoot, long appId, AuthConfig authConfig, TypeClient typeClient, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this.sourceRoot = sourceRoot;
        CompilerContext compilerContext = new CompilerContext(home, typeClient, allocatorStore, columnStore, typeTagStore);
        LoginUtils.loginWithAuthFile(appId, authConfig, typeClient);
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

    private static void ensureAppIdInitialized() {
        var appFile = new File(APP_FILE);
        if (appFile.exists())
            return;
        System.out.print("application ID: ");
        var scanner = new Scanner(System.in);
        long appId;
        try {
            appId = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid application ID");
            System.exit(1);
            return;
        }
        NncUtils.writeFile(appFile, Long.toString(appId));
    }

    private static void ensureAuthorized() {
        var autFile = new File(AUTH_FILE);
        if (!autFile.exists())
            auth();
    }

    private static void auth() {
        var scanner = new Scanner(System.in);
        System.out.print("username: ");
        var name = scanner.nextLine();
        System.out.print("password: ");
        var password = scanner.nextLine();
        NncUtils.writeFile(AUTH_FILE, name + "\n" + password);
    }

    private static void ensureHomeCreated() {
        try {
            Files.createDirectories(Paths.get(GLOBAL_HOME));
            Files.createDirectories(Paths.get(LOCAL_HOME));
        } catch (IOException e) {
            System.err.println("Fail to create home directory: " + GLOBAL_HOME);
        }
    }

    private static void clear() {
        NncUtils.clearDirectory(LOCAL_HOME);
        System.out.println("Clear successfully");
    }

    private static void changeHost(String host) {
        NncUtils.writeFile(HOST_FILE, host);
        System.out.println("Host changed");
    }

    private static String getHost() {
        var hostFile = new File(HOST_FILE);
        if (hostFile.exists())
            return NncUtils.readLine(hostFile);
        else
            return DEFAULT_HOST;
    }

    private static void logout() {
        var authFile = new File(AUTH_FILE);
        if (authFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            authFile.delete();
            System.out.println("Logged out successfully");
        } else {
            System.out.println("Not logged in");
        }
    }

    private static void usage() {
        System.out.println("Usage: ");
        System.out.println("metavm deploy");
        System.out.println("metavm clear");
        System.out.println("metavm host <host>");
        System.out.println("metavm logout");
    }

    public static boolean isMavenProject() {
        return new File("pom.xml").exists();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            return;
        }
        ensureHomeCreated();
        String command = args[0];
        switch (command) {
            case "clear" -> clear();
            case "host" -> {
                if (args.length < 2) {
                    System.out.println(getHost());
                    return;
                }
                changeHost(args[1].trim());
            }
            case "logout" -> logout();
            case "deploy" -> {
                var sourceRoot = args.length > 1 ? args[1] : (isMavenProject() ? "src/main/java" : "src");
                var f = new File(sourceRoot);
                if (!f.exists() || !f.isDirectory()) {
                    System.err.println("Source directory '" + sourceRoot + "' does not exist.");
                    return;
                }
                ensureAuthorized();
                ensureAppIdInitialized();
                CompilerHttpUtils.host = getHost();
                logger.info("Host: " + CompilerHttpUtils.host);
                var main = new Main(LOCAL_HOME,
                        sourceRoot,
                        NncUtils.readLong(APP_FILE),
                        AuthConfig.fromFile(AUTH_FILE),
                        new HttpTypeClient(),
                        new DirectoryAllocatorStore("/not_exist"),
                        new FileColumnStore("/not_exist"),
                        new FileTypeTagStore("/not_exist"));
                main.run();
            }
            default -> usage();
        }
    }

}
