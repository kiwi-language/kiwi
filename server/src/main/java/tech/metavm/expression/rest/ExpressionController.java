package tech.metavm.expression.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.common.Result;
import tech.metavm.expression.ExpressionService;
import tech.metavm.expression.dto.BoolExprDTO;
import tech.metavm.expression.dto.BoolExprParseRequest;

@RestController
@RequestMapping("/expression")
public class ExpressionController {

    @Autowired
    private ExpressionService expressionService;

    @PostMapping("/parse-bool")
    public Result<BoolExprDTO> parseBoolExpr(@RequestBody BoolExprParseRequest request) {
        return Result.success(expressionService.parseBoolExpr(request));
    }

}
