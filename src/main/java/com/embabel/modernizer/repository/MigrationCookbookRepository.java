package com.embabel.modernizer.repository;

import com.embabel.modernizer.entity.MigrationCookbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MigrationCookbookRepository extends JpaRepository<MigrationCookbook, Long> {

    Optional<MigrationCookbook> findByName(String name);
}
