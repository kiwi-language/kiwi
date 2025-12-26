package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.flow.Function;
import org.metavm.object.type.Klass;
import java.util.LinkedList;
import org.metavm.util.Utils;

@Slf4j
public class EntityUtils {


    public static String getEntityDesc(Object entity) {
        return switch (entity) {
            case org.metavm.flow.Method method -> method.toString();
            case Function func -> func.toString();
            case Klass klass -> klass.toString();
            default -> entity.getClass().getSimpleName() + "-" + entity;
        };
    }

    public static String getEntityPath(Object entity) {
        if (entity instanceof Entity e) {
            var list = new LinkedList<Entity>();
            var e1 = e;
            while (e1 != null) {
                list.addFirst(e1);
                e1 = e1.getParentEntity();
            }
            return Utils.join(list, EntityUtils::getEntityDesc, "/");
        } else
            return getEntityDesc(entity);
    }


}
