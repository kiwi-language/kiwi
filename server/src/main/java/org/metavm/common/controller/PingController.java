package org.metavm.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.metavm.common.Result;

@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    public Result<Void> ping() {
        return Result.voidSuccess();
    }

}
