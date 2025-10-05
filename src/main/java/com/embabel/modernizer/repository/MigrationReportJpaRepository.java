package com.embabel.modernizer.repository;

import com.embabel.modernizer.entity.MigrationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MigrationReportJpaRepository extends JpaRepository<MigrationReport, Long> {

    List<MigrationReport> findByMigrationPointId(Long migrationPointId);
}
