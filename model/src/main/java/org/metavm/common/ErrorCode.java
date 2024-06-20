package org.metavm.common;

public enum ErrorCode {

    SUCCESS(0, "Success"),
    RECORD_NOT_FOUND(101, "Record not found"),
    EMPTY_REQUEST(102,"Request parameters are empty"),
    INVALID_PARAMETERS(103, "Request parameters error: {}"),

    // metadata
    INVALID_TYPE(201, "Failed to save class '{}': {}"),
    INVALID_FIELD(202, "Failed to save field '{}': {}"),
    FAILED_TO_DELETE_KLASS(203, "Failed to delete class '{}': {}"),
    INVALID_DEFAULT_VALUE(204, "Invalid field default value: {}"),
    PROPERTY_NOT_FOUND(205, "Attribute {} does not exist"),
    MULTIPLE_TITLE_FIELDS(206, "At most one title field can be set"),
    INVALID_SYMBOL_NAME(207, "Name '{}' does not meet naming requirements"),
    DUPLICATE_CHOICE_OPTION(208, "Option repeated, ID: {}, Name: {}, Serial number: {}"),
    DUPLICATE_CHOICE_OPTION_PROP(209, "Option {} repeated: {}"),
    ERROR_DELETING_TYPE(209, "Deletion failed, reason: {}"),
    TYPE_NOT_FOUND(210, "Type does not exist, ID: {}"),
    INVALID_COLUMN(211, "Column '{}' configuration error, reason: {}"),
    INVALID_TYPE_PATH(307, "Path is illegal: {}"),
    STATIC_FIELD_CAN_NOT_BE_NULL(308, "Static field '{}' cannot be null"),
    TOO_MAY_FIELDS(309, "Field quantity exceeds limit"),
    OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES(209, "Overriding flow does not support modifying parameter quantity or parameter type"),
    ORDINAL_OUT_OF_BOUND(310, "Attribute sequence number out of range"),
    CAN_NOT_ASSIGN__CHILD_FIELD(311, "Child object field does not support update"),
    INVALID_CODE(312, "Number '{}' is illegal, please refer to the number naming specification"),
    CHANGING_CATEGORY(313, "Does not support modifying type category, for example, changing class to enum"),
    CHANGING_IS_TEMPLATE(314, "Does not support changing generic to non-generic, or changing non-generic type to generic"),
    PROPERTY_NOT_READABLE(315, "Attribute is not readable"),
    PROPERTY_NOT_WRITABLE(316, "Attribute is not writable"),
    TITLE_FIELD_MUST_BE_STRING(317, "Title field must be of string type"),
    CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED(318, "From object field does not support basic type"),
    ENTITY_STRUCT_LACKS_CANONICAL_CONSTRUCTOR(319, "Entity structure {} lacks canonical constructor"),
    CLASS_NOT_FOUND(320, "Class '{}' not found"),

    // instance
    INSTANCE_NOT_FOUND(301, "Object '{}' does not exist"),
    INVALID_FIELD_VALUE(302, "Attribute value error, attribute: {}, value: {}"),
    FIELD_REQUIRED(303, "Attribute {} cannot be null"),
    INVALID_TYPE_VALUE(304, "Data format error, type: {}, value: {}"),
    FIELD_VALUE_REQUIRED(305, "Attribute '{}' cannot be null"),
    STRONG_REFS_PREVENT_REMOVAL(306, "Object is referenced by other objects, cannot be deleted: {}"),
    STRONG_REFS_PREVENT_REMOVAL2(308, "'{}' is associated with '{}', cannot be deleted"),
    INVALID_INSTANCE_PATH(307, "Object path is illegal: {}"),
    INCORRECT_PARENT_REF(308, "Parent object reference error, child object: {}, parent object: {}, reference value: {}"),
    MULTI_PARENT(309, "Child object belongs to multiple parent objects: {}"),
    CONVERSION_FAILED(410, "Object '{}' cannot be converted to type '{}'"),
    CAN_NOT_MODIFY_READONLY_FIELD(411, "Cannot modify object read-only field"),
    TYPE_CAST_ERROR(412, "Type conversion error, original type: {}, target type: {}"),
    NOT_A_PHYSICAL_INSTANCE(413, "Object '{}' is not a physical object"),
    NOT_A_CLASS_INSTANCE(414, "Object '{}' is not a class object"),
    DELETE_NON_DURABLE_INSTANCE(415, "Cannot delete non-persistent object"),
    INCORRECT_INSTANCE_FIELD_VALUE(416, "Field '{}' is illegal, reason:{}"),
    FAILED_TO_RESOLVE_VALUE(417, "Failed to resolve value: {}"),
    FAILED_TO_RESOLVE_VALUE_OF_TYPE(418, "Failed to resolve value of type {}"),
    FAILED_TO_FORMAT_VALUE(419, "Failed to format value {}"),

    // Flow
    FLOW_NOT_FOUND(401, "Flow {} does not exist"),
    NODE_NOT_FOUND(402, "Node {} does not exist"),
    BRANCH_NOT_FOUND(403, "Branch {} does not exist"),
    MISSING_END_NODE(411, "Missing flow end node"),
    STACK_UNDERFLOW(412, "Stack underflow"),
    ILLEGAL_ACCESS(413, "No access permission"),
    FLOW_EXECUTION_FAILURE(414, "{}"),
    BRANCH_OWNER_MISMATCH(415, "Branch ({}) owner node is not the current node ({})"),
    FLOW_DECLARING_TYPE_MISMATCH(416, "Flow owner type is not the current type ({})"),
    BRANCH_INDEX_REQUIRED(417, "Branch sequence number cannot be null"),
    BRANCH_INDEX_DUPLICATE(417, "Branch sequence number cannot be repeated"),
    NUM_PRESELECTED_BRANCH_NOT_EQUAL_TO_ONE(417, "Branch node must have and only have one default branch"),
    BRANCH_OUTPUT_VALUE_MUST_AGREE_WITH_BRANCHES(418, "Branch node output field must set output value for each branch"),
    MISSING_MERGE_NODE_FIELD_VALUE(418, "Merge node has unset fields"),
    NOT_AN_ARRAY_VALUE(419, "The incoming array value is illegal, the data type is not an array"),
    INCORRECT_ELEMENT_TYPE(419, "Array type '{}' does not match array element type '{}'"),
    INCORRECT_INDEX_VALUE(419, "Index must be of integer type"),
    INDEX_OUT_OF_BOUND(420, "Index out of bounds"),
    INTERFACE_FLOW_NOT_IMPLEMENTED(421, "'{}' has not implemented the '{}' flow defined by the interface"),
    DEST_NODE_FIELD_MISSING_SOURCE_CONFIG(422, "Target node field must configure all source node values"),
    ILLEGAL_TARGET_BRANCH(423, "Jump target branch is illegal"),
    NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH(423, "Node '{}' field value does not match field definition"),
    NOT_A_FUNCTION(424, "Expression '{}' is not a function"),
    ILLEGAL_ARGUMENT(425, "Function call argument error"),
    ILLEGAL_FUNCTION_ARGUMENT(425, "Flow '{}' call argument error, formal parameter type: {}, actual parameter type: {}"),
    INCORRECT_FUNCTION_ARGUMENT(426, "Function '{}' call argument error"),
    CONFLICTING_FLOW(427, "Flow signature conflict: flow name and parameter type are the same, but the number is different"),
    OVERRIDE_FLOW_RETURN_TYPE_INCORRECT(428, "Overriding flow return type is incorrect, current flow return type: {}, overridden flow return type: {}"),
    NOT_A_CHILD_FIELD(429, "Field '{}' is not a child object field"),
    MASTER_FIELD_REQUIRED(430, "Parent object field must be filled"),
    MASTER_FIELD_SHOULD_BE_NULL(430, "When the parent object is an array, the parent object field cannot be set"),
    INVALID_MASTER(431, "'{}' cannot be used as a parent object"),
    INCORRECT_FIELD_VALUE(432, "Field value '{}' assignment error"),
    INVALID_ADD_OBJECT_CHILD(433, "Node '{}' cannot be used as a new record node's child node"),
    FIELD_NOT_INITIALIZED(433, "{} creation failed, field '{}' not initialized"),
    MODIFYING_READ_ONLY_ARRAY(433, "Read-only array does not support modification"),
    ADD_ELEMENT_NOT_SUPPORTED(433, "Current array does not support adding elements"),
    MISSING_REQUIRED_ARGUMENT(434, "Unconfigured required parameter '{}'"),
    STATIC_FLOW_CAN_NOT_BE_ABSTRACT(436, "Static flow cannot be set as abstract"),
    VIEW_NODE_SOURCE_TYPE_MISMATCH(437, "View node source type mismatch"),
    INSTANCE_METHOD_MISSING_STATIC_TYPE(438, "Instance method lacks static type"),
    MODIFYING_SYNTHETIC_FLOW(439, "{} is a synthetic flow, cannot be modified"),
    METHOD_RESOLUTION_FAILED(440, "Can not resolve method {}"),

    // expression
    EXPRESSION_INVALID(501, "Expression error, reason: {}"),
    EXPRESSION_INVALID_VALUE(502, "Expression value error, expected type: {}, actual value: {}"),
    FUNCTION_ARGUMENTS_INVALID(503, "Function {} parameters are incorrect"),
    INVALID_CONDITION_EXPR(504, "Condition expression is illegal: {}"),
    ILLEGAL_SEARCH_CONDITION(505, "Search condition error"),


    // user
    AUTH_FAILED(601, "Authorization failed"),
    LOGIN_NAME_NOT_FOUND(602, "Account '{}' does not exist"),
    INVALID_TOKEN(603, "Login information expired, please log in again"),
    USER_NOT_FOUND(604, "User (id:{}) does not exist"),
    ROLE_NOT_FOUND(604, "Role (id:{}) does not exist"),
    VERIFICATION_FAILED(605, "Please log in first"),
    ILLEGAL_SESSION_STATE(606, "Session status error"),
    NOT_A_MEMBER_OF_THE_APP(607, "User has not joined the application and cannot enter"),
    REENTERING_APP(608, "Please exit the current application before operating"),
    NOT_IN_APP(609, "Currently not in any APP"),
    PLATFORM_USER_REQUIRED(610, "Please log in to the platform account before operating"),
    INCORRECT_VERIFICATION_CODE(611, "Verification code error"),
    VERIFICATION_CODE_SENT_TOO_OFTEN(612, "Verification code is sent too frequently, please try again later"),
    TOO_MANY_LOGIN_ATTEMPTS(613, "Too many login attempts, please try again later"),
    LOGIN_REQUIRED(614, "Please log in first"),

    // Constraint
    DUPLICATE_KEY(701, "Unique attribute '{}' repeated"),
    CONSTRAINT_CHECK_FAILED(702, "Record '{}' save failed: {}"),
    CONSTRAINT_NOT_FOUND(703, "Constraint rule does not exist (id:{})"),

    // Job
    SCHEDULER_STATUS_ALREADY_EXISTS(801, "JobSchedulerStatus already exists"),

    // VIEW
    LIST_VIEW_NOT_FOUND(901, "Cannot find list view of type '{}'"),


    // Application
    CAN_NOT_EVICT_APP_OWNER(1001, "Application owner cannot exit the application"),
    CURRENT_USER_NOT_APP_ADMIN(1002, "You are not an administrator, cannot perform this operation"),
    CURRENT_USER_NOT_APP_OWNER(1002, "You are not the application owner, cannot perform this operation"),
    NUM_ADMINS_EXCEEDS_LIMIT(610, "Number of administrators exceeds limit"),
    ALREADY_JOINED_APP(611, "User '{}' has already joined the current application"),
    INVITATION_ALREADY_ACCEPTED(612, "Invitation has already been accepted"),
    ALREADY_AN_ADMIN(613, "User '{}' is already an administrator"),
    USER_NOT_ADMIN(614, "User '{}' is not an administrator"),
    INVALID_EMAIL_ADDRESS(615, "Email address error"),

    // Compiler
    RAW_TYPES_NOT_SUPPORTED(1201, "Raw classes not supported"),

    // VIEW
    INVALID_READ_VIEW_FIELD_FLOW(1401, "Flow {} cannot be used as a query view field"),
    INVALID_WRITE_VIEW_FIELD_FLOW(1402, "Flow {} cannot be used as an edit view field"),
    INVALID_GETTER_FLOW(1403, "Field mapping read flow is illegal"),
    INVALID_SETTER_FLOW(1404, "Field mapping write flow is illegal"),
    INVALID_OVERRIDDEN_MAPPING(1405, "Overridden mapping is illegal"),
    MUTABLE_TARGET_FIELD_FROM_READONLY_SOURCE(1406, "Source field is read-only, view field must be read-only"),
    FIELD_NOT_SEARCHABLE(1407, "Field '{}' does not support search"),
    INVALID_ELEMENT_MAPPING(1408, "Invalid array element mapping"),
    INVALID_SOURCE_MAPPING(1409, "Invalid source mapping"),
    INCORRECT_ARRAY_MAPPING_ARGUMENTS(1410, "Array mapping parameters error"),
    INCORRECT_MAPPING(1411, "Mapping configuration error"),
    DEFAULT_VIEW_NOT_FOUND(1412, "Cannot find default view"),
    FAIL_TO_SAVE_VIEW(1413, "Save view failed: {}"),

    // API
    INVALID_REQUEST_METHOD(1501, "Invalid request method"),
    INVALID_REQUEST_PATH(1502, "Invalid request path"),


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