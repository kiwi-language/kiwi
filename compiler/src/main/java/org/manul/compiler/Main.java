package org.manul.compiler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsonk.Type;
import org.manul.application.rest.dto.ApplicationDTO;
import org.manul.common.ErrorCode;
import org.manul.common.Page;
import org.manul.compiler.apigen.ApiGenerator;
import org.manul.compiler.apigen.Templates;
import org.manul.compiler.util.CompilationException;
import org.manul.compiler.util.HttpUtil;
import org.manul.compiler.util.MockEnter;
import org.manul.compiler.util.RequestException;
import org.manul.user.rest.dto.LoginInfo;
import org.manul.util.Constants;
import org.manul.util.Utils;

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

    public static final String VERSION = "0.0.1";

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
        home = root.resolve(".manul");
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
        extractResourceDirectory(ManulEnv.getStdLibPath(), stdlibCache);
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
        doLogin();
        Utils.writeFile(getTokenFile(), HttpUtil.getToken());
    }

    private void doLogin() {
        var scanner = new Scanner(System.in);
        System.out.print("username: ");
        var name = scanner.nextLine();
        System.out.print("password: ");
        var password = scanner.nextLine();
        HttpUtil.login(name, password);
    }

    @SneakyThrows
    private void restart() {
        try {
            var pb = new ProcessBuilder("manul-restart");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (IOException e) {
            System.err.println("Error: the restart subcommand is only available when manul is installed using the installer");
        }
    }

    private ApplicationDTO getApp(String name) {
        return HttpUtil.get("/manul-system/app", ApplicationDTO.class, Map.of("name", name));
    }

    private void enter(String name) {
        enterApp(getApp(name).id());
    }

    @SneakyThrows
    private void enterApp(long appId) {
        var filePath = Path.of(getAppFile());
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, Long.toString(appId));
        if (HttpUtil.getToken() != null)
            Utils.writeFile(getTokenFile(), HttpUtil.getToken());
    }

    private long createApp(String name) {
        return HttpUtil.post("/manul-system/app", new ApplicationDTO(null, name, null), Long.class);
    }

    private void deleteApp(String name) {
        var app = getApp(name);
        HttpUtil.delete("/manul-system/app/" + app.id(), Void.class);
    }

    private void printSourceTag(String name) {
        var tag = HttpUtil.post("/manul-system/type/source-tag",
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
        return HttpUtil.get("/manul-system/auth/get-login-info", LoginInfo.class).isSuccessful();
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
            System.err.println("Error: Env " + name + " already exists");
            System.exit(1);
        }
        Utils.createDirectories(path);
    }

    private void deleteEnv(String name) {
        var path = getEnvPath(name);
        if (path.toFile().isDirectory()) {
            Utils.clearDirectory(path);
        } else {
            System.err.println("Error: Env " + name + " does not exist");
            System.exit(1);
        }
    }

    private void setEnv(String name) throws IOException {
        var path = getEnvPath(name);
        if (path.toFile().isDirectory()) {
            Files.writeString(envFile, name);
            selectedEnv = path;
        } else {
            System.err.println("Error: Env " + name + " does not exist");
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
        if (isLoggedIn())
            HttpUtil.logout();
        deleteSessionFiles();
    }

    private void deploy(TypeClient typeClient, Path targetDir, boolean printDeployStatus) {
        Utils.ensureDirectoryExists(selectedEnv);
        var deployId = typeClient.deploy(getAppId(), targetDir.resolve("target.mva").toString());
        System.out.println("Deployment ID: " + deployId);
        if (printDeployStatus)
            printDeployStatus(typeClient, getAppId(), deployId);
    }

    private void secretDeploy(TypeClient typeClient, Path targetDir, boolean printDeployStatus) {
        Utils.ensureDirectoryExists(selectedEnv);
        var deployId = typeClient.secretDeploy(getAppId(), targetDir.resolve("target.mva").toString());
        System.out.println("Deployment is running, ID: " + deployId);
        if (printDeployStatus)
            printDeployStatus(typeClient, getAppId(), deployId);
    }

    @SneakyThrows
    private void printDeployStatus(TypeClient typeClient, long appId, String deployId) {
        for (;;) {
            var status = typeClient.getDeployStatus(appId, deployId);
            System.out.print(CARRIAGE_RETURN + ANSI_ERASE_LINE + "Status: " + status);
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
        var page = (Page<ApplicationDTO>) HttpUtil.get("/manul-system/app/search", Type.from(Page.class, ApplicationDTO.class));
        for (ApplicationDTO app : page.items()) {
            if(app.id() > 2)
                System.out.printf("%s%n", app.name());
        }
        return page.items();
    }

    public long getAppId() {
        var appFile = new File(getAppFile());
        if (appFile.exists()) {
            if (appFile.isDirectory()) {
                System.err.println("Error: Corrupted env files");
                System.err.println("Run 'manul clear' to reset");
                System.exit(1);
            }
            return Utils.readLong(appFile);
        } else {
            System.err.println("Error: No application selected");
            System.err.println("Please run 'manul set-app' to select one");
            System.exit(1);
            return -1L;
        }
    }

    public void printAppName() {
        var appId = getAppId();
        var app = HttpUtil.get("/manul-system/app/" + appId, ApplicationDTO.class);
        System.out.println(app.name());
    }

    void initializeHttpClient() {
        HttpUtil.setHost(getHost());
        var tokenFile = new File(getTokenFile());
        if (tokenFile.exists()) {
            HttpUtil.setToken(Utils.readLine(tokenFile));
        }
    }

    private void deleteSessionFiles() {
        Utils.deleteFile(getAppFile());
        Utils.deleteFile(getTokenFile());
    }

    private void usage() {
        System.out.println("Usage: ");
        System.out.println("manul build");
        System.out.println("manul deploy");
        System.out.println("manul redeploy");
        System.out.println("manul gen-api");
        System.out.println("manul revert");
        System.out.println("manul clear");
        System.out.println("manul host <host>");
        System.out.println("manul login");
        System.out.println("manul logout");
        System.out.println("manul create-app <name>");
        System.out.println("manul set-app <name>");
        System.out.println("manul delete-app <name>");
        System.out.println("manul app");
        System.out.println("manul env");
        System.out.println("manul create-env <env>");
        System.out.println("manul set-env <env>");
        System.out.println("manul delete-env <env>");
        System.out.println("manul deploy-status <deploy-id>");
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
                case "set-app" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    enter(args[1]);
                }
                case "app" -> printAppName();
                case "new" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    newProject(args[1]);
                }
                case "create-app" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    createApp(args[1]);
                }
                case "delete-app" -> {
                    if (args.length < 2) {
                        usage();
                        return;
                    }
                    deleteApp(args[1]);
                }
                case "restart" -> restart();
                case "list-apps" -> listApps();
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
                case "deploy" -> deploy(parseOptions(args));
                case "secret-deploy" -> secretDeploy(parseOptions(args));
                case "deploy-status" -> {
                    if (args.length < 2)
                        throw new InvalidUsageException("Deploy ID is required");
                    deployStatus(args[1]);
                }
                case "--version", "-v" -> printVersion();
                default -> usage();
            }
        } catch (InvalidUsageException e) {
            System.err.printf("Error: %s%n", e.getMessage());
            usage();
        } catch (CompilationException | RequestException e) {
            System.err.printf("Error: %s%n", e.getMessage());
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
        NO_WAIT(false),

        ;

        private final boolean hasArg;

        OptionKind(boolean hasArg) {
            this.hasArg = hasArg;
        }
    }

    @SneakyThrows
    void deploy(List<Option> options) {
        var printDeployStatus = options.stream().noneMatch(opt -> opt.kind == OptionKind.NO_WAIT);
        if (build(options))
            deploy(new HttpTypeClient(), targetRoot, printDeployStatus);
    }

    private void printVersion() {
        System.out.println("Manul " + VERSION);
    }

    @SneakyThrows
    void secretDeploy(List<Option> options) {
        var printDeployStatus = options.stream().noneMatch(opt -> opt.kind == OptionKind.NO_WAIT);
        if (build(options))
            secretDeploy(new HttpTypeClient(), targetRoot, printDeployStatus);
    }

    @SneakyThrows
    private void newProject(String name) {
        var root = Path.of(name);
        if (Files.exists(root)) {
            System.err.println("Error: Directory " + name + " already exists");
            System.exit(1);
        }
        if (!isLoggedIn()) {
            doLogin();
        }
        var examplePath = root.resolve("src").resolve("example.mnl");
        Utils.writeFile(examplePath, Templates.EXAMPLE_MNL);
        try {
            // TODO investigate why serialize error occurs and removes the sleep
            Thread.sleep(500);
            var appId = createApp(name);
            var appFile = root.resolve(getAppFile());
            Utils.writeFile(appFile, Long.toString(appId));
            if (HttpUtil.getToken() != null) {
                var tokenFile = root.resolve(getTokenFile());
                Utils.writeFile(tokenFile, HttpUtil.getToken());
}
        } catch (RequestException e) {
            if (e.getErrorCode() == ErrorCode.CONFLICTING_APP_NAME) {
                System.err.println("Error: Application name " + name + " already exists");
                System.exit(1);
            }
            throw e;
        }
    }

    boolean build(List<Option> options) {
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
            System.err.println("Error: src directory does not exist");
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
                    .filter(f -> Files.isRegularFile(f) && f.getFileName().toString().endsWith(".mnl"))
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
