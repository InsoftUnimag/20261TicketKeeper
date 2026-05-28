package com.empresa.ingreso.infrastructure.persistence.entity;

import com.empresa.ingreso.domain.model.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String allowedZone;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private boolean used;

    @Column(unique = true)
    private String externalTicketId;

    private String externalEventId;

    private String seatNumber;

    @Column(nullable = false)
    private boolean reEntryAllowed;
}
