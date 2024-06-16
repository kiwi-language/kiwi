package org.metavm.event;

import org.metavm.event.EventQueue;
import org.metavm.event.rest.dto.AppEvent;
import org.metavm.event.rest.dto.UserEvent;

public class MockEventQueue implements EventQueue {
    @Override
    public void publishUserEvent(UserEvent event) {

    }

    @Override
    public void publishAppEvent(AppEvent event) {

    }
}
