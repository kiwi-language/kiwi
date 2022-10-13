package tech.metavm.util;

import tech.metavm.dto.ErrorCode;
import tech.metavm.object.instance.query.Function;
import tech.metavm.object.meta.ChoiceOption;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;
    private Object[] params;

    public BusinessException(ErrorCode errorCode, Object... params) {
        super(ResultUtil.formatMessage(errorCode, params));
        this.errorCode = errorCode;
        this.params = params;
        this.detail = "";
    }

    public static BusinessException invalidParams(String detail) {
        return new BusinessException(ErrorCode.INVALID_PARAMETERS, detail);
    }

    public static BusinessException invalidNClass(TypeDTO typeDTO, String reason) {
        return new BusinessException(ErrorCode.INVALID_N_CLASS, typeDTO.name(), reason);
    }

    public static BusinessException deleteNClassError(Type nClass, String reason) {
        return new BusinessException(ErrorCode.DELETE_N_CLASS_ERROR, nClass.getName(), reason);
    }

    public static BusinessException invalidField(FieldDTO field, String reason) {
        throw new BusinessException(ErrorCode.INVALID_N_FIELD, field.name(), reason);
    }

    public static BusinessException duplicateOptionName(String optionName) {
        throw new BusinessException(ErrorCode.DUPLICATE_CHOICE_OPTION_PROP, "名称", optionName);
    }

    public static BusinessException duplicateOptionOrder(int order) {
        throw new BusinessException(ErrorCode.DUPLICATE_CHOICE_OPTION_PROP, "序号", order);
    }

    public static BusinessException duplicateOption(ChoiceOption choiceOption) {
        throw new BusinessException(ErrorCode.DUPLICATE_CHOICE_OPTION,
                choiceOption.getId(), choiceOption.getName(), choiceOption.getOrder());
    }

    public static BusinessException invalidField(Field field, String reason) {
        throw new BusinessException(ErrorCode.INVALID_N_FIELD, field.getName(), reason);
    }

    public static BusinessException invalidField(String fieldName, String reason) {
        throw new BusinessException(ErrorCode.INVALID_N_FIELD, fieldName, reason);
    }

    public static BusinessException instanceNotFound(long id) {
        throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id);
    }

    public static BusinessException fieldNotFound(long fieldId) {
        throw new BusinessException(ErrorCode.FIELD_NOT_FOUND, fieldId);
    }

    public static BusinessException invalidFieldValue(Field field, Object value) {
        return new BusinessException(ErrorCode.INVALID_FIELD_VALUE, field.getName(), value);
    }

    public static BusinessException invalidValue(Type type, Object value) {
        return new BusinessException(ErrorCode.INVALID_TYPE_VALUE, type.getName(), value);
    }

    public static BusinessException fieldRequired(Field field) {
        return new BusinessException(ErrorCode.FIELD_REQUIRED, field.getName());
    }

    public static BusinessException invalidDefaultValue(Object defaultValue) {
        return new BusinessException(ErrorCode.INVALID_DEFAULT_VALUE, defaultValue);
    }

    public static BusinessException multipleTitleFields() {
        return new BusinessException(ErrorCode.MULTIPLE_TITLE_FIELDS);
    }

    public static BusinessException invalidSymbolName(String name) {
        return new BusinessException(ErrorCode.INVALID_SYMBOL_NAME, name);
    }

    public static BusinessException flowNotFound(long id) {
        return new BusinessException(ErrorCode.FLOW_NOT_FOUND, id);
    }

    public static BusinessException branchNotFound(long id) {
        return new BusinessException(ErrorCode.BRANCH_NOT_FOUND, id);
    }

    public static BusinessException nodeNotFound(long id) {
        return new BusinessException(ErrorCode.NODE_NOT_FOUND, id);
    }

    public static BusinessException missingEndNode() {
        throw new BusinessException(ErrorCode.MISSING_END_NODE);
    }


    public static BusinessException stackUnderflow() {
        throw new BusinessException(ErrorCode.MISSING_END_NODE);
    }

    public static BusinessException illegalAccess() {
        return new BusinessException(ErrorCode.ILLEGAL_ACCESS);
    }

    public static BusinessException invalidExpression(String reason) {
        return new BusinessException(ErrorCode.EXPRESSION_INVALID, reason);
    }


    public static BusinessException invalidExpressionValue(String expectedType, Object actualValue) {
        return new BusinessException(ErrorCode.EXPRESSION_INVALID_VALUE, expectedType, actualValue);
    }

    public static BusinessException invalidFuncArguments(Function function) {
        return new BusinessException(ErrorCode.FUNCTION_ARGUMENTS_INVALID, function.name());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }

}

