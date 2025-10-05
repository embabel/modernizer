package com.embabel.modernizer.repository;

import com.embabel.modernizer.entity.MigrationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MigrationJobRepository extends JpaRepository<MigrationJob, Long> {

    Optional<MigrationJob> findByJobId(String jobId);
}
