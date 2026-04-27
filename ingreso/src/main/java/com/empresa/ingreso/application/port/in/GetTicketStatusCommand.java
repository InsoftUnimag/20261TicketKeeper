package com.empresa.ingreso.application.port.in;

public record GetTicketStatusCommand(String ticketCode, String requestedBy) {}
