package tech.metavm.object.meta.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/type")
public class TypeController {

    private final TypeManager typeManager;

    public TypeController(TypeManager typeManager) {
        this.typeManager = typeManager;
    }

    @GetMapping
    public Result<Page<TypeDTO>> list(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "categoryCodes", required = false) String categoryCodes,
            @RequestParam(value = "includeBuiltin", required = false, defaultValue = "false") boolean includeBuiltin,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        List<Integer> categoryCodeList = NncUtils.isNotEmpty(categoryCodes) ? NncUtils.splitIntegers(categoryCodes) : null;
        return Result.success(typeManager.query(searchText, categoryCodeList, includeBuiltin, page, pageSize));
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<TypeDTO> get(
            @PathVariable("id") long id,
            @RequestParam(value = "includingFields", defaultValue = "true") boolean includingFields,
            @RequestParam(value = "includingFieldTypes", defaultValue = "true") boolean includingFieldTypes
    ) {
        TypeDTO typeDTO = typeManager.getType(id, includingFields, includingFieldTypes);
        if(typeDTO == null) {
            return Result.failure(ErrorCode.RECORD_NOT_FOUND);
        }
        return Result.success(typeDTO);
    }

    @PostMapping("/batch-get")
    public Result<List<TypeDTO>> batchGet(@RequestBody BatchGetRequest request) {
        return Result.success(
                typeManager.batchGetTypes(request.ids(), request.includingFields(), request.includingFieldTypes())
        );
    }

    @PostMapping
    public Result<Long> save(@RequestBody TypeDTO typeDTO) {
        return Result.success(typeManager.saveType(typeDTO).id());
    }

    @PostMapping("/batch")
    public Result<List<Long>> batchSave(@RequestBody List<TypeDTO> typeDTOs) {
        return Result.success(typeManager.batchSave(typeDTOs));
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchRemove(@RequestBody List<Long> typeIds) {
        typeManager.batchRemove(typeIds);
        return Result.voidSuccess();
    }

    @GetMapping("/{id:[0-9]+}/array")
    public Result<TypeDTO> getArrayType(@PathVariable("id") long id) {
        return Result.success(typeManager.getArrayType(id));
    }

    @GetMapping("/{id:[0-9]+}/nullable")
    public Result<TypeDTO> getNullableType(@PathVariable("id") long id) {
        return Result.success(typeManager.getNullableType(id));
    }

    @GetMapping("/{id:[0-9]+}/nullable-array")
    public Result<TypeDTO> getNullableArrayType(@PathVariable("id") long id) {
        return Result.success(typeManager.getNullableArrayType(id));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        typeManager.remove(id);
        return Result.success(null);
    }

    @GetMapping("/field/{id:[0-9]+}")
    public Result<FieldDTO> getField(@PathVariable("id") long fieldId) {
        return Result.success(typeManager.getField(fieldId));
    }

    @PostMapping("/field")
    public Result<Long> saveField(@RequestBody FieldDTO field) {
        return Result.success(typeManager.saveField(field));
    }

    @DeleteMapping("/field/{id:[0-9]+}")
    public Result<Void> deleteField(@PathVariable("id") long id) {
        typeManager.removeField(id);
        return Result.success(null);
    }

    @PostMapping("/field/{id:[0-9]+}/set-as-title")
    public Result<Void> setAsTitle(@PathVariable("id") long id) {
        typeManager.setFieldAsTitle(id);
        return Result.success(null);
    }


    @GetMapping("/constraint")
    public Result<Page<ConstraintDTO>> listConstraint(
            @RequestParam("typeId") long typeId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pagSize", defaultValue = "20") int pageSize
    ) {
        return Result.success(typeManager.listConstraints(typeId, page, pageSize));
    }

    @GetMapping("/constraint/{id:[0-9]+}")
    public Result<ConstraintDTO> getConstraint(@PathVariable("id") long id) {
        return Result.success(typeManager.getConstraint(id));
    }

    @PostMapping("/load-by-paths")
    public Result<LoadByPathsResponse> loadByPaths(@RequestBody LoadByPathsRequest request) {
        return Result.success(typeManager.loadByPaths(request.paths()));
    }

    @PostMapping("/constraint")
    public Result<Long> saveConstraint(@RequestBody ConstraintDTO constraint) {
        return Result.success(typeManager.saveConstraint(constraint));
    }

    @PostMapping("/init-composite-types/{id}")
    public Result<Void> initCompositeTypes(@PathVariable long id) {
        typeManager.initCompositeTypes(id);
        return Result.voidSuccess();
    }

    @DeleteMapping("/constraint/{id:[0-9]+}")
    public Result<Void> removeConstraint(@PathVariable("id") long id) {
        typeManager.removeConstraint(id);
        return Result.voidSuccess();
    }

    @GetMapping("/primitives")
    public Result<Map<String, TypeDTO>> getPrimitiveTypes() {
        return Result.success(typeManager.getPrimitiveTypes());
    }

}
