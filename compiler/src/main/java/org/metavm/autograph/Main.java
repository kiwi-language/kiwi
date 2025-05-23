package org.metavm.autograph;

import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.Page;
import org.metavm.object.type.*;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Compiler compiler;
    private final String sourceRoot;

    public Main(String home, String sourceRoot, String targetDir, long appId, String token, TypeClient typeClient, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this.sourceRoot = sourceRoot;
        CompilerContext compilerContext = new CompilerContext(home, allocatorStore, columnStore, typeTagStore);
        typeClient.setAppId(appId);
        CompilerHttpUtils.setToken(appId, token);
        compilerContext.getBootstrap().boot();
        compiler = new Compiler(sourceRoot, targetDir, typeClient);
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
        doLogin(name, password);
        var apps = listApps();
        System.out.print("application: ");
        var appName = scanner.nextLine();
        var appId = Utils.findRequired(apps, app -> app.name().equals(appName)).id();
        enterApp(appId);
        System.out.println("Logged in successfully");
    }

    private static void doLogin(String loginName, String password) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
    }

    private static void enterApp(long appId) {
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        CompilerHttpUtils.setAppId(appId);
        Utils.writeFile(getAppFile(), Long.toString(appId));
        Utils.writeFile(getTokenFile(), CompilerHttpUtils.getToken());
        Utils.writeFile(getPlatformTokenFile(), CompilerHttpUtils.getToken(2L));
    }

    private static void createApp(String name) {
        var currentAppId = CompilerHttpUtils.getAppId();
        CompilerHttpUtils.setAppId(2);
        var appId = CompilerHttpUtils.post("/app", new ApplicationDTO(null, name, null), new TypeReference<Long>() {
        });
        System.out.println("application ID: " + appId);
        CompilerHttpUtils.setAppId(currentAppId);
    }

    private static void printSourceTag(String name) {
        var path = String.format("/type/source-tag/%s", name.replace('.', '/'));
        var tag = CompilerHttpUtils.get(path, new TypeReference<Integer>() {});
        System.out.println(tag);
    }

    private static String getAppFile() {
        return selectedEnv + File.separator + "app";
    }

    private static String getTokenFile() {
        return selectedEnv + File.separator + "token";
    }

    private static String getPlatformTokenFile() {
        return selectedEnv + File.separator + "platform_token";
    }

    private static String getHostFile() {
        return selectedEnv + File.separator + "host";
    }

    private static boolean isLoggedIn() {
        return CompilerHttpUtils.get("/get-login-info", new TypeReference<LoginInfo>() {
        }).isSuccessful();
    }

    private static void ensureHomeCreated() {
        Utils.createDirectories(HOME);
        Utils.createDirectories(getEnvPath("default"));
        if (!new File(ENV_FILE).exists())
            Utils.writeFile(ENV_FILE, "default");
    }

    private static void createEnv(String name) {
        var path = getEnvPath(name);
        if (new File(path).exists()) {
            System.err.println("Env " + name + " already exists");
            System.exit(1);
        }
        Utils.createDirectories(path);
    }

    private static void deleteEnv(String name) {
        var path = getEnvPath(name);
        if (Utils.isDirectory(path)) {
            Utils.clearDirectory(path);
        } else {
            System.err.println("Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private static void setEnv(String name) {
        var path = getEnvPath(name);
        if (Utils.isDirectory(path)) {
            Utils.writeFile(ENV_FILE, name);
            selectedEnv = path;
        } else {
            System.err.println("Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private static void initSelectedEnv() {
        var envFile = new File(ENV_FILE);
        if (envFile.isFile()) {
            var env = Utils.readLine(envFile);
            var path = getEnvPath(env);
            if (Utils.isDirectory(path)) {
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
        Utils.clearDirectory(HOME);
        System.out.println("Clear successfully");
    }

    private static void changeHost(String host) {
        Utils.writeFile(getHostFile(), host);
        System.out.println("Host changed");
    }

    private static String getHost() {
        var hostFile = new File(getHostFile());
        if (hostFile.exists())
            return Utils.readLine(hostFile);
        else
            return Constants.DEFAULT_HOST;
    }

    private static void logout() {
        if (isLoggedIn()) {
            doLogout();
            System.out.println("Logged out successfully");
        } else {
            System.out.println("Not logged in");
        }
    }

    private static void redeploy(TypeClient typeClient, String targetDir) {
        typeClient.deploy(targetDir + "/target.mva");
    }

    private static List<ApplicationDTO> listApps() {
        var page = CompilerHttpUtils.get("/app", new TypeReference<Page<ApplicationDTO>>() {});
        System.out.println("applications:");
        for (ApplicationDTO app : page.items()) {
            if(app.id() > 2)
                System.out.printf("\t%s%n", app.name());
        }
        return page.items();
    }

    private static void initializeHttpClient() {
        CompilerHttpUtils.setHost(getHost());
        var tokenFile = new File(getTokenFile());
        var platformTokenFile = new File(getPlatformTokenFile());
        var appFile = new File(getAppFile());
        if (tokenFile.exists() && platformTokenFile.exists() && appFile.exists()) {
            var appId = Utils.readLong(appFile);
            CompilerHttpUtils.setAppId(appId);
            CompilerHttpUtils.setToken(appId, Utils.readLine(tokenFile));
            CompilerHttpUtils.setToken(2L, Utils.readLine(platformTokenFile));
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
        System.out.println("metavm redeploy");
        System.out.println("metavm clear");
        System.out.println("metavm host <host>");
        System.out.println("metavm login");
        System.out.println("metavm logout");
        System.out.println("metavm app");
        System.out.println("metavm create-app <name>");
        System.out.println("metavm env");
        System.out.println("metavm create-env <env>");
        System.out.println("metavm set-env <env>");
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
        try {
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
                case "app" -> System.out.println(CompilerHttpUtils.getAppId());
                case "create-app" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    createApp(args[1]);
                }
                case "tag" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    printSourceTag(args[1]);
                }
                case "env" -> {
                    var selected = Utils.readLine(ENV_FILE);
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
                case "set-env" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    setEnv(args[1]);
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
                    logger.info("Application ID: {}", Utils.readLong(getAppFile()));
                    var main = new Main(selectedEnv,
                            sourceRoot,
                            "target",
                            Utils.readLong(getAppFile()),
                            Utils.readLine(getTokenFile()),
                            typeClient,
                            new DirectoryAllocatorStore("/not_exist"),
                            new FileColumnStore("/not_exist"),
                            new FileTypeTagStore("/not_exist"));
                    main.run();
                }
                case "redeploy" -> redeploy(new HttpTypeClient(), "target");
                case "deploy_direct" -> {
                    CompilerHttpUtils.setHost(Constants.DEFAULT_HOST);
                    var main = new Main(
                            args[1],
                            args[2],
                            "target",
                            Long.parseLong(args[3]),
                            args[4],
                            new HttpTypeClient(),
                            new DirectoryAllocatorStore("/not_exist"),
                            new FileColumnStore("/not_exist"),
                            new FileTypeTagStore("/not_exist")
                    );
                    main.run();
                }
                default -> usage();
            }
        }
        catch (CompilerException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
