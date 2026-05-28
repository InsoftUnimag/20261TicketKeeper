package com.empresa.ingreso.application.port.in;

public record AssignGateCommand(String sessionId, Long gateId, String ticketCategory, String zone) {}
