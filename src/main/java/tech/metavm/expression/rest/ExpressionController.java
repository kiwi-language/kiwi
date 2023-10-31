package tech.metavm.expression.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.Result;
import tech.metavm.expression.ExpressionService;
import tech.metavm.expression.dto.BoolExprDTO;
import tech.metavm.expression.dto.BoolExprParseRequest;
import tech.metavm.expression.dto.InstanceSearchItemDTO;

import java.util.List;

@RestController
@RequestMapping("/expression")
public class ExpressionController {

    @Autowired
    private ExpressionService expressionService;

    @PostMapping("/parse-bool")
    public Result<BoolExprDTO> parseBoolExpr(@RequestBody BoolExprParseRequest request) {
        return Result.success(expressionService.parseBoolExpr(request));
    }

    @GetMapping("/parse-search-expression")
    public Result<List<InstanceSearchItemDTO>> parseSearchText(
            @RequestParam("typeId") long typeId,
            @RequestParam("searchExpression") String searchText
    ) {
        return Result.success(expressionService.parseSearchText(typeId, searchText));
    }

}
