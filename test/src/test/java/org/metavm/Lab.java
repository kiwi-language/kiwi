package org.metavm;


import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.Page;
import org.metavm.compiler.util.CompilerHttpUtils;
import org.metavm.util.TypeReference;

public class Lab {

    public static void main(String[] args) {
        CompilerHttpUtils.setAppId(2L);
        CompilerHttpUtils.setToken(2L, "5e591be2-a14f-427a-8551-9115d2a52873");
        var apps = CompilerHttpUtils.get("/app?pageSize=100", new TypeReference<Page<ApplicationDTO>>() {}).data();
        for (ApplicationDTO app : apps) {
            System.out.println(app.name());
        }
    }

}
