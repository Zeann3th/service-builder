package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    Optional<AttributeDefinition> findByNameAndEntityTypeId(String name, Long entityTypeId);

    List<AttributeDefinition> findByEntityTypeId(Long entityTypeId);

    @Modifying
    @Query(value = "DELETE FROM attribute_definitions where entity_type_id = :entityTypeId", nativeQuery = true)
    void deleteByEntityTypeId(@Param("entityTypeId") Long entityTypeId);
}
