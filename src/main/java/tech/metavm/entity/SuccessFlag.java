package tech.metavm.entity;

@EntityType("结果")
public record SuccessFlag(@EntityField("是否成功") boolean successful) {

}
