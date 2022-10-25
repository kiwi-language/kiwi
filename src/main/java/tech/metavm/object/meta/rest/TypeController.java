package tech.metavm.object.meta.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.object.meta.TypeManager;
import tech.metavm.object.meta.PrimitiveTypeInitializer;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.List;

@RestController
@RequestMapping("/type")
public class TypeController {

    @Autowired
    private TypeManager typeManager;

    @Autowired
    private PrimitiveTypeInitializer primitiveTypeInitializer;

    @PostMapping("/init-primitives")
    public Result<Void> initPrimitives() {
        primitiveTypeInitializer.execute();
        return Result.success(null);
    }

    @GetMapping
    public Result<Page<TypeDTO>> list(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "categoryCodes", required = false) String categoryCodes,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        List<Integer> categoryCodeList = NncUtils.isNotEmpty(categoryCodes) ? NncUtils.splitIntegers(categoryCodes) : null;
        return Result.success(typeManager.query(searchText, categoryCodeList, page, pageSize));
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<TypeDTO> get(
            @PathVariable("id") long id,
            @RequestParam(value = "includingFields", defaultValue = "true") boolean includingFields,
            @RequestParam(value = "includingFieldTypes", defaultValue = "false") boolean includingFieldTypes
    ) {
        TypeDTO typeDTO = typeManager.getType(id, includingFields, includingFieldTypes);
        if(typeDTO == null) {
            return Result.failure(ErrorCode.RECORD_NOT_FOUND);
        }
        return Result.success(typeDTO);
    }

    @PostMapping
    public Result<Long> save(@RequestBody TypeDTO typeDTO) {
        try {
            return Result.success(typeManager.saveType(typeDTO));
        }
        catch (BusinessException e) {
            return Result.failure(e.getErrorCode(), e.getParams());
        }
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
        try {
            typeManager.deleteType(id);
            return Result.success(null);
        }
        catch (BusinessException e) {
            return Result.failure(e.getErrorCode(), e.getParams());
        }
    }

    @GetMapping("/field/{id:[0-9]+}")
    public Result<FieldDTO> getField(@PathVariable("id") long fieldId) {
        try {
            return Result.success(typeManager.getField(fieldId));
        }
        catch (BusinessException e) {
            return Result.failure(e.getErrorCode(), e.getParams());
        }
    }

    @PostMapping("/field")
    public Result<Long> saveField(@RequestBody FieldDTO field) {
        try {
            return Result.success(typeManager.saveField(field));
        }
        catch (BusinessException e) {
            return Result.failure(e.getErrorCode(), e.getParams());
        }
    }

    @DeleteMapping("/field/{id:[0-9]+}")
    public Result<Void> deleteField(@PathVariable("id") long id) {
        try {
            typeManager.removeField(id);
            return Result.success(null);
        }
        catch (BusinessException e) {
            return Result.failure(e.getErrorCode(), e.getParams());
        }
    }

    @PostMapping("/field/{id:[0-9]+}/set-as-title")
    public Result<Void> setAsTitle(@PathVariable("id") long id) {
        typeManager.setFieldAsTitle(id);
        return Result.success(null);
    }

}
