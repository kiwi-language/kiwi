package org.metavm.manufacturing.user;

import org.metavm.api.Index;
import org.metavm.api.lang.Lang;

import java.util.Date;

public class Session {

    public static final long TTL = 7 * 24 * 60 * 60;

    public static final Index<String, Session> tokenIndex = new Index<>(true, s -> s.token);

    private final User user;
    private final String token;
    private boolean closed;
    private Date expiryTime;

    public Session(User user) {
        this.user = user;
        this.token = Lang.getId(user) + "-" + Lang.secureRandom(32);
        expiryTime = new Date(System.currentTimeMillis() + TTL);
    }

    public void close() {
        this.closed = true;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public boolean isActive() {
        return !closed && System.currentTimeMillis() < expiryTime.getTime();
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

}
