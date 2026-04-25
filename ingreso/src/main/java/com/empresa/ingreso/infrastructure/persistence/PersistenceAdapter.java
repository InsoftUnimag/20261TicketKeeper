package com.empresa.ingreso.infrastructure.persistence;

import com.empresa.ingreso.application.port.out.LoadEventSessionPort;
import com.empresa.ingreso.application.port.out.LoadReaderDevicePort;
import com.empresa.ingreso.application.port.out.LoadTicketPort;
import com.empresa.ingreso.application.port.out.SaveAccessAttemptPort;
import com.empresa.ingreso.application.port.out.SaveEntryRecordPort;
import com.empresa.ingreso.application.port.out.SaveTicketPort;
import com.empresa.ingreso.domain.model.AccessAttempt;
import com.empresa.ingreso.domain.model.EntryRecord;
import com.empresa.ingreso.domain.model.EventSession;
import com.empresa.ingreso.domain.model.ReaderDevice;
import com.empresa.ingreso.domain.model.Ticket;
import com.empresa.ingreso.infrastructure.persistence.entity.AccessAttemptEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EntryRecordEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.EventSessionEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.ReaderDeviceEntity;
import com.empresa.ingreso.infrastructure.persistence.entity.TicketEntity;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataAccessAttemptRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEventSessionRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataReaderDeviceRepository;
import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataTicketRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PersistenceAdapter implements

    private final SpringDataReaderDeviceRepository readerDeviceRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataEventSessionRepository eventSessionRepository;
    private final SpringDataAccessAttemptRepository accessAttemptRepository;
    private final SpringDataEntryRecordRepository entryRecordRepository;

    public PersistenceAdapter(
            SpringDataReaderDeviceRepository readerDeviceRepository,
            SpringDataTicketRepository ticketRepository,
            SpringDataEventSessionRepository eventSessionRepository,
            SpringDataAccessAttemptRepository accessAttemptRepository,
    ) {
        this.readerDeviceRepository = readerDeviceRepository;
        this.ticketRepository = ticketRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.accessAttemptRepository = accessAttemptRepository;
        this.entryRecordRepository = entryRecordRepository;
    }


        TicketEntity entity = new TicketEntity();
        return toDomain(ticketRepository.save(entity));
    }

        AccessAttemptEntity entity = new AccessAttemptEntity();
        return toDomain(accessAttemptRepository.save(entity));
    }

        EntryRecordEntity entity = new EntryRecordEntity();
    }


    }

    }

}
