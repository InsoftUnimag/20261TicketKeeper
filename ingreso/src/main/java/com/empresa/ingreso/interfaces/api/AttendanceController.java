package com.empresa.ingreso.interfaces.api;

import com.empresa.ingreso.infrastructure.persistence.repository.SpringDataEntryRecordRepository;
import com.empresa.ingreso.interfaces.api.dto.AttendanceDetailResponse;
import com.empresa.ingreso.interfaces.api.dto.AttendanceSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/events/{eventId}/attendance")
@Tag(name = "Consultas de Asistencia", description = "Endpoints para consultar la asistencia a eventos")
public class AttendanceController {

    private final SpringDataEntryRecordRepository entryRecordRepository;

    public AttendanceController(SpringDataEntryRecordRepository entryRecordRepository) {
        this.entryRecordRepository = entryRecordRepository;
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener un resumen de la asistencia al evento")
    public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(@PathVariable String eventId) {
        long totalValidated = entryRecordRepository.countDistinctTicketsByEventId(eventId);
        var response = new AttendanceSummaryResponse(eventId, totalValidated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets")
    @Operation(summary = "Obtener el detalle de tickets asistentes al evento")
    public ResponseEntity<AttendanceDetailResponse> getAttendanceDetail(@PathVariable String eventId) {
        var attendingTickets = entryRecordRepository.findAttendingTicketsByEventId(eventId);

        var ticketDtos = attendingTickets.stream()
                .map(info -> new AttendanceDetailResponse.AttendingTicketDto(
                        info.externalTicketId(),
                        "ASISTIÓ",
                        info.firstEntryTime()
                ))
                .collect(Collectors.toList());

        var response = new AttendanceDetailResponse(eventId, ticketDtos);
        return ResponseEntity.ok(response);
    }
}
