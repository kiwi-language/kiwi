package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(value = "异常测试", compiled = true)
public class AstExceptionFoo extends Entity {

    @EntityField("数量")
    private int amount;

    @Nullable
    @EntityField("错误信息")
    public String errorMessage;

    @EntityField("执行次数")
    public int executionCount;

    public void test(int dec) {
        try {
            if (dec <= 0) {
                throw new RuntimeException("参数错误");
            }
            if (dec > amount) {
                throw new AstException();
            }
            amount -= dec;
            errorMessage = null;
        } catch (AstException e) {
            var message = e.getMessage();
            if (message != null) {
                errorMessage = message;
            } else {
                errorMessage = "执行失败";
            }
        } finally {
            executionCount++;
        }
    }

}
