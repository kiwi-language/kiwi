package org.metavm.common.controller;

import org.metavm.context.http.Controller;
import org.metavm.context.http.Get;
import org.metavm.context.http.Mapping;

@Controller
@Mapping("/ping")
public class PingController {

    @Get
    public void ping() {
    }

}
