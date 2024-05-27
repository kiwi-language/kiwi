package tech.metavm.object.type.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.common.Result;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.object.version.VersionManager;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/type")
public class TypeController {

    private final TypeManager typeManager;

    private final VersionManager versionManager;

    public TypeController(TypeManager typeManager, VersionManager versionManager) {
        this.typeManager = typeManager;
        this.versionManager = versionManager;
    }

    @PostMapping("/query")
    public Result<Page<TypeDTO>> query(@RequestBody TypeQuery request) {
        return Result.success(typeManager.query(request));
    }

    @PostMapping("/query-trees")
    public Result<TreeResponse> queryTrees(@RequestBody TypeTreeQuery query) {
        return Result.success(typeManager.queryTrees(query));
    }

    @PostMapping("/get-by-range")
    public Result<GetTypesResponse> getByRange(@RequestBody GetByRangeRequest request) {
        return Result.success(typeManager.getByRange(request));
    }

    @GetMapping("/load-all-metadata")
    public Result<LoadAllMetadataResponse> loadAllMetadata() {
        return Result.success(versionManager.loadAllMetadata());
    }

    @GetMapping("/{id}/descendants")
    public Result<GetTypesResponse> getDescendants(@PathVariable("id") String id) {
        return Result.success(typeManager.getDescendants(id));
    }

    @PostMapping("/get")
    public Result<GetTypeResponse> get(@RequestBody GetTypeRequest request) {
        GetTypeResponse resp = typeManager.getType(request);
        if (resp == null) {
            return Result.failure(ErrorCode.RECORD_NOT_FOUND);
        }
        return Result.success(resp);
    }

    @PostMapping("/get-by-code")
    public Result<GetTypeResponse> getByCode(@RequestBody GetTypeByCodeRequest request) {
        return Result.success(typeManager.getTypeByCode(request.getCode()));
    }

    @PostMapping("/batch-get")
    public Result<GetTypesResponse> batchGet(@RequestBody GetTypesRequest request) {
        return Result.success(
                typeManager.batchGetTypes(request)
        );
    }

    @GetMapping("/{id}/creating-fields")
    public Result<List<CreatingFieldDTO>> getCreatingFields(@PathVariable("id") String id) {
        return Result.success(typeManager.getCreatingFields(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody TypeDTO typeDTO) {
        return Result.success(typeManager.saveType(typeDTO).id());
    }

    @PostMapping("/batch")
    public Result<List<String>> batchSave(@RequestBody BatchSaveRequest request) {
        return Result.success(typeManager.batchSave(request));
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchRemove(@RequestBody List<String> typeIds) {
        typeManager.batchRemove(typeIds);
        return Result.voidSuccess();
    }

    @PostMapping("/enum-constant")
    public Result<String> saveEnumConstant(@RequestBody InstanceDTO instanceDTO) {
        return Result.success(typeManager.saveEnumConstant(instanceDTO));
    }

    @DeleteMapping("/enum-constant/{id}")
    public Result<Void> deleteEnumConstant(@PathVariable("id") String id) {
        typeManager.deleteEnumConstant(id);
        return Result.voidSuccess();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") String id) {
        typeManager.remove(id);
        return Result.success(null);
    }

    @GetMapping("/field/{id}")
    public Result<GetFieldResponse> getField(@PathVariable("id") String fieldId) {
        return Result.success(typeManager.getField(fieldId));
    }

    @PostMapping("/field")
    public Result<String> saveField(@RequestBody FieldDTO field) {
        return Result.success(typeManager.saveField(field));
    }

    @PostMapping("/move-field")
    public Result<Void> moveField(@RequestBody MovePropertyRequest request) {
        typeManager.moveField(request.id(), request.ordinal());
        return Result.voidSuccess();
    }

    @DeleteMapping("/field/{id}")
    public Result<Void> deleteField(@PathVariable("id") String id) {
        typeManager.removeField(id);
        return Result.success(null);
    }

    @PostMapping("/field/{id}/set-as-title")
    public Result<Void> setAsTitle(@PathVariable("id") String id) {
        typeManager.setFieldAsTitle(id);
        return Result.success(null);
    }

    @GetMapping("/constraint")
    public Result<Page<ConstraintDTO>> listConstraint(
            @RequestParam("typeId") String typeId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pagSize", defaultValue = "20") int pageSize
    ) {
        return Result.success(typeManager.listConstraints(typeId, page, pageSize));
    }

    @GetMapping("/constraint/{id}")
    public Result<ConstraintDTO> getConstraint(@PathVariable("id") String id) {
        return Result.success(typeManager.getConstraint(id));
    }

    @PostMapping("/constraint")
    public Result<String> saveConstraint(@RequestBody ConstraintDTO constraint) {
        return Result.success(typeManager.saveConstraint(constraint));
    }

    @DeleteMapping("/constraint/{id}")
    public Result<Void> removeConstraint(@PathVariable("id") String id) {
        typeManager.removeConstraint(id);
        return Result.voidSuccess();
    }

    @GetMapping("/primitive-map")
    public Result<Map<Integer, String>> getPrimitiveMap() {
        return Result.success(typeManager.getPrimitiveMap());
    }

}
