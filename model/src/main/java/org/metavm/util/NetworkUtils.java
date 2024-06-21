package org.metavm.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {

    public static final String localIP = getLocalIP();

    private static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            throw new InternalException("Failed to get local IP", e);
        }
    }

}
