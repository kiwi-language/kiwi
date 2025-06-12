package org.metavm.compiler;

import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.Page;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.CompilerHttpUtils;
import org.metavm.compiler.util.MockEnter;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.Constants;
import org.metavm.util.TypeReference;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String HOME = ".metavm";

    private static final String ENV_FILE = HOME + File.separator + ".env";

    private static String selectedEnv = getEnvPath("default");

    private final CompilationTask task;
    private final String sourceRoot;

    public Main(String sourceRoot, String targetDir) throws IOException {
        this.sourceRoot = sourceRoot;
        var sources = listFilePathsRecursively(sourceRoot);
        task = new CompilationTask(sources, targetDir);
    }

    public boolean run() {
        try (var files = Files.walk(Paths.get(sourceRoot))) {
            List<String> sources = new ArrayList<>();
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> sources.add(path.toString()));
            task.parse();
            MockEnter.enterStandard(task.getProject());
            task.analyze();
            if (task.getErrorCount() == 0) {
                task.generate();
                return true;
            }
            else
                return false;
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

    private static void enter(String appName) {
        CompilerHttpUtils.setAppId(2L);
        var apps = CompilerHttpUtils.get("/app", new TypeReference<Page<ApplicationDTO>>() {}).items();
        var appId = Utils.findRequired(apps, app -> app.name().equals(appName),
                () -> "Application '" + appName + "' does not exist").id();
        enterApp(appId);
    }

    private static void enterApp(long appId) {
        CompilerHttpUtils.post("/platform-user/enter-app/" + appId, null, new TypeReference<LoginInfo>() {
        });
        CompilerHttpUtils.setAppId(appId);
        deleteSessionFiles();
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

    private static void deploy(TypeClient typeClient, String targetDir) {
        Utils.ensureDirectoryExists(selectedEnv);
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
        if (isLoggedIn()) {
            CompilerHttpUtils.post("/logout", null, new TypeReference<Void>() {
            });
            deleteSessionFiles();
        }
    }

    private static void deleteSessionFiles() {
        Utils.deleteFile(getAppFile());
        Utils.deleteFile(getTokenFile());
        Utils.deleteFile(getPlatformTokenFile());
    }

    private static void usage() {
        System.out.println("Usage: ");
        System.out.println("kiwi deploy");
        System.out.println("kiwi redeploy");
        System.out.println("kiwi clear");
        System.out.println("kiwi host <host>");
        System.out.println("kiwi login");
        System.out.println("kiwi logout");
        System.out.println("kiwi app");
        System.out.println("kiwi create-app <name>");
        System.out.println("kiwi env");
        System.out.println("kiwi create-env <env>");
        System.out.println("kiwi set-env <env>");
        System.out.println("kiwi delete-env <env>");
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
                case "enter" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    enter(args[1]);
                }
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
                case "build" -> build();
                case "deploy" -> {
                    ensureLoggedIn();
                    if (build())
                        deploy(new HttpTypeClient(), "target");
                }
                default -> usage();
            }
        }
        catch (CompilationException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean build() throws IOException {
        var sourceRoot = "src";
        var f = new File(sourceRoot);
        if (!f.exists() || !f.isDirectory()) {
            System.err.println("Source directory '" + sourceRoot + "' does not exist.");
            return false;
        }
        var main = new Main(sourceRoot, "target");
        return main.run();
    }

    public static List<Path> listFilePathsRecursively(String dirPath) throws IOException {
        Path start = Paths.get(dirPath);
        if (!Files.exists(start) || !Files.isDirectory(start)) {
            throw new IllegalArgumentException("Provided path is not an existing directory: " + dirPath);
        }

        try (Stream<Path> stream = Files.walk(start)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath) // Convert Path to absolute Path
                    .collect(Collectors.toList());
        }
    }

}
