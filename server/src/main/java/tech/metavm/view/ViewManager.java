package tech.metavm.view;

import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.NncUtils;
import tech.metavm.view.rest.dto.ListViewDTO;

import java.util.List;

import static tech.metavm.view.ListView.IDX_TYPE_PRIORITY;

@Component
public class ViewManager extends InstanceContextFactoryAware {

    public ViewManager(InstanceContextFactory instanceContextFactory) {
        super(instanceContextFactory);
    }

    public Long getListViewTypeId() {
        return ModelDefRegistry.getTypeId(ListView.class);
    }

    public ListViewDTO getDefaultListView(long typeId) {
        try (IEntityContext context = newContext()) {
            ClassType type = context.getClassType(typeId);
            List<ListView> views = context.query(
                    IDX_TYPE_PRIORITY.newQueryBuilder()
                            .addEqItem(0, type)
                            .addGeItem(1, 0)
                            .limit(1)
                            .build()
            );
            if (NncUtils.isEmpty(views)) {
                return null;
            }
            return views.get(0).toDTO();
        }
    }

}
