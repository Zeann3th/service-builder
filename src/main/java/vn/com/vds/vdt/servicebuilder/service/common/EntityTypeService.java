package vn.com.vds.vdt.servicebuilder.service.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.CreateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.UpdateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;

public interface EntityTypeService {
    EntityType create(CreateEntityTypeRequest request);

    EntityType update(UpdateEntityTypeRequest request);

    EntityType getEntityTypeById(Long id);

    EntityType getEntityTypeByName(String name);

    Page<EntityType> getEntityTypes(Pageable pageable);

    void deleteEntityType(Long id);
}
