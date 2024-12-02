package org.metavm.common;

public enum ErrorCode {

    SUCCESS(0, "Success"),
    RECORD_NOT_FOUND(101, "Record not found"),
    EMPTY_REQUEST(102, "Request parameters are missing"),
    INVALID_PARAMETERS(103, "Invalid request parameters: {}"),

    // metadata
    INVALID_TYPE(201, "Unable to save class '{}': {}"),
    INVALID_FIELD(202, "Unable to save field '{}': {}"),
    FAILED_TO_DELETE_KLASS(203, "Unable to delete class '{}': {}"),
    INVALID_DEFAULT_VALUE(204, "Invalid default value for field: {}"),
    PROPERTY_NOT_FOUND(205, "Property {} not found"),
    MULTIPLE_TITLE_FIELDS(206, "Only one title field can be set"),
    INVALID_SYMBOL_NAME(207, "Invalid name '{}': does not meet naming requirements"),
    DUPLICATE_CHOICE_OPTION(208, "Duplicate option, ID: {}, Name: {}, Serial number: {}"),
    DUPLICATE_CHOICE_OPTION_PROP(209, "Duplicate option {}: {}"),
    ERROR_DELETING_TYPE(209, "Deletion failed: {}"),
    TYPE_NOT_FOUND(210, "Type not found: {}"),
    INVALID_COLUMN(211, "Invalid configuration for column '{}': {}"),
    INVALID_TYPE_PATH(307, "Invalid path: {}"),
    STATIC_FIELD_CAN_NOT_BE_NULL(308, "Static field '{}' cannot be null"),
    TOO_MANY_FIELDS(309, "Field limit exceeded"),
    OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES(209, "Cannot change parameter count or types in overriding flow {}"),
    ORDINAL_OUT_OF_BOUND(310, "Attribute sequence number out of range"),
    CAN_NOT_ASSIGN_CHILD_FIELD(311, "Cannot update child object field"),
    INVALID_CODE(312, "Invalid code '{}', please follow the naming convention"),
    CHANGING_CATEGORY(313, "Cannot change type category, such as class to enum"),
    CHANGING_IS_TEMPLATE(314, "Cannot change generic to non-generic or vice versa"),
    PROPERTY_NOT_READABLE(315, "Property is not readable"),
    PROPERTY_NOT_WRITABLE(316, "Property is not writable"),
    TITLE_FIELD_MUST_BE_STRING(317, "Title field must be a string"),
    CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED(318, "Child object field cannot be a primitive type"),
    ENTITY_STRUCT_LACKS_CANONICAL_CONSTRUCTOR(319, "Entity structure {} lacks a canonical constructor"),
    CLASS_NOT_FOUND(320, "Class '{}' not found"),
    NOT_AN_ENUM_CLASS(321, "Class `{}' is not an enum class"),
    INVALID_ELEMENT_NAME(322, "Invalid element name: {}"),

    // instance
    INSTANCE_NOT_FOUND(301, "Object '{}' not found"),
    INVALID_FIELD_VALUE(302, "Invalid value for field: {}, value: {}"),
    FIELD_REQUIRED(303, "Field {} is required"),
    INVALID_TYPE_VALUE(304, "Invalid data format for type: {}, value: {}"),
    FIELD_VALUE_REQUIRED(305, "Field '{}' is required"),
    STRONG_REFS_PREVENT_REMOVAL(306, "Object is referenced by others and cannot be deleted: {}"),
    STRONG_REFS_PREVENT_REMOVAL2(308, "'{}' is referenced by '{}', cannot be deleted"),
    INVALID_INSTANCE_PATH(307, "Invalid object path: {}"),
    INCORRECT_PARENT_REF(308, "Invalid parent object reference, child: {}, parent: {}, reference: {}"),
    MULTI_PARENT(309, "Child object belongs to multiple parents: {}"),
    CONVERSION_FAILED(410, "Cannot convert object '{}' to type '{}'"),
    CAN_NOT_MODIFY_READONLY_FIELD(411, "Cannot modify read-only field {}"),
    TYPE_CAST_ERROR(412, "Type conversion error, original type: {}, target type: {}"),
    NOT_A_PHYSICAL_INSTANCE(413, "Object '{}' is not a physical entity"),
    NOT_A_CLASS_INSTANCE(414, "Object '{}' is not a class entity"),
    DELETE_NON_DURABLE_INSTANCE(415, "Cannot delete non-persistent object"),
    INCORRECT_INSTANCE_FIELD_VALUE(416, "Invalid value for field '{}': {}"),
    FAILED_TO_RESOLVE_VALUE(417, "Failed to resolve value: {}"),
    FAILED_TO_RESOLVE_VALUE_OF_TYPE(418, "Failed to resolve value of type {}"),
    FAILED_TO_FORMAT_VALUE(419, "Failed to format value {}"),
    INCORRECT_ARRAY_ELEMENT(420, "Invalid element '{}' for array {}"),

    // Flow
    FLOW_NOT_FOUND(401, "Flow {} not found"),
    NODE_NOT_FOUND(402, "Node {} not found"),
    BRANCH_NOT_FOUND(403, "Branch {} not found"),
    MISSING_END_NODE(411, "Missing end node for flow"),
    STACK_UNDERFLOW(412, "Stack underflow"),
    ILLEGAL_ACCESS(413, "No access permission"),
    FLOW_EXECUTION_FAILURE(414, "{}"),
    BRANCH_OWNER_MISMATCH(415, "Branch ({}) owner node does not match current node ({})"),
    FLOW_DECLARING_TYPE_MISMATCH(416, "Flow owner type does not match current type ({})"),
    BRANCH_INDEX_REQUIRED(417, "Branch index is required"),
    BRANCH_INDEX_DUPLICATE(417, "Branch index cannot be duplicated"),
    NUM_PRESELECTED_BRANCH_NOT_EQUAL_TO_ONE(417, "Branch node must have exactly one default branch"),
    BRANCH_OUTPUT_VALUE_MUST_AGREE_WITH_BRANCHES(418, "Branch node output field must set output value for each branch"),
    MISSING_MERGE_NODE_FIELD_VALUE(418, "Merge node has unset fields"),
    NOT_AN_ARRAY_VALUE(419, "Invalid array value, data type is not an array"),
    INCORRECT_ELEMENT_TYPE(419, "Array type '{}' does not match element type '{}'"),
    INCORRECT_INDEX_VALUE(419, "Index must be an integer"),
    INDEX_OUT_OF_BOUND(420, "Index out of bounds"),
    INTERFACE_FLOW_NOT_IMPLEMENTED(421, "'{}' has not implemented the '{}' method defined by the interface {}"),
    DEST_NODE_FIELD_MISSING_SOURCE_CONFIG(422, "Target node field must configure all source node values"),
    ILLEGAL_TARGET_BRANCH(423, "Invalid jump target branch"),
    NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH(423, "Field value for node '{}' does not match field definition"),
    NOT_A_FUNCTION(424, "Expression '{}' is not a function"),
    ILLEGAL_ARGUMENT(425, "Invalid function call argument: {}"),
    ILLEGAL_FUNCTION_ARGUMENT(425, "Invalid flow '{}' call argument, expected type: {}, actual type: {}"),
    INCORRECT_FUNCTION_ARGUMENT(426, "Invalid function '{}' call argument"),
    CONFLICTING_FLOW(427, "Flow signature conflict: same name and parameter type, different number of parameters"),
    OVERRIDE_FLOW_RETURN_TYPE_INCORRECT(428, "Method {} does not correctly overrides {}, current return type: {}, overridden return type: {}"),
    NOT_A_CHILD_FIELD(429, "Field '{}' is not a child object field"),
    MASTER_FIELD_REQUIRED(430, "Parent object field is required"),
    MASTER_FIELD_SHOULD_BE_NULL(430, "When parent is an array, parent object field should be null"),
    INVALID_MASTER(431, "'{}' cannot be used as a parent object"),
    INCORRECT_FIELD_VALUE(432, "Invalid value for field '{}'"),
    INVALID_ADD_OBJECT_CHILD(433, "Node '{}' cannot be used as a child node for a new record"),
    FIELD_NOT_INITIALIZED(433, "Failed to create '{}', field '{}' is not initialized"),
    MODIFYING_READ_ONLY_ARRAY(433, "Cannot modify read-only array"),
    ADD_ELEMENT_NOT_SUPPORTED(433, "Adding elements to this array is not supported"),
    MISSING_REQUIRED_ARGUMENT(434, "Missing required parameter '{}'"),
    STATIC_FLOW_CAN_NOT_BE_ABSTRACT(436, "Static flow cannot be abstract"),
    INSTANCE_METHOD_MISSING_STATIC_TYPE(438, "Instance method lacks static type"),
    MODIFYING_SYNTHETIC_FLOW(439, "{} is a synthetic flow and cannot be modified"),
    METHOD_RESOLUTION_FAILED(440, "Unable to resolve method {} with arguments {}"),
    ILLEGAL_ARGUMENT1(441, "Invalid function call argument: {}"),
    ILLEGAL_ARGUMENT2(442, "Invalid argument '{}' in flow {} for parameter {}"),
    INCORRECT_ARGUMENT_COUNT(443, "Flow {} expects {} arguments but got {}"),

    // expression
    EXPRESSION_INVALID(501, "Invalid expression: {}"),
    EXPRESSION_INVALID_VALUE(502, "Invalid expression value, expected type: {}, actual value: {}"),
    FUNCTION_ARGUMENTS_INVALID(503, "Invalid parameters for function {}"),
    INVALID_CONDITION_EXPR(504, "Invalid condition expression: {}"),
    ILLEGAL_SEARCH_CONDITION(505, "Invalid search condition"),

    // user
    AUTH_FAILED(601, "Authorization failed"),
    LOGIN_NAME_NOT_FOUND(602, "Account '{}' not found"),
    INVALID_TOKEN(603, "Login expired, please log in again"),
    USER_NOT_FOUND(604, "User (ID: {}) not found"),
    ROLE_NOT_FOUND(604, "Role (ID: {}) not found"),
    VERIFICATION_FAILED(605, "Please log in first"),
    ILLEGAL_SESSION_STATE(606, "Invalid session state"),
    NOT_A_MEMBER_OF_THE_APP(607, "User has not joined the application"),
    REENTERING_APP(608, "Please exit the current application before proceeding"),
    NOT_IN_APP(609, "Not currently in any application"),
    PLATFORM_USER_REQUIRED(610, "Please log in with a platform account first"),
    INCORRECT_VERIFICATION_CODE(611, "Incorrect verification code"),
    VERIFICATION_CODE_SENT_TOO_OFTEN(612, "Verification code sent too frequently, please try again later"),
    TOO_MANY_LOGIN_ATTEMPTS(613, "Too many login attempts, please try again later"),
    LOGIN_REQUIRED(614, "Please log in first"),

    // Constraint
    DUPLICATE_KEY(701, "Duplicate unique key '{}'"),
    CONSTRAINT_CHECK_FAILED(702, "Failed to save record '{}': {}"),
    CONSTRAINT_NOT_FOUND(703, "Constraint rule not found (ID: {})"),
    DUPLICATE_KEY2(704, "Duplicate key for index {}: {}"),


    // Job
    SCHEDULER_STATUS_ALREADY_EXISTS(801, "Job scheduler status already exists"),

    // VIEW
    LIST_VIEW_NOT_FOUND(901, "List view of type '{}' not found"),

    // Application
    CAN_NOT_EVICT_APP_OWNER(1001, "Cannot remove the application owner"),
    CURRENT_USER_NOT_APP_ADMIN(1002, "You are not an administrator and cannot perform this action"),
    CURRENT_USER_NOT_APP_OWNER(1002, "You are not the application owner and cannot perform this action"),
    NUM_ADMINS_EXCEEDS_LIMIT(610, "Number of administrators exceeds the limit"),
    ALREADY_JOINED_APP(611, "User '{}' has already joined the application"),
    INVITATION_ALREADY_ACCEPTED(612, "Invitation has already been accepted"),
    ALREADY_AN_ADMIN(613, "User '{}' is already an administrator"),
    USER_NOT_ADMIN(614, "User '{}' is not an administrator"),
    INVALID_EMAIL_ADDRESS(615, "Invalid email address"),

    // Compiler
    RAW_TYPES_NOT_SUPPORTED(1201, "Raw types are not supported"),

    // API
    INVALID_REQUEST_METHOD(1501, "Invalid request method"),
    INVALID_REQUEST_PATH(1502, "Invalid request path"),

    // DDL
    COMMIT_RUNNING(1601, "Commit is in progress"),
    MISSING_FIELD_INITIALIZER(1602, "Initializer is missing for new field {}"),
    MISSING_TYPE_CONVERTER(1603, "Type converter is missing for field {}"),
    MISSING_SUPER_INITIALIZER(1604, "Super class initializer is missing class {}"),

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
}
