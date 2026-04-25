package com.empresa.ingreso.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "gate_assignments")
public class GateAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long sessionId;
    @Column(nullable = false)
    private Long gateId;
    @Column(nullable = false)
    private String ticketCategory;
    @Column(nullable = false)
    private String zone;
    @Column(nullable = false)
    private boolean active;
}
