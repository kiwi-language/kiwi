package tech.metavm.event.rest.dto;

import tech.metavm.event.AppEventKind;

import javax.annotation.Nullable;
import java.util.List;

public class FunctionChangeEvent extends AppEvent {

    private final long version;
    private final List<String> functionIds;
    @Nullable
    private final String triggerClientId;

    public FunctionChangeEvent(String appId, long version, List<String> functionIds, @Nullable String triggerClientId) {
        super(appId, AppEventKind.FUNCTION_CHANGE.code());
        this.functionIds = functionIds;
        this.version = version;
        this.triggerClientId = triggerClientId;
    }

    public long getVersion() {
        return version;
    }

    public List<String> getFunctionIds() {
        return functionIds;
    }

    @Nullable
    public String getTriggerClientId() {
        return triggerClientId;
    }
}
