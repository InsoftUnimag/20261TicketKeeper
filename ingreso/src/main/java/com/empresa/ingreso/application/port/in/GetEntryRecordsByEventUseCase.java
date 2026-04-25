package com.empresa.ingreso.application.port.in;

import java.util.List;

public interface GetEntryRecordsByEventUseCase { List<GetEntryRecordByTicketResult> execute(Long eventId); }
