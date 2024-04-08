import javax.annotation.Nullable;

public class LabComponentMaterial {

    private final @Nullable LabBOM version;

    public LabComponentMaterial(@Nullable LabBOM version) {
        this.version = version;
    }

    public @Nullable LabBOM getVersion() {
        return version;
    }
}
