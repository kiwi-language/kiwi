package tech.metavm.event;

import tech.metavm.event.rest.dto.AppEvent;
import tech.metavm.event.rest.dto.UserEvent;

public class MockEventQueue implements EventQueue {
    @Override
    public void publishUserEvent(UserEvent event) {

    }

    @Override
    public void publishAppEvent(AppEvent event) {

    }
}
