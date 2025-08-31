package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.EntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.service.common.EntityTypeService;

@RestController
@RequestMapping("/api/v1/entity-types")
@RequiredArgsConstructor
@ResponseWrapper
@SuppressWarnings("all")
public class EntityTypeController {

    private final EntityTypeService entityTypeService;

    @GetMapping("/id/{id}")
    public EntityType getEntityTypeById(@PathVariable("id") Long id) {
        return entityTypeService.getEntityTypeById(id);
    }

    @GetMapping("/name/{name}")
    public EntityType getEntityTypeByName(@PathVariable("name") String name) {
        return entityTypeService.getEntityTypeByName(name);
    }

    @GetMapping
    public Page<EntityType> getEntityTypes(Pageable pageable) {
        return entityTypeService.getEntityTypes(pageable);
    }

    @PostMapping
    public EntityType create(@RequestBody EntityTypeRequest request) {
        return entityTypeService.create(request);
    }

    @PutMapping("/{id}")
    public EntityType update(@PathVariable("id") Long id, @RequestBody EntityTypeRequest request) {
        return entityTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        entityTypeService.deleteEntityType(id);
    }

}
