package com.empresa.ingreso.application.port.out;

import com.empresa.ingreso.domain.model.AccessAttempt;

public interface SaveAccessAttemptPort {

    AccessAttempt save(AccessAttempt accessAttempt);
}
