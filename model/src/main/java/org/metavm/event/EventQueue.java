package org.metavm.event;

import org.metavm.event.rest.dto.AppEvent;
import org.metavm.event.rest.dto.UserEvent;

public interface EventQueue {

    void publishUserEvent(UserEvent event);

    void publishAppEvent(AppEvent event);

}
