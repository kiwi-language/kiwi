package org.metavm.server;

import com.sun.net.httpserver.Headers;
import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpRequestImpl implements HttpRequest {

    private final HttpMethod method;
    private final String requestURI;
    private final Map<String, String> pathVariables;
    private final Map<String, List<String>> queryParameters;
    private final Headers requestHeaders;
    private final Headers responseHeaders;
    private final String remoteAddr;
    private final InputStream requestBody;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private int status = HttpStatus.SC_OK;

    public HttpRequestImpl(
            HttpMethod method,
            String requestURI,
            Map<String, String> pathVariables,
           Map<String, List<String>> queryParameters,
           Headers requestHeaders,
           Headers responseHeaders,
           String remoteAddr,
            InputStream requestBody
    ) {
        this.method = method;
        this.requestURI = requestURI;
        this.pathVariables = pathVariables;
        this.queryParameters = queryParameters;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
        this.remoteAddr = remoteAddr;
        this.requestBody = requestBody;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getHeader(String name) {
        return requestHeaders.getFirst(name);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return requestHeaders;
    }

    @Override
    public void forEachHeader(BiConsumer<String, String> action) {
        responseHeaders.forEach((name, values) -> action.accept(name, values.getFirst()));
    }

    @Override
    public void addHeader(String name, String value) {
        responseHeaders.set(name, value);
    }

    @Override
    public String getQueryParameter(String name) {
        var params = queryParameters.get(name);
        return params != null ? params.getFirst() : null;
    }

    @Override
    public String getPathVariable(String name) {
        return pathVariables.get(name);
    }

    @Override
    public InputStream getBody() {
        return requestBody;
    }

    @Override
    public OutputStream getOut() {
        return out;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public byte[] getResponseBody() {
        return out.toByteArray();
    }

}
