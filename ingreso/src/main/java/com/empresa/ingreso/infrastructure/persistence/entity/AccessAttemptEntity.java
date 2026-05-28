package com.empresa.ingreso.infrastructure.persistence.entity;

import com.empresa.ingreso.domain.model.AccessChannel;
import com.empresa.ingreso.domain.model.AttemptResult;
import com.empresa.ingreso.shared.errors.ErrorCode;
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
@Table(name = "access_attempts")
public class AccessAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketId;

    @Column(nullable = false)
    private String enteredTicketCode;

    @Column(nullable = false)
    private Long readerId;

    @Column(nullable = false)
    private Long gateId;

    @Column(nullable = false)
    private String sessionId; // Changed from Long to String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptResult result;

    @Enumerated(EnumType.STRING)
    private ErrorCode errorCode;

    @Column(nullable = false)
    private OffsetDateTime attemptedAt;
}
