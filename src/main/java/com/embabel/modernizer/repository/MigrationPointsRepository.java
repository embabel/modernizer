package com.embabel.modernizer.repository;

import com.embabel.modernizer.entity.MigrationPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigrationPointsRepository extends JpaRepository<MigrationPoints, Long> {

    List<MigrationPoints> findByMigrationJobId(Long migrationJobId);
}
