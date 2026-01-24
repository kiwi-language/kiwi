package org.manul.common.controller;

import org.manul.context.http.Controller;
import org.manul.context.http.Get;
import org.manul.context.http.Mapping;

@Controller
@Mapping("/manul-system/ping")
public class PingController {

    @Get
    public void ping() {
    }

}
