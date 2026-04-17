package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEntryRecordRepository extends JpaRepository<EntryRecordEntity, Long> {
}
