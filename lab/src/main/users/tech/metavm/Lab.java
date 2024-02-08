package tech.metavm;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.EntityType;
import tech.metavm.user.LabPlatformUser;
import tech.metavm.user.LabRole;

import java.util.List;

@EntityType("实验室")
public class Lab {

    @EntityField(value = "标签", asTitle = true)
    private String label;

    @EntityFlow("创建平台用户")
    public LabPlatformUser createPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        return new LabPlatformUser(loginName, password, name, roles);
    }

}
