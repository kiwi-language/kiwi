package tech.metavm.management.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.management.LabService;

@RestController
@RequestMapping("/lab")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @PostMapping
    public void test() {
        labService.test();
    }


}
