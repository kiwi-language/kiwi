package tech.metavm.dto;

public enum ErrorCode {

    SUCCESS(0, "成功"),
    RECORD_NOT_FOUND(101, "记录不存在"),
    EMPTY_REQUEST(102,"请求参数为空"),
    INVALID_PARAMETERS(103, "请求参数错误：{}"),

    // 元数据相关错误
    INVALID_TYPE(201, "对象'{}'配置错误，原因: {}"),
    INVALID_FIELD(202, "属性'{}'配置错误，原因: {}"),
    DELETE_N_CLASS_ERROR(203, "对象'{}'删除失败，原因: {}"),
    INVALID_DEFAULT_VALUE(204, "属性默认值错误: {}"),
    FIELD_NOT_FOUND(205, "属性不存在, ID: {}"),
    MULTIPLE_TITLE_FIELDS(206, "最多设置一个标题属性"),
    INVALID_SYMBOL_NAME(207, "名称{}不符合要求"),
    DUPLICATE_CHOICE_OPTION(208, "选项重复, ID: {}, 名称: {}, 序号: {}"),
    DUPLICATE_CHOICE_OPTION_PROP(209, "选项{}重复: {}"),
    ERROR_DELETING_TYPE(209, "删除失败，原因: {}"),
    TYPE_NOT_FOUND(210, "类型不存在, ID: {}"),
    INVALID_COLUMN(211, "列'{}'配置错误，原因: {}"),

    // 实例相关错误
    INSTANCE_NOT_FOUND(301, "实例不存在(objectId: {})"),
    INVALID_FIELD_VALUE(302, "属性值错误，属性: {}, 值: {}"),
    FIELD_REQUIRED(303, "属性{}不能为空"),
    INVALID_TYPE_VALUE(304, "数据格式错误，类型: {}, 值: {}"),


    // Flow相关错误
    FLOW_NOT_FOUND(401, "流程{}不存在"),
    NODE_NOT_FOUND(402, "节点 {}不存在"),
    BRANCH_NOT_FOUND(403, "分支{}不存在"),
    MISSING_END_NODE(411, "缺失流程结束节点"),
    STACK_UNDERFLOW(412, "栈下溢出"),
    ILLEGAL_ACCESS(413, "无权限方案"),
    FLOW_EXECUTION_FAILURE(414, "流程执行失败: {}"),

    // 表达式相关错误
    EXPRESSION_INVALID(501, "表达式错误，原因: {}"),
    EXPRESSION_INVALID_VALUE(502, "表达式值错误，期望类型为：{}, 实际值微：{}"),
    FUNCTION_ARGUMENTS_INVALID(503, "函数{}参数不正确"),


    // 用户相关
    AUTH_FAILED(601, "用户名或密码错误"),
    LOGIN_NAME_NOT_FOUND(602, "账号'{}'不存在"),
    INVALID_TOKEN(603, "登陆信息过期，请重新登陆"),
    USER_NOT_FOUND(604, "用户(id:{})不存在"),
    ROLE_NOT_FOUND(604, "角色(id:{})不存在"),
    VERIFICATION_FAILED(605, "请先登录"),


    // Constraint
    DUPLICATE_KEY(701, "唯一属性'{}'重复"),
    CONSTRAINT_CHECK_FAILED(702, "记录'{}'约束条件'{}'校验不通过"),


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
