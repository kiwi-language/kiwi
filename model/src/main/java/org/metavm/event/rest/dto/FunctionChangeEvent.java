package org.metavm.event.rest.dto;

import lombok.Getter;
import org.jsonk.Json;
import org.metavm.event.AppEventKind;

import javax.annotation.Nullable;
import java.util.List;

@Json
public class FunctionChangeEvent extends AppEvent {

    @Getter
    private final long version;
    @Getter
    private final List<String> functionIds;
    @Nullable
    private final String triggerClientId;

    public FunctionChangeEvent(long appId, long version, List<String> functionIds, @Nullable String triggerClientId) {
        super(appId, AppEventKind.FUNCTION_CHANGE.code());
        this.functionIds = functionIds;
        this.version = version;
        this.triggerClientId = triggerClientId;
    }

    @Nullable
    public String getTriggerClientId() {
        return triggerClientId;
    }
}
