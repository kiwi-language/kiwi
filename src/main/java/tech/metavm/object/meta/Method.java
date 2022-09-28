package tech.metavm.object.meta;

import java.util.List;

public class Method {

    private final long id;
    private final String name;
    private final long flowId;
    private List<Type> argumentTypes;
    private List<Type> resultTypes;


    public Method(long id, String name, long flowId) {
        this.id = id;
        this.name = name;
        this.flowId = flowId;
    }


    long getId() {
        return id;
    }

}
