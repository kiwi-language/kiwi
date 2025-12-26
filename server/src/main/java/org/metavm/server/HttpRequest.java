package org.metavm.server;

import lombok.SneakyThrows;
import org.jsonk.Jsonk;
import org.jsonk.Type;
import org.metavm.user.Tokens;
import org.metavm.util.ContentTypes;
import org.metavm.util.Headers;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface HttpRequest {

    HttpMethod getMethod();

    String getRequestURI();

    String getHeader(String name);

    Map<String, List<String>> getHeaders();

    default String getClientIP() {
        String ipAddress = getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = getRemoteAddr();
        }
        return ipAddress;
    }

    default String getToken() {
        return Tokens.getToken(this);
    }

    void forEachHeader(BiConsumer<String, String> action);

    void addHeader(String name, String value);

    String getQueryParameter(String name);

    @Nullable String getOptionalQueryParameter(String name);

    default String getQueryParameter(String name, String defaultValue) {
        var v = getOptionalQueryParameter(name);
        return v != null ? v : defaultValue;
    }

    String getPathVariable(String name);

    InputStream getBody();

    default <R> R readRequestBody(Class<R> cls) {
        var contentLen = getContentLength();
        if (contentLen == 0)
            return null;
        return Jsonk.fromJson(new InputStreamReader(getBody()), cls);
    }

    default Object readRequestBody(Type type) {
        var contentLen = getContentLength();
        if (contentLen == 0)
            return null;
        return Jsonk.fromJson(new InputStreamReader(getBody()), type);
    }

    default long getContentLength() {
        var contentLen = getHeader("Content-Length");
        return contentLen != null ? Long.parseLong(contentLen) : 0;
    }

    OutputStream getOut();

    String getRemoteAddr();

    void setStatus(int status);

    int getStatus();

    @SneakyThrows
    default void writeBody(Object o) {
        Objects.requireNonNull(o);
        if (o instanceof String || o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long
                || o instanceof Float || o instanceof Double || o instanceof Boolean || o instanceof Character) {
            addHeader(Headers.CONTENT_TYPE, ContentTypes.text);
            getOut().write(o.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            addHeader(Headers.CONTENT_TYPE, ContentTypes.json);
            var w = new OutputStreamWriter(getOut());
            Jsonk.toJson(o, w);
            w.flush();
        }
    }

}
