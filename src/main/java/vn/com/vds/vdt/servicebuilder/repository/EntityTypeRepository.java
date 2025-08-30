package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;

import java.util.Optional;

@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
    Optional<EntityType> findByName(String name);

    @Modifying
    @Query(value = "DELETE FROM entity_types WHERE entity_type_id = :entityTypeId", nativeQuery = true)
    void deleteById(@Param("entityTypeId") Long entityTypeId);
}
