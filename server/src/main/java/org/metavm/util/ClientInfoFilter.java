package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.server.Filter;
import org.metavm.server.HttpRequest;
import org.metavm.context.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class ClientInfoFilter implements Filter {

    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        ContextUtil.setClientId(request.getHeader(Headers.CLIENT_ID));
        ContextUtil.setRequestUri(request.getRequestURI());
        Long metaVersion = Utils.tryParseLong(request.getHeader(Headers.META_VERSION));
        if(metaVersion != null)
            ContextUtil.setMetaVersion(metaVersion);
        proceed.accept(request);
    }

    @Override
    public int order() {
        return 3;
    }
}
