package org.metavm.util;

import org.metavm.event.rest.dto.AppEvent;
import org.metavm.event.rest.dto.UserEvent;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class Hooks {

    public static volatile Consumer<SearchSyncRequest> SEARCH_BULK;

    public static volatile Consumer<UserEvent> PUBLISH_USER_EVENT;

    public static volatile Consumer<AppEvent> PUBLISH_APP_EVENT;

    public static volatile LongConsumer CREATE_INDEX_REBUILD_TASK;

    public static volatile LongConsumer CREATE_INDEX_IF_NOT_EXISTS;

    public static volatile LongConsumer DROP_TABLES;

    public static volatile LongConsumer DROP_INDICES;

}
