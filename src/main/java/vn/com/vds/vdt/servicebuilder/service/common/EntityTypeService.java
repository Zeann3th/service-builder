package vn.com.vds.vdt.servicebuilder.service.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.EntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;

public interface EntityTypeService {
    EntityType create(EntityTypeRequest request);

    EntityType update(Long id, EntityTypeRequest request);

    EntityType getEntityTypeById(Long id);

    EntityType getEntityTypeByName(String name);

    Page<EntityType> getEntityTypes(Pageable pageable);

    void deleteEntityType(Long id);
}
