package org.metavm.application.rest;

import org.metavm.application.ApplicationManager;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.context.http.*;

@Controller
@Mapping("/internal-api/app")
public class AppInternalApi {

    private final ApplicationManager applicationManager;

    public AppInternalApi(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Post("/save")
    public long save(@RequestBody ApplicationDTO app) {
        return applicationManager.save(app);
    }

    @Delete("/delete/{id}")
    public void delete(@PathVariable("id") long id) {
        applicationManager.delete(id);
    }


}
