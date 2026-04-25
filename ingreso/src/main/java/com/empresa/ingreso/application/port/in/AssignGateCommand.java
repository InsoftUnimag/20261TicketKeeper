package com.empresa.ingreso.application.port.in;

public record AssignGateCommand(Long sessionId, Long gateId, String ticketCategory, String zone) {}
