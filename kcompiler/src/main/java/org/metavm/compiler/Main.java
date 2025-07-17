package org.metavm.compiler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Main {

    public final Path home;
    private final Path envFile;
    private Path selectedEnv;
    private final CompilationTask task;
    private final Path targetRoot;

    public Main(Path root) throws IOException {
        var sourceRoot = root.resolve("src");
        this.targetRoot = root.resolve("target");
        var sources = listFilePathsRecursively(sourceRoot);
        task = new CompilationTask(sources, targetRoot);
        home = root.resolve(".metavm");
        envFile = home.resolve(".env");
        selectedEnv = getEnvPath("default");
    }

    public boolean generateApi() {
        if (!ensureSourceAvailable())
            return false;
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0) {
            task.generateApi();
            return true;
        }
        else
            return false;
    }

    void ensureLoggedIn() {
        if (!isLoggedIn())
            login();
    }

    void login() {
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

    private void doLogin(String loginName, String password) {
        CompilerHttpUtils.post("/login", new LoginRequest(Constants.PLATFORM_APP_ID, loginName, password),
                new TypeReference<LoginInfo>() {
                });
    }

    private void enter(String appName) {
        var apps = CompilerHttpUtils.get("/app", new TypeReference<Page<ApplicationDTO>>() {}).items();
        var appId = Utils.findRequired(apps, app -> app.name().equals(appName),
                () -> "Application '" + appName + "' does not exist").id();
        enterApp(appId);
    }

    private void enterApp(long appId) {
        Utils.writeFile(getAppFile(), Long.toString(appId));
        Utils.writeFile(getTokenFile(), CompilerHttpUtils.getToken());
    }

    private void createApp(String name) {
        var appId = CompilerHttpUtils.post("/app", new ApplicationDTO(null, name, null), new TypeReference<Long>() {
        });
        System.out.println("application ID: " + appId);
    }

    private void printSourceTag(String name) {
        var tag = CompilerHttpUtils.post("/type/source-tag",
                Map.of("appId", getAppId(), "name", name),
                new TypeReference<Integer>() {});
        System.out.println(tag);
    }

    private String getAppFile() {
        return selectedEnv + File.separator + "app";
    }

    private String getTokenFile() {
        return selectedEnv + File.separator + "token";
    }

    private String getHostFile() {
        return selectedEnv + File.separator + "host";
    }

    boolean isLoggedIn() {
        return CompilerHttpUtils.get("/get-login-info", new TypeReference<LoginInfo>() {
        }).isSuccessful();
    }

    private void ensureHomeCreated() throws IOException {
        Utils.createDirectories(home);
        Utils.createDirectories(getEnvPath("default"));
        if (!envFile.toFile().exists())
            Files.writeString(envFile, "default");
    }

    private void createEnv(String name) {
        var path = getEnvPath(name);
        if (path.toFile().exists()) {
            System.err.println("Env " + name + " already exists");
            System.exit(1);
        }
        Utils.createDirectories(path);
    }

    private void deleteEnv(String name) {
        var path = getEnvPath(name);
        if (path.toFile().isDirectory()) {
            Utils.clearDirectory(path);
        } else {
            System.err.println("Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private void setEnv(String name) throws IOException {
        var path = getEnvPath(name);
        if (path.toFile().isDirectory()) {
            Files.writeString(envFile, name);
            selectedEnv = path;
        } else {
            System.err.println("Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private void initSelectedEnv() {
        var envFile = this.envFile.toFile();
        if (envFile.isFile()) {
            var env = Utils.readLine(envFile);
            var path = getEnvPath(env);
            if (path.toFile().isDirectory()) {
                selectedEnv = path;
                return;
            }
        }
        selectedEnv = getEnvPath("default");
    }

    private Path getEnvPath(String name) {
        return home.resolve(name);
    }

    private void clear() {
        Utils.clearDirectory(home);
        System.out.println("Clear successfully");
    }

    private void changeHost(String host) {
        Utils.writeFile(getHostFile(), host);
        System.out.println("Host changed");
    }

    private String getHost() {
        var hostFile = new File(getHostFile());
        if (hostFile.exists())
            return Utils.readLine(hostFile);
        else
            return Constants.DEFAULT_HOST;
    }

    private void logout() {
        if (isLoggedIn()) {
            doLogout();
            System.out.println("Logged out successfully");
        } else {
            System.out.println("Not logged in");
        }
    }

    private void deploy(TypeClient typeClient, Path targetDir) {
        Utils.ensureDirectoryExists(selectedEnv);
        typeClient.deploy(getAppId(), targetDir.resolve("target.mva").toString());
    }

    private void secretDeploy(TypeClient typeClient, Path targetDir) {
        Utils.ensureDirectoryExists(selectedEnv);
        typeClient.secretDeploy(getAppId(), targetDir.resolve("target.mva").toString());
    }

    private List<ApplicationDTO> listApps() {
        var page = CompilerHttpUtils.get("/app", new TypeReference<Page<ApplicationDTO>>() {});
        System.out.println("applications:");
        for (ApplicationDTO app : page.items()) {
            if(app.id() > 2)
                System.out.printf("\t%s%n", app.name());
        }
        return page.items();
    }

    public long getAppId() {
        var appFile = new File(getAppFile());
        if (appFile.exists()) {
            if (appFile.isDirectory()) {
                System.err.println("Corrupted file structure. Use kiwi clear to reset");
                System.exit(1);
            }
            return Utils.readLong(appFile);
        } else
            return -1;
    }

    void initializeHttpClient() {
        CompilerHttpUtils.setHost(getHost());
        var tokenFile = new File(getTokenFile());
        if (tokenFile.exists()) {
            CompilerHttpUtils.setToken(Utils.readLine(tokenFile));
        }
    }

    private void doLogout() {
        if (isLoggedIn()) {
            CompilerHttpUtils.post("/logout", null, new TypeReference<Void>() {
            });
            deleteSessionFiles();
        }
    }

    private void deleteSessionFiles() {
        Utils.deleteFile(getAppFile());
        Utils.deleteFile(getTokenFile());
    }

    private void usage() {
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

    public static void main(String[] args) throws IOException {
        new Main(Path.of(".")).run(args);
    }

    void run(String[] args) throws IOException {
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
                case "app" -> System.out.println(getAppId());
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
                    var selected = Files.readString(envFile);
                    var home = this.home.toFile();
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
                case "gen-api" -> generateApi();
                case "deploy" -> {
                    ensureLoggedIn();
                    deploy();
                }
                case "secret-deploy" -> secretDeploy();
                default -> usage();
            }
        }
        catch (CompilationException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    void deploy() {
        if (build())
            deploy(new HttpTypeClient(), targetRoot);
    }

    @SneakyThrows
    void secretDeploy() {
        if (build())
            secretDeploy(new HttpTypeClient(), targetRoot);
    }

    boolean build() throws IOException {
        if (!ensureSourceAvailable())
            return false;
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0) {
            task.generate();
            return true;
        }
        else
            return false;
    }

    private boolean ensureSourceAvailable() {
        var sourceRoot = "src";
        var f = new File(sourceRoot);
        if (!f.exists() || !f.isDirectory()) {
            System.err.println("Source directory '" + sourceRoot + "' does not exist.");
            return false;
        }
        else
            return true;
    }


    public List<Path> listFilePathsRecursively(Path start) throws IOException {
        if (!Files.exists(start) || !Files.isDirectory(start)) {
            throw new IllegalArgumentException("Provided path is not an existing directory: " + start);
        }

        try (Stream<Path> stream = Files.walk(start)) {
            return stream
                    .filter(f -> Files.isRegularFile(f) && f.getFileName().toString().endsWith(".kiwi"))
                    .map(Path::toAbsolutePath) // Convert Path to absolute Path
                    .collect(Collectors.toList());
        }
    }

}
