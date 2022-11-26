package tech.metavm.user.persistence;

import tech.metavm.entity.EntityPO;

import java.util.List;

public class UserPO extends EntityPO {

    private String name;
    private String loginName;
    private String password;
    private List<Long> roleIds;

    public UserPO(Long id, Long tenantId, String name, String loginName, String password, List<Long> roleIds) {
        super(id, tenantId);
        this.name = name;
        this.loginName = loginName;
        this.password = password;
        this.roleIds = roleIds;
    }

    public UserPO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
