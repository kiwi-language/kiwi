package tech.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.expression.Func;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public class BusinessException extends RuntimeException {

    public static final Logger logger = LoggerFactory.getLogger(BusinessException.class);

    private final ErrorCode errorCode;
    private final Object[] params;

    public BusinessException(ErrorCode errorCode, Object... params) {
        super(ResultUtil.formatMessage(errorCode, params));
        this.errorCode = errorCode;
        this.params = params;
    }

    public static BusinessException invalidParams(String detail) {
        return new BusinessException(ErrorCode.INVALID_PARAMETERS, detail);
    }

    public static BusinessException strongReferencesPreventRemoval(Instance source, Instance target) {
        return new BusinessException(ErrorCode.STRONG_REFS_PREVENT_REMOVAL2, target.getQualifiedTitle(), source.getQualifiedTitle());
    }

    public static BusinessException invalidType(TypeDTO typeDTO, String reason) {
        return new BusinessException(ErrorCode.INVALID_TYPE, typeDTO.name(), reason);
    }

    public static BusinessException deleteNClassError(Klass nClass, String reason) {
        return new BusinessException(ErrorCode.DELETE_N_CLASS_ERROR, nClass.getName(), reason);
    }

    public static BusinessException invalidField(FieldDTO field, String reason) {
        throw new BusinessException(ErrorCode.INVALID_FIELD, field.name(), reason);
    }

    public static BusinessException notNullFieldWithoutDefaultValue(Field field) {
        throw invalidField(field, "When data already exists under the type, the newly added required field must carry a default value");
    }

    public static BusinessException invalidConditionExpr(String expr) {
        throw new BusinessException(ErrorCode.INVALID_CONDITION_EXPR, expr);
    }

    public static BusinessException fieldValueRequired(Field field) {
        throw new BusinessException(ErrorCode.FIELD_VALUE_REQUIRED, field.getName());
    }

    public static BusinessException invalidColumn(String columnName, String reason) {
        throw new BusinessException(ErrorCode.INVALID_FIELD, columnName, reason);
    }

    public static BusinessException duplicateOptionName(String optionName) {
        throw new BusinessException(ErrorCode.DUPLICATE_CHOICE_OPTION_PROP, "name", optionName);
    }

    public static BusinessException duplicateOptionOrder(int order) {
        throw new BusinessException(ErrorCode.DUPLICATE_CHOICE_OPTION_PROP, "order", order);
    }

    public static BusinessException duplicateOption(EnumConstantRT choiceOption) {
        throw new BusinessException(ErrorCode.DUPLICATE_CHOICE_OPTION,
                choiceOption.getId(), choiceOption.getName(), choiceOption.getOrdinal());
    }

    public static BusinessException invalidField(Field field, String reason) {
        throw new BusinessException(ErrorCode.INVALID_FIELD, field.getName(), reason);
    }

    public static BusinessException invalidField(String fieldName, String reason) {
        throw new BusinessException(ErrorCode.INVALID_FIELD, fieldName, reason);
    }

    public static BusinessException tooManyFields() {
        throw new BusinessException(ErrorCode.TOO_MAY_FIELDS);
    }

    public static BusinessException instanceNotFound(long id) {
        throw new BusinessException(ErrorCode.INSTANCE_NOT_FOUND, id);
    }

    public static BusinessException loginNameNotFound(String loginName) {
        return new BusinessException(ErrorCode.LOGIN_NAME_NOT_FOUND, loginName);
    }

    public static BusinessException authFailed() {
        return new BusinessException(ErrorCode.AUTH_FAILED);
    }

    public static BusinessException verificationFailed() {
        return new BusinessException(ErrorCode.VERIFICATION_FAILED);
    }

    public static BusinessException typeNotFound(long typeId) {
        throw new BusinessException(ErrorCode.TYPE_NOT_FOUND, typeId);
    }

    public static BusinessException typeNotFound(String code) {
        throw new BusinessException(ErrorCode.TYPE_NOT_FOUND, code);
    }

    public static BusinessException fieldNotFound(long fieldId) {
        throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND, fieldId);
    }

    public static BusinessException invalidFieldValue(Field field, Object value) {
        return new BusinessException(ErrorCode.INVALID_FIELD_VALUE, field.getName(), value);
    }

    public static BusinessException invalidValue(Type type, Object value) {
        return new BusinessException(ErrorCode.INVALID_TYPE_VALUE, type.getName(), value);
    }

    public static BusinessException typeReferredByFields(Klass type, List<String> fieldNames) {
        List<String> quotedFieldNames = NncUtils.map(fieldNames, s -> "\"" + s + "\"");
        return new BusinessException(
                ErrorCode.ERROR_DELETING_TYPE,
                "\"" + type.getName() + "\" is used by types：" + NncUtils.join(quotedFieldNames)
        );
    }

    public static BusinessException typeReferredByFlows(List<String> flowNames) {
        return new BusinessException(
                ErrorCode.ERROR_DELETING_TYPE,
                "type is used by flows: " + NncUtils.join(flowNames)
        );
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

    public static BusinessException invalidName(String name) {
        return new BusinessException(ErrorCode.INVALID_SYMBOL_NAME, name);
    }

    public static BusinessException flowNotFound(String id) {
        return new BusinessException(ErrorCode.FLOW_NOT_FOUND, id);
    }

    public static BusinessException branchNotFound(long id) {
        return new BusinessException(ErrorCode.BRANCH_NOT_FOUND, id);
    }

    public static BusinessException nodeNotFound(String id) {
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

    public static BusinessException invalidFuncArguments(Func function) {
        return new BusinessException(ErrorCode.FUNCTION_ARGUMENTS_INVALID, function.name());
    }

    public static BusinessException duplicateKey(ClassInstance instance, Id constraintId) {
        Index constraint = instance.getKlass().getUniqueConstraint(constraintId);
        return new BusinessException(
                ErrorCode.DUPLICATE_KEY,
                NncUtils.join(constraint.getTypeFields(), Field::getName)
        );
    }

    public static BusinessException constraintCheckFailed(Instance instance, Constraint constraint) {
        String reason = constraint.getMessage() != null ? constraint.getMessage() : constraint.getDefaultMessage();
        throw new BusinessException(
                ErrorCode.CONSTRAINT_CHECK_FAILED,
                instance.getTitle(),
                reason
        );
    }

    public static BusinessException constraintNotFound(String id) {
        throw new BusinessException(ErrorCode.CONSTRAINT_NOT_FOUND, id);
    }

    public static BusinessException invalidToken() {
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }


    public static BusinessException userNotFound(String id) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND, id);
    }

    public static BusinessException roleNotFound(String id) {
        throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, id);
    }

    public static BusinessException schedulerStatusAlreadyExists() {
        throw new BusinessException(ErrorCode.SCHEDULER_STATUS_ALREADY_EXISTS);
    }

    public static BusinessException listViewNotFound(Klass type) {
        throw new BusinessException(ErrorCode.LIST_VIEW_NOT_FOUND, type.getName());
    }

    public static BusinessException invalidInstancePath(String path) {
        throw new BusinessException(ErrorCode.INVALID_INSTANCE_PATH, path);
    }

    public static BusinessException invalidInstancePath(List<String> path) {
        throw new BusinessException(ErrorCode.INVALID_INSTANCE_PATH, NncUtils.join(path, "."));
    }

    public static BusinessException invalidTypePath(String path) {
        throw new BusinessException(ErrorCode.INVALID_TYPE_PATH, path);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }

}

