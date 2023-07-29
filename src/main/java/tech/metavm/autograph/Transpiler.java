package tech.metavm.autograph;

public class Transpiler {

    private final IrCoreApplicationEnvironment applicationEnvironment;
    private final IrCoreProjectEnvironment projectEnvironment;

    private final String rootDir;

    public Transpiler(String rootDir) {
        applicationEnvironment = new IrCoreApplicationEnvironment(() -> {});
        projectEnvironment = new IrCoreProjectEnvironment(() -> {}, applicationEnvironment);
        this.rootDir = rootDir;
    }

}
