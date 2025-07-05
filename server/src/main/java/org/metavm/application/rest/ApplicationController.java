package org.metavm.application.rest;

import org.metavm.object.instance.core.Id;
import org.metavm.util.ContextUtil;
import org.springframework.web.bind.annotation.*;
import org.metavm.application.ApplicationManager;
import org.metavm.application.rest.dto.*;
import org.metavm.common.Page;
import org.metavm.common.Result;
import org.metavm.user.rest.dto.AppEvictRequest;
import org.metavm.user.rest.dto.AppMemberDTO;

@RestController
@RequestMapping("/app")
public class ApplicationController {

    private final ApplicationManager applicationManager;

    public ApplicationController(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @GetMapping
    public Result<Page<ApplicationDTO>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                             @RequestParam(value = "searchText", required = false) String searchText,
                                             @RequestParam(value = "newlyCreatedId", required = false) Long newlyCreatedId) {
        return Result.success(applicationManager.list(page, pageSize, searchText, ContextUtil.getUserId(), newlyCreatedId));
    }

    @PostMapping
    public Result<Long> save(@RequestBody ApplicationDTO appDTO) {
        if (appDTO.id() != null)
            applicationManager.ensureAppAdmin(appDTO.id(), ContextUtil.getUserId());
        else
            appDTO = new ApplicationDTO(null, appDTO.name(), ContextUtil.getUserId().toString());
        return Result.success(applicationManager.save(appDTO));
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<ApplicationDTO> get(@PathVariable("id") long id) {
        applicationManager.ensureAppAdmin(id, ContextUtil.getUserId());
        return Result.success(applicationManager.get(id));
    }

    @PostMapping("/{id:[0-9]+}/generate-secret")
    public Result<GenerateSecretResponse> generateSecret(@PathVariable("id") long id, @RequestBody GenerateSecretRequest request) {
        applicationManager.ensureAppAdmin(id, ContextUtil.getUserId());
        return Result.success(new GenerateSecretResponse(applicationManager.generateSecret(id, request.verificationCode())));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        applicationManager.ensureAppOwner(id, ContextUtil.getUserId());
        applicationManager.delete(id);
        return Result.voidSuccess();
    }

    @PostMapping("/evict")
    public Result<Void> evict(@RequestBody AppEvictRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.evict(request);
        return Result.voidSuccess();
    }

    @PostMapping("/invitation/{id}/accept")
    public Result<Void> accept(@PathVariable("id") String id) {
        applicationManager.acceptInvitation(id);
        return Result.voidSuccess();
    }

    @PostMapping("/invite")
    public Result<Void> invite(@RequestBody AppInvitationRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.invite(request);
        return Result.voidSuccess();
    }

    @GetMapping("/invitation/{id}")
    public Result<AppInvitationDTO> getInvitation(@PathVariable("id") String id) {
        return Result.success(applicationManager.getInvitation(id));
    }

    @PostMapping("/query-members")
    public Result<Page<AppMemberDTO>> queryAppMembers(@RequestBody AppMemberQuery query) {
        return Result.success(applicationManager.queryMembers(query));
    }

    @PostMapping("/query-invitees")
    public Result<Page<InviteeDTO>> queryAppMembers(@RequestBody InviteeQuery query) {
        return Result.success(applicationManager.queryInvitees(query));
    }

    @PostMapping("/promote")
    public Result<Void> promote(@RequestBody PromoteRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.promote(request.appId(), Id.parse(request.userId()));
        return Result.voidSuccess();
    }

    @PostMapping("/demote")
    public Result<Void> demote(@RequestBody DemoteRequest request) {
        applicationManager.ensureAppAdmin(request.appId(), ContextUtil.getUserId());
        applicationManager.demote(request.appId(), Id.parse(request.userId()));
        return Result.voidSuccess();
    }

}
