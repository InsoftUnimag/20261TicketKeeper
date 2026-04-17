package com.empresa.ingreso.infrastructure.persistence.repository;

import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReaderDeviceRepository extends JpaRepository<ReaderDeviceEntity, Long> {
}
