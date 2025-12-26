package org.metavm.compiler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsonk.Type;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.Page;
import org.metavm.compiler.apigen.ApiGenerator;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.CompilerHttpUtils;
import org.metavm.compiler.util.MockEnter;
import org.metavm.user.rest.dto.LoginInfo;
import org.metavm.util.Constants;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Main {

    private static final String ANSI_ERASE_LINE = "\u001B[2K";

    private static final String CARRIAGE_RETURN = "\r";

    public final Path home;
    private final Path envFile;
    private Path selectedEnv;
    private final Path sourceRoot;
    private final Path targetRoot;

    public Main(Path root) {
        sourceRoot = root.resolve("src");
        this.targetRoot = root.resolve("target");
        home = root.resolve(".metavm");
        envFile = home.resolve(".env");
        selectedEnv = getEnvPath("default");
    }

    boolean generateApi(List<Option> options) {
        if (!ensureSourceAvailable())
            return false;
        var task = createTask(options);
        task.parse();
        MockEnter.enterStandard(task.getProject());
        task.analyze();
        if (task.getErrorCount() == 0) {
            var versionOp = Utils.find(options, o -> o.kind == OptionKind.VERSION);
            var v = versionOp != null ? Long.parseLong(Objects.requireNonNull(versionOp.arg)) : ApiGenerator.CURRENT_VERSION;
            task.generateApi(v);
            return true;
        } else
            return false;
    }

    void revert() {
        Utils.ensureDirectoryExists(selectedEnv);
        var typeClient = new HttpTypeClient();
        typeClient.revert(getAppId());
    }

    private Path findStdLibRoot() {
        var stdlibCache = home.resolve("stdlib");
        extractResourceDirectory(KiwiEnv.getStdLibPath(), stdlibCache);
        return stdlibCache;
    }

    @SneakyThrows
    private void extractResourceDirectory(Path resourceDir, Path targetDir) {
        if (targetDir.resolve(".extracted").toFile().exists())
            return;
        if (Files.exists(targetDir))
            Utils.clearDirectory(targetDir);
        try (var stream = Files.walk(resourceDir)) {
            stream.forEach(sourcePath -> {
                var destPath = targetDir.resolve(resourceDir.relativize(sourcePath).toString());
                try {
                    if (Files.isDirectory(sourcePath))
                        Files.createDirectories(destPath);
                    else
                        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to extract resource: " + sourcePath, e);
                }
            });
        }
        Files.createFile(targetDir.resolve(".extracted"));
    }

    @SneakyThrows
    private CompilationTask createTask(List<Option> options) {
        var senseLint = false;
        for (Option option : options) {
            if (option.kind == OptionKind.SENSE_LINT) {
                senseLint = true;
                break;
            }
        }
        var sourcePaths = new ArrayList<>(listFilePathsRecursively(sourceRoot));
        sourcePaths.addAll(listFilePathsRecursively(findStdLibRoot().resolve("src")));
        return CompilationTaskBuilder.newBuilder(sourcePaths, targetRoot)
                .withSenseLint(senseLint)
                .build();
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

    private void doLogin(String username, String password) {
        CompilerHttpUtils.login(username, password);
    }

    private void enter(String appName) {
        //noinspection unchecked
        var apps = ((Page<ApplicationDTO>) CompilerHttpUtils.get("/app", Type.from(Page.class, ApplicationDTO.class))).items();
        var appId = Utils.findRequired(apps, app -> app.name().equals(appName),
                () -> "Application '" + appName + "' does not exist").id();
        enterApp(appId);
    }

    private void enterApp(long appId) {
        Utils.writeFile(getAppFile(), Long.toString(appId));
        Utils.writeFile(getTokenFile(), CompilerHttpUtils.getToken());
    }

    private void createApp(String name) {
        var appId = CompilerHttpUtils.post("/app", new ApplicationDTO(null, name, null), Long.class);
        System.out.println("application ID: " + appId);
    }

    private void printSourceTag(String name) {
        var tag = CompilerHttpUtils.post("/type/source-tag",
                Map.of("appId", getAppId(), "name", name), Integer.class);
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
        return CompilerHttpUtils.get("/auth/get-login-info", LoginInfo.class).isSuccessful();
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

    private void deploy(TypeClient typeClient, Path targetDir, boolean printDeployStatus) {
        Utils.ensureDirectoryExists(selectedEnv);
        var deployId = typeClient.deploy(getAppId(), targetDir.resolve("target.mva").toString());
        System.out.println("Deploy ID: " + deployId);
        if (printDeployStatus)
            printDeployStatus(typeClient, getAppId(), deployId);
    }

    private void secretDeploy(TypeClient typeClient, Path targetDir, boolean printDeployStatus) {
        Utils.ensureDirectoryExists(selectedEnv);
        var deployId = typeClient.secretDeploy(getAppId(), targetDir.resolve("target.mva").toString());
        System.out.println("Deploy is running, ID: " + deployId);
        if (printDeployStatus)
            printDeployStatus(typeClient, getAppId(), deployId);
    }

    @SneakyThrows
    private void printDeployStatus(TypeClient typeClient, long appId, String deployId) {
        for (;;) {
            var status = typeClient.getDeployStatus(appId, deployId);
            System.out.print(CARRIAGE_RETURN + ANSI_ERASE_LINE + "Deploy status: " + status);
            System.out.flush();
            if (status.equals("COMPLETED") || status.equals("ABORTED")) {
                System.out.println();
                break;
            }
            Thread.sleep(1000L);
        }
    }

    private void deployStatus(String deployId) {
        printDeployStatus(new HttpTypeClient(), getAppId(), deployId);
    }

    private List<ApplicationDTO> listApps() {
        //noinspection unchecked
        var page = (Page<ApplicationDTO>) CompilerHttpUtils.get("/app", Type.from(Page.class, ApplicationDTO.class));
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
            CompilerHttpUtils.logout();
            deleteSessionFiles();
        }
    }

    private void deleteSessionFiles() {
        Utils.deleteFile(getAppFile());
        Utils.deleteFile(getTokenFile());
    }

    private void usage() {
        System.out.println("Usage: ");
        System.out.println("kiwi build");
        System.out.println("kiwi deploy");
        System.out.println("kiwi redeploy");
        System.out.println("kiwi gen-api");
        System.out.println("kiwi revert");
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
        System.out.println("kiwi deploy-status <deploy-id>");
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
                case "build" -> build(parseOptions(args));
                case "gen-api" -> generateApi(parseOptions(args));
                case "revert" -> revert();
                case "deploy" -> {
                    ensureLoggedIn();
                    deploy(parseOptions(args));
                }
                case "secret-deploy" -> secretDeploy(parseOptions(args));
                case "deploy-status" -> {
                    if (args.length < 2)
                        throw new InvalidUsageException("Deploy ID is required");
                    deployStatus(args[1]);
                }
                default -> usage();
            }
        }
        catch (InvalidUsageException e) {
            System.err.println("Error: " + e.getMessage());
            usage();
        } catch (CompilationException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Option> parseOptions(String[] args) {
        var options = new ArrayList<Option>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String optionName = arg.substring(2);
                OptionKind kind;
                try {
                    kind = OptionKind.valueOf(optionName.toUpperCase().replace('-', '_'));
                } catch (IllegalArgumentException e) {
                    throw new InvalidUsageException("Unknown option: " + arg);
                }
                String optionArg = null;
                if (kind.hasArg) {
                    if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        optionArg = args[++i];
                    } else {
                        throw new InvalidUsageException("Option '" + arg + "' requires an argument");
                    }
                }
                options.add(new Option(kind, optionArg));
            } else {
                throw new InvalidUsageException("Unknown option: " + arg);
            }
        }
        return options;
    }

    record Option(OptionKind kind, @Nullable String arg) {
    }

    enum OptionKind {

        SENSE_LINT(false),
        VERSION(true),
        STATUS(false),

        ;

        private final boolean hasArg;

        OptionKind(boolean hasArg) {
            this.hasArg = hasArg;
        }
    }

    @SneakyThrows
    void deploy(List<Option> options) {
        var printDeployStatus = options.stream().anyMatch(opt -> opt.kind == OptionKind.STATUS);
        if (build(options))
            deploy(new HttpTypeClient(), targetRoot, printDeployStatus);
    }

    @SneakyThrows
    void secretDeploy(List<Option> options) {
        var printDeployStatus = options.stream().anyMatch(opt -> opt.kind == OptionKind.STATUS);
        if (build(options))
            secretDeploy(new HttpTypeClient(), targetRoot, printDeployStatus);
    }

    boolean build(List<Option> options) throws IOException {
        if (!ensureSourceAvailable())
            return false;
        var task = createTask(options);
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

    private static class InvalidUsageException extends RuntimeException {
        public InvalidUsageException(String message) {
            super(message);
        }
    }

}
