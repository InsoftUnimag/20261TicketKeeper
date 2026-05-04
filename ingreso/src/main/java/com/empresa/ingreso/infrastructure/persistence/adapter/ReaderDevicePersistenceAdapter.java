package com.empresa.ingreso.infrastructure.persistence.adapter;

import com.empresa.ingreso.application.port.out.LoadReaderDevicePort;
import com.empresa.ingreso.domain.model.ReaderDevice;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReaderDevicePersistenceAdapter implements LoadReaderDevicePort {

    private final SpringDataReaderDeviceRepository readerDeviceRepository;

    public ReaderDevicePersistenceAdapter(SpringDataReaderDeviceRepository readerDeviceRepository) {
        this.readerDeviceRepository = readerDeviceRepository;
    }

    @Override
    public Optional<ReaderDevice> findReaderDeviceById(Long readerId) {
        return readerDeviceRepository.findById(readerId).map(this::toDomain);
    }

    private ReaderDevice toDomain(ReaderDeviceEntity entity) {
        return new ReaderDevice(entity.getId(), entity.getGateId(), entity.getAssignedZone(), entity.isEnabled());
    }
}
