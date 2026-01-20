package org.manul.application.rest;

import org.manul.application.ApplicationManager;
import org.manul.application.rest.dto.*;
import org.manul.common.Page;
import org.manul.context.http.*;
import org.manul.object.instance.core.Id;
import org.manul.user.rest.dto.AppEvictRequest;
import org.manul.user.rest.dto.AppMemberDTO;
import org.manul.util.ContextUtil;

@Controller
@Mapping("/app")
public class ApplicationController {

    private final ApplicationManager applicationManager;

    public ApplicationController(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Get("/search")
    public Page<ApplicationDTO> search(@RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                             @RequestParam(value = "name", required = false) String name,
                                             @RequestParam(value = "newlyCreatedId", required = false) Long newlyCreatedId) {
        return applicationManager.search(page, pageSize, name, ContextUtil.getUserId(), newlyCreatedId);
    }

    @Get("/")
    public ApplicationDTO get(@RequestParam("name") String name) {
        return applicationManager.getByName(name);
    }

    @Post
    public Long save(@RequestBody ApplicationDTO appDTO) {
        if (appDTO.id() != null)
            applicationManager.ensureAppAdmin(appDTO.id(), ContextUtil.getUserId());
        else
            appDTO = new ApplicationDTO(null, appDTO.name(), ContextUtil.getUserId().toString());
        return applicationManager.save(appDTO);
    }

    @Get("/{id}")
    public ApplicationDTO get(@PathVariable("id") long id) {
        applicationManager.ensureAppAdmin(id, ContextUtil.getUserId());
        return applicationManager.get(id);
    }

    @Post("/{id}/generate-secret")
    public GenerateSecretResponse generateSecret(@PathVariable("id") long id, @RequestBody GenerateSecretRequest request) {
        applicationManager.ensureAppAdmin(id, ContextUtil.getUserId());
        return new GenerateSecretResponse(applicationManager.generateSecret(id, request.verificationCode()));
    }

    @Delete("/{id}")
    public void delete(@PathVariable("id") long id) {
        applicationManager.ensureAppOwner(id, ContextUtil.getUserId());
        applicationManager.delete(id);
    }

    @Post("/evict")
    public void evict(@RequestBody AppEvictRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.evict(request);
    }

    @Post("/invitation/{id}/accept")
    public void accept(@PathVariable("id") String id) {
        applicationManager.acceptInvitation(id);
    }

    @Post("/invite")
    public void invite(@RequestBody AppInvitationRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.invite(request);
    }

    @Get("/invitation/{id}")
    public AppInvitationDTO getInvitation(@PathVariable("id") String id) {
        return applicationManager.getInvitation(id);
    }

    @Post("/query-members")
    public Page<AppMemberDTO> queryAppMembers(@RequestBody AppMemberQuery query) {
        return applicationManager.queryMembers(query);
    }

    @Post("/query-invitees")
    public Page<InviteeDTO> queryAppMembers(@RequestBody InviteeQuery query) {
        return applicationManager.queryInvitees(query);
    }

    @Post("/promote")
    public void promote(@RequestBody PromoteRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.promote(request.appId(), Id.parse(request.userId()));
    }

    @Post("/demote")
    public void demote(@RequestBody DemoteRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.demote(request.appId(), Id.parse(request.userId()));
    }

}
