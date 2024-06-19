package org.metavm.manufacturing.material;

import org.metavm.api.Component;
import org.metavm.manufacturing.material.dto.MaterialDTO;

@Component
public class MaterialService {

    private final CurrentUserProvider currentUserProvider;

    public MaterialService(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    public Object save(MaterialDTO materialDTO) {
        var user = currentUserProvider.get();
        if(materialDTO.id() != null) {
            var material = (Material) materialDTO.id();
            if(material.getOwner() != user)
                throw new IllegalStateException("Illegal access");
            material.setName(materialDTO.name());
            material.setUnit(materialDTO.unit());
            material.setStorageValidityPeriod(materialDTO.storageValidPeriod());
            material.setStorageValidityPeriodUnit(materialDTO.storageValidPeriodUnit());
            return material;
        }
        else {
            return new Material(
                    materialDTO.code(),
                    materialDTO.name(),
                    materialDTO.kind(),
                    materialDTO.unit(),
                    materialDTO.storageValidPeriod(),
                    materialDTO.storageValidPeriodUnit(),
                    user
            );
        }
    }

}
