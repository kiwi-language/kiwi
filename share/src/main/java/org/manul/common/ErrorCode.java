package org.manul.common;

import static org.manul.util.HttpStatus.*;

public enum ErrorCode {

    // general
    EMPTY_REQUEST("request parameters are missing", SC_BAD_REQUEST),
    INVALID_PARAMETERS("invalid request parameters: {}", SC_BAD_REQUEST),
    OBJECT_NOT_FOUND("Object not found", SC_NOT_FOUND),

    // metadata
    INVALID_TYPE("unable to save class {}: {}", SC_BAD_REQUEST),
    INVALID_FIELD("unable to save field {}: {}", SC_BAD_REQUEST),
    FAILED_TO_DELETE_KLASS("unable to delete class {}: {}", SC_BAD_REQUEST),
    INVALID_DEFAULT_VALUE("invalid default value for field: {}", SC_BAD_REQUEST),
    MULTIPLE_TITLE_FIELDS("only one title field can be set", SC_BAD_REQUEST),
    INVALID_SYMBOL_NAME("invalid name {}: does not meet naming requirements", SC_BAD_REQUEST),
    DUPLICATE_CHOICE_OPTION_PROP("duplicate option {}: {}", SC_BAD_REQUEST),
    ERROR_DELETING_TYPE("deletion failed: {}", SC_BAD_REQUEST),
    TYPE_NOT_FOUND("type {} does not exist", SC_NOT_FOUND),
    INVALID_COLUMN("invalid configuration for column {}: {}", SC_BAD_REQUEST),
    INVALID_TYPE_PATH("invalid path: {}", SC_BAD_REQUEST),
    STATIC_FIELD_CAN_NOT_BE_NULL("static field {} cannot be null", SC_BAD_REQUEST),
    TOO_MANY_FIELDS("field limit exceeded", SC_BAD_REQUEST),
    OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES("cannot change parameter count or types in overriding flow {}", SC_BAD_REQUEST),
    ORDINAL_OUT_OF_BOUND("attribute sequence number out of range", SC_BAD_REQUEST),
    CAN_NOT_ASSIGN_CHILD_FIELD("cannot update child object field", SC_BAD_REQUEST),
    INVALID_CODE("invalid code {}, please follow the naming convention", SC_BAD_REQUEST),
    CHANGING_CATEGORY("cannot change type category, such as class to enum", SC_BAD_REQUEST),
    CHANGING_IS_TEMPLATE("cannot change generic to non-generic or vice versa", SC_BAD_REQUEST),
    PROPERTY_NOT_READABLE("property is not readable", SC_BAD_REQUEST),
    PROPERTY_NOT_WRITABLE("property is not writable", SC_BAD_REQUEST),
    TITLE_FIELD_MUST_BE_STRING("title field '{}: {}' is not a string", SC_BAD_REQUEST),
    CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED("child object field cannot be a primitive type", SC_BAD_REQUEST),
    ENTITY_STRUCT_LACKS_CANONICAL_CONSTRUCTOR("entity structure {} lacks a canonical constructor", SC_BAD_REQUEST),
    CLASS_NOT_FOUND("class {} does not exist", SC_NOT_FOUND),
    NOT_AN_ENUM_CLASS("class `{}' is not an enum class", SC_BAD_REQUEST),
    INVALID_ELEMENT_NAME("invalid element name: {}", SC_BAD_REQUEST),
    CONSTRUCTOR_NOT_FOUND("cannot find constructor in class {} for arguments: {}", SC_NOT_FOUND),

    // instance
    INSTANCE_NOT_FOUND("object {} does not exist", SC_NOT_FOUND),
    INVALID_FIELD_VALUE("invalid value for field: {}, value: {}", SC_BAD_REQUEST),
    FIELD_REQUIRED("field {} is required", SC_BAD_REQUEST),
    INVALID_TYPE_VALUE("invalid data format for type: {}, value: {}", SC_BAD_REQUEST),
    FIELD_VALUE_REQUIRED("field {} is required", SC_BAD_REQUEST),
    STRONG_REFS_PREVENT_REMOVAL("object is referenced by others and cannot be deleted: {}", SC_BAD_REQUEST),
    STRONG_REFS_PREVENT_REMOVAL2("{} is referenced by {}, cannot be deleted", SC_BAD_REQUEST),
    INVALID_INSTANCE_PATH("invalid object path: {}", SC_BAD_REQUEST),
    INCORRECT_PARENT_REF("invalid parent object reference, child: {}, parent: {}, reference: {}", SC_BAD_REQUEST),
    MULTI_PARENT("object {} has multiple parents", SC_BAD_REQUEST),
    CONVERSION_FAILED("cannot convert object {} to type {}", SC_BAD_REQUEST),
    CAN_NOT_MODIFY_READONLY_FIELD("cannot modify read-only field {}", SC_BAD_REQUEST),
    TYPE_CAST_ERROR("type conversion error, original type: {}, target type: {}", SC_BAD_REQUEST),
    NOT_A_PHYSICAL_INSTANCE("object {} is not a physical entity", SC_BAD_REQUEST),
    NOT_A_CLASS_INSTANCE("object '} is not a class entity", SC_BAD_REQUEST),
    DELETE_NON_DURABLE_INSTANCE("cannot delete non-persistent object", SC_BAD_REQUEST),
    INCORRECT_INSTANCE_FIELD_VALUE("invalid value {} for field {} with type {}", SC_BAD_REQUEST),
    FAILED_TO_RESOLVE_VALUE("failed to resolve value: {}", SC_BAD_REQUEST),
    FAILED_TO_RESOLVE_VALUE_OF_TYPE("failed to resolve value of type {}", SC_BAD_REQUEST),
    FAILED_TO_FORMAT_VALUE("failed to format value {}", SC_BAD_REQUEST),
    INCORRECT_ARRAY_ELEMENT("invalid element {} for array {}", SC_BAD_REQUEST),
    INVALID_ID("invalid ID: {}", SC_BAD_REQUEST),

    // Flow
    FLOW_NOT_FOUND("flow {} does not exist", SC_NOT_FOUND),
    NODE_NOT_FOUND("node {} does not exist", SC_NOT_FOUND),
    BRANCH_NOT_FOUND("branch {} does not exist", SC_NOT_FOUND),
    MISSING_END_NODE("missing end node for flow", SC_BAD_REQUEST),
    STACK_UNDERFLOW("stack underflow", SC_BAD_REQUEST),
    ILLEGAL_ACCESS("illegal access", SC_BAD_REQUEST),
    FLOW_EXECUTION_FAILURE("{}", SC_BAD_REQUEST),
    BRANCH_OWNER_MISMATCH("branch ({}) owner node does not match current node ({})", SC_BAD_REQUEST),
    FLOW_DECLARING_TYPE_MISMATCH("flow owner type does not match current type ({})", SC_BAD_REQUEST),
    BRANCH_INDEX_REQUIRED("branch index is required", SC_BAD_REQUEST),
    BRANCH_INDEX_DUPLICATE("branch index cannot be duplicated", SC_BAD_REQUEST),
    NUM_PRESELECTED_BRANCH_NOT_EQUAL_TO_ONE("branch node must have exactly one default branch", SC_BAD_REQUEST),
    BRANCH_OUTPUT_VALUE_MUST_AGREE_WITH_BRANCHES("branch node output field must set output value for each branch", SC_BAD_REQUEST),
    NOT_AN_ARRAY_VALUE("invalid array value, data type is not an array", SC_BAD_REQUEST),
    INCORRECT_ELEMENT_TYPE("array type {} does not match element type {}", SC_BAD_REQUEST),
    INCORRECT_INDEX_VALUE("index must be an integer", SC_BAD_REQUEST),
    INDEX_OUT_OF_BOUND("index out of bounds", SC_BAD_REQUEST),
    INTERFACE_FLOW_NOT_IMPLEMENTED("{} has not implemented the {} method defined by the interface {}", SC_BAD_REQUEST),
    DEST_NODE_FIELD_MISSING_SOURCE_CONFIG("target node field must configure all source node values", SC_BAD_REQUEST),
    ILLEGAL_TARGET_BRANCH("invalid jump target branch", SC_BAD_REQUEST),
    NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH("field value for node {} does not match field definition", SC_BAD_REQUEST),
    NOT_A_FUNCTION("expression {} is not a function", SC_BAD_REQUEST),
    ILLEGAL_ARGUMENT("invalid function call argument: {}", SC_BAD_REQUEST),
    ILLEGAL_FUNCTION_ARGUMENT("invalid flow {} call argument, expected type: {}, actual type: {}", SC_BAD_REQUEST),
    INCORRECT_FUNCTION_ARGUMENT("invalid function {} call argument", SC_BAD_REQUEST),
    CONFLICTING_FLOW("flow signature conflict: same name and parameter type, different number of parameters", SC_BAD_REQUEST),
    OVERRIDE_FLOW_RETURN_TYPE_INCORRECT("method {} does not correctly overrides {}, current return type: {}, overridden return type: {}", SC_BAD_REQUEST),
    NOT_A_CHILD_FIELD("field {} is not a child object field", SC_BAD_REQUEST),
    MASTER_FIELD_REQUIRED("parent object field is required", SC_BAD_REQUEST),
    MASTER_FIELD_SHOULD_BE_NULL("when parent is an array, parent object field should be null", SC_BAD_REQUEST),
    INVALID_MASTER("{} cannot be used as a parent object", SC_BAD_REQUEST),
    INCORRECT_FIELD_VALUE("invalid value for field {}", SC_BAD_REQUEST),
    INVALID_ADD_OBJECT_CHILD("node {} cannot be used as a child node for a new record", SC_BAD_REQUEST),
    FIELD_NOT_INITIALIZED("failed to create {}, field {} is not initialized", SC_BAD_REQUEST),
    MODIFYING_READ_ONLY_ARRAY("cannot modify read-only array", SC_BAD_REQUEST),
    ADD_ELEMENT_NOT_SUPPORTED("adding elements to this array is not supported", SC_BAD_REQUEST),
    MISSING_REQUIRED_ARGUMENT("missing required parameter {}", SC_BAD_REQUEST),
    STATIC_FLOW_CAN_NOT_BE_ABSTRACT("static flow cannot be abstract", SC_BAD_REQUEST),
    INSTANCE_METHOD_MISSING_STATIC_TYPE("instance method lacks static type", SC_BAD_REQUEST),
    MODIFYING_SYNTHETIC_FLOW("{} is a synthetic flow and cannot be modified", SC_BAD_REQUEST),
    METHOD_RESOLUTION_FAILED("unable to resolve method {} with arguments {}", SC_BAD_REQUEST),
    ILLEGAL_ARGUMENT1("invalid function call argument: {}", SC_BAD_REQUEST),
    ILLEGAL_ARGUMENT2("invalid argument {} in flow {} for parameter {}:{}", SC_BAD_REQUEST),
    INCORRECT_ARGUMENT_COUNT("flow {} expects {} arguments but got {}", SC_BAD_REQUEST),
    INDEX_KEY_COMPUTE_ERROR("failed to compute index key: {}, error: {}", SC_BAD_REQUEST),

    // expression
    EXPRESSION_INVALID("invalid expression: {}", SC_BAD_REQUEST),
    EXPRESSION_INVALID_VALUE("invalid expression value, expected type: {}, actual value: {}", SC_BAD_REQUEST),
    FUNCTION_ARGUMENTS_INVALID("invalid parameters for function {}", SC_BAD_REQUEST),
    INVALID_CONDITION_EXPR("invalid condition expression: {}", SC_BAD_REQUEST),
    ILLEGAL_SEARCH_CONDITION("invalid search condition", SC_BAD_REQUEST),

    // user
    AUTH_FAILED("authentication failed", SC_BAD_REQUEST),
    USER_NOT_FOUND("user {} does not exist", SC_NOT_FOUND),
    INVALID_TOKEN("session expired, please log in again", SC_BAD_REQUEST),
    USER_ID_NOT_FOUND("user ID {} does not exist", SC_NOT_FOUND),
    ROLE_ID_NOT_FOUND("role ID {} does not exist", SC_NOT_FOUND),
    VERIFICATION_FAILED("please log in first", SC_UNAUTHORIZED),
    ILLEGAL_SESSION_STATE("invalid session state", SC_BAD_REQUEST),
    NOT_A_MEMBER_OF_THE_APP("user has not joined the application", SC_BAD_REQUEST),
    REENTERING_APP("please exit the current application before proceeding", SC_BAD_REQUEST),
    NOT_IN_APP("not currently in any application", SC_BAD_REQUEST),
    PLATFORM_USER_REQUIRED("please log in with a platform account first", SC_BAD_REQUEST),
    INCORRECT_VERIFICATION_CODE("incorrect verification code", SC_BAD_REQUEST),
    VERIFICATION_CODE_SENT_TOO_OFTEN("verification code sent too frequently, please try again later", SC_BAD_REQUEST),
    TOO_MANY_LOGIN_ATTEMPTS("too many login attempts, please try again later", SC_BAD_REQUEST),
    LOGIN_REQUIRED("please log in first", SC_UNAUTHORIZED),
    USERNAME_NOT_AVAILABLE("username {} is not available", SC_BAD_REQUEST),

    // Constraint
    DUPLICATE_KEY("duplicate unique key {}", SC_BAD_REQUEST),
    CONSTRAINT_CHECK_FAILED("failed to save object {}: {}", SC_BAD_REQUEST),
    CONSTRAINT_NOT_FOUND("constraint {} does not exist", SC_NOT_FOUND),
    DUPLICATE_KEY2("duplicate key for index {}: {}", SC_BAD_REQUEST),


    // Job
    SCHEDULER_STATUS_ALREADY_EXISTS("job scheduler status already exists", SC_BAD_REQUEST),

    // VIEW
    LIST_VIEW_NOT_FOUND("list view of type {} does not exist", SC_NOT_FOUND),

    // Application
    CAN_NOT_EVICT_APP_OWNER("cannot remove the application owner", SC_BAD_REQUEST),
    CURRENT_USER_NOT_APP_ADMIN("you are not an administrator and cannot perform this action", SC_BAD_REQUEST),
    CURRENT_USER_NOT_APP_OWNER("you are not the application owner and cannot perform this action", SC_BAD_REQUEST),
    NUM_ADMINS_EXCEEDS_LIMIT("number of administrators exceeds the limit", SC_BAD_REQUEST),
    ALREADY_JOINED_APP("user {} has already joined the application", SC_BAD_REQUEST),
    INVITATION_ALREADY_ACCEPTED("invitation has already been accepted", SC_BAD_REQUEST),
    ALREADY_AN_ADMIN("user {} is already an administrator", SC_BAD_REQUEST),
    USER_NOT_ADMIN("user {} is not an administrator", SC_BAD_REQUEST),
    INVALID_EMAIL_ADDRESS("invalid email address", SC_BAD_REQUEST),
    APP_NOT_ACTIVE("application is removed or inactive", SC_BAD_REQUEST),
    ILLEGAL_APP_NAME("Application names are limited to 100 lowercase alphanumeric characters, underscores (_), and hyphens (-). The sequence '---' is prohibited.", SC_BAD_REQUEST),
    CONFLICTING_APP_NAME("application name already exists", SC_BAD_REQUEST),

    // Compiler
    RAW_TYPES_NOT_SUPPORTED("raw types are not supported", SC_BAD_REQUEST),
    DEPLOY_FAILED("{}", SC_BAD_REQUEST),


    // API
    INVALID_REQUEST_METHOD("invalid request method", SC_BAD_REQUEST),
    INVALID_REQUEST_PATH("invalid request path", SC_NOT_FOUND),
    INVALID_REQUEST_BODY("invalid request body: {}", SC_BAD_REQUEST),
    BEAN_NOT_FOUND("bean {} does not exist", SC_NOT_FOUND),
    MISSING_X_APP_ID("missing HTTP header X-APP-ID", SC_BAD_REQUEST),
    INVALID_APP_ID("invalid APP ID", SC_BAD_REQUEST),

    // DDL
    COMMIT_RUNNING("commit is in progress", SC_BAD_REQUEST),
    REVERSION_FAILED("reversion failed: {}", SC_BAD_REQUEST),
    NO_BACKUP("no backup found for appId {}", SC_BAD_REQUEST),
    NO_ONGOING_MIGRATION("no ongoing migration", SC_BAD_REQUEST),

    // schema
    UNSUPPORTED_SCHEMA("unsupported schema: {}", SC_BAD_REQUEST),

    // query
    ILLEGAL_QUERY("illegal query", SC_BAD_REQUEST),

    // Persistence
    RETRY_FAILED("retry failed", SC_BAD_REQUEST),


    // API
    INVALID_QUERY_PARAM("Invalid query param {}: {}", SC_BAD_REQUEST),

    // Common
    UNKNOWN("unknown error", SC_INTERNAL_SERVER_ERROR),
    METHOD_NOT_FOUND("method not found", SC_NOT_FOUND);

    private final String message;
    private final int httpStatus;

    ErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String message() {
        return message;
    }

    public int httpStatus() {
        return httpStatus;
    }

    /**
     * Returns the ErrorCode for the given name, or UNKNOWN if not found.
     */
    public static ErrorCode fromName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

}