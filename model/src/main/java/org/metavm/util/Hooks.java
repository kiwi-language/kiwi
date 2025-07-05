package org.metavm.util;

import org.metavm.event.rest.dto.AppEvent;
import org.metavm.event.rest.dto.UserEvent;

import java.util.function.Consumer;

public class Hooks {

    public static volatile Consumer<SearchSyncRequest> SEARCH_BULK;

    public static volatile Consumer<UserEvent> PUBLISH_USER_EVENT;

    public static volatile Consumer<AppEvent> PUBLISH_APP_EVENT;
}
