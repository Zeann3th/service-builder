package vn.com.vds.vdt.servicebuilder.service.common.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.CreateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.UpdateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.entity.Instance;
import vn.com.vds.vdt.servicebuilder.exception.CommandExceptionBuilder;
import vn.com.vds.vdt.servicebuilder.repository.AttributeDefinitionRepository;
import vn.com.vds.vdt.servicebuilder.repository.AttributeValueRepository;
import vn.com.vds.vdt.servicebuilder.repository.EntityTypeRepository;
import vn.com.vds.vdt.servicebuilder.repository.InstanceRepository;
import vn.com.vds.vdt.servicebuilder.service.common.EntityTypeService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class EntityTypeServiceImpl implements EntityTypeService {

    private final EntityTypeRepository entityTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final InstanceRepository instanceRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Transactional
    @Override
    public EntityType create(CreateEntityTypeRequest request) {
        String name = request.getName().toLowerCase();
        Optional<EntityType> optionalEntityType = entityTypeRepository.findByName(name);
        if (optionalEntityType.isPresent()) {
            throw CommandExceptionBuilder.exception("QS00005", "Entity type name already exists: " + name);
        }

        EntityType entityType = EntityType.builder()
                .name(name)
                .displayName(request.getDisplayName())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .schemaVersion(1L)
                .build();

        EntityType savedEntityType = entityTypeRepository.save(entityType);

        if (request.getAttributes() != null) {
            for (var attr : request.getAttributes()) {
                String attrName = attr.getName().toLowerCase();
                AttributeDefinition def = AttributeDefinition.builder()
                        .entityTypeId(savedEntityType.getEntityTypeId())
                        .name(attrName)
                        .displayName(attr.getDisplayName())
                        .dataType(attr.getDataType())
                        .isRequired(Boolean.TRUE.equals(attr.getIsRequired()))
                        .defaultValue(attr.getDefaultValue())
                        .validationRules(
                                attr.getValidationRules() != null ? attr.getValidationRules() : new HashMap<>())
                        .build();
                attributeDefinitionRepository.save(def);
            }
        }

        return savedEntityType;
    }

    @Transactional
    @Override
    public EntityType update(UpdateEntityTypeRequest request) {
        EntityType entityType = entityTypeRepository.findById(request.getEntityTypeId())
                .orElseThrow(() -> CommandExceptionBuilder.exception("QS00004",
                        "Entity type not found: " + request.getEntityTypeId()));

        entityType.setDisplayName(request.getDisplayName());
        entityType.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        entityType.setSchemaVersion(entityType.getSchemaVersion() + 1L);

        EntityType savedEntityType = entityTypeRepository.save(entityType);

        if (request.getAttributes() != null) {
            for (var attr : request.getAttributes()) {
                String attrName = attr.getName().toLowerCase();
                AttributeDefinition def = attributeDefinitionRepository
                        .findByNameAndEntityTypeId(attrName, savedEntityType.getEntityTypeId())
                        .orElseGet(() -> AttributeDefinition.builder()
                                .entityTypeId(savedEntityType.getEntityTypeId())
                                .name(attrName)
                                .build());

                def.setDisplayName(attr.getDisplayName());
                def.setDataType(attr.getDataType());
                def.setIsRequired(Boolean.TRUE.equals(attr.getIsRequired()));
                def.setDefaultValue(attr.getDefaultValue());
                def.setValidationRules(attr.getValidationRules() != null ? attr.getValidationRules() : new HashMap<>());
                attributeDefinitionRepository.save(def);
            }
        }

        return savedEntityType;
    }

    @Override
    public EntityType getEntityTypeById(Long id) {
        EntityType entityType = entityTypeRepository.findById(id).orElseThrow(
                () -> CommandExceptionBuilder.exception(ErrorCodes.QS00004, "Entity type not found: " + id));
        return entityType;
    }

    @Override
    public EntityType getEntityTypeByName(String name) {
        return entityTypeRepository.findByName(name).orElseThrow(
                () -> CommandExceptionBuilder.exception(ErrorCodes.QS00004, "Entity type not found: " + name));
    }

    @Override
    public Page<EntityType> getEntityTypes(Pageable pageable) {
        Page<EntityType> page = entityTypeRepository.findAll(pageable);
        return page;
    }

    @Transactional
    @Override
    public void deleteEntityType(Long id) {
        // Get all entities in the Entities table with that entityTypeId
        List<Instance> instances = instanceRepository.findEntitiesByEntityTypeId(id);

        // delete all rows related to that EntityType in attribute_values table
        for (Instance instance : instances) {
            attributeValueRepository.deleteByEntityId(instance.getEntityId());
        }

        for (Instance instance : instances) {
            instanceRepository.deleteInstanceById(instance.getEntityId());
        }

        attributeDefinitionRepository.deleteByEntityTypeId(id);

        entityTypeRepository.deleteById(id);

    }
}
