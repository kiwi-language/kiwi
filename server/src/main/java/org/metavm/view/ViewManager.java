package org.metavm.view;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.metavm.entity.*;
import org.metavm.object.type.Klass;
import org.metavm.object.view.FieldsObjectMapping;
import org.metavm.object.view.MappingSaver;
import org.metavm.object.view.rest.dto.ObjectMappingDTO;
import org.metavm.util.NncUtils;
import org.metavm.view.rest.dto.ListViewDTO;

import java.util.List;

import static org.metavm.view.ListView.IDX_TYPE_PRIORITY;

@Component
public class ViewManager extends EntityContextFactoryAware {

    public ViewManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public String getListViewTypeId() {
        return ModelDefRegistry.getTypeId(ListView.class);
    }

    public ListViewDTO getDefaultListView(String typeId) {
        try (IEntityContext context = newContext()) {
            Klass type = context.getKlass(typeId);
            List<ListView> views = context.query(
                    IDX_TYPE_PRIORITY.newQueryBuilder()
                            .from(new EntityIndexKey(List.of(type, 0)))
                            .to(new EntityIndexKey(List.of(type, Long.MAX_VALUE)))
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
            mapping.getSourceKlass().removeMapping(mapping);
        }
    }

    @Transactional
    public void setDefaultMapping(String id) {
        try (var context = newContext()) {
            context.getEntity(FieldsObjectMapping.class, id).setDefault();
        }
    }
}
