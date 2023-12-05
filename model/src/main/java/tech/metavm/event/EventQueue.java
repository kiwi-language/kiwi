package tech.metavm.event;

import tech.metavm.event.rest.dto.AppEvent;
import tech.metavm.event.rest.dto.UserEvent;

public interface EventQueue {

    void publishUserEvent(UserEvent event);

    void publishAppEvent(AppEvent event);

}
