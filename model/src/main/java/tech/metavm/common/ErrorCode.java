package tech.metavm.common;

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
    PROPERTY_NOT_FOUND(205, "属性{}不存在"),
    MULTIPLE_TITLE_FIELDS(206, "最多设置一个标题属性"),
    INVALID_SYMBOL_NAME(207, "名称'{}'不符合命名要求"),
    DUPLICATE_CHOICE_OPTION(208, "选项重复, ID: {}, 名称: {}, 序号: {}"),
    DUPLICATE_CHOICE_OPTION_PROP(209, "选项{}重复: {}"),
    ERROR_DELETING_TYPE(209, "删除失败，原因: {}"),
    TYPE_NOT_FOUND(210, "类型不存在, ID: {}"),
    INVALID_COLUMN(211, "列'{}'配置错误，原因: {}"),
    INVALID_TYPE_PATH(307, "路径不合法: {}"),
    STATIC_FIELD_CAN_NOT_BE_NULL(308, "静态字段'{}'不能为空"),
    TOO_MAY_FIELDS(309, "字段数量超出限制"),
    OVERRIDE_FLOW_CAN_NOT_ALTER_PARAMETER_TYPES(209, "复写流程不支持修改参数数量或参数类型"),
    ORDINAL_OUT_OF_BOUND(310, "属性序号超出范围"),
    CAN_NOT_ASSIGN__CHILD_FIELD(311, "子对象字段不支持更新"),
    INVALID_CODE(312, "编号'{}'不合法，请参考编号命名规范"),
    CHANGING_CATEGORY(313, "不支持修改类型类别，例如将class改为enum"),
    CHANGING_IS_TEMPLATE(314, "不支持将范型改为非范型，或将非范型类型改为范型"),
    PROPERTY_NOT_READABLE(315, "属性不可读"),
    PROPERTY_NOT_WRITABLE(316, "属性不可写"),
    TITLE_FIELD_MUST_BE_STRING(317, "标题字段必须为字符串类型"),
    CHILD_FIELD_CAN_NOT_BE_PRIMITIVE_TYPED(318, "从对象字段不支持基础类型"),

    // 实例相关错误
    INSTANCE_NOT_FOUND(301, "对象'{}'不存在"),
    INVALID_FIELD_VALUE(302, "属性值错误，属性: {}, 值: {}"),
    FIELD_REQUIRED(303, "属性{}不能为空"),
    INVALID_TYPE_VALUE(304, "数据格式错误，类型: {}, 值: {}"),
    FIELD_VALUE_REQUIRED(305, "属性'{}'不能为空"),
    STRONG_REFS_PREVENT_REMOVAL(306, "对象被其他对象关联，无法删除: {}"),
    STRONG_REFS_PREVENT_REMOVAL2(308, "'{}' 被 '{}' 关联，无法删除"),
    INVALID_INSTANCE_PATH(307, "对象路径不合法: {}"),
    INCORRECT_PARENT_REF(308, "父对象引用错误，子对象: {}, 父对象: {}, 引用值: {}"),
    MULTI_PARENT(309, "子对象归属于多个父对象: {}"),
    CONVERSION_FAILED(410, "对象'{}'不能转化为类型'{}'"),
    CAN_NOT_MODIFY_READONLY_FIELD(411, "无法修改对象只读字段"),

    // Flow相关错误
    FLOW_NOT_FOUND(401, "流程{}不存在"),
    NODE_NOT_FOUND(402, "节点 {}不存在"),
    BRANCH_NOT_FOUND(403, "分支{}不存在"),
    MISSING_END_NODE(411, "缺失流程结束节点"),
    STACK_UNDERFLOW(412, "栈下溢出"),
    ILLEGAL_ACCESS(413, "无权限访问"),
    FLOW_EXECUTION_FAILURE(414, "{}"),
    BRANCH_OWNER_MISMATCH(415, "分支({})所属节点不是当前节点({})"),
    FLOW_DECLARING_TYPE_MISMATCH(416, "流程所属类型不是当前类型({})"),
    BRANCH_INDEX_REQUIRED(417, "分支序号不能为空"),
    BRANCH_INDEX_DUPLICATE(417, "分支序号不能重复"),
    NUM_PRESELECTED_BRANCH_NOT_EQUAL_TO_ONE(417, "分支节点必须有且仅有一个默认分支"),
    BRANCH_OUTPUT_VALUE_MUST_AGREE_WITH_BRANCHES(418, "分支节点输出字段必须为每个分支设置输出值"),
    MISSING_MERGE_NODE_FIELD_VALUE(418, "合并节点存在未设置的字段"),
    NOT_AN_ARRAY_VALUE(419, "传入的数组值不合法， 数据类型不是数组"),
    INCORRECT_ELEMENT_TYPE(419, "元素值不合法， 数据类型与数组元素类型不相符"),
    INCORRECT_INDEX_VALUE(419, "索引必须是整数类型"),
    INDEX_OUT_OF_BOUND(420, "索引超出边界"),
    INTERFACE_FLOW_NOT_IMPLEMENTED(421, "'{}'未实现接口'{}'定义的'{}'流程"),
    DEST_NODE_FIELD_MISSING_SOURCE_CONFIG(422, "目标节点字段必须配置所有来源节点值"),
    ILLEGAL_TARGET_BRANCH(423, "跳转目标分支不合法"),
    NODE_FIELD_DEF_AND_FIELD_VALUE_MISMATCH(423, "节点'{}'的字段值与字段定义不匹配"),
    NOT_A_FUNCTION(424, "表达式'{}'不是函数"),
    ILLEGAL_ARGUMENT(425, "函数调用参数错误"),
    INCORRECT_FUNCTION_ARGUMENT(426, "函数'{}'调用参数错误"),
    CONFLICTING_FLOW(427, "流程签名冲突：流程名称和参数类型相同，但编号不同"),
    OVERRIDE_FLOW_RETURN_TYPE_INCORRECT(428, "复写流程返回类型不正确"),
    NOT_A_CHILD_FIELD(429, "字段'{}'不是子对象字段"),
    MASTER_FIELD_REQUIRED(430, "父对象字段必填"),
    MASTER_FIELD_SHOULD_BE_NULL(430, "父对象为数组时，不能设置父对象字段"),
    INVALID_MASTER(431, "'{}'不能作为父对象"),
    INCORRECT_FIELD_VALUE(432, "字段值'{}'赋值错误"),
    INVALID_ADD_OBJECT_CHILD(433, "节点'{}'不能作为新建记录节点的子节点"),
    FIELD_NOT_INITIALIZED(433, "{}创建失败，字段'{}'未初始化"),
    MODIFYING_READ_ONLY_ARRAY(433, "只读数组不支持修改"),
    ADD_ELEMENT_NOT_SUPPORTED(433, "当前数组不支持添加元素"),
    MISSING_REQUIRED_ARGUMENT(434, "未配置必填参数'{}'"),
    STATIC_FLOW_CAN_NOT_BE_ABSTRACT(436, "静态流程不能设置为为抽象"),
    VIEW_NODE_SOURCE_TYPE_MISMATCH(437, "视图节点源头类型不匹配"),
    INSTANCE_METHOD_MISSING_STATIC_TYPE(438, "实例方法缺少静态类型"),

    // 表达式相关错误
    EXPRESSION_INVALID(501, "表达式错误，原因: {}"),
    EXPRESSION_INVALID_VALUE(502, "表达式值错误，期望类型为：{}, 实际值为：{}"),
    FUNCTION_ARGUMENTS_INVALID(503, "函数{}参数不正确"),
    INVALID_CONDITION_EXPR(504, "条件表达式不合法: {}"),
    ILLEGAL_SEARCH_CONDITION(505, "搜索条件错误"),


    // 用户相关
    AUTH_FAILED(601, "用户名或密码错误"),
    LOGIN_NAME_NOT_FOUND(602, "账号'{}'不存在"),
    INVALID_TOKEN(603, "登陆信息过期，请重新登陆"),
    USER_NOT_FOUND(604, "用户(id:{})不存在"),
    ROLE_NOT_FOUND(604, "角色(id:{})不存在"),
    VERIFICATION_FAILED(605, "请先登录"),
    ILLEGAL_SESSION_STATE(606, "会话状态错误"),
    NOT_A_MEMBER_OF_THE_APP(607, "用户未加入应用无法进入"),
    REENTERING_APP(608, "请先退出当前应用再进行操作"),
    NOT_IN_APP(609, "当前未进入任何APP"),
    PLATFORM_USER_REQUIRED(610, "请登录平台账户再进行操作"),
    INCORRECT_VERIFICATION_CODE(611, "验证码错误"),
    VERIFICATION_CODE_SENT_TOO_OFTEN(612, "验证码发送太频繁，请稍后再试"),
    TOO_MANY_LOGIN_ATTEMPTS(613, "登录尝试次数过多，请稍后再试"),

    // Constraint
    DUPLICATE_KEY(701, "唯一属性'{}'重复"),
    CONSTRAINT_CHECK_FAILED(702, "记录'{}'保存失败: {}"),
    CONSTRAINT_NOT_FOUND(703, "约束规则不存在(id:{})"),

    // Job
    SCHEDULER_STATUS_ALREADY_EXISTS(801, "JobSchedulerStatus已经存在"),

    // VIEW
    LIST_VIEW_NOT_FOUND(901, "找不到类型'{}'的列表视图"),


    // Application
    CAN_NOT_EVICT_APP_OWNER(1001, "应用所有人无法退出应用"),
    CURRENT_USER_NOT_APP_ADMIN(1002, "您不是管理员，无法执行该操作"),
    CURRENT_USER_NOT_APP_OWNER(1002, "您不是应用所有者，无法执行该操作"),
    NUM_ADMINS_EXCEEDS_LIMIT(610, "管理员数量超出上限"),
    ALREADY_JOINED_APP(611, "用户'{}'已加入当前应用"),
    INVITATION_ALREADY_ACCEPTED(612, "邀请已经接受"),
    ALREADY_AN_ADMIN(613, "用户'{}'已经是管理员"),
    USER_NOT_ADMIN(614, "用户'{}'不是管理员"),
    INVALID_EMAIL_ADDRESS(615, "邮箱地址错误"),

    // Compiler
    RAW_TYPES_NOT_SUPPORTED(1201, "Raw classes not supported"),

    // VIEW
    INVALID_READ_VIEW_FIELD_FLOW(1401, "流程{}不能作为查询视图字段"),
    INVALID_WRITE_VIEW_FIELD_FLOW(1402, "流程{}不能作为编辑视图字段"),
    INVALID_GETTER_FLOW(1403, "字段映射读取流程不合法"),
    INVALID_SETTER_FLOW(1404, "字段映射写入流程不合法"),
    INVALID_OVERRIDDEN_MAPPING(1405, "被复写映射不合法"),
    MUTABLE_TARGET_FIELD_FROM_READONLY_SOURCE(1406, "源头字段为只读字段，视图字段必须为只读"),
    FIELD_NOT_SEARCHABLE(1407, "字段'{}'不支持搜索"),
    INVALID_ELEMENT_MAPPING(1408, "无效的数组元素映射"),
    INVALID_SOURCE_MAPPING(1409, "无效的源头映射"),
    INCORRECT_ARRAY_MAPPING_ARGUMENTS(1410, "数组映射参数错误"),
    INCORRECT_MAPPING(1411, "映射配置错误")
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
