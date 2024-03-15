package tech.metavm.view;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.view.FieldsObjectMapping;
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

    public String getListViewTypeId() {
        return ModelDefRegistry.getTypeId(ListView.class);
    }

    public ListViewDTO getDefaultListView(String typeId) {
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
    public String saveMapping(ObjectMappingDTO viewMapping) {
        try (var context = newContext()) {
            var mapping = MappingSaver.create(context).save(viewMapping);
            context.finish();
            return mapping.getStringId();
        }
    }

    @Transactional
    public void removeMapping(String id) {
        try (var context = newContext()) {
            var mapping = context.getEntity(FieldsObjectMapping.class, id);
            mapping.getSourceType().removeMapping(mapping);
        }
    }

    @Transactional
    public void setDefaultMapping(String id) {
        try (var context = newContext()) {
            context.getEntity(FieldsObjectMapping.class, id).setDefault();
        }
    }
}
