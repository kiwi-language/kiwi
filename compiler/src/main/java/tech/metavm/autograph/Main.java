package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.type.*;
import tech.metavm.user.rest.dto.LoginInfo;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.CompilerHttpUtils;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String HOME = ".metavm";

    private static final String ENV_FILE = HOME + File.separator + ".env";

    private static String selectedEnv = getEnvPath("default");

    public static final String DEFAULT_HOST = "https://metavm.tech/api";

    private final Compiler compiler;
    private final String sourceRoot;

    public Main(String home, String sourceRoot, long appId, String token, TypeClient typeClient, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this.sourceRoot = sourceRoot;
        CompilerContext compilerContext = new CompilerContext(home, typeClient, allocatorStore, columnStore, typeTagStore);
        CompilerHttpUtils.setAppId(appId);
        CompilerHttpUtils.setToken(appId, token);
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

    private static void ensureLoggedIn() {
        if (!isLoggedIn())
            login();
    }

    private static void login() {
        var scanner = new Scanner(System.in);
        System.out.print("username: ");
        var name = scanner.nextLine();
        System.out.print("password: ");
        var password = scanner.nextLine();
        System.out.print("application ID: ");
        var appId = scanner.nextLong();
        doLogin(name, password, appId);
        System.out.println("Logged in successfully");
    }

    private static void doLogin(String loginName, String password, long appId) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        CompilerHttpUtils.setAppId(appId);
        NncUtils.writeFile(getAppFile(), Long.toString(appId));
        NncUtils.writeFile(getTokenFile(), CompilerHttpUtils.getToken());
    }

    private static String getAppFile() {
        return selectedEnv + File.separator + "app";
    }

    private static String getTokenFile() {
        return selectedEnv + File.separator + "token";
    }

    private static String getHostFile() {
        return selectedEnv + File.separator + "host";
    }

    private static boolean isLoggedIn() {
        return CompilerHttpUtils.get("/get-login-info", new TypeReference<LoginInfo>() {
        }).isSuccessful();
    }

    private static void ensureHomeCreated() {
        NncUtils.createDirectories(HOME);
        NncUtils.createDirectories(getEnvPath("default"));
        if (!new File(ENV_FILE).exists())
            NncUtils.writeFile(ENV_FILE, "default");
    }

    private static void createEnv(String name) {
        var path = getEnvPath(name);
        if (new File(path).exists()) {
            System.err.println("Env " + name + " already exists");
            System.exit(1);
        }
        NncUtils.createDirectories(path);
    }

    private static void deleteEnv(String name) {
        var path = getEnvPath(name);
        if (NncUtils.isDirectory(path)) {
            NncUtils.clearDirectory(path);
        } else {
            System.err.println("Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private static void changeEnv(String name) {
        var path = getEnvPath(name);
        if (NncUtils.isDirectory(path)) {
            NncUtils.writeFile(ENV_FILE, name);
            selectedEnv = path;
        } else {
            System.err.println("Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private static void initSelectedEnv() {
        var envFile = new File(ENV_FILE);
        if (envFile.isFile()) {
            var env = NncUtils.readLine(envFile);
            var path = getEnvPath(env);
            if (NncUtils.isDirectory(path)) {
                selectedEnv = path;
                return;
            }
        }
        selectedEnv = getEnvPath("default");
    }

    private static String getEnvPath(String name) {
        return HOME + File.separator + name;
    }

    private static void clear() {
        NncUtils.clearDirectory(HOME);
        System.out.println("Clear successfully");
    }

    private static void changeHost(String host) {
        NncUtils.writeFile(getHostFile(), host);
        System.out.println("Host changed");
    }

    private static String getHost() {
        var hostFile = new File(getHostFile());
        if (hostFile.exists())
            return NncUtils.readLine(hostFile);
        else
            return DEFAULT_HOST;
    }

    private static void logout() {
        if (isLoggedIn()) {
            doLogout();
            System.out.println("Logged out successfully");
        } else {
            System.out.println("Not logged in");
        }
    }

    private static void initializeHttpClient() {
        CompilerHttpUtils.setHost(getHost());
        var tokenFile = new File(getTokenFile());
        var appFile = new File(getAppFile());
        if (tokenFile.exists() && appFile.exists()) {
            var appId = NncUtils.readLong(appFile);
            CompilerHttpUtils.setAppId(appId);
            CompilerHttpUtils.setToken(appId, NncUtils.readLine(tokenFile));
        }
    }

    private static void doLogout() {
        if (isLoggedIn())
            CompilerHttpUtils.post("/logout", null, new TypeReference<Void>() {
            });
    }

    private static void usage() {
        System.out.println("Usage: ");
        System.out.println("metavm deploy");
        System.out.println("metavm clear");
        System.out.println("metavm host <host>");
        System.out.println("metavm login");
        System.out.println("metavm logout");
        System.out.println("metavm env");
        System.out.println("metavm create-env <env>");
        System.out.println("metavm change-env <env>");
        System.out.println("metavm delete-env <env>");
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
        initSelectedEnv();
        initializeHttpClient();
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
            case "login" -> login();
            case "logout" -> logout();
            case "env" -> {
                var selected = NncUtils.readLine(ENV_FILE);
                var home = new File(HOME);
                for (File file : Objects.requireNonNull(home.listFiles())) {
                    if (file.isDirectory())
                        System.out.println(file.getName() + (file.getName().equals(selected) ? " *" : ""));
                }
            }
            case "create-env" -> {
                if (args.length < 2) {
                    usage();
                    return;
                }
                createEnv(args[1]);
            }
            case "change-env" -> {
                if (args.length < 2) {
                    usage();
                    return;
                }
                changeEnv(args[1]);
            }
            case "delete-env" -> {
                if (args.length < 2) {
                    usage();
                    return;
                }
                deleteEnv(args[1]);
            }
            case "deploy" -> {
                var sourceRoot = args.length > 1 ? args[1] : (isMavenProject() ? "src/main/java" : "src");
                var f = new File(sourceRoot);
                if (!f.exists() || !f.isDirectory()) {
                    System.err.println("Source directory '" + sourceRoot + "' does not exist.");
                    return;
                }
                var typeClient = new HttpTypeClient();
                ensureLoggedIn();
                logger.info("Host: " + CompilerHttpUtils.getHost());
                logger.info("Application ID: {}", NncUtils.readLong(getAppFile()));
                var main = new Main(selectedEnv,
                        sourceRoot,
                        NncUtils.readLong(getAppFile()),
                        NncUtils.readLine(getTokenFile()),
                        typeClient,
                        new DirectoryAllocatorStore("/not_exist"),
                        new FileColumnStore("/not_exist"),
                        new FileTypeTagStore("/not_exist"));
                main.run();
            }
            default -> usage();
        }
    }

}
