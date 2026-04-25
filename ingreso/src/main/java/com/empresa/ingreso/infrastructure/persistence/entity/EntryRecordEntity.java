package com.empresa.ingreso.infrastructure.persistence.entity;

import com.empresa.ingreso.domain.model.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "entry_records")
public class EntryRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long gateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType;

    @Column(nullable = false)
    private OffsetDateTime enteredAt;
}
