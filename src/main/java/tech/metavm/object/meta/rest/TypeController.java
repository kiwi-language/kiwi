package tech.metavm.object.meta.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.object.instance.ArrayKind;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.object.meta.rest.dto.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/type")
public class TypeController {

    private final TypeManager typeManager;

    public TypeController(TypeManager typeManager) {
        this.typeManager = typeManager;
    }

    @PostMapping("/query")
    public Result<Page<TypeDTO>> query(@RequestBody QueryTypeRequest request) {
        return Result.success(typeManager.query(request));
    }

    @GetMapping("/{id:[0-9]+}/descendants")
    public Result<GetTypesResponse> getDescendants(@PathVariable("id") long id) {
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

    @PostMapping("/batch-get")
    public Result<GetTypesResponse> batchGet(@RequestBody GetTypesRequest request) {
        return Result.success(
                typeManager.batchGetTypes(request)
        );
    }

    @GetMapping("/{id:[0-9]+}/creating-fields")
    public Result<List<CreatingFieldDTO>> getCreatingFields(@PathVariable("id") long id) {
        return Result.success(typeManager.getCreatingFields(id));
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

    @PostMapping("/enum-constant")
    public Result<Long> saveEnumConstant(@RequestBody InstanceDTO instanceDTO) {
        return Result.success(typeManager.saveEnumConstant(instanceDTO));
    }

    @DeleteMapping("/enum-constant/{id:[0-9]+}")
    public Result<Void> deleteEnumConstant(@PathVariable("id") long id) {
        typeManager.deleteEnumConstant(id);
        return Result.voidSuccess();
    }

    @GetMapping("/{id:[0-9]+}/array")
    public Result<TypeDTO> getArrayType(@PathVariable("id") long id) {
        return Result.success(typeManager.getArrayType(id, ArrayKind.READ_WRITE.code()).type());
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
    public Result<GetFieldResponse> getField(@PathVariable("id") long fieldId) {
        return Result.success(typeManager.getField(fieldId));
    }

    @PostMapping("/field")
    public Result<Long> saveField(@RequestBody FieldDTO field) {
        return Result.success(typeManager.saveField(field));
    }

    @PostMapping("/move-field")
    public Result<Void> moveField(@RequestBody MovePropertyRequest request) {
        typeManager.moveField(request.id(), request.ordinal());
        return Result.voidSuccess();
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

    @PostMapping("/get-union-type")
    public Result<GetTypeResponse> getUnionType(@RequestBody GetUnionTypeRequest request) {
        return Result.success(typeManager.getUnionType(request.memberIds()));
    }

    @PostMapping("/get-array-type")
    public Result<GetTypeResponse> getArrayType(@RequestBody GetArrayTypeRequest request) {
        return Result.success(typeManager.getArrayType(request.elementTypeId(), request.kind()));
    }

    @PostMapping("/get-parameterized-type")
    public Result<GetTypeResponse> getParameterizedType(@RequestBody GetParameterizedTypeRequest request) {
        return Result.success(typeManager.getParameterizedType(request));
    }

    @PostMapping("/get-function-type")
    public Result<GetTypeResponse> getFunctionType(@RequestBody GetFunctionTypeRequest request) {
        return Result.success(typeManager.getFunctionType(request.parameterTypeIds(), request.returnTypeId()));
    }

    @PostMapping("/get-uncertain-type")
    public Result<GetTypeResponse> getUncertainType(@RequestBody GetUncertainTypeRequest request) {
        return Result.success(typeManager.getUncertainType(request.lowerBoundId(), request.upperBoundId()));
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

    @GetMapping("/primitive-map")
    public Result<Map<Integer, Long>> getPrimitiveMap() {
        return Result.success(typeManager.getPrimitiveMap());
    }

}
