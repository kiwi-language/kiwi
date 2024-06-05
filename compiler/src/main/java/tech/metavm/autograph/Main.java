package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.type.*;
import tech.metavm.util.AuthConfig;
import tech.metavm.util.CompilerHttpUtils;
import tech.metavm.util.LoginUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String DEFAULT_HOME = System.getProperty("user.home") + File.separator + ".metavm";

    public static final String DEFAULT_AUTH_FILE = DEFAULT_HOME + File.separator + "auth";

    public static final String HOST_FILE = DEFAULT_HOME + File.separator + "host";

    public static final String DEFAULT_HOST = "https://metavm.tech/api";

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
        ensureHomeCreated();
        try (var authWriter = new PrintWriter(new FileOutputStream(DEFAULT_AUTH_FILE))) {
            authWriter.println(appId);
            authWriter.println(name);
            authWriter.println(password);
        } catch (IOException e) {
            System.out.println("Fail to write auth file");
            System.exit(1);
        }
    }

    private static void ensureHomeCreated() {
        try {
            Files.createDirectories(Paths.get(DEFAULT_HOME));
        } catch (IOException e) {
            System.err.println("Fail to create home directory: " + DEFAULT_HOME);
        }
    }

    private static void reset() {
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
        System.out.println("Reset successfully");
    }

    private static void changeHost(String host) {
        try (var hostWriter = new PrintWriter(new FileOutputStream(HOST_FILE))) {
            hostWriter.println(host);
            System.out.println("Host changed");
        } catch (IOException e) {
            System.err.println("Fail to write host file: " + HOST_FILE);
            System.exit(1);
        }
    }

    private static String getHost() {
        var hostFile = new File(HOST_FILE);
        if (hostFile.exists()) {
            try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(hostFile)))) {
                return reader.readLine();
            } catch (IOException e) {
                System.err.println("Fail to read host file: " + HOST_FILE);
                System.exit(1);
                throw new RuntimeException();
            }
        } else
            return DEFAULT_HOST;
    }

    private static void logout() {
        var authFile = new File(DEFAULT_AUTH_FILE);
        if(authFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            authFile.delete();
            System.out.println("Logged out successfully");
        }
        else {
            System.out.println("Not logged in");
        }
    }

    private static void usage() {
        System.out.println("Usage: ");
        System.out.println("metavm deploy");
        System.out.println("metavm reset");
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
            case "reset" -> reset();
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
                if(!f.exists() || !f.isDirectory()) {
                    System.err.println("Source directory '" + sourceRoot + "' does not exist.");
                    return;
                }
                ensureAuthorized();
                CompilerHttpUtils.host = getHost();
                logger.info("Host: " + CompilerHttpUtils.host);
                var main = new Main(DEFAULT_HOME, sourceRoot, AuthConfig.fromFile(DEFAULT_AUTH_FILE), new HttpTypeClient(),
                        new DirectoryAllocatorStore("/not_exist"), new FileColumnStore("/not_exist"), new FileTypeTagStore("/not_exist"));
                main.run();
            }
            default -> usage();
        }
    }

}
