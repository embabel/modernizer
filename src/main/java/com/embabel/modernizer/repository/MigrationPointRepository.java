package com.embabel.modernizer.repository;

import com.embabel.modernizer.entity.MigrationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigrationPointRepository extends JpaRepository<MigrationPoint, Long> {

    List<MigrationPoint> findByMigrationPointsId(Long migrationPointsId);
}
