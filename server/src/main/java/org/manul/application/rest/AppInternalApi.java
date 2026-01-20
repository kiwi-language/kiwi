package org.manul.application.rest;

import org.manul.application.ApplicationManager;
import org.manul.application.rest.dto.ApplicationDTO;
import org.manul.application.rest.dto.UpdateAppNameRequest;
import org.manul.context.http.*;

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

    @Post("/update-name")
    public long updateName(@RequestBody UpdateAppNameRequest req) {
        return applicationManager.updateName(req);
    }

    @Delete("/delete/{id}")
    public void delete(@PathVariable("id") long id) {
        applicationManager.delete(id);
    }


}
