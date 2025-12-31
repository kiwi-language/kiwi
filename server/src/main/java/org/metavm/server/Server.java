package org.metavm.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsonk.Jsonk;
import org.metavm.common.ErrorCode;
import org.metavm.common.ErrorResponse;
import org.metavm.context.DisposableBean;
import org.metavm.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
public class Server implements DisposableBean {

    private final HttpServer httpServer;

    @SneakyThrows
    public Server(int port, List<Controller> controllers, List<Filter> filters) {
        filters = filters.stream().sorted(Comparator.comparingInt(Filter::order).reversed()).toList();
        httpServer = HttpServer.create(new InetSocketAddress(port), 128);
        for (var controller : controllers) {
            log.info("Creating context for controller: " + controller.getClass().getSimpleName());
            httpServer.createContext(controller.getPath(), buildHandler(controller, filters));
        }
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();
    }

    @Override
    public void destroy() {
        httpServer.stop(0);
    }

    private static HttpHandler buildHandler(Controller controller, List<Filter> filters) {
        var routes = Utils.map(controller.getRoutes(), c -> createRoute(c, filters));
        return exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,X-App-ID,X-Refresh-Policy,Authorization");
                exchange.sendResponseHeaders(HttpStatus.SC_NO_CONTENT, -1);
                return;
            }
            var subPath = exchange.getRequestURI().getPath().substring(controller.getPath().length());
            for (var route : routes) {
                if (!exchange.getRequestMethod().equals(route.getMethod().name()))
                    continue;
                var m = route.match(subPath, exchange.getRequestURI().getQuery());
                if (m != null) {
                    var req = new HttpRequestImpl(
                            HttpMethod.valueOf(exchange.getRequestMethod()),
                            exchange.getRequestURI().getPath(),
                            m.pathVariables(),
                            m.queryParameters(),
                            exchange.getRequestHeaders(),
                            exchange.getResponseHeaders(),
                            exchange.getRemoteAddress().toString(),
                            exchange.getRequestBody()
                    );
                    try {
                        route.process(req);
                        var resp = req.getResponseBody();
                        if (resp.length > 0) {
                            exchange.sendResponseHeaders(req.getStatus(), resp.length);
                            exchange.getResponseBody().write(resp);
                        } else {
                            var status = req.getStatus() == HttpStatus.SC_OK ? HttpStatus.SC_NO_CONTENT : req.getStatus();
                            exchange.sendResponseHeaders(status, -1);
                        }
                    } catch(BadRequestException e) {
                        exchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, -1);
                    } catch (Exception e) {
                        var resp = toJsonBytes(ErrorResponse.create(ErrorCode.UNKNOWN));
                        log.error("Failed handle request " + req.getRequestURI(), e);
                        exchange.sendResponseHeaders(HttpStatus.SC_INTERNAL_SERVER_ERROR, resp.length);
                        exchange.getResponseBody().write(resp);
                    }
                    exchange.close();
                    break;
                }
            }
            throw new IOException("No route matched for path: " + subPath);
        };
    }

    @SneakyThrows
    public static byte[] toJsonBytes(Object o) {
        var bout = new ByteArrayOutputStream();
        try (var w = new OutputStreamWriter(bout)) {
            Jsonk.toJson(o, w);
        }
        return bout.toByteArray();
    }

    private static Route createRoute(RouteConfig config, List<Filter> filters) {
        var h = config.handle();
        for (Filter filter : filters) {
            var l = h;
            h = r -> filter.filter(r, l);
        }
        return new Route(
                config.method(),
                config.path(),
                h
        );
    }

}
