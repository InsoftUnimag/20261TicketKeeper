package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.ReaderDevice;
import java.util.Optional;

public interface LoadReaderDevicePort {

    Optional<ReaderDevice> findReaderDeviceById(Long readerId);
}
