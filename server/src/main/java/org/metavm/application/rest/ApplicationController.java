package org.metavm.application.rest;

import org.metavm.application.ApplicationManager;
import org.metavm.application.rest.dto.*;
import org.metavm.common.Page;
import org.metavm.context.http.*;
import org.metavm.object.instance.core.Id;
import org.metavm.user.rest.dto.AppEvictRequest;
import org.metavm.user.rest.dto.AppMemberDTO;
import org.metavm.util.ContextUtil;

@Controller
@Mapping("/app")
public class ApplicationController {

    private final ApplicationManager applicationManager;

    public ApplicationController(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Get
    public Page<ApplicationDTO> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                             @RequestParam(value = "searchText", required = false) String searchText,
                                             @RequestParam(value = "newlyCreatedId", required = false) Long newlyCreatedId) {
        return applicationManager.list(page, pageSize, searchText, ContextUtil.getUserId(), newlyCreatedId);
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
