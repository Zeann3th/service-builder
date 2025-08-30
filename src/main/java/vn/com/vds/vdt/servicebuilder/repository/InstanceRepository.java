package vn.com.vds.vdt.servicebuilder.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.vds.vdt.servicebuilder.entity.Instance;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {
    @Query("SELECT i FROM Instance i JOIN EntityType et ON i.entityTypeId = et.id WHERE et.name = :entityTypeName")
    Page<Instance> findByEntityTypeName(String entityTypeName, Pageable pageable);

    @Query(value = "SELECT * FROM entities WHERE entity_type_id = :entityTypeId", nativeQuery = true)
    List<Instance> findEntitiesByEntityTypeId(@Param("entityTypeId") Long entityTypeId);

    @Modifying
    @Query(value = "DELETE FROM entities WHERE entity_id = :entityId", nativeQuery = true)
    void deleteInstanceById(@Param("entityId") Long entityId);
}
