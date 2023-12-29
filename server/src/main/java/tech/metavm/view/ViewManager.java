package tech.metavm.view;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.view.DefaultObjectMapping;
import tech.metavm.object.view.MappingSaver;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.view.rest.dto.ListViewDTO;

import java.util.List;

import static tech.metavm.view.ListView.IDX_TYPE_PRIORITY;

@Component
public class ViewManager extends EntityContextFactoryBean {

    public ViewManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
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

    @Transactional
    public Long saveMapping(ObjectMappingDTO viewMapping) {
        try (var context = newContext()) {
            var mapping = MappingSaver.create(context).save(viewMapping);
            context.finish();
            return mapping.getIdRequired();
        }
    }

    @Transactional
    public void removeMapping(long id) {
        try (var context = newContext()) {
            var mapping = context.getEntity(DefaultObjectMapping.class, id);
            mapping.getSourceType().removeMapping(mapping);
        }
    }

    @Transactional
    public void setDefaultMapping(long id) {
        try (var context = newContext()) {
            context.getEntity(DefaultObjectMapping.class, id).setDefault();
        }
    }
}
