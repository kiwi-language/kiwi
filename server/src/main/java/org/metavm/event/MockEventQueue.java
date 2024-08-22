package org.metavm.event;

import org.metavm.event.rest.dto.AppEvent;
import org.metavm.event.rest.dto.UserEvent;
import org.metavm.util.Hooks;

public class MockEventQueue implements EventQueue {

    public MockEventQueue() {
        Hooks.PUBLISH_USER_EVENT = this::publishUserEvent;
        Hooks.PUBLISH_APP_EVENT = this::publishAppEvent;
    }

    @Override
    public void publishUserEvent(UserEvent event) {

    }

    @Override
    public void publishAppEvent(AppEvent event) {

    }
}
