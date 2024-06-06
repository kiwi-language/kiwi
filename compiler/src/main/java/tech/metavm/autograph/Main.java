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
import java.util.Scanner;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String LOCAL_HOME = ".metavm";

    public static final String TOKEN_FILE = LOCAL_HOME + File.separator + "token";

    public static final String HOST_FILE = LOCAL_HOME + File.separator + "host";

    public static final String APP_FILE = LOCAL_HOME + File.separator + "app";

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
        if(!isLoggedIn())
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
        NncUtils.writeFile(APP_FILE, Long.toString(appId));
        NncUtils.writeFile(TOKEN_FILE, CompilerHttpUtils.getToken());
    }

    private static boolean isLoggedIn() {
        return CompilerHttpUtils.get("/get-login-info", new TypeReference<LoginInfo>() {}).isSuccessful();
    }

    private static void ensureHomeCreated() {
        try {
            Files.createDirectories(Paths.get(LOCAL_HOME));
        } catch (IOException e) {
            System.err.println("Fail to create home directory: " + LOCAL_HOME);
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
//        var authFile = new File(TOKEN_FILE);
//        var appFile = new File(APP_FILE);
        if (isLoggedIn()) {
            //noinspection ResultOfMethodCallIgnored
//            authFile.delete();
            doLogout();
            System.out.println("Logged out successfully");
        } else {
            System.out.println("Not logged in");
        }
    }

    private static void initializeHttpClient() {
        var tokenFile = new File(TOKEN_FILE);
        var appFile = new File(APP_FILE);
        if(tokenFile.exists() && appFile.exists()) {
            var appId = NncUtils.readLong(appFile);
            CompilerHttpUtils.setAppId(appId);
            CompilerHttpUtils.setToken(appId, NncUtils.readLine(tokenFile));
        }
    }

    private static void doLogout() {
        if(isLoggedIn())
            CompilerHttpUtils.post("/logout", null, new TypeReference<Void>() {});
    }

    private static void usage() {
        System.out.println("Usage: ");
        System.out.println("metavm deploy");
        System.out.println("metavm clear");
        System.out.println("metavm host <host>");
        System.out.println("metavm login");
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
            case "deploy" -> {
                var sourceRoot = args.length > 1 ? args[1] : (isMavenProject() ? "src/main/java" : "src");
                var f = new File(sourceRoot);
                if (!f.exists() || !f.isDirectory()) {
                    System.err.println("Source directory '" + sourceRoot + "' does not exist.");
                    return;
                }
                var typeClient = new HttpTypeClient();
                ensureLoggedIn();
                CompilerHttpUtils.host = getHost();
                logger.info("Host: " + CompilerHttpUtils.host);
                logger.info("Application ID: {}", NncUtils.readLong(APP_FILE));
                var main = new Main(LOCAL_HOME,
                        sourceRoot,
                        NncUtils.readLong(APP_FILE),
                        NncUtils.readLine(TOKEN_FILE),
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
