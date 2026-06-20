package vn.com.vds.vdt.servicebuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.vds.vdt.servicebuilder.entity.RelationshipType;

import java.util.Optional;

@Repository
public interface RelationshipTypeRepository extends JpaRepository<RelationshipType, Long> {
    Optional<RelationshipType> findByFromEntityTypeIdAndToEntityTypeId(Long fromEntityId, Long toEntityId);

    Optional<RelationshipType> findByName(String name);
}
