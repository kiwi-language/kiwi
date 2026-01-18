package org.manul.common;

public enum ErrorCode {

    SUCCESS(0, "success"),
    EMPTY_REQUEST(102, "request parameters are missing"),
    INVALID_PARAMETERS(103, "invalid request parameters: {}"),

    // metadata
    INVALID_TYPE(201, "unable to save class {}: {}"),
    INVALID_FIELD(202, "unable to save field {}: {}"),
    FAILED_TO_DELETE_KLASS(203, "unable to delete class {}: {}"),
    INVALID_DEFAULT_VALUE(204, "invalid default value for field: {}"),
    MULTIPLE_TITLE_FIELDS(206, "only one title field can be set"),
    INVALID_SYMBOL_NAME(207, "invalid name {}: does not meet naming requirements"),
    DUPLICATE_CHOICE_OPTION_PROP(209, "duplicate option {}: {}"),
    ERROR_DELETING_TYPE(209, "deletion failed: {}"),
    TYPE_NOT_FOUND(210, "type {} does not exist"),
    INVALID_COLUMN(211, "invalid configuration for column {}: {}"),
    INVALID_TYPE_PATH(307, "invalid path: {}"),
    STATIC_FIELD_CAN_NOT_BE_NULL(308, "static field {} cannot be null"),
    TOO_MANY_FIELDS(309, "field limit exceeded"),
    OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES(209, "cannot change parameter count or types in overriding flow {}"),
    ORDINAL_OUT_OF_BOUND(310, "attribute sequence number out of range"),
    CAN_NOT_ASSIGN_CHILD_FIELD(311, "cannot update child object field"),
    INVALID_CODE(312, "invalid code {}, please follow the naming convention"),
    CHANGING_CATEGORY(313, "cannot change type category, such as class to enum"),
    CHANGING_IS_TEMPLATE(314, "cannot change generic to non-generic or vice versa"),
    PROPERTY_NOT_READABLE(315, "property is not readable"),
    PROPERTY_NOT_WRITABLE(316, "property is not writable"),
    TITLE_FIELD_MUST_BE_STRING(317, "title field '{}: {}' is not a string"),
    CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED(318, "child object field cannot be a primitive type"),
    ENTITY_STRUCT_LACKS_CANONICAL_CONSTRUCTOR(319, "entity structure {} lacks a canonical constructor"),
    CLASS_NOT_FOUND(320, "class {} does not exist"),
    NOT_AN_ENUM_CLASS(321, "class `{}' is not an enum class"),
    INVALID_ELEMENT_NAME(322, "invalid element name: {}"),
    CONSTRUCTOR_NOT_FOUND(323, "cannot find constructor in class {} for arguments: {}"),

    // instance
    INSTANCE_NOT_FOUND(301, "object {} does not exist"),
    INVALID_FIELD_VALUE(302, "invalid value for field: {}, value: {}"),
    FIELD_REQUIRED(303, "field {} is required"),
    INVALID_TYPE_VALUE(304, "invalid data format for type: {}, value: {}"),
    FIELD_VALUE_REQUIRED(305, "field {} is required"),
    STRONG_REFS_PREVENT_REMOVAL(306, "object is referenced by others and cannot be deleted: {}"),
    STRONG_REFS_PREVENT_REMOVAL2(308, "{} is referenced by {}, cannot be deleted"),
    INVALID_INSTANCE_PATH(307, "invalid object path: {}"),
    INCORRECT_PARENT_REF(308, "invalid parent object reference, child: {}, parent: {}, reference: {}"),
    MULTI_PARENT(309, "object {} has multiple parents"),
    CONVERSION_FAILED(410, "cannot convert object {} to type {}"),
    CAN_NOT_MODIFY_READONLY_FIELD(411, "cannot modify read-only field {}"),
    TYPE_CAST_ERROR(412, "type conversion error, original type: {}, target type: {}"),
    NOT_A_PHYSICAL_INSTANCE(413, "object {} is not a physical entity"),
    NOT_A_CLASS_INSTANCE(414, "object '} is not a class entity"),
    DELETE_NON_DURABLE_INSTANCE(415, "cannot delete non-persistent object"),
    INCORRECT_INSTANCE_FIELD_VALUE(416, "invalid value {} for field {} with type {}"),
    FAILED_TO_RESOLVE_VALUE(417, "failed to resolve value: {}"),
    FAILED_TO_RESOLVE_VALUE_OF_TYPE(418, "failed to resolve value of type {}"),
    FAILED_TO_FORMAT_VALUE(419, "failed to format value {}"),
    INCORRECT_ARRAY_ELEMENT(420, "invalid element {} for array {}"),
    INVALID_ID(421, "invalid ID: {}"),

    // Flow
    FLOW_NOT_FOUND(401, "flow {} does not exist"),
    NODE_NOT_FOUND(402, "node {} does not exist"),
    BRANCH_NOT_FOUND(403, "branch {} does not exist"),
    MISSING_END_NODE(411, "missing end node for flow"),
    STACK_UNDERFLOW(412, "stack underflow"),
    ILLEGAL_ACCESS(413, "illegal access"),
    FLOW_EXECUTION_FAILURE(414, "{}"),
    BRANCH_OWNER_MISMATCH(415, "branch ({}) owner node does not match current node ({})"),
    FLOW_DECLARING_TYPE_MISMATCH(416, "flow owner type does not match current type ({})"),
    BRANCH_INDEX_REQUIRED(417, "branch index is required"),
    BRANCH_INDEX_DUPLICATE(417, "branch index cannot be duplicated"),
    NUM_PRESELECTED_BRANCH_NOT_EQUAL_TO_ONE(417, "branch node must have exactly one default branch"),
    BRANCH_OUTPUT_VALUE_MUST_AGREE_WITH_BRANCHES(418, "branch node output field must set output value for each branch"),
    NOT_AN_ARRAY_VALUE(419, "invalid array value, data type is not an array"),
    INCORRECT_ELEMENT_TYPE(419, "array type {} does not match element type {}"),
    INCORRECT_INDEX_VALUE(419, "index must be an integer"),
    INDEX_OUT_OF_BOUND(420, "index out of bounds"),
    INTERFACE_FLOW_NOT_IMPLEMENTED(421, "{} has not implemented the {} method defined by the interface {}"),
    DEST_NODE_FIELD_MISSING_SOURCE_CONFIG(422, "target node field must configure all source node values"),
    ILLEGAL_TARGET_BRANCH(423, "invalid jump target branch"),
    NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH(423, "field value for node {} does not match field definition"),
    NOT_A_FUNCTION(424, "expression {} is not a function"),
    ILLEGAL_ARGUMENT(425, "invalid function call argument: {}"),
    ILLEGAL_FUNCTION_ARGUMENT(425, "invalid flow {} call argument, expected type: {}, actual type: {}"),
    INCORRECT_FUNCTION_ARGUMENT(426, "invalid function {} call argument"),
    CONFLICTING_FLOW(427, "flow signature conflict: same name and parameter type, different number of parameters"),
    OVERRIDE_FLOW_RETURN_TYPE_INCORRECT(428, "method {} does not correctly overrides {}, current return type: {}, overridden return type: {}"),
    NOT_A_CHILD_FIELD(429, "field {} is not a child object field"),
    MASTER_FIELD_REQUIRED(430, "parent object field is required"),
    MASTER_FIELD_SHOULD_BE_NULL(430, "when parent is an array, parent object field should be null"),
    INVALID_MASTER(431, "{} cannot be used as a parent object"),
    INCORRECT_FIELD_VALUE(432, "invalid value for field {}"),
    INVALID_ADD_OBJECT_CHILD(433, "node {} cannot be used as a child node for a new record"),
    FIELD_NOT_INITIALIZED(433, "failed to create {}, field {} is not initialized"),
    MODIFYING_READ_ONLY_ARRAY(433, "cannot modify read-only array"),
    ADD_ELEMENT_NOT_SUPPORTED(433, "adding elements to this array is not supported"),
    MISSING_REQUIRED_ARGUMENT(434, "missing required parameter {}"),
    STATIC_FLOW_CAN_NOT_BE_ABSTRACT(436, "static flow cannot be abstract"),
    INSTANCE_METHOD_MISSING_STATIC_TYPE(438, "instance method lacks static type"),
    MODIFYING_SYNTHETIC_FLOW(439, "{} is a synthetic flow and cannot be modified"),
    METHOD_RESOLUTION_FAILED(440, "unable to resolve method {} with arguments {}"),
    ILLEGAL_ARGUMENT1(441, "invalid function call argument: {}"),
    ILLEGAL_ARGUMENT2(442, "invalid argument {} in flow {} for parameter {}:{}"),
    INCORRECT_ARGUMENT_COUNT(443, "flow {} expects {} arguments but got {}"),
    INDEX_KEY_COMPUTE_ERROR(444, "failed to compute index key: {}, error: {}"),

    // expression
    EXPRESSION_INVALID(501, "invalid expression: {}"),
    EXPRESSION_INVALID_VALUE(502, "invalid expression value, expected type: {}, actual value: {}"),
    FUNCTION_ARGUMENTS_INVALID(503, "invalid parameters for function {}"),
    INVALID_CONDITION_EXPR(504, "invalid condition expression: {}"),
    ILLEGAL_SEARCH_CONDITION(505, "invalid search condition"),

    // user
    AUTH_FAILED(601, "authentication failed"),
    USER_NOT_FOUND(602, "user {} does not exist"),
    INVALID_TOKEN(603, "session expired, please log in again"),
    USER_ID_NOT_FOUND(604, "user ID {} does not exist"),
    ROLE_ID_NOT_FOUND(604, "role ID {} does not exist"),
    VERIFICATION_FAILED(605, "please log in first"),
    ILLEGAL_SESSION_STATE(606, "invalid session state"),
    NOT_A_MEMBER_OF_THE_APP(607, "user has not joined the application"),
    REENTERING_APP(608, "please exit the current application before proceeding"),
    NOT_IN_APP(609, "not currently in any application"),
    PLATFORM_USER_REQUIRED(610, "please log in with a platform account first"),
    INCORRECT_VERIFICATION_CODE(611, "incorrect verification code"),
    VERIFICATION_CODE_SENT_TOO_OFTEN(612, "verification code sent too frequently, please try again later"),
    TOO_MANY_LOGIN_ATTEMPTS(613, "too many login attempts, please try again later"),
    LOGIN_REQUIRED(614, "please log in first"),
    USERNAME_NOT_AVAILABLE(615, "username {} is not available"),

    // Constraint
    DUPLICATE_KEY(701, "duplicate unique key {}"),
    CONSTRAINT_CHECK_FAILED(702, "failed to save object {}: {}"),
    CONSTRAINT_NOT_FOUND(703, "constraint {} does not exist"),
    DUPLICATE_KEY2(704, "duplicate key for index {}: {}"),


    // Job
    SCHEDULER_STATUS_ALREADY_EXISTS(801, "job scheduler status already exists"),

    // VIEW
    LIST_VIEW_NOT_FOUND(901, "list view of type {} does not exist"),

    // Application
    CAN_NOT_EVICT_APP_OWNER(1001, "cannot remove the application owner"),
    CURRENT_USER_NOT_APP_ADMIN(1002, "you are not an administrator and cannot perform this action"),
    CURRENT_USER_NOT_APP_OWNER(1002, "you are not the application owner and cannot perform this action"),
    NUM_ADMINS_EXCEEDS_LIMIT(610, "number of administrators exceeds the limit"),
    ALREADY_JOINED_APP(611, "user {} has already joined the application"),
    INVITATION_ALREADY_ACCEPTED(612, "invitation has already been accepted"),
    ALREADY_AN_ADMIN(613, "user {} is already an administrator"),
    USER_NOT_ADMIN(614, "user {} is not an administrator"),
    INVALID_EMAIL_ADDRESS(615, "invalid email address"),
    APP_NOT_ACTIVE(616, "application is removed or inactive"),

    // Compiler
    RAW_TYPES_NOT_SUPPORTED(1201, "raw types are not supported"),
    DEPLOY_FAILED(1202, "{}"),


    // API
    INVALID_REQUEST_METHOD(1501, "invalid request method"),
    INVALID_REQUEST_PATH(1502, "invalid request path"),
    INVALID_REQUEST_BODY(1503, "invalid request body: {}"),
    BEAN_NOT_FOUND(1504, "bean {} does not exist"),
    MISSING_X_APP_ID(1505, "missing HTTP header X-APP-ID"),
    INVALID_APP_ID(1506, "invalid APP ID"),

    // DDL
    COMMIT_RUNNING(1601, "commit is in progress"),
    REVERSION_FAILED(1602, "reversion failed: {}"),
    NO_BACKUP(1603, "no backup found for appId {}"),
    NO_ONGOING_MIGRATION(1604, "no ongoing migration"),

    // schema
    UNSUPPORTED_SCHEMA(1701, "unsupported schema: {}"),

    // query
    ILLEGAL_QUERY(1801, "illegal query"),

    // Persistence
    RETRY_FAILED(2001, "retry failed"),


    // Common
    UNKNOWN(5000, "unknown error"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode value : values()) {
            if (value.code == code)
                return value;
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }

}