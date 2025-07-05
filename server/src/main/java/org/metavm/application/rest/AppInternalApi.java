package org.metavm.application.rest;

import org.metavm.application.ApplicationManager;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal-api/app")
public class AppInternalApi {

    private final ApplicationManager applicationManager;

    public AppInternalApi(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @PostMapping("/save")
    public long save(@RequestBody ApplicationDTO app) {
        return applicationManager.save(app);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") long id) {
        applicationManager.delete(id);
    }


}
